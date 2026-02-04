package net.citizensnpcs.api.trait;

import java.util.function.Consumer;

import it.unimi.dsi.fastutil.ints.Int2ReferenceOpenHashMap;

public class IntMapTraitLookup implements TraitLookup {
    private final Int2ReferenceOpenHashMap<Trait> traits = new Int2ReferenceOpenHashMap<>();

    @Override
    public void add(int typeId, Trait trait) {
        traits.put(typeId, trait);
    }

    @Override
    public void clear() {
        traits.clear();
    }

    @Override
    public void forEach(Consumer<Trait> visitor) {
        for (Trait trait : traits.values()) {
            if (trait != null) {
                visitor.accept(trait);
            }
        }
    }

    @Override
    public Trait get(int typeId) {
        return traits.get(typeId);
    }

    @Override
    public boolean has(int typeId) {
        return traits.containsKey(typeId);
    }

    @Override
    public Iterable<Trait> list() {
        return traits.values();
    }

    @Override
    public Trait remove(int typeId) {
        return traits.remove(typeId);
    }
}
