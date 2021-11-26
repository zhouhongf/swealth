package com.myworld.swealth.search.service

import com.myworld.swealth.common.ApiResult
import com.myworld.swealth.common.ResultUtil
import com.myworld.swealth.common.StringUtil
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
import java.util.ArrayList


@Service
class ElasticServiceWealth {
    private val log = LogManager.getRootLogger()

    @Autowired
    private lateinit var restHighLevelClient: RestHighLevelClient


    @Throws(Exception::class)
    fun search(indexName: String, keyword: String, pageIndex: Int, pageSize: Int): ApiResult<Any?> {
        val multiMatchQueryBuilder: MultiMatchQueryBuilder = if (StringUtil.isChineseLetters(keyword)) {
            QueryBuilders.multiMatchQuery(keyword, "name", "bank_name")
        } else {
            QueryBuilders.multiMatchQuery(keyword, "code", "code_register")
        }
        log.info("multiMatchQueryBuilder内容是：{}", multiMatchQueryBuilder.toString())
        val from = pageIndex * pageSize
        val searchSourceBuilder = ElasticSearchSource.initSearchSourceBuilder(queryBuilder = multiMatchQueryBuilder, from = from, size = pageSize)

        // 添加高亮显示
        var highlightBuilder = HighlightBuilder()
        highlightBuilder = ElasticSearchSource.addHightlight(highlightBuilder, "name")
        highlightBuilder = ElasticSearchSource.addHightlight(highlightBuilder, "bank_name")
        highlightBuilder = ElasticSearchSource.addHightlight(highlightBuilder, "code")
        highlightBuilder = ElasticSearchSource.addHightlight(highlightBuilder, "code_register")
        searchSourceBuilder.highlighter(highlightBuilder)

        // 添加suggestBuilder
        var suggestBuilder = SuggestBuilder()
        if (StringUtil.isChineseLetters(keyword)) {
            suggestBuilder = ElasticSearchSource.addSuggestion(suggestBuilder, "suggest_name", "name", keyword, true)
            suggestBuilder = ElasticSearchSource.addSuggestion(suggestBuilder, "suggest_bank_name", "bank_name", keyword, true)
        } else {
            suggestBuilder = ElasticSearchSource.addSuggestion(suggestBuilder, "suggest_code", "code", keyword, false)
            suggestBuilder = ElasticSearchSource.addSuggestion(suggestBuilder, "suggest_code_register", "code_register", keyword, false)
        }
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
                val highlightBankName = highlightFields["bank_name"]
                if (highlightBankName != null) {
                    val fragments = highlightBankName.fragments()
                    val stringBuffer = StringBuilder()
                    for (text in fragments) {
                        stringBuffer.append(text)
                    }
                    hit.sourceAsMap["bank_name"] = stringBuffer.toString()
                }
                val highlightCode = highlightFields["code"]
                if (highlightCode != null) {
                    val fragments = highlightCode.fragments()
                    val stringBuffer = StringBuilder()
                    for (text in fragments) {
                        stringBuffer.append(text)
                    }
                    hit.sourceAsMap["code"] = stringBuffer.toString()
                }
                val highlightCodeRegister = highlightFields["code_register"]
                if (highlightCodeRegister != null) {
                    val fragments = highlightCodeRegister.fragments()
                    val stringBuffer = StringBuilder()
                    for (text in fragments) {
                        stringBuffer.append(text)
                    }
                    hit.sourceAsMap["code_register"] = stringBuffer.toString()
                }
                hit.sourceAsMap["id"] = hit.id
                res.add(hit.sourceAsMap)
            }
            return ResultUtil.success(msg = indexName, num = totalHitsValue, data = res)
        }
        return ResultUtil.failure(-2, "未能获取到数据")
    }

}
