package com.zhixun.item.config;

import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;

@Configuration
public class ElasticsearchConfig {

    @Bean(destroyMethod = "close")
    public RestHighLevelClient restHighLevelClient(
            @Value("${zhixun.elasticsearch.uris}") String uris
    ) {
        HttpHost[] hosts = Arrays.stream(uris.split(","))
                .map(String::trim)
                .filter(value -> !value.isBlank())
                .map(HttpHost::create)
                .toArray(HttpHost[]::new);
        return new RestHighLevelClient(RestClient.builder(hosts));
    }
}
