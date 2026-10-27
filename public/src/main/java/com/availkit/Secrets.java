
package com.availkit;
import java.security.SecureRandom;
import java.util.Base64;
/** Generate once per distinct action; these return private values and must never be logged. */
public final class Secrets {
 private static final SecureRandom RANDOM=new SecureRandom();private Secrets(){}
 public static String newIdempotencyKey(){return random(24);}
 public static String newBookingToken(){return random(32);}
 public static String newContactToken(){return random(32);}
 private static String random(int n){byte[] b=new byte[n];RANDOM.nextBytes(b);return Base64.getUrlEncoder().withoutPadding().encodeToString(b);}
}
