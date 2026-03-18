<div align="center">

# ⚡ FluentJava

**Extension methods for Java — at compile time.**

*Write Java. Read Kotlin.*

[![Java 17+](https://img.shields.io/badge/Java-17%2B-blue?logo=openjdk&logoColor=white)](https://openjdk.org/)
[![License](https://img.shields.io/badge/License-Apache%202.0-green.svg)](LICENSE)
[![Maven Central](https://img.shields.io/badge/Maven%20Central-1.0.0-orange?logo=apache-maven)](https://central.sonatype.com/)
[![Zero Runtime Overhead](https://img.shields.io/badge/Runtime%20Overhead-Zero-brightgreen)](#how-it-works)

[📖 Documentation](https://fluent-java.github.io/fluent-java-doc/fluentjava-doc.html) · [🔌 IntelliJ Plugin](../fluentjava-intellij-plugin/)

</div>

---

FluentJava brings **extension methods** to Java. Call intuitive, chainable methods directly on `String`, `List`, `Map`, `Optional`, `Path`, `LocalDate`, numbers, and any object — without inheritance, wrappers, or runtime tricks.

```java
// ❌ Before — verbose, null-unsafe, scattered helpers
String slug = null;
if (title != null) {
    String trimmed = title.trim();
    if (!trimmed.isEmpty()) {
        slug = trimmed.toLowerCase()
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("^-+|-+$", "");
    }
}

// ✅ After — one line, null-safe, reads like natural language
String slug = title.trimToNull().toSlug();
```

At compile time, the FluentJava javac plugin rewrites fluent calls into plain static method invocations. **In production, there is no agent, no reflection, no runtime dependency beyond a lightweight utility JAR.** Same approach as [Lombok](https://projectlombok.org/) and [Error Prone](https://errorprone.info/) — a battle-tested compiler plugin technique.

> 📖 **Full documentation:** [fluent-java.github.io/fluent-java-doc](https://fluent-java.github.io/fluent-java-doc/fluentjava-doc.html)

---

## Why FluentJava?

| | Feature | Description |
|---|---------|-------------|
| 🧹 | **Readable code** | Replace verbose utility calls with expressive, chainable syntax that reads like natural language |
| 🔒 | **Null-safe by design** | Every method handles `null` gracefully — no more `NullPointerException` surprises |
| ⚡ | **Zero runtime overhead** | Compile-time AST rewrite → plain `invokestatic` calls. No reflection, no proxy, no agent |
| 🧩 | **250+ built-in methods** | Covers String (60), List (51), Map (22), Number (20), Date (34), Optional (9), Path (14), Object (10) |
| 🔧 | **Extensible via `@FluentExtension`** | Add your own fluent methods on **any** class — including JDK and third-party types |
| 🖥️ | **Full IntelliJ support** | Autocompletion, zero red squiggles, Ctrl+B navigation, Ctrl+Q documentation |
| 📦 | **Drop-in Maven & Gradle** | One dependency + one plugin. Works with existing projects instantly |
| 🔄 | **Works without plugin too** | The runtime doubles as a standalone static utility library |

### How does it compare?

| Concern | Apache Commons / Guava | Kotlin stdlib | **FluentJava** |
|---------|----------------------|---------------|----------------|
| Syntax | `StringUtils.trimToNull(s)` | `s.trimToNull()` | `s.trimToNull()` |
| Null safety | Manual checks everywhere | Built-in (`?.`) | Built-in (every method) |
| Extensibility | Write more utility classes | Extension functions | `@FluentExtension` |
| Runtime cost | Library on classpath | Kotlin runtime (~1.5 MB) | Static calls only |
| Language | Java | Kotlin | **Java** |

---

## 20 Before & After Examples

### String Operations

#### 1. Clean and slugify a title

❌ **Before** — Java:
```java
String slug = null;
if (title != null) {
    String trimmed = title.trim();
    if (!trimmed.isEmpty()) {
        slug = trimmed.toLowerCase()
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("^-+|-+$", "");
    }
}
```

✅ **After** — FluentJava:
```java
String slug = title.trimToNull().toSlug();
```

---

#### 2. Parse an integer safely

❌ **Before:**
```java
Integer quantity = null;
if (rawQuantity != null) {
    String trimmed = rawQuantity.trim();
    if (!trimmed.isEmpty()) {
        try {
            quantity = Integer.valueOf(trimmed);
        } catch (NumberFormatException ignored) {}
    }
}
```

✅ **After:**
```java
Integer quantity = rawQuantity.toIntOrNull();
```

---

#### 3. Mask sensitive data

❌ **Before:**
```java
String masked;
if (cardNumber == null || cardNumber.length() <= 4) {
    masked = cardNumber;
} else {
    int hidden = cardNumber.length() - 4;
    masked = "*".repeat(hidden) + cardNumber.substring(hidden);
}
```

✅ **After:**
```java
String masked = cardNumber.mask(4);
// "4532015112830366" → "************0366"
```

---

#### 4. Normalize whitespace and extract initials

❌ **Before:**
```java
String initials = "";
if (fullName != null) {
    String normalized = fullName.trim().replaceAll("\\s+", " ");
    if (!normalized.isEmpty()) {
        StringBuilder sb = new StringBuilder();
        for (String part : normalized.split(" ")) {
            if (!part.isEmpty()) sb.append(Character.toUpperCase(part.charAt(0)));
        }
        initials = sb.toString();
    }
}
```

✅ **After:**
```java
String initials = fullName.normalizeWhitespace().toInitials();
// "  Jean   Pierre  Dupont  " → "JPD"
```

---

#### 5. Validate an email format

❌ **Before:**
```java
boolean valid = false;
if (email != null) {
    valid = email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
}
```

✅ **After:**
```java
boolean valid = email.isEmail();
```

---

#### 6. Convert between case formats

❌ **Before:**
```java
// camelCase to snake_case — fragile regex
String snake = input.replaceAll("([a-z])([A-Z])", "$1_$2").toLowerCase();

// snake_case to camelCase — manual loop
String[] parts = input.split("_");
StringBuilder camel = new StringBuilder(parts[0].toLowerCase());
for (int i = 1; i < parts.length; i++) {
    if (!parts[i].isEmpty()) {
        camel.append(Character.toUpperCase(parts[i].charAt(0)))
             .append(parts[i].substring(1).toLowerCase());
    }
}
```

✅ **After:**
```java
String snake  = input.toSnakeCase();    // "myFieldName" → "my_field_name"
String camel  = input.toCamelCase();    // "my_field_name" → "myFieldName"
String kebab  = input.toKebabCase();    // "myFieldName" → "my-field-name"
String pascal = input.toPascalCase();   // "my_field_name" → "MyFieldName"
```

---

### List Operations

#### 7. Filter, transform, and deduplicate

❌ **Before:**
```java
List<String> emails = new ArrayList<>();
Set<String> seen = new LinkedHashSet<>();
for (User user : users) {
    if (user != null && user.isActive() && user.getEmail() != null) {
        String email = user.getEmail().trim().toLowerCase();
        if (!email.isEmpty() && seen.add(email)) {
            emails.add(email);
        }
    }
}
```

✅ **After:**
```java
List<String> emails = users
    .filterBy(User::isActive)
    .mapTo(User::getEmail)
    .mapNotNull(e -> e.trimToNull())
    .mapTo(String::toLowerCase)
    .distinctBy(e -> e);
```

---

#### 8. Group by a business key

❌ **Before:**
```java
Map<String, List<User>> byDept = new LinkedHashMap<>();
for (User user : users) {
    byDept.computeIfAbsent(user.getDepartment(), k -> new ArrayList<>()).add(user);
}
```

✅ **After:**
```java
Map<String, List<User>> byDept = users.groupBy(User::getDepartment);
```

---

#### 9. Paginate results safely

❌ **Before:**
```java
List<Order> page = Collections.emptyList();
int start = Math.max(0, (pageNum - 1) * size);
if (start < orders.size()) {
    page = orders.subList(start, Math.min(orders.size(), start + size));
}
```

✅ **After:**
```java
List<Order> page = orders.paginate(pageNum, size);
```

---

#### 10. Sort and pick first match

❌ **Before:**
```java
User cheapest = null;
users.sort(Comparator.comparingDouble(User::getSalary));
for (User u : users) {
    if (u.getDepartment().equals("Engineering")) {
        cheapest = u;
        break;
    }
}
```

✅ **After:**
```java
User cheapest = users
    .sortedBy(User::getSalary)
    .firstOrNull(u -> u.getDepartment().equals("Engineering"));
```

---

#### 11. Aggregate numeric values

❌ **Before:**
```java
double total = 0;
int count = 0;
for (Order order : orders) {
    total += order.getAmount();
    count++;
}
double average = count > 0 ? total / count : 0;
```

✅ **After:**
```java
double total   = orders.sumOf(Order::getAmount);
double average = orders.averageOf(Order::getAmount);
```

---

### Map Operations

#### 12. Lazy-initialize a map entry

❌ **Before:**
```java
List<String> errors = errorMap.get("email");
if (errors == null) {
    errors = new ArrayList<>();
    errorMap.put("email", errors);
}
errors.add("Invalid address");
```

✅ **After:**
```java
errorMap.getOrPut("email", ArrayList::new).add("Invalid address");
```

---

#### 13. Merge maps and filter keys

❌ **Before:**
```java
Map<String, Object> merged = new HashMap<>(defaults);
merged.putAll(overrides);
Map<String, Object> filtered = new LinkedHashMap<>();
for (Map.Entry<String, Object> entry : merged.entrySet()) {
    if (entry.getKey().startsWith("app.")) {
        filtered.put(entry.getKey(), entry.getValue());
    }
}
```

✅ **After:**
```java
Map<String, Object> result = defaults
    .merge(overrides)
    .filterKeys(k -> k.startsWith("app."));
```

---

### Number Operations

#### 14. Clamp and round a value

❌ **Before:**
```java
double price = rawPrice;
if (price < 0) price = 0;
if (price > 9999.99) price = 9999.99;
price = Math.round(price * 100.0) / 100.0;
```

✅ **After:**
```java
double price = rawPrice.coerceIn(0, 9999.99).roundTo(2);
```

---

#### 15. Number formatting and checks

❌ **Before:**
```java
String label;
if (position == 1) label = "1st";
else if (position == 2) label = "2nd";
else if (position == 3) label = "3rd";
else label = position + "th";
boolean even = position % 2 == 0;
```

✅ **After:**
```java
String label = position.toOrdinal();   // "1st", "2nd", "3rd", "4th"...
boolean even = position.isEven();
```

---

### Date Operations

#### 16. Business day logic

❌ **Before:**
```java
DayOfWeek dow = date.getDayOfWeek();
boolean businessDay = dow != DayOfWeek.SATURDAY && dow != DayOfWeek.SUNDAY;

LocalDate next = date;
do {
    next = next.plusDays(1);
} while (next.getDayOfWeek() == DayOfWeek.SATURDAY
      || next.getDayOfWeek() == DayOfWeek.SUNDAY);
```

✅ **After:**
```java
boolean businessDay = date.isBusinessDay();
LocalDate next = date.nextWeekday();
```

---

#### 17. Date calculations and formatting

❌ **Before:**
```java
long days = ChronoUnit.DAYS.between(startDate, endDate);
String formatted = startDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
```

✅ **After:**
```java
long days = startDate.daysUntil(endDate);
String formatted = startDate.format("dd/MM/yyyy");
```

---

### Optional, Path & Validator

#### 18. Optional pipeline

❌ **Before:**
```java
String city = null;
if (user != null && user.getAddress() != null) {
    city = user.getAddress().getCity();
}
Optional<String> opt = Optional.ofNullable(city);
String result = opt.map(String::toUpperCase).orElse(null);
```

✅ **After:**
```java
String result = city.toOptional()
    .mapTo(String::toUpperCase)
    .orNull();
```

---

#### 19. File operations

❌ **Before:**
```java
String content = Files.readString(path, StandardCharsets.UTF_8);
Files.writeString(output, transformed, StandardCharsets.UTF_8,
    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
```

✅ **After:**
```java
String content = path.readText();
output.writeText(transformed);
```

---

#### 20. Fluent validation

❌ **Before:**
```java
List<String> errors = new ArrayList<>();
if (name == null || name.isBlank()) errors.add("Name is required");
if (name != null && name.length() < 3) errors.add("Name too short");
if (name != null && name.length() > 80) errors.add("Name too long");
if (email == null || !email.matches("^.+@.+\\..+$")) errors.add("Invalid email");
```

✅ **After:**
```java
List<String> errors = FluentValidator.ofString(name)
    .notBlank("Name is required")
    .minLength(3, "Name too short")
    .maxLength(80, "Name too long")
    .validate();
```

---

## Getting Started

FluentJava requires **Java 17+**.

### Maven (Recommended)

```xml
<dependencies>
    <dependency>
        <groupId>io.fluentjava</groupId>
        <artifactId>fluentjava-runtime</artifactId>
        <version>1.0.0</version>
    </dependency>
</dependencies>

<build>
    <plugins>
        <plugin>
            <groupId>io.fluentjava</groupId>
            <artifactId>fluentjava-maven-plugin</artifactId>
            <version>1.0.0</version>
            <extensions>true</extensions>
        </plugin>
    </plugins>
</build>
```

That's it. The Maven plugin auto-configures the compiler (`fork`, `-Xplugin`, `--add-exports`).
No annotation processor. No bytecode agent. No config hell.

### Gradle — Groovy DSL

```groovy
plugins {
    id 'java'
    id 'io.fluentjava' version '1.0.0'
}

dependencies {
    implementation 'io.fluentjava:fluentjava-runtime:1.0.0'
}
```

### Gradle — Kotlin DSL

```kotlin
plugins {
    java
    id("io.fluentjava") version "1.0.0"
}

dependencies {
    implementation("io.fluentjava:fluentjava-runtime:1.0.0")
}
```

Then just write:

```java
String slug = title.trimToNull().toSlug();
List<String> names = users.mapTo(User::getName).filterNotNull();
boolean isOpen = date.isBusinessDay();
```

---

## IntelliJ IDEA Support

The companion [**FluentJava IntelliJ Plugin**](../fluentjava-intellij-plugin/) makes the IDE experience seamless:

| Feature | Description |
|---------|-------------|
| **Zero red squiggles** | Fluent methods injected via `PsiAugmentProvider` (same technique as Lombok) |
| **Autocompletion** | Methods appear after `.` on all supported types |
| **Go To Declaration** | `Ctrl+B` navigates to the utility class or your `@FluentExtension` source |
| **Inline Documentation** | `Ctrl+Q` shows description, signature, and usage example |
| **Auto-configuration** | `compiler.xml` is configured automatically on project open |
| **Dynamic extensions** | Your project's `@FluentExtension` classes are discovered in real-time |

> See the full [IntelliJ Plugin README](../fluentjava-intellij-plugin/README.md) for installation and details.

---

## Create Your Own Extension Methods

One of FluentJava's most powerful features: add fluent methods on **any class** — JDK types, third-party libraries, or your own domain objects.

### Example

```java
package com.acme.extensions;

import io.fluentjava.annotation.FluentExtension;

@FluentExtension(target = String.class)
public final class MoneyExtensions {

    private MoneyExtensions() {}

    /**
     * Formats a numeric string as a price with currency symbol.
     * "1299.99".toPrice("€") → "€1,299.99"
     */
    public static String toPrice(String value, String currencySymbol) {
        if (value == null) return null;
        double amount = Double.parseDouble(value);
        return currencySymbol + String.format("%,.2f", amount);
    }
}
```

Usage — feels like a native method:

```java
String display = "1299.99".toPrice("€");
// → "€1,299.99"
```

### Rules

| Rule | Description |
|------|-------------|
| **Annotation** | Class must be annotated `@FluentExtension(target = YourType.class)` |
| **Methods** | Must be `public static` |
| **First parameter** | Acts as the receiver (_the object before the dot_) |
| **Other parameters** | Become the method arguments |
| **Null safety** | Handle `null` receiver explicitly (recommended) |

### Why this is powerful

- **Domain-specific DSLs** — Build a vocabulary that matches your team's language
- **Testable** — Regular static methods, easy to unit test
- **IDE-supported** — The IntelliJ plugin picks up `@FluentExtension` classes automatically
- **No inheritance required** — Extend `final` classes, JDK types, third-party types
- **Compile-time rewrite** — Same AST transformation as built-in methods

---

## Use Without Compiler Plugin

FluentJava's runtime works as a **standalone utility library**. No compiler plugin required:

```java
import static io.fluentjava.string.FluentString.*;
import static io.fluentjava.list.FluentList.*;

// Direct static calls
String slug = toSlug(trimToNull(title));
String safeName = orDefault(name, "Anonymous");
User first = firstOrNull(users);
```

In this mode, you only need the `fluentjava-runtime` dependency — no plugin in your build config.

---

## Complete API Reference

> 📖 **Full reference with examples:** [fluent-java.github.io/fluent-java-doc](https://fluent-java.github.io/fluent-java-doc/fluentjava-doc.html)

| Type | Class | Methods | Highlights |
|------|-------|:-------:|------------|
| `Object` | `FluentObject` | 10 | `also`, `let`, `takeIf`, `takeUnless`, `isNull`, `orElse`, `requireNotNull` |
| `String` | `FluentString` | 60 | `trimToNull`, `toSlug`, `toIntOrNull`, `mask`, `isEmail`, `toCamelCase`, `toBase64`, `digest` |
| `List` | `FluentList` | 51 | `filterBy`, `mapTo`, `groupBy`, `paginate`, `distinctBy`, `sumOf`, `sortedBy`, `chunked` |
| `Map` | `FluentMap` | 22 | `getOrPut`, `filterKeys`, `mapValues`, `merge`, `invertMap`, `toSortedMap` |
| `Number` | `FluentNumber` | 20 | `coerceIn`, `roundTo`, `isBetween`, `isEven`, `toOrdinal`, `isPrime`, `toHex` |
| `LocalDate` / `LocalDateTime` | `FluentDate` | 34 | `isBusinessDay`, `nextWeekday`, `daysUntil`, `format`, `age`, `quarterOf` |
| `Optional` | `FluentOptional` | 9 | `toOptional`, `orNull`, `mapTo`, `filterBy`, `orElseThrow` |
| `Path` | `FluentPath` | 14 | `readText`, `writeText`, `exists`, `extension`, `listFiles`, `sizeInBytes` |
| _Validation_ | `FluentValidator` | 17 | `notNull`, `notBlank`, `minLength`, `maxLength`, `matches`, `satisfies` |
| _Error handling_ | `Try` | 13 | `Try.of`, `map`, `recover`, `orElse`, `onFailure`, `onSuccess` |

**Total: 250+ methods** across 10 utility classes.

<details>
<summary><strong>FluentObject</strong> — 10 methods</summary>

`also`, `let`, `takeIf`, `takeUnless`, `isNull`, `isNotNull`, `orElse`, `orElseGet`, `requireNotNull`, `requireThat`
</details>

<details>
<summary><strong>FluentString</strong> — 60 methods</summary>

**Null-safety:** `isBlankSafe`, `isNullOrBlank`, `isNullOrEmpty`, `orEmpty`, `orDefault`, `trimToNull`, `ifBlank`, `ifEmpty`

**Parsing:** `toIntOrNull`, `toLongOrNull`, `toDoubleOrNull`, `toBooleanOrNull`, `toBigDecimalOrNull`

**Slicing & padding:** `take`, `takeLast`, `drop`, `dropLast`, `left`, `right`, `padStart`, `padEnd`, `center`, `truncate`, `ellipsize`

**Transformation:** `reversed`, `capitalized`, `decapitalized`, `removePrefix`, `removeSuffix`, `wrap`, `unwrap`, `repeat`, `normalizeWhitespace`, `stripAccents`, `toInitials`, `redact`, `toCurrency`, `toBase64`, `fromBase64`, `digest`

**Case conversion:** `toCamelCase`, `toSnakeCase`, `toPascalCase`, `toKebabCase`, `toSlug`

**Search & matching:** `containsIgnoreCase`, `startsWithIgnoreCase`, `endsWithIgnoreCase`, `countOccurrences`, `matchesPattern`, `countWords`

**Splitting:** `splitToList`, `lines`

**Validation:** `isNumeric`, `isAlpha`, `isAlphanumeric`, `isEmail`, `isUrl`, `isIPv4`

**Security:** `mask`
</details>

<details>
<summary><strong>FluentList</strong> — 51 methods</summary>

**Null-safety:** `isNullOrEmpty`, `orEmpty`

**Access:** `firstOrNull`, `firstOrNull(predicate)`, `lastOrNull`, `lastOrNull(predicate)`, `second`, `third`, `getOrNull`, `getOrDefault`, `randomOrNull`

**Filter & map:** `filterBy`, `mapTo`, `flatMap`, `mapNotNull`, `filterNotNull`, `forEachIndexed`

**Sort & dedup:** `sortedBy`, `sortedByDescending`, `distinctBy`, `shuffled`

**Grouping:** `groupBy`, `associateBy`, `associate`, `partition`, `frequencies`

**Aggregation:** `sumOf`, `sumOfInt`, `sumOfLong`, `averageOf`, `maxByOrNull`, `minByOrNull`, `countBy`

**Predicates:** `none`, `any`, `all`

**Slicing:** `takeWhile`, `dropWhile`, `paginate`, `chunked`, `windowed`, `sample`

**Set ops:** `zip`, `intersect`, `subtract`, `union`, `toSet`

**Other:** `indexOfFirst`, `indexOfLast`, `flatten`, `toCsv`
</details>

<details>
<summary><strong>FluentMap</strong> — 22 methods</summary>

`isNullOrEmpty`, `orEmpty`, `getOrEmpty`, `getOrNull`, `getOrPut`, `filterKeys`, `filterValues`, `filterByValue`, `mapValues`, `mapKeys`, `any`, `all`, `none`, `count`, `toList`, `merge`, `invertMap`, `forEach`, `toSortedMap`, `containsAllKeys`, `flatMapValues`, `entries`
</details>

<details>
<summary><strong>FluentNumber</strong> — 20 methods</summary>

`coerceIn`, `coerceAtLeast`, `coerceAtMost`, `isBetween`, `isPositive`, `isNegative`, `isZero`, `roundTo`, `percentOf`, `isEven`, `isOdd`, `toOrdinal`, `toPercentString`, `clamp`, `isPrime`, `factorial`, `digits`, `toBinary`, `toHex`, `toOctal`
</details>

<details>
<summary><strong>FluentDate</strong> — 34 methods</summary>

`isWeekend`, `isWeekday`, `isToday`, `isPast`, `isFuture`, `isLeapYear`, `daysUntil`, `monthsUntil`, `yearsUntilNow`, `format`, `atStartOfDay`, `atEndOfDay`, `toEpochMillis`, `fromEpochMillis`, `isBefore`, `isAfter`, `isBetween`, `startOfWeek`, `endOfWeek`, `startOfMonth`, `endOfMonth`, `startOfYear`, `endOfYear`, `nextWeekday`, `age`, `isBusinessDay`, `toLocalDate`, `toLocalDateTime`, `quarterOf`, `weekOfYear`
</details>

<details>
<summary><strong>FluentOptional</strong> — 9 methods</summary>

`toOptional`, `orNull`, `orEmpty`, `ifPresent`, `mapTo`, `filterBy`, `isPresent`, `isEmpty`, `orElseThrow`
</details>

<details>
<summary><strong>FluentPath</strong> — 14 methods</summary>

`readText`, `writeText`, `readLines`, `exists`, `extension`, `nameWithoutExtension`, `fileName`, `copyTo`, `moveTo`, `deleteIfExists`, `sizeInBytes`, `isDirectory`, `listFiles`, `createDirectories`
</details>

<details>
<summary><strong>FluentValidator</strong> — 17 methods</summary>

`of`, `ofString`, `notNull`, `satisfies`, `notBlank`, `minLength`, `maxLength`, `matches`, `notEmpty`, `min`, `max`, `positive`, `validate`, `isValid`, `throwIfInvalid`, `throwIfInvalid(exceptionFn)`, `firstError`
</details>

<details>
<summary><strong>Try</strong> — 13 methods</summary>

`Try.of`, `isSuccess`, `isFailure`, `orNull`, `orElse`, `orElseGet`, `orElseThrow`, `map`, `flatMap`, `recover`, `onSuccess`, `onFailure`, `getError`
</details>

---

## How It Works

```
   What you write              What javac sees              What runs in production
┌─────────────────────┐   ┌──────────────────────────┐   ┌──────────────────────────────┐
│ title.trimToNull()  │ → │ FluentString.trimToNull  │ → │ invokestatic FluentString    │
│      .toSlug()      │   │  (FluentString.toSlug    │   │   .trimToNull(String)        │
│                     │   │   (title))               │   │ invokestatic FluentString    │
│                     │   │                          │   │   .toSlug(String)            │
└─────────────────────┘   └──────────────────────────┘   └──────────────────────────────┘
       Source                   After AST rewrite                  Bytecode
```

1. **PARSE phase** — The javac plugin intercepts the AST after parsing, before type resolution
2. **Rewrite** — `receiver.method(args)` becomes `method(receiver, args)` + static imports injected
3. **Compile** — javac resolves types against the rewritten AST → clean `invokestatic` bytecode

Same proven technique used by **Lombok**, **Error Prone**, and **Checker Framework**.

### Modules

| Module | Role |
|--------|------|
| `fluentjava-runtime` | Production JAR — static utility methods + `@FluentExtension` annotation |
| `fluentjava-plugin` | Javac plugin — AST rewrite at compile time _(never deployed to production)_ |
| `fluentjava-maven-plugin` | Maven integration — auto-configures the compiler |
| `fluentjava-gradle-plugin` | Gradle integration — auto-configures the compiler |

### Security guarantees

| Property | Guarantee |
|----------|-----------|
| **Runtime reflection** | Zero |
| **JVM agents** | Zero |
| **`sun.misc.Unsafe`** | Zero |
| **`--add-opens` at runtime** | Zero |
| **Plugin scope** | `provided` — never in production JAR |

---

## FAQ

**Does FluentJava add runtime overhead?**
No. The compiled bytecode is plain `invokestatic` calls — identical to calling utility methods manually.

**Do I have to use the compiler plugin?**
No. You can use FluentJava purely as a static utility library with direct calls like `FluentString.trimToNull(s)`.

**Can I add my own fluent methods?**
Yes. `@FluentExtension` lets you add methods on any class — JDK, third-party, or your own.

**Does the IDE understand my custom extensions?**
Yes. The IntelliJ plugin discovers `@FluentExtension` classes from your project automatically.

**Does FluentJava replace JDK APIs?**
No. It complements them with a convenience layer for common operations.

**Is it safe for enterprise codebases?**
Yes. Compile-time only, auditable, no bytecode manipulation at runtime, no agents, no reflection.

**Which Java version is required?**
Java 17 or higher (LTS).

**Does it work with Lombok?**
Yes. Both use javac plugin APIs and coexist without conflict.

---

## License

Apache License 2.0 — see [LICENSE](LICENSE).

---

<div align="center">

**[📖 Documentation](https://fluent-java.github.io/fluent-java-doc/fluentjava-doc.html)** · **[🔌 IntelliJ Plugin](../fluentjava-intellij-plugin/)**

_FluentJava — Because Java deserves extension methods._

</div>
