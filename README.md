# FluentJava

FluentJava apporte a Java une experience d'appel fluide, lisible et expressive, tout en gardant un resultat de compilation simple: des appels statiques Java classiques.

L'idee est directe: ecrire du code qui se lit comme une API native, sans sacrifier la clarte, la performance ou la simplicite de production.

```java
String slug = title.trimToNull().toSlug();
```

Le code compile ensuite vers des appels statiques ordinaires du runtime FluentJava. En production, il n'y a ni agent, ni reflection, ni magie runtime a maintenir.

## Pourquoi FluentJava

- Rend le code metier plus lisible et plus compact.
- Evite une accumulation de helpers statiques disperses dans le projet.
- Centralise des operations utiles sur `String`, `List`, `Map`, `Optional`, `Path`, `LocalDate`, nombres et objets.
- Permet d'ajouter vos propres methodes fluides sur n'importe quelle classe grace a `@FluentExtension`.
- Reste compatible avec un usage classique en methodes statiques si vous ne voulez pas activer le plugin de compilation.

## Modules

- `fluentjava-runtime`: le runtime public, avec les classes utilitaires statiques et l'annotation `@FluentExtension`.
- `fluentjava-plugin`: le plugin `javac` qui transforme les appels fluides a la compilation.
- `fluentjava-maven-plugin`: la facon recommandee d'activer FluentJava avec Maven.
- `fluentjava-gradle-plugin`: le plugin Gradle pour activer FluentJava dans un build Gradle.

## 10 exemples pertinents

### 1. Nettoyer un titre et produire un slug

Avant, en Java pur:

```java
String slug = null;
if (title != null) {
    String trimmed = title.trim();
    if (!trimmed.isEmpty()) {
        slug = trimmed
                .toLowerCase()
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("^-+|-+$", "");
    }
}
```

Apres, avec FluentJava:

```java
String slug = title.trimToNull().toSlug();
```

### 2. Parser un entier sans try/catch dans le code metier

Avant, en Java pur:

```java
Integer quantity = null;
if (rawQuantity != null) {
    String trimmed = rawQuantity.trim();
    if (!trimmed.isEmpty()) {
        try {
            quantity = Integer.valueOf(trimmed);
        } catch (NumberFormatException ignored) {
        }
    }
}
```

Apres, avec FluentJava:

```java
Integer quantity = rawQuantity.toIntOrNull();
```

### 3. Masquer une information sensible

Avant, en Java pur:

```java
String maskedCard;
if (cardNumber == null || cardNumber.length() <= 4) {
    maskedCard = cardNumber;
} else {
    int hidden = cardNumber.length() - 4;
    maskedCard = "*".repeat(hidden) + cardNumber.substring(hidden);
}
```

Apres, avec FluentJava:

```java
String maskedCard = cardNumber.mask(4);
```

### 4. Normaliser une phrase puis extraire des initiales

Avant, en Java pur:

```java
String initials = "";
if (fullName != null) {
    String normalized = fullName.trim().replaceAll("\\s+", " ");
    if (!normalized.isEmpty()) {
        String[] parts = normalized.split(" ");
        StringBuilder builder = new StringBuilder();
        for (String part : parts) {
            if (!part.isEmpty()) {
                builder.append(Character.toUpperCase(part.charAt(0)));
            }
        }
        initials = builder.toString();
    }
}
```

Apres, avec FluentJava:

```java
String initials = fullName.normalizeWhitespace().toInitials();
```

### 5. Filtrer, transformer et dedupliquer une liste

Avant, en Java pur:

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

Apres, avec FluentJava:

```java
List<String> emails = users
        .filterBy(User::isActive)
        .mapTo(User::getEmail)
    .mapNotNull(email -> email.trimToNull())
        .mapTo(String::toLowerCase)
        .distinctBy(email -> email);
```

### 6. Regrouper une liste par cle metier

Avant, en Java pur:

```java
Map<String, List<User>> byDepartment = new LinkedHashMap<>();
for (User user : users) {
    String key = user.getDepartment();
    byDepartment.computeIfAbsent(key, ignored -> new ArrayList<>()).add(user);
}
```

