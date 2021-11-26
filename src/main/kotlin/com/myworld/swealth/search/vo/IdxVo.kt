package com.myworld.swealth.search.vo

/**
 * 创建索引模板，用于解析为JSON 格式
 * idxName : idx_location
 * idxSql : {"dynamic":false,"properties":{"location_id":{"type":"long"},"flag":{"type":"text","index":true},"local_code":{"type":"text","index":true},"local_name":{"type":"text","index":true,"analyzer":"ik_max_word"},"lv":{"type":"long"},"sup_local_code":{"type":"text","index":true},"url":{"type":"text","index":true}}}
 */
data class IdxVo(var idxName: String) {

    lateinit var idxSql: IdxSql

    data class IdxSql(
        var isDynamic: Boolean = false,
        var properties: Map<String, Map<String, Any>>
    )
}
