
package example;
import com.availkit.*;
import com.availkit.Models.*;
import com.fasterxml.jackson.databind.*;
import java.nio.file.*;
import java.util.concurrent.*;
public final class FirstBooking {
 public static void main(String[] args) {
  try { run(args); } catch(Exception error) { Throwable e=error instanceof ExecutionException?error.getCause():error;System.err.println(e instanceof AvailKitException?e.toString():"Example stopped; inspect retained private intents before any new mutation.");System.exit(1); }
 }
 static void run(String[] args)throws Exception {
  // docs:configuration:start
  if(args.length!=1)throw new IllegalArgumentException();
  JsonNode config=new ObjectMapper().readTree(PrivateFiles.read(Paths.get(args[0]),16384));
  if(!config.path("environment").asText().equals("test"))throw new IllegalArgumentException();
  String credential=PrivateFiles.credential(Paths.get(config.path("credentialFile").asText()));
  Path intents=Paths.get(config.path("intentDirectory").asText());
  ClientOptions options=new ClientOptions(config.path("baseUrl").asText("https://api.availkit.com"),10000,1048576);
  // docs:configuration:end
  try(WorkspaceClient client=new WorkspaceClient(config.path("workspaceId").asText(),com.availkit.Environment.TEST,credential,options)) {
   // docs:lifecycle:start
   Availability available=client.availability(config.path("offeringId").asText(),config.path("from").asText(),config.path("to").asText()).get();
   if(available.slots.size()<2){System.out.println("Fewer than two available times; no booking sent.");return;}
   RequestIntent create=client.prepareBook(new BookingInput(config.path("offeringId").asText(),available.slots.get(0).start),Secrets.newIdempotencyKey());
   PrivateFiles.writeNew(intents.resolve("create.json"),create.exportPrivate());
   Booking booked=client.execute(create).get();
   Booking current=client.getBooking(booked.id).get();
   if(available.slots.size()>1) {
    RequestIntent move=client.prepareRescheduleBooking(current.id,new RescheduleInput(current.start,available.slots.get(1).start),Secrets.newIdempotencyKey());
    PrivateFiles.writeNew(intents.resolve("move.json"),move.exportPrivate());
    current=client.execute(move).get();
   }
   RequestIntent cancel=client.prepareCancel(current.id,Secrets.newIdempotencyKey());
   PrivateFiles.writeNew(intents.resolve("cancel.json"),cancel.exportPrivate());
   Booking cancelled=client.execute(cancel).get();
   System.out.println("Owned test booking lifecycle: "+cancelled.status);
   // docs:lifecycle:end
  }
 }
}
