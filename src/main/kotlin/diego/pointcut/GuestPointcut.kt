package diego.pointcut

import com.sun.net.httpserver.HttpExchange
import mateo.Diego
import plsar.model.web.HttpRequest
import plsar.web.Pointcut

class GuestPointcut : Pointcut {
    override val key = "diego:guest"
    override val isEvaluation = true

    override fun isTrue(httpRequest: HttpRequest?, exchange: HttpExchange?): Boolean {
        return !Diego.isAuthenticated
    }

    override fun process(httpRequest: HttpRequest?, exchange: HttpExchange?): String {
        return "chess not!"
    }
}