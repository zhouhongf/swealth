package com.myworld.swealth.search.config

import org.apache.http.HttpHost
import org.elasticsearch.client.RestClient
import org.elasticsearch.client.RestClientBuilder
import org.elasticsearch.client.RestHighLevelClient
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
open class ElasticConfig{
    @Value("\${my.elasticsearch.host}")
    private var host: String = "localhost"
    @Value("\${my.elasticsearch.port}")
    private var port: Int = 9200
    @Value("\${my.elasticsearch.scheme}")
    private var scheme: String = "http"

    @Bean
    open fun restClientBuilder(): RestClientBuilder {
        return RestClient.builder(HttpHost(host, port, scheme))
    }

    @Bean
    open fun elasticsearchRestClient(): RestClient {
        return RestClient.builder(HttpHost(host, port, scheme)).build()
    }

    @Bean
    open fun restHighLevelClient(@Autowired restClientBuilder: RestClientBuilder): RestHighLevelClient {
        return RestHighLevelClient(restClientBuilder)
    }
}