Apres, avec FluentJava:

```java
Map<String, List<User>> byDepartment = users.groupBy(User::getDepartment);
```

### 7. Paginer proprement une liste

Avant, en Java pur:

```java
List<Order> pageItems = Collections.emptyList();
int start = Math.max(0, (page - 1) * size);
if (start < orders.size()) {
    int end = Math.min(orders.size(), start + size);
    pageItems = orders.subList(start, end);
}
```

Apres, avec FluentJava:

```java
List<Order> pageItems = orders.paginate(page, size);
```

### 8. Initialiser une valeur dans une map a la demande

Avant, en Java pur:

```java
List<String> errors = errorsByField.get("email");
if (errors == null) {
    errors = new ArrayList<>();
    errorsByField.put("email", errors);
}
errors.add("Adresse invalide");
```

Apres, avec FluentJava:

```java
errorsByField.getOrPut("email", ArrayList::new).add("Adresse invalide");
```

### 9. Manipuler les dates metier sans verbosite

Avant, en Java pur:

```java
boolean businessDay = false;
if (deliveryDate != null) {
    DayOfWeek day = deliveryDate.getDayOfWeek();
    businessDay = day != DayOfWeek.SATURDAY && day != DayOfWeek.SUNDAY;
}

LocalDate nextOpenDay = deliveryDate;
if (nextOpenDay != null) {
    do {
        nextOpenDay = nextOpenDay.plusDays(1);
    } while (nextOpenDay.getDayOfWeek() == DayOfWeek.SATURDAY
            || nextOpenDay.getDayOfWeek() == DayOfWeek.SUNDAY);
}
```

Apres, avec FluentJava:

```java
boolean businessDay = deliveryDate.isBusinessDay();
LocalDate nextOpenDay = deliveryDate.nextWeekday();
```

### 10. Construire une validation metier lisible

Avant, en Java pur:

```java
List<String> errors = new ArrayList<>();
if (name == null || name.isBlank()) {
    errors.add("Le nom est obligatoire");
}
if (name != null && name.length() < 3) {
    errors.add("Le nom doit contenir au moins 3 caracteres");
}
if (name != null && name.length() > 80) {
    errors.add("Le nom est trop long");
}
```

Apres, avec FluentJava:

```java
List<String> errors = FluentValidator.ofString(name)
        .notBlank("Le nom est obligatoire")
        .minLength(3, "Le nom doit contenir au moins 3 caracteres")
        .maxLength(80, "Le nom est trop long")
        .validate();
```

## Installation

FluentJava cible Java 17+.

### Maven

Configuration recommandee pour beneficier de l'ecriture fluide directement dans le code source:

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

Avec cette configuration, vous ecrivez simplement:

```java
String slug = title.trimToNull().toSlug();
```

### Gradle

Groovy DSL:

```groovy
plugins {
    id 'java'
    id 'io.fluentjava' version '1.0.0'
}

dependencies {
    implementation 'io.fluentjava:fluentjava-runtime:1.0.0'
}
```

Kotlin DSL:

```kotlin
plugins {
    java
    id("io.fluentjava") version "1.0.0"
}

dependencies {
    implementation("io.fluentjava:fluentjava-runtime:1.0.0")
}
```

### Plugin IntelliJ

Avec le plugin FluentJava pour IntelliJ IDEA, l'experience IDE devient coherente avec l'experience de compilation:

- autocompletion sur les methodes FluentJava;
- navigation vers la methode source;
- documentation inline;
- disparition des faux positifs sur les appels fluides;
- prise en charge des methodes creees dans votre propre projet avec `@FluentExtension`.

Autrement dit, le plugin IDE ne se limite pas aux methodes livrees par la librairie: il sait aussi accompagner les extensions que vous ajoutez vous-meme.

## Ajouter vos propres methodes avec @FluentExtension

L'un des points forts de FluentJava est la possibilite d'ajouter des methodes fluides sur n'importe quelle classe utile a votre domaine: une classe metier, un type du JDK, ou un type tiers que vous utilisez deja.

