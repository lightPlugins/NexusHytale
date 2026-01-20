package io.nexstudios.hytale.nexus.configs;

import com.hypixel.hytale.logger.HytaleLogger;
import lombok.Getter;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.ConfigurationOptions;
import org.spongepowered.configurate.loader.HeaderMode;
import org.spongepowered.configurate.yaml.NodeStyle;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Objects;

/**
 * The NexusFile class represents a YAML configuration file, providing methods to manage its
 * lifecycle and access its contents. It includes functionality for resource loading, default
 * configuration merging, directory creation, and header customization.
 *
 * This class is designed to handle configuration files with detailed controls over their storage
 * location, resource fallback logic, and runtime access to configuration data. It also ensures that
 * any required resources or directories are present when interacting with the configuration file.
 */
public final class NexusFile {

    @Getter
    private final Path filePath;
    private final String resourcePath; // IMMER ohne führenden Slash
    private final boolean loadDefault;
    private final ClassLoader classLoader;
    private final String headerText;

    private final YamlConfigurationLoader diskLoader;

    private volatile NexusFileConfiguration config;

    /**
     * Constructs a NexusFile instance, representing a YAML configuration file
     * with options for resource loading, default merging, and header customization.
     *
     * @param dataDirectory the path to the directory where the configuration file is stored; must not be null
     * @param fileName the name of the configuration file; must not be null
     * @param resourcePath the path to the resource inside the JAR for loading defaults; must not be null
     * @param loadDefault determines whether defaults from the resource should be merged into the configuration
     * @param classLoader the class loader used to load resources; must not be null
     * @param headerText optional text to include as a header in the configuration file
     */
    public NexusFile(
            Path dataDirectory,
            String fileName,
            String resourcePath,
            boolean loadDefault,
            ClassLoader classLoader,
            String headerText
    ) {
        Path internalDataDirectory = Objects.requireNonNull(dataDirectory, "dataDirectory");
        this.filePath = internalDataDirectory.resolve(Objects.requireNonNull(fileName, "fileName"));
        this.resourcePath = normalizeResourcePath(Objects.requireNonNull(resourcePath, "resourcePath"));
        this.loadDefault = loadDefault;
        this.classLoader = Objects.requireNonNull(classLoader, "classLoader");
        this.headerText = headerText;

        mkdirs(internalDataDirectory);

        this.diskLoader = YamlConfigurationLoader.builder()
                .path(this.filePath)
                .indent(2)
                .nodeStyle(NodeStyle.BLOCK)
                .headerMode(HeaderMode.PRESERVE)
                .defaultOptions(opts -> applyHeader(opts, this.headerText))
                .build();
    }

    /**
     * Constructs a NexusFile instance, representing a YAML configuration file
     * with options for resource loading and default merging.
     *
     * @param dataDirectory the path to the directory where the configuration file is stored; must not be null
     * @param fileName the name of the configuration file; must not be null
     * @param resourcePath the path to the resource inside the JAR for loading defaults; must not be null
     * @param loadDefault determines whether defaults from the resource should be merged into the configuration
     * @param classLoader the class loader used to load resources; must not be null
     */
    public NexusFile(
            Path dataDirectory,
            String fileName,
            String resourcePath,
            boolean loadDefault,
            ClassLoader classLoader
    ) {
        this(dataDirectory, fileName, resourcePath, loadDefault, classLoader, null);
    }

    /**
     * Checks whether the file at the specified filePath exists in the file system.
     *
     * @return {@code true} if the file exists; {@code false} otherwise
     */
    public boolean exists() {
        return Files.exists(filePath);
    }

    /**
     * Retrieves the current NexusFileConfiguration instance associated with this NexusFile.
     * This method ensures that the configuration is loaded before returning it.
     *
     * @return the current NexusFileConfiguration instance
     */
    public NexusFileConfiguration getConfig() {
        NexusFileConfiguration snapshot = config;
        if (snapshot == null) {
            throw new IllegalStateException("Config ist nicht geladen. Erst reload() aufrufen.");
        }
        return snapshot;
    }

