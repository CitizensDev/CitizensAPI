package net.citizensnpcs.api.util;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.google.common.primitives.Doubles;
import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;

/**
 * A DataKey implementation that uses raw Map<String, Object> for storage instead of Bukkit's ConfigurationSection. This
 * preserves literal key names containing dots instead of interpreting them as path separators.
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
        return new MemoryDataKey(deepCopyMap(getCurrentSection()), "", "");
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> deepCopyMap(Map<String, Object> source) {
        Map<String, Object> result = Maps.newHashMapWithExpectedSize(source.size());
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
        return Objects.equals(getDisplayPath(), other.getDisplayPath()) && root == other.root;
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
        String[] segments = path.split("\\" + INTERNAL_SEPARATOR);
        for (String segment : segments) {
            Object next = current.get(segment);
            if (!(next instanceof Map))
                return Collections.emptyMap();
            current = (Map<String, Object>) next;
        }
        return current;
    }

    private String getDisplayPath() {
        return path.replace(INTERNAL_SEPARATOR, '.');
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
                Double res = Doubles.tryParse(raw);
                return res != null ? res : def;
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
                Integer res = Ints.tryParse(raw);
                return res != null ? res : def;
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
                Long res = Longs.tryParse(raw);
                return res != null ? res : def;
            }
        }
        return def;
    }

    @Override
    public String getPath() {
        return getDisplayPath();
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
            String newPath = path.isEmpty() ? k : path + INTERNAL_SEPARATOR + k;
            return new MemoryDataKey(root, newPath, k);
        });
    }

    @SuppressWarnings("unchecked")
    private Object getValueAtCurrentPath() {
        if (path.isEmpty())
            return root;

        Map<String, Object> current = root;
        String[] segments = path.split("\\" + INTERNAL_SEPARATOR);
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
        String displayPath = getDisplayPath();
        return 31 * (displayPath == null ? 0 : displayPath.hashCode()) + System.identityHashCode(root);
    }

    @Override
    public boolean hasSubKeys() {
        Map<String, Object> current = getCurrentSection();
        return current != null && !current.isEmpty();
    }

    @Override
    public boolean keyExists(String key) {
        if (key == null || key.isEmpty())
            return !getCurrentSection().isEmpty();

        return navigateToValue(key) != null;
    }

    @Override
    public String name() {
        return name;
    }

    private MemoryDataKey navigatePath(Map<String, Object> root, String currentInternalPath, String relativePath) {
        // relativePath uses dots as separators - split and add each as a segment
        String[] pathParts = relativePath.split("\\.");
        StringBuilder newInternalPath = new StringBuilder(currentInternalPath);

        for (String part : pathParts) {
            if (newInternalPath.length() > 0) {
                newInternalPath.append(INTERNAL_SEPARATOR);
            }
            newInternalPath.append(part);
        }
        String lastName = pathParts[pathParts.length - 1];
        return new MemoryDataKey(root, newInternalPath.toString(), lastName);
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
    public void setMap(String key, Map<String, Object> value) {
        setRaw(key, value);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void setRaw(String key, Object value) {
        String fullInternalPath;
        if (key == null || key.isEmpty()) {
            fullInternalPath = path;
        } else if (path.isEmpty()) {
            fullInternalPath = key.replace('.', INTERNAL_SEPARATOR);
        } else {
            fullInternalPath = path + INTERNAL_SEPARATOR + key.replace('.', INTERNAL_SEPARATOR);
        }
        if (fullInternalPath.isEmpty()) {
            if (value == null) {
                root.clear();
            } else if (value instanceof Map) {
                root.clear();
                root.putAll((Map<String, Object>) value);
            }
            throw new IllegalStateException("Unsupported root value");
        }
        String[] segments = fullInternalPath.split("\\" + INTERNAL_SEPARATOR);
        Map<String, Object> current = root;

        for (int i = 0; i < segments.length - 1; i++) {
            Object next = current.get(segments[i]);
            if (!(next instanceof Map)) {
                if (value == null)
                    return;
                next = new HashMap<String, Object>();
                current.put(segments[i], next);
            }
            current = (Map<String, Object>) next;
        }
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
        return "MemoryDataKey[" + getDisplayPath() + "]";
    }

    private static final char INTERNAL_SEPARATOR = '\0';
    private static final Splitter INTERNAL_SEPARATOR_SPLITTER = Splitter.on(INTERNAL_SEPARATOR);
}
