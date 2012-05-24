package net.citizensnpcs.api.abstraction;

import java.util.Map;

public interface ItemStack {
    Material getType();

    int getAmount();

    short getDurability();

    void setAmount(int amount);

    void setType(Material type);

    void setDurability(short durability);

    Map<Enchantment, Integer> getEnchantments();

    int getEnchantmentLevel(Enchantment enchantment);

    void addEnchantments(Map<Enchantment, Integer> enchantments);
}
