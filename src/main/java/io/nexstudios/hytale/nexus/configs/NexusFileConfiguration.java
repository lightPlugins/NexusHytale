package io.nexstudios.hytale.nexus.configs;

import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

import java.nio.file.Path;
import java.util.*;

/**
 * Represents a configuration file loader and accessor for a Nexus configuration.
 * This class provides methods to interact with the configuration file, retrieve
 * sections and values, modify existing nodes, and create new nodes.
 * <p>
 * The configuration is expected to be in YAML format and is loaded into a root node
 * that can be accessed and manipulated using various utility methods provided by this class.
 * <p>
 * Fields:
 * - {@code filePath}: Specifies the file path of the configuration file.
 * - {@code loader}: A YAML configuration loader used for reading from and writing to the file.
 * - {@code root}: Represents the root node of the configuration structure.
 * <p>
 * Methods:
 * - Constructors:
 * Provides a constructor for creating an instance of {@code NexusFileConfiguration} with
 * the given file path, YAML loader, and root node.
 * <p>
 * - Data Retrieval:
 * Includes methods for retrieving strings, integers, booleans, doubles, string lists,
 * list of maps, and sections from the configuration file.
 * Supports default values for basic configurations if the path does not exist.
 * <p>
 * - Section Management:
 * Allows retrieving the root section, creating a configuration section at a
 * specific path, and getting a list or specific sections by path.
 * <p>
 * - Node Existence:
 * Provides a method to check if a node exists at a given path and is not virtual.
 * <p>
 * - Modification:
 * Includes methods to set the value of a configuration node, as well as
 * retrieve and modify keys using section or nested structure techniques.
 * <p>
 * - File Information:
 * Allows retrieval of file information such as file name and file name without
 * an extension from the configuration file path.
 * <p>
 * - Utility:
 * Includes methods for retrieving all keys or key-value pairs from the root configuration,
 * with the option to retrieve them recursively from nested sections.
 */
public record NexusFileConfiguration(Path filePath, YamlConfigurationLoader loader, CommentedConfigurationNode root) {

    /**
     * Constructs a new NexusFileConfiguration instance with the specified file path,
     * YAML configuration loader, and root configuration node.
     *
     * @param filePath the path to the configuration file, must not be null
     * @param loader the YAML configuration loader responsible for reading and writing the configuration, must not be null
     * @param root the root node of the configuration, must not be null
     */
    public NexusFileConfiguration(Path filePath, YamlConfigurationLoader loader, CommentedConfigurationNode root) {
        this.filePath = Objects.requireNonNull(filePath, "filePath");
        this.loader = Objects.requireNonNull(loader, "loader");
        this.root = Objects.requireNonNull(root, "root");
    }

    /**
     * Retrieves the root configuration section of the file.
     *
     * @return a {@link NexusConfigurationSection} representing the root section
     *         of the configuration file.
     */
    public NexusConfigurationSection getRootSection() {
        return new NexusConfigurationSection(this, root);
    }

    /**
     * Retrieves a configuration section from the specified path.
     * If the node at the given path does not exist or is not a map, this method returns null.
     *
     * @param path the path to the configuration section to retrieve
     * @return a {@link NexusConfigurationSection} representing the section at the given path,
     *         or null if the section is not present or is not a valid map
     */
    public NexusConfigurationSection getConfigurationSection(String path) {
        CommentedConfigurationNode node = node(path);
        if (node.virtual() || !node.isMap()) return null;
        return new NexusConfigurationSection(this, node);
    }

    /**
     * Retrieves a list of configuration sections from the specified path.
     * If the node at the given path is not a list or does not exist, this method returns an empty list.
     *
     * @param path the path to the node containing the list of configuration sections
     * @return a list of {@link NexusConfigurationSection} objects representing the child sections at the specified path,
     *         or an empty list if the node is not a list or does not exist
     */
    public List<NexusConfigurationSection> getSectionList(String path) {
        CommentedConfigurationNode node = node(path);
        if (node.virtual() || !node.isList()) return List.of();

        List<NexusConfigurationSection> out = new ArrayList<>();
        for (CommentedConfigurationNode child : node.childrenList()) {
            out.add(new NexusConfigurationSection(this, child));
        }
        return out;
    }

