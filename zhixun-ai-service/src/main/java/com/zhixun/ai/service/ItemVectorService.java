package com.zhixun.ai.service;

import com.zhixun.ai.dto.ItemFeatures;
import com.zhixun.ai.dto.ItemVectorIndexRequest;
import com.zhixun.ai.dto.ItemVectorSearchRequest;
import com.zhixun.ai.dto.VectorMatchResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ItemVectorService {

    private final VectorStore vectorStore;
    private final FeatureExtractionService featureExtractionService;

    public ItemFeatures index(ItemVectorIndexRequest request) {
        require(request.postId(), "postId");
        require(request.categoryId(), "categoryId");
        requireText(request.itemType(), "itemType");
        requireText(request.content(), "content");
        safeType(request.itemType());
        ItemFeatures features = featureExtractionService.extract(request.content());
        String vectorText = features.normalizedText() + " " + String.join(" ", features.keywords());
        Document document = Document.builder()
                .id(documentId(request.postId()))
                .text(vectorText)
                .metadata(Map.of(
                        "postId", request.postId(),
                        "itemType", request.itemType(),
                        "categoryId", request.categoryId(),
                        "campusArea", request.campusArea() == null ? "" : request.campusArea()
                ))
                .build();
        vectorStore.add(List.of(document));
        return features;
    }

    public List<VectorMatchResponse> search(ItemVectorSearchRequest request) {
        require(request.postId(), "postId");
        requireText(request.content(), "content");
        SearchRequest searchRequest = SearchRequest.builder()
                .query(request.content())
                .topK(Math.min(request.resolvedTopK() * 3, 50))
                .similarityThreshold(0.0)
                .build();
        safeType(request.targetItemType());
        return vectorStore.similaritySearch(searchRequest).stream()
                .filter(document -> !document.getId().equals(documentId(request.postId())))
                .limit(request.resolvedTopK())
                .map(document -> new VectorMatchResponse(
                        parseDocumentId(document.getId()),
                        document.getScore() == null ? 0.0 : document.getScore()
                ))
                .toList();
    }

    public void delete(Long postId) {
        vectorStore.delete(List.of(documentId(postId)));
    }

    private String documentId(Long postId) {
        return "item-" + postId;
    }

    private Long parseDocumentId(String documentId) {
        if (documentId == null || !documentId.startsWith("item-")) {
            throw new IllegalArgumentException("Unexpected vector document id: " + documentId);
        }
        return Long.valueOf(documentId.substring("item-".length()));
    }

    private String safeType(String itemType) {
        if (!"LOST".equals(itemType) && !"FOUND".equals(itemType)) {
            throw new IllegalArgumentException("itemType must be LOST or FOUND");
        }
        return itemType;
    }

    private void require(Object value, String field) {
        if (value == null) {
            throw new IllegalArgumentException(field + " must not be null");
        }
    }

    private void requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " must not be blank");
        }
    }
}
