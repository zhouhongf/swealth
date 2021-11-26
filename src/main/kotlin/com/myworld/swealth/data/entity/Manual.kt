package com.myworld.swealth.data.entity

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.mapping.Field
import java.io.Serializable

@Document(collection = "MANUAL")
class Manual(
    @Id
    @Field("_id")
    var id: String? = null,
    var ukey: String? = null,
    @Field("bank_name")
    var bankName: String? = null,
    @Field("file_type")
    var fileType: String? = null,
    @Field("file_suffix")
    var fileSuffix: String? = null,
    var status: String? = null,
    @Field("create_time")
    var createTime: String? = null,
    var content: ByteArray? = null
): Serializable
