package com.myworld.swealth.data.model

enum class WealthTypeRisk(name: String, var value: Int) {
    LOW("低风险", 1), LOW_RARE("较低风险", 2), MIDDLE("中等风险", 3), HIGH_RARE("较高风险", 4), HIGH("高风险", 5);

    companion object {
        fun getNameByValue(value: Int): String? {
            for (wealthTypeRisk in values()) {
                if (wealthTypeRisk.value == value) {
                    return wealthTypeRisk.name
                }
            }
            return null
        }

        fun getValueByName(name: String): Int? {
            for (wealthTypeRisk in values()) {
                if (wealthTypeRisk.name == name) {
                    return wealthTypeRisk.value
                }
            }
            return null
        }
    }

}
