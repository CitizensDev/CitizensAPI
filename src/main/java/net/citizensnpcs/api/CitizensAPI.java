package net.citizensnpcs.api;

import java.io.File;
import java.lang.ref.WeakReference;

import net.citizensnpcs.api.abstraction.Server;
import net.citizensnpcs.api.attachment.AttachmentFactory;
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

    /**
     * Gets the AttachmentManager.
     * 
     * @return Citizens attachment manager
     */
    public static AttachmentFactory getAttachmentManager() {
        return getImplementation().getAttachmentManager();
    }

    public static File getDataFolder() {
        return getImplementation().getDataFolder();
    }

    private static CitizensPlugin getImplementation() {
        return instance.implementation != null ? instance.implementation.get() : null;
    }

    /**
     * Gets the {@link NPCRegistry}.
     * 
     * @return The NPC registry
     */
    public static NPCRegistry getNPCRegistry() {
        return getImplementation().getNPCRegistry();
    }

    public static ScriptCompiler getScriptCompiler() {
        return scriptCompiler;
    }

    public static File getScriptFolder() {
        return getImplementation().getScriptFolder();
    }

    public static Server getServer() {
        return null;
    }

    public static boolean hasImplementation() {
        return getImplementation() != null;
    }

    public static void setImplementation(CitizensPlugin implementation) {
        if (hasImplementation())
            getImplementation().onImplementationChanged();
        instance.implementation = new WeakReference<CitizensPlugin>(implementation);
    }

    static {
        new Thread(scriptCompiler).start();
    }
}