package com.myworld.swealth.data.entity

import com.fasterxml.jackson.annotation.JsonIgnore
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.mapping.Field
import java.io.Serializable

@Document(collection = "WEALTH")
class Wealth(
    @Id
    @Field("_id")
    var id: String? = null,
    @JsonIgnore
    var ukey: String? = null,

    @Indexed
    var code: String? = null,
    @Indexed
    @Field("code_register")
    var codeRegister: String? = null,

    @Indexed
    var name: String? = null,
    @Field("bank_name")
    var bankName: String? = null,
    @Field("bank_level")
    var bankLevel: String? = null,

    var risk: Int? = null,
    var term: Int? = null,
    @Field("term_looped")
    var termLooped: String? = null,
    var currency: String? = null,

    @Field("redeem_type")
    var redeemType: String? = null,
    @Field("fixed_type")
    var fixedType: String? = null,
    @Field("promise_type")
    var promiseType: String? = null,
    @Field("rate_type")
    var rateType: String? = null,

    @Field("rate_min")
    var rateMin: Float? = null,
    @Field("rate_max")
    var rateMax: Float? = null,
    @Field("rate_netvalue")
    var rateNetvalue: String? = null,

    @Field("amount_buy_min")
    var amountBuyMin: Long? = null,
    @Field("file_type")
    var fileType: String? = null,

    @JsonIgnore
    @Field("create_time")
    var createTime: String? = null
    ) : Serializable, Comparable<Wealth> {


    /**
     * 设置为升序排列，即小的在前，大的在后
     */
    override fun compareTo(other: Wealth): Int {
        return if (rateMax!! >= other.rateMax!!) {
            rateMin!!.compareTo(other.rateMin!!)
        } else {
            -1
        }
    }
}
