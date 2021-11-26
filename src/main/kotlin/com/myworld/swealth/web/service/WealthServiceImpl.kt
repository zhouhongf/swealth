package com.myworld.swealth.web.service

import com.myworld.swealth.data.entity.Wealth
import com.myworld.swealth.data.repository.ManualRepository
import com.myworld.swealth.data.repository.WealthRepository
import com.myworld.swealth.common.ApiResult
import com.myworld.swealth.common.ResultUtil
import com.myworld.swealth.common.StringUtil
import com.myworld.swealth.data.entity.Text
import org.apache.commons.io.IOUtils
import org.apache.logging.log4j.LogManager
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.aggregation.Aggregation
import org.springframework.data.mongodb.core.aggregation.AggregationOperation
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Update
import org.springframework.stereotype.Service
import java.io.ByteArrayInputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Pattern
import javax.servlet.http.HttpServletResponse
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

@Service
class WealthServiceImpl : WealthService {
    private val log = LogManager.getRootLogger()

    @Autowired
    private lateinit var wealthRepository: WealthRepository
    @Autowired
    private lateinit var manualRepository: ManualRepository
    @Autowired
    private lateinit var mongoTemplate: MongoTemplate

    override fun getWealthInfo(id: String): ApiResult<Any?> {
        val wealth = wealthRepository.findById(id)
        return if (wealth.isPresent) {
            ResultUtil.success(data=wealth)
        } else {
            ResultUtil.failure(code = -2, msg = "没有找到数据")
        }

    }

    @Throws(IOException::class)
    override fun getWealthManual(id: String, response: HttpServletResponse) {
        if (StringUtil.isInteger(id)) {
            val manual = manualRepository.findById(id)
            if (manual.isPresent) {
                IOUtils.copy(ByteArrayInputStream(manual.get().content), response.outputStream)
                val contentType = StringUtil.getFileMimeType(manual.get().fileSuffix!!)
                response.contentType = contentType
            }
        } else {
            val manual = manualRepository.findByUkey(id)
            if (manual != null) {
                IOUtils.copy(ByteArrayInputStream(manual.content), response.outputStream)
                val contentType = StringUtil.getFileMimeType(manual.fileSuffix!!)
                response.contentType = contentType
            }
        }
    }

