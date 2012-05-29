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
 * A simple callback interface for use in {@link ScriptCompiler}.
 */
public interface CompileCallback {

    public void onCompileTaskFinished();

    /**
     * Called when a script has been compiled using the relevant script engine.
     * 
     * @param script
     *            The newly created script
     */
    public void onScriptCompiled(ScriptFactory script);
}