### Exemple

```java
package com.acme.extension;

import io.fluentjava.annotation.FluentExtension;

@FluentExtension(target = String.class)
public final class StringMarketingExtensions {

    private StringMarketingExtensions() {
    }

    public static String bracketize(String value, String left, String right) {
        if (value == null) {
            return null;
        }
        return left + value + right;
    }
}
```

Ensuite, dans votre code:

```java
String label = productName.bracketize("[", "]");
```

### Regles a respecter

- la classe porte `@FluentExtension(target = VotreType.class)`;
- les methodes doivent etre `public static`;
- le premier parametre represente le receveur de l'appel fluide;
- les autres parametres deviennent les arguments de la methode.

### Pourquoi c'est puissant

- vous etendez le langage de votre projet sans wrapper ni inheritance artificielle;
- vous gardez des signatures simples et testables;
- vous pouvez construire une API vraiment metier, proche du vocabulaire de votre equipe;
- le plugin IntelliJ supporte aussi ces methodes lorsqu'elles sont declarees dans le projet.

## Utiliser FluentJava sans plugin

FluentJava reste utile meme si vous ne voulez pas activer l'ecriture fluide par plugin de compilation.

Vous pouvez appeler les classes statiques directement:

```java
import io.fluentjava.string.FluentString;

String slug = FluentString.toSlug(FluentString.trimToNull(title));
```

Ou avec des imports statiques:

```java
import static io.fluentjava.string.FluentString.toSlug;
import static io.fluentjava.string.FluentString.trimToNull;

String slug = toSlug(trimToNull(title));
```

Dans ce mode, vous n'avez besoin que de la dependance runtime. Aucun plugin n'est necessaire dans le `pom.xml` ou dans le build Gradle.

## Reference API

### FluentObject

- `also`: execute une action sur l'objet et retourne cet objet.
- `let`: applique une fonction a l'objet et retourne le resultat.
- `takeIf`: retourne l'objet si le predicat est vrai, sinon `null`.
- `takeUnless`: retourne l'objet si le predicat est faux, sinon `null`.
- `isNull`: verifie si l'objet est `null`.
- `isNotNull`: verifie si l'objet n'est pas `null`.
- `orElse`: retourne l'objet ou une valeur de secours s'il est `null`.
- `orElseGet`: retourne l'objet ou calcule une valeur de secours via un `Supplier`.
- `requireNotNull`: retourne l'objet s'il n'est pas `null`, sinon leve une exception.
- `requireThat`: leve une exception si une condition est fausse.

### FluentString

