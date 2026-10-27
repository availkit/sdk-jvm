
package com.availkit;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.*;
import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.regex.*;
import java.nio.charset.StandardCharsets;
import java.security.*;

/** Maintained schema subset and cross-field checks; no network or credential loading. */
final class Protocol {
 static final ObjectMapper JSON=new ObjectMapper(JsonFactory.builder().enable(StreamReadFeature.STRICT_DUPLICATE_DETECTION)
  .streamReadConstraints(StreamReadConstraints.builder().maxNestingDepth(24).maxStringLength(131072).maxNumberLength(32).build()).build())
  .setSerializationInclusion(JsonInclude.Include.NON_NULL).enable(DeserializationFeature.FAIL_ON_TRAILING_TOKENS).disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
 static final JsonNode CONTRACT;
 static { try(java.io.InputStream s=Protocol.class.getResourceAsStream("/com/availkit/contract.json")) { CONTRACT=JSON.readTree(s); } catch(Exception e){throw new ExceptionInInitializerError("AvailKit contract unavailable");} }
 static JsonNode route(String op){JsonNode s=CONTRACT.path("routes").get(op);if(s==null)throw AvailKitException.input("invalid_operation");return s;}
 static String id(String s){if(s==null||!s.matches("[0-9a-f]{32}"))throw AvailKitException.input("invalid_id");return s;}
 static String token(String s){if(s==null||!s.matches("[A-Za-z0-9_-]{42}[AEIMQUYcgkosw048]"))throw AvailKitException.input("invalid_credential");return s;}
 static String key(String s){if(s==null||!s.matches("[\\x20-\\x7e]{16,128}")||!s.trim().equals(s))throw AvailKitException.input("invalid_idempotency_key");return s;}
 static Instant instant(String s){try{if(s==null||!s.matches("[0-9]{4}-[0-9]{2}-[0-9]{2}T[0-9]{2}:[0-9]{2}:[0-9]{2}(?:\\.[0-9]{1,9})?Z"))throw new Exception();return Instant.parse(s);}catch(Exception e){throw AvailKitException.input("invalid_timestamp");}}
 static boolean text(String s,int max){return s!=null&&!s.codePoints().allMatch(c->Character.isWhitespace(c)||Character.isSpaceChar(c))&&s.codePointCount(0,s.length())<=max&&s.codePoints().noneMatch(Character::isISOControl);}
 static String hash(String value){try{byte[] raw=MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));StringBuilder s=new StringBuilder();for(byte b:raw)s.append(String.format(Locale.ROOT,"%02x",b&255));return s.toString();}catch(Exception e){throw AvailKitException.input("invalid_credential");}}
 static JsonNode parse(byte[] data){try{return JSON.readTree(data);}catch(Exception e){throw new AvailKitException("protocol","invalid_json",null,false);}}
 static boolean valid(JsonNode v,JsonNode s,int depth){
  if(v==null||s==null||s.isNull()||depth>20)return false;
  if(s.has("$ref"))return valid(v,CONTRACT.path("schemas").path(s.path("$ref").asText().replaceAll(".*/","")),depth+1);
  if(s.has("allOf"))for(JsonNode x:s.path("allOf"))if(!valid(v,x,depth+1))return false;
  if(s.has("anyOf")){boolean ok=false;for(JsonNode x:s.path("anyOf"))ok|=valid(v,x,depth+1);if(!ok)return false;}
  if(s.has("oneOf")){int n=0;for(JsonNode x:s.path("oneOf"))if(valid(v,x,depth+1))n++;if(n!=1)return false;}
  if(s.has("const")&&!s.get("const").equals(v))return false;
  if(s.has("enum")){boolean ok=false;for(JsonNode x:s.path("enum"))ok|=x.equals(v);if(!ok)return false;}
  if(s.has("type")){JsonNode types=s.get("type");boolean ok=false;if(types.isTextual())ok=kind(v,types.asText());else for(JsonNode t:types)ok|=kind(v,t.asText());if(!ok)return false;}
  if(v.isObject()){
   JsonNode props=s.path("properties");for(JsonNode r:s.path("required"))if(!v.has(r.asText()))return false;
   Iterator<Map.Entry<String,JsonNode>> fields=v.fields();while(fields.hasNext()) {Map.Entry<String,JsonNode> f=fields.next();if(!props.has(f.getKey())){if(s.path("additionalProperties").isBoolean()&&!s.path("additionalProperties").asBoolean())return false;}else if(!valid(f.getValue(),props.path(f.getKey()),depth+1))return false;
    if(f.getKey().equals("signingSecret")){try{token(f.getValue().asText());}catch(Exception e){return false;}}
    if((f.getKey().equals("id")||f.getKey().endsWith("Id")||f.getKey().equals("nextAfter")&&f.getValue().isTextual())&&!f.getValue().isNull()&&f.getValue().isTextual()&&!f.getValue().asText().matches("[0-9a-f]{32}"))return false;
   }
  } else if(v.isArray()) {if(v.size()<s.path("minItems").asInt(0)||v.size()>s.path("maxItems").asInt(10000))return false;for(JsonNode x:v)if(!valid(x,s.path("items"),depth+1))return false;
  } else if(v.isTextual()) {String z=v.asText();int n=z.codePointCount(0,z.length());if(n<s.path("minLength").asInt(0)||n>s.path("maxLength").asInt(8192))return false;if(s.has("pattern")&&!Pattern.compile(s.path("pattern").asText()).matcher(z).find())return false;if("date-time".equals(s.path("format").asText()))try{instant(z);}catch(Exception e){return false;}
  } else if(v.isNumber()) {if(!v.isIntegralNumber()||!v.canConvertToLong())return false;long n=v.asLong();if(n<s.path("minimum").asLong(-9007199254740991L)||n>s.path("maximum").asLong(9007199254740991L))return false;}
  return true;
 }
 static boolean kind(JsonNode v,String t){switch(t){case"object":return v.isObject();case"array":return v.isArray();case"string":return v.isTextual();case"null":return v.isNull();case"boolean":return v.isBoolean();case"integer":return v.isIntegralNumber();case"number":return v.isNumber();default:return false;}}
 static byte[] body(Object body,JsonNode schema){
  if(schema==null||schema.isNull()){if(body!=null)throw AvailKitException.input("invalid_input");return null;}
  try{JsonNode v=JSON.valueToTree(body);if(!valid(v,schema,0))throw new Exception();semantic(v,schema.path("$ref").asText().replaceAll(".*/",""));byte[] b=JSON.writeValueAsBytes(v);if(b.length>16384)throw new Exception();return b;}catch(Exception e){throw AvailKitException.input("invalid_input");}
 }
 static void semantic(JsonNode v,String name){
  if((name.equals("ResourceInput")||name.equals("OfferingInput"))&&!text(v.path("name").asText(),160))throw AvailKitException.input("invalid_input");
  if(name.equals("ResourceInput")&&!text(v.path("timeZone").asText(),100))throw AvailKitException.input("invalid_time_zone");
  if(name.equals("CustomerInput")) {
   if(!text(v.path("displayName").asText(),160))throw AvailKitException.input("invalid_customer");
   if(v.hasNonNull("email")&&(!text(v.path("email").asText(),254)||!v.path("email").asText().matches("[^@\\s]+@[^@\\s]+")))throw AvailKitException.input("invalid_customer");
   if(v.hasNonNull("phone")&&!text(v.path("phone").asText(),32))throw AvailKitException.input("invalid_customer");
  }
  if(name.equals("PublicBookingInput"))semantic(v.path("contact"),"CustomerInput");
  if(name.equals("BookingPageInput")&&!text(v.path("title").asText(),160))throw AvailKitException.input("invalid_input");
  if(name.equals("RescheduleInput")&&(instant(v.path("start").asText()).getNano()%1000000!=0||instant(v.path("expectedStart").asText()).getNano()%1000000!=0))throw AvailKitException.input("invalid_timestamp");
  if(name.equals("WebhookCreate")){try{java.net.URI u=new java.net.URI(v.path("url").asText());if(!"https".equals(u.getScheme())||u.getHost()==null||!u.getHost().contains(".")||u.getHost().matches("[0-9.]+")||u.getHost().contains(":")||u.getRawUserInfo()!=null||u.getRawQuery()!=null||u.getRawFragment()!=null||u.getPort()!=-1&&u.getPort()!=443)throw new Exception();}catch(Exception e){throw AvailKitException.input("invalid_webhook_url");}}
  if(name.equals("Hours")){List<JsonNode> seen=new ArrayList<>();for(JsonNode w:v.path("windows")){String a=w.path("start").asText(),b=w.path("end").asText();if(!a.matches("(?:[01][0-9]|2[0-3]):[0-5][0-9]")||!b.matches("(?:(?:[01][0-9]|2[0-3]):[0-5][0-9]|24:00)")||a.compareTo(b)>=0)throw AvailKitException.input("invalid_hours");for(JsonNode p:seen)if(p.path("weekday").equals(w.path("weekday"))&&p.path("start").asText().compareTo(b)<0&&a.compareTo(p.path("end").asText())<0)throw AvailKitException.input("invalid_hours");seen.add(w);}}
 }
 static void query(JsonNode route,Map<String,String> q){
  Set<String> allowed=new HashSet<>();for(JsonNode p:route.path("parameters")){String n=p.path("name").asText();allowed.add(n);String v=q.get(n);if(v==null){if(p.path("required").asBoolean())throw AvailKitException.input("invalid_query");continue;}JsonNode s=p.path("schema");JsonNode value;try{value="integer".equals(s.path("type").asText())?JSON.getNodeFactory().numberNode(Long.parseLong(v)):JSON.getNodeFactory().textNode(v);}catch(Exception e){throw AvailKitException.input("invalid_query");}if(!valid(value,s,0))throw AvailKitException.input("invalid_query");if(n.equals("after")&&!"events".equals(route.path("path").asText().replaceAll(".*/","")))id(v);if(n.equals("offeringId"))id(v);if(n.equals("q")&&!text(v,160))throw AvailKitException.input("invalid_query");}
  if(!allowed.containsAll(q.keySet()))throw AvailKitException.input("invalid_query");
  if(q.containsKey("from")){Duration range=Duration.between(instant(q.get("from")),instant(q.get("to")));if(range.isZero()||range.isNegative()||range.compareTo(Duration.ofDays(7))>0)throw AvailKitException.input("invalid_range");}
 }
 static void relations(String op,String path,Map<String,String> q,byte[] bytes,JsonNode d,String scopeId){
  JsonNode b=bytes==null?null:parse(bytes);boolean ok=true;
  if(op.equals("setHours")||op.equals("getHours")){try{semantic(d,"Hours");}catch(Exception e){throw new AvailKitException("protocol","invalid_response",null,false);}}
  if(d.has("items")){JsonNode items=d.path("items");int limit=Integer.parseInt(q.getOrDefault("limit","25"));String after=q.getOrDefault("after","");ok=items.size()<=limit;for(JsonNode item:items){String next=item.path("id").asText();ok&=next.compareTo(after)>0;after=next;}JsonNode next=d.path("nextAfter");ok&=next.isNull()||items.size()==limit&&next.asText().equals(after);if(op.equals("listWebhookDeliveries"))for(JsonNode item:items)ok&=item.path("subscriptionId").asText().equals(path.split("/")[2]);}
  String[] p=path.split("/");Set<String> identity=new HashSet<>(Arrays.asList("getResource","updateResource","archiveResource","getOffering","updateOffering","archiveOffering","getBooking","cancel","rescheduleBooking","getCustomer","replaceCustomer","archiveCustomer","eraseCustomer","disableWebhook","getPublicBooking","cancelPublicBooking","reschedulePublicBooking"));
  if(identity.contains(op))ok&=d.path("id").asText().equals(p[2]);
  if(d.has("status")&&d.has("start"))ok&=instant(d.path("end").asText()).isAfter(instant(d.path("start").asText()));
  if(op.equals("cancel")||op.equals("cancelPublicBooking"))ok&=d.path("status").asText().equals("cancelled");
  if(Arrays.asList("book","bookPublic","rescheduleBooking","reschedulePublicBooking").contains(op))ok&=d.path("status").asText().equals("confirmed")&&instant(d.path("start").asText()).equals(instant(b.path("start").asText()));
  if(op.equals("book"))ok&=d.path("offeringId").equals(b.path("offeringId"))&&Objects.equals(d.get("customerId"),b.get("customerId"));
  if(op.equals("bookPublic"))ok&=d.path("offering").path("id").equals(b.path("offeringId"));
  if(op.startsWith("archive"))ok&=d.path("archived").asBoolean();
  if(op.equals("disableWebhook"))ok&=!d.path("enabled").asBoolean();
  if(op.equals("getPublicPage"))ok&=d.path("id").asText().equals(scopeId);
  if(d.has("page"))ok&=d.path("page").path("id").asText().equals(scopeId);
  if(op.equals("availability")||op.equals("publicAvailability")){Instant lo=instant(q.get("from")),hi=instant(q.get("to")),last=null;for(JsonNode slot:d.path("slots")){Instant a=instant(slot.path("start").asText()),z=instant(slot.path("end").asText());ok&=!a.isBefore(lo)&&!z.isAfter(hi)&&a.isBefore(z)&&(last==null||a.isAfter(last));last=a;}}
  if(op.equals("getUsage")){Instant as=instant(d.path("asOf").asText());java.time.ZonedDateTime start=as.atZone(ZoneOffset.UTC).withDayOfMonth(1).truncatedTo(ChronoUnit.DAYS);ok&=instant(d.path("period").path("start").asText()).equals(start.toInstant())&&instant(d.path("period").path("end").asText()).equals(start.plusMonths(1).toInstant());for(JsonNode v:d.path("counters"))ok&=v.path("remaining").asLong()==Math.max(0,v.path("limit").asLong()-v.path("used").asLong());}
  if(op.equals("pruneHistory")||op.equals("pruneExpiredReplays")){long n=d.path("deleted").asLong(),limit=b.path("limit").asLong();ok&=n<=limit&&(!d.path("hasMore").asBoolean()||n==limit);if(op.equals("pruneHistory"))ok&=Duration.between(instant(d.path("cutoff").asText()),instant(d.path("asOf").asText())).equals(Duration.ofDays(30));}
  if(op.equals("events")){long after=Long.parseLong(q.getOrDefault("after","0")),floor=d.path("retentionFloor").asLong(),high=d.path("highWater").asLong(),prev=Math.max(after,floor);ok&=floor<=high&&d.path("events").size()<=Integer.parseInt(q.getOrDefault("limit","100"));for(JsonNode e:d.path("events")){long n=e.path("sequence").asLong();ok&=n>prev&&n<=high;prev=n;}ok&=d.path("nextAfter").asLong()==prev&&prev<=Math.max(after,high);}
  if(!ok)throw new AvailKitException("protocol","invalid_response",null,false);
 }
}
