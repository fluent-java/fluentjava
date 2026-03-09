# FluentJava

Fluent extension methods for Java, resolved at compile time.

Write:

```java
String slug = title.trimToNull().toSlug();
```

FluentJava rewrites it during compilation to static calls like:

```java
String slug = io.fluentjava.string.FluentString.toSlug(
    io.fluentjava.string.FluentString.trimToNull(title)
);
```

No runtime agent, no reflection overhead in production.

## Modules

- `fluentjava-runtime`: runtime static utility classes + `@FluentExtension` annotation.
- `fluentjava-plugin`: javac plugin that rewrites fluent calls.
- `fluentjava-maven-plugin`: auto-configures Maven compiler (recommended for users).

## Quick Start (Maven)

Add the runtime dependency and FluentJava Maven plugin with `<extensions>true</extensions>`:

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

That's it. Zero `--add-exports`. Zero `maven-compiler-plugin`. Zero `fluentjava-plugin` dependency.
The FluentJava Maven plugin handles everything automatically:

```
[FluentJava] Injecting compiler args...
[FluentJava] Adding dependency: fluentjava-plugin (provided)
[FluentJava] Configured: fork=true
[FluentJava] Configured: -Xplugin:FluentJava
[FluentJava] Configured: --add-exports (4 exports)
[FluentJava] Done — FluentJava is ready.
```

### How the Maven plugin works

When declared with `<extensions>true</extensions>`, the plugin registers a
`AbstractMavenLifecycleParticipant` that runs **before** lifecycle computation:

1. Adds `fluentjava-plugin` as a `provided` dependency (so javac can find `-Xplugin:FluentJava`).
2. Configures `maven-compiler-plugin` with `fork=true`, `-Xplugin:FluentJava`, and the four `--add-exports` flags.
3. Auto-binds the `configure` goal to the `initialize` phase for `@FluentExtension` scanning.

## Dynamic Project Extensions

FluentJava supports user-defined extension methods via `@FluentExtension`.

### 1. Create an extension class

```java
package com.acme.extensions;

import io.fluentjava.annotation.FluentExtension;

@FluentExtension(target = String.class)
public final class StringExtensions {

    private StringExtensions() {
    }

    public static String bracketize(String value, String left, String right) {
        if (value == null) {
            return null;
        }
        return left + value + right;
    }
}
```

Rules:
- The class must be annotated with `@FluentExtension(target = <TargetType>.class)`.
- Methods must be `public static`.
- The first parameter is the receiver (same type as `target`).
- Additional parameters become the method's arguments.

### 2. Use it directly in Java code

```java
String out = "FluentJava".bracketize("[", "]");
// → [FluentJava]
```

### 3. What you get

**IDE support** (with FluentJava IntelliJ plugin):
- Autocompletion on your extension methods.
- No red squiggles — methods appear as real methods on the target type.
- `Ctrl+B` navigation to the extension method source.
- `Ctrl+Q` documentation integration.

**Compile-time transformation**:
- `fluentjava-maven-plugin` scans sources and generates `target/classes/META-INF/fluentjava/extensions`.
- The javac plugin rewrites `"x".bracketize("[", "]")` → `StringExtensions.bracketize("x", "[", "]")`.

## Built-in Methods

### Object (`FluentObject`)

`also`, `let`, `takeIf`, `takeUnless`, `isNull`, `isNotNull`, `orElse`, `orElseGet`, `requireNotNull`, `requireThat`

### String (`FluentString`)