    /**
     * Retrieves a list of maps from the configuration node located at the specified path.
     * If the node at the given path is not a list or does not exist, this method returns an empty list.
     * Each child node of the list is converted into a map representation.
     *
     * @param path the path to the node containing the list of maps
     * @return a list of maps, where each map represents a child node of the list at the specified path,
     *         or an empty list if the node does not exist or is not a list
     */
    public List<Map<String, Object>> getMapList(String path) {
        CommentedConfigurationNode node = node(path);
        if (node.virtual() || !node.isList()) return List.of();

        List<Map<String, Object>> out = new ArrayList<>();
        for (CommentedConfigurationNode child : node.childrenList()) {
            out.add(asMap(child));
        }
        return out;
    }

    /**
     * Retrieves the name of the configuration file from its path.
     *
     * @return the name of the file as a string
     */
    public String getFileName() {
        return filePath.getFileName().toString();
    }

    /**
     * Retrieves the file name without its extension from the configuration file's path.
     *
     * @return the file name without the extension as a string
     */
    public String getFileNameWithoutExtension() {
        String name = getFileName();
        int dot = name.lastIndexOf('.');
        return dot > 0 ? name.substring(0, dot) : name;
    }

    /**
     * Checks whether a configuration node exists at the specified path and is not virtual.
     *
     * @param path the path to the configuration node to check; must not be null
     * @return {@code true} if a non-virtual node exists at the specified path, {@code false} otherwise
     */
    public boolean contains(String path) {
        return !node(path).virtual();
    }

    /**
     * Retrieves a string value from the configuration at the specified path.
     * If the configuration does not contain a value at the given path,
     * the default value will be returned.
     *
     * @param path the path to the configuration node to retrieve the string value from, must not be null
     * @param def the default string value to return if the node at the specified path does not exist
     * @return the string value at the specified path, or the default string if the node is not present
     */
    public String getString(String path, String def) {
        return node(path).getString(def);
    }

    /**
     * Retrieves an integer value from the configuration at the specified path.
     * If the configuration does not contain a value at the given path,
     * the default value will be returned.
     *
     * @param path the path to the configuration node to retrieve the integer value from, must not be null
     * @param def the default integer value to return if the node at the specified path does not exist
     * @return the integer value at the specified path, or the default integer if the node is not present
     */
    public int getInt(String path, int def) {
        return node(path).getInt(def);
    }

    /**
     * Retrieves a boolean value from the configuration at the specified path.
     * If the configuration does not contain a value at the given path,
     * the default value will be returned.
     *
     * @param path the path to the configuration node to retrieve the boolean value from, must not be null
     * @param def the default boolean value to return if the node at the specified path does not exist
     * @return the boolean value at the specified path, or the default boolean if the node is not present
     */
    public boolean getBoolean(String path, boolean def) {
        return node(path).getBoolean(def);
    }

    /**
     * Retrieves a double value from the configuration at the specified path.
     * If the configuration does not contain a value at the given path,
     * the default value will be returned.
     *
     * @param path the path to the configuration node to retrieve the double value from, must not be null
     * @param def the default double value to return if the node at the specified path does not exist
     * @return the double value at the specified path, or the default double if the node is not present
     */
    public double getDouble(String path, double def) {
        return node(path).getDouble(def);
    }

    /**
     * Retrieves a list of string values from the configuration node at the specified path.
     * If the node at the given path is not a list, contains non-string elements, or does not exist,
     * this method returns an empty list.
     *
     * @param path the path to the node containing the list of string values; must not be null
     * @return a list of string values at the specified path, or an empty list if the node does not exist,
     *         is not a list, or contains non-string elements
     */
    public List<String> getStringList(String path) {
        try {
            return node(path).getList(String.class, List.of());
        } catch (SerializationException e) {
            return List.of();
        }
    }

    /**
     * Sets the value of a configuration node at the specified path. If the path does not exist,
     * it will be created. If the operation fails due to a serialization error, it throws a runtime exception.
     *
     * @param path the path to the configuration node to set the value for, must not be null
     * @param value the value to be set at the specified path
     * @throws RuntimeException if the value could not be set at the specified path
     */
    public void set(String path, Object value) {
        try {
            node(path).set(value);
        } catch (SerializationException e) {
            throw new RuntimeException("Konnte Wert nicht setzen: " + path, e);
        }
    }

