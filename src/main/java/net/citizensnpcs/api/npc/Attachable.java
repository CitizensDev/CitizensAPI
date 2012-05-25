package net.citizensnpcs.api.npc;

import net.citizensnpcs.api.attachment.Attachment;

public interface Attachable {

    public abstract Attachment attach(Class<? extends Attachment> attach);

    /**
     * Removes an attachment from this NPC.
     * 
     * @param attachment
     *            Attachment to remove
     */
    public abstract void detach(Class<? extends Attachment> attachment);

    /**
     * Gets an attachment from the given class.
     * 
     * @param attachment
     *            Attachment to get
     * @return Attachment with the given name
     */
    public abstract <T extends Attachment> T getAttachment(Class<T> attachment);

    /**
     * Checks if this NPC has the given attachment.
     * 
     * @param attachment
     *            Attachment to check
     * @return Whether this NPC has the given attachment
     */
    public abstract boolean isAttached(Class<? extends Attachment> attachment);

}