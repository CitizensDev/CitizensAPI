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
