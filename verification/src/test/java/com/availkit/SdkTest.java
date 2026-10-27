package com.availkit;
import com.availkit.Models.*;
import com.fasterxml.jackson.databind.*;
import com.sun.net.httpserver.*;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import java.io.*;
import java.lang.reflect.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.*;
import java.time.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import javax.crypto.*;
import javax.crypto.spec.*;

final class SdkTest {
 static final String ID="a".repeat(32),OTHER="b".repeat(32),TOKEN="A".repeat(43),KEY="original-intent-key-0001";
 private interface Fifo extends com.sun.jna.Library { int mkfifo(String path,int mode); }
 static final ObjectMapper JSON=new ObjectMapper();
 static JsonNode fixtures;
 @BeforeAll static void load()throws Exception{try(InputStream s=SdkTest.class.getResourceAsStream("/fixtures.json")){fixtures=JSON.readTree(s);}}
 static final class Reply {int status;String body;boolean noStore=true,chunked=false,drop=false;long byteDelay;Reply(int status,String body){this.status=status;this.body=body;}}
 static final class Captured {final String path,method,auth,key,contact,cookie;final byte[] body;Captured(HttpExchange e)throws Exception{path=e.getRequestURI().toString();method=e.getRequestMethod();auth=e.getRequestHeaders().getFirst("Authorization");key=e.getRequestHeaders().getFirst("Idempotency-Key");contact=e.getRequestHeaders().getFirst("X-AvailKit-Contact-Token");cookie=e.getRequestHeaders().getFirst("Cookie");body=e.getRequestBody().readAllBytes();}}
 static final class Server implements AutoCloseable {
  final HttpServer server;final ExecutorService pool=Executors.newCachedThreadPool();final BlockingQueue<Reply> replies=new LinkedBlockingQueue<>();final BlockingQueue<Captured> calls=new LinkedBlockingQueue<>();final AtomicInteger count=new AtomicInteger();
  Server()throws Exception{server=HttpServer.create(new InetSocketAddress("127.0.0.1",0),0);server.setExecutor(pool);server.createContext("/",e->{try{Captured c=new Captured(e);count.incrementAndGet();calls.add(c);Reply r=replies.poll(3,TimeUnit.SECONDS);if(r==null){e.close();return;}if(r.drop){e.close();return;}byte[] body=r.body.getBytes(StandardCharsets.UTF_8);e.getResponseHeaders().set("Content-Type","application/json");if(r.noStore)e.getResponseHeaders().set("Cache-Control","no-store");if(r.status>=300&&r.status<400)e.getResponseHeaders().set("Location","/redirected");if(r.status==408||r.status==503)e.getResponseHeaders().set("Retry-After","0");e.sendResponseHeaders(r.status,r.chunked?0:body.length);try(OutputStream out=e.getResponseBody()){if(r.byteDelay>0)for(byte b:body){out.write(b);out.flush();Thread.sleep(r.byteDelay);}else out.write(body);}}catch(Exception ignored){}finally{e.close();}});server.start();}
  ClientOptions options(){return options(2000,1048576);}ClientOptions options(long ms,int max){return new ClientOptions("http://127.0.0.1:"+server.getAddress().getPort(),ms,max);}
  void reply(String op){JsonNode r=Protocol.route(op);replies.add(new Reply(r.path("status").asInt(),fixtures.path(op).path("response").toString()));}
  Captured take()throws Exception{Captured c=calls.poll(2,TimeUnit.SECONDS);assertNotNull(c);return c;}
  public void close(){server.stop(0);pool.shutdownNow();}
 }
 static String methodName(String op){switch(op){case"getPublicPage":return"getPage";case"listPublicOfferings":return"listOfferings";case"publicAvailability":return"availability";case"bookPublic":return"book";case"getPublicBooking":return"getBooking";case"cancelPublicBooking":return"cancel";case"reschedulePublicBooking":return"reschedule";case"setHours":return"replaceHours";case"updateResource":return"replaceResource";case"updateOffering":return"replaceOffering";default:return op;}}
 static List<Object> args(String op)throws Exception{
  JsonNode s=Protocol.route(op),f=fixtures.path(op);List<Object>a=new ArrayList<>();String path=s.path("path").asText().replaceAll("\\{(?:workspaceId|pageId)\\}","");java.util.regex.Matcher m=java.util.regex.Pattern.compile("\\{([^}]+)\\}").matcher(path);while(m.find())a.add(f.path("ids").path(m.group(1)).asText(ID));
  JsonNode input=s.path("request");String type=input.path("$ref").asText().replaceAll(".*/","");if(!input.isNull()&&!type.isEmpty()&&!Arrays.asList("Empty","WebhookDisable").contains(type))a.add(JSON.treeToValue(f.path("request"),Class.forName("com.availkit.Models$"+type)));
  for(JsonNode p:s.path("parameters")){String n=p.path("name").asText();if(p.path("schema").path("type").asText().equals("integer"))a.add(f.path("query").path(n).asLong(p.path("schema").path("default").asLong()));else a.add(f.path("query").has(n)?f.path("query").path(n).asText():null);}
  if(s.path("idempotent").asBoolean())a.add(KEY);if(s.path("public").asBoolean()&&Arrays.asList("bookPublic","getPublicBooking","cancelPublicBooking","reschedulePublicBooking").contains(op)){a.add(TOKEN);if(op.equals("bookPublic"))a.add(TOKEN);}return a;
 }
 @Test void everyTypedOperationUsesExactScopedWire()throws Exception{
  try(Server server=new Server();WorkspaceClient workspace=new WorkspaceClient(ID,Environment.TEST,TOKEN,server.options());PublicBookingClient publicClient=new PublicBookingClient(ID,server.options())){
   int workspaceCount=0,publicCount=0;Iterator<String> ops=Protocol.CONTRACT.path("routes").fieldNames();while(ops.hasNext()){
    String op=ops.next();JsonNode spec=Protocol.route(op);Object client=spec.path("public").asBoolean()?publicClient:workspace;String name=methodName(op);List<Object> args=args(op);Method method=Arrays.stream(client.getClass().getMethods()).filter(x->x.getName().equals(name)&&x.getParameterCount()==args.size()).findFirst().orElseThrow();server.reply(op);
    Object result=((CompletableFuture<?>)method.invoke(client,args.toArray())).get(3,TimeUnit.SECONDS);assertNotNull(result,op);assertTrue(result.toString().contains("redacted"));Captured call=server.take();String expected=spec.path("path").asText().replaceAll("\\{[^}]+\\}",ID);assertEquals(expected,call.path.split("\\?",2)[0],op);assertEquals(spec.path("method").asText(),call.method);assertNull(call.cookie);
    if(spec.path("public").asBoolean()){publicCount++;assertFalse(call.auth!=null&&call.auth.startsWith("Bearer"));}else{workspaceCount++;assertEquals("Bearer "+TOKEN,call.auth);}
    if(!spec.path("request").isNull())assertEquals(fixtures.path(op).path("request"),JSON.readTree(call.body),op);else assertEquals(0,call.body.length);
    assertEquals(spec.path("idempotent").asBoolean()?KEY:null,call.key);if(op.equals("bookPublic")){assertEquals("Booking "+TOKEN,call.auth);assertEquals(TOKEN,call.contact);}
   }assertEquals(34,workspaceCount);assertEquals(7,publicCount);assertEquals(41,server.count.get());
  }
 }
 static AvailKitException failed(CompletableFuture<?> future)throws Exception{ExecutionException e=assertThrows(ExecutionException.class,()->future.get(3,TimeUnit.SECONDS));assertInstanceOf(AvailKitException.class,e.getCause());return(AvailKitException)e.getCause();}
 @Test void redactedStructuredErrorsAndPermissions()throws Exception{
  try(Server s=new Server();WorkspaceClient c=new WorkspaceClient(ID,Environment.TEST,TOKEN,s.options())){
   for(int status:new int[]{401,403,409,429}){s.replies.add(new Reply(status,"{\"error\":{\"code\":\"scope_denied\",\"message\":\""+TOKEN+"\"}}"));AvailKitException e=failed(c.book(new BookingInput(ID,"2030-01-01T09:00:00Z"),KEY));assertEquals("api",e.kind);assertEquals("scope_denied",e.code);assertFalse(e.uncertain);assertFalse(e.toString().contains(TOKEN));assertNull(e.getCause());}
   s.replies.add(new Reply(403,"{\"error\":{\"code\":\""+TOKEN+"\",\"message\":\"private\"}}"));AvailKitException unrecognized=failed(c.book(new BookingInput(ID,"2030-01-01T09:00:00Z"),Secrets.newIdempotencyKey()));assertEquals("api_error",unrecognized.code);assertTrue(unrecognized.uncertain);assertFalse(unrecognized.toString().contains(TOKEN));assertEquals(5,s.count.get());
  }
 }
 @Test void lostMutationIsSentOnceAndExplicitReplayPreservesBytes()throws Exception{
  try(Server s=new Server();WorkspaceClient c=new WorkspaceClient(ID,Environment.TEST,TOKEN,s.options())){
   RequestIntent original=c.prepareBook(new BookingInput(ID,"2030-01-01T09:00:00Z"),KEY);byte[] retained=original.exportPrivate();Reply drop=new Reply(201,"");drop.drop=true;s.replies.add(drop);AvailKitException error=failed(c.execute(original));assertTrue(error.uncertain);Captured first=s.take();Thread.sleep(100);assertEquals(1,s.count.get());assertArrayEquals(retained,original.exportPrivate());
   RequestIntent restored=RequestIntent.importPrivate(retained);s.reply("book");assertEquals(ID,c.execute(restored).get().id);Captured second=s.take();assertArrayEquals(first.body,second.body);assertEquals(first.key,second.key);assertEquals(first.auth,second.auth);assertFalse(original.toString().contains(KEY));assertEquals(2,s.count.get());
   try(WorkspaceClient live=new WorkspaceClient(ID,Environment.LIVE,TOKEN,s.options());WorkspaceClient foreign=new WorkspaceClient(OTHER,Environment.TEST,TOKEN,s.options());WorkspaceClient changedKey=new WorkspaceClient(ID,Environment.TEST,"B".repeat(42)+"A",s.options())){assertThrows(AvailKitException.class,()->live.execute(restored));assertThrows(AvailKitException.class,()->foreign.execute(restored));assertThrows(AvailKitException.class,()->changedKey.execute(restored));}
  }
 }
 @Test void unsafeExpiredFutureAndTamperedIntentsNeverSend()throws Exception{
  try(Server s=new Server();WorkspaceClient c=new WorkspaceClient(ID,Environment.TEST,TOKEN,s.options())){
   RequestIntent i=c.prepareCancel(ID,KEY);JsonNode original=JSON.readTree(i.exportPrivate());for(String change:new String[]{"expired","future","nearFuture","path","operation","headers"}){com.fasterxml.jackson.databind.node.ObjectNode m=original.deepCopy();if(change.equals("expired"))m.put("createdAt",Instant.now().minusSeconds(86401).toString());if(change.equals("future"))m.put("createdAt",Instant.now().plusSeconds(30).toString());if(change.equals("nearFuture"))m.put("createdAt",Instant.now().plusSeconds(1).toString());if(change.equals("path"))m.put("path","/customers/"+ID+"/erase");if(change.equals("operation"))m.put("operation","eraseCustomer");if(change.equals("headers"))m.set("headers",JSON.createObjectNode().put("Authorization","Bearer "+TOKEN));RequestIntent changed=RequestIntent.importPrivate(JSON.writeValueAsBytes(m));assertThrows(AvailKitException.class,()->c.execute(changed));}assertEquals(0,s.count.get());
  }
 }
 @Test void redirectsAnd408503DoNotFollowOrRetry()throws Exception{
  try(Server s=new Server();WorkspaceClient c=new WorkspaceClient(ID,Environment.TEST,TOKEN,s.options())){for(int status:new int[]{307,308,408,503}){s.replies.add(new Reply(status,"{\"error\":{\"code\":\"unavailable\",\"message\":\"unavailable\"}}"));failed(c.book(new BookingInput(ID,"2030-01-01T09:00:00Z"),Secrets.newIdempotencyKey()));s.take();}Thread.sleep(100);assertEquals(4,s.count.get());}
 }
 @Test void totalDeadlineAndCancellationRemainSendOnce()throws Exception{
  try(Server s=new Server();WorkspaceClient c=new WorkspaceClient(ID,Environment.TEST,TOKEN,s.options(180,1048576))){Reply trickle=new Reply(201,fixtures.path("book").path("response").toString());trickle.chunked=true;trickle.byteDelay=40;s.replies.add(trickle);long start=System.nanoTime();AvailKitException e=failed(c.book(new BookingInput(ID,"2030-01-01T09:00:00Z"),KEY));assertTrue(e.uncertain);assertEquals("timeout",e.code);assertTrue(Duration.ofNanos(System.nanoTime()-start).toMillis()<1200);assertEquals(1,s.count.get());}
  try(Server s=new Server();WorkspaceClient c=new WorkspaceClient(ID,Environment.TEST,TOKEN,s.options())){Reply trickle=new Reply(201,fixtures.path("book").path("response").toString());trickle.byteDelay=40;s.replies.add(trickle);CompletableFuture<Booking> f=c.book(new BookingInput(ID,"2030-01-01T09:00:00Z"),KEY);s.take();assertTrue(f.cancel(true));assertTrue(f.isCancelled());assertEquals(1,s.count.get());}
 }
 @Test void malformedOversizedAndCrossScopeSuccessRejected()throws Exception{
  try(Server s=new Server();WorkspaceClient c=new WorkspaceClient(ID,Environment.TEST,TOKEN,s.options())){
   for(String response:new String[]{"{}","null",fixtures.path("getBooking").path("response").toString().replace("\"id\":\""+ID,"\"id\":\""+OTHER),"{\"id\":\""+ID+"\",\"id\":\""+ID+"\"}"}){s.replies.add(new Reply(200,response));assertEquals("protocol",failed(c.getBooking(ID)).kind);}
  }
  try(Server s=new Server();PublicBookingClient c=new PublicBookingClient(ID,s.options())){Reply missingCache=new Reply(200,fixtures.path("getPublicPage").path("response").toString());missingCache.noStore=false;s.replies.add(missingCache);assertEquals("invalid_response",failed(c.getPage()).code);s.replies.add(new Reply(200,fixtures.path("getPublicPage").path("response").toString().replace(ID,OTHER)));assertEquals("invalid_response",failed(c.getPage()).code);}
  try(Server s=new Server();WorkspaceClient c=new WorkspaceClient(ID,Environment.TEST,TOKEN,s.options(1000,32))){s.reply("getBooking");assertEquals("response_too_large",failed(c.getBooking(ID)).code);Reply r=new Reply(200," ".repeat(64));r.chunked=true;s.replies.add(r);assertEquals("response_too_large",failed(c.getBooking(ID)).code);}
 }
 @Test void malformedHoursAcknowledgmentsAreUnknown()throws Exception{try(Server s=new Server();WorkspaceClient c=new WorkspaceClient(ID,Environment.TEST,TOKEN,s.options())){for(String windows:new String[]{"[{\"weekday\":1,\"start\":\"25:00\",\"end\":\"26:00\"}]","[{\"weekday\":1,\"start\":\"23:00\",\"end\":\"24:30\"}]","[{\"weekday\":1,\"start\":\"09:00\",\"end\":\"11:00\"},{\"weekday\":1,\"start\":\"10:00\",\"end\":\"12:00\"}]"}){s.replies.add(new Reply(200,"{\"windows\":"+windows+"}"));AvailKitException e=failed(c.replaceHours(ID,new Hours(Arrays.asList(new HoursWindow(1,"09:00","10:00")))));assertEquals("protocol",e.kind);assertTrue(e.uncertain);}assertEquals(3,s.count.get());}}
 @Test void malformedOneTimeSecretFailsClosed()throws Exception {try(Server s=new Server();WorkspaceClient c=new WorkspaceClient(ID,Environment.TEST,TOKEN,s.options())){com.fasterxml.jackson.databind.node.ObjectNode response=fixtures.path("createWebhook").path("response").deepCopy();response.put("signingSecret",TOKEN+"\n");s.replies.add(new Reply(201,response.toString()));AvailKitException e=failed(c.createWebhook(new WebhookCreate("https://receiver.example.invalid/hook")));assertEquals("protocol",e.kind);assertTrue(e.uncertain);assertEquals(1,s.count.get());}}
 @Test void invalidInputsDoNotSendAndCollectionsAreImmutable()throws Exception{
  try(Server s=new Server();WorkspaceClient c=new WorkspaceClient(ID,Environment.TEST,TOKEN,s.options())){assertThrows(AvailKitException.class,()->c.listResources(null,101,null));assertThrows(AvailKitException.class,()->c.listResources("bad",25,null));assertThrows(AvailKitException.class,()->c.listResources(null,25," \n"));assertThrows(AvailKitException.class,()->c.prepareBook(new BookingInput("bad","2030-01-01T09:00:00Z"),KEY));assertThrows(AvailKitException.class,()->c.availability(ID,"2030-01-01T09:00:00Z","2030-01-09T09:00:00Z"));assertThrows(AvailKitException.class,()->c.replaceHours(ID,new Hours(Arrays.asList(new HoursWindow(1,"09:00","11:00"),new HoursWindow(1,"10:00","12:00")))));assertThrows(AvailKitException.class,()->c.prepareRescheduleBooking(ID,new RescheduleInput("2030-01-01T09:00:00.0001Z","2030-01-01T10:00:00Z"),KEY));assertEquals(0,s.count.get());List<Resource> source=new ArrayList<>();ResourcePage page=new ResourcePage(source,null);source.add(new Resource(ID,"x","UTC",1,false));assertTrue(page.items.isEmpty());assertThrows(UnsupportedOperationException.class,()->page.items.clear());}
  assertThrows(AvailKitException.class,()->new ClientOptions("https://key@api.availkit.com",1,1));assertThrows(AvailKitException.class,()->new ClientOptions("http://api.availkit.com",1,1));
 }

