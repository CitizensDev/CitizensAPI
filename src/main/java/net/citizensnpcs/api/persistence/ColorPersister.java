package net.citizensnpcs.api.persistence;

import org.bukkit.Color;

import net.citizensnpcs.api.util.DataKey;

public class ColorPersister implements Persister<Color> {
    @Override
    public Color create(DataKey root) {
        return root.keyExists("") ? Color.fromARGB(root.getInt("")) : null;
    }

    @Override
    public void save(Color color, DataKey root) {
        root.setInt("", color.asARGB());
    }
}