    /**
     * Saves the default configuration file to the disk. This method ensures that
     * the specified file path exists by creating necessary parent directories
     * and by copying the resource from the JAR to the target location if the
     * file does not already exist.
     *
     * If the file already exists, the method does not overwrite it.
     *
     * @throws RuntimeException if an I/O error occurs while creating directories
     *                          or copying the resource to the disk
     */
    public void saveDefault() {
        try {
            mkdirs(filePath.getParent());
            if (Files.notExists(filePath)) {
                copyResourceToDisk(resourcePath, filePath);
            }
        } catch (IOException e) {
            throw new RuntimeException("Konnte Default-Config nicht erstellen: " + filePath, e);
        }
    }

    /**
     * Reloads the configuration file associated with this NexusFile instance.
     *
     * The method performs the following steps:
     * 1. Ensures the parent directories for the configuration file exist by creating them if necessary.
     * 2. Checks if the configuration file exists; if not:
     *    - Copies a default resource file from the JAR to the disk if the resource exists.
     *    - Otherwise, creates an empty configuration file.
     * 3. Loads the configuration file into memory.
     * 4. If the loadDefault flag is enabled and a resource file exists, merges default values
     *    from the resource file into the loaded configuration and persists the changes to disk.
     * 5. Updates the current configuration instance with loaded data.
     *
     * @throws RuntimeException if an exception occurs during the reload process, such as I/O errors
     *                          or issues with loading or saving the configuration file.
     */
    public synchronized void reload() {
        try {
            mkdirs(filePath.getParent());

            if (Files.notExists(filePath)) {
                if (resourceExists(resourcePath)) {
                    copyResourceToDisk(resourcePath, filePath);
                } else {
                    CommentedConfigurationNode empty = diskLoader.createNode();
                    diskLoader.save(empty);
                }
            }

            CommentedConfigurationNode serverRoot = diskLoader.load();

            if (loadDefault && resourceExists(resourcePath)) {
                CommentedConfigurationNode defaultsRoot = loadDefaultsFromResource();
                serverRoot.mergeFrom(defaultsRoot);

                // Materialisiert neue Keys in der Datei
                diskLoader.save(serverRoot);
            }

            // Snapshot bauen und am Ende atomar publishen
            this.config = new NexusFileConfiguration(filePath, diskLoader, serverRoot);
        } catch (Exception e) {
            throw new RuntimeException("Konnte Config nicht reloaden: " + filePath, e);
        }
    }

    /**
     * Saves the current configuration to disk.
     *
     * This method verifies that the configuration is loaded before proceeding.
     * It delegates the save operation to the underlying configuration system,
     * ensuring that the latest state is persisted to the file.
     *
     * @throws IllegalStateException if the configuration is not loaded
     * @throws RuntimeException if an error occurs while saving the configuration
     */
    public synchronized void save() {
        NexusFileConfiguration snapshot = config;
        if (snapshot == null) {
            throw new IllegalStateException("Config ist nicht geladen. Erst reload() aufrufen.");
        }
        snapshot.save();
    }

    /**
     * Loads the default configuration values from a YAML resource file bundled within the application.
     * The method reads the resource specified by the {@code resourcePath} field, applies
     * header configurations, and returns the parsed YAML configuration as a {@code CommentedConfigurationNode}.
     *
     * @return a {@code CommentedConfigurationNode} containing the loaded default values from the resource
     * @throws IOException if an I/O error occurs while reading the resource
     */
    private CommentedConfigurationNode loadDefaultsFromResource() throws IOException {
        YamlConfigurationLoader defaultsLoader = YamlConfigurationLoader.builder()
                .indent(2)
                .nodeStyle(NodeStyle.BLOCK)
                .headerMode(HeaderMode.PRESERVE)
                .defaultOptions(opts -> applyHeader(opts, this.headerText))
                .source(() -> new BufferedReader(new InputStreamReader(openResourceOrThrow(resourcePath), StandardCharsets.UTF_8)))
                .build();

        return defaultsLoader.load();
    }

    /**
     * Checks whether a resource with the specified path exists and can be accessed
     * using the class loader.
     *
     * @param path the resource path to check; must not be null
     * @return {@code true} if the resource exists; {@code false} otherwise
     */
    private boolean resourceExists(String path) {
        return classLoader.getResource(path) != null;
    }

