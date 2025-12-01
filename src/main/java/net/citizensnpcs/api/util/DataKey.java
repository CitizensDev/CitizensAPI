package net.citizensnpcs.api.util;

import java.util.Map;
import java.util.Objects;

import org.bukkit.configuration.ConfigurationSection;

import com.google.common.collect.Iterables;
import com.google.common.primitives.Ints;

/**
 * A hierarchical abstract storage class. Similar to Bukkit's {@link ConfigurationSection}.
 */
public abstract class DataKey {
    protected final String path;

    protected DataKey(String path) {
        this.path = path;
    }

    public abstract DataKey copy();

    protected String createRelativeKey(String from) {
        if (from.isEmpty())
            return path;
        if (from.charAt(0) == '.')
            return path.isEmpty() ? from.substring(1) : path + from;
        return path.isEmpty() ? from : path + '.' + from;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;
        DataKey other = (DataKey) obj;
        if (!Objects.equals(path, other.path))
            return false;
        return true;
    }

    public abstract boolean getBoolean(String key);

    public boolean getBoolean(String key, boolean value) {
        if (keyExists(key))
            return getBoolean(key);
        setBoolean(key, value);
        return value;
    }

    public abstract double getDouble(String key);

    public double getDouble(String key, double value) {
        if (keyExists(key))
            return getDouble(key);
        setDouble(key, value);
        return value;
    }

    public abstract DataKey getFromRoot(String path);

    public abstract int getInt(String key);

    public int getInt(String key, int value) {
        if (keyExists(key))
            return getInt(key);
        setInt(key, value);
        return value;
    }

    public Iterable<DataKey> getIntegerSubKeys() {
        return Iterables.filter(getSubKeys(), k -> Ints.tryParse(k.name()) != null);
    }

    public abstract long getLong(String key);

    public long getLong(String key, long value) {
        if (keyExists(key))
            return getLong(key);
        setLong(key, value);
        return value;
    }

    public String getPath() {
        return path;
    }

    public abstract Object getRaw(String key);

    @SuppressWarnings("unchecked")
    public <T> T getRawUnchecked(String key) {
        return (T) getRaw(key);
    }

    public DataKey getRelative(int key) {
        return getRelative(Integer.toString(key));
    }

    public abstract DataKey getRelative(String relative);

    public abstract String getString(String key);

    public String getString(String key, String value) {
        if (keyExists(key))
            return getString(key);
        setString(key, value);
        return value;
    }

    public abstract Iterable<DataKey> getSubKeys();

    public abstract Map<String, Object> getValuesDeep();

    @Override
    public int hashCode() {
        return 31 + (path == null ? 0 : path.hashCode());
    }

    public boolean hasSubKeys() {
        return getSubKeys().iterator().hasNext();
    }

    public boolean keyExists() {
        return keyExists("");
    }

    public abstract boolean keyExists(String key);

    public abstract String name();

    public abstract void removeKey(String key);

    public abstract void setBoolean(String key, boolean value);

    public abstract void setDouble(String key, double value);

    public abstract void setInt(String key, int value);

    public abstract void setLong(String key, long value);

    public abstract void setMap(String key, Map<String, Object> value);

    public abstract void setRaw(String key, Object value);

    public abstract void setString(String key, String value);
}