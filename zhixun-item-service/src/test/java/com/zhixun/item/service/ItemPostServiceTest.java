package com.zhixun.item.service;

import com.zhixun.common.enums.ItemStatus;
import com.zhixun.common.enums.ItemType;
import com.zhixun.common.exception.BusinessException;
import com.zhixun.item.domain.ItemPost;
import com.zhixun.item.dto.CreateItemPostRequest;
import com.zhixun.item.dto.UpdateItemPostRequest;
import com.zhixun.item.exception.ItemResultCode;
import com.zhixun.item.mapper.ItemCategoryMapper;
import com.zhixun.item.mapper.ItemPostMapper;
import com.zhixun.item.mapper.ItemImageMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.context.ApplicationEventPublisher;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ItemPostServiceTest {

    @Mock
    private ItemPostMapper postMapper;
    @Mock
    private ItemCategoryMapper categoryMapper;
    @Mock
    private ApplicationEventPublisher eventPublisher;
    @Mock
    private ItemImageMapper imageMapper;

    private ItemPostService service;

    @BeforeEach
    void setUp() {
        service = new ItemPostService(postMapper, categoryMapper, eventPublisher, imageMapper);
        org.mockito.Mockito.lenient().when(imageMapper.findByPostId(org.mockito.ArgumentMatchers.anyLong()))
                .thenReturn(java.util.List.of());
    }

    @Test
    void createsPublishedPostAndReturnsPrivateFeaturesOnlyToOwner() {
        AtomicReference<ItemPost> inserted = new AtomicReference<>();
        when(categoryMapper.existsEnabledById(2L)).thenReturn(true);
        doAnswer(invocation -> {
            ItemPost post = invocation.getArgument(0);
            post.setId(99L);
            post.setCategoryName("证件卡片");
            post.setViewCount(0L);
            post.setCreatedAt(LocalDateTime.now());
            post.setUpdatedAt(LocalDateTime.now());
            inserted.set(post);
            return 1;
        }).when(postMapper).insert(any(ItemPost.class));
        when(postMapper.findById(99L)).thenAnswer(ignored -> Optional.of(inserted.get()));

        var response = service.create(7L, createRequest(true));

        assertThat(response.id()).isEqualTo(99L);
        assertThat(response.status()).isEqualTo(ItemStatus.SEARCHING);
        assertThat(response.privateFeatures()).isEqualTo("背面有姓名缩写");
        assertThat(response.postNo()).startsWith("ZX");
    }

    @Test
    void hidesPrivateFeaturesFromPublicViewerAndCountsView() {
        ItemPost post = existingPost(7L, ItemStatus.SEARCHING);
        when(postMapper.findById(12L)).thenReturn(Optional.of(post));

        var response = service.detail(12L, null);

        assertThat(response.privateFeatures()).isNull();
        assertThat(response.viewCount()).isEqualTo(1L);
        verify(postMapper).incrementViewCount(12L);
    }

    @Test
    void rejectsUpdateFromAnotherUser() {
        when(postMapper.findById(12L)).thenReturn(Optional.of(existingPost(7L, ItemStatus.DRAFT)));

        assertThatThrownBy(() -> service.update(12L, 8L, updateRequest()))
                .isInstanceOf(BusinessException.class)
                .satisfies(exception -> assertThat(((BusinessException) exception).getCode())
                        .isEqualTo(ItemResultCode.ITEM_OPERATION_FORBIDDEN.code()));
    }

    private CreateItemPostRequest createRequest(boolean publishNow) {
        return new CreateItemPostRequest(ItemType.LOST, "校园卡遗失", 2L, "校园卡",
                "在图书馆附近遗失", "蓝色卡套", "背面有姓名缩写",
                LocalDateTime.now().minusHours(1), "东校区", "图书馆", "蓝色",
                null, "请通过站内消息联系", publishNow);
    }

    private UpdateItemPostRequest updateRequest() {
        return new UpdateItemPostRequest("校园卡遗失", 2L, "校园卡", "描述",
                "公开特征", "私密特征", LocalDateTime.now().minusHours(1),
                "东校区", "图书馆", "蓝色", null, null);
    }

    private ItemPost existingPost(Long publisherId, ItemStatus status) {
        ItemPost post = new ItemPost();
        post.setId(12L);
        post.setPostNo("ZXTEST");
        post.setPublisherId(publisherId);
        post.setItemType(ItemType.LOST);
        post.setStatus(status);
        post.setTitle("校园卡遗失");
        post.setCategoryId(2L);
        post.setCategoryName("证件卡片");
        post.setItemName("校园卡");
        post.setDescription("描述");
        post.setPublicFeatures("蓝色卡套");
        post.setPrivateFeatures("背面有姓名缩写");
        post.setViewCount(0L);
        post.setCreatedAt(LocalDateTime.now());
        post.setUpdatedAt(LocalDateTime.now());
        return post;
    }
}
