# Stable SDK method matrix

All34 workspace and7 public operations are required and implemented. Request and response types are immutable; UTC timestamps preserve the wire RFC3339 string.

| Operation | Client method | Response | HTTP |
| --- | --- | --- | --- |
| createResource | WorkspaceClient.createResource | Resource | POST `/v1/workspaces/{workspaceId}/resources` |
| listResources | WorkspaceClient.listResources | ResourcePage | GET `/v1/workspaces/{workspaceId}/resources` |
| setHours | WorkspaceClient.replaceHours | Hours | PUT `/v1/workspaces/{workspaceId}/resources/{resourceId}/hours` |
| getHours | WorkspaceClient.getHours | Hours | GET `/v1/workspaces/{workspaceId}/resources/{resourceId}/hours` |
| createOffering | WorkspaceClient.createOffering | Offering | POST `/v1/workspaces/{workspaceId}/offerings` |
| listOfferings | WorkspaceClient.listOfferings | OfferingPage | GET `/v1/workspaces/{workspaceId}/offerings` |
| availability | WorkspaceClient.availability | Availability | GET `/v1/workspaces/{workspaceId}/availability` |
| book | WorkspaceClient.book | Booking | POST `/v1/workspaces/{workspaceId}/bookings` |
| listBookings | WorkspaceClient.listBookings | BookingPage | GET `/v1/workspaces/{workspaceId}/bookings` |
| cancel | WorkspaceClient.cancel | Booking | POST `/v1/workspaces/{workspaceId}/bookings/{bookingId}/cancel` |
| events | WorkspaceClient.events | Events | GET `/v1/workspaces/{workspaceId}/events` |
| getResource | WorkspaceClient.getResource | Resource | GET `/v1/workspaces/{workspaceId}/resources/{resourceId}` |
| updateResource | WorkspaceClient.replaceResource | Resource | PUT `/v1/workspaces/{workspaceId}/resources/{resourceId}` |
| getOffering | WorkspaceClient.getOffering | Offering | GET `/v1/workspaces/{workspaceId}/offerings/{offeringId}` |
| updateOffering | WorkspaceClient.replaceOffering | Offering | PUT `/v1/workspaces/{workspaceId}/offerings/{offeringId}` |
| getBooking | WorkspaceClient.getBooking | Booking | GET `/v1/workspaces/{workspaceId}/bookings/{bookingId}` |
| createWebhook | WorkspaceClient.createWebhook | WebhookCreated | POST `/v1/workspaces/{workspaceId}/webhooks` |
| listWebhooks | WorkspaceClient.listWebhooks | WebhookPage | GET `/v1/workspaces/{workspaceId}/webhooks` |
| disableWebhook | WorkspaceClient.disableWebhook | Webhook | POST `/v1/workspaces/{workspaceId}/webhooks/{subscriptionId}/disable` |
| listWebhookDeliveries | WorkspaceClient.listWebhookDeliveries | WebhookDeliveryPage | GET `/v1/workspaces/{workspaceId}/webhooks/{subscriptionId}/deliveries` |
| getUsage | WorkspaceClient.getUsage | Usage | GET `/v1/workspaces/{workspaceId}/usage` |
| rescheduleBooking | WorkspaceClient.rescheduleBooking | Booking | POST `/v1/workspaces/{workspaceId}/bookings/{bookingId}/reschedule` |
| archiveResource | WorkspaceClient.archiveResource | Resource | POST `/v1/workspaces/{workspaceId}/resources/{resourceId}/archive` |
| archiveOffering | WorkspaceClient.archiveOffering | Offering | POST `/v1/workspaces/{workspaceId}/offerings/{offeringId}/archive` |
| pruneHistory | WorkspaceClient.pruneHistory | HistoryPruneResult | POST `/v1/workspaces/{workspaceId}/maintenance/history` |
| createCustomer | WorkspaceClient.createCustomer | Customer | POST `/v1/workspaces/{workspaceId}/customers` |
| listCustomers | WorkspaceClient.listCustomers | CustomerPage | GET `/v1/workspaces/{workspaceId}/customers` |
| getCustomer | WorkspaceClient.getCustomer | Customer | GET `/v1/workspaces/{workspaceId}/customers/{customerId}` |
| replaceCustomer | WorkspaceClient.replaceCustomer | Customer | PUT `/v1/workspaces/{workspaceId}/customers/{customerId}` |
| archiveCustomer | WorkspaceClient.archiveCustomer | Customer | POST `/v1/workspaces/{workspaceId}/customers/{customerId}/archive` |
| eraseCustomer | WorkspaceClient.eraseCustomer | CustomerErased | POST `/v1/workspaces/{workspaceId}/customers/{customerId}/erase` |
| pruneExpiredReplays | WorkspaceClient.pruneExpiredReplays | MaintenanceResult | POST `/v1/workspaces/{workspaceId}/maintenance/expired-replays` |
| getPublicPage | PublicBookingClient.getPage | PublicPage | GET `/v1/public/pages/{pageId}` |
| listPublicOfferings | PublicBookingClient.listOfferings | PublicOfferingPage | GET `/v1/public/pages/{pageId}/offerings` |
| publicAvailability | PublicBookingClient.availability | Availability | GET `/v1/public/pages/{pageId}/availability` |
| bookPublic | PublicBookingClient.book | PublicBooking | POST `/v1/public/pages/{pageId}/bookings` |
| getPublicBooking | PublicBookingClient.getBooking | PublicBooking | GET `/v1/public/pages/{pageId}/bookings/{bookingId}` |
| cancelPublicBooking | PublicBookingClient.cancel | PublicBooking | POST `/v1/public/pages/{pageId}/bookings/{bookingId}/cancel` |
| reschedulePublicBooking | PublicBookingClient.reschedule | PublicBooking | POST `/v1/public/pages/{pageId}/bookings/{bookingId}/reschedule` |
| getBookingPage | WorkspaceClient.getBookingPage | BookingPageSettings | GET `/v1/workspaces/{workspaceId}/booking-page` |
| replaceBookingPage | WorkspaceClient.replaceBookingPage | BookingPageSettings | PUT `/v1/workspaces/{workspaceId}/booking-page` |
