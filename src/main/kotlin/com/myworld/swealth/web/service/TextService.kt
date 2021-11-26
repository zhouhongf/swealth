package com.myworld.swealth.web.service

import com.myworld.swealth.common.ApiResult
import java.io.IOException
import javax.servlet.http.HttpServletResponse


interface TextService {

    fun detail(id: String): ApiResult<Any?>
    @Throws(IOException::class)
    fun wordcloud(id: String, response: HttpServletResponse)

    fun textMore(bankName: String, pageSize: Int, pageIndex: Int): ApiResult<*>?
}
