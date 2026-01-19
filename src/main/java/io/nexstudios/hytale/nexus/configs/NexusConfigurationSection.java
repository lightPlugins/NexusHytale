package io.nexstudios.hytale.nexus.configs;

import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;

import java.util.*;

/**
 * Represents a section within a Nexus configuration, providing methods for interacting with
 * configuration nodes, retrieving values, and managing subsections.
 * <p>
 * This class encapsulates logic for accessing configuration elements with various data types,
 * creating new sections, and retrieving structured data from nested nodes.
 */
public record NexusConfigurationSection(NexusFileConfiguration config, CommentedConfigurationNode section) {

    /**
     * Constructs a new instance of the NexusConfigurationSection class.
     *
     * @param config The NexusFileConfiguration instance that this section belongs to; must not be null.
     * @param section The underlying CommentedConfigurationNode representing this section; must not be null.
     */
    public NexusConfigurationSection(NexusFileConfiguration config, CommentedConfigurationNode section) {
        this.config = Objects.requireNonNull(config, "config");
        this.section = Objects.requireNonNull(section, "section");
    }

    /**
     * Determines if the current configuration section represents a valid map-like structure.
     *
     * @return true if the section is a map, false otherwise.
     */
    public boolean isSection() {
        return section.isMap();
    }

    /**
     * Checks if the current configuration section represents a list-like structure.
     *
     * @return true if the section is a list, false otherwise.
     */
    public boolean isList() {
        return section.isList();
    }

    /**
     * Checks whether the specified path exists and does not represent a virtual (non-existent) node
     * in the underlying configuration section.
     *
     * @param path The path to check within the configuration section; must not be null.
     * @return true if the path exists and is not virtual, false otherwise.
     */
    public boolean contains(String path) {
        return !node(path).virtual();
    }

    /**
     * Retrieves the string value associated with the specified path in the configuration.
     * If the path does not exist or the value is not a string, the default value is returned.
     *
     * @param path The path within the configuration to retrieve the string value; must not be null.
     * @param def The default value to return if the specified path is unavailable or invalid.
     * @return The string value at the specified path, or the provided default value if the
     *         path does not exist or is not a string.
     */
    public String getString(String path, String def) {
        return node(path).getString(def);
    }

    /**
     * Retrieves the integer value associated with the specified path in the configuration.
     * If the path does not exist or the value is not an integer, the default value is returned.
     *
     * @param path The path within the configuration to retrieve the integer value; must not be null.
     * @param def The default value to return if the specified path is unavailable or invalid.
     * @return The integer value at the specified path, or the provided default value if the
     *         path does not exist or is not an integer.
     */
    public int getInt(String path, int def) {
        return node(path).getInt(def);
    }

    /**
     * Retrieves the boolean value associated with the specified path in the configuration.
     * If the path does not exist or the value is not a boolean, the default value is returned.
     *
     * @param path The path within the configuration to retrieve the boolean value; must not be null.
     * @param def The default value to return if the specified path is unavailable or invalid.
     * @return The boolean value at the specified path, or the provided default value if the
     *         path does not exist or is not a boolean.
     */
    public boolean getBoolean(String path, boolean def) {
        return node(path).getBoolean(def);
    }

    /**
     * Retrieves the double value associated with the specified path in the configuration.
     * If the path does not exist or the value is not a double, the default value is returned.
     *
     * @param path The path within the configuration to retrieve the double value; must not be null.
     * @param def The default value to return if the specified path is unavailable or invalid.
     * @return The double value at the specified path, or the provided default value if the
     *         path does not exist or is not a double.
     */
    public double getDouble(String path, double def) {
        return node(path).getDouble(def);
    }

    /**
     * Retrieves a list of string values from the configuration located at the specified path.
     * If the path does not exist or the values cannot be deserialized as a list of strings, an empty list is returned.
     *
     * @param path The path within the configuration to retrieve the list of strings; must not be null.
     * @return A list of strings at the specified path, or an empty list if the path does not exist
     *         or the values are not a valid list of strings.
     */
    public List<String> getStringList(String path) {
        try {
            return node(path).getList(String.class, List.of());
        } catch (SerializationException e) {
            return List.of();
        }
    }

    /**
     * Retrieves a list of {@code NexusConfigurationSection} instances from the specified path
     * if the underlying node represents a valid list structure. If the node at the given path
     * is virtual or does not represent a list, an empty list is returned.
     *
     * @param path The path to the configuration node to retrieve the list from; must not be null.
     * @return A list of {@code NexusConfigurationSection} instances, or an empty list if the
     *         specified path does not exist or is not a valid list.
     */
    public List<NexusConfigurationSection> getSectionList(String path) {
        CommentedConfigurationNode n = node(path);
        if (n.virtual() || !n.isList()) return List.of();

        List<NexusConfigurationSection> out = new ArrayList<>();
        for (CommentedConfigurationNode child : n.childrenList()) {
            out.add(new NexusConfigurationSection(config, child));
        }
        return out;
    }

