
package example
import com.availkit.*
import com.availkit.Models.*
import com.fasterxml.jackson.databind.ObjectMapper
import java.nio.file.Paths
import java.util.concurrent.ExecutionException

fun main(args: Array<String>) {
    try { runBooking(args) } catch (failure: Exception) {
        val error = if (failure is ExecutionException) failure.cause else failure
        System.err.println(if (error is AvailKitException) error.toString() else "Example stopped; retain private intents and reconcile before another mutation.")
        kotlin.system.exitProcess(1)
    }
}
fun runBooking(args: Array<String>) {
    // docs:configuration:start
    require(args.size == 1)
    val config = ObjectMapper().readTree(PrivateFiles.read(Paths.get(args[0]), 16384))
    require(config.path("environment").asText() == "test")
    val key = PrivateFiles.credential(Paths.get(config.path("credentialFile").asText()))
    val intents = Paths.get(config.path("intentDirectory").asText())
    val options = ClientOptions(config.path("baseUrl").asText("https://api.availkit.com"), 10000, 1048576)
    // docs:configuration:end
    WorkspaceClient(config.path("workspaceId").asText(), com.availkit.Environment.TEST, key, options).use { client ->
        // docs:lifecycle:start
        val times = client.availability(config.path("offeringId").asText(), config.path("from").asText(), config.path("to").asText()).get().slots
        if (times.size < 2) { println("Fewer than two available times; no booking sent."); return }
        val create = client.prepareBook(BookingInput(config.path("offeringId").asText(), times[0].start), Secrets.newIdempotencyKey())
        PrivateFiles.writeNew(intents.resolve("create.json"), create.exportPrivate())
        val booked = client.execute(create).get()
        var current = client.getBooking(booked.id).get()
        if (times.size > 1) {
            val move = client.prepareRescheduleBooking(current.id, RescheduleInput(current.start, times[1].start), Secrets.newIdempotencyKey())
            PrivateFiles.writeNew(intents.resolve("move.json"), move.exportPrivate())
            current = client.execute(move).get()
        }
        val cancel = client.prepareCancel(current.id, Secrets.newIdempotencyKey())
        PrivateFiles.writeNew(intents.resolve("cancel.json"), cancel.exportPrivate())
        println("Owned test booking lifecycle: ${client.execute(cancel).get().status}")
        // docs:lifecycle:end
    }
}
