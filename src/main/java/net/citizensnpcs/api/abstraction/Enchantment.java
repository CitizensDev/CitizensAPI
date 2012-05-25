package net.citizensnpcs.api.abstraction;

public interface Enchantment {
    boolean canEnchantItem(ItemStack res);

    int getId();

    int getMaxLevel();
}
