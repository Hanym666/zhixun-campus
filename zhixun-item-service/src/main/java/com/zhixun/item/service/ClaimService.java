package com.zhixun.item.service;

import com.zhixun.common.api.PageResult;
import com.zhixun.common.enums.ClaimStatus;
import com.zhixun.common.enums.ItemStatus;
import com.zhixun.common.enums.ItemType;
import com.zhixun.common.enums.NotificationType;
import com.zhixun.common.exception.BusinessException;
import com.zhixun.item.domain.ClaimRequest;
import com.zhixun.item.domain.ItemPost;
import com.zhixun.item.dto.ClaimResponse;
import com.zhixun.item.dto.CreateClaimRequest;
import com.zhixun.item.dto.ReviewClaimRequest;
import com.zhixun.item.exception.ItemResultCode;
import com.zhixun.item.mapper.ClaimRequestMapper;
import com.zhixun.item.mapper.ItemPostMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.context.ApplicationEventPublisher;
import com.zhixun.item.event.ItemIndexEvent;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;

@Service
@RequiredArgsConstructor
public class ClaimService {

    private static final char[] NO_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789".toCharArray();
    private static final SecureRandom RANDOM = new SecureRandom();

    private final ClaimRequestMapper claimMapper;
    private final ItemPostMapper postMapper;
    private final NotificationService notificationService;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public ClaimResponse create(Long claimantId, CreateClaimRequest request) {
        ItemPost post = postMapper.findById(request.postId())
                .orElseThrow(() -> new BusinessException(ItemResultCode.ITEM_NOT_FOUND));
        if (post.getItemType() != ItemType.FOUND || post.getStatus() != ItemStatus.SEARCHING) {
            throw new BusinessException(ItemResultCode.ITEM_STATUS_INVALID,
                    "只有正在寻找失主的招领信息可以提交认领申请");
        }
        if (claimantId.equals(post.getPublisherId())) {
            throw new BusinessException(ItemResultCode.CLAIM_OWN_POST_FORBIDDEN);
        }
        if (claimMapper.existsActive(post.getId(), claimantId)) {
            throw new BusinessException(ItemResultCode.CLAIM_DUPLICATED);
        }

        ClaimRequest claim = new ClaimRequest();
        claim.setClaimNo(generateClaimNo());
        claim.setPostId(post.getId());
        claim.setClaimantId(claimantId);
        claim.setEvidence(request.evidence().trim());
        claim.setStatus(ClaimStatus.PENDING);
        claimMapper.insert(claim);

        notificationService.send(post.getPublisherId(), NotificationType.CLAIM_SUBMITTED,
                "收到新的认领申请", "“" + post.getTitle() + "”收到了一条认领申请",
                "CLAIM", claim.getId());
        return toResponse(requireClaim(claim.getId()));
    }

    @Transactional(readOnly = true)
    public PageResult<ClaimResponse> findMine(Long claimantId, int page, int size) {
        return findPage(claimantId, null, page, size);
    }

    @Transactional(readOnly = true)
    public PageResult<ClaimResponse> findReceived(Long publisherId, int page, int size) {
        return findPage(null, publisherId, page, size);
    }

    @Transactional
    public ClaimResponse review(Long id, Long publisherId, ReviewClaimRequest request) {
        ClaimRequest claim = requirePublisher(id, publisherId);
        if (claim.getStatus() != ClaimStatus.PENDING) {
            throw new BusinessException(ItemResultCode.CLAIM_STATUS_INVALID);
        }

        String comment = clean(request.reviewComment());
        if (Boolean.TRUE.equals(request.approved())) {
            if (request.handoverAt() == null || clean(request.handoverLocation()) == null) {
                throw new BusinessException("通过申请时必须填写交接时间和地点");
            }
            if (postMapper.startClaimVerification(claim.getPostId()) != 1) {
                throw new BusinessException(ItemResultCode.ITEM_STATUS_INVALID);
            }
            if (claimMapper.approve(id, publisherId, comment,
                    request.handoverLocation().trim(), request.handoverAt()) != 1) {
                throw new BusinessException(ItemResultCode.CLAIM_STATUS_INVALID);
            }
            claimMapper.rejectOtherPending(claim.getPostId(), id, publisherId);
            eventPublisher.publishEvent(new ItemIndexEvent(
                    claim.getPostId(), ItemIndexEvent.Action.DELETE));
            notificationService.send(claim.getClaimantId(), NotificationType.CLAIM_APPROVED,
                    "认领申请已通过", "请按约定的时间和地点完成物品交接", "CLAIM", id);
        } else {
            if (claimMapper.reject(id, publisherId, comment) != 1) {
                throw new BusinessException(ItemResultCode.CLAIM_STATUS_INVALID);
            }
            notificationService.send(claim.getClaimantId(), NotificationType.CLAIM_REJECTED,
                    "认领申请未通过", comment == null ? "发布者未通过本次认领申请" : comment,
                    "CLAIM", id);
        }
        return toResponse(requireClaim(id));
    }

