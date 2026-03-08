<p align="center">
  <img src="https://img.shields.io/badge/Java-17%2B-orange?logo=openjdk&logoColor=white" alt="Java 17+"/>
  <img src="https://img.shields.io/badge/Maven%20Central-1.0.0-blue" alt="Maven Central"/>
  <img src="https://img.shields.io/badge/License-MIT-green" alt="License MIT"/>
  <img src="https://img.shields.io/badge/IntelliJ%20Plugin-FluentJava-violet?logo=intellijidea&logoColor=white" alt="IntelliJ Plugin"/>
  <img src="https://img.shields.io/badge/Dependencies-Zero-teal" alt="Zero Dependencies"/>
  <img src="https://img.shields.io/badge/Build-passing-brightgreen" alt="Build passing"/>
</p>

# FluentJava

**Extension methods for Java's core types — resolved at compile time, zero overhead at runtime.**

Write `str.toSlug()` instead of 10 lines of `Normalizer.normalize()` + `replaceAll()` chains. FluentJava's javac plugin rewrites it to `FluentString.toSlug(str)` **before** compilation. The bytecode is pure `invokestatic` — identical to what you'd write by hand.

---

### Before / After

**String cleanup and slug generation**

```java
// ❌ Before — Java pur standard, verbeux et illisible
String input = rawInput != null ? rawInput.trim() : null;
if (input != null && input.isEmpty()) input = null;
if (input != null) {
    input = java.text.Normalizer.normalize(input, java.text.Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
    input = input.toLowerCase()
                 .replaceAll("[^a-z0-9\\s-]", "")
                 .trim()
                 .replaceAll("\\s+", "-")
                 .replaceAll("-+", "-");
}
String slug = input;

// ✅ After — FluentJava
String slug = rawInput.trimToNull().stripAccents().toSlug();
```

**List transformation pipeline**

```java
// ❌ Before — Java Stream
List<String> emails = users == null ? Collections.emptyList() :
    users.stream()
         .filter(u -> u != null && u.isPremium())
         .map(User::getEmail)
         .filter(Objects::nonNull)
         .map(String::toLowerCase)
         .distinct()
         .sorted()
         .collect(Collectors.toList());

// ✅ After — FluentJava
List<String> emails = users
    .filterBy(User::isPremium)
    .mapTo(User::getEmail)
    .filterNotNull()
    .distinctBy(String::toLowerCase)
    .sortedBy(String::toLowerCase);
```

**Safe parsing**

```java
// ❌ Before — Java pur, try/catch obligatoire
Integer age = null;
if (rawAge != null) {
    String trimmed = rawAge.trim();
    if (!trimmed.isEmpty()) {
        try {
            age = Integer.parseInt(trimmed);
        } catch (NumberFormatException ignored) {}
    }
}
if (age == null || age < 18 || age > 120) {
    throw new IllegalArgumentException("Invalid age");
}

// ✅ After — FluentJava
Integer age = rawAge.trimToNull().toIntOrNull();
if (age == null || !age.isBetween(18, 120)) {
    throw new IllegalArgumentException("Invalid age");
}
```

---

## Table of Contents

