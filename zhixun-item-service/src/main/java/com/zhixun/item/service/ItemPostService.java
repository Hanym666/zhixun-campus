package com.zhixun.item.service;

import com.zhixun.common.api.PageResult;
import com.zhixun.common.enums.ItemStatus;
import com.zhixun.common.enums.ItemType;
import com.zhixun.common.exception.BusinessException;
import com.zhixun.item.domain.ItemPost;
import com.zhixun.item.dto.CreateItemPostRequest;
import com.zhixun.item.dto.ItemPostResponse;
import com.zhixun.item.dto.UpdateItemPostRequest;
import com.zhixun.item.exception.ItemResultCode;
import com.zhixun.item.event.ItemIndexEvent;
import com.zhixun.item.mapper.ItemCategoryMapper;
import com.zhixun.item.mapper.ItemImageMapper;
import com.zhixun.item.dto.ItemImageResponse;
import com.zhixun.item.mapper.ItemPostMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.context.ApplicationEventPublisher;

import java.security.SecureRandom;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ItemPostService {

    private static final char[] POST_NO_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789".toCharArray();
    private static final SecureRandom RANDOM = new SecureRandom();

    private final ItemPostMapper postMapper;
    private final ItemCategoryMapper categoryMapper;
    private final ApplicationEventPublisher eventPublisher;
    private final ItemImageMapper imageMapper;

    @Transactional
    public ItemPostResponse create(Long publisherId, CreateItemPostRequest request) {
        ensureCategory(request.categoryId());
        ItemPost post = new ItemPost();
        post.setPostNo(generatePostNo());
        post.setPublisherId(publisherId);
        post.setItemType(request.itemType());
        post.setStatus(request.shouldPublishNow() ? ItemStatus.SEARCHING : ItemStatus.DRAFT);
        applyRequest(post, request);
        post.setPublishedAt(request.shouldPublishNow() ? LocalDateTime.now() : null);
        post.setCreatedBy(publisherId);
        post.setUpdatedBy(publisherId);
        postMapper.insert(post);
        if (request.shouldPublishNow()) {
            publishIndexEvent(post.getId(), ItemIndexEvent.Action.UPSERT);
        }
        return toResponse(requirePost(post.getId()), publisherId);
    }

    @Transactional(readOnly = true)
    public PageResult<ItemPostResponse> findPublic(ItemType itemType, Long categoryId,
                                                   String keyword, int page, int size) {
        int safePage = Math.max(page, 1);
        int safeSize = Math.min(Math.max(size, 1), 100);
        String cleanKeyword = clean(keyword);
        long total = postMapper.countPage(itemType, categoryId, cleanKeyword,
                ItemStatus.SEARCHING, null);
        var posts = postMapper.findPage(itemType, categoryId, cleanKeyword,
                        ItemStatus.SEARCHING, null, (long) (safePage - 1) * safeSize, safeSize)
                .stream().map(post -> toResponse(post, null)).toList();
        return PageResult.of(posts, total, safePage, safeSize);
    }

    @Transactional(readOnly = true)
    public PageResult<ItemPostResponse> findMine(Long publisherId, ItemStatus status,
                                                 int page, int size) {
        int safePage = Math.max(page, 1);
        int safeSize = Math.min(Math.max(size, 1), 100);
        long total = postMapper.countPage(null, null, null, status, publisherId);
        var posts = postMapper.findPage(null, null, null, status, publisherId,
                        (long) (safePage - 1) * safeSize, safeSize)
                .stream().map(post -> toResponse(post, publisherId)).toList();
        return PageResult.of(posts, total, safePage, safeSize);
    }

    @Transactional
    public ItemPostResponse detail(Long id, Long requesterId) {
        ItemPost post = requirePost(id);
        boolean owner = requesterId != null && requesterId.equals(post.getPublisherId());
        if (post.getStatus() == ItemStatus.DRAFT && !owner) {
            throw new BusinessException(ItemResultCode.ITEM_NOT_FOUND);
        }
        if (!owner) {
            postMapper.incrementViewCount(id);
            post.setViewCount(post.getViewCount() + 1);
        }
        return toResponse(post, requesterId);
    }

    @Transactional
    public ItemPostResponse update(Long id, Long publisherId, UpdateItemPostRequest request) {
        ItemPost existing = requireOwned(id, publisherId);
        ensureEditable(existing);
        ensureCategory(request.categoryId());
        applyRequest(existing, request);
        if (postMapper.updateOwned(existing, publisherId) != 1) {
            throw new BusinessException(ItemResultCode.ITEM_STATUS_INVALID);
        }
        if (existing.getStatus() == ItemStatus.SEARCHING) {
            publishIndexEvent(id, ItemIndexEvent.Action.UPSERT);
        }
        return toResponse(requirePost(id), publisherId);
    }

    @Transactional
    public ItemPostResponse publish(Long id, Long publisherId) {
        requireOwned(id, publisherId);
        if (postMapper.publishOwned(id, publisherId) != 1) {
            throw new BusinessException(ItemResultCode.ITEM_STATUS_INVALID);
        }
        publishIndexEvent(id, ItemIndexEvent.Action.UPSERT);
        return toResponse(requirePost(id), publisherId);
    }

    @Transactional
    public void delete(Long id, Long publisherId) {
        requireOwned(id, publisherId);
        if (postMapper.softDeleteOwned(id, publisherId) != 1) {
            throw new BusinessException(ItemResultCode.ITEM_STATUS_INVALID);
        }
        publishIndexEvent(id, ItemIndexEvent.Action.DELETE);
    }

    private ItemPost requirePost(Long id) {
        return postMapper.findById(id)
                .orElseThrow(() -> new BusinessException(ItemResultCode.ITEM_NOT_FOUND));
    }

    private ItemPost requireOwned(Long id, Long publisherId) {
        ItemPost post = requirePost(id);
        if (!publisherId.equals(post.getPublisherId())) {
            throw new BusinessException(ItemResultCode.ITEM_OPERATION_FORBIDDEN);
        }
        return post;
    }

    private void ensureEditable(ItemPost post) {
        if (post.getStatus() != ItemStatus.DRAFT && post.getStatus() != ItemStatus.SEARCHING) {
            throw new BusinessException(ItemResultCode.ITEM_STATUS_INVALID);
        }
    }

    private void ensureCategory(Long categoryId) {
        if (!categoryMapper.existsEnabledById(categoryId)) {
            throw new BusinessException(ItemResultCode.CATEGORY_NOT_FOUND);
        }
    }

    private void applyRequest(ItemPost post, CreateItemPostRequest request) {
        post.setTitle(request.title().trim());
        post.setCategoryId(request.categoryId());
        post.setItemName(request.itemName().trim());
        post.setDescription(request.description().trim());
        post.setPublicFeatures(clean(request.publicFeatures()));
        post.setPrivateFeatures(clean(request.privateFeatures()));
        post.setOccurredAt(request.occurredAt());
        post.setCampusArea(clean(request.campusArea()));
        post.setLocationName(clean(request.locationName()));
        post.setColor(clean(request.color()));
        post.setBrand(clean(request.brand()));
        post.setContactHint(clean(request.contactHint()));
    }

    private void applyRequest(ItemPost post, UpdateItemPostRequest request) {
        post.setTitle(request.title().trim());
        post.setCategoryId(request.categoryId());
        post.setItemName(request.itemName().trim());
        post.setDescription(request.description().trim());
        post.setPublicFeatures(clean(request.publicFeatures()));
        post.setPrivateFeatures(clean(request.privateFeatures()));
        post.setOccurredAt(request.occurredAt());
        post.setCampusArea(clean(request.campusArea()));
        post.setLocationName(clean(request.locationName()));
        post.setColor(clean(request.color()));
        post.setBrand(clean(request.brand()));
        post.setContactHint(clean(request.contactHint()));
    }

    private ItemPostResponse toResponse(ItemPost post, Long requesterId) {
        boolean owner = requesterId != null && requesterId.equals(post.getPublisherId());
        return new ItemPostResponse(post.getId(), post.getPostNo(), post.getPublisherId(),
                post.getItemType(), post.getStatus(), post.getTitle(), post.getCategoryId(),
                post.getCategoryName(), post.getItemName(), post.getDescription(),
                post.getPublicFeatures(), owner ? post.getPrivateFeatures() : null,
                post.getOccurredAt(), post.getCampusArea(), post.getLocationName(),
                post.getColor(), post.getBrand(), post.getContactHint(), post.getViewCount(),
                post.getPublishedAt(), post.getCreatedAt(), post.getUpdatedAt(),
                imageMapper.findByPostId(post.getId()).stream()
                        .map(image -> new ItemImageResponse(image.getId(), image.getImageUrl(),
                                image.getSortOrder(), image.getCoverImage()))
                        .toList());
    }

    private String generatePostNo() {
        StringBuilder suffix = new StringBuilder(6);
        for (int i = 0; i < 6; i++) {
            suffix.append(POST_NO_CHARS[RANDOM.nextInt(POST_NO_CHARS.length)]);
        }
        return "ZX" + System.currentTimeMillis() + suffix;
    }

    private String clean(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    private void publishIndexEvent(Long postId, ItemIndexEvent.Action action) {
        eventPublisher.publishEvent(new ItemIndexEvent(postId, action));
    }
}
