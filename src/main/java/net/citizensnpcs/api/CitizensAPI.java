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
import net.citizensnpcs.api.scripting.ScriptCompiler;

/**
 * Contains methods used in order to utilize the Citizens API.
 */
public final class CitizensAPI {
    private static CitizensPlugin plugin;
    private static final ScriptCompiler scriptCompiler = new ScriptCompiler();

    private CitizensAPI() {
    }

    /**
     * Gets the {@link AttachmentFactory} that is used to register NPC attachments.
     * 
     * @return Citizens attachment factory
     */
    public static AttachmentFactory getAttachmentFactory() {
        return plugin.getAttachmentFactory();
    }

    public static File getDataFolder() {
        return plugin.getDataFolder();
    }

    /**
     * Gets the {@link NPCRegistry} that is used to register {@link NPC}s.
     * 
     * @return Citizens NPC registry
     */
    public static NPCRegistry getNPCRegistry() {
        return plugin.getNPCRegistry();
    }

    /**
     * Gets the {@link ScriptCompiler}.
     * 
     * @return Citizens script compiler
     */
    public static ScriptCompiler getScriptCompiler() {
        return scriptCompiler;
    }

    /**
     * Gets the folder that contains Citizens scripts.
     * 
     * @return Citizens script folder
     */
    public static File getScriptFolder() {
        return plugin.getScriptFolder();
    }

    /**
     * Gets the {@link Server} that is implementing Citizens.
     * 
     * @return Server implementing Citizens
     */
    public static Server getServer() {
        return plugin.getServer();
    }

    /**
     * Sets the plugin implementation of Citizens. This should only be used by plugins that use the CitizensAPI
     * compatibility layer.
     */
    public static void setImplementation(CitizensPlugin citizens) {
        if (plugin != null) {
            throw new IllegalArgumentException("A Citizens implementation has already been registered for this server.");
        }
        plugin = citizens;
    }

    static {
        new Thread(scriptCompiler).start();
    }
}
