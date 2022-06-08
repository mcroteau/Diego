package diego.pointcut

import com.sun.net.httpserver.HttpExchange
import diego.Diego
import plsar.model.web.HttpRequest
import plsar.web.Pointcut

class IdentityPointcut : Pointcut {
    override val key = "diego:id"
    override val isEvaluation = false

    override fun isTrue(httpRequest: HttpRequest?, exchange: HttpExchange?): Boolean {
        return true
    }

    override fun process(httpRequest: HttpRequest?, exchange: HttpExchange?): String {
        return Diego.get("userId")
    }
}