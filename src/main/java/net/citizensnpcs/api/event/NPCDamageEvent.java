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

import net.citizensnpcs.api.abstraction.Cancellable;
import net.citizensnpcs.api.npc.NPC;

public class NPCDamageEvent extends NPCEvent implements Cancellable {
    private boolean cancelled;
    private int damage;

    public NPCDamageEvent(NPC npc, int damage) {
        super(npc);
        this.damage = damage;
    }

    public int getDamage() {
        return damage;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }

    public void setDamage(int damage) {
        this.damage = damage;
    }
}
