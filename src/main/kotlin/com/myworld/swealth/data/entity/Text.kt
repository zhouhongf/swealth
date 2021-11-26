package com.myworld.swealth.data.entity

import com.fasterxml.jackson.annotation.JsonIgnore
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.mapping.Field
import java.io.Serializable

@Document(collection = "TEXT")
class Text(
    @Id
    @Field("_id")
    @JsonIgnore
    var id: String? = null,
    @Field("bank_name")
    var bankName: String? = null,
    @JsonIgnore
    @Field("bank_level")
    var bankLevel: String? = null,

    var name: String? = null,
    var date: String? = null,
    @JsonIgnore
    @Field("type_main")
    var typeMain: String? = null,
    @JsonIgnore
    @Field("type_next")
    var typeNext: String? = null,

    var url: String? = null,
    @JsonIgnore
    var status: String? = null,
    @JsonIgnore
    @Field("create_time")
    var createTime: String? = null,
    var content: String? = null
): Serializable
