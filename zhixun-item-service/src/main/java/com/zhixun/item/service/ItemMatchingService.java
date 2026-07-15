package com.zhixun.item.service;

import com.zhixun.common.enums.ItemStatus;
import com.zhixun.common.enums.ItemType;
import com.zhixun.common.enums.NotificationType;
import com.zhixun.common.exception.BusinessException;
import com.zhixun.item.ai.AiVectorClient;
import com.zhixun.item.ai.VectorCandidate;
import com.zhixun.item.domain.ItemPost;
import com.zhixun.item.domain.MatchRecord;
import com.zhixun.item.dto.ItemMatchResponse;
import com.zhixun.item.event.ItemIndexEvent;
import com.zhixun.item.exception.ItemResultCode;
import com.zhixun.item.mapper.ItemPostMapper;
import com.zhixun.item.mapper.MatchRecordMapper;
import com.zhixun.item.search.ItemSearchIndexService;
import com.zhixun.item.search.KeywordCandidate;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ItemMatchingService {

    private final ItemPostMapper postMapper;
    private final MatchRecordMapper matchRecordMapper;
    private final ItemSearchIndexService searchIndexService;
    private final AiVectorClient aiVectorClient;
    private final NotificationService notificationService;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public List<ItemMatchResponse> findMatches(Long postId, Long requesterId, int limit) {
        ItemPost source = requireOwnedSearching(postId, requesterId);
        int safeLimit = Math.min(Math.max(limit, 1), 20);
        List<KeywordCandidate> keywordCandidates;
        try {
            keywordCandidates = searchIndexService.search(source, safeLimit * 2);
        } catch (Exception exception) {
            keywordCandidates = List.of();
        }
        List<VectorCandidate> vectorCandidates = aiVectorClient.search(source, safeLimit * 2);
        boolean vectorAvailable = !vectorCandidates.isEmpty();

        Map<Long, Scores> scores = new HashMap<>();
        keywordCandidates.forEach(candidate -> scores
                .computeIfAbsent(candidate.postId(), ignored -> new Scores())
                .keyword = clamp(candidate.score()));
        vectorCandidates.forEach(candidate -> scores
                .computeIfAbsent(candidate.postId(), ignored -> new Scores())
                .vector = clamp(candidate.score()));

        List<ItemMatchResponse> results = new ArrayList<>();
        for (Map.Entry<Long, Scores> entry : scores.entrySet()) {
            postMapper.findById(entry.getKey()).ifPresent(candidate -> {
                if (isValidCandidate(source, candidate)) {
                    Scores value = entry.getValue();
                    value.finalScore = vectorAvailable
                            ? value.keyword * 0.55 + value.vector * 0.45
                            : value.keyword;
                    persist(source.getId(), candidate.getId(), value);
                    results.add(toResponse(candidate, value));
                }
            });
        }
        results.sort(java.util.Comparator.comparingDouble(ItemMatchResponse::finalScore).reversed());
        List<ItemMatchResponse> limited = results.stream().limit(safeLimit).toList();
        notifyStrongest(source, limited);
        return limited;
    }

    @Transactional(readOnly = true)
    public void requestReindex(Long postId, Long requesterId) {
        requireOwnedSearching(postId, requesterId);
        eventPublisher.publishEvent(new ItemIndexEvent(postId, ItemIndexEvent.Action.UPSERT));
    }

    private ItemPost requireOwnedSearching(Long postId, Long requesterId) {
        ItemPost post = postMapper.findById(postId)
                .orElseThrow(() -> new BusinessException(ItemResultCode.ITEM_NOT_FOUND));
        if (!requesterId.equals(post.getPublisherId())) {
            throw new BusinessException(ItemResultCode.ITEM_OPERATION_FORBIDDEN);
        }
        if (post.getStatus() != ItemStatus.SEARCHING) {
            throw new BusinessException(ItemResultCode.ITEM_STATUS_INVALID);
        }
        return post;
    }

    private boolean isValidCandidate(ItemPost source, ItemPost candidate) {
        ItemType target = source.getItemType() == ItemType.LOST ? ItemType.FOUND : ItemType.LOST;
        return candidate.getItemType() == target
                && candidate.getStatus() == ItemStatus.SEARCHING
                && !source.getPublisherId().equals(candidate.getPublisherId());
    }

    private void persist(Long sourceId, Long targetId, Scores scores) {
        MatchRecord record = new MatchRecord();
        record.setSourcePostId(sourceId);
        record.setTargetPostId(targetId);
        record.setKeywordScore(decimal(scores.keyword));
        record.setVectorScore(decimal(scores.vector));
        record.setFinalScore(decimal(scores.finalScore));
        matchRecordMapper.upsert(record);
    }

    private void notifyStrongest(ItemPost source, List<ItemMatchResponse> matches) {
        if (matches.isEmpty() || matches.getFirst().finalScore() < 0.55) {
            return;
        }
        ItemMatchResponse strongest = matches.getFirst();
        if (matchRecordMapper.markNotified(source.getId(), strongest.postId()) == 1) {
            notificationService.send(source.getPublisherId(), NotificationType.MATCH_FOUND,
                    "发现可能匹配的物品", "系统为“" + source.getTitle() + "”发现了高相似候选",
                    "ITEM_MATCH", strongest.postId());
        }
    }

    private ItemMatchResponse toResponse(ItemPost post, Scores scores) {
        return new ItemMatchResponse(post.getId(), post.getPostNo(), post.getItemType(),
                post.getTitle(), post.getItemName(), post.getCategoryName(), post.getCampusArea(),
                post.getLocationName(), round(scores.keyword), round(scores.vector),
                round(scores.finalScore));
    }

    private BigDecimal decimal(double value) {
        return BigDecimal.valueOf(clamp(value)).setScale(5, RoundingMode.HALF_UP);
    }

    private double round(double value) {
        return decimal(value).doubleValue();
    }

    private double clamp(double value) {
        return Math.max(0.0, Math.min(value, 1.0));
    }

    private static class Scores {
        private double keyword;
        private double vector;
        private double finalScore;
    }
}
