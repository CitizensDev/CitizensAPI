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
package net.citizensnpcs.api.scripting;

/**
 * Provides useful objects or methods to an instance of {@link Script}. It should be run just before the script is
 * evaluated, to ensure that the root level of the script can access the provided functions.
 */
public interface ContextProvider {

    /**
     * Provides context to a script, such as via {@link Script#setAttribute(String, Object)}.
     * 
     * @param script
     *            The script to provide context to.
     */
    public void provide(Script script);
}
