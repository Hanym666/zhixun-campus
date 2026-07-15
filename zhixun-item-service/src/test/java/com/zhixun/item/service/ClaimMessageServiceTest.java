package com.zhixun.item.service;

import com.zhixun.common.enums.ClaimStatus;
import com.zhixun.common.enums.NotificationType;
import com.zhixun.common.exception.BusinessException;
import com.zhixun.item.domain.ClaimMessage;
import com.zhixun.item.domain.ClaimRequest;
import com.zhixun.item.dto.SendClaimMessageRequest;
import com.zhixun.item.mapper.ClaimMessageMapper;
import com.zhixun.item.mapper.ClaimRequestMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClaimMessageServiceTest {

    @Mock ClaimMessageMapper messageMapper;
    @Mock ClaimRequestMapper claimMapper;
    @Mock NotificationService notificationService;

    private ClaimMessageService service;

    @BeforeEach
    void setUp() {
        service = new ClaimMessageService(messageMapper, claimMapper, notificationService);
    }

    @Test
    void claimantCanSendMessageToPublisher() {
        when(claimMapper.findById(3L)).thenReturn(Optional.of(claim()));
        doAnswer(invocation -> {
            ClaimMessage message = invocation.getArgument(0);
            message.setId(9L);
            return 1;
        }).when(messageMapper).insert(any(ClaimMessage.class));

        var response = service.send(3L, 8L, new SendClaimMessageRequest("请问在哪里交接？"));

        assertThat(response.id()).isEqualTo(9L);
        verify(notificationService).send(eq(7L), eq(NotificationType.CLAIM_MESSAGE),
                any(), any(), eq("CLAIM"), eq(3L));
    }

    @Test
    void unrelatedUserCannotReadMessages() {
        when(claimMapper.findById(3L)).thenReturn(Optional.of(claim()));

        assertThatThrownBy(() -> service.list(3L, 99L, null, 50))
                .isInstanceOf(BusinessException.class);
    }

    private ClaimRequest claim() {
        ClaimRequest claim = new ClaimRequest();
        claim.setId(3L);
        claim.setClaimantId(8L);
        claim.setPostPublisherId(7L);
        claim.setPostTitle("拾到校园卡");
        claim.setStatus(ClaimStatus.PENDING);
        return claim;
    }
}
