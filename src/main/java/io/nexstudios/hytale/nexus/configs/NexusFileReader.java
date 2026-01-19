package io.nexstudios.hytale.nexus.configs;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.loader.HeaderMode;
import org.spongepowered.configurate.yaml.NodeStyle;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;

/**
 * Read-only YAML file reader for the Nexus Configurate-based config system.
 *
 * <p>This class scans a directory (recursively), loads all <code>.yml</code> files,
 * and caches the resulting {@link NexusFileConfiguration} instances.</p>
 *
 * <p>IMPORTANT: This reader does not create YAML files. It only reads existing ones.
 * It may create the directory if it does not exist, to ensure scanning works.</p>
 */
@Getter
public final class NexusFileReader {

    /**
     * The base data directory (e.g. getDataDirectory().toAbsolutePath()).
     */
    private final Path dataDirectory;

    /**
     * Directory relative to {@link #dataDirectory} that will be scanned.
     */
    private final String directoryPath;

    /**
     * All discovered YAML files (absolute paths).
     */
    private List<Path> files;

    /**
     * Cached loaded configurations in the same order as {@link #files}.
     */
    private final List<NexusFileConfiguration> nexusFiles;

    /**
     * Cached configurations by file name without extension (e.g. "weapons" -> weapons.yml).
     */
    private final Map<String, NexusFileConfiguration> nexusFileMap;

    /**
     * If true, a file named "_example.yml" will be excluded.
     */
    private final boolean excludeExample;

    /**
     * Creates a new reader and immediately scans and loads all YAML files.
     *
     * @param dataDirectory  Base folder that contains your plugin/mod data.
     * @param directoryPath  Subfolder relative to dataDirectory that contains YAML files.
     */
    public NexusFileReader(Path dataDirectory, String directoryPath) {
        this(dataDirectory, directoryPath, true);
    }

    /**
     * Creates a new reader and immediately scans and loads all YAML files.
     *
     * @param dataDirectory    Base folder that contains your plugin/mod data.
     * @param directoryPath    Subfolder relative to dataDirectory that contains YAML files.
     * @param excludeExample   If true, ignores "_example.yml".
     */
    public NexusFileReader(Path dataDirectory, String directoryPath, boolean excludeExample) {
        this.dataDirectory = Objects.requireNonNull(dataDirectory, "dataDirectory");
        this.directoryPath = Objects.requireNonNull(directoryPath, "directoryPath");
        this.excludeExample = excludeExample;

        this.files = new ArrayList<>();
        this.nexusFiles = new ArrayList<>();
        this.nexusFileMap = new HashMap<>();

        reload();
    }

    /**
     * Re-scans the directory and reloads all YAML files into cache.
     */
    public void reload() {
        loadYmlFiles();
        readNexusFiles();
    }

    /**
     * Finds all YAML files within the configured directory (recursive).
     *
     * <p>This method does NOT create YAML files. It only collects their paths.
     * The directory itself may be created if it doesn't exist, to avoid scan errors.</p>
     */
    private void loadYmlFiles() {
        this.files = new ArrayList<>();

        Path directory = dataDirectory.resolve(directoryPath);

        try {
            if (!Files.exists(directory)) {
                Files.createDirectories(directory);
            }

            Files.walkFileTree(directory, EnumSet.noneOf(FileVisitOption.class), Integer.MAX_VALUE, new SimpleFileVisitor<>() {
                @Override
                public @NotNull FileVisitResult visitFile(@NotNull Path file, @NotNull BasicFileAttributes attrs) {
                    if (file.toString().endsWith(".yml")) {
                        String fileName = file.getFileName().toString();
                        if (!(excludeExample && fileName.equalsIgnoreCase("_example.yml"))) {
                            files.add(file.toAbsolutePath());
                        }
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            throw new RuntimeException("Something went wrong while scanning YAML files in: " + directory, e);
        }
    }

    /**
     * Loads all discovered YAML files into {@link NexusFileConfiguration} objects and caches them.
     *
     * <p>This method is read-only: it will not write back to disk.</p>
     */
    private void readNexusFiles() {
        nexusFiles.clear();
        nexusFileMap.clear();

        for (Path file : files) {
            NexusFileConfiguration cfg = loadSingle(file);
            nexusFiles.add(cfg);
            nexusFileMap.put(getFileNameWithoutExtension(file), cfg);
        }
    }

    /**
     * Loads a single YAML file into a {@link NexusFileConfiguration}.
     *
     * <p>Formatting options are set to "block style" with 2 spaces indentation to match typical YAML style.
     * Header mode is PRESERVE to avoid rewriting or altering headers (this reader does not write anyway).</p>
     */
    private NexusFileConfiguration loadSingle(Path file) {
        try {
            YamlConfigurationLoader loader = YamlConfigurationLoader.builder()
                    .path(file)
                    .indent(2)
                    .nodeStyle(NodeStyle.BLOCK)
                    .headerMode(HeaderMode.PRESERVE)
                    .build();

            CommentedConfigurationNode root = loader.load();
            return new NexusFileConfiguration(file, loader, root);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load YAML file: " + file, e);
        }
    }

    /**
     * Returns a cached config by its file name (without extension).
     *
     * @param name File name without extension, e.g. "weapons" for "weapons.yml"
     * @return Cached configuration or null if not present
     */
    public NexusFileConfiguration getByName(String name) {
        return nexusFileMap.get(name);
    }

    /**
     * Returns the file name without any extension.
     *
     * @param file File path
     * @return Name without extension
     */
    public String getFileNameWithoutExtension(Path file) {
        String fileName = file.getFileName().toString();
        int pos = fileName.lastIndexOf('.');
        if (pos > 0) {
            fileName = fileName.substring(0, pos);
        }
        return fileName;
    }

    /**
     * Casts an arbitrary value (usually from Section#getValues/get(...)) into a List<Map<String, Object>>.
     *
     * <p>If the value is not a suitable list, an empty list is returned.</p>
     */
    @SuppressWarnings("unchecked")
    public static List<Map<String, Object>> castListOfMap(Object value) {
        if (value instanceof List<?> list) {
            List<Map<String, Object>> out = new ArrayList<>();
            for (Object element : list) {
                if (element instanceof Map<?, ?> map) {
                    out.add((Map<String, Object>) map);
                }
            }
            return out;
        }
        return List.of();
    }

    /**
     * Convenience method to read a List<Map<String, Object>> from a {@link NexusFileConfiguration} path.
     *
     * <p>If the path does not exist or is not a suitable list, an empty list is returned.</p>
     */
    public static List<Map<String, Object>> getMapList(NexusFileConfiguration config, String path) {
        if (config == null || path == null || path.isEmpty()) {
            return List.of();
        }
        Object raw = config.getRootSection().getValues(true).get(path); // deep map lookup is dot-based, may not match list paths
        // Prefer using sections instead (recommended):
        // return config.getRootSection().getMapList(path);

        // Best practice: use getMapList on sections:
        return config.getRootSection().getMapList(path);
    }
}