    /**
     * Opens an {@link InputStream} for the specified resource path. If the resource
     * is not found in the JAR using the provided class loader, an
     * {@link IllegalStateException} is thrown.
     *
     * @param path the path to the resource inside the JAR; must not be null
     * @return the {@link InputStream} of the resource if found
     * @throws IllegalStateException if the resource cannot be found
     */
    private InputStream openResourceOrThrow(String path) {
        InputStream in = classLoader.getResourceAsStream(path);
        if (in == null) {
            throw new IllegalStateException("Resource nicht gefunden im JAR: " + path);
        }
        return in;
    }

    /**
     * Copies a resource from the JAR file to the specified disk location. This method ensures that
     * the parent directories of the target path are created before attempting to copy the resource.
     * If the target file already exists, the method does nothing.
     *
     * @param resourcePath the path to the resource inside the JAR; must not be null
     * @param target the target file path on the disk where the resource will be copied; must not be null
     * @throws IOException if an I/O error occurs during directory creation or file copy
     */
    private void copyResourceToDisk(String resourcePath, Path target) throws IOException {
        mkdirs(target.getParent());

        if (Files.exists(target)) {
            return;
        }

        try (InputStream in = openResourceOrThrow(resourcePath)) {
            Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    /**
     * Creates the directories along the specified path if they do not already exist.
     * If the directory creation fails due to an I/O error, a runtime exception is thrown.
     *
     * @param dir the path to the directory that needs to be created; may be {@code null},
     *            in which case no action is performed
     * @throws RuntimeException if an I/O error occurs while creating directories
     */
    private static void mkdirs(Path dir) {
        try {
            if (dir != null) Files.createDirectories(dir);
        } catch (IOException e) {
            throw new RuntimeException("Konnte Verzeichnis nicht erstellen: " + dir, e);
        }
    }

    /**
     * Applies a header to the provided configuration options if the header text is not null or blank.
     *
     * @param opts the {@code ConfigurationOptions} to which the header will be applied; must not be null
     * @param headerText the header text to be applied; if {@code null} or blank, no changes are made
     * @return the modified {@code ConfigurationOptions} with the header applied, or the original options if no header is applied
     */
    private static ConfigurationOptions applyHeader(ConfigurationOptions opts, String headerText) {
        if (headerText == null || headerText.isBlank()) return opts;
        return opts.header(headerText);
    }

    /**
     * Normalizes the provided resource path by removing all leading slashes.
     *
     * @param rp the resource path to be normalized
     * @return the normalized resource path without any leading slashes
     */
    private static String normalizeResourcePath(String rp) {
        // ClassLoader-Resources: KEIN führender Slash.
        while (rp.startsWith("/")) rp = rp.substring(1);
        return rp;
    }

    private void ensureLoaded() {
        if (config == null) {
            throw new IllegalStateException("Config ist nicht geladen. Erst reload() aufrufen.");
        }
    }


    /**
     * Processes configuration settings, logging information about map entries and their arguments.
     *
     * @param settings   the NexusFile instance containing configuration data
     * @param sectionKey the key to retrieve the section list from the configuration
     * @param LOGGER     the HytaleLogger instance for logging output
     */
    public void example(NexusFile settings, String sectionKey, HytaleLogger LOGGER) {
        var cfg = settings.getConfig();
        var mapEntries = cfg.getSectionList(sectionKey);

        if (mapEntries.isEmpty()) {
            LOGGER.atInfo().log("'map' ist leer oder fehlt.");
            return;
        }

        for (int mapIndex = 0; mapIndex < mapEntries.size(); mapIndex++) {
            var entry = mapEntries.get(mapIndex);

            int id = entry.getInt("id", -1);
            var args = entry.getSectionList("args");

            LOGGER.atInfo().log("map[" + mapIndex + "] id=" + id + " -> args size=" + args.size());

            for (int argIndex = 0; argIndex < args.size(); argIndex++) {
                var arg = args.get(argIndex);

                String name = arg.getString("name", "<missing>");
                int amount = arg.getInt("amount", -1);
                String permission = arg.getString("permission", "<missing>");

                LOGGER.atInfo().log(
                        "  args[" + argIndex + "]: name='" + name + "', amount=" + amount + ", permission='" + permission + "'"
                );
            }
        }
    }
}
