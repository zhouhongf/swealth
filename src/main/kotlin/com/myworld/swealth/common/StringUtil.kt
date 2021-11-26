package com.myworld.swealth.common

import org.apache.commons.lang.StringUtils
import org.apache.logging.log4j.LogManager
import java.util.regex.Pattern

/**
 * 字符串处理类
 * submail中可能需要使用第二个方法
 */
object StringUtil {
    private val log = LogManager.getRootLogger()
    private val placeSuffix = arrayOf("自治州", "自治县", "自治旗", "联合旗", "市辖区", "地区", "辖区", "左旗", "右旗", "前旗", "后旗", "中旗", "街道", "新区", "高新区", "开发区", "省", "市", "区", "县")

    fun isNotNull(string: String): Boolean {
        return StringUtils.isNotEmpty(string) && "null" != string
    }

    fun isNullOrEmpty(text: String?): Boolean {
        return text == null || text.trim { it <= ' ' }.isEmpty()
    }

    /**
     * 两种过滤匹配方式
     * 1、去掉特定后缀名称
     * 2、正则表达式匹配
     */
    fun filterPlaceName(name: String): String {
        var nameCopy = name
        // 去掉特定后缀，如果剩余name字数大于等于2的，则返回
        for (suffix in placeSuffix) {
            if (nameCopy.contains(suffix)) {
                nameCopy = nameCopy.replace(suffix, "")
            }
        }
        if (nameCopy.length > 1) {
            log.info("去掉后缀后的城市名字是：$nameCopy")
            return nameCopy
        }
        // 如果去掉后缀后，name只剩下1个字了，则使用正则表达式重新匹配
        val pattern = Pattern.compile("([\\u4e00-\\u9fa5]{2,})[市|区|县|盟|旗]")
        val matcher = pattern.matcher(name)
        return if (matcher.find()) matcher.group(1) else name
    }

    @JvmStatic
    fun isChineseLetters(word: String): Boolean {
        val pattern = Pattern.compile("[\\u4e00-\\u9fa5]+")
        val matcher = pattern.matcher(word)
        return matcher.find()
    }

    @JvmStatic
    fun hasWhiteSpace(word: String): Boolean {
        val pattern = Pattern.compile("\\s+")
        val matcher = pattern.matcher(word.trim { it <= ' ' })
        return matcher.find()
    }

    @JvmStatic
    fun isInteger(str: String): Boolean {
        val pattern = Pattern.compile("^[0-9]+$")
        val isInt = pattern.matcher(str)
        return isInt.matches()
    }

    @JvmStatic
    fun getFileMimeType(fileSuffix: String): String {
        val fileSuffixNet = fileSuffix.split(".")[1]
        var mimeType = "application/$fileSuffixNet"
        when (fileSuffixNet) {
            "text" -> mimeType = "text/plain"
            "docx" -> mimeType = "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
            "doc" -> mimeType = "application/msword"
            "pptx" -> mimeType = "application/vnd.openxmlformats-officedocument.presentationml.presentation"
            "ppt" -> mimeType = "application/vnd.ms-powerpoint"
            "xlsx" -> mimeType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
            "xls" -> mimeType = "application/vnd.ms-excel"
            "zip" -> mimeType = "application/x-zip-compressed"
            "rar" -> mimeType = "application/octet-stream"
            "pdf" -> mimeType = "application/pdf"
            "jpg" -> mimeType = "image/jpeg"
            "png" -> mimeType = "image/png"
            "apk" -> mimeType = "application/vnd.android.package-archive"
            "html" -> mimeType = "text/html"
            "htm" -> mimeType = "text/html"
            "stm" -> mimeType = "text/html"
        }
        return mimeType
    }
}