- `isBlankSafe`: verifie si une chaine est `null`, vide ou composee uniquement d'espaces.
- `isNullOrBlank`: alias lisible pour le meme besoin que `isBlankSafe`.
- `isNullOrEmpty`: verifie si une chaine est `null` ou vide.
- `orEmpty`: retourne une chaine vide quand la valeur est `null`.
- `orDefault`: retourne une valeur par defaut quand la chaine est absente ou vide.
- `trimToNull`: supprime les espaces en debut et fin puis retourne `null` si le resultat est vide.
- `toIntOrNull`: convertit une chaine en `Integer`, ou retourne `null` en cas d'echec.
- `toLongOrNull`: convertit une chaine en `Long`, ou retourne `null` en cas d'echec.
- `toDoubleOrNull`: convertit une chaine en `Double`, ou retourne `null` en cas d'echec.
- `toBooleanOrNull`: convertit une chaine vers un booleen tolerant, ou retourne `null`.
- `toBigDecimalOrNull`: convertit une chaine en `BigDecimal`, ou retourne `null` en cas d'echec.
- `take`: retourne les `n` premiers caracteres.
- `takeLast`: retourne les `n` derniers caracteres.
- `drop`: supprime les `n` premiers caracteres.
- `dropLast`: supprime les `n` derniers caracteres.
- `padStart`: complete la chaine a gauche jusqu'a une longueur donnee.
- `padEnd`: complete la chaine a droite jusqu'a une longueur donnee.
- `reversed`: inverse la chaine.
- `capitalized`: met la premiere lettre en majuscule.
- `decapitalized`: met la premiere lettre en minuscule.
- `truncate`: tronque une chaine et ajoute un suffixe.
- `removePrefix`: supprime un prefixe s'il est present.
- `removeSuffix`: supprime un suffixe s'il est present.
- `wrap`: entoure une chaine avec un motif au debut et a la fin.
- `unwrap`: retire ce motif s'il encadre bien la chaine.
- `repeat`: repete la chaine `n` fois.
- `containsIgnoreCase`: cherche une sous-chaine sans sensibilite a la casse.
- `startsWithIgnoreCase`: teste un prefixe sans sensibilite a la casse.
- `endsWithIgnoreCase`: teste un suffixe sans sensibilite a la casse.
- `countOccurrences`: compte le nombre d'occurrences d'une sous-chaine.
- `isNumeric`: verifie qu'une chaine ne contient que des chiffres.
- `isAlpha`: verifie qu'une chaine ne contient que des lettres.
- `isAlphanumeric`: verifie qu'une chaine ne contient que des lettres ou des chiffres.
- `isEmail`: detecte un format d'email simple.
- `toSlug`: transforme une chaine en slug URL lisible.
- `mask`: masque le debut d'une chaine en ne laissant visibles que les derniers caracteres.
- `toCamelCase`: convertit une chaine en `camelCase`.
- `toSnakeCase`: convertit une chaine en `snake_case`.
- `left`: alias pratique pour recuperer la partie gauche d'une chaine.
- `right`: alias pratique pour recuperer la partie droite d'une chaine.
- `center`: centre une chaine dans une largeur donnee avec un caractere de remplissage.
- `normalizeWhitespace`: normalise les suites d'espaces en un seul separateur.
- `stripAccents`: retire les accents et diacritiques.
- `ifBlank`: retourne une valeur par defaut si la chaine est blanche.
- `ifEmpty`: retourne une valeur par defaut si la chaine est vide.
- `splitToList`: decoupe une chaine en liste selon un delimiteur.
- `lines`: decoupe une chaine ligne par ligne.
- `toBase64`: encode une chaine en Base64.
- `fromBase64`: decode une chaine Base64.
- `ellipsize`: raccourcit une chaine avec des points de suspension.
- `toPascalCase`: convertit une chaine en `PascalCase`.
- `toKebabCase`: convertit une chaine en `kebab-case`.
- `matchesPattern`: teste une chaine contre une expression reguliere.
- `digest`: produit un condensat avec l'algorithme demande.
- `toInitials`: extrait les initiales d'un texte.
- `countWords`: compte les mots dans une chaine.
- `isUrl`: detecte un format d'URL.
- `isIPv4`: detecte une adresse IPv4.
- `redact`: masque une valeur sensible de facon generique.
- `toCurrency`: formate une chaine numerique comme montant monetaire.

### FluentList

