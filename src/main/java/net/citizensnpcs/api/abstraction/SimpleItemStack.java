/*
 * CitizensAPI
 * Copyright (C) 2012 CitizensDev <http://citizensnpcs.net>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package net.citizensnpcs.api.abstraction;

import java.util.Map;

import com.google.common.collect.Maps;

public class SimpleItemStack implements ItemStack {
    private int amount;
    private short durability;
    private final Map<Enchantment, Integer> enchantments = Maps.newHashMap();
    private Material type;

    public SimpleItemStack(Material type, int amount, short durability) {
        this.type = type;
        this.amount = amount;
        this.durability = durability;
    }

    @Override
    public void addEnchantments(Map<Enchantment, Integer> enchantments) {
        this.enchantments.putAll(enchantments);
    }

    @Override
    public int getAmount() {
        return amount;
    }

    @Override
    public short getDurability() {
        return durability;
    }

    @Override
    public int getEnchantmentLevel(Enchantment enchantment) {
        return enchantments.containsKey(enchantment) ? enchantments.get(enchantment) : -1;
    }

    @Override
    public Map<Enchantment, Integer> getEnchantments() {
        return enchantments;
    }

    @Override
    public Material getType() {
        return type;
    }

    @Override
    public void setAmount(int amount) {
        this.amount = amount;
    }

    @Override
    public void setDurability(short durability) {
        this.durability = durability;
    }

    @Override
    public void setType(Material type) {
        this.type = type;
    }
}
