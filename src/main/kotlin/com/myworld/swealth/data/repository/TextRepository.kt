package com.myworld.swealth.data.repository

import com.myworld.swealth.data.entity.Text
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository


@Repository
interface TextRepository : MongoRepository<Text, String> {
    fun findByBankName(bankName: String, pageable: Pageable): Page<Text>?
}
