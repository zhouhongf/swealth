package com.myworld.swealth.search.vo

/**
 * http交互Vo对象
 */
data class ElasticDataVo<T>(var idxName: String, var elasticEntity: ElasticEntity<*>)
