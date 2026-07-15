package com.zhixun.item.ai;

import com.zhixun.common.enums.ItemType;
import com.zhixun.item.domain.ItemPost;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.stream.Stream;

@Component
public class AiVectorClient {

    private static final Logger log = LoggerFactory.getLogger(AiVectorClient.class);

    private final RestClient restClient;

    public AiVectorClient(RestClient.Builder builder,
                          @Value("${zhixun.ai.base-url}") String baseUrl) {
        this.restClient = builder.baseUrl(baseUrl).build();
    }

    public void indexSafely(ItemPost post) {
        try {
            restClient.post().uri("/api/ai/items/index")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(new IndexRequest(post.getId(), post.getItemType().name(),
                            post.getCategoryId(), content(post), post.getCampusArea()))
                    .retrieve().toBodilessEntity();
        } catch (RuntimeException exception) {
            log.warn("AI vector indexing unavailable for post {}", post.getId());
        }
    }

    public List<VectorCandidate> search(ItemPost post, int topK) {
        ItemType target = post.getItemType() == ItemType.LOST ? ItemType.FOUND : ItemType.LOST;
        try {
            List<VectorCandidate> response = restClient.post().uri("/api/ai/items/search")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(new SearchRequest(post.getId(), target.name(), content(post), topK))
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {
                    });
            return response == null ? List.of() : response;
        } catch (RuntimeException exception) {
            log.warn("AI vector search unavailable for post {}", post.getId());
            return List.of();
        }
    }

    public void deleteSafely(Long postId) {
        try {
            restClient.delete().uri("/api/ai/items/{postId}", postId)
                    .retrieve().toBodilessEntity();
        } catch (RuntimeException exception) {
            log.warn("AI vector deletion unavailable for post {}", postId);
        }
    }

    private String content(ItemPost post) {
        return String.join(" ", Stream.of(post.getTitle(), post.getItemName(), post.getDescription(),
                        post.getPublicFeatures(), post.getCampusArea(), post.getLocationName(),
                        post.getColor(), post.getBrand())
                .filter(value -> value != null && !value.isBlank()).toList());
    }

    private record IndexRequest(Long postId, String itemType, Long categoryId,
                                String content, String campusArea) {
    }

    private record SearchRequest(Long postId, String targetItemType, String content, int topK) {
    }
}
