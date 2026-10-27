
package com.availkit;
import com.fasterxml.jackson.databind.*;
import okhttp3.*;
import okio.BufferedSink;
import java.io.*;
import java.net.Proxy;
import java.nio.charset.StandardCharsets;
import java.time.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;

/** Shared protocol engine; use PublicBookingClient or the separate server-only WorkspaceClient. */
public abstract class ClientCore implements AutoCloseable {
 private final String root,scope,scopeId;
 private final boolean publicClient;
 private final Map<String,String> headers;
 private final ClientOptions options;
 private final OkHttpClient http;
 private final Semaphore active=new Semaphore(32);
 private final ScheduledExecutorService timers=Executors.newSingleThreadScheduledExecutor(r->{Thread t=new Thread(r,"availkit-deadline");t.setDaemon(true);return t;});
 private volatile boolean closed;
 private static final Set<String> SAFE=new HashSet<>(Arrays.asList(("booking_page_paused booking_access_in_use contact_changed validation_failed synthetic_email_required unauthorized forbidden scope_denied not_found invalid_input invalid_query invalid_cursor invalid_limit quota_exceeded unavailable credential_invalid capability_denied customer_archived idempotency_conflict capacity_conflict capacity_exceeded version_conflict catalog_in_use catalog_archived event_cursor_expired invalid_id invalid_range identity_unavailable recovery_unavailable csrf_invalid recent_auth_required secret_already_revealed idempotency_mismatch membership_changed membership_denied last_credential identity_limit identity_rate_limited membership_conflict request_key_invalid").split(" ")));
 protected static ClientOptions checkedOptions(ClientOptions options){if(options==null)throw AvailKitException.input("invalid_options");return options;}
 protected ClientCore(String pageId,ClientOptions options){this(Protocol.id(pageId),checkedOptions(options),true,Collections.emptyMap(),checkedOptions(options).baseUrl+":public:"+pageId);}
 protected ClientCore(String id,ClientOptions options,boolean isPublic,Map<String,String> headers,String scope) {
  this.scopeId=Protocol.id(id);this.options=checkedOptions(options);this.publicClient=isPublic;this.headers=Collections.unmodifiableMap(new LinkedHashMap<>(headers));this.scope=scope;this.root=(isPublic?"/v1/public/pages/":"/v1/workspaces/")+id;
  ExecutorService pool=new ThreadPoolExecutor(4,4,30,TimeUnit.SECONDS,new LinkedBlockingQueue<Runnable>(),r->{Thread t=new Thread(r,"availkit-http");t.setDaemon(true);return t;});
  Dispatcher dispatcher=new Dispatcher(pool);dispatcher.setMaxRequests(4);dispatcher.setMaxRequestsPerHost(4);
  http=new OkHttpClient.Builder().dispatcher(dispatcher).proxy(Proxy.NO_PROXY).cookieJar(CookieJar.NO_COOKIES).cache(null).authenticator(Authenticator.NONE).proxyAuthenticator(Authenticator.NONE)
   .retryOnConnectionFailure(false).followRedirects(false).followSslRedirects(false).callTimeout(options.timeoutMillis,TimeUnit.MILLISECONDS)
   .connectTimeout(options.timeoutMillis,TimeUnit.MILLISECONDS).readTimeout(options.timeoutMillis,TimeUnit.MILLISECONDS).writeTimeout(options.timeoutMillis,TimeUnit.MILLISECONDS).build();
 }
 @Override public final String toString(){return "AvailKit client [credentials redacted]";}
 @Override public void close(){closed=true;http.dispatcher().cancelAll();http.dispatcher().executorService().shutdown();http.connectionPool().evictAll();timers.shutdownNow();}
 protected static Map<String,String> query(Object... pairs){Map<String,String> q=new LinkedHashMap<>();for(int i=0;i<pairs.length;i+=2)if(pairs[i+1]!=null)q.put((String)pairs[i],String.valueOf(pairs[i+1]));return q;}
 protected static Map<String,String> access(String bookingToken,String contactToken){Map<String,String> h=new LinkedHashMap<>();h.put("Authorization","Booking "+Protocol.token(bookingToken));if(contactToken!=null)h.put("X-AvailKit-Contact-Token",Protocol.token(contactToken));return h;}
 private String path(String op,String[] ids){JsonNode spec=Protocol.route(op);if(spec.path("public").asBoolean()!=publicClient)throw AvailKitException.input("invalid_operation");String p=spec.path("path").asText().replace(publicClient?"/v1/public/pages/{pageId}":"/v1/workspaces/{workspaceId}","");for(String id:ids)p=p.replaceFirst("\\{[^}]+\\}",Protocol.id(id));if(p.contains("{"))throw AvailKitException.input("invalid_id");return p;}
 protected RequestIntent prepare(String op,String[] ids,Object body,String key,Map<String,String> extra){JsonNode spec=Protocol.route(op);if(!spec.path("idempotent").asBoolean())throw AvailKitException.input("unsafe_replay");return new RequestIntent(op,path(op,ids),Protocol.body(body,spec.get("request")),Protocol.key(key),scope,extra,Instant.now());}
 protected <T> CompletableFuture<T> execute(RequestIntent intent,Class<T> type){
  if(intent==null||!scope.equals(intent.scope))throw AvailKitException.input("intent_scope_mismatch");JsonNode spec=Protocol.route(intent.operation);if(!spec.path("idempotent").asBoolean()||spec.path("public").asBoolean()!=publicClient)throw AvailKitException.input("unsafe_replay");
  Duration age=Duration.between(intent.createdAt,Instant.now());if(age.compareTo(Duration.ofHours(24))>=0)throw AvailKitException.input("intent_expired");if(age.isNegative())throw AvailKitException.input("invalid_intent_timestamp");
  List<String> ids=new ArrayList<>();java.util.regex.Matcher m=java.util.regex.Pattern.compile("[0-9a-f]{32}").matcher(intent.path);while(m.find())ids.add(m.group());if(!path(intent.operation,ids.toArray(new String[0])).equals(intent.path))throw AvailKitException.input("invalid_intent");
  JsonNode b=Protocol.parse(intent.body);if(!Protocol.valid(b,spec.get("request"),0))throw AvailKitException.input("invalid_intent");Protocol.body(b,spec.get("request"));Protocol.key(intent.key);
  Map<String,String> expected=publicClient?access(intent.headers.getOrDefault("Authorization","").replaceFirst("^Booking ",""),intent.operation.equals("bookPublic")?intent.headers.get("X-AvailKit-Contact-Token"):null):Collections.emptyMap();
  if(!intent.headers.equals(expected)||publicClient&&intent.operation.equals("bookPublic")&&!intent.headers.containsKey("X-AvailKit-Contact-Token"))throw AvailKitException.input("invalid_intent");
  return send(intent.operation,intent.path,Collections.emptyMap(),intent.body,intent.key,intent.headers,type);
 }
 protected <T> CompletableFuture<T> call(String op,String[] ids,Object body,Map<String,String> q,String key,Map<String,String> extra,Class<T> type){JsonNode spec=Protocol.route(op);Protocol.query(spec,q);return send(op,path(op,ids),new LinkedHashMap<>(q),Protocol.body(body,spec.get("request")),key,extra,type);}
 private <T> CompletableFuture<T> send(String op,String path,Map<String,String> q,byte[] body,String key,Map<String,String> extra,Class<T> type){
  if(closed)throw AvailKitException.input("client_closed");if(!active.tryAcquire())throw new AvailKitException("transport","client_busy",null,false);
  JsonNode spec=Protocol.route(op);boolean mutation=!spec.path("method").asText().equals("GET");HttpUrl.Builder url=HttpUrl.get(options.baseUrl+root+path).newBuilder();for(Map.Entry<String,String> e:q.entrySet())url.addQueryParameter(e.getKey(),e.getValue());
  Request.Builder rb=new Request.Builder().url(url.build()).header("Accept","application/json").header("Accept-Encoding","identity").header("Cache-Control","no-store").header("User-Agent","availkit-jvm/0.1.1");for(Map.Entry<String,String> e:headers.entrySet())rb.header(e.getKey(),e.getValue());for(Map.Entry<String,String> e:extra.entrySet())rb.header(e.getKey(),e.getValue());if(key!=null)rb.header("Idempotency-Key",Protocol.key(key));
  RequestBody payload=body==null?null:new RequestBody(){private final AtomicBoolean used=new AtomicBoolean();public MediaType contentType(){return MediaType.get("application/json");}public long contentLength(){return body.length;}public boolean isOneShot(){return true;}public void writeTo(BufferedSink sink)throws IOException{if(!used.compareAndSet(false,true))throw new IOException("AvailKit request already sent");sink.write(body);}};
  Call call=http.newCall(rb.method(spec.path("method").asText(),payload).build());
  CompletableFuture<T> result=new CompletableFuture<T>(){@Override public boolean cancel(boolean interrupt){boolean changed=super.cancel(interrupt);if(changed)call.cancel();return changed;}};
  long deadline=System.nanoTime()+TimeUnit.MILLISECONDS.toNanos(options.timeoutMillis);
  ScheduledFuture<?> timer=timers.schedule(()->{result.completeExceptionally(new AvailKitException("transport","timeout",null,mutation));call.cancel();},options.timeoutMillis,TimeUnit.MILLISECONDS);
  result.whenComplete((v,e)->timer.cancel(false));
  call.enqueue(new Callback(){public void onFailure(Call c,IOException e){active.release();result.completeExceptionally(new AvailKitException("transport",System.nanoTime()>=deadline?"timeout":"request_failed",null,mutation));}
   public void onResponse(Call c,Response response){try(Response r=response){int status=r.code();String media=r.header("Content-Type","").split(";",2)[0].trim().toLowerCase(Locale.ROOT);
    if(status>=300&&status<400||!(media.equals("application/json")||media.equals("application/problem+json"))||status>=200&&status<300&&!media.equals("application/json")||!r.header("Content-Encoding","identity").equalsIgnoreCase("identity"))throw new AvailKitException("protocol","invalid_response",status,mutation);
    if(r.body()==null||r.body().contentLength()>options.maxResponseBytes)throw new AvailKitException("protocol","response_too_large",status,mutation);
    ByteArrayOutputStream buffer=new ByteArrayOutputStream();try(InputStream stream=r.body().byteStream()){byte[] chunk=new byte[8192];int n;while((n=stream.read(chunk,0,Math.min(chunk.length,options.maxResponseBytes+1-buffer.size())))!=-1){buffer.write(chunk,0,n);if(buffer.size()>options.maxResponseBytes)throw new AvailKitException("protocol","response_too_large",status,mutation);if(System.nanoTime()>=deadline)throw new AvailKitException("transport","timeout",null,mutation);}}
    if(r.body().contentLength()!=-1&&buffer.size()!=r.body().contentLength())throw new AvailKitException("protocol","incomplete_response",status,mutation);
    JsonNode data=Protocol.parse(buffer.toByteArray());
    if(status<200||status>=300){String code=null;if(data.path("error").isObject()&&data.path("error").path("code").isTextual()&&data.path("error").path("message").isTextual())code=data.path("error").path("code").asText();else if(data.path("type").asText().equals("urn:availkit:problem")&&data.path("status").asInt()==status&&data.path("code").isTextual())code=data.path("code").asText();if(code==null)throw new AvailKitException("protocol","invalid_error",status,mutation);throw new AvailKitException("api",SAFE.contains(code)?code:"api_error",status,mutation&&(status>=500||!SAFE.contains(code)));}
    if(status!=spec.path("status").asInt()||!Protocol.valid(data,spec.path("response"),0)||((publicClient||path.startsWith("/customers")||path.startsWith("/usage")||path.startsWith("/maintenance"))&&!r.header("Cache-Control","").equals("no-store")))throw new AvailKitException("protocol","invalid_response",status,mutation);
    Protocol.relations(op,path,q,body,data,scopeId);T value=Protocol.JSON.treeToValue(data,type);if(System.nanoTime()>=deadline)throw new AvailKitException("transport","timeout",null,mutation);result.complete(value);
   }catch(AvailKitException e){result.completeExceptionally(new AvailKitException(e.kind,e.code,e.status,e.uncertain||mutation&&e.kind.equals("protocol")));}catch(Exception e){result.completeExceptionally(new AvailKitException("protocol","invalid_response",null,mutation));}finally{active.release();}}
  });return result;
 }
}
