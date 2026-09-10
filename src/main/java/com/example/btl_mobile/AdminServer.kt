package com.example.btl_mobile

import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.cio.*
import io.ktor.server.engine.*
import io.ktor.server.http.content.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.Serializable
import android.content.Context
import android.util.Log

import java.io.BufferedReader
import java.io.InputStreamReader
import android.content.res.AssetManager

@Serializable
data class OrderUpdateRequest(
    val id: Int,
    val status: String,
    val reason: String? = null
)

class AdminServer(private val context: Context, private val adminApi: AdminApi) {
    private val assets: AssetManager = context.assets

    private var server: ApplicationEngine? = null

    fun start(port: Int = 8080) {
        server = embeddedServer(CIO, port = port, host = "0.0.0.0") {
            install(ContentNegotiation) {
                json(Json {
                    prettyPrint = true
                    isLenient = true
                    ignoreUnknownKeys = true
                    encodeDefaults = true
                })
            }
            install(CORS) {
                anyHost()
                allowHeader(HttpHeaders.ContentType)
                allowMethod(HttpMethod.Options)
                allowMethod(HttpMethod.Get)
                allowMethod(HttpMethod.Post)
                allowMethod(HttpMethod.Put)
                allowMethod(HttpMethod.Patch)
                allowMethod(HttpMethod.Delete)
            }

            routing {
                // Serve static files from assets
                get("/") {
                    val stream = assets.open("admin_web/index.html")
                    val html = stream.bufferedReader().use { reader: BufferedReader -> 
                        reader.readText() 
                    }
                    call.respondText(html, ContentType.Text.Html)
                }

                get("/{path...}") {
                    val path = call.parameters.getAll("path")?.joinToString("/") ?: ""
                    try {
                        val stream = assets.open("admin_web/$path")
                        val content = stream.readBytes()
                        val contentType = when {
                            path.endsWith(".js") -> ContentType.Application.JavaScript
                            path.endsWith(".css") -> ContentType.Text.CSS
                            path.endsWith(".png") -> ContentType.Image.PNG
                            path.endsWith(".jpg") || path.endsWith(".jpeg") -> ContentType.Image.JPEG
                            else -> ContentType.Text.Html
                        }
                        call.respondBytes(content, contentType)
                    } catch (e: Exception) {
                        call.respond(HttpStatusCode.NotFound)
                    }
                }

                // API Routes
                route("/api") {
                    get("/rates") {
                        try {
                            call.respond(adminApi.getAllRates())
                        } catch (e: Exception) {
                            Log.e("AdminServer", "Error /api/rates: ${e.message}")
                            call.respond(HttpStatusCode.InternalServerError, e.message ?: "Error")
                        }
                    }
                    post("/rates/update") {
                        try {
                            val rate = call.receive<ShippingRate>()
                            if (adminApi.updateRate(rate)) {
                                call.respond(HttpStatusCode.OK, mapOf("status" to "success"))
                            } else {
                                call.respond(HttpStatusCode.BadRequest, mapOf("status" to "error"))
                            }
                        } catch (e: Exception) {
                            Log.e("AdminServer", "Error /api/rates/update: ${e.message}")
                            call.respond(HttpStatusCode.BadRequest, e.message ?: "Error")
                        }
                    }

                    get("/accounts/{type}") {
                        try {
                            val type = call.parameters["type"] ?: "khach_hang"
                            call.respond(adminApi.getAllAccounts(type))
                        } catch (e: Exception) {
                            Log.e("AdminServer", "Error /api/accounts: ${e.message}")
                            call.respond(HttpStatusCode.InternalServerError, e.message ?: "Error")
                        }
                    }
                    post("/accounts/toggle") {
                        try {
                            val params = call.receive<Map<String, String>>()
                            val id = params["id"]?.toIntOrNull() ?: 0
                            val status = params["status"] ?: ""
                            if (adminApi.toggleAccountStatus(id, status)) {
                                call.respond(HttpStatusCode.OK, mapOf("status" to "success"))
                            } else {
                                call.respond(HttpStatusCode.BadRequest)
                            }
                        } catch (e: Exception) {
                            Log.e("AdminServer", "Error /api/accounts/toggle: ${e.message}")
                            call.respond(HttpStatusCode.BadRequest)
                        }
                    }
                    post("/accounts/employee/add") {
                        try {
                            val acc = call.receive<Account>()
                            val id = adminApi.addEmployeeAccount(acc)
                            if (id != -1L) {
                                call.respond(HttpStatusCode.Created, mapOf("id" to id))
                            } else {
                                call.respond(HttpStatusCode.BadRequest)
                            }
                        } catch (e: Exception) {
                            Log.e("AdminServer", "Error /api/accounts/employee/add: ${e.message}")
                            call.respond(HttpStatusCode.BadRequest)
                        }
                    }
                    post("/accounts/employee/update") {
                        try {
                            val acc = call.receive<Account>()
                            if (adminApi.updateEmployeeAccount(acc)) {
                                call.respond(HttpStatusCode.OK)
                            } else {
                                call.respond(HttpStatusCode.BadRequest)
                            }
                        } catch (e: Exception) {
                            Log.e("AdminServer", "Error /api/accounts/employee/update: ${e.message}")
                            call.respond(HttpStatusCode.BadRequest)
                        }
                    }

                    get("/employees") {
                        try {
                            call.respond(adminApi.getAllEmployees())
                        } catch (e: Exception) {
                            Log.e("AdminServer", "Error /api/employees: ${e.message}")
                            call.respond(HttpStatusCode.InternalServerError)
                        }
                    }
                    post("/employees/upsert") {
                        try {
                            val emp = call.receive<Employee>()
                            if (adminApi.upsertEmployee(emp)) {
                                call.respond(HttpStatusCode.OK)
                            } else {
                                call.respond(HttpStatusCode.BadRequest)
                            }
                        } catch (e: Exception) {
                            Log.e("AdminServer", "Error /api/employees/upsert: ${e.message}")
                            call.respond(HttpStatusCode.BadRequest)
                        }
                    }

                    get("/orders") {
                        try {
                            val status = call.request.queryParameters["status"]
                            call.respond(adminApi.getOrders(status))
                        } catch (e: Exception) {
                            Log.e("AdminServer", "Error /api/orders: ${e.message}")
                            call.respond(HttpStatusCode.InternalServerError)
                        }
                    }
                    post("/orders/update") {
                        try {
                            val req = call.receive<OrderUpdateRequest>()
                            if (adminApi.updateOrderStatus(req.id, req.status, req.reason)) {
                                call.respond(HttpStatusCode.OK)
                            } else {
                                call.respond(HttpStatusCode.BadRequest)
                            }
                        } catch (e: Exception) {
                            Log.e("AdminServer", "Error /api/orders/update: ${e.message}")
                            call.respond(HttpStatusCode.BadRequest)
                        }
                    }
                    get("/image") {
                        val uriStr = call.request.queryParameters["uri"]
                        if (!uriStr.isNullOrBlank()) {
                            try {
                                if (uriStr.startsWith("file://")) {
                                    val path = uriStr.substring(7)
                                    val file = java.io.File(path)
                                    if (file.exists()) {
                                        call.respondFile(file)
                                    } else {
                                        call.respond(HttpStatusCode.NotFound)
                                    }
                                } else {
                                    val uri = android.net.Uri.parse(uriStr)
                                    this@AdminServer.context.contentResolver.openInputStream(uri)?.use { input ->
                                        call.respondBytes(input.readBytes(), ContentType.Image.Any)
                                    } ?: call.respond(HttpStatusCode.NotFound)
                                }
                            } catch (e: Exception) {
                                Log.e("AdminServer", "Error serving image: ${e.message}")
                                call.respond(HttpStatusCode.InternalServerError)
                            }
                        } else {
                            call.respond(HttpStatusCode.BadRequest)
                        }
                    }

                    get("/stats") {
                        try {
                            call.respond(adminApi.getDashboardStats())
                        } catch (e: Exception) {
                            Log.e("AdminServer", "Error /api/stats: ${e.message}")
                            call.respond(HttpStatusCode.InternalServerError)
                        }
                    }
                }
            }
        }.start(wait = false)
        Log.d("AdminServer", "Server started on port $port")
    }

    fun stop() {
        server?.stop(1000, 2000)
        Log.d("AdminServer", "Server stopped")
    }
}
