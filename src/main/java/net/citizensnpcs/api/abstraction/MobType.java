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

public enum MobType {
    BLAZE("Blaze"),
    CAVE_SPIDER("CaveSpider"),
    CHICKEN("Chicken"),
    COW("Cow"),
    CREEPER("Creeper"),
    ENDER_DRAGON("EnderDragon"),
    ENDERMAN("Enderman"),
    GHAST("Ghast"),
    GIANT("Giant"),
    IRON_GOLEM("VillagerGolem"),
    MAGMA_CUBE("LavaSlime"),
    MUSHROOM_COW("MushroomCow"),
    OCELOT("Ozelot"),
    PIG("Pig"),
    PIG_ZOMBIE("PigZombie"),
    PLAYER("Player"),
    SHEEP("Sheep"),
    SILVERFISH("Silverfish"),
    SKELETON("Skeleton"),
    SLIME("Slime"),
    SNOWMAN("Snowman"),
    SPIDER("Spider"),
    SQUID("Squid"),
    UNKNOWN(null),
    VILLAGER("Villager"),
    WOLF("Wolf"),
    ZOMBIE("Zombie");

    private String name;

    MobType(String name) {
        this.name = name;
    }public String getName() {
        return this.name;
    }

    public static MobType fromName(String name) {
        if (name == null)
            return null;
        return BY_NAME.get(name.toLowerCase());
    }

    private static final Map<String, MobType> BY_NAME = Maps.newHashMap();
    static {
        for (MobType type : values()) {
            BY_NAME.put(type.name.toLowerCase(), type);
            BY_NAME.put(type.name().toLowerCase(), type);
        }
    }
}
