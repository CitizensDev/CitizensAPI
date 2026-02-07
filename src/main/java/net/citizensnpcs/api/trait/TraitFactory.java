package net.citizensnpcs.api.trait;

import java.util.Collection;

public interface TraitFactory {
    int getId(Class<? extends Trait> clazz);

    /**
     * @return All currently registered traits, including <em>internal</em> traits.
     */
    Collection<TraitInfo> getRegisteredTraits();

    /**
     * Gets the {@link TraitTemplateParser} with the given trait name or null if not found.
     *
     * @param name
     * @return the trait template parser
     */
    TraitTemplateParser getTemplateParser(String name);

    /**
     * Gets a trait with the given class.
     *
     * @param clazz
     *            Class of the trait
     * @return Trait with the given class
     */
    <T extends Trait> T getTrait(Class<T> clazz);

    /**
     * Gets a trait with the given name.
     *
     * @param name
     *            Name of the trait
     * @return Trait with the given name
     */
    <T extends Trait> T getTrait(String name);

    /**
     * Gets the {@link Trait} class with the given name, or null if not found.
     *
     * @param name
     *            The trait name
     * @return The trait class
     */
    Class<? extends Trait> getTraitClass(String name);

    /**
     * Registers a trait using the given information.
     *
     * @param info
     *            Registration information
     */
    void registerTrait(TraitInfo info);
}