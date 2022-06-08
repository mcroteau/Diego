package diego.interceptor

import com.sun.net.httpserver.HttpExchange
import plsar.model.web.HttpRequest
import plsar.web.Interceptor
import diego.Diego

class DiegoInterceptor : Interceptor {
     override fun intercept(request: HttpRequest?, httpExchange: HttpExchange?) {
        Diego.SAVE(request)
        Diego.SAVE(httpExchange)
    }
}