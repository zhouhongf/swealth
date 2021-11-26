package com.myworld.swealth.data.repository

import com.myworld.swealth.data.entity.Wordcloud
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository

@Repository
interface WordcloudRepository : MongoRepository<Wordcloud, String>
