package net.citizensnpcs.api.attachment;

/**
 * Builds a attachment.
 */
public final class AttachmentFactory {
    private final Class<? extends Attachment> attachment;
    private String name;

    /**
     * Constructs a factory with the given attachment class.
     * 
     * @param character
     *            Class of the attachment
     */
    public AttachmentFactory(Class<? extends Attachment> attachment) {
        this.attachment = attachment;
    }

    public Class<? extends Attachment> getAttachmentClass() {
        return attachment;
    }

    public String getAttachmentName() {
        return name;
    }

    public AttachmentFactory withName(String name) {
        this.name = name;
        return this;
    }
}