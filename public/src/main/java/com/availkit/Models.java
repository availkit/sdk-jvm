package com.availkit;
import com.fasterxml.jackson.annotation.*;
import java.util.*;
/** Immutable wire models. UTC timestamps use RFC3339 strings to preserve the contracted precision. */
public final class Models {
 private Models() {}
 public static final class Availability {
  @JsonProperty("slots") @org.jetbrains.annotations.NotNull public final List<Slot> slots;
  @JsonCreator public Availability(@JsonProperty("slots") @org.jetbrains.annotations.NotNull List<Slot> slots) {
   this.slots = slots == null ? null : Collections.unmodifiableList(new ArrayList<>(slots));
  }
  @Override public String toString() { return "AvailKit.Availability [redacted]"; }
 }
 public static final class Booking {
  @JsonProperty("id") @org.jetbrains.annotations.NotNull public final String id;
  @JsonProperty("offeringId") @org.jetbrains.annotations.NotNull public final String offeringId;
  @JsonProperty("resourceId") @org.jetbrains.annotations.NotNull public final String resourceId;
  @JsonProperty("start") @org.jetbrains.annotations.NotNull public final String start;
  @JsonProperty("end") @org.jetbrains.annotations.NotNull public final String end;
  @JsonProperty("status") @org.jetbrains.annotations.NotNull public final String status;
  @JsonProperty("customerId") @org.jetbrains.annotations.Nullable public final String customerId;
  @JsonCreator public Booking(@JsonProperty("id") @org.jetbrains.annotations.NotNull String id, @JsonProperty("offeringId") @org.jetbrains.annotations.NotNull String offeringId, @JsonProperty("resourceId") @org.jetbrains.annotations.NotNull String resourceId, @JsonProperty("start") @org.jetbrains.annotations.NotNull String start, @JsonProperty("end") @org.jetbrains.annotations.NotNull String end, @JsonProperty("status") @org.jetbrains.annotations.NotNull String status, @JsonProperty("customerId") @org.jetbrains.annotations.Nullable String customerId) {
   this.id = id;
   this.offeringId = offeringId;
   this.resourceId = resourceId;
   this.start = start;
   this.end = end;
   this.status = status;
   this.customerId = customerId;
  }
  public Booking(@org.jetbrains.annotations.NotNull String id, @org.jetbrains.annotations.NotNull String offeringId, @org.jetbrains.annotations.NotNull String resourceId, @org.jetbrains.annotations.NotNull String start, @org.jetbrains.annotations.NotNull String end, @org.jetbrains.annotations.NotNull String status) { this(id, offeringId, resourceId, start, end, status, null); }
  @Override public String toString() { return "AvailKit.Booking [redacted]"; }
 }
 public static final class BookingInput {
  @JsonProperty("offeringId") @org.jetbrains.annotations.NotNull public final String offeringId;
  @JsonProperty("start") @org.jetbrains.annotations.NotNull public final String start;
  @JsonProperty("customerId") @org.jetbrains.annotations.Nullable public final String customerId;
  @JsonCreator public BookingInput(@JsonProperty("offeringId") @org.jetbrains.annotations.NotNull String offeringId, @JsonProperty("start") @org.jetbrains.annotations.NotNull String start, @JsonProperty("customerId") @org.jetbrains.annotations.Nullable String customerId) {
   this.offeringId = offeringId;
   this.start = start;
   this.customerId = customerId;
  }
  public BookingInput(@org.jetbrains.annotations.NotNull String offeringId, @org.jetbrains.annotations.NotNull String start) { this(offeringId, start, null); }
  @Override public String toString() { return "AvailKit.BookingInput [redacted]"; }
 }
 public static final class BookingPage {
  @JsonProperty("items") @org.jetbrains.annotations.NotNull public final List<Booking> items;
  @JsonProperty("nextAfter") @org.jetbrains.annotations.Nullable public final String nextAfter;
  @JsonCreator public BookingPage(@JsonProperty("items") @org.jetbrains.annotations.NotNull List<Booking> items, @JsonProperty("nextAfter") @org.jetbrains.annotations.Nullable String nextAfter) {
   this.items = items == null ? null : Collections.unmodifiableList(new ArrayList<>(items));
   this.nextAfter = nextAfter;
  }
  @Override public String toString() { return "AvailKit.BookingPage [redacted]"; }
 }
 public static final class BookingPageInput {
  @JsonProperty("title") @org.jetbrains.annotations.NotNull public final String title;
  @JsonProperty("enabled") public final boolean enabled;
  @JsonCreator public BookingPageInput(@JsonProperty("title") @org.jetbrains.annotations.NotNull String title, @JsonProperty("enabled") boolean enabled) {
   this.title = title;
   this.enabled = enabled;
  }
  @Override public String toString() { return "AvailKit.BookingPageInput [redacted]"; }
 }
 public static final class BookingPageSettings {
  @JsonProperty("pageId") @org.jetbrains.annotations.Nullable public final String pageId;
  @JsonProperty("title") @org.jetbrains.annotations.NotNull public final String title;
  @JsonProperty("enabled") public final boolean enabled;
  @JsonProperty("needsRepublish") public final boolean needsRepublish;
  @JsonCreator public BookingPageSettings(@JsonProperty("pageId") @org.jetbrains.annotations.Nullable String pageId, @JsonProperty("title") @org.jetbrains.annotations.NotNull String title, @JsonProperty("enabled") boolean enabled, @JsonProperty("needsRepublish") boolean needsRepublish) {
   this.pageId = pageId;
   this.title = title;
   this.enabled = enabled;
   this.needsRepublish = needsRepublish;
  }
  @Override public String toString() { return "AvailKit.BookingPageSettings [redacted]"; }
 }
 public static final class Customer {
  @JsonProperty("id") @org.jetbrains.annotations.NotNull public final String id;
  @JsonProperty("displayName") @org.jetbrains.annotations.NotNull public final String displayName;
  @JsonProperty("email") @org.jetbrains.annotations.Nullable public final String email;
  @JsonProperty("phone") @org.jetbrains.annotations.Nullable public final String phone;
  @JsonProperty("archived") public final boolean archived;
  @JsonProperty("erased") @org.jetbrains.annotations.Nullable public final Boolean erased;
  @JsonCreator public Customer(@JsonProperty("id") @org.jetbrains.annotations.NotNull String id, @JsonProperty("displayName") @org.jetbrains.annotations.NotNull String displayName, @JsonProperty("email") @org.jetbrains.annotations.Nullable String email, @JsonProperty("phone") @org.jetbrains.annotations.Nullable String phone, @JsonProperty("archived") boolean archived, @JsonProperty("erased") @org.jetbrains.annotations.Nullable Boolean erased) {
   this.id = id;
   this.displayName = displayName;
   this.email = email;
   this.phone = phone;
   this.archived = archived;
   this.erased = erased;
  }
  public Customer(@org.jetbrains.annotations.NotNull String id, @org.jetbrains.annotations.NotNull String displayName, @org.jetbrains.annotations.Nullable String email, @org.jetbrains.annotations.Nullable String phone, boolean archived) { this(id, displayName, email, phone, archived, null); }
  @Override public String toString() { return "AvailKit.Customer [redacted]"; }
 }
 public static final class CustomerErased {
  @JsonProperty("id") @org.jetbrains.annotations.NotNull public final String id;
  @JsonProperty("erased") public final boolean erased;
  @JsonCreator public CustomerErased(@JsonProperty("id") @org.jetbrains.annotations.NotNull String id, @JsonProperty("erased") boolean erased) {
   this.id = id;
   this.erased = erased;
  }
  @Override public String toString() { return "AvailKit.CustomerErased [redacted]"; }
 }
 public static final class CustomerInput {
  @JsonProperty("displayName") @org.jetbrains.annotations.NotNull public final String displayName;
  @JsonProperty("email") @org.jetbrains.annotations.Nullable public final String email;
  @JsonProperty("phone") @org.jetbrains.annotations.Nullable public final String phone;
  @JsonCreator public CustomerInput(@JsonProperty("displayName") @org.jetbrains.annotations.NotNull String displayName, @JsonProperty("email") @org.jetbrains.annotations.Nullable String email, @JsonProperty("phone") @org.jetbrains.annotations.Nullable String phone) {
   this.displayName = displayName;
   this.email = email;
   this.phone = phone;
  }
  public CustomerInput(@org.jetbrains.annotations.NotNull String displayName) { this(displayName, null, null); }
  @Override public String toString() { return "AvailKit.CustomerInput [redacted]"; }
 }
 public static final class CustomerPage {
  @JsonProperty("items") @org.jetbrains.annotations.NotNull public final List<Customer> items;
  @JsonProperty("nextAfter") @org.jetbrains.annotations.Nullable public final String nextAfter;
  @JsonCreator public CustomerPage(@JsonProperty("items") @org.jetbrains.annotations.NotNull List<Customer> items, @JsonProperty("nextAfter") @org.jetbrains.annotations.Nullable String nextAfter) {
   this.items = items == null ? null : Collections.unmodifiableList(new ArrayList<>(items));
   this.nextAfter = nextAfter;
  }
  @Override public String toString() { return "AvailKit.CustomerPage [redacted]"; }
 }
 public static final class Empty {
  @JsonCreator public Empty() {
  }
  @Override public String toString() { return "AvailKit.Empty [redacted]"; }
 }
 public static final class Events {
  @JsonProperty("events") @org.jetbrains.annotations.NotNull public final List<BookingEvent> events;
  @JsonProperty("nextAfter") public final long nextAfter;
  @JsonProperty("retentionFloor") public final long retentionFloor;
  @JsonProperty("highWater") public final long highWater;
  @JsonCreator public Events(@JsonProperty("events") @org.jetbrains.annotations.NotNull List<BookingEvent> events, @JsonProperty("nextAfter") long nextAfter, @JsonProperty("retentionFloor") long retentionFloor, @JsonProperty("highWater") long highWater) {
   this.events = events == null ? null : Collections.unmodifiableList(new ArrayList<>(events));
   this.nextAfter = nextAfter;
   this.retentionFloor = retentionFloor;
   this.highWater = highWater;
  }
  @Override public String toString() { return "AvailKit.Events [redacted]"; }
 }
 public static final class HistoryPruneInput {
  @JsonProperty("kind") @org.jetbrains.annotations.NotNull public final String kind;
  @JsonProperty("limit") public final long limit;
  @JsonCreator public HistoryPruneInput(@JsonProperty("kind") @org.jetbrains.annotations.NotNull String kind, @JsonProperty("limit") long limit) {
   this.kind = kind;
   this.limit = limit;
  }
  @Override public String toString() { return "AvailKit.HistoryPruneInput [redacted]"; }
 }
 public static final class HistoryPruneResult {
  @JsonProperty("asOf") @org.jetbrains.annotations.NotNull public final String asOf;
  @JsonProperty("cutoff") @org.jetbrains.annotations.NotNull public final String cutoff;
  @JsonProperty("deleted") public final long deleted;
  @JsonProperty("hasMore") public final boolean hasMore;
  @JsonCreator public HistoryPruneResult(@JsonProperty("asOf") @org.jetbrains.annotations.NotNull String asOf, @JsonProperty("cutoff") @org.jetbrains.annotations.NotNull String cutoff, @JsonProperty("deleted") long deleted, @JsonProperty("hasMore") boolean hasMore) {
   this.asOf = asOf;
   this.cutoff = cutoff;
   this.deleted = deleted;
   this.hasMore = hasMore;
  }
  @Override public String toString() { return "AvailKit.HistoryPruneResult [redacted]"; }
 }
 public static final class Hours {
  @JsonProperty("windows") @org.jetbrains.annotations.NotNull public final List<HoursWindow> windows;
  @JsonCreator public Hours(@JsonProperty("windows") @org.jetbrains.annotations.NotNull List<HoursWindow> windows) {
   this.windows = windows == null ? null : Collections.unmodifiableList(new ArrayList<>(windows));
  }
  @Override public String toString() { return "AvailKit.Hours [redacted]"; }
 }
 public static final class MaintenanceInput {
  @JsonProperty("limit") public final long limit;
  @JsonCreator public MaintenanceInput(@JsonProperty("limit") long limit) {
   this.limit = limit;
  }
  @Override public String toString() { return "AvailKit.MaintenanceInput [redacted]"; }
 }
 public static final class MaintenanceResult {
  @JsonProperty("asOf") @org.jetbrains.annotations.NotNull public final String asOf;
  @JsonProperty("deleted") public final long deleted;
  @JsonProperty("hasMore") public final boolean hasMore;
  @JsonCreator public MaintenanceResult(@JsonProperty("asOf") @org.jetbrains.annotations.NotNull String asOf, @JsonProperty("deleted") long deleted, @JsonProperty("hasMore") boolean hasMore) {
   this.asOf = asOf;
   this.deleted = deleted;
   this.hasMore = hasMore;
  }
  @Override public String toString() { return "AvailKit.MaintenanceResult [redacted]"; }
 }
 public static final class Offering {
  @JsonProperty("id") @org.jetbrains.annotations.NotNull public final String id;
  @JsonProperty("name") @org.jetbrains.annotations.NotNull public final String name;
  @JsonProperty("resourceId") @org.jetbrains.annotations.NotNull public final String resourceId;
  @JsonProperty("durationMinutes") public final long durationMinutes;
  @JsonProperty("archived") public final boolean archived;
  @JsonCreator public Offering(@JsonProperty("id") @org.jetbrains.annotations.NotNull String id, @JsonProperty("name") @org.jetbrains.annotations.NotNull String name, @JsonProperty("resourceId") @org.jetbrains.annotations.NotNull String resourceId, @JsonProperty("durationMinutes") long durationMinutes, @JsonProperty("archived") boolean archived) {
   this.id = id;
   this.name = name;
   this.resourceId = resourceId;
   this.durationMinutes = durationMinutes;
   this.archived = archived;
  }
  @Override public String toString() { return "AvailKit.Offering [redacted]"; }
 }
 public static final class OfferingInput {
  @JsonProperty("name") @org.jetbrains.annotations.NotNull public final String name;
  @JsonProperty("resourceId") @org.jetbrains.annotations.NotNull public final String resourceId;
  @JsonProperty("durationMinutes") public final long durationMinutes;
  @JsonCreator public OfferingInput(@JsonProperty("name") @org.jetbrains.annotations.NotNull String name, @JsonProperty("resourceId") @org.jetbrains.annotations.NotNull String resourceId, @JsonProperty("durationMinutes") long durationMinutes) {
   this.name = name;
   this.resourceId = resourceId;
   this.durationMinutes = durationMinutes;
  }
  @Override public String toString() { return "AvailKit.OfferingInput [redacted]"; }
 }
 public static final class OfferingPage {
  @JsonProperty("items") @org.jetbrains.annotations.NotNull public final List<Offering> items;
  @JsonProperty("nextAfter") @org.jetbrains.annotations.Nullable public final String nextAfter;
  @JsonCreator public OfferingPage(@JsonProperty("items") @org.jetbrains.annotations.NotNull List<Offering> items, @JsonProperty("nextAfter") @org.jetbrains.annotations.Nullable String nextAfter) {
   this.items = items == null ? null : Collections.unmodifiableList(new ArrayList<>(items));
   this.nextAfter = nextAfter;
  }
  @Override public String toString() { return "AvailKit.OfferingPage [redacted]"; }
 }
 public static final class PublicBooking {
  @JsonProperty("id") @org.jetbrains.annotations.NotNull public final String id;
  @JsonProperty("start") @org.jetbrains.annotations.NotNull public final String start;
  @JsonProperty("end") @org.jetbrains.annotations.NotNull public final String end;
  @JsonProperty("status") @org.jetbrains.annotations.NotNull public final String status;
  @JsonProperty("offering") @org.jetbrains.annotations.NotNull public final PublicOffering offering;
  @JsonProperty("page") @org.jetbrains.annotations.NotNull public final PublicPage page;
  @JsonProperty("pageEnabled") public final boolean pageEnabled;
  @JsonCreator public PublicBooking(@JsonProperty("id") @org.jetbrains.annotations.NotNull String id, @JsonProperty("start") @org.jetbrains.annotations.NotNull String start, @JsonProperty("end") @org.jetbrains.annotations.NotNull String end, @JsonProperty("status") @org.jetbrains.annotations.NotNull String status, @JsonProperty("offering") @org.jetbrains.annotations.NotNull PublicOffering offering, @JsonProperty("page") @org.jetbrains.annotations.NotNull PublicPage page, @JsonProperty("pageEnabled") boolean pageEnabled) {
   this.id = id;
   this.start = start;
   this.end = end;
   this.status = status;
   this.offering = offering;
   this.page = page;
   this.pageEnabled = pageEnabled;
  }
  @Override public String toString() { return "AvailKit.PublicBooking [redacted]"; }
 }
 public static final class PublicBookingInput {
  @JsonProperty("offeringId") @org.jetbrains.annotations.NotNull public final String offeringId;
  @JsonProperty("start") @org.jetbrains.annotations.NotNull public final String start;
  @JsonProperty("contact") @org.jetbrains.annotations.NotNull public final CustomerInput contact;
  @JsonCreator public PublicBookingInput(@JsonProperty("offeringId") @org.jetbrains.annotations.NotNull String offeringId, @JsonProperty("start") @org.jetbrains.annotations.NotNull String start, @JsonProperty("contact") @org.jetbrains.annotations.NotNull CustomerInput contact) {
   this.offeringId = offeringId;
   this.start = start;
   this.contact = contact;
  }
  @Override public String toString() { return "AvailKit.PublicBookingInput [redacted]"; }
 }
 public static final class PublicOffering {
  @JsonProperty("id") @org.jetbrains.annotations.NotNull public final String id;
  @JsonProperty("name") @org.jetbrains.annotations.NotNull public final String name;
  @JsonProperty("durationMinutes") public final long durationMinutes;
  @JsonProperty("timeZone") @org.jetbrains.annotations.NotNull public final String timeZone;
  @JsonCreator public PublicOffering(@JsonProperty("id") @org.jetbrains.annotations.NotNull String id, @JsonProperty("name") @org.jetbrains.annotations.NotNull String name, @JsonProperty("durationMinutes") long durationMinutes, @JsonProperty("timeZone") @org.jetbrains.annotations.NotNull String timeZone) {
   this.id = id;
   this.name = name;
   this.durationMinutes = durationMinutes;
   this.timeZone = timeZone;
  }
  @Override public String toString() { return "AvailKit.PublicOffering [redacted]"; }
 }
 public static final class PublicOfferingPage {
  @JsonProperty("items") @org.jetbrains.annotations.NotNull public final List<PublicOffering> items;
  @JsonProperty("nextAfter") @org.jetbrains.annotations.Nullable public final String nextAfter;
  @JsonCreator public PublicOfferingPage(@JsonProperty("items") @org.jetbrains.annotations.NotNull List<PublicOffering> items, @JsonProperty("nextAfter") @org.jetbrains.annotations.Nullable String nextAfter) {
   this.items = items == null ? null : Collections.unmodifiableList(new ArrayList<>(items));
   this.nextAfter = nextAfter;
  }
  @Override public String toString() { return "AvailKit.PublicOfferingPage [redacted]"; }
 }
 public static final class PublicPage {
  @JsonProperty("id") @org.jetbrains.annotations.NotNull public final String id;
  @JsonProperty("title") @org.jetbrains.annotations.NotNull public final String title;
  @JsonProperty("environment") @org.jetbrains.annotations.NotNull public final String environment;
  @JsonProperty("emailNotifications") @org.jetbrains.annotations.Nullable public final Boolean emailNotifications;
  @JsonCreator public PublicPage(@JsonProperty("id") @org.jetbrains.annotations.NotNull String id, @JsonProperty("title") @org.jetbrains.annotations.NotNull String title, @JsonProperty("environment") @org.jetbrains.annotations.NotNull String environment, @JsonProperty("emailNotifications") @org.jetbrains.annotations.Nullable Boolean emailNotifications) {
   this.id = id;
   this.title = title;
   this.environment = environment;
   this.emailNotifications = emailNotifications;
  }
  public PublicPage(@org.jetbrains.annotations.NotNull String id, @org.jetbrains.annotations.NotNull String title, @org.jetbrains.annotations.NotNull String environment) { this(id, title, environment, null); }
  @Override public String toString() { return "AvailKit.PublicPage [redacted]"; }
 }
 public static final class RescheduleInput {
  @JsonProperty("expectedStart") @org.jetbrains.annotations.NotNull public final String expectedStart;
  @JsonProperty("start") @org.jetbrains.annotations.NotNull public final String start;
  @JsonCreator public RescheduleInput(@JsonProperty("expectedStart") @org.jetbrains.annotations.NotNull String expectedStart, @JsonProperty("start") @org.jetbrains.annotations.NotNull String start) {
   this.expectedStart = expectedStart;
   this.start = start;
  }
  @Override public String toString() { return "AvailKit.RescheduleInput [redacted]"; }
 }
 public static final class Resource {
  @JsonProperty("id") @org.jetbrains.annotations.NotNull public final String id;
  @JsonProperty("name") @org.jetbrains.annotations.NotNull public final String name;
  @JsonProperty("timeZone") @org.jetbrains.annotations.NotNull public final String timeZone;
  @JsonProperty("capacity") public final long capacity;
  @JsonProperty("archived") public final boolean archived;
  @JsonCreator public Resource(@JsonProperty("id") @org.jetbrains.annotations.NotNull String id, @JsonProperty("name") @org.jetbrains.annotations.NotNull String name, @JsonProperty("timeZone") @org.jetbrains.annotations.NotNull String timeZone, @JsonProperty("capacity") long capacity, @JsonProperty("archived") boolean archived) {
   this.id = id;
   this.name = name;
   this.timeZone = timeZone;
   this.capacity = capacity;
   this.archived = archived;
  }
  @Override public String toString() { return "AvailKit.Resource [redacted]"; }
 }
 public static final class ResourceInput {
  @JsonProperty("name") @org.jetbrains.annotations.NotNull public final String name;
  @JsonProperty("timeZone") @org.jetbrains.annotations.NotNull public final String timeZone;
  @JsonProperty("capacity") public final long capacity;
  @JsonCreator public ResourceInput(@JsonProperty("name") @org.jetbrains.annotations.NotNull String name, @JsonProperty("timeZone") @org.jetbrains.annotations.NotNull String timeZone, @JsonProperty("capacity") long capacity) {
   this.name = name;
   this.timeZone = timeZone;
   this.capacity = capacity;
  }
  @Override public String toString() { return "AvailKit.ResourceInput [redacted]"; }
 }
 public static final class ResourcePage {
  @JsonProperty("items") @org.jetbrains.annotations.NotNull public final List<Resource> items;
  @JsonProperty("nextAfter") @org.jetbrains.annotations.Nullable public final String nextAfter;
  @JsonCreator public ResourcePage(@JsonProperty("items") @org.jetbrains.annotations.NotNull List<Resource> items, @JsonProperty("nextAfter") @org.jetbrains.annotations.Nullable String nextAfter) {
   this.items = items == null ? null : Collections.unmodifiableList(new ArrayList<>(items));
   this.nextAfter = nextAfter;
  }
  @Override public String toString() { return "AvailKit.ResourcePage [redacted]"; }
 }
 public static final class Usage {
  @JsonProperty("asOf") @org.jetbrains.annotations.NotNull public final String asOf;
  @JsonProperty("period") @org.jetbrains.annotations.NotNull public final UsagePeriod period;
  @JsonProperty("counters") @org.jetbrains.annotations.NotNull public final UsageCounters counters;
  @JsonCreator public Usage(@JsonProperty("asOf") @org.jetbrains.annotations.NotNull String asOf, @JsonProperty("period") @org.jetbrains.annotations.NotNull UsagePeriod period, @JsonProperty("counters") @org.jetbrains.annotations.NotNull UsageCounters counters) {
   this.asOf = asOf;
   this.period = period;
   this.counters = counters;
  }
  @Override public String toString() { return "AvailKit.Usage [redacted]"; }
 }
 public static final class UsageCounter {
  @JsonProperty("used") public final long used;
  @JsonProperty("limit") public final long limit;
  @JsonProperty("remaining") public final long remaining;
  @JsonCreator public UsageCounter(@JsonProperty("used") long used, @JsonProperty("limit") long limit, @JsonProperty("remaining") long remaining) {
   this.used = used;
   this.limit = limit;
   this.remaining = remaining;
  }
  @Override public String toString() { return "AvailKit.UsageCounter [redacted]"; }
 }
 public static final class UsageCounters {
  @JsonProperty("monthlyBookingCreates") @org.jetbrains.annotations.NotNull public final UsageCounter monthlyBookingCreates;
  @JsonProperty("catalogObjects") @org.jetbrains.annotations.NotNull public final UsageCounter catalogObjects;
  @JsonProperty("bookingRecords") @org.jetbrains.annotations.NotNull public final UsageCounter bookingRecords;
  @JsonProperty("committedEvents") @org.jetbrains.annotations.NotNull public final UsageCounter committedEvents;
  @JsonProperty("webhookSubscriptions") @org.jetbrains.annotations.NotNull public final UsageCounter webhookSubscriptions;
  @JsonProperty("webhookDeliveries") @org.jetbrains.annotations.NotNull public final UsageCounter webhookDeliveries;
  @JsonProperty("liveReplays") @org.jetbrains.annotations.NotNull public final UsageCounter liveReplays;
  @JsonProperty("customerRecords") @org.jetbrains.annotations.NotNull public final UsageCounter customerRecords;
  @JsonCreator public UsageCounters(@JsonProperty("monthlyBookingCreates") @org.jetbrains.annotations.NotNull UsageCounter monthlyBookingCreates, @JsonProperty("catalogObjects") @org.jetbrains.annotations.NotNull UsageCounter catalogObjects, @JsonProperty("bookingRecords") @org.jetbrains.annotations.NotNull UsageCounter bookingRecords, @JsonProperty("committedEvents") @org.jetbrains.annotations.NotNull UsageCounter committedEvents, @JsonProperty("webhookSubscriptions") @org.jetbrains.annotations.NotNull UsageCounter webhookSubscriptions, @JsonProperty("webhookDeliveries") @org.jetbrains.annotations.NotNull UsageCounter webhookDeliveries, @JsonProperty("liveReplays") @org.jetbrains.annotations.NotNull UsageCounter liveReplays, @JsonProperty("customerRecords") @org.jetbrains.annotations.NotNull UsageCounter customerRecords) {
   this.monthlyBookingCreates = monthlyBookingCreates;
   this.catalogObjects = catalogObjects;
   this.bookingRecords = bookingRecords;
   this.committedEvents = committedEvents;
   this.webhookSubscriptions = webhookSubscriptions;
   this.webhookDeliveries = webhookDeliveries;
   this.liveReplays = liveReplays;
   this.customerRecords = customerRecords;
  }
  @Override public String toString() { return "AvailKit.UsageCounters [redacted]"; }
 }
 public static final class UsagePeriod {
  @JsonProperty("start") @org.jetbrains.annotations.NotNull public final String start;
  @JsonProperty("end") @org.jetbrains.annotations.NotNull public final String end;
  @JsonCreator public UsagePeriod(@JsonProperty("start") @org.jetbrains.annotations.NotNull String start, @JsonProperty("end") @org.jetbrains.annotations.NotNull String end) {
   this.start = start;
   this.end = end;
  }
  @Override public String toString() { return "AvailKit.UsagePeriod [redacted]"; }
 }
 public static final class Webhook {
  @JsonProperty("id") @org.jetbrains.annotations.NotNull public final String id;
  @JsonProperty("url") @org.jetbrains.annotations.NotNull public final String url;
  @JsonProperty("enabled") public final boolean enabled;
  @JsonProperty("createdAt") @org.jetbrains.annotations.NotNull public final String createdAt;
  @JsonCreator public Webhook(@JsonProperty("id") @org.jetbrains.annotations.NotNull String id, @JsonProperty("url") @org.jetbrains.annotations.NotNull String url, @JsonProperty("enabled") boolean enabled, @JsonProperty("createdAt") @org.jetbrains.annotations.NotNull String createdAt) {
   this.id = id;
   this.url = url;
   this.enabled = enabled;
   this.createdAt = createdAt;
  }
  @Override public String toString() { return "AvailKit.Webhook [redacted]"; }
 }
 public static final class WebhookCreate {
  @JsonProperty("url") @org.jetbrains.annotations.NotNull public final String url;
  @JsonCreator public WebhookCreate(@JsonProperty("url") @org.jetbrains.annotations.NotNull String url) {
   this.url = url;
  }
  @Override public String toString() { return "AvailKit.WebhookCreate [redacted]"; }
 }
 public static final class WebhookCreated {
  @JsonProperty("id") @org.jetbrains.annotations.NotNull public final String id;
  @JsonProperty("url") @org.jetbrains.annotations.NotNull public final String url;
  @JsonProperty("enabled") public final boolean enabled;
  @JsonProperty("createdAt") @org.jetbrains.annotations.NotNull public final String createdAt;
  @JsonProperty("signingSecret") @org.jetbrains.annotations.NotNull public final String signingSecret;
  @JsonCreator public WebhookCreated(@JsonProperty("id") @org.jetbrains.annotations.NotNull String id, @JsonProperty("url") @org.jetbrains.annotations.NotNull String url, @JsonProperty("enabled") boolean enabled, @JsonProperty("createdAt") @org.jetbrains.annotations.NotNull String createdAt, @JsonProperty("signingSecret") @org.jetbrains.annotations.NotNull String signingSecret) {
   this.id = id;
   this.url = url;
   this.enabled = enabled;
   this.createdAt = createdAt;
   this.signingSecret = signingSecret;
  }
  @Override public String toString() { return "AvailKit.WebhookCreated [redacted]"; }
 }
 public static final class WebhookDelivery {
  @JsonProperty("id") @org.jetbrains.annotations.NotNull public final String id;
  @JsonProperty("subscriptionId") @org.jetbrains.annotations.NotNull public final String subscriptionId;
  @JsonProperty("eventId") @org.jetbrains.annotations.NotNull public final String eventId;
  @JsonProperty("state") @org.jetbrains.annotations.NotNull public final String state;
  @JsonProperty("createdAt") @org.jetbrains.annotations.NotNull public final String createdAt;
  @JsonProperty("attempts") public final long attempts;
  @JsonProperty("maxAttempts") public final long maxAttempts;
  @JsonProperty("nextAttemptAt") @org.jetbrains.annotations.Nullable public final String nextAttemptAt;
  @JsonProperty("leaseExpiresAt") @org.jetbrains.annotations.Nullable public final String leaseExpiresAt;
  @JsonProperty("lastAttemptAt") @org.jetbrains.annotations.Nullable public final String lastAttemptAt;
  @JsonProperty("lastOutcome") @org.jetbrains.annotations.Nullable public final String lastOutcome;
  @JsonProperty("lastHttpStatus") @org.jetbrains.annotations.Nullable public final Long lastHttpStatus;
  @JsonCreator public WebhookDelivery(@JsonProperty("id") @org.jetbrains.annotations.NotNull String id, @JsonProperty("subscriptionId") @org.jetbrains.annotations.NotNull String subscriptionId, @JsonProperty("eventId") @org.jetbrains.annotations.NotNull String eventId, @JsonProperty("state") @org.jetbrains.annotations.NotNull String state, @JsonProperty("createdAt") @org.jetbrains.annotations.NotNull String createdAt, @JsonProperty("attempts") long attempts, @JsonProperty("maxAttempts") long maxAttempts, @JsonProperty("nextAttemptAt") @org.jetbrains.annotations.Nullable String nextAttemptAt, @JsonProperty("leaseExpiresAt") @org.jetbrains.annotations.Nullable String leaseExpiresAt, @JsonProperty("lastAttemptAt") @org.jetbrains.annotations.Nullable String lastAttemptAt, @JsonProperty("lastOutcome") @org.jetbrains.annotations.Nullable String lastOutcome, @JsonProperty("lastHttpStatus") @org.jetbrains.annotations.Nullable Long lastHttpStatus) {
   this.id = id;
   this.subscriptionId = subscriptionId;
   this.eventId = eventId;
   this.state = state;
   this.createdAt = createdAt;
   this.attempts = attempts;
   this.maxAttempts = maxAttempts;
   this.nextAttemptAt = nextAttemptAt;
   this.leaseExpiresAt = leaseExpiresAt;
   this.lastAttemptAt = lastAttemptAt;
   this.lastOutcome = lastOutcome;
   this.lastHttpStatus = lastHttpStatus;
  }
  @Override public String toString() { return "AvailKit.WebhookDelivery [redacted]"; }
 }
 public static final class WebhookDeliveryPage {
  @JsonProperty("items") @org.jetbrains.annotations.NotNull public final List<WebhookDelivery> items;
  @JsonProperty("nextAfter") @org.jetbrains.annotations.Nullable public final String nextAfter;
  @JsonCreator public WebhookDeliveryPage(@JsonProperty("items") @org.jetbrains.annotations.NotNull List<WebhookDelivery> items, @JsonProperty("nextAfter") @org.jetbrains.annotations.Nullable String nextAfter) {
   this.items = items == null ? null : Collections.unmodifiableList(new ArrayList<>(items));
   this.nextAfter = nextAfter;
  }
  @Override public String toString() { return "AvailKit.WebhookDeliveryPage [redacted]"; }
 }
 public static final class WebhookDisable {
  @JsonCreator public WebhookDisable() {
  }
  @Override public String toString() { return "AvailKit.WebhookDisable [redacted]"; }
 }
 public static final class WebhookPage {
  @JsonProperty("items") @org.jetbrains.annotations.NotNull public final List<Webhook> items;
  @JsonProperty("nextAfter") @org.jetbrains.annotations.Nullable public final String nextAfter;
  @JsonCreator public WebhookPage(@JsonProperty("items") @org.jetbrains.annotations.NotNull List<Webhook> items, @JsonProperty("nextAfter") @org.jetbrains.annotations.Nullable String nextAfter) {
   this.items = items == null ? null : Collections.unmodifiableList(new ArrayList<>(items));
   this.nextAfter = nextAfter;
  }
  @Override public String toString() { return "AvailKit.WebhookPage [redacted]"; }
 }
 public static final class HoursWindow {
  @JsonProperty("weekday") public final long weekday;
  @JsonProperty("start") @org.jetbrains.annotations.NotNull public final String start;
  @JsonProperty("end") @org.jetbrains.annotations.NotNull public final String end;
  @JsonCreator public HoursWindow(@JsonProperty("weekday") long weekday, @JsonProperty("start") @org.jetbrains.annotations.NotNull String start, @JsonProperty("end") @org.jetbrains.annotations.NotNull String end) {
   this.weekday = weekday;
   this.start = start;
   this.end = end;
  }
  @Override public String toString() { return "AvailKit.HoursWindow [redacted]"; }
 }
 public static final class Slot {
  @JsonProperty("start") @org.jetbrains.annotations.NotNull public final String start;
  @JsonProperty("end") @org.jetbrains.annotations.NotNull public final String end;
  @JsonProperty("remainingCapacity") public final long remainingCapacity;
  @JsonCreator public Slot(@JsonProperty("start") @org.jetbrains.annotations.NotNull String start, @JsonProperty("end") @org.jetbrains.annotations.NotNull String end, @JsonProperty("remainingCapacity") long remainingCapacity) {
   this.start = start;
   this.end = end;
   this.remainingCapacity = remainingCapacity;
  }
  @Override public String toString() { return "AvailKit.Slot [redacted]"; }
 }
 public static final class BookingEvent {
  @JsonProperty("id") @org.jetbrains.annotations.NotNull public final String id;
  @JsonProperty("sequence") public final long sequence;
  @JsonProperty("type") @org.jetbrains.annotations.NotNull public final String type;
  @JsonProperty("bookingId") @org.jetbrains.annotations.NotNull public final String bookingId;
  @JsonProperty("occurredAt") @org.jetbrains.annotations.NotNull public final String occurredAt;
  @JsonCreator public BookingEvent(@JsonProperty("id") @org.jetbrains.annotations.NotNull String id, @JsonProperty("sequence") long sequence, @JsonProperty("type") @org.jetbrains.annotations.NotNull String type, @JsonProperty("bookingId") @org.jetbrains.annotations.NotNull String bookingId, @JsonProperty("occurredAt") @org.jetbrains.annotations.NotNull String occurredAt) {
   this.id = id;
   this.sequence = sequence;
   this.type = type;
   this.bookingId = bookingId;
   this.occurredAt = occurredAt;
  }
  @Override public String toString() { return "AvailKit.BookingEvent [redacted]"; }
 }
}
