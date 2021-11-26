package com.myworld.swealth.web.service

import com.myworld.swealth.data.entity.Wealth
import com.myworld.swealth.common.ApiResult
import org.springframework.data.mongodb.core.aggregation.AggregationOperation
import java.io.IOException
import javax.servlet.http.HttpServletResponse

interface WealthService {
    fun getWealthInfo(id: String): ApiResult<Any?>
    @Throws(IOException::class)
    fun getWealthManual(id: String, response: HttpServletResponse)

    fun getWealthRank(bankLevel: String?,
                      bankName: String?,
                      risk: Int?,
                      term: Int?,
                      promise: String?,
                      fixed: String?,
                      start: Long?,
                      end: Long?,
                      pageIndex: Int = 0,
                      pageSize: Int = 10): ApiResult<Any?>

    fun makeAggregationOperationBaseList(bankLevel: String?,
                                         bankName: String?,
                                         risk: Int?,
                                         term: Int?,
                                         promise: String?,
                                         fixed: String?,
                                         start: Long?,
                                         end: Long?): List<AggregationOperation>

    fun bucketAnalyze(keyword: String,
                      bankLevel: String?,
                      bankName: String?,
                      risk: Int?,
                      term: Int?,
                      promise: String?,
                      fixed: String?,
                      start: Long?,
                      end: Long?): ApiResult<Any?>

    fun textAndWealth(bankName: String): ApiResult<Any?>
    fun wealthMore(bankName: String, pageSize: Int, pageIndex: Int): ApiResult<*>?

    fun findWealthAll(): ApiResult<Any?>
    fun findWealthByID(id: String): ApiResult<Any?>
    fun findWealthListByMany(wealth: Wealth): ApiResult<Any?>
    fun findWealthLikeName(name: String): ApiResult<Any?>
    fun findWealthPage(wealth: Wealth): ApiResult<Any?>
    fun update(wealth: Wealth): ApiResult<Any?>
    fun delete(id: String): ApiResult<Any?>
    fun create(wealth: Wealth): ApiResult<Any?>
}
