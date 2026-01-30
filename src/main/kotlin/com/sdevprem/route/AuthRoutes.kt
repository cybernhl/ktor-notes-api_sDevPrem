package com.sdevprem.route

import com.sdevprem.data.auth.jwt.JwtConfig
import com.sdevprem.data.model.User
import com.sdevprem.data.service.UserService
import com.sdevprem.model.ApiResponse
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.core.qualifier.named
import org.koin.ktor.ext.inject
import kotlin.collections.map
import kotlin.collections.toMap
import kotlin.reflect.KClass
import kotlin.reflect.full.primaryConstructor

fun Route.authRoutes() {
    val userService by inject<UserService>()
    val secret by inject<String>(named("jwtSecret"))
    route("signup") {
        post {
            val user = call.receiveUserFromRequest()
                ?: return@post call.respond(
                    HttpStatusCode.BadRequest,
                    ApiResponse<Unit>(HttpStatusCode.BadRequest.value, "Missing or malformed user data", null)
                )
            call.handleUserSignup(user, userService, secret)
        }
    }

    route("login") {
        post {
            val user = call.receive<User>()
            try {
                val dbUser = userService.getUserByEmail(user.email)
                if (dbUser == null) {
                    return@post call.respond(HttpStatusCode.NotFound, "User not found")
                } else {
                    if (!userService.isUserPasswordValid(user.password, dbUser.password))
                        return@post call.respond(HttpStatusCode.BadRequest, "Invalid Credentials")
                    val token = JwtConfig.generateToken(dbUser, secret)
                    return@post call.respond(
                        HttpStatusCode.Accepted, mapOf(
                            "user" to dbUser.copy(password = user.password),
                            "token" to token
                        )
                    )
                }
            } catch (e: Exception) {
                println(e.message)
                call.respond(HttpStatusCode.InternalServerError, "Something went wrong")
            }
        }
    }
}

fun Application.registerAuthRoute() {
    routing {
        authRoutes()
    }
}


private suspend fun ApplicationCall.receiveUserFromRequest(): User? {
    return try {
        receive<User>()
    } catch (e: Exception) {
        try {
            val parameters = receiveParameters()
            val userName = parameters["userName"]
            val email = parameters["email"]
            val password = parameters["password"]

            if (userName != null && email != null && password != null) {
                User(userName = userName, email = email, password = password)
            } else {
                null // form-data 欄位不完整
            }
        } catch (e2: Exception) {
            null // 連 form-data 都接收失敗
        }
    }
}

suspend inline fun <reified T : Any> ApplicationCall.receiveOrNull(): T? {
    return try {
        receive<T>()
    } catch (e: Exception) {
        // application.log.debug("Failed to receive content: ${e.message}")
        null
    }
}

//suspend inline fun <reified T : Any> ApplicationCall.receiveOrNull(): T? {
//    // 優先嘗試接收 JSON
//    try {
//        return receive<T>()
//    } catch (e: Exception) {
//        // JSON 接收失敗，記錄日誌（可選），然後嘗試 form-data
//        application.log.debug("JSON receive failed for type ${T::class.simpleName}. Falling back to form-data. Error: ${e.message}")
//    }
//
//    // 回退機制：嘗試接收 form-data
//    return try {
//        val parameters = receiveParameters()
//        val kClass = T::class
//        val constructor = kClass.primaryConstructor ?: return null // 如果沒有主建構函式，直接失敗
//
//        // 將建構函式參數名與 form-data 的值映射起來
//        val constructorArgs = constructor.parameters
//            .map { param ->
//                val value = parameters[param.name!!]
//                // 這裡可以根據 param.type 進行更複雜的類型轉換（例如 String -> Int）
//                // 為了簡單起見，我們先假設所有參數都是 String
//                if (value == null && !param.isOptional) {
//                    // 如果一個必要的參數在 form-data 中找不到，則失敗
//                    return null
//                }
//                param to convertValue(value, param.type.classifier as KClass<*>)
//            }
//            .toMap()
//
//        // 使用反射呼叫建構函式來創建 T 的實例
//        constructor.callBy(constructorArgs)
//    } catch (e: Exception) {
//        // form-data 接收或反射呼叫也失敗了
//        application.log.debug("Form-data receive also failed for type ${T::class.simpleName}. Error: ${e.message}")
//        null
//    }
//}
//
//private fun convertValue(value: String?, targetClass: KClass<*>): Any? {
//    if (value == null) return null
//    return when (targetClass) {
//        String::class -> value
//        Int::class -> value.toIntOrNull()
//        Long::class -> value.toLongOrNull()
//        Boolean::class -> value.toBoolean()
//        // 可以根據需要添加更多類型轉換
//        else -> value // 預設返回字串，讓建構函式呼叫時自行處理
//    }
//}

private suspend fun ApplicationCall.handleUserSignup(
    user: User,
    userService: UserService,
    secret: String
) {
    try {
        if (userService.isUserEmailExist(user.email)) {
            respond(
                HttpStatusCode.Conflict, // 409 Conflict 更符合語意
                ApiResponse<Unit>(409, "User email already exists", null)
            )
        } else {
            val newUser = userService.insertUser(user)
            val token = JwtConfig.generateToken(newUser, secret)
            val responseData = mapOf("token" to token)
            val apiResponse = ApiResponse(
                code = 2000,
                message = "User ${newUser.userName} created successfully.",
                data = responseData
            )
            respond(HttpStatusCode.Created, apiResponse)
        }
    } catch (e: Exception) {
        // 在真實專案中，這裡應該記錄詳細的錯誤日誌
        println("Error during signup: ${e.message}")
        respond(
            HttpStatusCode.InternalServerError,
            ApiResponse<Unit>(500, "Something went wrong during user creation", null)
        )
    }
}