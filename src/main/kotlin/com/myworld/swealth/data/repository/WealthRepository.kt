package com.myworld.swealth.data.repository

import com.myworld.swealth.data.entity.Wealth
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository

@Repository
interface WealthRepository : MongoRepository<Wealth, String> {
    fun findByBankName(bankName: String, pageable: Pageable): Page<Wealth>?
}