- `firstOrNull`: retourne le premier element ou `null`.
- `isNullOrEmpty`: verifie si une liste est `null` ou vide.
- `orEmpty`: retourne une liste vide si la valeur est `null`.
- `firstOrNull(predicate)`: retourne le premier element correspondant a un predicat.
- `lastOrNull`: retourne le dernier element ou `null`.
- `lastOrNull(predicate)`: retourne le dernier element correspondant a un predicat.
- `getOrNull`: recupere un element par index ou `null` hors limite.
- `getOrDefault`: recupere un element par index ou une valeur par defaut.
- `filterBy`: filtre une liste selon un predicat.
- `mapTo`: transforme chaque element avec une fonction.
- `flatMap`: transforme chaque element en liste puis aplatit le resultat.
- `mapNotNull`: transforme puis elimine les resultats `null`.
- `filterNotNull`: retire les elements `null`.
- `distinctBy`: deduplique selon une cle calculee.
- `sortedBy`: trie par cle croissante.
- `sortedByDescending`: trie par cle decroissante.
- `groupBy`: groupe une liste en `Map<K, List<T>>`.
- `associateBy`: cree une map indexee par une cle calculee.
- `partition`: separe la liste en deux groupes selon un predicat.
- `chunked`: decoupe une liste en blocs de taille fixe.
- `sumOf`: somme des valeurs numeriques produites par une fonction.
- `maxByOrNull`: retourne l'element ayant la plus grande cle.
- `minByOrNull`: retourne l'element ayant la plus petite cle.
- `averageOf`: calcule une moyenne a partir d'un extracteur.
- `countBy`: compte les elements correspondant a un predicat.
- `none`: verifie qu'aucun element ne correspond.
- `any`: verifie qu'au moins un element correspond.
- `all`: verifie que tous les elements correspondent.
- `takeWhile`: prend les elements tant qu'une condition reste vraie.
- `dropWhile`: ignore les elements en tete tant qu'une condition reste vraie.
- `paginate`: extrait une page de resultat de maniere sure.
- `zip`: combine deux listes deux a deux.
- `intersect`: retourne l'intersection de deux listes.
- `subtract`: retire d'une liste les elements presents dans une autre.
- `union`: fusionne deux listes sans doublon.
- `second`: retourne le deuxieme element ou `null`.
- `third`: retourne le troisieme element ou `null`.
- `toSet`: convertit une liste en `Set` en conservant l'ordre d'insertion.
- `associate`: construit une map a partir de fonctions de cle et de valeur.
- `indexOfFirst`: retourne l'index du premier element correspondant.
- `indexOfLast`: retourne l'index du dernier element correspondant.
- `flatten`: aplatit une liste de listes.
- `randomOrNull`: retourne un element aleatoire ou `null`.
- `forEachIndexed`: itere en fournissant index et valeur.
- `toCsv`: joint les elements en texte CSV simple.
- `frequencies`: compte les occurrences de chaque element.
- `shuffled`: retourne une copie melangee de la liste.
- `sample`: retourne un echantillon aleatoire sans repetition.
- `windowed`: produit des fenetres glissantes de taille fixe.
- `sumOfInt`: somme des valeurs entieres produites par un extracteur.
- `sumOfLong`: somme des valeurs longues produites par un extracteur.

### FluentMap

- `isNullOrEmpty`: verifie si une map est `null` ou vide.
- `orEmpty`: retourne une map vide si la valeur est `null`.
- `getOrEmpty`: retourne une valeur texte ou une chaine vide si absente.
- `getOrNull`: recupere une valeur ou `null` si absente.
- `filterKeys`: filtre une map sur ses cles.
- `filterValues`: filtre une map sur ses valeurs.
- `mapValues`: transforme les valeurs en gardant les memes cles.
- `mapKeys`: transforme les cles en gardant les memes valeurs.
- `any`: verifie si au moins une entree correspond a un predicat.
- `all`: verifie si toutes les entrees correspondent.
- `none`: verifie qu'aucune entree ne correspond.
- `count`: compte les entrees correspondant a un predicat.
- `toList`: convertit les entrees d'une map en liste.
- `merge`: fusionne deux maps, la seconde prenant le dessus en cas de collision.
- `invertMap`: inverse les cles et les valeurs.
- `getOrPut`: retourne une valeur existante ou l'initialise a la demande.
- `forEach`: parcourt les entrees avec une action `(key, value)`.
- `toSortedMap`: retourne une map triee par ordre naturel des cles.
- `containsAllKeys`: verifie la presence de plusieurs cles.
- `flatMapValues`: transforme chaque valeur en liste de valeurs derivees.
- `entries`: retourne les entrees sous forme de liste.
- `filterByValue`: alias orientee lisibilite pour filtrer par valeur.

### FluentNumber

