package net.citizensnpcs.api.util;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import com.google.common.collect.Iterables;

/**
 * A DataKey implementation that uses raw Map&lt;String, Object&gt; for storage instead of Bukkit's
 * ConfigurationSection. This preserves literal key names containing dots instead of interpreting them as path
 * separators.
 */
public class MemoryDataKey extends DataKey {
    private final String name;
    private final Map<String, Object> root;

    public MemoryDataKey() {
        this(new HashMap<>(), "", "");
    }

    public MemoryDataKey(Map<String, Object> root) {
        this(root, "", "");
    }

    private MemoryDataKey(Map<String, Object> root, String path, String name) {
        super(path);
        this.root = root;
        this.name = name;
    }

    @Override
    public MemoryDataKey copy() {
        Map<String, Object> copied = deepCopyMap(getCurrentSection());
        return new MemoryDataKey(copied, "", "");
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> deepCopyMap(Map<String, Object> source) {
        Map<String, Object> result = new HashMap<>();
        for (Map.Entry<String, Object> entry : source.entrySet()) {
            Object value = entry.getValue();
            if (value instanceof Map) {
                result.put(entry.getKey(), deepCopyMap((Map<String, Object>) value));
            } else {
                result.put(entry.getKey(), value);
            }
        }
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;
        MemoryDataKey other = (MemoryDataKey) obj;
        return Objects.equals(path, other.path) && root == other.root;
    }

    @Override
    public boolean getBoolean(String key) {
        Object value = getRaw(key);
        if (value instanceof Boolean)
            return (Boolean) value;
        if (value != null)
            return Boolean.parseBoolean(value.toString());
        return false;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> getCurrentSection() {
        if (path.isEmpty())
            return root;

        Map<String, Object> current = root;
        String[] segments = path.split("\\.");
        for (String segment : segments) {
            Object next = current.get(segment);
            if (!(next instanceof Map))
                return Collections.emptyMap();
            current = (Map<String, Object>) next;
        }
        return current;
    }

    @Override
    public double getDouble(String key) {
        return getDouble(key, 0);
    }

    @Override
    public double getDouble(String key, double def) {
        Object value = getRaw(key);
        if (value instanceof Number)
            return ((Number) value).doubleValue();
        if (value != null) {
            String raw = value.toString();
            if (!raw.isEmpty()) {
                try {
                    return Double.parseDouble(raw);
                } catch (NumberFormatException e) {
                    return def;
                }
            }
        }
        return def;
    }

    @Override
    public DataKey getFromRoot(String path) {
        return navigatePath(root, "", path);
    }

    @Override
    public int getInt(String key) {
        return getInt(key, 0);
    }

    @Override
    public int getInt(String key, int def) {
        Object value = getRaw(key);
        if (value instanceof Number)
            return ((Number) value).intValue();
        if (value != null) {
            String raw = value.toString();
            if (!raw.isEmpty()) {
                try {
                    return Integer.parseInt(raw);
                } catch (NumberFormatException e) {
                    return def;
                }
            }
        }
        return def;
    }

    @Override
    public long getLong(String key) {
        return getLong(key, 0);
    }

    @Override
    public long getLong(String key, long def) {
        Object value = getRaw(key);
        if (value instanceof Number)
            return ((Number) value).longValue();
        if (value != null) {
            String raw = value.toString();
            if (!raw.isEmpty()) {
                try {
                    return Long.parseLong(raw);
                } catch (NumberFormatException e) {
                    return def;
                }
            }
        }
        return def;
    }

    @Override
    public Object getRaw(String key) {
        if (key == null || key.isEmpty())
            return getValueAtCurrentPath();
        return navigateToValue(key);
    }

    @Override
    public MemoryDataKey getRelative(String relative) {
        if (relative == null || relative.isEmpty())
            return this;
        return navigatePath(root, path, relative);
    }

    /**
     * Returns the root map backing this DataKey.
     */
    public Map<String, Object> getRootMap() {
        return root;
    }

    @Override
    public String getString(String key) {
        Object val = getRaw(key);
        if (val != null && !(val instanceof Map))
            return val.toString();
        return "";
    }

    @Override
    public Iterable<DataKey> getSubKeys() {
        Map<String, Object> current = getCurrentSection();
        if (current == null || current.isEmpty())
            return Collections.emptyList();

        return Iterables.transform(current.keySet(), k -> {
            String newPath = path.isEmpty() ? k : path + "." + k;
            return new MemoryDataKey(root, newPath, k);
        });
    }

    @SuppressWarnings("unchecked")
    private Object getValueAtCurrentPath() {
        if (path.isEmpty())
            return root;

        Map<String, Object> current = root;
        String[] segments = path.split("\\.");
        for (int i = 0; i < segments.length - 1; i++) {
            Object next = current.get(segments[i]);
            if (!(next instanceof Map))
                return null;
            current = (Map<String, Object>) next;
        }
        return current.get(segments[segments.length - 1]);
    }

    @Override
    public Map<String, Object> getValuesDeep() {
        Map<String, Object> section = getCurrentSection();
        if (section == null)
            return Collections.emptyMap();
        return deepCopyMap(section);
    }

    @Override
    public int hashCode() {
        return 31 * (path == null ? 0 : path.hashCode()) + System.identityHashCode(root);
    }

    @Override
    public boolean keyExists(String key) {
        if (key == null || key.isEmpty()) {
            return !getCurrentSection().isEmpty();
        }
        return navigateToValue(key) != null;
    }

    @Override
    public String name() {
        return name;
    }

    private MemoryDataKey navigatePath(Map<String, Object> root, String currentPath, String relativePath) {
        String newPath;
        if (currentPath.isEmpty()) {
            newPath = relativePath;
        } else if (relativePath.startsWith(".")) {
            newPath = currentPath + relativePath;
        } else {
            newPath = currentPath + "." + relativePath;
        }
        int lastSegment = relativePath.lastIndexOf('.');
        return new MemoryDataKey(root, newPath,
                lastSegment == -1 ? relativePath : relativePath.substring(lastSegment + 1));
    }

    @SuppressWarnings("unchecked")
    private Object navigateToValue(String relativePath) {
        Map<String, Object> current = getCurrentSection();
        if (current == null)
            return null;

        String[] segments = relativePath.split("\\.");
        for (int i = 0; i < segments.length - 1; i++) {
            Object next = current.get(segments[i]);
            if (!(next instanceof Map))
                return null;
            current = (Map<String, Object>) next;
        }
        return current.get(segments[segments.length - 1]);
    }

    @Override
    public void removeKey(String key) {
        setRaw(key, null);
    }

    @Override
    public void setBoolean(String key, boolean value) {
        setRaw(key, value);
    }

    @Override
    public void setDouble(String key, double value) {
        setRaw(key, value);
    }

    @Override
    public void setInt(String key, int value) {
        setRaw(key, value);
    }

    @Override
    public void setLong(String key, long value) {
        setRaw(key, value);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void setRaw(String key, Object value) {
        // Build full path from root to handle non-existent intermediate paths
        String fullPath;
        if (key == null || key.isEmpty()) {
            fullPath = path;
        } else if (path.isEmpty()) {
            fullPath = key;
        } else {
            fullPath = path + "." + key;
        }

        if (fullPath.isEmpty()) {
            // Setting at root level
            if (value == null) {
                root.clear();
            } else if (value instanceof Map) {
                root.clear();
                root.putAll((Map<String, Object>) value);
            }
            return;
        }

        String[] segments = fullPath.split("\\.");
        Map<String, Object> current = root;

        // Navigate/create path to parent
        for (int i = 0; i < segments.length - 1; i++) {
            Object next = current.get(segments[i]);
            if (!(next instanceof Map)) {
                if (value == null)
                    return; // Nothing to remove
                next = new HashMap<String, Object>();
                current.put(segments[i], next);
            }
            current = (Map<String, Object>) next;
        }

        // Set or remove the value
        String lastSegment = segments[segments.length - 1];
        if (value == null) {
            current.remove(lastSegment);
        } else {
            current.put(lastSegment, value);
        }
    }

    @Override
    public void setString(String key, String value) {
        setRaw(key, value);
    }

    @Override
    public String toString() {
        return "MemoryDataKey[" + path + "]";
    }
}
