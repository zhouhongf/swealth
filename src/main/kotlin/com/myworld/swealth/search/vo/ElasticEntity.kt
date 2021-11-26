package com.myworld.swealth.search.vo

/**
 * 数据存储对象
 */
data class ElasticEntity<T>(
    /**
     * 主键标识，用户ES持久化
     */
    var id: String? = null,
    /**
     * JSON对象，实际存储数据
     */
    var data: MutableMap<*, *>? = null
)
