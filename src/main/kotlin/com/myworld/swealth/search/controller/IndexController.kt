package com.myworld.swealth.search.controller

import com.myworld.swealth.search.service.ElasticServiceBase
import com.myworld.swealth.search.vo.IdxVo
import com.myworld.swealth.common.ApiResult
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/elastic")
class IndexController {

    @Autowired
    lateinit var elasticServiceBase: ElasticServiceBase

    // @PostMapping(value = "/indexCreate")
    @Throws(Exception::class)
    fun indexCreate(@RequestBody idxVo: IdxVo?): ApiResult<Any?> {
        return elasticServiceBase.createIndex(idxVo!!)
    }

    // @GetMapping(value = "/exist/{index}")
    @Throws(Exception::class)
    fun indexExist(@PathVariable(value = "index") index: String?): Boolean {
        return elasticServiceBase.isExistsIndex(index)
    }

    // @GetMapping(value = "/del/{index}")
    @Throws(Exception::class)
    fun indexDel(@PathVariable(value = "index") index: String?): ApiResult<Any?> {
        return elasticServiceBase.deleteIndex(index)
    }

}
