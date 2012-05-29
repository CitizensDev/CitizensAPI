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
package net.citizensnpcs.api;

import java.io.File;

import net.citizensnpcs.api.abstraction.Server;
import net.citizensnpcs.api.attachment.AttachmentFactory;
import net.citizensnpcs.api.npc.NPCRegistry;

public interface CitizensPlugin {
    /**
     * Gets the folder containing all Citizens files.
     * 
     * @return Folder containing Citizens files
     */
    public File getDataFolder();

    /**
     * Gets the {@link NPCRegistry} that is used to register NPCs.
     * 
     * @return NPC registry
     */
    public NPCRegistry getNPCRegistry();

    /**
     * Gets the Citizens script folder.
     * 
     * @return Folder containing Citizens scripts
     */
    public File getScriptFolder();

    /**
     * Gets the {@link AttachmentFactory} that is used to register NPC attachments.
     * 
     * @return Citizens attachment factory
     */
    public AttachmentFactory getAttachmentFactory();

    /**
     * Gets the {@link Server} that is implementing Citizens.
     * 
     * @return Server implementing Citizens
     */
    public Server getServer();
}