 @Test void privateFileFifoAndDescriptorModesFailClosed()throws Exception {
  Path dir=Files.createTempDirectory("availkit-jvm-fifo-");Files.setPosixFilePermissions(dir,PosixFilePermissions.fromString("rwx------"));
  Path fifo=dir.resolve("pipe");assertEquals(0,com.sun.jna.Native.load("c",Fifo.class).mkfifo(fifo.toString(),0x180));
  try {
   long start=System.nanoTime();assertThrows(AvailKitException.class,()->PrivateFiles.read(fifo,4096));assertTrue(Duration.ofNanos(System.nanoTime()-start).toMillis()<500);
   Path empty=dir.resolve("empty");Files.write(empty,new byte[0]);Files.setPosixFilePermissions(empty,PosixFilePermissions.fromString("rw-------"));assertThrows(AvailKitException.class,()->PrivateFiles.read(empty,4096));
   Path key=dir.resolve("key");Files.write(key,TOKEN.getBytes(StandardCharsets.US_ASCII));Files.setPosixFilePermissions(key,PosixFilePermissions.fromString("rw-r--r--"));assertThrows(AvailKitException.class,()->PrivateFiles.read(key,4096));Files.setPosixFilePermissions(key,PosixFilePermissions.fromString("rw-------"));assertEquals(TOKEN,PrivateFiles.credential(key));assertThrows(AvailKitException.class,()->PrivateFiles.read(key,0));
   Files.delete(key);Files.delete(empty);
  } finally {Files.delete(fifo);Files.delete(dir);}
 }
 @Test void rawWebhookVerificationAndPrivateFiles()throws Exception{
  byte[] body="{\"event\":1}".getBytes(StandardCharsets.UTF_8);Mac mac=Mac.getInstance("HmacSHA256");mac.init(new SecretKeySpec(Base64.getUrlDecoder().decode(TOKEN),"HmacSHA256"));mac.update("1000.".getBytes(StandardCharsets.US_ASCII));String signature="t=1000,v1="+HexFormat.of().formatHex(mac.doFinal(body));assertTrue(Webhooks.verify(body,signature,TOKEN,1000,0));assertFalse(Webhooks.verify("{}".getBytes(),signature,TOKEN,1000,300));assertFalse(Webhooks.verify(body,signature,TOKEN,1301,300));assertFalse(Webhooks.verify(body,signature+",v1=bad",TOKEN,1000,300));
  Path dir=Files.createTempDirectory("availkit-jvm-private-");Files.setPosixFilePermissions(dir,PosixFilePermissions.fromString("rwx------"));try{Path path=dir.resolve("intent.json");PrivateFiles.writeNew(path,body);assertArrayEquals(body,PrivateFiles.read(path,1024));assertThrows(AvailKitException.class,()->PrivateFiles.writeNew(path,body));Path symlink=dir.resolve("link");Files.createSymbolicLink(symlink,path);assertThrows(AvailKitException.class,()->PrivateFiles.read(symlink,1024));Files.setPosixFilePermissions(path,PosixFilePermissions.fromString("rw-r--r--"));assertThrows(AvailKitException.class,()->PrivateFiles.read(path,1024));Files.delete(symlink);Files.delete(path);}finally{Files.delete(dir);}
 }
}
