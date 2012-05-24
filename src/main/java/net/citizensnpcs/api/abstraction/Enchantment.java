package net.citizensnpcs.api.abstraction;

public interface Enchantment {
    int getId();

    int getMaxLevel();

    boolean canEnchantItem(ItemStack res);
}
