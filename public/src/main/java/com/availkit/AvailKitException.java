
package com.availkit;
import java.util.*;
/** Finite diagnostics only. No raw response, URL, headers, request, token or nested cause. */
public final class AvailKitException extends RuntimeException {
 public final String kind, code;
 public final Integer status;
 public final boolean uncertain;
 AvailKitException(String kind, String code, Integer status, boolean uncertain) {
  super("AvailKit " + kind + ": " + code, null, false, true);
  this.kind=kind; this.code=code; this.status=status; this.uncertain=uncertain;
 }
 static AvailKitException input(String code) { return new AvailKitException("input",code,null,false); }
 public Map<String,Object> diagnostics() {
  Map<String,Object> d=new LinkedHashMap<>();d.put("kind",kind);d.put("code",code);d.put("status",status);d.put("uncertain",uncertain);return Collections.unmodifiableMap(d);
 }
 @Override public String toString() { return getMessage(); }
}
