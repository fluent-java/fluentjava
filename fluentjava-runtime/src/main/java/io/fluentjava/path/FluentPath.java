package io.fluentjava.path;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

/**
 * Fluent utility methods for {@link java.nio.file.Path}.
 *
 * <p>All methods are {@code public static}, null-safe, and handle
 * {@link IOException} gracefully by returning sensible defaults
 * (null, empty list, -1, false) instead of throwing.</p>
 *
 * <h3>Fluent usage (with javac plugin):</h3>
 * <pre>{@code
 *   String content = path.readText();
 *   List<String> lines = path.readLines();
 *   path.writeText("hello").copyTo(backup);
 * }</pre>
 *
 * @since 1.0.0
 */
public final class FluentPath {

    /** Utility class — cannot be instantiated. */
    private FluentPath() {
        throw new AssertionError("FluentPath is a utility class and cannot be instantiated");
    }

    /**
     * Reads the entire file content as a string (UTF-8).
     *
     * @param path the file path (may be {@code null})
     * @return the file content, or {@code null} if path is null or an error occurs
     */
    public static String readText(Path path) {
        if (path == null) {
            return null;
        }
        try {
            return Files.readString(path);
        } catch (IOException e) {
            return null;
        }
    }

    /**
     * Writes the given string content to the file (UTF-8), creating it if needed.
     *
     * @param path    the file path (may be {@code null})
     * @param content the content to write
     * @return the path for chaining, or {@code null} if path is null or error occurs
     */
    public static Path writeText(Path path, String content) {
        if (path == null) {
            return null;
        }
        try {
            return Files.writeString(path, content == null ? "" : content);
        } catch (IOException e) {
            return null;
        }
    }

    /**
     * Reads all lines from the file (UTF-8).
     *
     * @param path the file path (may be {@code null})
     * @return an unmodifiable list of lines, or empty list on error
     */
    public static List<String> readLines(Path path) {
        if (path == null) {
            return List.of();
        }
        try {
            return Collections.unmodifiableList(Files.readAllLines(path));
        } catch (IOException e) {
            return List.of();
        }
    }

    /**
     * Checks if the file or directory exists (null-safe).
     *
     * @param path the path to check (may be {@code null})
     * @return {@code true} if the path exists
     */
    public static boolean exists(Path path) {
        return path != null && Files.exists(path);
    }

    /**
     * Returns the file extension without the leading dot.
     *
     * <h4>Examples:</h4>
     * <pre>{@code
     *   FluentPath.extension(Path.of("config.json"))  // "json"
     *   FluentPath.extension(Path.of("Makefile"))      // ""
     * }</pre>
     *
     * @param path the file path (may be {@code null})
     * @return the extension, or {@code ""} if none
     */
    public static String extension(Path path) {
        if (path == null) {
            return "";
        }
        String name = fileName(path);
        if (name == null) {
            return "";
        }
        int dot = name.lastIndexOf('.');
        return dot > 0 ? name.substring(dot + 1) : "";
    }

    /**
     * Returns the file name without its extension.
     *
     * <h4>Examples:</h4>
     * <pre>{@code
     *   FluentPath.nameWithoutExtension(Path.of("config.json"))  // "config"
     *   FluentPath.nameWithoutExtension(Path.of("Makefile"))      // "Makefile"
     * }</pre>
     *
     * @param path the file path (may be {@code null})
     * @return the name without extension, or {@code null} if path is null
     */
    public static String nameWithoutExtension(Path path) {
        if (path == null) {
            return null;
        }
        String name = fileName(path);
        if (name == null) {
            return null;
        }
        int dot = name.lastIndexOf('.');
        return dot > 0 ? name.substring(0, dot) : name;
    }

    /**
     * Returns the file name as a string (null-safe).
     *
     * @param path the path (may be {@code null})
     * @return the file name string, or {@code null} if path is null
     */
    public static String fileName(Path path) {
        if (path == null || path.getFileName() == null) {
            return null;
        }
        return path.getFileName().toString();
    }

    /**
     * Copies the file to the target path, replacing if it exists.
     *
     * @param path   the source path (may be {@code null})
     * @param target the target path
     * @return the target path, or {@code null} on error
     */
    public static Path copyTo(Path path, Path target) {
        if (path == null || target == null) {
            return null;
        }
        try {
            return Files.copy(path, target, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            return null;
        }
    }

    /**
     * Moves the file to the target path, replacing if it exists.
     *
     * @param path   the source path (may be {@code null})
     * @param target the target path
     * @return the target path, or {@code null} on error
     */
    public static Path moveTo(Path path, Path target) {
        if (path == null || target == null) {
            return null;
        }
        try {
            return Files.move(path, target, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            return null;
        }
    }

    /**
     * Deletes the file if it exists (null-safe).
     *
     * @param path the path to delete (may be {@code null})
     * @return {@code true} if the file was deleted
     */
    public static boolean deleteIfExists(Path path) {
        if (path == null) {
            return false;
        }
        try {
            return Files.deleteIfExists(path);
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * Returns the file size in bytes, or {@code -1} on error.
     *
     * @param path the file path (may be {@code null})
     * @return the size in bytes, or -1
     */
    public static long sizeInBytes(Path path) {
        if (path == null) {
            return -1L;
        }
        try {
            return Files.size(path);
        } catch (IOException e) {
            return -1L;
        }
    }

    /**
     * Checks if the path is a directory (null-safe).
     *
     * @param path the path to check (may be {@code null})
     * @return {@code true} if the path is a directory
     */
    public static boolean isDirectory(Path path) {
        return path != null && Files.isDirectory(path);
    }

    /**
     * Lists files and directories in the given directory path.
     *
     * @param path the directory path (may be {@code null})
     * @return an unmodifiable list of paths, or empty list on error
     */
    public static List<Path> listFiles(Path path) {
        if (path == null || !Files.isDirectory(path)) {
            return List.of();
        }
        try (Stream<Path> stream = Files.list(path)) {
            return stream.toList();
        } catch (IOException e) {
            return List.of();
        }
    }

    /**
     * Creates directories including any necessary parent directories (null-safe).
     *
     * @param path the directory path (may be {@code null})
     * @return the path, or {@code null} on error
     */
    public static Path createDirectories(Path path) {
        if (path == null) {
            return null;
        }
        try {
            return Files.createDirectories(path);
        } catch (IOException e) {
            return null;
        }
    }
}
