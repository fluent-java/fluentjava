# FluentJava

Fluent methods on Java's core types — `String`, `List`, `Map`, `LocalDate`, `Object` — and a chainable validator, all resolved at compile time. No agent, no reflection at runtime, standard bytecode.

```java
// This is valid Java with FluentJava
Integer qty    = rawInput.trimToNull().toIntOrNull();
String  label  = code.trimToNull().orDefault("N/A");
boolean active = user.isNotNull() && user.takeIf(User::isEnabled).isNotNull();
```

**Java 17+**

---

## Setup

```xml
<dependencies>
    <dependency>
        <groupId>io.fluentjava</groupId>
        <artifactId>fluentjava-runtime</artifactId>
        <version>1.0.0</version>
    </dependency>
    <dependency>
        <groupId>io.fluentjava</groupId>
        <artifactId>fluentjava-plugin</artifactId>
        <version>1.0.0</version>
        <scope>provided</scope>
    </dependency>
</dependencies>

<build>
    <plugins>
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-compiler-plugin</artifactId>
            <version>3.12.1</version>
            <configuration>
                <compilerArgs>
                    <arg>-Xplugin:FluentJava</arg>
                    <arg>-J--add-exports=jdk.compiler/com.sun.tools.javac.api=ALL-UNNAMED</arg>
                    <arg>-J--add-exports=jdk.compiler/com.sun.tools.javac.tree=ALL-UNNAMED</arg>
                    <arg>-J--add-exports=jdk.compiler/com.sun.tools.javac.util=ALL-UNNAMED</arg>
                    <arg>-J--add-exports=jdk.compiler/com.sun.tools.javac.code=ALL-UNNAMED</arg>
                </compilerArgs>
                <fork>true</fork>
            </configuration>
        </plugin>
    </plugins>
</build>
```

> **IntelliJ** — Install the `fluentjava-intellij-plugin` to get autocompletion and avoid red underlines on fluent methods in the IDE.

---

## Usage

### 1. Safe string parsing

```java
// try/catch, null check, trim, isEmpty — 10 lines every time
Integer age = null;
if (rawAge != null) {
    String v = rawAge.trim();
    if (!v.isEmpty()) {
        try { age = Integer.parseInt(v); }
        catch (NumberFormatException ignored) {}
    }
}
```

```java
Integer age = rawAge.trimToNull().toIntOrNull();
```

Same null-safety, same behavior. Also available: `toLongOrNull()`, `toDoubleOrNull()`, `toBigDecimalOrNull()`, `toBooleanOrNull()`.

---

### 2. Null-safe defaults

```java
String displayName = username.trimToNull().orDefault("Anonymous");
String locale      = config.getOrEmpty("locale");       // "" instead of null
User   resolved    = candidate.orElseGet(this::loadFallback);
```

---

### 3. String checks that read like English

```java
if (email.isBlankSafe())       { ... }   // null, empty, or whitespace-only
if (!username.isAlphanumeric()) { ... }
if (!contact.isEmail())         { ... }
if (token.isNull())             { ... }   // no Objects.isNull import needed
if (response.isNotNull())       { ... }
```

---

### 4. Text formatting and masking

```java
String note   = raw.normalizeWhitespace().truncate(200, "...");
String slug   = title.toSlug();          // "Spring Boot Guide" → "spring-boot-guide"
String masked = card.mask(4);            // "1234567890123456"  → "************3456"
String ref    = id.padStart(10, '0');    // "42"                → "0000000042"
```

---

### 5. Validation

Collect and throw multiple errors the clean way:

```java
List<String> errors = new ArrayList<>();
if (password == null || password.isBlank())     errors.add("Password required");
else if (password.length() < 8)                errors.add("Min 8 characters");
else if (!password.matches(".*[A-Z].*"))        errors.add("Needs an uppercase");
else if (!password.matches(".*\\d.*"))          errors.add("Needs a digit");
if (!errors.isEmpty()) throw new IllegalArgumentException(String.join(", ", errors));
```

```java
FluentValidator.ofString(password)
        .notBlank("Password required")
        .minLength(8, "Min 8 characters")
        .matches(".*[A-Z].*", "Needs an uppercase")
        .matches(".*\\d.*", "Needs a digit")
        .throwIfInvalid();
```

Validators compose across fields:

```java
FluentValidator.ofString(email)
        .notBlank("Email required")
        .matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$", "Invalid email format")
        .throwIfInvalid();

FluentValidator.of(age)
        .notNull("Age required")
        .min(18, "Must be 18 or older")
        .max(120, "Invalid age")
        .throwIfInvalid();

FluentValidator.of(tags)
        .notEmpty("At least one tag required")
        .throwIfInvalid();
```

Prefer collecting errors without throwing:

```java
List<String> errors = FluentValidator.ofString(email)
        .notBlank("Email required")
        .matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$", "Invalid format")
        .validate();

boolean ok     = validator.isValid();
String  first  = validator.firstError();   // null when valid
```

Custom exception:

```java
FluentValidator.of(order)
        .notNull("Order required")
        .satisfies(o -> o.getAmount() > 0, "Amount must be positive")
        .throwIfInvalid(msgs -> new OrderValidationException(msgs));
```

---

### 6. List pipelines

```java
List<String> emails = users
        .filterBy(User::isPremium)
        .mapTo(User::getEmail)
        .filterNotNull()
        .mapTo(String::trim)
        .filterBy(s -> !s.isEmpty())
        .distinctBy(String::toLowerCase)
        .sortedBy(String::toLowerCase);
```

Safe access without try/catch or bounds checks:

