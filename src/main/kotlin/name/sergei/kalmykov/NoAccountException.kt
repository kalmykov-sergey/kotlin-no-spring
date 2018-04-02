package name.sergei.kalmykov

class NoAccountException(accountId: Long) : RuntimeException("No account $accountId")
