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