    /**
     * Creates a new section at the specified path in the configuration. If the path
     * does not exist or is not a map, a new map is created. The created section is
     * returned as a {@link NexusConfigurationSection}.
     *
     * @param path the path to the configuration section to create; must not be null
     * @return a {@link NexusConfigurationSection} representing the newly created section
     * @throws RuntimeException if the section could not be created due to a serialization error
     */
    public NexusConfigurationSection createSection(String path) {
        CommentedConfigurationNode n = node(path);
        if (!n.isMap()) {
            try {
                n.set(new LinkedHashMap<String, Object>());
            } catch (SerializationException e) {
                throw new RuntimeException("Konnte Section nicht erstellen: " + path, e);
            }
        }
        return new NexusConfigurationSection(this, n);
    }

    /**
     * Retrieves a set of keys from the root configuration section.
     *
     * @param deep a boolean indicating whether to retrieve keys recursively;
     *             if {@code true}, keys will be fetched from all nested sections,
     *             otherwise only the direct keys of the root section will be retrieved
     * @return a set of strings representing the keys in the configuration section;
     *         the set may be empty if no keys are present
     */
    public Set<String> getKeys(boolean deep) {
        return getRootSection().getKeys(deep);
    }

    /**
     * Retrieves a map of key-value pairs from the root section.
     *
     * @param deep if true, includes values from nested sections recursively;
     *             if false, includes only the values from the current section.
     * @return a map containing the keys and corresponding objects as their values.
     */
    public Map<String, Object> getValues(boolean deep) {
        return getRootSection().getValues(deep);
    }

    public void save() {
        try {
            loader.save(root);
        } catch (Exception e) {
            throw new RuntimeException("Konnte Config nicht speichern.", e);
        }
    }

    /**
     * Retrieves a {@link CommentedConfigurationNode} located at the specified path.
     * The path is split into segments to access the node's hierarchy.
     *
     * @param path the path to the desired node, represented as a string with segments separated by a delimiter
     * @return the {@link CommentedConfigurationNode} at the specified path
     */
    public CommentedConfigurationNode node(String path) {
        return root.node((Object[]) splitPath(path));
    }

    /**
     * Splits the given path string into an array of strings using the dot (".") delimiter.
     *
     * @param path the input string representing the path to be split. If the input is null or blank, an empty array is returned.
     * @return an array of strings obtained by splitting the input string at each dot (".") delimiter.
     *         Returns an empty array if the input string is null or blank.
     */
    static String[] splitPath(String path) {
        if (path == null || path.isBlank()) return new String[0];
        return path.split("\\.");
    }

    /**
     * Converts a given CommentedConfigurationNode into a map representation.
     *
     * The resulting map will contain the keys and values from the node,
     * recursively processing child nodes that represent maps or lists.
     * If the node is null, virtual, or not a map, an empty map is returned.
     *
     * @param node the CommentedConfigurationNode to convert to a map
     * @return a map representation of the given CommentedConfigurationNode, or an empty map if the node is null, virtual, or invalid
     */
    static Map<String, Object> asMap(CommentedConfigurationNode node) {
        if (node == null || node.virtual() || !node.isMap()) return Map.of();

        Map<String, Object> out = new LinkedHashMap<>();
        for (var e : node.childrenMap().entrySet()) {
            String key = String.valueOf(e.getKey());
            CommentedConfigurationNode child = e.getValue();

            if (child.isMap()) {
                out.put(key, asMap(child));
            } else if (child.isList()) {
                out.put(key, asList(child));
            } else {
                out.put(key, child.raw());
            }
        }
        return out;
    }

    /**
     * Converts a given CommentedConfigurationNode to a List of Objects.
     * If the node is null, virtual, or not a list type, an empty list is returned.
     * This method traverses the node's children, converting any map-type children
     * to a map using the asMap method, and recursively converting any list-type children
     * to a list. Raw values are directly added to the resulting list.
     *
     * @param node the CommentedConfigurationNode to convert, which may contain nested structures
     * @return a List of Objects representing the contents of the node. If the node is null,
     *         virtual, or not a list, an empty list is returned
     */
    static List<Object> asList(CommentedConfigurationNode node) {
        if (node == null || node.virtual() || !node.isList()) return List.of();

        List<Object> out = new ArrayList<>();
        for (CommentedConfigurationNode child : node.childrenList()) {
            if (child.isMap()) out.add(asMap(child));
            else if (child.isList()) out.add(asList(child));
            else out.add(child.raw());
        }
        return out;
    }
}
