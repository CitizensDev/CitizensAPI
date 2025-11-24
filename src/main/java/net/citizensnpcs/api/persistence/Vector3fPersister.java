package net.citizensnpcs.api.persistence;

import org.joml.Vector3f;
import org.joml.Vector3fc;

import net.citizensnpcs.api.util.DataKey;

public class Vector3fPersister implements Persister<Vector3fc> {
    @Override
    public Vector3fc create(DataKey root) {
        return new Vector3f().set(root.getDouble("0"), root.getDouble("1"), root.getDouble("2"));
    }

    @Override
    public void save(Vector3fc instance, DataKey root) {
        root.setDouble("0", instance.x());
        root.setDouble("1", instance.y());
        root.setDouble("2", instance.z());
    }
}
