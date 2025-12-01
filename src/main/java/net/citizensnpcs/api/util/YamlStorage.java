package net.citizensnpcs.api.util;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ForkJoinPool;

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

public class YamlStorage implements Storage {
    private final Map<String, Object> data = new LinkedHashMap<>();
    private final File file;
    private final String header;
    private final boolean transformLists;

    public YamlStorage(File file) {
        this(file, null);
    }

    public YamlStorage(File file, String header) {
        this(file, header, true);
    }

    public YamlStorage(File file, String header, boolean transformLists) {
        this.file = file;
        this.header = header;
        this.transformLists = transformLists;
        if (!file.exists()) {
            try {
                file.getParentFile().mkdirs();
                file.createNewFile();
            } catch (IOException e) {
                Messaging.severe("Could not create file: " + file.getName());
            }
            save();
        }
    }

    @SuppressWarnings("unchecked")
    private Object deepCopy(Object source) {
        if (source instanceof Map) {
            Map<String, Object> result = new LinkedHashMap<>();
            for (Map.Entry<String, Object> e : ((Map<String, Object>) source).entrySet()) {
                result.put(e.getKey(), deepCopy(e.getValue()));
            }
            return result;
        }
        if (source instanceof List) {
            List<Object> result = new ArrayList<>();
            for (Object item : (List<?>) source) {
                result.add(deepCopy(item));
            }
            return result;
        }
        return source;
    }

    @SuppressWarnings("unchecked")
    private void doSave(boolean async) {
        Map<String, Object> toSave = (Map<String, Object>) deepCopy(data);
        if (transformLists) {
            transformMapsToLists(toSave);
        }
        DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        options.setPrettyFlow(true);
        Yaml yaml = new Yaml(options);

        Runnable task = () -> {
            try (FileWriter writer = new FileWriter(file)) {
                if (header != null && !header.isEmpty()) {
                    writer.write("# " + header + "\n");
                }
                yaml.dump(toSave, writer);
            } catch (IOException e) {
                e.printStackTrace();
            }
        };
        if (async) {
            ForkJoinPool.commonPool().submit(task);
        } else {
            task.run();
        }
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof YamlStorage && Objects.equals(file, ((YamlStorage) obj).file);
    }

    @Override
    public DataKey getKey(String root) {
        return new MemoryDataKey(data).getRelative(root);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(file);
    }

    private boolean isSequentialIntKeys(Map<String, ?> map) {
        if (map.isEmpty())
            return false;
        int i = 0;
        for (String key : map.keySet()) {
            try {
                if (Integer.parseInt(key) != i++)
                    return false;
            } catch (NumberFormatException e) {
                return false;
            }
        }
        return true;
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean load() {
        try (FileReader reader = new FileReader(file)) {
            Object loaded = new Yaml().load(reader);
            data.clear();
            if (loaded instanceof Map) {
                data.putAll((Map<String, Object>) deepCopy(loaded));
            }
            if (transformLists) {
                transformListsToMaps(data);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public void save() {
        doSave(false);
    }

    @Override
    public void saveAsync() {
        doSave(true);
    }

    @Override
    public String toString() {
        return "YamlStorage{file=" + file + "}";
    }

    @SuppressWarnings("unchecked")
    private void transformListsToMaps(Map<String, Object> map) {
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            Object value = entry.getValue();
            if (value instanceof Collection) {
                Map<String, Object> indexed = new LinkedHashMap<>();
                int i = 0;
                for (Object item : (Collection<?>) value) {
                    Object copied = deepCopy(item);
                    indexed.put(String.valueOf(i++), copied);
                    if (copied instanceof Map) {
                        transformListsToMaps((Map<String, Object>) copied);
                    }
                }
                entry.setValue(indexed);
            } else if (value instanceof Map) {
                Map<String, Object> mutable = new LinkedHashMap<>((Map<String, Object>) value);
                entry.setValue(mutable);
                transformListsToMaps(mutable);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void transformMapsToLists(Map<String, Object> map) {
        for (Map.Entry<String, Object> entry : new ArrayList<>(map.entrySet())) {
            if (!(entry.getValue() instanceof Map))
                continue;
            Map<String, Object> child = (Map<String, Object>) entry.getValue();
            transformMapsToLists(child);
            if (!isSequentialIntKeys(child))
                continue;
            List<Object> list = new ArrayList<>(child.size());
            for (int i = 0; i < child.size(); i++) {
                list.add(child.get(String.valueOf(i)));
            }
            entry.setValue(list);
        }
    }
}
