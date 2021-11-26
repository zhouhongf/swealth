package com.myworld.swealth.search.service

import com.alibaba.fastjson.JSON
import com.myworld.swealth.search.vo.ElasticEntity
import com.myworld.swealth.search.vo.IdxVo
import com.myworld.swealth.common.ApiResult
import com.myworld.swealth.common.ResultUtil
import com.myworld.swealth.search.config.ElasticSearchSource
import org.apache.logging.log4j.LogManager
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest
import org.elasticsearch.action.bulk.BulkRequest
import org.elasticsearch.action.delete.DeleteRequest
import org.elasticsearch.action.index.IndexRequest
import org.elasticsearch.action.search.SearchRequest
import org.elasticsearch.action.support.IndicesOptions
import org.elasticsearch.client.RequestOptions
import org.elasticsearch.client.RestHighLevelClient
import org.elasticsearch.client.indices.CreateIndexRequest
import org.elasticsearch.client.indices.GetIndexRequest
import org.elasticsearch.common.settings.Settings
import org.elasticsearch.common.xcontent.XContentType
import org.elasticsearch.index.query.QueryBuilder
import org.elasticsearch.index.query.QueryBuilders
import org.elasticsearch.index.reindex.DeleteByQueryRequest
import org.elasticsearch.rest.RestStatus
import org.elasticsearch.search.builder.SearchSourceBuilder
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.util.*
import java.util.function.Consumer

@Service
class ElasticServiceBase {
    private val log = LogManager.getRootLogger()

    @Autowired
    private lateinit var restHighLevelClient: RestHighLevelClient

    @Throws(Exception::class)
    fun createIndex(idxVo: IdxVo): ApiResult<Any?> {
        val idxSQL = JSON.toJSONString(idxVo.idxSql)
        val idxName = idxVo.idxName
        if (isExistsIndex(idxName)) {
            return ResultUtil.failure(-2, "索引已经存在，不允许创建")
        }
        val request = CreateIndexRequest(idxName)
        request.settings(Settings.builder().put("index.number_of_shards", 3).put("index.number_of_replicas", 2).put("analysis.analyzer.default.tokenizer", "ik_max_word"))
        request.mapping(idxSQL, XContentType.JSON)
        val res = restHighLevelClient.indices().create(request, RequestOptions.DEFAULT)
        return if (res.isAcknowledged) ResultUtil.success() else ResultUtil.failure(-2, "创建索引失败")
    }

    /**
     * 某个index是否存在, 人类可读
     */
    @Throws(Exception::class)
    fun indexExist(idxName: String?): Boolean {
        val request = GetIndexRequest(idxName)
        request.local(false)
        request.humanReadable(true)
        request.includeDefaults(false)
        request.indicesOptions(IndicesOptions.lenientExpandOpen())
        return restHighLevelClient.indices().exists(request, RequestOptions.DEFAULT)
    }

    /**
     * 判断某个index是否存在
     */
    @Throws(Exception::class)
    fun isExistsIndex(idxName: String?): Boolean {
        return restHighLevelClient.indices().exists(GetIndexRequest(idxName), RequestOptions.DEFAULT)
    }

    @Throws(Exception::class)
    fun deleteIndex(idxName: String?): ApiResult<Any?> {
        if (!isExistsIndex(idxName)) {
            return ResultUtil.failure(-2, "该索引已不存在")
        }
        val resp = restHighLevelClient.indices().delete(DeleteIndexRequest(idxName), RequestOptions.DEFAULT)
        return if (resp.isAcknowledged) ResultUtil.success() else ResultUtil.failure(-2, "删除索引失败")
    }

    /**
     * 设置分片
     */
    fun buildSetting(request: CreateIndexRequest) {
        request.settings(Settings.builder().put("index.number_of_shards", 3).put("index.number_of_replicas", 2))
    }

    @Throws(Exception::class)
    fun insertOrUpdateOne(idxName: String?, entity: ElasticEntity<*>) {
        val request = IndexRequest(idxName)
        request.id(entity.id)
        // request.source(entity.getData(), XContentType.JSON);
        request.source(JSON.toJSONString(entity.data), XContentType.JSON)
        restHighLevelClient.index(request, RequestOptions.DEFAULT)
    }

    /**
     * 批量插入数据
     */
    @Throws(Exception::class)
    fun insertBatch(idxName: String?, list: List<ElasticEntity<*>>) {
        val request = BulkRequest()
        list.forEach(Consumer { (id, data) -> request.add(IndexRequest(idxName).id(id).source(JSON.toJSONString(data), XContentType.JSON)) })
        restHighLevelClient.bulk(request, RequestOptions.DEFAULT)
    }

    /**
     * 批量插入数据
     */
    @Throws(Exception::class)
    fun insertBatchTrueObj(idxName: String?, list: List<ElasticEntity<*>>) {
        val request = BulkRequest()
        list.forEach(Consumer { (id, data) -> request.add(IndexRequest(idxName).id(id).source(data, XContentType.JSON)) })
        restHighLevelClient.bulk(request, RequestOptions.DEFAULT)
    }

    /**
     * 删除一条记录
     */
    @Throws(Exception::class)
    fun deleteOne(idxName: String?, entity: ElasticEntity<*>) {
        val request = DeleteRequest(idxName)
        request.id(entity.id)
        restHighLevelClient.delete(request, RequestOptions.DEFAULT)
    }

    /**
     * 批量删除
     */
    @Throws(Exception::class)
    fun <T> deleteBatch(idxName: String?, idList: Collection<T>) {
        val request = BulkRequest()
        idList.forEach(Consumer { item: T -> request.add(DeleteRequest(idxName, item.toString())) })
        restHighLevelClient.bulk(request, RequestOptions.DEFAULT)
    }

    @Throws(Exception::class)
    fun deleteByQuery(idxName: String?, builder: QueryBuilder?) {
        val request = DeleteByQueryRequest(idxName)
        request.setQuery(builder)
        //设置批量操作数量,最大为10000
        request.batchSize = 10000
        request.setConflicts("proceed")
        restHighLevelClient.deleteByQuery(request, RequestOptions.DEFAULT)
    }

    /**
     * 查询
     */
    @Throws(Exception::class)
    fun <T> search(idxName: String, searchSourceBuilder: SearchSourceBuilder, c: Class<T>): List<T> {
        val request = SearchRequest(idxName)
        request.source(searchSourceBuilder)
        val response = restHighLevelClient.search(request, RequestOptions.DEFAULT)
        val hits = response.hits.hits
        val res: MutableList<T> = ArrayList(hits.size)
        for (hit in hits) {
            res.add(JSON.parseObject(hit.sourceAsString, c))
        }
        return res
    }

    @Throws(Exception::class)
    fun latest(indexName: String, pageIndex: Int, pageSize: Int): ApiResult<Any?> {
        val queryBuilder: QueryBuilder = QueryBuilders.matchAllQuery()
        val from = pageIndex * pageSize
        val searchSourceBuilder = ElasticSearchSource.initSearchSourceBuilder(queryBuilder = queryBuilder, from = from, size = pageSize)
        // 查找索引
        val request = SearchRequest(indexName)
        request.source(searchSourceBuilder)
        // 获得返回字母
        val response = restHighLevelClient.search(request, RequestOptions.DEFAULT)
        if (RestStatus.OK == response.status()) {
            val hits = response.hits
            val totalHitsValue = hits.totalHits?.value
            val searchHits = hits.hits

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
