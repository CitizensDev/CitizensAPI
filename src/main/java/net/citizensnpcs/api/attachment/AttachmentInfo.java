package net.citizensnpcs.api.attachment;

/**
 * Builds a attachment.
 */
public final class AttachmentInfo {
    private final Class<? extends Attachment> attachment;
    private String name;

    /**
     * Constructs with the given attachment class.
     * 
     * @param character
     *            Class of the attachment
     */
    public AttachmentInfo(Class<? extends Attachment> attachment) {
        this.attachment = attachment;
    }

    public Class<? extends Attachment> getAttachmentClass() {
        return attachment;
    }

    public String getAttachmentName() {
        return name;
    }

    public AttachmentInfo withName(String name) {
        this.name = name;
        return this;
    }
}