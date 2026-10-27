# Kotlin / Android public-booking path

Use **only** `com.availkit:availkit-public:0.1.1` from an Android app. It exposes
published-page reads and per-booking capabilities and contains no workspace
client, account/passkey ceremony, native login or recovery API. Never put a
workspace key in an APK. The complete `../kotlin-public` command-line example
executes the same typed public API and is verified as a real Kotlin/JVM consumer.
This directory is an integration recipe, not a claim of emulator/device/UI
acceptance.

Minimum Android API26 follows the Java time/NIO and Jackson runtime floor.
Use a maintained Android Gradle plugin/Kotlin2.4.x project, normal Android
`INTERNET` permission, current platform certificate updates and its normal
`google()` / `mavenCentral()` repositories. Add the hosted Maven repository and
select OkHttp's Android variant rather than its JVM JAR:

```kotlin
repositories {
    google()
    mavenCentral()
    maven { url = uri("https://availkit.dev/downloads/maven") }
}
dependencies {
    implementation("com.availkit:availkit-public:0.1.1") {
        exclude(group = "com.squareup.okhttp3", module = "okhttp-jvm")
        exclude(group = "net.java.dev.jna", module = "jna")
    }
    implementation("com.squareup.okhttp3:okhttp:5.5.1")
}
android { defaultConfig { minSdk = 26 } }
```

The Android OkHttp variant uses its documented AndroidX startup initializer.
If an application deliberately disables that initializer, follow upstream
OkHttp initialization instructions before making any request. Do not replace
TLS validation, install a permissive trust manager, add logging interceptors,
or enable retry middleware. For release minification retain the immutable
Jackson model constructors/properties and their annotations; see `consumer-rules.pro`.

Calls return `CompletableFuture<T>` immediately and HTTP runs off the main
thread. Deliver completed UI state with your lifecycle's main-thread dispatcher.
Cancelling the **original** future cancels its HTTP call; a transformed future
does not automatically propagate cancellation upstream. Cancel on explicit
operation cancellation/lifecycle teardown, and treat an admitted mutation as
unknown. A coroutine adapter must register cancellation on that original future;
never wrap it in a retrying flow or use a lifecycle restart to repeat a mutation.
Read requests are also explicit; there is no background polling.

Before an explicit Book tap, generate booking/contact tokens once, prepare the
intent and durably retain its `exportPrivate()` bytes in the app's internal
private storage or an appropriate platform vault. Only after successful private
persistence call `client.execute(intent)`. Keep the create latch across activity
recreation/process restart: if an exported create exists, present its retained
state and an explicit reconciliation/replay decision instead of preparing a new
create. Scope the latch to the page; do not move it to a different page/environment.
Store no capabilities/contact values in URLs, logs, analytics, external/shared
storage, clipboard or saved-instance-state bundles that are routinely exported.
Apply Android backup policy appropriate to private booking capabilities.

Linux x64 `PrivateFiles` in the CLI is not an Android storage abstraction. The
Android dependency declaration excludes its JNA dependency; never invoke that
helper from the app. Use app-private
storage, exclusive creation, flush/fsync and a durable parent-directory latch
or a transactionally persisted vault record. Never silently overwrite a retained
intent. Do not print `exportPrivate()`. An unknown create capability cannot
discover a booking ID; owner-provided authoritative IDs or exact original-intent
replay are the documented recovery paths, subject to server retention/authority.
Reads do not manufacture an acknowledgment or enable contradictory writes.

Example read integration (with explicit event ownership and lifecycle cleanup):

```kotlin
val client = PublicBookingClient(publishedPageId)
val call = client.getPage() // no credentials or account cookies
call.whenComplete { page, failure ->
    runOnUiThread {
        if (failure != null) showSafeFailure() else renderPage(page)
    }
}
// On explicit cancellation/lifecycle teardown:
call.cancel(true)
// On owning component shutdown, after preserving private pending state:
client.close()
```

Do not create the client as a Compose recomposition side effect. Keep a single
owned instance and issue Book/Move/Cancel only from explicit guarded events.
Known Read/Book permission/environment rules remain server authority; native
networking does not alter the browser account/session contract.

Upstream references: [OkHttp requirements and Android initialization](https://github.com/square/okhttp/blob/master/README.md),
[separate JVM/Android variants](https://raw.githubusercontent.com/square/okhttp/master/CHANGELOG.md),
[Jackson Android floor](https://github.com/FasterXML/jackson/wiki/Jackson-Release-2.21).
