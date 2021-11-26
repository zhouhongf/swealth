package com.myworld.swealth.web.controller

import com.alibaba.fastjson.JSONObject
import com.google.gson.Gson
import com.myworld.swealth.web.service.WealthService
import com.myworld.swealth.common.ApiResult
import com.myworld.swealth.common.ResultUtil
import com.myworld.swealth.search.WealthES
import com.myworld.swealth.search.service.ElasticServiceBase
import com.myworld.swealth.search.service.ElasticServiceWealth
import com.myworld.swealth.search.vo.ElasticDataVo
import com.myworld.swealth.search.vo.ElasticEntity
import org.apache.commons.lang.StringUtils
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*
import java.io.IOException
import java.util.ArrayList
import javax.servlet.http.HttpServletResponse

@RestController
@RequestMapping("/wealth")
class WealthController {


    private val log : Logger = LogManager.getRootLogger()
    companion object {
        private const val indexName = "wealth"
    }

    @Autowired
    private lateinit var wealthService: WealthService
    @Autowired
    private lateinit var elasticServiceBase: ElasticServiceBase
    @Autowired
    private lateinit var elasticServiceWealth: ElasticServiceWealth


    @GetMapping(value = ["/search"])
    @Throws(Exception::class)
    fun search(@RequestParam keyword: String?, @RequestParam pageIndex: Int, @RequestParam pageSize: Int): ApiResult<Any?> {
        return if (keyword != null) {
            elasticServiceWealth.search(indexName, keyword, pageIndex, pageSize)
        } else {
            elasticServiceBase.latest(indexName, pageIndex, pageSize)
        }

    }

    // @PostMapping(value = "/add")
    @Throws(Exception::class)
    fun add(@RequestBody elasticDataVo: ElasticDataVo<*>): ApiResult<Any?> {
        if (StringUtils.isEmpty(elasticDataVo.idxName)) {
            return ResultUtil.failure(-2, "索引为空，不允许提交")
        }
        val id = elasticDataVo.elasticEntity.id
        val data = elasticDataVo.elasticEntity.data
        val elasticEntity = ElasticEntity<Any?>(id, data)
        elasticServiceBase.insertOrUpdateOne(elasticDataVo.idxName, elasticEntity)
        return ResultUtil.success()
    }

    // @PostMapping(value = "/delete")
    @Throws(Exception::class)
    fun delete(@RequestBody elasticDataVo: ElasticDataVo<*>): ApiResult<Any?> {
        if (StringUtils.isEmpty(elasticDataVo.idxName)) {
            return ResultUtil.failure(-2, "索引为空，不允许提交")
        }
        elasticServiceBase.deleteOne(elasticDataVo.idxName, elasticDataVo.elasticEntity)
        return ResultUtil.success()
    }

    // @PostMapping(value = "/insertBatch")
    @Throws(Exception::class)
    fun insertBatch(idxName: String?, wealthESs: List<WealthES>) {
        val elasticEntitys: MutableList<ElasticEntity<*>> = ArrayList(wealthESs.size)
        for (wealthES in wealthESs) {
            val id = wealthES.id.toString()
            val map: MutableMap<String?, Any?> = JSONObject.parseObject(Gson().toJson(wealthES))
            val elasticEntity = ElasticEntity<Any>(id, map)
            elasticEntitys.add(elasticEntity)
        }
        elasticServiceBase.insertBatch(idxName, elasticEntitys)
    }




    @GetMapping("/detail/{id}")
    fun getWealthDetail(@PathVariable id: String): ApiResult<Any?> {
        return wealthService.getWealthInfo(id)
    }

    @GetMapping("/manual/{id}")
    @Throws(IOException::class)
    fun getWealthManual(@PathVariable id: String, response: HttpServletResponse) {
        wealthService.getWealthManual(id, response)
    }

    @GetMapping("/rank")
    fun getWealthRank(@RequestParam bankLevel: String?,
                      @RequestParam bankName: String?,
                      @RequestParam risk: Int?,
                      @RequestParam term: Int?,
                      @RequestParam promise: String?,
                      @RequestParam fixed: String?,
                      @RequestParam start: Long?,
                      @RequestParam end: Long?,
                      @RequestParam pageIndex: Int,
                      @RequestParam pageSize: Int): ApiResult<Any?> {
        return wealthService.getWealthRank(bankLevel, bankName, risk, term, promise, fixed, start, end, pageIndex, pageSize)
    }

    @GetMapping("/bucket")
    fun bucketOnRisk(@RequestParam keyword: String,
                     @RequestParam bankLevel: String?,
                     @RequestParam bankName: String?,
                     @RequestParam risk: Int?,
                     @RequestParam term: Int?,
                     @RequestParam promise: String?,
                     @RequestParam fixed: String?,
                     @RequestParam start: Long?,
                     @RequestParam end: Long?): ApiResult<Any?> {
        return wealthService.bucketAnalyze(keyword, bankLevel, bankName, risk, term, promise, fixed, start, end)
    }

    @GetMapping("/textAndWealth")
    fun textAndWealth(@RequestParam bankName: String): ApiResult<Any?> {
        return wealthService.textAndWealth(bankName)
    }

    @GetMapping("/wealthMore")
    fun wealthMore(@RequestParam bankName: String, @RequestParam pageSize: Int, @RequestParam pageIndex: Int): ApiResult<*>? {
        return wealthService.wealthMore(bankName, pageSize, pageIndex)
    }

}
