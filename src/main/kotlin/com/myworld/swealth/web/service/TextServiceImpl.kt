package com.myworld.swealth.web.service

import com.myworld.swealth.common.ApiResult
import com.myworld.swealth.common.ResultUtil
import com.myworld.swealth.data.repository.TextRepository
import com.myworld.swealth.data.repository.WordcloudRepository
import org.apache.commons.io.IOUtils
import org.apache.logging.log4j.LogManager
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import java.io.ByteArrayInputStream
import java.io.IOException
import javax.servlet.http.HttpServletResponse

@Service
class TextServiceImpl : TextService {

    private val log = LogManager.getRootLogger()

    @Autowired
    private lateinit var textRepository: TextRepository
    @Autowired
    private lateinit var wordcloudRepository: WordcloudRepository

    override fun detail(id: String): ApiResult<Any?> {
        val text = textRepository.findById(id)
        return if (text.isPresent) {
            ResultUtil.success(data = text)
        } else {
            ResultUtil.failure(code = -2, msg = "没有找到数据")
        }
    }

    @Throws(IOException::class)
    override fun wordcloud(id: String, response: HttpServletResponse) {
        val file = wordcloudRepository.findById(id)
        if (file.isPresent) {
            IOUtils.copy(ByteArrayInputStream(file.get().image), response.outputStream)
            response.contentType = "image/png"
        }
    }

    override fun textMore(bankName: String, pageSize: Int, pageIndex: Int): ApiResult<*>? {
        val pageable: Pageable = PageRequest.of(pageIndex, pageSize, Sort.Direction.DESC, "createTime")
        val textPaged = textRepository.findByBankName(bankName, pageable) ?: return ResultUtil.failure(-2, "没有数据")
        val texts = textPaged.content
        val textsList: MutableList<Any> = ArrayList()
        for (text in texts) {
            val textShort: MutableMap<String, Any> = HashMap()
            textShort["id"] = text.id!!
            textShort["name"] = text.name!!
            textShort["date"] = text.date!!
            textsList.add(textShort)
        }
        return ResultUtil.success(num = textPaged.totalElements, data = textsList)
    }

}
