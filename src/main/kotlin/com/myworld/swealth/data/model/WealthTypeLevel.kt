package com.myworld.swealth.data.model

enum class WealthTypeLevel(name: String, var value: String) {
    STATE("国有银行", "STATE"), COMP("股份银行", "COMP"), CITY("城商银行", "CITY"), COUNTY("农商银行", "COUNTY"), ALL("全部银行", "ALL"), BOARD("上市银行", "BOARD"), LOCAL("当地银行", "LOCAL"), CUSTOM("会员定制", "CUSTOM");

    companion object {
        fun getNameByValue(value: String): String? {
            for (wealthTypeLevel in values()) {
                if (wealthTypeLevel.value == value) {
                    return wealthTypeLevel.name
                }
            }
            return null
        }

        fun getValueByName(name: String): String? {
            for (wealthTypeLevel in values()) {
                if (wealthTypeLevel.name == name) {
                    return wealthTypeLevel.value
                }
            }
            return null
        }
    }

}