```java
User  first = candidates.firstOrNull(User::isActive);
User  last  = history.lastOrNull();
User  item  = page.getOrNull(index);        // no IndexOutOfBoundsException
```

Predicates:

```java
boolean hasAdmin = users.any(User::isAdmin);
boolean allValid = users.all(User::isVerified);
boolean noBanned = users.none(User::isBanned);
long    pending  = orders.countBy(o -> o.getStatus() == PENDING);
```

---

### 7. Aggregation and grouping

```java
// Collectors.groupingBy / stream().mapToDouble().sum() / .average().orElse(0.0)
Map<String, List<Order>> byRegion = orders.groupBy(Order::getRegion);
Map<Long, User>          byId     = users.associateBy(User::getId);

double total   = orders.sumOf(Order::getAmount);
double average = orders.averageOf(Order::getAmount);
Order  biggest = orders.maxByOrNull(Order::getAmount);

List<List<Order>> batches = orders.chunked(50);
List<Order>       currentPage = orders.paginate(2, 20);    // page 2, 20 per page
```

---

### 8. Date semantics

```java
// ChronoUnit.DAYS.between / DayOfWeek.SATURDAY checks / Period.between
if (dueDate.isPast())                     { applyLateFee(invoice); }
if (paymentDate.isWeekend())              { paymentDate = paymentDate.plusDays(2); }
if (birthDate.yearsUntilNow() >= 18)      { grantAccess(); }
if (invoiceDate.daysUntil(LocalDate.now()) > 30)  { sendReminder(); }
if (slot.isBetween(openDate, closeDate))  { allow(); }

String display = reportDate.format("dd/MM/yyyy");
```

---

### 9. Map safe access and transformation

```java
// config != null && config.containsKey(...) && config.get(...) != null
String locale = settings.getOrEmpty("locale");
Integer timeout = prefs.getOrNull("timeout");

Map<String, String> activeFeatures = flags
        .filterKeys(k -> k.startsWith("feature."))
        .filterValues("true"::equals);

Map<String, Integer> wordLengths = words.mapValues(String::length);
```

---

### 10. Object pipelines

```java
// side-effect and return the same object (logging, auditing, events)
Order saved = order.also(this::audit).also(this::notify);

// conditional selection — null when predicate fails, chainable with orElseGet
User active = user.takeIf(User::isEnabled).orElseGet(this::loadFallback);

// inline transform without a temporary variable
String city = user.let(u -> u.getAddress() == null ? "N/A" : u.getAddress().getCity());

// early exit
if (payload.isNull()) return Response.badRequest();
```

---

## API reference

### String
`trimToNull`  `orDefault`  `orEmpty`  `isBlankSafe`  `isNullOrBlank`  `isNullOrEmpty`
`toIntOrNull`  `toLongOrNull`  `toDoubleOrNull`  `toBigDecimalOrNull`  `toBooleanOrNull`
`isEmail`  `isAlphanumeric`  `isAlpha`  `isNumeric`
`normalizeWhitespace`  `truncate`  `mask`  `toSlug`  `capitalized`  `reversed`
`padStart`  `padEnd`  `take`  `takeLast`  `drop`  `dropLast`
`removePrefix`  `removeSuffix`  `containsIgnoreCase`  `countOccurrences`
`toCamelCase`  `toSnakeCase`  `stripAccents`

### List
`filterBy`  `mapTo`  `flatMap`  `mapNotNull`  `filterNotNull`
`distinctBy`  `sortedBy`  `sortedByDescending`
`firstOrNull`  `lastOrNull`  `getOrNull`  `getOrDefault`
`groupBy`  `associateBy`  `partition`  `chunked`  `paginate`  `zip`
`sumOf`  `averageOf`  `maxByOrNull`  `minByOrNull`
`any`  `all`  `none`  `countBy`
`takeWhile`  `dropWhile`  `intersect`  `subtract`  `union`
`isNullOrEmpty`  `orEmpty`

### Validator
`of(value)`  `ofString(value)`
`notNull`  `notBlank`  `notEmpty`  `satisfies`
`minLength`  `maxLength`  `matches`
`min`  `max`  `positive`
`validate`  `List<String>`  `isValid`  `firstError`  `throwIfInvalid`

### Map
`getOrEmpty`  `getOrNull`  `filterKeys`  `filterValues`  `mapValues`  `mapKeys`
`any`  `all`  `none`  `count`  `toList`  `merge`  `invertMap`
`isNullOrEmpty`  `orEmpty`

### Object
`isNull`  `isNotNull`  `orElse`  `orElseGet`
`takeIf`  `takeUnless`  `let`  `also`
`requireNotNull`  `requireThat`

### Date (`LocalDate` / `LocalDateTime`)
`isPast`  `isFuture`  `isToday`  `isWeekday`  `isWeekend`  `isLeapYear`
`daysUntil`  `monthsUntil`  `yearsUntilNow`
`isBefore`  `isAfter`  `isBetween`
`format`  `atStartOfDay`  `atEndOfDay`
`toEpochMillis`  `fromEpochMillis`

### Number
`isBetween`  `coerceIn`  `coerceAtLeast`  `coerceAtMost`
`isPositive`  `isNegative`  `isZero`  `isEven`  `isOdd`
`roundTo`  `percentOf`  `toOrdinal`

---

## How it works

FluentJava is a `javac` compile-time plugin. When you write `raw.trimToNull()`, the plugin rewrites it to `FluentString.trimToNull(raw)` before bytecode is emitted. The resulting `.class` file contains standard `invokestatic` instructions, identical to code you would write by hand. Decompile it and you see plain static calls — no agent, no reflection, no magic at runtime.

---

## License

Apache License, Version 2.0
