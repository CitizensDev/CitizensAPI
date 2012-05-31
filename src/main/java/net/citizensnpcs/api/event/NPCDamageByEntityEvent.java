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
package net.citizensnpcs.api.event;

import net.citizensnpcs.api.abstraction.entity.Entity;
import net.citizensnpcs.api.npc.NPC;

public class NPCDamageByEntityEvent extends NPCDamageEvent {
    private final Entity entity;

    public NPCDamageByEntityEvent(NPC npc, int damage, Entity entity) {
        super(npc, damage);
        this.entity = entity;
    }

    public Entity getDamager() {
        return entity;
    }
}