    /**
     * Retrieves a list of maps from the configuration at the specified path. Each map in the list
     * represents a child node of the configuration section if the underlying node is a valid list.
     * If the node at the given path is virtual or does not represent a list, an empty list is returned.
     *
     * @param path The path to the configuration node to retrieve the list of maps from; must not be null.
     * @return A list of maps where each map corresponds to a child node's key-value pairs,
     *         or an empty list if the path does not exist or is not a valid list.
     */
    public List<Map<String, Object>> getMapList(String path) {
        CommentedConfigurationNode n = node(path);
        if (n.virtual() || !n.isList()) return List.of();

        List<Map<String, Object>> out = new ArrayList<>();
        for (CommentedConfigurationNode child : n.childrenList()) {
            out.add(NexusFileConfiguration.asMap(child));
        }
        return out;
    }

    /**
     * Sets the value at the specified configuration path.
     * If the path does not exist, it will be created.
     * An exception is thrown if the value cannot be serialized.
     *
     * @param path The configuration path where the value is to be set; must not be null.
     * @param value The value to set at the specified path; can be any serializable object.
     * @throws RuntimeException if the value cannot be serialized or the path setting fails.
     */
    public void set(String path, Object value) {
        try {
            node(path).set(value);
        } catch (SerializationException e) {
            throw new RuntimeException("Konnte Wert nicht setzen: " + path, e);
        }
    }

    /**
     * Retrieves a configuration section for the specified path. If the path does not represent a valid
     * map-like structure or is virtual (non-existent), this method returns null.
     *
     * @param path The path within the configuration to retrieve the section; must not be null.
     * @return A {@code NexusConfigurationSection} instance representing the configuration section at the specified path,
     *         or null if the path does not exist, is virtual, or is not a valid map-like structure.
     */
    public NexusConfigurationSection getConfigurationSection(String path) {
        CommentedConfigurationNode n = node(path);
        if (n.virtual() || !n.isMap()) return null;
        return new NexusConfigurationSection(config, n);
    }

    /**
     * Creates a new configuration section at the specified path.
     * If the path does not exist or does not represent a map-like structure,
     * a new map will be created at that location.
     *
     * @param path The path where the new configuration section should be created; must not be null.
     * @return A {@code NexusConfigurationSection} instance representing the newly created section.
     * @throws RuntimeException if the section cannot be created due to a serialization error.
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
        return new NexusConfigurationSection(config, n);
    }

    /**
     * Retrieves the keys from the configuration section. If the specified parameter
     * {@code deep} is true, it will include keys from nested sections as dot-separated values.
     *
     * @param deep if {@code true}, the method will include keys from nested maps,
     *             represented in dot-notation format. If {@code false}, only the top-level keys
     *             will be retrieved.
     * @return a set of strings representing the keys in the configuration section. If the
     *         section is not a map, an empty set is returned.
     */
    public Set<String> getKeys(boolean deep) {
        if (!section.isMap()) return Set.of();

        Set<String> keys = new LinkedHashSet<>();
        for (var entry : section.childrenMap().entrySet()) {
            String key = String.valueOf(entry.getKey());
            keys.add(key);

            if (deep) {
                CommentedConfigurationNode child = entry.getValue();
                if (child.isMap()) {
                    NexusConfigurationSection childSection = new NexusConfigurationSection(config, child);
                    for (String sub : childSection.getKeys(true)) {
                        keys.add(key + "." + sub);
                    }
                }
            }
        }
        return keys;
    }

    /**
     * Retrieves all key-value pairs from the configuration section as a map. If the deep parameter
     * is true, the method recursively retrieves values from nested maps and includes them in the result.
     *
     * @param deep if true, retrieves values from nested sections and includes them with dot-separated keys.
     * @return a map of key-value pairs representing the configuration data; an empty map is returned if the section is not a map.
     */
    public Map<String, Object> getValues(boolean deep) {
        if (!section.isMap()) return Map.of();

        Map<String, Object> out = new LinkedHashMap<>();
        for (var entry : section.childrenMap().entrySet()) {
            String key = String.valueOf(entry.getKey());
            CommentedConfigurationNode child = entry.getValue();

            if (deep && child.isMap()) {
                NexusConfigurationSection childSection = new NexusConfigurationSection(config, child);
                Map<String, Object> sub = childSection.getValues(true);
                for (var subEntry : sub.entrySet()) {
                    out.put(key + "." + subEntry.getKey(), subEntry.getValue());
                }
            } else if (deep && child.isList()) {
                out.put(key, NexusFileConfiguration.asList(child));
            } else {
                out.put(key, child.raw());
            }
        }
        return out;
    }

    /**
     * Retrieves a sub-node from the configuration based on the provided path.
     *
     * @param path the path to the desired sub-node, as a dot-separated string
     * @return the sub-node located at the specified path
     */
    public CommentedConfigurationNode node(String path) {
        return section.node((Object[]) NexusFileConfiguration.splitPath(path));
    }

    /**
     * Retrieves the raw configuration node associated with the current instance.
     *
     * @return the CommentedConfigurationNode representing the raw unprocessed node.
     */
    public CommentedConfigurationNode rawNode() {
        return section;
    }
}
