

SDK **0.1.1**. Standalone build and synthetic test instructions are in
[DEVELOPMENT.md](DEVELOPMENT.md). [Versioned release notes](https://github.com/availkit/sdk-jvm/releases)
record dated verification and available distribution channels. Source availability
does not imply registry publication or mobile/service acceptance.
Install examples name this SDK version; check the release notes for artifact availability.

# AvailKit Java and Kotlin SDK 0.1.1

Two conventional Maven artifacts are hosted at `https://availkit.dev/downloads/maven`:

- `com.availkit:availkit:0.1.1`: trusted backend, all34 stable workspace operations and the public client dependency.
- `com.availkit:availkit-public:0.1.1`: seven public booking operations, immutable wire models and protocol utilities. It has no workspace client, workspace credential constructor or account/passkey API.

These are hosted artifacts, not a Maven Central availability claim. No private repository checkout is required. Add this repository and the appropriate dependency to your Maven POM:

```xml
<repositories><repository><id>availkit-hosted</id><url>https://availkit.dev/downloads/maven</url><releases><checksumPolicy>fail</checksumPolicy></releases></repository></repositories>
<dependencies><dependency><groupId>com.availkit</groupId><artifactId>availkit</artifactId><version>0.1.1</version></dependency></dependencies>
```

The downloadable `availkit-jvm-examples-0.1.1.zip` contains complete standalone Java, Kotlin backend and Kotlin public consumers. Enter `examples/java`, `examples/kotlin` or `examples/kotlin-public` and run `mvn -B compile exec:java -Dexec.args=/absolute/private/config.json`. Maven installs only declared dependencies; it does not clone the application. The examples require a pre-existing owned **test** resource/offering with hours and a test Read+Book key; they don't create or erase business data. They require at least two available times before sending any mutation, discover times, book, read, move, then cancel exactly their own booking. Every acknowledged step gates the next action. A failed/unknown response halts the example and retains its intent; no cleanup mutation is guessed.

Before running, create a fresh owned0700 directory for config/credentials and another fresh owned0700 intent directory. Put only the workspace credential plus one optional newline in a0600 file. Never pass a secret in a command argument. Create a0600 config file:

```json
{"workspaceId":"YOUR_32_HEX_ID","environment":"test","credentialFile":"/absolute/private/key","intentDirectory":"/absolute/private/new-intents","offeringId":"YOUR_32_HEX_ID","from":"2030-01-01T00:00:00Z","to":"2030-01-02T00:00:00Z"}
```

Use actual future UTC dates, at most seven days apart. Optional `baseUrl` defaults to `https://api.availkit.com`. The Kotlin public example config instead contains `pageId`, `offeringId`, `from`, `to`, `displayName`, `email` and `intentDirectory`; it never reads a workspace key. On a test page use the permitted synthetic contact policy. On a live page email can cause a real notification; this example explicitly requires a test page.

<!-- docs:install:start -->
```sh
curl -fsS https://availkit.dev/downloads/availkit-jvm-examples-0.1.1.zip -o availkit-jvm-examples-0.1.1.zip
curl -fsS https://availkit.dev/downloads/SHA256SUMS -o SHA256SUMS
sha256sum --check --ignore-missing SHA256SUMS
unzip availkit-jvm-examples-0.1.1.zip -d availkit-jvm-consumer
cd availkit-jvm-consumer/examples/java
mvn -B compile
# For the actual Kotlin consumer use ../kotlin, or ../kotlin-public for public-page access.
```
<!-- docs:install:end -->

Runtime policy: backend support is Java21 LTS with its current security updates; Kotlin/JVM2.4.x is the supported consumer line, with2.4.21 pinned for verification. Library bytecode targets Java8 so the credential-free public artifact can run on Android API26+; the backend support policy is still Java21. Android integration uses OkHttp's Android variant, not `java.net.http`. See [the public Android path](examples/android/README.md). Android device/emulator execution is a separate validation scope; JVM Kotlin verification does not prove an Android UI was exercised. OS trust stores/platform TLS must remain current. Earlier Java/Kotlin lines and future major lines are unverified until tested. Maven3.9.16 and JDK21 build this source. File-helper acceptance is Linux x64; it fails closed on other platforms.

All methods return `CompletableFuture<T>`. Each request has one finite **total** deadline (default10s;1ms–120s configurable), up to1MiB response by default (hard limit8MiB), at most32 admitted requests/client and four transport workers. Cancel the returned future to cancel its HTTP call. Cancellation or deadline after a mutation starts leaves its outcome unknown. Close clients at shutdown. HTTPS validates certificates and hostname using platform defaults. HTTP is accepted only for explicit localhost/127.0.0.1/::1 origins used in tests. Redirects, cookies, ambient proxies, authenticators, caches, connection-failure retries and status followups for one-shot mutation bodies are disabled. There is no SDK retry loop. Do not add a retrying wrapper or logging HTTP interceptor.

Workspace construction requires explicit workspaceID, test/live intent and a matching issued key. The server derives environment/permissions from that key; a local label does not grant or switch authority. Use Read for reads; Edit catalog for resource/offering/hours/page writes; Book for booking mutations; linking a customer also needs Manage. Contacts and webhook administration require Manage; privacy erasure/history pruning additionally requires Privacy; expired replay pruning requires Privacy. Availability reads are advisory; book can still conflict. No credential discovery, login, passkey, session, recovery, native account API or cross-origin account-cookie behavior is provided.

`prepareBook`, `prepareCancel`, `prepareRescheduleBooking` and their public equivalents snapshot an immutable original intent. Persist `exportPrivate()` **before** `execute(intent)`. `PrivateFiles.writeNew` uses exclusive0600 creation in an owned0700 directory and syncs the file/directory. `read` checks bounded regular owned0600 files on the exact opened descriptor with no-follow/nonblocking and a pinned private directory. These helpers support Linux x64 with libc statx; owner/type/mode are checked on the opened descriptor with JNA5.19.1, not inferred from a pre-open path check. They use openat/no-follow/nonblocking, so a substituted FIFO cannot hang before validation. On Android and other desktop/server platforms use an application vault. The HTTP SDK remains portable. `importPrivate` and inspecting an intent never send. To make an explicit recovery decision, import the original private bytes and use the same exact client scope and `execute` once. The local24h guard is conservative; server retention, privacy pruning and current authority may still deny a replay. Never change body/key/tokens or scope to bypass denial. Only booking create/cancel/move have this replay contract. Other mutations must not be replayed automatically or given a newly invented guarantee. An authoritative booking read can establish current state; it does not recreate a lost acknowledgment, identify an unknown create by capability alone or settle an uncertain operation by itself.

Public clients use a published pageID and private32-byte capabilities generated with `Secrets.newBookingToken()` and `newContactToken()` **before** create. A create intent exports both capabilities; retain it privately. The contact token is a reuse proof, not a workspace/account credential. Capabilities authorize one booking; possession cannot discover its ID. A page can be paused without granting workspace access. Keep mobile capabilities and contact values in application private storage; never log them, place them in URLs or analytics, or embed a workspace key in an APK.

`AvailKitException` exposes finite `kind`, `code`, nullable `status`, `uncertain`, and `diagnostics()`. It retains no cause, raw response, URL, headers, body or user-supplied error message. A recognized structured4xx rejection is known; an unrecognized error code on a mutation stays unknown; malformed/oversized/truncated success or transport failure after mutation admission is unknown. A cancelled future uses standard `CancellationException`; treat the mutation as unknown and retain the intent. Client/config/intent/model `toString` excludes values, including one-time webhook secrets. The schema validator rejects duplicate JSON members, wrong status/media, unexpected fields, invalid types/IDs/timestamps/order/cursors, impossible success assertions and cross-page responses. Request IDs are32 lowercase hex; keys are16–128 printable ASCII without surrounding whitespace. UTC timestamps are RFC3339; move inputs have millisecond precision or coarser. IANA timezone existence and webhook DNS/address policy remain server authority.

For webhooks, call `Webhooks.verify(rawBody, signature, secret)` before JSON decoding. Verification checks exact `t=...,v1=...`, HMAC-SHA256, canonical32-byte signing secret, constant-time MAC equality, absolute age (300s default, up to3600s), and a16KiB raw-body bound. Deduplicate the stable `AvailKit-Delivery-ID` transactionally after verification; retries keep identity but refresh signatures. Never log signing secrets or contact-bearing payloads.

Coverage and wire snapshot: `coverage.json`, `contract.json`, [method matrix](METHODS.md). Models use explicit JetBrains nullability annotations for Java/Kotlin consumers, preserve wire camelCase and immutable collections; nullable optional fields are omitted on input. Lists are one bounded page per call: pass `nextAfter` unchanged into the same collection/client/environment and impose your own finite page budget. `events` uses its numeric `nextAfter` and retention floor; event retention expiry is an error, not a silent reset. `getUsage` counters are advisory, not reservations. `createWebhook` returns its signing secret only once: privately store it immediately; it has no replay guarantee.

Build: `mvn -B -T1 clean package` serially runs Java/Kotlin fixture tests and produces both JARs/source JARs. `python3 build.py --output /absolute/artifacts` builds and assembles deterministic standalone POM/JAR/checksum paths plus the example ZIP. Runtime dependencies are pinned OkHttp5.5.1 (one-shot bodies, deadlines and Android path), Jackson2.21.8 LTS (strict bounded JSON), JetBrains annotations26.1.0 (static Java/Kotlin nullability) and JNA5.19.1 (exact Linux descriptor checks for explicit private files); normal Maven Central transitive dependencies are resolved conventionally, with no vendored binary or application source. Test-only JUnit5.13.4 and Kotlin2.4.21 are not SDK runtime requirements.

Dependency/runtime references checked25October2026: [OkHttp changelog](https://raw.githubusercontent.com/square/okhttp/master/CHANGELOG.md), [Jackson2.21 LTS](https://github.com/FasterXML/jackson/wiki/Jackson-Release-2.21), [Kotlin releases](https://kotlinlang.org/docs/releases.html), [Maven3.9.16](https://maven.apache.org/download.cgi). Hosted distribution does not establish registry namespace ownership or activate publication.

After creating the protected configuration and fresh intent directory, explicitly run the example:

<!-- docs:run:start -->
```sh
mvn -B exec:java -Dexec.args=/absolute/private/config.json
```
<!-- docs:run:end -->

The optional private-file path uses [JNA5.19.1](https://github.com/java-native-access/jna) to call libc; no native binary is built or published by AvailKit. The core/public HTTP paths never initialize JNA. Android consumers exclude this dependency and use platform private storage as documented.

The example ZIP includes `coverage.json` and `runtime-policy.json`; both JARs store the same family metadata under `META-INF/availkit/`. Coverage distinguishes the public-only artifact from the trusted backend artifact. Runtime support and coverage do not establish registry publication or Android device acceptance.


See [CHANGELOG.md](CHANGELOG.md), [SUPPORT.md](SUPPORT.md) and
[SECURITY.md](SECURITY.md) for release policy and private reporting prerequisites.
`contract-projection.json` pins only this SDK's supported wire operations;
additive unrelated API routes do not imply this SDK implements them.

The separate optional business export endpoint and current versioned browser
recovery protocol are not methods of this SDK. Use their documented browser/
TypeScript integration where appropriate; this package preserves its declared
workspace/public booking surface rather than inventing account/native APIs.
