package com.zhixun.ai.config;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.redis.RedisVectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import redis.clients.jedis.JedisPooled;

@Configuration
public class VectorStoreConfig {

    @Bean(destroyMethod = "close")
    public JedisPooled vectorJedis(
            @Value("${spring.data.redis.host}") String host,
            @Value("${spring.data.redis.port}") int port
    ) {
        return new JedisPooled(host, port);
    }

    @Bean
    public VectorStore itemVectorStore(
            JedisPooled jedisPooled,
            EmbeddingModel embeddingModel,
            @Value("${spring.ai.vectorstore.redis.index-name}") String indexName,
            @Value("${spring.ai.vectorstore.redis.prefix}") String prefix
    ) {
        return RedisVectorStore.builder(jedisPooled, embeddingModel)
                .indexName(indexName)
                .prefix(prefix)
                .metadataFields(
                        RedisVectorStore.MetadataField.numeric("postId"),
                        RedisVectorStore.MetadataField.tag("itemType"),
                        RedisVectorStore.MetadataField.numeric("categoryId"),
                        RedisVectorStore.MetadataField.tag("campusArea")
                )
                .initializeSchema(true)
                .build();
    }
}
