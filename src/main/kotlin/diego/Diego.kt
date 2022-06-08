package diego

import com.sun.net.httpserver.HttpExchange
import diego.support.DbAccess
import plsar.model.web.HttpRequest
import plsar.model.web.HttpSession
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.concurrent.ConcurrentHashMap
import kotlin.experimental.and

object Diego {
    const val USER_KEY: String = "user"
    const val HASH_256: String = "SHA-256"
    var dbAccess: DbAccess? = null
    var sessions: MutableMap<String, HttpSession> = ConcurrentHashMap<String, HttpSession>()

    //////// Thank you Apache Shiro! ////////
    private val requestStorage: ThreadLocal<HttpRequest> = InheritableThreadLocal<HttpRequest>()
    private val exchangeStorage: ThreadLocal<HttpExchange> = InheritableThreadLocal<HttpExchange>()
    fun SAVE(request: HttpRequest?) {
        requestStorage.set(request)
    }

    fun SAVE(exchange: HttpExchange?) {
        exchangeStorage.set(exchange)
    }

    val request: HttpRequest
        get() = requestStorage.get()
    val exchange: HttpExchange
        get() = exchangeStorage.get()

    fun hasRole(role: String?): Boolean {
        val user:Any? = Diego!!.user
        if (user != null) {
            val roles: Set<String?>? = dbAccess?.getRoles(user)
            if (roles!!.contains(role)) {
                return true
            }
        }
        return false
    }

    fun hasPermission(permission: String?): Boolean {
        val user:String? = Diego.user
        if (user != null) {
            val permissions: Set<String?>? = dbAccess?.getPermissions(user)
            if (permissions!!.contains(permission)) {
                return true
            }
        }
        return false
    }

    val user: String?
        get() {
            val req: HttpRequest = Diego.request
            val httpSession: HttpSession? = req.getSession(false)
            return if (httpSession != null) {
                httpSession.get(Diego.USER_KEY) as String
            } else ""
        }

    operator fun get(key: String): String {
        val req: HttpRequest = Diego.request
        val httpSession: HttpSession? = req.getSession(false)
        return if (httpSession != null) {
            java.lang.String.valueOf(httpSession?.get(key))
        } else ""
    }

    operator fun set(key: String, value: String): Boolean {
        val req: HttpRequest = Diego.request
        val httpSession: HttpSession? = req.getSession(false)
        if (httpSession != null) {
            httpSession.set(key, value)
        }
        return true
    }

    fun signin(username: String, passwordUntouched: String): Boolean {
        val hashed: String = Diego.hash(passwordUntouched)
        val password: String? = dbAccess?.getPassword(username)
        if (!isAuthenticated && password == hashed) {
            val req: HttpRequest = Diego.request
            val oldHttpSession: HttpSession? = req.getSession(false)
            if (oldHttpSession != null) {
                oldHttpSession.dispose()
            }
            val httpSession: HttpSession? = req.getSession(true)
            httpSession!!.set(Diego.USER_KEY, username)
            sessions[httpSession!!.id] = httpSession
            return true
        }
        return false
    }

    fun signout(): Boolean {
        val req: HttpRequest = Diego.request
        val httpSession: HttpSession? = req.getSession(false)
        if (httpSession != null) {
            httpSession.dispose()
            httpSession.remove(Diego.USER_KEY)
            if (sessions.containsKey(httpSession.id)) {
                sessions.remove(httpSession.id)
            }
        }
        return true
    }

    val isAuthenticated: Boolean
        get() {
            val req: HttpRequest = Diego.request
            if (req != null) {
                val httpSession: HttpSession? = req.getSession(false)
                if (httpSession != null && sessions.containsKey(httpSession.id)) {
                    return true
                }
            }
            return false
        }

    fun configure(dbAccess: DbAccess): Boolean {
        Diego.dbAccess = dbAccess
        return true
    }

    fun hash(password: String): String {
        var md: MessageDigest? = null
        val passwordHashed = StringBuffer()
        try {
            md = MessageDigest.getInstance(Diego.HASH_256)
            md.update(password.toByteArray())
            val byteData: ByteArray = md.digest()
            for (i in byteData.indices) {
                passwordHashed.append(Integer.toString((byteData[i] and 0xff.toByte()) + 0x100, 16).substring(1))
            }
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
        }
        return passwordHashed.toString()
    }
}