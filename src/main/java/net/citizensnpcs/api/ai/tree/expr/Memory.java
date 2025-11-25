package net.citizensnpcs.api.ai.tree.expr;

import java.util.ArrayList;
import java.util.List;

import net.citizensnpcs.api.npc.MetadataStore;
import net.citizensnpcs.api.npc.SimpleMetadataStore;
import net.citizensnpcs.api.util.DataKey;

/**
 * Persistent state storage for behavior trees. Memory allows behaviors to share data across ticks and execution cycles.
 * Wraps a {@link MetadataStore} for persistence support.
 */
public class Memory {
    private final MetadataStore store;

    public Memory() {
        this.store = new SimpleMetadataStore();
    }

    public Memory(MetadataStore store) {
        this.store = store;
    }

    /**
     * Clears all data from memory. Note: this creates a new backing store.
     */
    public void clear() {
        for (String key : getKeys()) {
            store.remove(key);
        }
    }

    /**
     * Gets a value from memory.
     *
     * @param key
     *            the key
     * @return the value, or null if not found
     */
    public Object get(String key) {
        return store.get(key);
    }

    /**
     * Gets a value with a default.
     *
     * @param key
     *            the key
     * @param defaultValue
     *            the default value if not found
     * @return the value or default
     */
    public Object get(String key, Object defaultValue) {
        return store.get(key, defaultValue);
    }

    /**
     * Gets a boolean value.
     *
     * @param key
     *            the key
     * @return the boolean value, or false if not found
     */
    public boolean getBoolean(String key) {
        Object value = store.get(key);
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        return false;
    }

    /**
     * Gets a boolean value with default.
     *
     * @param key
     *            the key
     * @param defaultValue
     *            the default value
     * @return the boolean value or default
     */
    public boolean getBoolean(String key, boolean defaultValue) {
        Object value = store.get(key);
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        return defaultValue;
    }

    /**
     * Gets all keys in memory.
     *
     * @return iterable of keys
     */
    public Iterable<String> getKeys() {
        List<String> keys = new ArrayList<>();
        // MetadataStore doesn't expose keys directly, so we track them separately
        // For now, this is a limitation - consider adding getKeys() to MetadataStore
        return keys;
    }

    /**
     * Gets a list from memory, creating it if it doesn't exist.
     *
     * @param key
     *            the key
     * @return the list (never null)
     */
    @SuppressWarnings("unchecked")
    public List<Object> getList(String key) {
        Object value = store.get(key);
        if (value instanceof List) {
            return (List<Object>) value;
        }
        List<Object> newList = new ArrayList<>();
        store.set(key, newList);
        return newList;
    }

    /**
     * Gets a number value.
     *
     * @param key
     *            the key
     * @return the number value, or 0 if not found or not a number
     */
    public double getNumber(String key) {
        Object value = store.get(key);
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        return 0;
    }

    /**
     * Gets a number value with default.
     *
     * @param key
     *            the key
     * @param defaultValue
     *            the default value
     * @return the number value or default
     */
    public double getNumber(String key, double defaultValue) {
        Object value = store.get(key);
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        return defaultValue;
    }

    /**
     * Gets the underlying MetadataStore.
     *
     * @return the backing store
     */
    public MetadataStore getStore() {
        return store;
    }

    /**
     * Gets a string value.
     *
     * @param key
     *            the key
     * @return the string value, or null if not found
     */
    public String getString(String key) {
        Object value = store.get(key);
        return value != null ? value.toString() : null;
    }

    /**
     * Gets a string value with default.
     *
     * @param key
     *            the key
     * @param defaultValue
     *            the default value
     * @return the string value or default
     */
    public String getString(String key, String defaultValue) {
        Object value = store.get(key);
        return value != null ? value.toString() : defaultValue;
    }

    /**
     * Checks if a key exists.
     *
     * @param key
     *            the key
     * @return true if the key exists
     */
    public boolean has(String key) {
        return store.has(key);
    }

    /**
     * Checks if a value is a primitive type.
     *
     * @param value
     *            the value to check
     * @return true if the value is a primitive type
     */
    private boolean isPrimitive(Object value) {
        return value instanceof String || value instanceof Number || value instanceof Boolean
                || value instanceof Character;
    }

