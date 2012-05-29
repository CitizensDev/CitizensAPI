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
package net.citizensnpcs.api.util;

public interface Storage {

    /**
     * Returns a {@link DataKey} starting from the given root.
     * 
     * @param root
     *            The root to start at
     * @return the created key
     */
    public DataKey getKey(String root);

    /**
     * Loads data from a file or other location.
     */
    public void load();

    /**
     * Saves the in-memory aspects of the storage to disk.
     */
    public void save();
}
