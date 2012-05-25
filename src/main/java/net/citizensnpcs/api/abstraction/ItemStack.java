package net.citizensnpcs.api.abstraction;

import java.util.Map;

public interface ItemStack {
    void addEnchantments(Map<Enchantment, Integer> enchantments);

    int getAmount();

    short getDurability();

    int getEnchantmentLevel(Enchantment enchantment);

    Map<Enchantment, Integer> getEnchantments();

    Material getType();

    void setAmount(int amount);

    void setDurability(short durability);

    void setType(Material type);
}