    @Transactional
    public ClaimResponse complete(Long id, Long publisherId) {
        ClaimRequest claim = requirePublisher(id, publisherId);
        if (claimMapper.complete(id) != 1) {
            throw new BusinessException(ItemResultCode.CLAIM_STATUS_INVALID);
        }
        if (postMapper.completeClaim(claim.getPostId(), publisherId) != 1) {
            throw new BusinessException(ItemResultCode.ITEM_STATUS_INVALID);
        }
        notificationService.send(claim.getClaimantId(), NotificationType.CLAIM_COMPLETED,
                "物品交接已完成", "发布者已确认物品交接完成", "CLAIM", id);
        return toResponse(requireClaim(id));
    }

    @Transactional
    public ClaimResponse cancel(Long id, Long claimantId) {
        ClaimRequest claim = requireClaim(id);
        if (!claimantId.equals(claim.getClaimantId())) {
            throw new BusinessException(ItemResultCode.CLAIM_OPERATION_FORBIDDEN);
        }
        if (claimMapper.cancel(id, claimantId) != 1) {
            throw new BusinessException(ItemResultCode.CLAIM_STATUS_INVALID);
        }
        return toResponse(requireClaim(id));
    }

    private PageResult<ClaimResponse> findPage(Long claimantId, Long publisherId,
                                               int page, int size) {
        int safePage = Math.max(page, 1);
        int safeSize = Math.min(Math.max(size, 1), 100);
        long total = claimMapper.countPage(claimantId, publisherId);
        var records = claimMapper.findPage(claimantId, publisherId,
                        (long) (safePage - 1) * safeSize, safeSize)
                .stream().map(this::toResponse).toList();
        return PageResult.of(records, total, safePage, safeSize);
    }

    private ClaimRequest requireClaim(Long id) {
        return claimMapper.findById(id)
                .orElseThrow(() -> new BusinessException(ItemResultCode.CLAIM_NOT_FOUND));
    }

    private ClaimRequest requirePublisher(Long id, Long publisherId) {
        ClaimRequest claim = requireClaim(id);
        if (!publisherId.equals(claim.getPostPublisherId())) {
            throw new BusinessException(ItemResultCode.CLAIM_OPERATION_FORBIDDEN);
        }
        return claim;
    }

    private ClaimResponse toResponse(ClaimRequest claim) {
        return new ClaimResponse(claim.getId(), claim.getClaimNo(), claim.getPostId(),
                claim.getPostTitle(), claim.getPostPublisherId(), claim.getClaimantId(),
                claim.getEvidence(), claim.getStatus(), claim.getReviewerId(),
                claim.getReviewComment(), claim.getReviewedAt(), claim.getHandoverLocation(),
                claim.getHandoverAt(), claim.getCompletedAt(), claim.getCreatedAt(),
                claim.getUpdatedAt());
    }

    private String generateClaimNo() {
        StringBuilder suffix = new StringBuilder(6);
        for (int i = 0; i < 6; i++) {
            suffix.append(NO_CHARS[RANDOM.nextInt(NO_CHARS.length)]);
        }
        return "CL" + System.currentTimeMillis() + suffix;
    }

    private String clean(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
