package net.citizensnpcs.api.npc;

import net.citizensnpcs.api.attachment.Attachment;

public interface Attachable {
    /**
     * Attaches the {@link Attachment}.
     * 
     * @param attach
     *            The attachment to attach
     * @return The created attachment
     */
    public abstract Attachment attach(Class<? extends Attachment> attach);

    /**
     * Removes an attachment.
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
     * Checks if the given attachment is attached.
     * 
     * @param attachment
     *            Attachment to check
     * @return Whether the given attachment is attached
     */
    public abstract boolean isAttached(Class<? extends Attachment> attachment);

}