
package example
import com.availkit.*
import com.availkit.Models.*
import com.fasterxml.jackson.databind.ObjectMapper
import java.nio.file.Paths
import java.util.concurrent.ExecutionException

// Public-page CLI consumer of exactly the same credential-free API used from Kotlin/Android.
fun main(args: Array<String>) {
    try {
        // docs:configuration:start
        require(args.size == 1)
        val config = ObjectMapper().readTree(PrivateFiles.read(Paths.get(args[0]), 16384))
        val intents = Paths.get(config.path("intentDirectory").asText())
        val options = ClientOptions(config.path("baseUrl").asText("https://api.availkit.com"), 10000, 262144)
        // docs:configuration:end
        PublicBookingClient(config.path("pageId").asText(), options).use { client ->
            // docs:lifecycle:start
            val page = client.getPage().get()
            require(page.environment == "test")
            val times = client.availability(config.path("offeringId").asText(), config.path("from").asText(), config.path("to").asText()).get().slots
            if (times.size < 2) { println("Fewer than two times available; no booking sent."); return }
            val bookingToken = Secrets.newBookingToken()
            val contactToken = Secrets.newContactToken()
            val create = client.prepareBook(PublicBookingInput(config.path("offeringId").asText(), times[0].start, CustomerInput(config.path("displayName").asText(), config.path("email").asText(), null)), Secrets.newIdempotencyKey(), bookingToken, contactToken)
            PrivateFiles.writeNew(intents.resolve("create.json"), create.exportPrivate())
            val booked = client.execute(create).get()
            var current = client.getBooking(booked.id, bookingToken).get()
            if (times.size > 1) {
                val move = client.prepareReschedule(current.id, RescheduleInput(current.start, times[1].start), Secrets.newIdempotencyKey(), bookingToken)
                PrivateFiles.writeNew(intents.resolve("move.json"), move.exportPrivate())
                current = client.execute(move).get()
            }
            val cancel = client.prepareCancel(current.id, Secrets.newIdempotencyKey(), bookingToken)
            PrivateFiles.writeNew(intents.resolve("cancel.json"), cancel.exportPrivate())
            println("Owned public test booking: ${client.execute(cancel).get().status}")
            // docs:lifecycle:end
        }
    } catch (failure: Exception) {
        val error = if (failure is ExecutionException) failure.cause else failure
        System.err.println(if (error is AvailKitException) error.toString() else "Stopped; retain private capabilities/intents. Do not automatically create again.")
        kotlin.system.exitProcess(1)
    }
}
