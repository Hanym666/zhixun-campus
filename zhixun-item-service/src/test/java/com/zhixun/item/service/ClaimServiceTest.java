package com.zhixun.item.service;

import com.zhixun.common.enums.ClaimStatus;
import com.zhixun.common.enums.ItemStatus;
import com.zhixun.common.enums.ItemType;
import com.zhixun.common.enums.NotificationType;
import com.zhixun.common.exception.BusinessException;
import com.zhixun.item.domain.ClaimRequest;
import com.zhixun.item.domain.ItemPost;
import com.zhixun.item.dto.CreateClaimRequest;
import com.zhixun.item.dto.ReviewClaimRequest;
import com.zhixun.item.exception.ItemResultCode;
import com.zhixun.item.mapper.ClaimRequestMapper;
import com.zhixun.item.mapper.ItemPostMapper;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClaimServiceTest {

    @Mock
    private ClaimRequestMapper claimMapper;
    @Mock
    private ItemPostMapper postMapper;
    @Mock
    private NotificationService notificationService;
    @Mock
    private ApplicationEventPublisher eventPublisher;

    private ClaimService service;

    @BeforeEach
    void setUp() {
        service = new ClaimService(claimMapper, postMapper, notificationService, eventPublisher);
    }

    @Test
    void rejectsClaimingOwnPost() {
        when(postMapper.findById(10L)).thenReturn(Optional.of(foundPost(7L)));

        assertThatThrownBy(() -> service.create(7L,
                new CreateClaimRequest(10L, "卡套背面有独特贴纸")))
                .isInstanceOf(BusinessException.class)
                .satisfies(exception -> assertThat(((BusinessException) exception).getCode())
                        .isEqualTo(ItemResultCode.CLAIM_OWN_POST_FORBIDDEN.code()));
    }

    @Test
    void createsPendingClaimAndNotifiesPublisher() {
        AtomicReference<ClaimRequest> inserted = new AtomicReference<>();
        when(postMapper.findById(10L)).thenReturn(Optional.of(foundPost(7L)));
        when(claimMapper.existsActive(10L, 8L)).thenReturn(false);
        doAnswer(invocation -> {
            ClaimRequest claim = invocation.getArgument(0);
            claim.setId(21L);
            claim.setPostTitle("拾到校园卡");
            claim.setPostPublisherId(7L);
            claim.setCreatedAt(LocalDateTime.now());
            claim.setUpdatedAt(LocalDateTime.now());
            inserted.set(claim);
            return 1;
        }).when(claimMapper).insert(any(ClaimRequest.class));
        when(claimMapper.findById(21L)).thenAnswer(ignored -> Optional.of(inserted.get()));

        var response = service.create(8L,
                new CreateClaimRequest(10L, "卡套背面有独特贴纸"));

        assertThat(response.status()).isEqualTo(ClaimStatus.PENDING);
        verify(notificationService).send(eq(7L), eq(NotificationType.CLAIM_SUBMITTED),
                any(), any(), eq("CLAIM"), eq(21L));
    }

    @Test
    void publisherApprovesClaimAndStartsVerification() {
        ClaimRequest pending = claim(ClaimStatus.PENDING);
        ClaimRequest approved = claim(ClaimStatus.APPROVED);
        when(claimMapper.findById(21L)).thenReturn(Optional.of(pending), Optional.of(approved));
        when(postMapper.startClaimVerification(10L)).thenReturn(1);
        when(claimMapper.approve(eq(21L), eq(7L), any(), any(), any())).thenReturn(1);

        var response = service.review(21L, 7L, new ReviewClaimRequest(true, "信息吻合",
                "东门服务台", LocalDateTime.now().plusDays(1)));

        assertThat(response.status()).isEqualTo(ClaimStatus.APPROVED);
        verify(claimMapper).rejectOtherPending(10L, 21L, 7L);
        verify(notificationService).send(eq(8L), eq(NotificationType.CLAIM_APPROVED),
                any(), any(), eq("CLAIM"), eq(21L));
    }

    @Test
    void publisherCompletesClaimAndPostTogether() {
        ClaimRequest approved = claim(ClaimStatus.APPROVED);
        ClaimRequest completed = claim(ClaimStatus.COMPLETED);
        when(claimMapper.findById(21L)).thenReturn(Optional.of(approved), Optional.of(completed));
        when(claimMapper.complete(21L)).thenReturn(1);
        when(postMapper.completeClaim(10L, 7L)).thenReturn(1);

        var response = service.complete(21L, 7L);

        assertThat(response.status()).isEqualTo(ClaimStatus.COMPLETED);
        verify(notificationService).send(eq(8L), eq(NotificationType.CLAIM_COMPLETED),
                any(), any(), eq("CLAIM"), eq(21L));
    }

    private ItemPost foundPost(Long publisherId) {
        ItemPost post = new ItemPost();
        post.setId(10L);
        post.setPublisherId(publisherId);
        post.setItemType(ItemType.FOUND);
        post.setStatus(ItemStatus.SEARCHING);
        post.setTitle("拾到校园卡");
        return post;
    }

    private ClaimRequest claim(ClaimStatus status) {
        ClaimRequest claim = new ClaimRequest();
        claim.setId(21L);
        claim.setClaimNo("CLTEST");
        claim.setPostId(10L);
        claim.setPostTitle("拾到校园卡");
        claim.setPostPublisherId(7L);
        claim.setClaimantId(8L);
        claim.setEvidence("卡套背面有独特贴纸");
        claim.setStatus(status);
        claim.setCreatedAt(LocalDateTime.now());
        claim.setUpdatedAt(LocalDateTime.now());
        return claim;
    }
}
