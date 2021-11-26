package com.myworld.swealth.data.entity

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.mapping.Field
import java.io.Serializable

@Document(collection = "text_wordcloud")
class Wordcloud(
    @Id
    @Field("_id")
    var id: String,
    var image: ByteArray
): Serializable