    /**
     * 理财排名
     */
    override fun getWealthRank(bankLevel: String?,
                               bankName: String?,
                               risk: Int?,
                               term: Int?,
                               promise: String?,
                               fixed: String?,
                               start: Long?,
                               end: Long?,
                               pageIndex: Int,
                               pageSize: Int): ApiResult<Any?> {
        if (bankLevel == null && risk == null && term == null && promise == null && fixed == null && start == null && end == null) {
            return ResultUtil.failure(-2, "缺少查询参数")
        }
        val query = Query()
        if (bankLevel != null) {
            query.addCriteria(Criteria.where("bank_level").`is`(bankLevel))
        }
        if (bankName != null) {
            query.addCriteria(Criteria.where("bank_name").`is`(bankName))
        }
        if (risk != null) {
            query.addCriteria(Criteria.where("risk").`is`(risk))
        }
        if (term != null) {
            query.addCriteria(Criteria.where("term").lte(term))
        }
        if (promise != null) {
            query.addCriteria(Criteria.where("promise_type").`is`(promise))
        }
        if (fixed != null) {
            query.addCriteria(Criteria.where("fixed_type").`is`(fixed))
        }

        if (start != null && end != null) {
            val startTime = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Date(start))
            val endTime = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Date(end))
            query.addCriteria(Criteria.where("create_time").gte(startTime).lt(endTime))
        }

        val numSkip = pageIndex.toLong() * pageSize

        val totalCount = mongoTemplate.count(query, Wealth::class.java)
        if (totalCount == 0L) {
            return ResultUtil.failure(-2, "该条件下无数据")
        }
        val wealthList = mongoTemplate.find(query.with(Sort(Sort.Direction.DESC, "rate_max", "rate_min")).skip(numSkip).limit(pageSize), Wealth::class.java)
        val list: MutableList<Map<String, Any?>> = ArrayList()
        for (wealth in wealthList) {
            val rateMax = wealth.rateMax
            val rateMin = wealth.rateMin
            val map: MutableMap<String, Any?> = HashMap()
            map["id"] = wealth.id
            map["code"] = wealth.code
            map["bankName"] = wealth.bankName
            var rate = "暂无"
            if (rateMax != null && rateMin != null) {
                if (rateMax > rateMin) {
                    rate = String.format("%.2f", rateMax * 100) + "% ~ " + String.format("%.2f", rateMin * 100) + "%"
                } else if (rateMax == rateMin) {
                    rate = String.format("%.2f", rateMin * 100) + "%"
                }
            } else if (rateMax != null) {
                rate = String.format("%.2f", rateMax * 100) + "%"
            } else if (rateMin != null) {
                rate = String.format("%.2f", rateMin * 100) + "%"
            }
            map["rate"] = rate
            list.add(map)
        }
        return ResultUtil.success(num = totalCount, data = list)
    }

    // 学习之用
    fun analyzeWealthData(keys: Array<String?>, values: Array<String?>, groupKey: String?, queryKey: Array<String?>, collectionName: String?) { // 聚合操作
        val operations: MutableList<AggregationOperation> = ArrayList()
        // 筛选条件
        for (i in keys.indices) { //operations.add(Aggregation.match(new Criteria(keys[i]).is(values[i])));
            operations.add(Aggregation.match(Criteria.where(keys[i]).`is`(values[i])))
        }
        // 可以根据bank_name或者bank_level分类统计最大、最小、平均、求和值。
        operations.add(
            Aggregation.group("bank_level").max("risk").`as`("max").min("risk").`as`("min").avg("risk").`as`("avg").sum("risk").`as`("sum")
        )
        // 分组字段
        var groupOperation = Aggregation.group(groupKey)
        // 聚合查询字段
        for (i in queryKey.indices) {
            groupOperation = groupOperation.sum(queryKey[i]).`as`(queryKey[i])
        }
        // 添加选项  (聚合查询字段和添加筛选是有区别的注意)
        operations.add(groupOperation)
        // 最终聚合查询所有信息
        val aggregation = Aggregation.newAggregation(operations)
        // 查询结果
        val results = mongoTemplate.aggregate(aggregation, collectionName, HashMap::class.java)
        //获取结果
        val result = results.mappedResults
    }

    override fun makeAggregationOperationBaseList(bankLevel: String?,
                                                  bankName: String?,
                                                  risk: Int?,
                                                  term: Int?,
                                                  promise: String?,
                                                  fixed: String?,
                                                  start: Long?,
                                                  end: Long?): List<AggregationOperation> {
        val operations: MutableList<AggregationOperation> = ArrayList()
        if (bankLevel != null) {
            operations.add(Aggregation.match(Criteria.where("bank_level").`is`(bankLevel)))
        }
        if (bankName != null) {
            operations.add(Aggregation.match(Criteria.where("bank_name").`is`(bankName)))
        }
        if (risk != null) {
            operations.add(Aggregation.match(Criteria.where("risk").`is`(risk)))
        }
        if (term != null) {
            operations.add(Aggregation.match(Criteria.where("term").lte(term)))
        }
        if (promise != null) {
            operations.add(Aggregation.match(Criteria.where("promise_type").`is`(promise)))
        }
        if (fixed != null) {
            operations.add(Aggregation.match(Criteria.where("fixed_type").`is`(fixed)))
        }

        if (start != null && end != null) {
            val startTime = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Date(start))
            val endTime = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Date(end))
            operations.add(Aggregation.match(Criteria.where("create_time").gte(startTime).lt(endTime)))
        }
        return operations
    }

    /**
     * bucket聚合分类，只适用于 数字类型 的字段
     * risk, term, rateMax, rateMin
     */
    override fun bucketAnalyze(keyword: String,
                               bankLevel: String?,
                               bankName: String?,
                               risk: Int?,
                               term: Int?,
                               promise: String?,
                               fixed: String?,
                               start: Long?,
                               end: Long?): ApiResult<Any?> {
        var boundaries = arrayOf<Any>()
        when (keyword) {
            "risk" -> boundaries = arrayOf(0, 1, 2, 3, 4, 5, 6)
            "term" -> boundaries = arrayOf(0, 1, 8, 31, 91, 181, 366, 1096, 3651, 36500)
            "rate_max" -> boundaries = arrayOf(0.0, 0.01, 0.02, 0.03, 0.04, 0.05, 0.06, 0.07, 0.08, 0.09, 0.1, 0.12, 0.15)
            "rate_min" -> boundaries = arrayOf(0.0, 0.01, 0.02, 0.03, 0.04, 0.05, 0.06, 0.07, 0.08, 0.09, 0.1, 0.12, 0.15)
        }
        val operations: MutableList<AggregationOperation> = ArrayList()
        operations.add(Aggregation.match(Criteria.where(keyword).exists(true)))
        operations.add(
            Aggregation.bucket(keyword)
                .withBoundaries(*boundaries)
                .withDefaultBucket(-1)
                .andOutputCount().`as`("num")
                .andOutput(keyword).max().`as`("max")
                .andOutput(keyword).min().`as`("min")
                .andOutput(keyword).avg().`as`("avg")
                .andOutput(keyword).sum().`as`("sum")
                .andOutput("wkey").push().`as`("wkey")
                .andOutput(keyword).push().`as`("value")
        )
        val operationsBase = makeAggregationOperationBaseList(bankLevel, bankName, risk, term, promise, fixed, start, end)
        operations.addAll(1, operationsBase)
        val aggregation = Aggregation.newAggregation(operations)
        val results = mongoTemplate.aggregate(aggregation, "wealth", HashMap::class.java)
        val result = results.mappedResults
        return if (result.size > 0) ResultUtil.success(data=result) else ResultUtil.failure(-2, "未能找到符合条件的数据")
    }


    override fun textAndWealth(bankName: String): ApiResult<Any?> {
        val query = Query(Criteria.where("bank_name").`is`(bankName))
        val wealths = mongoTemplate.find(query.with(Sort(Sort.Direction.DESC, "create_time")).skip(0L).limit(5), Wealth::class.java)
        val texts = mongoTemplate.find(query.with(Sort(Sort.Direction.DESC, "create_time")).skip(0L).limit(5), Text::class.java)
        val textsList: MutableList<Any> = ArrayList()
        for (text in texts) {
            val textShort: MutableMap<String, Any> = HashMap()
            textShort["id"] = text.id!!
            textShort["name"] = text.name!!
            textShort["date"] = text.date!!
            textsList.add(textShort)
        }

        val wealthsList: MutableList<Any> = ArrayList()
        for (wealth in wealths) {
            val wealthShort: MutableMap<String, Any> = HashMap()
            wealthShort["id"] = wealth.id!!
            wealthShort["name"] = wealth.name!!
            wealthShort["rateMax"] = wealth.rateMax!!
            wealthShort["rateMin"] = wealth.rateMin!!
            wealthShort["amountBuyMin"] = wealth.amountBuyMin!!
            wealthShort["term"] = wealth.term!!
            wealthShort["risk"] = wealth.risk!!
            wealthsList.add(wealthShort)
        }

        val map: MutableMap<String, Any> = HashMap()
        map["wealths"] = wealthsList
        map["texts"] = textsList
        return ResultUtil.success(data = map)
    }

    override fun wealthMore(bankName: String, pageSize: Int, pageIndex: Int): ApiResult<*>? {
        val pageable: Pageable = PageRequest.of(pageIndex, pageSize, Sort.Direction.DESC, "createTime")
        val wealthPaged = wealthRepository.findByBankName(bankName, pageable) ?: return ResultUtil.failure(-2, "没有数据")
        val wealths = wealthPaged.content
        val wealthsList: MutableList<Any> = ArrayList()
        for (wealth in wealths) {
            val wealthShort: MutableMap<String, Any> = HashMap()
            wealthShort["id"] = wealth.id!!
            wealthShort["name"] = wealth.name!!
            wealthShort["rateMax"] = wealth.rateMax!!
            wealthShort["rateMin"] = wealth.rateMin!!
            wealthShort["amountBuyMin"] = wealth.amountBuyMin!!
            wealthShort["term"] = wealth.term!!
            wealthShort["risk"] = wealth.risk!!
            wealthsList.add(wealthShort)
        }
        return ResultUtil.success(num = wealthPaged.totalElements, data = wealthsList)
    }


    /**
     * MongoDB查询全部
     */
    override fun findWealthAll(): ApiResult<Any?> {
        val wealthList = mongoTemplate.findAll(Wealth::class.java)
        val count = mongoTemplate.count(Query().with(Sort(Sort.Direction.DESC, "create_time")), Wealth::class.java)
        return ResultUtil.success(num = count, data = wealthList)
    }

    /**
     * MongoDB根据id查询
     */
    override fun findWealthByID(id: String): ApiResult<Any?> {
        val wealth = mongoTemplate.findById(id, Wealth::class.java)
        return ResultUtil.success(data =  wealth)
    }

    /**
     * MongoDB准确查询
     */
    override fun findWealthListByMany(wealth: Wealth): ApiResult<Any?> {
        val query = Query(Criteria
            .where("risk").`is`(wealth.risk)
            .and("term").`is`(wealth.term)
            .and("create_time").gt(wealth.createTime))
        val wealthList = mongoTemplate.find(query, Wealth::class.java)
        return ResultUtil.success(data = wealthList)
    }

    /**
     * MongoDB模糊查询
     * 模糊查询以 【^】开始 以【$】结束 【.*】相当于Mysql中的%
     */
    override fun findWealthLikeName(name: String): ApiResult<Any?> {
        val regex = String.format("%s%s%s", "^.*", name, ".*$")
        val pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE)
        val query = Query(Criteria.where("name").regex(pattern))
        val wealthList = mongoTemplate.find(query, Wealth::class.java)
        return ResultUtil.success(data =  wealthList)
    }

    /**
     * MongoDB分页查询
     */
    override fun findWealthPage(wealth: Wealth): ApiResult<Any?> {
        val query = Query()
        val totalCount = mongoTemplate.count(query, Wealth::class.java).toInt()
        val wealthList = mongoTemplate.find(query.skip(0L).limit(10), Wealth::class.java)
        return ResultUtil.success(data =  wealthList)
    }

    /**
     * MongoDB修改
     */
    override fun update(wealth: Wealth): ApiResult<Any?> {
        val query = Query(Criteria.where("_id").`is`(wealth.id))
        val update = Update()
        update["name"] = wealth.name
        update["bank_name"] = wealth.bankName
        update["risk"] = wealth.risk
        update["term"] = wealth.term
        val result = mongoTemplate.upsert(query, update, Wealth::class.java)
        val count = result.modifiedCount
        return if (count > 0) {
            ResultUtil.success()
        } else {
            ResultUtil.failure(-2, "更新不成功")
        }
    }

    /**
     * MongoDB删除
     */
    override fun delete(id: String): ApiResult<Any?> {
        val query = Query(Criteria.where("_id").`is`(id))
        val result = mongoTemplate.remove(query, Wealth::class.java)
        val count = result.deletedCount
        return if (count > 0) {
            ResultUtil.success()
        } else {
            ResultUtil.failure(-2, "删除不成功")
        }
    }

    /**
     * MongoDB新增
     */
    override fun create(wealth: Wealth): ApiResult<Any?> {
        val insert = mongoTemplate.insert(wealth)
        return ResultUtil.success(data = insert)
    }

}
