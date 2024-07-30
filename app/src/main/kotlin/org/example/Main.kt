package org.example

import io.ktor.application.*
import io.ktor.features.ContentNegotiation
import io.ktor.http.*
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
        install(ContentNegotiation) {
            json(Json { prettyPrint = true; isLenient = true })
        }
        routing {
            get("/") {
                call.respondText("Public endpoint")
            }

            webSocket("/secure-chat") {
                send("You are connected!")
                for (frame in incoming) {
                    when (frame) {
                        is Frame.Text -> {
                            val receivedText = frame.readText()
                            send("You said: $receivedText")
                        }
                    }
                }
            }

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

            get("/posts") {
                val posts = transaction {
                    Posts.selectAll().map {
                        Post(it[Posts.title], it[Posts.content])
                    }
                }
                call.respond(posts)
            }
        }
    }.start(wait = true)
}
