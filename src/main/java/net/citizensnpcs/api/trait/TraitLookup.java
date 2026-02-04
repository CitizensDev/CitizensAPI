package net.citizensnpcs.api.trait;

import java.util.function.Consumer;

public interface TraitLookup {
    public void add(int typeId, Trait trait);

    public void clear();

    public void forEach(Consumer<Trait> visitor);

    public Trait get(int typeId);

    public boolean has(int typeId);

    public Iterable<Trait> list();

    public Trait remove(int typeId);
}
