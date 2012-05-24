package net.citizensnpcs.api;

import java.io.File;
import java.lang.ref.WeakReference;

import net.citizensnpcs.api.abstraction.Server;
import net.citizensnpcs.api.attachment.AttachmentManager;
import net.citizensnpcs.api.npc.NPCRegistry;
import net.citizensnpcs.api.scripting.ScriptCompiler;

/**
 * Contains methods used in order to utilize the Citizens API.
 */
public final class CitizensAPI {
    private WeakReference<CitizensPlugin> implementation;

    private CitizensAPI() {
    }

    private static final CitizensAPI instance = new CitizensAPI();
    private static final ScriptCompiler scriptCompiler = new ScriptCompiler();

    public static File getDataFolder() {
        return getImplementation().getDataFolder();
    }

    /**
     * Gets the {@link NPCRegistry}.
     * 
     * @return The NPC registry
     */
    public static NPCRegistry getNPCRegistry() {
        return getImplementation().getNPCRegistry();
    }

    public static Server getServer() {
        return null;
    }

    public static ScriptCompiler getScriptCompiler() {
        return scriptCompiler;
    }

    public static File getScriptFolder() {
        return getImplementation().getScriptFolder();
    }

    /**
     * Gets the AttachmentManager.
     * 
     * @return Citizens attachment manager
     */
    public static AttachmentManager getAttachmentManager() {
        return getImplementation().getAttachmentManager();
    }

    public static void setImplementation(CitizensPlugin implementation) {
        if (hasImplementation())
            getImplementation().onImplementationChanged();
        instance.implementation = new WeakReference<CitizensPlugin>(implementation);
    }

    public static boolean hasImplementation() {
        return getImplementation() != null;
    }

    private static CitizensPlugin getImplementation() {
        return instance.implementation != null ? instance.implementation.get() : null;
    }

    static {
        new Thread(scriptCompiler).start();
    }
}