- [What is FluentJava?](#what-is-fluentjava)
- [Quick Start](#-quick-start)
- [Configuration](#-configuration)
- [IntelliJ Plugin](#-intellij-plugin)
- [Static API (without plugin)](#-using-fluentjava-without-the-plugin--static-api)
- [10 Real-World Examples](#-10-real-world-examples)
- [API Reference](#-api-reference)
- [Extending FluentJava](#-extending-fluentjava)
- [Technical Details](#️-technical-details)
- [FAQ](#-faq)

---

## What is FluentJava?

### The problem

Java forces you to write verbose, nested static calls or multi-line Stream pipelines for common operations. Trimming a string to null, parsing safely, filtering a list — each one requires boilerplate that obscures intent.

FluentJava solves this by adding **extension methods** on `String`, `List`, `Map`, `Number`, `LocalDate`, `Optional`, and `Path`. You write left-to-right fluent code. The javac plugin rewrites it into standard static calls at compile time.

### How the transformation works

```
Source code              Compile time                  Bytecode
───────────              ────────────                  ────────
str.toSlug()     ──▶    FluentJava Plugin  ──▶    FluentString.toSlug(str)
list.filterBy()  ──▶    (AST rewrite)      ──▶    FluentList.filterBy(list, fn)
date.isWeekday() ──▶    pre-javac          ──▶    FluentDate.isWeekday(date)
```

The plugin operates during the `PARSE` phase — before type resolution. It rewrites `obj.method(args)` into `method(obj, args)` and injects the necessary `import static` declarations. javac then compiles the result as normal Java.

### Naming convention — `splitToList` / `lines`

FluentJava avoids shadowing Java standard methods. When a method name would conflict with an existing Java method (like `String.lines()` returning `Stream<String>` or `String.split()` returning `String[]`), FluentJava uses a distinct name:

```java
// Java standard — returns Stream<String>
str.lines()

// FluentJava — returns List<String>, null-safe
str.lines()           // Renamed internally, no conflict

// Java standard — returns String[], throws NPE on null
str.split(",")

// FluentJava — returns List<String>, null-safe, filters empty entries
str.splitToList(",")
```

> Methods that have the same name as a Java standard method but different behavior use a distinct name (`splitToList`, `matchesPattern`, etc.) to avoid ambiguity. Zero surprise.

### Key principles

| Principle | Detail |
|-----------|--------|
| Zero runtime overhead | `invokestatic` — identical to hand-written static calls |
| Zero annotations | Nothing to add to your existing code |
| Zero runtime reflection | Reflection is only used at compile-time for method discovery |
| Zero external dependencies | No transitive dependencies |
| Full IDE support | IntelliJ plugin with autocompletion, Go To Declaration, documentation |
| No naming conflicts | Distinct names for methods that would shadow Java standard API |

---

## ⚡ Quick Start

### Maven

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
            <executions>
                <execution>
                    <goals><goal>configure</goal></goals>
                </execution>
            </executions>
        </plugin>
    </plugins>
</build>
```

### Gradle (Groovy DSL)

```groovy
plugins {
    id 'java'
    id 'io.fluentjava' version '1.0.0'
}

dependencies {
    implementation 'io.fluentjava:fluentjava-runtime:1.0.0'
}
```

### Gradle (Kotlin DSL)

```kotlin
plugins {
    java
    id("io.fluentjava") version "1.0.0"
}

dependencies {
    implementation("io.fluentjava:fluentjava-runtime:1.0.0")
}
```

> The Maven and Gradle plugins automatically inject all required compiler flags. You never configure them manually.

---

## ⚙ Configuration

### What the plugins inject automatically

Behind the scenes, the build plugins configure `maven-compiler-plugin` (or Gradle's `JavaCompile` tasks) with:

```xml
<arg>-Xplugin:FluentJava</arg>
<fork>true</fork>
```

You never need to write this yourself.

### Manual configuration (for constrained build pipelines)

<details>
<summary><b>Maven — manual setup</b></summary>

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
                <fork>true</fork>
                <compilerArgs>
                    <arg>-Xplugin:FluentJava</arg>
                </compilerArgs>
                <annotationProcessorPaths>
                    <path>
                        <groupId>io.fluentjava</groupId>
                        <artifactId>fluentjava-plugin</artifactId>
                        <version>1.0.0</version>
                    </path>
                </annotationProcessorPaths>
            </configuration>
        </plugin>
    </plugins>
</build>
```

</details>

<details>
<summary><b>Gradle — manual setup</b></summary>

```groovy
dependencies {
    implementation 'io.fluentjava:fluentjava-runtime:1.0.0'
    compileOnly   'io.fluentjava:fluentjava-plugin:1.0.0'
    annotationProcessor 'io.fluentjava:fluentjava-plugin:1.0.0'
}

tasks.withType(JavaCompile).configureEach {
    options.fork = true
    options.compilerArgs += [
        '-Xplugin:FluentJava'
    ]
}
```

</details>

### Compatibility

| Java | Maven | Gradle | Status |
|------|-------|--------|--------|
| 17   | 3.8+  | 7.0+   | ✅ Supported |
| 21   | 3.8+  | 8.0+   | ✅ Supported |
| 22+  | 3.12+ | 8.5+   | ✅ Supported |

---

## 🧩 IntelliJ Plugin

**Plugin name: `FluentJava`** — available on the JetBrains Marketplace.

### Installation

**From Marketplace:** Settings → Plugins → Marketplace → search `FluentJava` → Install → Restart

### Features

| Feature | Description |
|---------|-------------|
| **Autocompletion** | `str.` shows FluentJava methods alongside Java native methods |
| **Go To Declaration** | `Ctrl+B` on `str.toSlug()` navigates to `FluentString.toSlug()` |
| **Inline documentation** | `Ctrl+Q` shows description + usage example |
| **Zero red errors** | The IDE understands fluent calls natively — no false error marks |
| **Rename refactoring** | Renaming a method updates all usages |

### How it works internally

- **`PsiAugmentProvider`** — same API as Lombok. Injects virtual `LightMethodBuilder` instances into the PSI tree of `String`, `List`, `Map`, etc.
- **`FluentMethodRegistry`** — single source of truth. Every component reads from it.
- **`FluentJavaCompletionContributor`** — populates the autocompletion popup from the registry.
- **`FluentJavaGotoDeclarationHandler`** — resolves `Ctrl+B` to the real `FluentXxx` class.
- **`FluentJavaDocumentationProvider`** — generates hover docs from the registry.

---

## 📖 Using FluentJava Without the Plugin — Static API

FluentJava can be used **without the javac plugin** by calling the static methods directly.

**Use cases:**
- Projects with strict build constraints
- Test scripts and utilities
- Progressive migration
- Plain Java projects without Maven/Gradle

**Direct class import:**

```java
import io.fluentjava.string.FluentString;
import io.fluentjava.list.FluentList;
import io.fluentjava.map.FluentMap;
import io.fluentjava.number.FluentNumber;
import io.fluentjava.date.FluentDate;

// Equivalent to: rawInput.trimToNull().toSlug()
String slug = FluentString.toSlug(FluentString.trimToNull(rawInput));

// Equivalent to: users.filterBy(User::isPremium).mapTo(User::getEmail)
List<String> emails = FluentList.mapTo(
    FluentList.filterBy(users, User::isPremium),
    User::getEmail
);
```

**Static imports (cleaner):**

```java
import static io.fluentjava.string.FluentString.*;
import static io.fluentjava.list.FluentList.*;

String slug   = toSlug(trimToNull(rawInput));
boolean valid = isEmail(email) && isAlphanumeric(username);
```

> The Static API produces **identical bytecode** to the fluent plugin style.
> Both approaches are strictly equivalent at runtime. The only difference is source-level readability.

---

## 🚀 10 Real-World Examples

### 1. REST endpoint — username validation and normalization

```java
// ❌ Before — Java pur
String name = null;
if (rawName != null) {
    name = rawName.trim();
    if (name.isEmpty()) name = null;
}
if (name == null) throw new IllegalArgumentException("Username required");
if (!name.matches("[a-zA-Z0-9]+")) throw new IllegalArgumentException("Alphanumeric only");
if (name.length() < 3 || name.length() > 20) throw new IllegalArgumentException("3-20 chars");
String result = name.substring(0, 1).toUpperCase() + name.substring(1).toLowerCase();

// ✅ After — FluentJava
String name = rawName.trimToNull();
if (name == null) throw new IllegalArgumentException("Username required");
if (!name.isAlphanumeric()) throw new IllegalArgumentException("Alphanumeric only");
if (name.length() < 3 || name.length() > 20) throw new IllegalArgumentException("3-20 chars");
String result = name.capitalized();
```

### 2. Multi-field form parsing

```java
// ❌ Before — Java pur
Integer age = null;
if (rawAge != null) {
    String t = rawAge.trim();
    if (!t.isEmpty()) {
        try { age = Integer.parseInt(t); } catch (NumberFormatException ignored) {}
    }
}
Double price = null;
if (rawPrice != null) {
    String t = rawPrice.trim();
    if (!t.isEmpty()) {
        try { price = Double.parseDouble(t); } catch (NumberFormatException ignored) {}
    }
}
Boolean active = rawActive == null ? null :
    "true".equalsIgnoreCase(rawActive.trim()) ? Boolean.TRUE :
    "false".equalsIgnoreCase(rawActive.trim()) ? Boolean.FALSE : null;

// ✅ After — FluentJava
Integer age    = rawAge.trimToNull().toIntOrNull();
Double  price  = rawPrice.trimToNull().toDoubleOrNull();
Boolean active = rawActive.trimToNull().toBooleanOrNull();
```

### 3. User list transformation pipeline

```java
// ❌ Before — Java Stream
List<String> emails = new ArrayList<>();
if (users != null) {
    for (User u : users) {
        if (u != null && u.isPremium()) {
            String email = u.getEmail();
            if (email != null) {
                String lower = email.toLowerCase().trim();
                if (!lower.isEmpty() && !emails.contains(lower)) {
                    emails.add(lower);
                }
            }
        }
    }
    Collections.sort(emails);
}

// ✅ After — FluentJava
List<String> emails = users
    .filterBy(User::isPremium)
    .mapTo(User::getEmail)
    .filterNotNull()
    .distinctBy(String::toLowerCase)
    .sortedBy(String::toLowerCase);
```

### 4. Dashboard — aggregation and reporting

```java
// ❌ Before — Java Stream
Map<String, List<Order>> byRegion = orders.stream()
    .collect(Collectors.groupingBy(Order::getRegion));
double total = orders.stream()
    .mapToDouble(Order::getAmount).sum();
double avg = orders.stream()
    .mapToDouble(Order::getAmount).average().orElse(0.0);
Order biggest = orders.stream()
    .max(Comparator.comparingDouble(Order::getAmount)).orElse(null);

// ✅ After — FluentJava
Map<String, List<Order>> byRegion = orders.groupBy(Order::getRegion);
double total   = orders.sumOf(Order::getAmount);
double avg     = orders.averageOf(Order::getAmount);
Order  biggest = orders.maxByOrNull(Order::getAmount);
```

### 5. Slug generation and data masking

```java
// ❌ Before — Java pur
String normalized = java.text.Normalizer.normalize(title, java.text.Normalizer.Form.NFD);
String stripped = normalized.replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
String slug = stripped.toLowerCase()
    .replaceAll("[^a-z0-9\\s-]", "")
    .trim().replaceAll("\\s+", "-").replaceAll("-+", "-");
String masked = "*".repeat(Math.max(0, card.length() - 4)) + card.substring(Math.max(0, card.length() - 4));

// ✅ After — FluentJava
String slug   = title.stripAccents().toSlug();
String masked = card.mask(4);
```

### 6. Invoice date logic

```java
// ❌ Before — Java pur
if (dueDate.isBefore(LocalDate.now())) { applyLateFee(invoice); }
if (dueDate.getDayOfWeek() == DayOfWeek.SATURDAY || dueDate.getDayOfWeek() == DayOfWeek.SUNDAY) {
    dueDate = dueDate.plusDays(dueDate.getDayOfWeek() == DayOfWeek.SATURDAY ? 2 : 1);
}
long days = ChronoUnit.DAYS.between(invoiceDate, LocalDate.now());
int yearsOld = Period.between(birthDate, LocalDate.now()).getYears();
String formatted = dueDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));

// ✅ After — FluentJava
if (dueDate.isPast())    { applyLateFee(invoice); }
if (dueDate.isWeekend()) { dueDate = dueDate.nextWeekday(); }
long days    = invoiceDate.daysUntil(LocalDate.now());
int yearsOld = birthDate.yearsUntilNow();
String formatted = dueDate.format("dd/MM/yyyy");
```

### 7. Multi-rule validation with error collection

```java
// ❌ Before — Java pur
List<String> errors = new ArrayList<>();
if (password == null || password.isBlank()) errors.add("Password required");
else {
    if (password.length() < 8) errors.add("Min 8 characters");
    if (!password.matches(".*[A-Z].*")) errors.add("Needs an uppercase letter");
    if (!password.matches(".*\\d.*")) errors.add("Needs a digit");
}
if (!errors.isEmpty()) throw new IllegalArgumentException(String.join(", ", errors));

// ✅ After — FluentJava
FluentValidator.ofString(password)
    .notBlank("Password required")
    .minLength(8, "Min 8 characters")
    .matches(".*[A-Z].*", "Needs an uppercase letter")
    .matches(".*\\d.*", "Needs a digit")
    .throwIfInvalid();
```

### 8. Null-safe object pipeline

```java
// ❌ Before — Java pur
Order saved = order;
if (saved != null) {
    auditService.record(saved);
    notificationService.notify(saved);
}
User active = null;
if (user != null && user.isEnabled()) {
    active = user;
}
if (active == null) {
    active = loadFallback();
}
String city = null;
if (user != null && user.getAddress() != null) {
    city = user.getAddress().getCity();
}
if (city == null) city = "N/A";

// ✅ After — FluentJava
Order saved  = order.also(this::audit).also(this::notify);
User  active = user.takeIf(User::isEnabled).orElseGet(this::loadFallback);
String city  = user.let(u -> u.getAddress() == null ? "N/A" : u.getAddress().getCity());
```

### 9. Configuration map manipulation

```java
// ❌ Before — Java pur
Map<String, String> features = new LinkedHashMap<>();
for (Map.Entry<String, String> e : flags.entrySet()) {
    if (e.getKey().startsWith("feature.") && "true".equals(e.getValue())) {
        features.put(e.getKey(), e.getValue());
    }
}
Map<String, String> inverted = new LinkedHashMap<>();
for (Map.Entry<String, String> e : config.entrySet()) {
    inverted.put(e.getValue(), e.getKey());
}

// ✅ After — FluentJava
Map<String, String> features = flags
    .filterKeys(k -> k.startsWith("feature."))
    .filterValues("true"::equals);
Map<String, String> inverted = config.invertMap();
```

### 10. Financial number logic

```java
// ❌ Before — Java pur
double clamped = Math.min(Math.max(value, 0), 100);
double rounded = Math.round(value * 100.0) / 100.0;
double pct     = total == 0 ? 0 : (part / total) * 100.0;
boolean negative = value < 0;
String ordinal = n + (n % 100 >= 11 && n % 100 <= 13 ? "th" :
    n % 10 == 1 ? "st" : n % 10 == 2 ? "nd" : n % 10 == 3 ? "rd" : "th");

// ✅ After — FluentJava
double  clamped  = value.coerceIn(0, 100);
double  rounded  = value.roundTo(2);
double  pct      = part.percentOf(total);
boolean negative = value.isNegative();
String  ordinal  = n.toOrdinal();
```

---

## 📚 API Reference

### FluentString

**Null safety & defaults** — Null-safe checks and fallback values for strings.

| Method | Description | Example |
|--------|-------------|---------|
| `isBlankSafe(s)` | Returns `true` if null, empty, or whitespace-only | `" ".isBlankSafe()` → `true` |
| `isNullOrBlank(s)` | Alias — checks if null or blank | `str.isNullOrBlank()` |
| `isNullOrEmpty(s)` | Returns `true` if null or zero-length | `"".isNullOrEmpty()` → `true` |
| `orEmpty(s)` | Returns the string or `""` if null | `null.orEmpty()` → `""` |
| `orDefault(s, def)` | Returns the string if not blank, otherwise the default | `null.orDefault("N/A")` → `"N/A"` |
| `trimToNull(s)` | Trims whitespace; returns `null` if result is empty | `"  ".trimToNull()` → `null` |
| `ifBlank(s, def)` | Returns default if string is null or blank | `" ".ifBlank("x")` → `"x"` |
| `ifEmpty(s, def)` | Returns default if string is null or empty | `"".ifEmpty("x")` → `"x"` |

**Parsing** — Safe type conversion without try/catch.

| Method | Description | Example |
|--------|-------------|---------|
| `toIntOrNull(s)` | Parses as integer, returns `null` on failure | `"42".toIntOrNull()` → `42` |
| `toLongOrNull(s)` | Parses as long, returns `null` on failure | `"999".toLongOrNull()` → `999L` |
| `toDoubleOrNull(s)` | Parses as double, returns `null` on failure | `"3.14".toDoubleOrNull()` → `3.14` |
| `toBooleanOrNull(s)` | Parses as boolean, returns `null` if unrecognized | `"true".toBooleanOrNull()` → `true` |
| `toBigDecimalOrNull(s)` | Parses as BigDecimal, returns `null` on failure | `"1.23".toBigDecimalOrNull()` |

**Slicing & padding** — Extract or pad portions of a string.

| Method | Description | Example |
|--------|-------------|---------|
| `take(s, n)` | First n characters | `"hello".take(3)` → `"hel"` |
| `takeLast(s, n)` | Last n characters | `"hello".takeLast(3)` → `"llo"` |
| `drop(s, n)` | Remove first n characters | `"hello".drop(2)` → `"llo"` |
| `dropLast(s, n)` | Remove last n characters | `"hello".dropLast(2)` → `"hel"` |
| `left(s, n)` | Leftmost n characters | `"hello".left(3)` → `"hel"` |
| `right(s, n)` | Rightmost n characters | `"hello".right(3)` → `"llo"` |
| `padStart(s, len, ch)` | Pad at start to reach length | `"42".padStart(5, '0')` → `"00042"` |
| `padEnd(s, len, ch)` | Pad at end to reach length | `"hi".padEnd(5, '.')` → `"hi..."` |
| `center(s, size, ch)` | Center in a field with padding | `"hi".center(6, '-')` → `"--hi--"` |
| `truncate(s, max, sfx)` | Truncate with suffix if too long | `"hello world".truncate(8, "...")` → `"hello..."` |
| `ellipsize(s, max)` | Truncate with ellipsis `…` | `"long text".ellipsize(6)` → `"long t…"` |

**Transformation** — String manipulation and formatting.

| Method | Description | Example |
|--------|-------------|---------|
| `reversed(s)` | Reverse the string | `"hello".reversed()` → `"olleh"` |
| `capitalized(s)` | First char uppercase, rest lowercase | `"hello".capitalized()` → `"Hello"` |
| `decapitalized(s)` | First char lowercase | `"Hello".decapitalized()` → `"hello"` |
| `removePrefix(s, pfx)` | Remove prefix if present | `"prefix-val".removePrefix("prefix-")` → `"val"` |
| `removeSuffix(s, sfx)` | Remove suffix if present | `"file.txt".removeSuffix(".txt")` → `"file"` |
| `wrap(s, w)` | Wrap with string on both sides | `"hi".wrap("*")` → `"*hi*"` |
| `unwrap(s, w)` | Remove wrapping string from both sides | `"*hi*".unwrap("*")` → `"hi"` |
| `repeat(s, n)` | Repeat n times | `"ab".repeat(3)` → `"ababab"` |
| `normalizeWhitespace(s)` | Collapse whitespace and trim | `" a  b ".normalizeWhitespace()` → `"a b"` |
| `stripAccents(s)` | Remove diacritical marks | `"café".stripAccents()` → `"cafe"` |
| `toInitials(s)` | Extract first letter of each word | `"Jean Marc".toInitials()` → `"JM"` |
| `redact(s)` | Partially hide sensitive data | `"secret123".redact()` → `"s***3123"` |
| `toCurrency(s, code)` | Format as currency amount | `"42.5".toCurrency("EUR")` → `"42,50 €"` |
| `toBase64(s)` | Encode to Base64 | `"hello".toBase64()` |
| `fromBase64(s)` | Decode from Base64 | `"aGVsbG8=".fromBase64()` → `"hello"` |
| `digest(s, algo)` | Cryptographic hash | `"data".digest("SHA-256")` |

**Case conversion** — Convert between naming conventions.

| Method | Description | Example |
|--------|-------------|---------|
| `toCamelCase(s)` | Convert to camelCase | `"hello world".toCamelCase()` → `"helloWorld"` |
| `toSnakeCase(s)` | Convert to snake_case | `"helloWorld".toSnakeCase()` → `"hello_world"` |
| `toPascalCase(s)` | Convert to PascalCase | `"hello world".toPascalCase()` → `"HelloWorld"` |
| `toKebabCase(s)` | Convert to kebab-case | `"helloWorld".toKebabCase()` → `"hello-world"` |
| `toSlug(s)` | URL-safe slug | `"Café Latte!".toSlug()` → `"cafe-latte"` |

**Search & matching** — Find and test patterns in strings.

| Method | Description | Example |
|--------|-------------|---------|
| `containsIgnoreCase(s, sub)` | Case-insensitive contains | `"Hello".containsIgnoreCase("hello")` → `true` |
| `startsWithIgnoreCase(s, pfx)` | Case-insensitive startsWith | `"Hello".startsWithIgnoreCase("he")` → `true` |
| `endsWithIgnoreCase(s, sfx)` | Case-insensitive endsWith | `"Hello".endsWithIgnoreCase("LO")` → `true` |
| `countOccurrences(s, sub)` | Count substring appearances | `"abab".countOccurrences("ab")` → `2` |
| `matchesPattern(s, regex)` | Test regex match (null-safe) | `"abc".matchesPattern("[a-z]+")` → `true` |
| `countWords(s)` | Count whitespace-separated words | `"hello world".countWords()` → `2` |

**Splitting** — Split strings into lists.

| Method | Description | Example |
|--------|-------------|---------|
| `splitToList(s, delim)` | Split by delimiter, returns `List<String>` | `"a,b,c".splitToList(",")` → `["a","b","c"]` |
| `lines(s)` | Split by line separator, returns `List<String>` | `"a\nb".lines()` → `["a","b"]` |

**Validation** — Check string format.

| Method | Description | Example |
|--------|-------------|---------|
| `isNumeric(s)` | Contains only digits | `"123".isNumeric()` → `true` |
| `isAlpha(s)` | Contains only letters | `"abc".isAlpha()` → `true` |
| `isAlphanumeric(s)` | Letters or digits only | `"abc123".isAlphanumeric()` → `true` |
| `isEmail(s)` | Valid email format | `"a@b.com".isEmail()` → `true` |
| `isUrl(s)` | Valid HTTP(S) URL | `"https://x.com".isUrl()` → `true` |
| `isIPv4(s)` | Valid IPv4 address | `"192.168.1.1".isIPv4()` → `true` |

**Security & display** — Mask sensitive data.

| Method | Description | Example |
|--------|-------------|---------|
| `mask(s, visible)` | Mask all but last N chars | `"1234567890".mask(4)` → `"******7890"` |

---

### FluentList

**Null safety** — Safe handling of null or empty lists.

| Method | Description | Example |
|--------|-------------|---------|
| `isNullOrEmpty(list)` | Returns `true` if null or empty | `null.isNullOrEmpty()` → `true` |
| `orEmpty(list)` | Returns empty list if null | `null.orEmpty()` → `[]` |

**Element access** — Safe element retrieval without exceptions.

| Method | Description | Example |
|--------|-------------|---------|
| `firstOrNull(list)` | First element or `null` | `users.firstOrNull()` |
| `firstOrNull(list, pred)` | First matching element or `null` | `users.firstOrNull(User::isActive)` |
| `lastOrNull(list)` | Last element or `null` | `users.lastOrNull()` |
| `lastOrNull(list, pred)` | Last matching element or `null` | `users.lastOrNull(User::isAdmin)` |
| `second(list)` | Second element or `null` | `list.second()` |
| `third(list)` | Third element or `null` | `list.third()` |
| `getOrNull(list, i)` | Element at index or `null` | `list.getOrNull(5)` |
| `getOrDefault(list, i, def)` | Element at index or default | `list.getOrDefault(5, fallback)` |
| `randomOrNull(list)` | Random element or `null` | `list.randomOrNull()` |

**Filtering & mapping** — Transform and filter list elements.

| Method | Description | Example |
|--------|-------------|---------|
| `filterBy(list, pred)` | Filter by predicate | `users.filterBy(User::isActive)` |
| `mapTo(list, fn)` | Map each element | `users.mapTo(User::getName)` |
| `flatMap(list, fn)` | FlatMap elements | `groups.flatMap(Group::getMembers)` |
| `mapNotNull(list, fn)` | Map and exclude nulls | `list.mapNotNull(this::tryParse)` |
| `filterNotNull(list)` | Remove null elements | `list.filterNotNull()` |
| `forEachIndexed(list, action)` | Iterate with index | `list.forEachIndexed((i, e) -> ...)` |

**Sorting & deduplication** — Order and deduplicate.

| Method | Description | Example |
|--------|-------------|---------|
| `sortedBy(list, fn)` | Sort ascending by key | `users.sortedBy(User::getName)` |
| `sortedByDescending(list, fn)` | Sort descending by key | `orders.sortedByDescending(Order::getDate)` |
| `distinctBy(list, fn)` | Deduplicate by key | `emails.distinctBy(String::toLowerCase)` |
| `shuffled(list)` | New shuffled copy | `list.shuffled()` |

**Grouping & partitioning** — Group elements.

| Method | Description | Example |
|--------|-------------|---------|
| `groupBy(list, fn)` | Group by key function | `orders.groupBy(Order::getRegion)` |
| `associateBy(list, fn)` | Key → element map | `users.associateBy(User::getId)` |
| `associate(list, kFn, vFn)` | Key → value map | `list.associate(x -> x.id, x -> x.name)` |
| `partition(list, pred)` | Split into accepted/rejected | `users.partition(User::isActive)` |
| `frequencies(list)` | Element → occurrence count | `words.frequencies()` |

**Aggregation** — Compute summaries. 

| Method | Description | Example |
|--------|-------------|---------|
| `sumOf(list, fn)` | Sum as double | `orders.sumOf(Order::getAmount)` |
| `sumOfInt(list, fn)` | Sum as int | `items.sumOfInt(Item::getQty)` |
| `sumOfLong(list, fn)` | Sum as long | `records.sumOfLong(Record::getId)` |
| `averageOf(list, fn)` | Average as double | `scores.averageOf(Score::getValue)` |
| `maxByOrNull(list, fn)` | Element with max key | `orders.maxByOrNull(Order::getTotal)` |
| `minByOrNull(list, fn)` | Element with min key | `prices.minByOrNull(Price::getAmount)` |
| `countBy(list, pred)` | Count matching elements | `orders.countBy(Order::isPaid)` |

**Predicates** — Boolean tests on list contents.

| Method | Description | Example |
|--------|-------------|---------|
| `any(list, pred)` | At least one match | `users.any(User::isAdmin)` |
| `all(list, pred)` | All elements match | `users.all(User::isVerified)` |
| `none(list, pred)` | No element matches | `users.none(User::isBanned)` |

**Windowing & slicing** — Extract sublists and windows.

| Method | Description | Example |
|--------|-------------|---------|
| `chunked(list, size)` | Split into sublists of size | `items.chunked(10)` |
| `windowed(list, size)` | Sliding windows | `data.windowed(3)` |
| `paginate(list, page, size)` | Page of results (1-based) | `items.paginate(2, 20)` |
| `take(list, n) / takeWhile` | Take first n / while pred | `list.takeWhile(x -> x > 0)` |
| `drop(list, n) / dropWhile` | Drop first n / while pred | `list.dropWhile(x -> x < 0)` |
| `sample(list, n)` | Random sample of n elements | `users.sample(5)` |

**Set operations** — Combine lists.

| Method | Description | Example |
|--------|-------------|---------|
| `intersect(list, other)` | Common elements | `a.intersect(b)` |
| `subtract(list, other)` | Elements in a but not b | `a.subtract(b)` |
| `union(list, other)` | Union without duplicates | `a.union(b)` |
| `zip(list, other)` | Pair elements from two lists | `names.zip(ages)` |
| `flatten(list)` | Flatten nested lists | `nested.flatten()` |

**Conversion** — Convert to other types.

| Method | Description | Example |
|--------|-------------|---------|
| `toSet(list)` | Convert to ordered Set | `list.toSet()` |
| `toCsv(list)` | Join with commas | `tags.toCsv()` → `"a, b, c"` |
| `indexOfFirst(list, pred)` | Index of first match (-1 if none) | `list.indexOfFirst(x -> x > 5)` |
| `indexOfLast(list, pred)` | Index of last match (-1 if none) | `list.indexOfLast(x -> x > 5)` |

---

### FluentMap

| Method | Description | Example |
|--------|-------------|---------|
| `isNullOrEmpty(map)` | Null or empty check | `map.isNullOrEmpty()` |
| `orEmpty(map)` | Returns empty map if null | `null.orEmpty()` |
| `getOrNull(map, key)` | Value or `null` | `map.getOrNull("key")` |
| `getOrEmpty(map, key)` | Value or `""` | `map.getOrEmpty("locale")` |
| `getOrPut(map, key, sup)` | Get or compute and insert | `map.getOrPut(k, () -> new V())` |
| `filterKeys(map, pred)` | Filter entries by key | `map.filterKeys(k -> k.startsWith("x"))` |
| `filterValues(map, pred)` | Filter entries by value | `map.filterValues(v -> v > 0)` |
| `filterByValue(map, pred)` | Alias for filterValues | `map.filterByValue(Objects::nonNull)` |
| `mapValues(map, fn)` | Transform all values | `map.mapValues(String::toUpperCase)` |
| `mapKeys(map, fn)` | Transform all keys | `map.mapKeys(String::toLowerCase)` |
| `flatMapValues(map, fn)` | FlatMap each value to a list | `map.flatMapValues(v -> v.items())` |
| `merge(map, other)` | Merge two maps (second wins) | `a.merge(b)` |
| `invertMap(map)` | Swap keys ↔ values | `map.invertMap()` |
| `toSortedMap(map)` | Convert to TreeMap (sorted keys) | `map.toSortedMap()` |
| `toList(map)` | Entries as a list | `map.toList()` |
| `entries(map)` | Entry set as unmodifiable list | `map.entries()` |
| `forEach(map, action)` | Iterate entries (null-safe) | `map.forEach((k, v) -> ...)` |
| `containsAllKeys(map, k...)` | All keys present | `map.containsAllKeys("a", "b")` |
| `any(map, pred)` | Any entry matches | `map.any(e -> e.getValue() > 0)` |
| `all(map, pred)` | All entries match | `map.all(e -> e.getValue() != null)` |
| `none(map, pred)` | No entry matches | `map.none(e -> e.getKey().isEmpty())` |
| `count(map, pred)` | Count matching entries | `map.count(e -> e.getValue() > 0)` |

---

### FluentNumber

| Method | Description | Example |
|--------|-------------|---------|
| `coerceIn(n, min, max)` | Clamp value in `[min, max]` | `150.coerceIn(0, 100)` → `100.0` |
| `coerceAtLeast(n, min)` | Ensure minimum value | `(-5).coerceAtLeast(0)` → `0.0` |
| `coerceAtMost(n, max)` | Ensure maximum value | `150.coerceAtMost(100)` → `100.0` |
| `clamp(n, min, max)` | Clamp value (Number params) | `n.clamp(0, 100)` |
| `isBetween(n, min, max)` | Inclusive range check | `50.isBetween(0, 100)` → `true` |
| `isPositive(n)` | Greater than zero | `42.isPositive()` → `true` |
| `isNegative(n)` | Less than zero | `(-1).isNegative()` → `true` |
| `isZero(n)` | Equals zero | `0.isZero()` → `true` |
| `isEven(n)` | Divisible by 2 | `4.isEven()` → `true` |
| `isOdd(n)` | Not divisible by 2 | `3.isOdd()` → `true` |
| `isPrime(n)` | Prime number test | `7.isPrime()` → `true` |
| `roundTo(n, dec)` | Round to decimals (HALF_UP) | `3.456.roundTo(2)` → `3.46` |
| `percentOf(part, total)` | Percentage calculation | `25.percentOf(200)` → `12.5` |
| `toOrdinal(n)` | English ordinal | `3.toOrdinal()` → `"3rd"` |
| `toPercentString(n, dec)` | Format as percentage string | `0.85.toPercentString(1)` → `"85.0%"` |
| `factorial(n)` | Compute n! | `5.factorial()` → `120` |
| `digits(n)` | Extract digit list | `123.digits()` → `[1, 2, 3]` |
| `toBinary(n)` | Binary representation | `10.toBinary()` → `"1010"` |
| `toHex(n)` | Hexadecimal representation | `255.toHex()` → `"ff"` |
| `toOctal(n)` | Octal representation | `8.toOctal()` → `"10"` |

---

### FluentDate

**LocalDate** — Date predicates and calculations.

| Method | Description | Example |
|--------|-------------|---------|
| `isWeekend(date)` | Saturday or Sunday | `date.isWeekend()` |
| `isWeekday(date)` | Monday through Friday | `date.isWeekday()` |
| `isBusinessDay(date)` | Monday through Friday (alias) | `date.isBusinessDay()` |
| `isToday(date)` | Equals today's date | `date.isToday()` |
| `isPast(date)` | Before today | `date.isPast()` |
| `isFuture(date)` | After today | `date.isFuture()` |
| `isLeapYear(date)` | Year is a leap year | `date.isLeapYear()` |
| `isBefore(date, other)` | Strictly before another date | `date.isBefore(deadline)` |
| `isAfter(date, other)` | Strictly after another date | `date.isAfter(start)` |
| `isBetween(date, from, to)` | Inside inclusive range | `date.isBetween(start, end)` |
| `daysUntil(from, to)` | Days between two dates | `start.daysUntil(end)` |
| `monthsUntil(from, to)` | Months between two dates | `start.monthsUntil(end)` |
| `yearsUntilNow(date)` | Years from date to today | `birthDate.yearsUntilNow()` |
| `age(date)` | Age in years from today | `birthDate.age()` |
| `format(date, pattern)` | Format with pattern | `date.format("dd/MM/yyyy")` |
| `atStartOfDay(date)` | DateTime at midnight | `date.atStartOfDay()` |
| `atEndOfDay(date)` | DateTime at 23:59:59.999 | `date.atEndOfDay()` |
| `startOfWeek(date)` | Monday of current week | `date.startOfWeek()` |
| `endOfWeek(date)` | Sunday of current week | `date.endOfWeek()` |
| `startOfMonth(date)` | First day of month | `date.startOfMonth()` |
| `endOfMonth(date)` | Last day of month | `date.endOfMonth()` |
| `startOfYear(date)` | January 1st | `date.startOfYear()` |
| `endOfYear(date)` | December 31st | `date.endOfYear()` |
| `nextWeekday(date)` | Next Monday–Friday | `date.nextWeekday()` |
| `quarterOf(date)` | Quarter 1–4 | `date.quarterOf()` |
| `weekOfYear(date)` | ISO week number | `date.weekOfYear()` |
| `toLocalDateTime(date)` | Convert to start-of-day DateTime | `date.toLocalDateTime()` |

**LocalDateTime** — DateTime operations.

| Method | Description | Example |
|--------|-------------|---------|
| `format(dt, pattern)` | Format with pattern | `dt.format("yyyy-MM-dd HH:mm")` |
| `toEpochMillis(dt)` | Convert to epoch millis | `dt.toEpochMillis()` |
| `isBefore(dt, other)` | Strictly before another DateTime | `dt.isBefore(deadline)` |
| `isAfter(dt, other)` | Strictly after another DateTime | `dt.isAfter(start)` |
| `isBetween(dt, from, to)` | Inside inclusive range | `dt.isBetween(from, to)` |
| `toLocalDate(dt)` | Extract date part | `dt.toLocalDate()` |

**Long → DateTime** — Epoch conversion.

| Method | Description | Example |
|--------|-------------|---------|
| `fromEpochMillis(millis)` | Long → LocalDateTime | `millis.fromEpochMillis()` |

**String → Date** — Parse date strings.

| Method | Description | Example |
|--------|-------------|---------|
| `toLocalDate(s, pattern)` | Parse string → LocalDate | `"01/01/2024".toLocalDate("dd/MM/yyyy")` |
| `toLocalDateTime(s, pattern)` | Parse string → LocalDateTime | `"01/01/2024 10:00".toLocalDateTime("dd/MM/yyyy HH:mm")` |

---

### FluentOptional

| Method | Description | Example |
|--------|-------------|---------|
| `toOptional(value)` | Wrap in Optional (null-safe) | `value.toOptional()` |
| `orNull(opt)` | Value or `null` | `opt.orNull()` |
| `orEmpty(opt)` | String value or `""` | `opt.orEmpty()` |
| `ifPresent(opt, action)` | Execute if present | `opt.ifPresent(v -> log(v))` |
| `mapTo(opt, fn)` | Transform if present | `opt.mapTo(String::toUpperCase)` |
| `filterBy(opt, pred)` | Filter by predicate | `opt.filterBy(s -> !s.isEmpty())` |
| `isPresent(opt)` | Has a value | `opt.isPresent()` |
| `isEmpty(opt)` | Has no value | `opt.isEmpty()` |
| `orElseThrow(opt, msg)` | Value or throw exception | `opt.orElseThrow("Not found")` |

---

### FluentPath

| Method | Description | Example |
|--------|-------------|---------|
| `readText(path)` | Read file as UTF-8 string | `path.readText()` |
| `writeText(path, content)` | Write UTF-8 text to file | `path.writeText("data")` |
| `readLines(path)` | Read all lines | `path.readLines()` |
| `exists(path)` | File exists check | `path.exists()` |
| `extension(path)` | File extension without dot | `path.extension()` → `"txt"` |
| `nameWithoutExtension(path)` | Filename without extension | `path.nameWithoutExtension()` → `"data"` |
| `fileName(path)` | File name string | `path.fileName()` → `"data.txt"` |
| `copyTo(path, target)` | Copy file | `src.copyTo(dest)` |
| `moveTo(path, target)` | Move file | `src.moveTo(dest)` |
| `deleteIfExists(path)` | Delete if exists | `path.deleteIfExists()` |
| `sizeInBytes(path)` | File size in bytes | `path.sizeInBytes()` |
| `isDirectory(path)` | Check if directory | `path.isDirectory()` |
| `listFiles(path)` | List directory contents | `dir.listFiles()` |
| `createDirectories(path)` | Create directories recursively | `path.createDirectories()` |

---

### FluentObject

| Method | Description | Example |
|--------|-------------|---------|
| `isNull(obj)` | Null check | `value.isNull()` |
| `isNotNull(obj)` | Not-null check | `value.isNotNull()` |
| `orElse(obj, fallback)` | Value or fallback if null | `name.orElse("default")` |
| `orElseGet(obj, supplier)` | Value or computed fallback | `user.orElseGet(this::loadDefault)` |
| `takeIf(obj, pred)` | Value if predicate passes, else `null` | `user.takeIf(User::isActive)` |
| `takeUnless(obj, pred)` | Value if predicate fails, else `null` | `user.takeUnless(User::isBanned)` |
| `let(obj, fn)` | Inline transform | `user.let(User::getName)` |
| `also(obj, action)` | Side-effect and return self | `order.also(this::audit)` |
| `requireNotNull(obj, msg)` | Throw if null | `user.requireNotNull("User required")` |
| `requireThat(cond, msg)` | Throw if condition false | `requireThat(age > 0, "Invalid")` |

---

### FluentValidator

| Method | Description | Example |
|--------|-------------|---------|
| `of(value)` | Create validator for any value | `FluentValidator.of(age)` |
| `ofString(value)` | Create string validator | `FluentValidator.ofString(email)` |
| `notNull(msg)` | Value must not be null | `.notNull("Required")` |
| `notBlank(msg)` | String must not be blank | `.notBlank("Required")` |
| `notEmpty(msg)` | Collection must not be empty | `.notEmpty("At least one")` |
| `satisfies(pred, msg)` | Custom predicate | `.satisfies(x -> x > 0, "Positive")` |
| `minLength(n, msg)` | Minimum string length | `.minLength(8, "Too short")` |
| `maxLength(n, msg)` | Maximum string length | `.maxLength(100, "Too long")` |
| `matches(regex, msg)` | Must match regex | `.matches("[A-Z]+", "Uppercase only")` |
| `min(n, msg)` | Minimum numeric value | `.min(18, "Must be 18+")` |
| `max(n, msg)` | Maximum numeric value | `.max(120, "Too high")` |
| `positive(msg)` | Must be positive | `.positive("Must be positive")` |
| `validate()` | Collect errors as `List<String>` | `.validate()` |
| `isValid()` | Returns `true` if no errors | `.isValid()` |
| `firstError()` | First error or `null` | `.firstError()` |
| `throwIfInvalid()` | Throws if validation fails | `.throwIfInvalid()` |
| `throwIfInvalid(exFn)` | Throws custom exception | `.throwIfInvalid(msgs -> new MyEx(msgs))` |

---

## 🔧 Extending FluentJava

Adding a new method requires only **2 files** — everything else is automatic.

### Step 1 — Add the static method in the runtime

```java
// In FluentString.java (or any FluentXxx class)

/**
 * Returns the string repeated with a separator between each repetition.
 *
 * @param s         the source string (may be null)
 * @param count     number of repetitions
 * @param separator the separator between repetitions
 * @return the repeated string with separators, or null if s is null
 */
public static String repeatWithSeparator(String s, int count, String separator) {
    if (s == null) return null;
    return String.join(separator, Collections.nCopies(count, s));
}
```

### Step 2 — Add the entry in FluentMethodRegistry

```java
method("repeatWithSeparator", TargetType.STRING)
    .param("count", "int")
    .param("separator", "java.lang.String")
    .returns("java.lang.String")
    .doc("Repeats string with separator.", "\"ab\".repeatWithSeparator(3, \"-\") → \"ab-ab-ab\"")
    .build(),
```

### What updates automatically

| Component | How it updates |
|-----------|----------------|
| javac plugin | Auto-discovery via reflection — zero changes |
| IntelliJ autocompletion | Reads registry — zero changes |
| Go To Declaration | Reads registry — zero changes |
| Inline documentation | Reads registry — zero changes |
| Zero red errors | PsiAugmentProvider injects the method into PSI |

### Adding a method with generics

```java
method("mapTo", TargetType.LIST)
    .typeParam("R")
    .param("mapper", "java.util.function.Function", "E", "R")
    .returns("java.util.List", "R")
    .doc("Maps each element.", "names.mapTo(String::length)")
    .build(),
```

### Adding a new target type

1. Add a `TargetType` in the enum:
   ```java
   STREAM("java.util.stream.Stream", "io.fluentjava.stream.FluentStream"),
   ```
2. Add methods in `ALL_METHODS`
3. Create `FluentStream.java` in `fluentjava-runtime`
4. Add to `META-INF/fluentjava/targets`

---

## 🏗️ Technical Details

### Project structure

```
fluentjava/
├── fluentjava-runtime/           ← Static utility classes, shipped to production
│   └── io/fluentjava/
│       ├── string/FluentString.java      (60 methods)
│       ├── list/FluentList.java          (51 methods)
│       ├── map/FluentMap.java            (22 methods)
│       ├── number/FluentNumber.java      (20 methods)
│       ├── date/FluentDate.java          (34 methods)
│       ├── optional/FluentOptional.java  (9 methods)
│       ├── path/FluentPath.java          (14 methods)
│       ├── object/FluentObject.java      (10 methods)
│       └── validator/FluentValidator.java
│
├── fluentjava-plugin/            ← javac plugin, provided scope (never in prod)
│   └── io/fluentjava/plugin/
│       ├── FluentJavaPlugin.java          implements Plugin
│       ├── FluentTaskListener.java        implements TaskListener
│       ├── FluentImportInjector.java      injects import static declarations
│       └── FluentMethodRewriter.java      rewrites AST method calls
│
├── fluentjava-maven-plugin/      ← Maven plugin (hides compiler config)
│   └── io/fluentjava/maven/FluentJavaMojo.java
│
└── fluentjava-gradle-plugin/     ← Gradle plugin (hides compiler config)
    └── io/fluentjava/gradle/FluentJavaPlugin.java

fluentjava-intellij-plugin/       ← IntelliJ IDEA plugin (JetBrains Marketplace)
├── FluentMethodRegistry.java           single source of truth
├── FluentJavaPsiAugmentProvider.java   PSI injection (Lombok technique)
├── FluentJavaCompletionContributor.java autocompletion
├── FluentJavaGotoDeclarationHandler.java Ctrl+B navigation
└── FluentJavaDocumentationProvider.java  Ctrl+Q documentation
```

### How the javac plugin works

1. Plugin registered in `META-INF/services/com.sun.source.util.Plugin`
2. `FluentJavaPlugin.init()` called by javac at startup
3. `FluentTargetRegistry` reads `META-INF/fluentjava/targets` and discovers Fluent classes by reflection
4. `FluentMethodRewriter` registers a `TaskListener` triggered after each file is parsed
5. For each `MethodCallExpr`, if the method name matches a known method → rewrite to static call
6. `FluentImportInjector` injects the required `import static` declarations
7. javac compiles the transformed AST as standard Java

### How the IntelliJ plugin works

1. `FluentMethodRegistry` — single source of truth for all 222+ methods
2. `FluentJavaPsiAugmentProvider` — injects `LightMethodBuilder` into the PSI tree of String, List, Map, etc.
3. IntelliJ treats these virtual methods as real — no red underlines
4. `FluentJavaCompletionContributor` — feeds the autocompletion popup from the registry
5. `FluentJavaGotoDeclarationHandler` — resolves `Ctrl+B` to the FluentXxx class
6. `FluentJavaDocumentationProvider` — generates hover docs from the registry

### Auto-discovery (zero hardcoded lists)

```
META-INF/fluentjava/targets
        │
        ▼
FluentTargetRegistry (reflection at compile-time)
        │
        ├── FluentMethodRewriter (knows which methods to rewrite)
        └── FluentImportInjector (knows which imports to inject)
```

### Security guarantees

| Property | Guarantee |
|----------|-----------|
| Runtime agents | Zero — no `-javaagent` |
| Runtime reflection | Zero — reflection only at compile-time |
| `--add-opens` at runtime | Zero |
| Production classpath | Only `fluentjava-runtime.jar` |
| Plugin JAR | `scope:provided` — never deployed |
| Bytecode | Standard `invokestatic` — decompiler shows normal static calls |
| External dependencies | Zero |

### What a decompiler shows

```java
// Source (with FluentJava)
String slug = rawInput.trimToNull().stripAccents().toSlug();

// Decompiled .class (javap / IntelliJ decompiler)
String slug = FluentString.toSlug(
    FluentString.stripAccents(
        FluentString.trimToNull(rawInput)
    )
);
```

Identical to hand-written static calls. FluentJava does not exist in the bytecode.

---

## ❓ FAQ

**Is this like Lombok?**
Similar concept (compile-time transformation, `PsiAugmentProvider` for IDE support), different mechanism. Lombok uses annotation processing; FluentJava uses the javac Plugin API. Zero annotations required.

**What if I decompile the bytecode?**
You see `FluentString.toSlug(str)` — normal static calls. FluentJava does not exist in the bytecode.

**Does it conflict with existing Java methods?**
No. Methods that would shadow Java standard API use distinct names (`splitToList` instead of `split`, `matchesPattern` instead of `matches`). The plugin only rewrites methods registered in its method list.

**Does it work with Java records, sealed classes, Java 21+ features?**
Yes. FluentJava only touches `MethodCallExpr` nodes in the AST. It does not modify class declarations, records, or sealed types.

**Does it work with Spring Boot, Quarkus, Micronaut?**
Yes. FluentJava operates entirely at compile-time. It is framework-agnostic.

**What happens if I remove FluentJava from a project?**
The source code no longer compiles (fluent calls are not resolved). Migration is straightforward: `str.toSlug()` → `FluentString.toSlug(str)`.

**Can I add my own extension methods?**
Yes — 2 files to modify, ~10 lines of code. See [Extending FluentJava](#-extending-fluentjava).

**Is the runtime JAR production-safe?**
Yes. Pure static methods, zero dependencies, zero reflection, zero agents.

---

## License

Apache License, Version 2.0
