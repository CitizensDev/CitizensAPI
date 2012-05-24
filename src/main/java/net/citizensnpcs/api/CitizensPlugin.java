package net.citizensnpcs.api;

import java.io.File;

import net.citizensnpcs.api.attachment.AttachmentManager;
import net.citizensnpcs.api.npc.NPCRegistry;

public interface CitizensPlugin {
    public File getScriptFolder();

    /**
     * Gets the {@link NPCRegistry}.
     * 
     * @return The NPC registry
     */
    public NPCRegistry getNPCRegistry();

    /**
     * Gets the AttachmentManager.
     * 
     * @return The attachment manager
     */
    public AttachmentManager getAttachmentManager();

    public void onImplementationChanged();

    public File getDataFolder();
}
