package com.zhixun.item.event;

import com.zhixun.item.ai.AiVectorClient;
import com.zhixun.item.mapper.ItemPostMapper;
import com.zhixun.item.search.ItemSearchIndexService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class ItemIndexEventListener {

    private final ItemPostMapper postMapper;
    private final ItemSearchIndexService searchIndexService;
    private final AiVectorClient aiVectorClient;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(ItemIndexEvent event) {
        if (event.action() == ItemIndexEvent.Action.DELETE) {
            searchIndexService.deleteSafely(event.postId());
            aiVectorClient.deleteSafely(event.postId());
            return;
        }
        postMapper.findById(event.postId()).ifPresent(post -> {
            searchIndexService.indexSafely(post);
            aiVectorClient.indexSafely(post);
        });
    }
}
