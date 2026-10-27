package com.availkit;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import static com.availkit.Models.*;
/** Public page and per-booking capability client. No workspace or account credentials. */
public final class PublicBookingClient extends ClientCore {
 public PublicBookingClient(String pageId) { this(pageId, new ClientOptions()); }
 public PublicBookingClient(String pageId, ClientOptions options) { super(pageId, options); }
 public @org.jetbrains.annotations.NotNull CompletableFuture<PublicPage> getPage() { return call("getPublicPage", new String[]{}, null, Collections.emptyMap(), null, Collections.emptyMap(), PublicPage.class); }
 public @org.jetbrains.annotations.NotNull CompletableFuture<PublicOfferingPage> listOfferings(@org.jetbrains.annotations.Nullable String after, long limit) { return call("listPublicOfferings", new String[]{}, null, query("after", after, "limit", limit), null, Collections.emptyMap(), PublicOfferingPage.class); }
 public @org.jetbrains.annotations.NotNull CompletableFuture<Availability> availability(String offeringId, String from, String to) { return call("publicAvailability", new String[]{}, null, query("offeringId", offeringId, "from", from, "to", to), null, Collections.emptyMap(), Availability.class); }
 public @org.jetbrains.annotations.NotNull RequestIntent prepareBook(PublicBookingInput body, String idempotencyKey, String bookingToken, String contactToken) { return prepare("bookPublic", new String[]{}, body, idempotencyKey, access(bookingToken, contactToken)); }
 public @org.jetbrains.annotations.NotNull CompletableFuture<PublicBooking> book(PublicBookingInput body, String idempotencyKey, String bookingToken, String contactToken) { return execute(prepareBook(body, idempotencyKey, bookingToken, contactToken), PublicBooking.class); }
 public @org.jetbrains.annotations.NotNull CompletableFuture<PublicBooking> getBooking(String bookingId, String bookingToken) { return call("getPublicBooking", new String[]{bookingId}, null, Collections.emptyMap(), null, access(bookingToken, null), PublicBooking.class); }
 public @org.jetbrains.annotations.NotNull RequestIntent prepareCancel(String bookingId, String idempotencyKey, String bookingToken) { return prepare("cancelPublicBooking", new String[]{bookingId}, new Empty(), idempotencyKey, access(bookingToken, null)); }
 public @org.jetbrains.annotations.NotNull CompletableFuture<PublicBooking> cancel(String bookingId, String idempotencyKey, String bookingToken) { return execute(prepareCancel(bookingId, idempotencyKey, bookingToken), PublicBooking.class); }
 public @org.jetbrains.annotations.NotNull RequestIntent prepareReschedule(String bookingId, RescheduleInput body, String idempotencyKey, String bookingToken) { return prepare("reschedulePublicBooking", new String[]{bookingId}, body, idempotencyKey, access(bookingToken, null)); }
 public @org.jetbrains.annotations.NotNull CompletableFuture<PublicBooking> reschedule(String bookingId, RescheduleInput body, String idempotencyKey, String bookingToken) { return execute(prepareReschedule(bookingId, body, idempotencyKey, bookingToken), PublicBooking.class); }
 public @org.jetbrains.annotations.NotNull CompletableFuture<PublicBooking> execute(RequestIntent intent) { return execute(intent, PublicBooking.class); }
}
