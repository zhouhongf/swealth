package com.myworld.swealth.web.controller

import com.myworld.swealth.common.ApiResult
import com.myworld.swealth.search.service.ElasticServiceBase
import com.myworld.swealth.search.service.ElasticServiceText
import com.myworld.swealth.web.service.TextService
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.io.IOException
import javax.servlet.http.HttpServletResponse


@RestController
@RequestMapping("/text")
class TextController {

    private val log : Logger = LogManager.getRootLogger()
    companion object {
        private const val indexName = "news"
    }

    @Autowired
    private lateinit var textService: TextService
    @Autowired
    private lateinit var elasticServiceBase: ElasticServiceBase
    @Autowired
    private lateinit var elasticServiceText: ElasticServiceText


    @GetMapping(value = ["/search"])
    @Throws(Exception::class)
    fun search(@RequestParam keyword: String?, @RequestParam pageIndex: Int, @RequestParam pageSize: Int): ApiResult<Any?> {
        return if (keyword != null) {
            elasticServiceText.search(indexName, keyword, pageIndex, pageSize)
        } else {
            elasticServiceBase.latest(indexName, pageIndex, pageSize)
        }
    }

    @GetMapping("/detail/{id}")
    fun detail(@PathVariable id: String): ApiResult<Any?> {
        return textService.detail(id)
    }

    @GetMapping(value = ["/outline"])
    @Throws(Exception::class)
    fun outline(@RequestParam keyword: String, @RequestParam pageIndex: Int, @RequestParam pageSize: Int): ApiResult<Any?> {
        return elasticServiceText.outline(indexName, keyword, pageIndex, pageSize)
    }

    @GetMapping("/wordcloud/{id}")
    @Throws(IOException::class)
    fun wordcloud(@PathVariable id: String, response: HttpServletResponse) {
        textService.wordcloud(id, response)
    }

    @GetMapping("/textMore")
    fun textMore(@RequestParam bankName: String, @RequestParam pageSize: Int, @RequestParam pageIndex: Int): ApiResult<*>? {
        return textService.textMore(bankName, pageSize, pageIndex)
    }

}