- `coerceIn`: force une valeur a rester dans un intervalle.
- `coerceAtLeast`: impose une borne minimale.
- `coerceAtMost`: impose une borne maximale.
- `isBetween`: teste si une valeur appartient a un intervalle.
- `isPositive`: verifie si un nombre est strictement positif.
- `isNegative`: verifie si un nombre est strictement negatif.
- `isZero`: verifie si un nombre vaut zero.
- `roundTo`: arrondit une valeur a un nombre de decimales donne.
- `percentOf`: calcule un pourcentage.
- `isEven`: verifie si un entier est pair.
- `isOdd`: verifie si un entier est impair.
- `toOrdinal`: formate un entier en ordinal anglais (`1st`, `2nd`, `3rd`, ...).
- `toPercentString`: formate un nombre en pourcentage texte.
- `clamp`: alias pratique de limitation a un intervalle.
- `isPrime`: verifie si un entier est premier.
- `factorial`: calcule une factorielle.
- `digits`: retourne les chiffres d'un entier sous forme de liste.
- `toBinary`: retourne la representation binaire d'un entier.
- `toHex`: retourne la representation hexadecimale d'un entier.
- `toOctal`: retourne la representation octale d'un entier.

### FluentDate

- `isWeekend`: verifie si une date tombe un samedi ou un dimanche.
- `isWeekday`: verifie si une date est un jour de semaine.
- `isToday`: verifie si une date correspond a aujourd'hui.
- `isPast`: verifie si une date est dans le passe.
- `isFuture`: verifie si une date est dans le futur.
- `isLeapYear`: verifie si l'annee de la date est bissextile.
- `daysUntil`: calcule un nombre de jours entre deux dates.
- `monthsUntil`: calcule un nombre de mois entre deux dates.
- `yearsUntilNow`: calcule des annees completes jusqu'a aujourd'hui.
- `format`: formate une `LocalDate` ou une `LocalDateTime` selon un pattern.
- `atStartOfDay`: convertit une date en debut de jour.
- `atEndOfDay`: convertit une date en fin de jour.
- `toEpochMillis`: convertit une `LocalDateTime` en millisecondes epoch.
- `fromEpochMillis`: convertit des millisecondes epoch en `LocalDateTime`.
- `isBefore`: compare deux dates ou deux `LocalDateTime`.
- `isAfter`: compare deux dates ou deux `LocalDateTime`.
- `isBetween`: teste l'appartenance a une plage inclusive pour date ou date-heure.
- `startOfWeek`: retourne le debut de semaine ISO.
- `endOfWeek`: retourne la fin de semaine ISO.
- `startOfMonth`: retourne le premier jour du mois.
- `endOfMonth`: retourne le dernier jour du mois.
- `startOfYear`: retourne le premier jour de l'annee.
- `endOfYear`: retourne le dernier jour de l'annee.
- `nextWeekday`: retourne le prochain jour ouvrable.
- `age`: calcule un age en annees completes.
- `isBusinessDay`: alias metier pour jour ouvrable.
- `toLocalDate`: parse une chaine vers `LocalDate`.
- `toLocalDateTime`: parse une chaine vers `LocalDateTime`.
- `quarterOf`: retourne le trimestre de l'annee.
- `weekOfYear`: retourne le numero de semaine ISO.

### FluentOptional

- `toOptional`: cree un `Optional` a partir d'une valeur nullable.
- `orNull`: extrait la valeur ou retourne `null`.
- `orEmpty`: extrait une chaine ou retourne une chaine vide.
- `ifPresent`: execute une action si une valeur est presente.
- `mapTo`: transforme un `Optional` en un autre `Optional`.
- `filterBy`: filtre un `Optional` avec un predicat.
- `isPresent`: teste la presence d'une valeur.
- `isEmpty`: teste l'absence de valeur.
- `orElseThrow`: extrait la valeur ou leve une exception explicite.

### FluentPath

- `readText`: lit un fichier texte en UTF-8.
- `writeText`: ecrit un fichier texte en UTF-8.
- `readLines`: lit toutes les lignes d'un fichier.
- `exists`: verifie qu'un chemin existe.
- `extension`: recupere l'extension d'un fichier.
- `nameWithoutExtension`: recupere le nom sans extension.
- `fileName`: recupere le nom du fichier.
- `copyTo`: copie un fichier vers une destination.
- `moveTo`: deplace un fichier vers une destination.
- `deleteIfExists`: supprime un chemin si present.
- `sizeInBytes`: retourne la taille d'un fichier.
- `isDirectory`: verifie si le chemin represente un repertoire.
- `listFiles`: liste le contenu d'un repertoire.
- `createDirectories`: cree un repertoire et ses parents si necessaire.

