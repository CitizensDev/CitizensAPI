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
