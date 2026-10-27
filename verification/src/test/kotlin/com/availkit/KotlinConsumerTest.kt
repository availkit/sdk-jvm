package com.availkit
import com.availkit.Models.*
import com.sun.net.httpserver.HttpServer
import java.net.InetSocketAddress
import java.nio.charset.StandardCharsets
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

class KotlinConsumerTest {
    @Test fun optionalNullsArePartOfTheKotlinTypeContract() {
        val cursor: String? = ResourcePage(emptyList(), null).nextAfter
        val pageId: String? = BookingPageSettings(null, "Example", false, false).pageId
        val email: String? = CustomerInput("Example").email
        assertNull(cursor)
        assertNull(pageId)
        assertNull(email)
    }

    @Test fun publicPageCapabilitiesAndRetainedIntentWorkFromRealKotlinConsumer() {
        val id = "a".repeat(32)
        val token = "A".repeat(43)
        val count = AtomicInteger()
        val replies = LinkedBlockingQueue<Pair<Int, String>>()
        val seen = LinkedBlockingQueue<Pair<String?, String>>()
        val server = HttpServer.create(InetSocketAddress("127.0.0.1", 0), 0)
        server.createContext("/") { exchange ->
            count.incrementAndGet()
            seen.add(exchange.requestHeaders.getFirst("Authorization") to String(exchange.requestBody.readAllBytes(), StandardCharsets.UTF_8))
            val response = replies.poll(2, TimeUnit.SECONDS) ?: error("No fixture reply")
            val body = response.second.toByteArray()
            exchange.responseHeaders.set("Content-Type", "application/json")
            exchange.responseHeaders.set("Cache-Control", "no-store")
            exchange.sendResponseHeaders(response.first, body.size.toLong())
            exchange.responseBody.use { it.write(body) }
        }
        server.start()
        try {
            PublicBookingClient(id, ClientOptions("http://127.0.0.1:${server.address.port}", 2000, 262144)).use { client ->
                val page = """{"id":"$id","title":"Example","environment":"test"}"""
                val offering = """{"id":"$id","name":"Consultation","durationMinutes":30,"timeZone":"UTC"}"""
                val booked = """{"id":"$id","start":"2030-01-01T09:00:00Z","end":"2030-01-01T09:30:00Z","status":"confirmed","offering":$offering,"page":$page,"pageEnabled":true}"""
                replies.add(200 to page)
                assertEquals("test", client.getPage().get().environment)
                assertNull(seen.take().first)
                val intent = client.prepareBook(PublicBookingInput(id, "2030-01-01T09:00:00Z", CustomerInput("Example", "guest@example.invalid", null)), Secrets.newIdempotencyKey(), token, token)
                val saved = intent.exportPrivate()
                replies.add(201 to booked)
                val booking = client.execute(RequestIntent.importPrivate(saved)).get()
                assertEquals(id, booking.id)
                assertEquals("Booking $token", seen.take().first)
                replies.add(200 to booked)
                assertEquals("confirmed", client.getBooking(booking.id, token).get().status)
                seen.take()
                val cancel = client.prepareCancel(booking.id, Secrets.newIdempotencyKey(), token)
                replies.add(200 to booked.replace("confirmed", "cancelled"))
                assertEquals("cancelled", client.execute(cancel).get().status)
                assertFalse(cancel.toString().contains(token))
                assertArrayEquals(saved, intent.exportPrivate())
                assertEquals(4, count.get())
            }
        } finally { server.stop(0) }
    }
}
