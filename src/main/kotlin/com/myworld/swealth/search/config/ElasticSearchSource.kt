package com.myworld.swealth.search.config

import org.apache.logging.log4j.LogManager
import org.elasticsearch.common.unit.TimeValue
import org.elasticsearch.index.query.QueryBuilder
import org.elasticsearch.search.builder.SearchSourceBuilder
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder
import org.elasticsearch.search.sort.FieldSortBuilder
import org.elasticsearch.search.sort.ScoreSortBuilder
import org.elasticsearch.search.sort.SortOrder
import org.elasticsearch.search.suggest.SuggestBuilder
import org.elasticsearch.search.suggest.SuggestBuilders
import java.util.concurrent.TimeUnit


object ElasticSearchSource {

    private val log = LogManager.getRootLogger()

    fun getClazz(clazzName: String): Class<*>? {
        return try {
            Class.forName(clazzName)
        } catch (e: ClassNotFoundException) {
            log.info("未能找到相应的class, 问题：{}", e.message)
            null
        }
    }

    /**
     * queryBuilder  设置查询对象
     * from  设置from选项，确定要开始搜索的结果索引。 默认为0。
     * size  设置大小选项，确定要返回的搜索匹配数。 默认为10。
     * sort  设置排序字段
     * direction 设置排序方向， 默认为DESC
     */
    @JvmStatic
    @JvmOverloads
    fun initSearchSourceBuilder(
        queryBuilder: QueryBuilder,
        from: Int = 0,
        size: Int = 10,
        sortField: String? = "create_time",
        sortOrder: SortOrder? = SortOrder.DESC,
        timeout: Int = 60
    ): SearchSourceBuilder {
        val sourceBuilder = SearchSourceBuilder()

        sourceBuilder.query(queryBuilder)                       //设置查询对象。可以使任何类型的 QueryBuilder
        sourceBuilder.from(from)                                //设置from选项，确定要开始搜索的结果索引。 默认为0。
        sourceBuilder.size(size)                                //设置大小选项，确定要返回的搜索匹配数。 默认为10。
        sourceBuilder.timeout(TimeValue(timeout.toLong(), TimeUnit.SECONDS))
        sourceBuilder.sort(ScoreSortBuilder().order(SortOrder.DESC))
        sourceBuilder.sort(FieldSortBuilder(sortField).order(sortOrder))

        return sourceBuilder
    }

    @JvmStatic
    @JvmOverloads
    fun addSuggestion(suggestBuilder: SuggestBuilder, suggestName: String, fieldName: String, text: String, isChinese: Boolean = false) : SuggestBuilder {
        val termSuggestBuilder = SuggestBuilders.termSuggestion(fieldName).text(text)
        if (isChinese) {
            termSuggestBuilder.analyzer("ik_max_word")
        }
        suggestBuilder.addSuggestion(suggestName, termSuggestBuilder)
        return suggestBuilder
    }

    @JvmStatic
    @JvmOverloads
    fun addHightlight(highlightBuilder: HighlightBuilder, fieldName: String, tagName: String = "strong") : HighlightBuilder {
        val hightlightCodeRegister = HighlightBuilder.Field(fieldName)
        hightlightCodeRegister.preTags("<$tagName>").postTags("</$tagName>")
        highlightBuilder.field(hightlightCodeRegister)
        return highlightBuilder
    }
}
