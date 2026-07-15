package com.zhixun.item.event;

public record ItemIndexEvent(Long postId, Action action) {
    public enum Action {
        UPSERT,
        DELETE
    }
}
