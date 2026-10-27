
package com.availkit;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;
/** Authenticate exact raw bytes first; transactionally deduplicate AvailKit-Delivery-ID only after verification. */
public final class Webhooks {
 private Webhooks(){}
 public static boolean verify(byte[] body,String signature,String secret,long nowEpochSeconds,int toleranceSeconds){
  if(toleranceSeconds<0||toleranceSeconds>3600||nowEpochSeconds<0)throw AvailKitException.input("invalid_webhook_tolerance");
  try {if(body==null||body.length>16384||signature==null||!signature.matches("t=(0|[1-9][0-9]{0,19}),v1=[0-9a-f]{64}"))return false;Protocol.token(secret);String stamp=signature.substring(2,signature.indexOf(","));java.math.BigInteger age=java.math.BigInteger.valueOf(nowEpochSeconds).subtract(new java.math.BigInteger(stamp)).abs();if(age.compareTo(java.math.BigInteger.valueOf(toleranceSeconds))>0)return false;
   Mac mac=Mac.getInstance("HmacSHA256");mac.init(new SecretKeySpec(Base64.getUrlDecoder().decode(secret),"HmacSHA256"));mac.update((stamp+".").getBytes(StandardCharsets.US_ASCII));byte[] expected=mac.doFinal(body),claimed=new byte[32];String hex=signature.substring(signature.indexOf("v1=")+3);for(int i=0;i<32;i++)claimed[i]=(byte)Integer.parseInt(hex.substring(i*2,i*2+2),16);return MessageDigest.isEqual(expected,claimed);
  }catch(Exception e){return false;}
 }
 public static boolean verify(byte[] body,String signature,String secret){return verify(body,signature,secret,java.time.Instant.now().getEpochSecond(),300);}
}
