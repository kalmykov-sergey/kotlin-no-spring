package name.sergei.kalmykov

data class JSend<out T>(
        val status: Status,
        val data: T
) {
    enum class Status {
        SUCCESS, FAIL, ERROR
    }
}

