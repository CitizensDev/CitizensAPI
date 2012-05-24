package net.citizensnpcs.api.attachment;

public interface AttachmentManager {

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
     * Registers a attachment using the given factory.
     * 
     * @param factory
     *            Factory to use to register a attachment with
     */
    public void registerAttachment(AttachmentFactory factory);
}