`isBlankSafe`, `isNullOrBlank`, `isNullOrEmpty`, `orEmpty`, `orDefault`, `trimToNull`, `toIntOrNull`, `toLongOrNull`, `toDoubleOrNull`, `toBooleanOrNull`, `toBigDecimalOrNull`, `take`, `takeLast`, `drop`, `dropLast`, `padStart`, `padEnd`, `reversed`, `capitalized`, `decapitalized`, `truncate`, `removePrefix`, `removeSuffix`, `wrap`, `unwrap`, `repeat`, `containsIgnoreCase`, `startsWithIgnoreCase`, `endsWithIgnoreCase`, `countOccurrences`, `isNumeric`, `isAlpha`, `isAlphanumeric`, `isEmail`, `toSlug`, `mask`, `toCamelCase`, `toSnakeCase`, `left`, `right`, `center`, `normalizeWhitespace`, `stripAccents`, `ifBlank`, `ifEmpty`, `splitToList`, `lines`, `toBase64`, `fromBase64`, `ellipsize`, `toPascalCase`, `toKebabCase`, `matchesPattern`, `digest`, `toInitials`, `countWords`, `isUrl`, `isIPv4`, `redact`, `toCurrency`

### List (`FluentList`)

`isNullOrEmpty`, `orEmpty`, `firstOrNull`, `firstOrNull(predicate)`, `lastOrNull`, `lastOrNull(predicate)`, `getOrNull`, `getOrDefault`, `filterBy`, `mapTo`, `flatMap`, `mapNotNull`, `filterNotNull`, `distinctBy`, `sortedBy`, `sortedByDescending`, `groupBy`, `associateBy`, `partition`, `chunked`, `sumOf`, `maxByOrNull`, `minByOrNull`, `averageOf`, `countBy`, `none`, `any`, `all`, `takeWhile`, `dropWhile`, `paginate`, `zip`, `intersect`, `subtract`, `union`, `second`, `third`, `toSet`, `associate`, `indexOfFirst`, `indexOfLast`, `flatten`, `randomOrNull`, `forEachIndexed`, `toCsv`, `frequencies`, `shuffled`, `sample`, `windowed`, `sumOfInt`, `sumOfLong`

### Map (`FluentMap`)

`isNullOrEmpty`, `orEmpty`, `getOrEmpty`, `getOrNull`, `filterKeys`, `filterValues`, `mapValues`, `mapKeys`, `any`, `all`, `none`, `count`, `toList`, `merge`, `invertMap`, `getOrPut`, `forEach`, `toSortedMap`, `containsAllKeys`, `flatMapValues`, `entries`, `filterByValue`

### Number (`FluentNumber`)

`coerceIn`, `coerceAtLeast`, `coerceAtMost`, `isBetween`, `isPositive`, `isNegative`, `isZero`, `roundTo`, `percentOf`, `isEven`, `isOdd`, `toOrdinal`, `toPercentString`, `clamp`, `isPrime`, `factorial`, `digits`, `toBinary`, `toHex`, `toOctal`

### Date (`FluentDate`)

`isWeekend`, `isWeekday`, `isToday`, `isPast`, `isFuture`, `isLeapYear`, `daysUntil`, `monthsUntil`, `yearsUntilNow`, `format`, `atStartOfDay`, `atEndOfDay`, `toEpochMillis`, `fromEpochMillis`, `isBefore`, `isAfter`, `isBetween`, `startOfWeek`, `endOfWeek`, `startOfMonth`, `endOfMonth`, `startOfYear`, `endOfYear`, `nextWeekday`, `age`, `isBusinessDay`, `toLocalDate`, `toLocalDateTime`, `quarterOf`, `weekOfYear`

### Optional (`FluentOptional`)

`toOptional`, `orNull`, `orEmpty`, `ifPresent`, `mapTo`, `filterBy`, `isPresent`, `isEmpty`, `orElseThrow`

### Path (`FluentPath`)

`readText`, `writeText`, `readLines`, `exists`, `extension`, `nameWithoutExtension`, `fileName`, `copyTo`, `moveTo`, `deleteIfExists`, `sizeInBytes`, `isDirectory`, `listFiles`, `createDirectories`

## Demo

See `fluentjava-demo` for a working sample including a custom extension method.

```bash
cd fluentjava-demo
mvn clean compile
```