### FluentValidator

- `of`: cree un validateur pour une valeur generique.
- `ofString`: cree un validateur specialise pour une chaine.
- `notNull`: ajoute une regle de non-nullite.
- `satisfies`: ajoute une regle personnalisee.
- `notBlank`: ajoute une regle de chaine non blanche.
- `minLength`: ajoute une longueur minimale.
- `maxLength`: ajoute une longueur maximale.
- `matches`: ajoute une contrainte regex.
- `notEmpty`: ajoute une contrainte de non-vide.
- `min`: ajoute une borne minimale numerique.
- `max`: ajoute une borne maximale numerique.
- `positive`: exige un nombre strictement positif.
- `validate`: execute les regles et retourne la liste des erreurs.
- `isValid`: retourne `true` si aucune regle n'echoue.
- `throwIfInvalid`: leve une exception si la validation echoue.
- `throwIfInvalid(exceptionFn)`: leve une exception personnalisee a partir des erreurs.
- `firstError`: retourne le premier message d'erreur.

### Try

`Try` est une utilite complementaire pour gerer des erreurs de facon plus fluide:

- `of`: execute un supplier et capture une erreur eventuelle.
- `isSuccess`: verifie si l'execution a reussi.
- `isFailure`: verifie si l'execution a echoue.
- `orNull`: retourne la valeur ou `null` en cas d'echec.
- `orElse`: retourne une valeur de secours.
- `orElseGet`: calcule une valeur de secours a la demande.
- `orElseThrow`: relance l'erreur ou retourne la valeur.
- `map`: transforme la valeur en cas de succes.
- `flatMap`: chaine des operations `Try`.
- `recover`: reconstruit une valeur apres une erreur.
- `onSuccess`: execute un effet de bord sur succes.
- `onFailure`: execute un effet de bord sur echec.
- `getError`: retourne l'erreur capturee.

## Partie technique

FluentJava repose sur une architecture simple et robuste:

- le runtime expose des methodes statiques pures, organisees par type fonctionnel (`FluentString`, `FluentList`, `FluentMap`, etc.);
- le plugin de compilation reconnait les appels fluides et les reecrit vers ces methodes statiques;
- l'API runtime reste utilisable telle quelle, meme sans activer l'ecriture fluide;
- les extensions de projet marquees avec `@FluentExtension` sont decouvertes pendant la compilation pour enrichir le langage du projet;
- le bytecode final reste lisible et previsible, ce qui simplifie debug, perf, revue de code et maintenance long terme.

En pratique, FluentJava ne cherche pas a transformer Java en autre chose. La librairie cherche plutot a rendre Java plus agreable dans les endroits ou il devient repetitif, sans imposer un modele exotique.

## FAQ

### Est-ce que FluentJava ajoute un cout runtime ?

Non. Le resultat final repose sur des appels statiques classiques.

### Est-ce que je suis oblige d'utiliser le plugin ?

Non. Vous pouvez utiliser FluentJava uniquement comme librairie de methodes statiques.

### Est-ce que je peux ajouter mes propres methodes fluides ?

Oui. `@FluentExtension` est prevu pour cela, sur n'importe quelle classe cible pertinente pour votre projet.

### Est-ce que l'IDE peut comprendre mes extensions maison ?

Oui, avec le plugin IntelliJ FluentJava, y compris pour les methodes declarees directement dans votre projet.

### Est-ce que FluentJava remplace les API standards du JDK ?

Non. FluentJava les complete avec une couche de confort et de lisibilite.

### Est-ce adapte a un codebase entreprise ?

Oui, justement parce que l'approche reste compile-time, testable, lisible et facile a auditer.

## Licence

FluentJava est distribue sous licence Apache License 2.0. Voir le fichier `LICENSE` pour le texte complet.
