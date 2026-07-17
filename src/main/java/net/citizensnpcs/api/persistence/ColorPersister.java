package net.citizensnpcs.api.persistence;

import org.bukkit.Color;

import net.citizensnpcs.api.util.DataKey;

public class ColorPersister implements Persister<Color> {
    @Override
    public Color create(DataKey root) {
        return Color.fromARGB(root.getInt("", 0));
    }

    @Override
    public void save(Color color, DataKey root) {
        root.setInt("", color.asARGB());
    }
}
