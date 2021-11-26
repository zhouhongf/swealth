package com.myworld.swealth

import org.springframework.boot.SpringApplication
import org.springframework.cloud.client.SpringCloudApplication
import org.springframework.cloud.openfeign.EnableFeignClients
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories


@EnableFeignClients
@SpringCloudApplication
@EnableMongoRepositories(basePackages = ["com.**.repository"])
open class SwealthApplication

fun main(args: Array<String>) {
    SpringApplication.run(SwealthApplication::class.java, *args)
}
