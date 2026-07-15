package com.zhixun.item.service;

import com.zhixun.common.enums.ItemStatus;
import com.zhixun.common.enums.ItemType;
import com.zhixun.common.exception.BusinessException;
import com.zhixun.item.ai.AiVectorClient;
import com.zhixun.item.ai.VectorCandidate;
import com.zhixun.item.domain.ItemPost;
import com.zhixun.item.mapper.ItemPostMapper;
import com.zhixun.item.mapper.MatchRecordMapper;
import com.zhixun.item.search.ItemSearchIndexService;
import com.zhixun.item.search.KeywordCandidate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ItemMatchingServiceTest {

    @Mock ItemPostMapper postMapper;
    @Mock MatchRecordMapper matchRecordMapper;
    @Mock ItemSearchIndexService searchIndexService;
    @Mock AiVectorClient aiVectorClient;
    @Mock NotificationService notificationService;
    @Mock ApplicationEventPublisher eventPublisher;

    private ItemMatchingService service;

    @BeforeEach
    void setUp() {
        service = new ItemMatchingService(postMapper, matchRecordMapper, searchIndexService,
                aiVectorClient, notificationService, eventPublisher);
    }

    @Test
    void mergesKeywordAndVectorScores() throws Exception {
        ItemPost source = post(1L, 7L, ItemType.LOST, "丢失蓝色校园卡");
        ItemPost target = post(2L, 8L, ItemType.FOUND, "拾到蓝色校园卡");
        when(postMapper.findById(1L)).thenReturn(Optional.of(source));
        when(postMapper.findById(2L)).thenReturn(Optional.of(target));
        when(searchIndexService.search(source, 20))
                .thenReturn(List.of(new KeywordCandidate(2L, 0.8)));
        when(aiVectorClient.search(source, 20))
                .thenReturn(List.of(new VectorCandidate(2L, 0.6)));
        when(matchRecordMapper.markNotified(1L, 2L)).thenReturn(0);

        var matches = service.findMatches(1L, 7L, 10);

        assertThat(matches).hasSize(1);
        assertThat(matches.getFirst().finalScore()).isEqualTo(0.71);
        assertThat(matches.getFirst().keywordScore()).isEqualTo(0.8);
        assertThat(matches.getFirst().vectorScore()).isEqualTo(0.6);
    }

    @Test
    void rejectsMatchingAnotherUsersPost() {
        when(postMapper.findById(1L))
                .thenReturn(Optional.of(post(1L, 7L, ItemType.LOST, "校园卡")));

        assertThatThrownBy(() -> service.findMatches(1L, 9L, 10))
                .isInstanceOf(BusinessException.class);
    }

    private ItemPost post(Long id, Long publisherId, ItemType type, String title) {
        ItemPost post = new ItemPost();
        post.setId(id);
        post.setPostNo("ZX" + id);
        post.setPublisherId(publisherId);
        post.setItemType(type);
        post.setStatus(ItemStatus.SEARCHING);
        post.setTitle(title);
        post.setItemName("校园卡");
        post.setDescription(title);
        post.setCategoryId(2L);
        post.setCategoryName("证件卡片");
        post.setCampusArea("东校区");
        return post;
    }
}
