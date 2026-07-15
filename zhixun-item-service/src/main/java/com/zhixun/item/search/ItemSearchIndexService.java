package com.zhixun.item.search;

import com.zhixun.common.enums.ItemStatus;
import com.zhixun.common.enums.ItemType;
import com.zhixun.item.domain.ItemPost;
import lombok.RequiredArgsConstructor;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ItemSearchIndexService {

    private static final Logger log = LoggerFactory.getLogger(ItemSearchIndexService.class);
    private static final String INDEX_MAPPING = """
            {
              "settings":{"number_of_shards":1,"number_of_replicas":0},
              "mappings":{"properties":{
                "postId":{"type":"long"},
                "itemType":{"type":"keyword"},
                "status":{"type":"keyword"},
                "categoryId":{"type":"long"},
                "title":{"type":"text"},
                "itemName":{"type":"text"},
                "description":{"type":"text"},
                "publicFeatures":{"type":"text"},
                "campusArea":{"type":"keyword"},
                "locationName":{"type":"text"},
                "color":{"type":"keyword"},
                "brand":{"type":"keyword"},
                "occurredAt":{"type":"date","format":"strict_date_optional_time||epoch_millis"}
              }}
            }
            """;

    private final RestHighLevelClient client;

    @Value("${zhixun.elasticsearch.item-index}")
    private String indexName;

    public void ensureIndex() throws IOException {
        if (!client.indices().exists(new GetIndexRequest(indexName), RequestOptions.DEFAULT)) {
            CreateIndexRequest request = new CreateIndexRequest(indexName);
            request.source(INDEX_MAPPING, XContentType.JSON);
            client.indices().create(request, RequestOptions.DEFAULT);
        }
    }

    public void index(ItemPost post) throws IOException {
        ensureIndex();
        Map<String, Object> source = new HashMap<>();
        source.put("postId", post.getId());
        source.put("itemType", post.getItemType().name());
        source.put("status", post.getStatus().name());
        source.put("categoryId", post.getCategoryId());
        put(source, "title", post.getTitle());
        put(source, "itemName", post.getItemName());
        put(source, "description", post.getDescription());
        put(source, "publicFeatures", post.getPublicFeatures());
        put(source, "campusArea", post.getCampusArea());
        put(source, "locationName", post.getLocationName());
        put(source, "color", post.getColor());
        put(source, "brand", post.getBrand());
        if (post.getOccurredAt() != null) {
            source.put("occurredAt", post.getOccurredAt().toString());
        }
        client.index(new IndexRequest(indexName).id(post.getId().toString()).source(source),
                RequestOptions.DEFAULT);
    }

    public List<KeywordCandidate> search(ItemPost sourcePost, int limit) throws IOException {
        ensureIndex();
        ItemType targetType = sourcePost.getItemType() == ItemType.LOST ? ItemType.FOUND : ItemType.LOST;
        BoolQueryBuilder query = QueryBuilders.boolQuery()
                .filter(QueryBuilders.termQuery("itemType", targetType.name()))
                .filter(QueryBuilders.termQuery("status", ItemStatus.SEARCHING.name()))
                .mustNot(QueryBuilders.termQuery("postId", sourcePost.getId()))
                .must(QueryBuilders.multiMatchQuery(searchText(sourcePost),
                        "title^3", "itemName^4", "description^1.5", "publicFeatures^2.5",
                        "locationName^2", "color^2", "brand^2"));
        query.should(QueryBuilders.termQuery("categoryId", sourcePost.getCategoryId()).boost(3));
        if (sourcePost.getCampusArea() != null) {
            query.should(QueryBuilders.termQuery("campusArea", sourcePost.getCampusArea()).boost(2));
        }
        SearchSourceBuilder source = new SearchSourceBuilder().query(query).size(limit);
        SearchResponse response = client.search(new SearchRequest(indexName).source(source),
                RequestOptions.DEFAULT);
        float maxScore = response.getHits().getMaxScore();
        return java.util.Arrays.stream(response.getHits().getHits())
                .map(hit -> new KeywordCandidate(Long.valueOf(hit.getId()), normalize(hit, maxScore)))
                .toList();
    }

    public void delete(Long postId) throws IOException {
        if (client.indices().exists(new GetIndexRequest(indexName), RequestOptions.DEFAULT)) {
            client.delete(new DeleteRequest(indexName, postId.toString()), RequestOptions.DEFAULT);
        }
    }

    public void indexSafely(ItemPost post) {
        try {
            index(post);
        } catch (Exception exception) {
            log.warn("Unable to index item post {}", post.getId(), exception);
        }
    }

    public void deleteSafely(Long postId) {
        try {
            delete(postId);
        } catch (Exception exception) {
            log.warn("Unable to delete item post {} from search index", postId, exception);
        }
    }

    private double normalize(SearchHit hit, float maxScore) {
        return maxScore > 0 && !Float.isNaN(maxScore) ? hit.getScore() / maxScore : 0.0;
    }

    private String searchText(ItemPost post) {
        return String.join(" ", java.util.stream.Stream.of(post.getTitle(), post.getItemName(),
                        post.getDescription(), post.getPublicFeatures(), post.getLocationName(),
                        post.getColor(), post.getBrand())
                .filter(value -> value != null && !value.isBlank()).toList());
    }

    private void put(Map<String, Object> source, String key, String value) {
        if (value != null && !value.isBlank()) {
            source.put(key, value);
        }
    }
}
