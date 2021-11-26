package com.myworld.swealth.data.repository

import com.myworld.swealth.data.entity.Manual
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository

@Repository
interface ManualRepository : MongoRepository<Manual, String> {
    fun findByUkey(ukey: String): Manual?
}