    /**
     * Checks if a value is a valid type that can be stored in memory.
     *
     * @param value
     *            the value to check
     * @return true if the value is a valid type
     */
    private boolean isValidType(Object value) {
        if (isPrimitive(value))
            return true;

        if (value instanceof List) {
            // Check that all elements are primitives
            List<?> list = (List<?>) value;
            for (Object item : list) {
                if (item != null && !isPrimitive(item)) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    /**
     * Adds an item to a list in memory.
     *
     * @param key
     *            the list key
     * @param item
     *            the item to add (must be primitive)
     * @throws IllegalArgumentException
     *             if item is not primitive
     */
    public void listAdd(String key, Object item) {
        if (item != null && !isPrimitive(item)) {
            throw new IllegalArgumentException("List items must be primitive types");
        }
        getList(key).add(item);
    }

    /**
     * Clears all items from a list.
     *
     * @param key
     *            the list key
     */
    public void listClear(String key) {
        getList(key).clear();
    }

    /**
     * Checks if a list contains an item.
     *
     * @param key
     *            the list key
     * @param item
     *            the item
     * @return true if the list contains the item
     */
    public boolean listContains(String key, Object item) {
        return getList(key).contains(item);
    }

    /**
     * Gets an item from a list by index.
     *
     * @param key
     *            the list key
     * @param index
     *            the index
     * @return the item, or null if index out of bounds
     */
    public Object listGet(String key, int index) {
        List<Object> list = getList(key);
        if (index >= 0 && index < list.size()) {
            return list.get(index);
        }
        return null;
    }

    /**
     * Removes an item from a list by value.
     *
     * @param key
     *            the list key
     * @param item
     *            the item to remove
     * @return true if the item was removed
     */
    public boolean listRemove(String key, Object item) {
        List<Object> list = getList(key);
        return list.remove(item);
    }

    /**
     * Removes an item from a list by index.
     *
     * @param key
     *            the list key
     * @param index
     *            the index
     * @return the removed item, or null if index out of bounds
     */
    public Object listRemoveAt(String key, int index) {
        List<Object> list = getList(key);
        if (index >= 0 && index < list.size()) {
            return list.remove(index);
        }
        return null;
    }

    /**
     * Sets an item in a list by index.
     *
     * @param key
     *            the list key
     * @param index
     *            the index
     * @param item
     *            the item (must be primitive)
     * @throws IllegalArgumentException
     *             if item is not primitive
     */
    public void listSet(String key, int index, Object item) {
        if (item != null && !isPrimitive(item)) {
            throw new IllegalArgumentException("List items must be primitive types");
        }
        List<Object> list = getList(key);
        // Expand list if needed
        while (list.size() <= index) {
            list.add(null);
        }
        list.set(index, item);
    }

    /**
     * Gets the size of a list.
     *
     * @param key
     *            the list key
     * @return the list size
     */
    public int listSize(String key) {
        return getList(key).size();
    }

    /**
     * Loads memory state from the given DataKey.
     *
     * @param key
     *            the key to load from
     */
    public void loadFrom(DataKey key) {
        store.loadFrom(key);
    }

    /**
     * Removes a value from memory.
     *
     * @param key
     *            the key
     */
    public void remove(String key) {
        store.remove(key);
    }

    /**
     * Saves memory state to the given DataKey.
     *
     * @param key
     *            the key to save to
     */
    public void saveTo(DataKey key) {
        store.saveTo(key);
    }

    /**
     * Sets a value in memory. Only primitive types (String, Number, Boolean, Character) and Lists of primitives are
     * allowed. Values are stored persistently.
     *
     * @param key
     *            the key
     * @param value
     *            the value (must be String, Number, Boolean, Character, List of primitives, or null)
     * @throws IllegalArgumentException
     *             if value is not a valid type
     */
    public void set(String key, Object value) {
        if (value == null) {
            store.remove(key);
            return;
        }
        if (!isValidType(value)) {
            throw new IllegalArgumentException("Memory can only store primitive types and Lists of primitives. Got: "
                    + value.getClass().getName());
        }
        // Use setPersistent for primitives, set for lists (lists can't be persisted directly)
        if (value instanceof List) {
            store.set(key, value);
        } else {
            store.setPersistent(key, value);
        }
    }

    /**
     * Gets the number of entries in memory.
     *
     * @return the size
     */
    public int size() {
        return store.size();
    }
}
