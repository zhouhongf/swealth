package com.myworld.swealth.search.service

import com.myworld.swealth.common.ApiResult
import com.myworld.swealth.common.ResultUtil
import com.myworld.swealth.search.config.ElasticSearchSource
import org.apache.logging.log4j.LogManager
import org.elasticsearch.action.search.SearchRequest
import org.elasticsearch.client.RequestOptions
import org.elasticsearch.client.RestHighLevelClient
import org.elasticsearch.index.query.MultiMatchQueryBuilder
import org.elasticsearch.index.query.QueryBuilder
import org.elasticsearch.index.query.QueryBuilders
import org.elasticsearch.rest.RestStatus
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder
import org.elasticsearch.search.suggest.SuggestBuilder
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.util.*
import kotlin.collections.HashSet
import kotlin.collections.Map
import kotlin.collections.MutableList
import kotlin.collections.MutableSet
import kotlin.collections.isEmpty
import kotlin.collections.iterator
import kotlin.collections.set


@Service
class ElasticServiceText {

    private val log = LogManager.getRootLogger()

    @Autowired
    private lateinit var restHighLevelClient: RestHighLevelClient

    @Throws(Exception::class)
    fun search(indexName: String, keyword: String, pageIndex: Int, pageSize: Int): ApiResult<Any?> {
        val multiMatchQueryBuilder: MultiMatchQueryBuilder = QueryBuilders.multiMatchQuery(keyword, "name", "content")
        log.info("multiMatchQueryBuilder内容是：{}", multiMatchQueryBuilder.toString())

        val from = pageIndex * pageSize
        val searchSourceBuilder = ElasticSearchSource.initSearchSourceBuilder(queryBuilder = multiMatchQueryBuilder, from = from, size = pageSize)

        // 添加高亮显示
        var highlightBuilder = HighlightBuilder()
        highlightBuilder = ElasticSearchSource.addHightlight(highlightBuilder, "name")
        highlightBuilder = ElasticSearchSource.addHightlight(highlightBuilder, "content")
        searchSourceBuilder.highlighter(highlightBuilder)

        // 添加suggestBuilder
        var suggestBuilder = SuggestBuilder()
        suggestBuilder = ElasticSearchSource.addSuggestion(suggestBuilder, "suggest_name", "name", keyword, true)
        searchSourceBuilder.suggest(suggestBuilder)

        // 查找索引
        val request = SearchRequest(indexName)
        request.source(searchSourceBuilder)
        // 获得返回字母
        val response = restHighLevelClient.search(request, RequestOptions.DEFAULT)
        if (RestStatus.OK == response.status()) {
            // log.info("response的内容是：{}", response)
            val hits = response.hits
            val totalHitsValue = hits.totalHits?.value
            val searchHits = hits.hits

            // 遍历获取suggest内容
            val suggestWords : MutableSet<String> = HashSet()
            val searchSuggests = response.suggest
            for (suggest in searchSuggests.iterator()) {
                for (entry in suggest) {
                    val word = entry.text.string()
                    if (word.length > 1) {
                        suggestWords.add(word)
                    }
                }
            }
            log.info("suggest的内容是：{}", suggestWords)

            // 如果没有匹配的内容返回，则返回suggest内容
            if (searchHits.isEmpty()) {
                return ResultUtil.failure(code = -2, msg = "未能获取到数据", data = suggestWords)
            }

            val res: MutableList<Map<String, Any>> = ArrayList(searchHits.size)
            for (hit in searchHits) {
                val highlightFields = hit.highlightFields

                val highlightName = highlightFields["name"]
                if (highlightName != null) {
                    val fragments = highlightName.fragments()
                    val stringBuffer = StringBuilder()
                    for (text in fragments) {
                        stringBuffer.append(text)
                    }
                    hit.sourceAsMap["name"] = stringBuffer.toString()
                }

                val highlightContent = highlightFields["content"]
                if (highlightContent != null) {
                    val fragments = highlightContent.fragments()
                    val stringBuffer = StringBuilder()
                    for (text in fragments) {
                        stringBuffer.append(text)
                    }
                    hit.sourceAsMap["content"] = stringBuffer.toString()
                }

                hit.sourceAsMap["id"] = hit.id
                res.add(hit.sourceAsMap)
            }
            return ResultUtil.success(msg = "新闻数据", num = totalHitsValue, data = res)
        }
        return ResultUtil.failure(-2, "未能获取到数据")
    }

    @Throws(Exception::class)
    fun outline(indexName: String, keyword: String, pageIndex: Int, pageSize: Int): ApiResult<Any?> {
        val queryBuilder: QueryBuilder = QueryBuilders.matchQuery( "type_main", keyword)
        log.info("queryBuilder内容是：{}", queryBuilder.toString())

        val from = pageIndex * pageSize
        val searchSourceBuilder = ElasticSearchSource.initSearchSourceBuilder(queryBuilder = queryBuilder, from = from, size = pageSize)

        val includeFields = arrayOf("name", "bank_name", "create_time")
        val excludeFields = arrayOf("content", "type_main")
        searchSourceBuilder.fetchSource(includeFields, excludeFields)

        // 查找索引
        val request = SearchRequest(indexName)
        request.source(searchSourceBuilder)
        // 获得返回字母
        val response = restHighLevelClient.search(request, RequestOptions.DEFAULT)
        if (RestStatus.OK == response.status()) {
            log.info("response的内容是：{}", response)
            val hits = response.hits
            val totalHitsValue = hits.totalHits?.value
            val searchHits = hits.hits

            // 如果没有匹配的内容返回，则返回suggest内容
            if (searchHits.isEmpty()) {
                return ResultUtil.failure(code = -2, msg = "未能获取到数据")
            }

            val res: MutableList<Map<String, Any>> = ArrayList(searchHits.size)
            for (hit in searchHits) {
                hit.sourceAsMap["id"] = hit.id
                res.add(hit.sourceAsMap)
            }
            return ResultUtil.success(msg = indexName, num = totalHitsValue, data = res)
        }
        return ResultUtil.failure(-2, "未能获取到数据")
    }

}
