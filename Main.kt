import io.ktor.auth.*
import io.ktor.auth.jwt.*
import io.ktor.features.*
import io.ktor.http.cio.websocket.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.websocket.*
import kotlinx.coroutines.channels.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.*
import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.sun.org.apache.xml.internal.security.algorithms.Algorithm
import io.ktor.application.*
import io.ktor.http.*
import java.util.Date

object Posts : Table() {
    val id = integer("id").autoIncrement().primaryKey()
    val title = varchar("title", 255)
    val content = text("content")
}

@Serializable
data class Post(val title: String, val content: String)

fun main() {
    Database.connect("jdbc:h2:mem:test", driver = "org.h2.Driver")

    transaction {
        SchemaUtils.create(Posts)
    }

    embeddedServer(Netty, port = 8080) {
        install(WebSockets)
        install(Authentication) {
            jwt("auth-jwt") {
                realm = "ktor sample app"
                verifier(JwtConfig.verifier)
                validate { credential ->
                    if (credential.payload.getClaim("name").asString() != "") {
                        JWTPrincipal(credential.payload)
                    } else null
                }
            }
        }
        routing {
            // Public endpoint accessible without authentication
            get("/") {
                call.respondText("Public endpoint")
            }

            // Protected endpoints that require authentication
            authenticate("auth-jwt") {
                get("/protected") {
                    call.respondText("You are authenticated")
                }

                // WebSocket endpoint for secure chat
                webSocket("/secure-chat") {
                    send("You are connected securely!")
                    for (frame in incoming) {
                        when (frame) {
                            is Frame.Text -> {
                                val receivedText = frame.readText()
                                send("You said: $receivedText")
                            }

                            is Frame.Binary -> TODO()
                            is Frame.Close -> TODO()
                            is Frame.Ping -> TODO()
                            is Frame.Pong -> TODO()
                        }
                    }
                }

                // Handling HTTP POST requests at /submit endpoint
                post("/submit") {
                    val post = call.receive<Post>()
                    transaction {
                        Posts.insert {
                            it[title] = post.title
                            it[content] = post.content
                        }
                    }
                    call.respond(HttpStatusCode.OK, "Received post with title: ${post.title}")
                }

                // Retrieve posts from the database
                get("/posts") {
                    val posts = transaction {
                        Posts.selectAll().map {
                            Post(it[Posts.title], it[Posts.content])
                        }
                    }
                    call.respond(posts)
                }
            }
        }
    }.start(wait = true)
}

// Object for JWT configuration
object JwtConfig {
    private const val secret = "secret"
    private const val issuer = "ktor.io"
    private const val validityInMs = 36_000_00 * 10 // 10 hours

    private val algorithm = Algorithm.HMAC512(secret)

    val verifier: JWTVerifier = JWT.require(algorithm)
        .withIssuer(issuer)
        .build()

    fun generateToken(name: String): String = JWT.create()
        .withSubject("Authentication")
        .withIssuer(issuer)
        .withClaim("name", name)
        .withExpiresAt(getExpiration())
        .sign(algorithm)

    private fun getExpiration() = Date(System.currentTimeMillis() + validityInMs)
}
