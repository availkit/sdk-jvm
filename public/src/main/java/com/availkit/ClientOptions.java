
package com.availkit;
import java.net.*;
/** Immutable transport policy. HTTPS except explicit literal loopback/localhost test origins. */
public final class ClientOptions {
 public final String baseUrl;
 public final long timeoutMillis;
 public final int maxResponseBytes;
 public ClientOptions() { this("https://api.availkit.com",10000,1048576); }
 public ClientOptions(String baseUrl,long timeoutMillis,int maxResponseBytes) {
  String origin=null;
  try {
   URI u=new URI(baseUrl);
   if (!baseUrl.matches("[\\x21-\\x7e]+") || u.getHost()==null || u.getRawUserInfo()!=null || u.getRawQuery()!=null || u.getRawFragment()!=null || !("".equals(u.getRawPath()) || "/".equals(u.getRawPath())) || u.getPort() < -1 || u.getPort()>65535 || u.getPort()==0) throw new Exception();
   boolean secure="https".equals(u.getScheme());
   if (!secure && !("http".equals(u.getScheme()) && ArraysSupport.loopback(u.getHost()))) throw new Exception();
   origin=u.getScheme()+"://"+u.getRawAuthority();
  } catch (Exception ignored) {}
  if(origin==null) throw AvailKitException.input("invalid_base_url");
  if(timeoutMillis<1 || timeoutMillis>120000 || maxResponseBytes<1 || maxResponseBytes>8388608) throw AvailKitException.input("invalid_limits");
  this.baseUrl=origin;this.timeoutMillis=timeoutMillis;this.maxResponseBytes=maxResponseBytes;
 }
 @Override public String toString(){return "AvailKit.ClientOptions [redacted]";}
 private static final class ArraysSupport { static boolean loopback(String h) {return h.equals("localhost")||h.equals("127.0.0.1")||h.equals("[::1]")||h.equals("::1");} }
}
