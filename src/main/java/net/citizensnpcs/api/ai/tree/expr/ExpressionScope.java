package net.citizensnpcs.api.ai.tree.expr;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import com.google.common.collect.Sets;

import net.citizensnpcs.api.npc.NPC;

/**
 * A variable scope for expression evaluation with lazy binding support. Variables are only computed when accessed and
 * cached for the evaluation cycle.
 */
public class ExpressionScope {
    private final Map<String, Object> cachedValues = new HashMap<>();
    private final Map<String, Object> eagerValues = new HashMap<>();
    private final Map<String, Supplier<?>> lazyBindings = new HashMap<>();
    private Memory memory;
    private NPC npc;
    private final ExpressionScope parent;

    public ExpressionScope() {
        this.parent = null;
    }

    public ExpressionScope(ExpressionScope parent) {
        this.parent = parent;
    }

    /**
     * Binds a lazily-evaluated variable. The supplier is called only when the variable is first accessed, and the
     * result is cached for the evaluation cycle.
     */
    public void bind(String name, Supplier<?> supplier) {
        lazyBindings.put(name, supplier);
    }

    /**
     * Creates a child scope that inherits from this scope.
     */
    public ExpressionScope createChild() {
        return new ExpressionScope(this);
    }

    /**
     * Gets a variable value, computing lazy bindings if necessary. Lookup order: eager values -> cached lazy values ->
     * compute lazy -> parent scope
     *
     * @param name
     *            the variable name
     * @return the value, or null if not found
     */
    public Object get(String name) {
        Object value = eagerValues.get(name);
        if (value != null)
            return value;

        value = cachedValues.get(name);
        if (value != null)
            return value;

        Supplier<?> supplier = lazyBindings.get(name);
        if (supplier != null) {
            value = supplier.get();
            cachedValues.put(name, value);
            return value;
        }
        if (parent != null)
            return parent.get(name);

        return null;
    }

    /**
     * Gets the memory instance for this scope or parent scope if not present.
     */
    public Memory getMemory() {
        if (memory != null)
            return memory;

        if (parent != null)
            return parent.getMemory();

        return null;
    }

    /**
     * Gets the NPC for this scope or parent NPC if not present.
     */
    public NPC getNPC() {
        if (npc != null)
            return npc;

        if (parent != null)
            return parent.getNPC();

        return null;
    }

    /**
     * Gets all variable names currently in scope.
     */
    public Iterable<String> getVariableNames() {
        Set<String> keys = Sets.newHashSet(eagerValues.keySet());
        keys.addAll(lazyBindings.keySet());
        return keys;
    }

    /**
     * Checks if a variable exists in this scope or parent scopes.
     *
     * @param name
     *            the variable name
     */
    public boolean has(String name) {
        if (eagerValues.containsKey(name) || lazyBindings.containsKey(name))
            return true;

        return parent != null && parent.has(name);
    }

    /**
     * Removes a variable from the scope.
     */
    public void remove(String name) {
        eagerValues.remove(name);
        lazyBindings.remove(name);
        cachedValues.remove(name);
    }

    /**
     * Clears cached lazy values. Call this between evaluation cycles to allow lazy values to be recomputed with fresh
     * data.
     */
    public void resetCache() {
        cachedValues.clear();
    }

    /**
     * Sets an eager (immediate) value for a variable.
     */
    public void set(String name, Object value) {
        eagerValues.put(name, value);
    }

    /**
     * Sets the memory instance for this scope.
     */
    public void setMemory(Memory memory) {
        this.memory = memory;
    }

    /**
     * Sets the NPC for this scope.
     */
    public void setNPC(NPC npc) {
        this.npc = npc;
    }
}
