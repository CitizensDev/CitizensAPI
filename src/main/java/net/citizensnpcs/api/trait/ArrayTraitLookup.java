package net.citizensnpcs.api.trait;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class ArrayTraitLookup implements TraitLookup {
    private final long[] sig;
    private final Trait[] traits;

    public ArrayTraitLookup(int capacity) {
        int cap = roundUpPow2(capacity);
        traits = new Trait[cap];
        sig = new long[(cap + 63) >>> 6];
    }

    @Override
    public void add(int typeId, Trait trait) {
        traits[typeId] = trait;
        sig[typeId >>> 6] |= (1L << (typeId & 63));
    }

    @Override
    public void clear() {
        for (int i = 0; i < traits.length; i++) {
            traits[i] = null;
        }
        for (int i = 0; i < sig.length; i++) {
            sig[i] = 0;
        }
    }

    @Override
    public void forEach(Consumer<Trait> visitor) {
        for (int w = 0; w < sig.length; w++) {
            long bits = sig[w];
            while (bits != 0L) {
                int bit = Long.numberOfTrailingZeros(bits);
                int typeId = (w << 6) + bit;

                visitor.accept(traits[typeId]);

                bits &= bits - 1;
            }
        }
    }

    @Override
    public Trait get(int typeId) {
        return traits[typeId];
    }

    @Override
    public boolean has(int typeId) {
        return (sig[typeId >>> 6] & (1L << (typeId & 63))) != 0;
    }

    @Override
    public Iterable<Trait> list() {
        List<Trait> list = new ArrayList<>();
        forEach(list::add);
        return list;
    }

    @Override
    public Trait remove(int typeId) {
        Trait trait = traits[typeId];
        sig[typeId >>> 6] &= ~(1L << (typeId & 63));
        return trait;
    }

    private int roundUpPow2(int x) {
        int n = 1;
        while (n < x)
            n <<= 1;
        return n;
    }
}
