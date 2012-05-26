package net.citizensnpcs.api.attachment;

/**
 * Represents a factory of {@link Attachment}s.
 */
public interface AttachmentFactory {

    /**
     * Gets a attachment with the given class.
     * 
     * @param clazz
     *            Class of the attachment
     * @return Attachment with the given class
     */
    public <T extends Attachment> T getAttachment(Class<T> clazz);

    /**
     * Gets a attachment with the given name.
     * 
     * @param name
     *            Name of the attachment
     * @return Attachment with the given name
     */
    public <T extends Attachment> T getAttachment(String name);

    /**
     * Registers a attachment using the given information.
     * 
     * @param info
     *            Information to use when creating attachments
     */
    public void registerAttachment(AttachmentInfo info);
}