
package com.availkit;
import com.fasterxml.jackson.databind.JsonNode;
import java.nio.charset.StandardCharsets;
import java.time.*;
import java.util.*;
/** Exact private booking intent. Export/import never sends. Store exported bytes in private durable storage before sending. */
public final class RequestIntent {
 final String operation,path,key,scope;final byte[] body;final Map<String,String> headers;final Instant createdAt;
 RequestIntent(String operation,String path,byte[] body,String key,String scope,Map<String,String> headers,Instant createdAt){this.operation=operation;this.path=path;this.body=body.clone();this.key=key;this.scope=scope;this.headers=Collections.unmodifiableMap(new LinkedHashMap<>(headers));this.createdAt=createdAt;}
 public byte[] exportPrivate(){try{Map<String,Object> m=new LinkedHashMap<>();m.put("version",1);m.put("operation",operation);m.put("path",path);m.put("body",new String(body,StandardCharsets.UTF_8));m.put("idempotencyKey",key);m.put("scope",scope);m.put("headers",headers);m.put("createdAt",createdAt.toString());return Protocol.JSON.writeValueAsBytes(m);}catch(Exception e){throw AvailKitException.input("invalid_intent");}}
 public static RequestIntent importPrivate(byte[] bytes){try{if(bytes==null||bytes.length>131072)throw new Exception();JsonNode m=Protocol.parse(bytes);Set<String> keys=new HashSet<>();m.fieldNames().forEachRemaining(keys::add);if(!keys.equals(new HashSet<>(Arrays.asList("version","operation","path","body","idempotencyKey","scope","headers","createdAt")))||!m.path("version").isIntegralNumber()||m.path("version").asInt()!=1||!m.path("headers").isObject())throw new Exception();for(String k:Arrays.asList("operation","path","body","idempotencyKey","scope","createdAt"))if(!m.path(k).isTextual())throw new Exception();byte[] body=m.path("body").asText().getBytes(StandardCharsets.UTF_8);if(body.length>16384)throw new Exception();Map<String,String> h=new LinkedHashMap<>();Iterator<Map.Entry<String,JsonNode>> i=m.path("headers").fields();while(i.hasNext()){Map.Entry<String,JsonNode> e=i.next();if(!e.getValue().isTextual())throw new Exception();h.put(e.getKey(),e.getValue().asText());}return new RequestIntent(m.path("operation").asText(),m.path("path").asText(),body,Protocol.key(m.path("idempotencyKey").asText()),m.path("scope").asText(),h,Protocol.instant(m.path("createdAt").asText()));}catch(Exception e){throw AvailKitException.input("invalid_intent");}}
 @Override public String toString(){return "AvailKit.RequestIntent [private]";}
}
