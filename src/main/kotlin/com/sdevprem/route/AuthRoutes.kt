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


fun Route.authRoutes() {
    val userService by inject<UserService>()
    val secret by inject<String>(named("jwtSecret")) // ⭐ 從 Koin 獲取 secret

    route("signup") {
        post {
            val user = call.receive<User>()
            try {
                val isUserExist = userService.isUserEmailExist(user.email)
                if (isUserExist) {
                    return@post call.respond(HttpStatusCode.BadRequest, "User email already exist")
                } else {
                    val newUser = userService.insertUser(user)
                    val token = JwtConfig.generateToken(newUser, secret)
//                    val responseData = mapOf(
//                        "user" to newUser,
//                        "token" to token
//                    )
                    val responseData = mapOf(
                        "token" to token
                    )
//                    val apiResponse = ApiResponse(
//                        code = 2000,
//                        message = "User ${newUser.userName} created successfully.",
//                        data = responseData
//                    )
                    val apiResponse = ApiResponse(
                        code = 2000,
                        message = "User ${newUser.userName} created successfully.",
                        data = responseData
                    )
                    return@post call.respond(HttpStatusCode.OK, apiResponse)
                }
            } catch (e: Exception) {
                println(e.message)
                call.respond(HttpStatusCode.InternalServerError, "Something went wrong")
            }
        }
    }

//    route("signup") {
//        post {
//            val user = call.receiveUserFromRequest()
//            if (user == null) {
//                return@post call.respond(
//                    HttpStatusCode.BadRequest,
//                    ApiResponse<Unit>(400, "Missing or malformed user data", null)
//                )
//            }
//            call.handleUserSignup(user, userService, secret)
//        }
//    }

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

/**
 * 輔助擴充函式，嘗試從 JSON 或 form-data 中解析 User 物件
 */
private suspend fun ApplicationCall.receiveUserFromRequest(): User? {
    return try {
        // 優先嘗試接收 JSON
        receive<User>()
    } catch (e: Exception) {
        // 如果 JSON 失敗，嘗試接收 form-data
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