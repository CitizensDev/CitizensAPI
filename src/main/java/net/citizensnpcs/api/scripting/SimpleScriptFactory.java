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

import javax.script.CompiledScript;
import javax.script.ScriptException;

public class SimpleScriptFactory implements ScriptFactory {
    private final ContextProvider[] providers;
    private final CompiledScript src;

    SimpleScriptFactory(CompiledScript src, ContextProvider... providers) {
        if (src == null)
            throw new IllegalArgumentException("src cannot be null");
        if (providers == null)
            providers = new ContextProvider[0];
        this.src = src;
        this.providers = providers;
    }

    @Override
    public Script newInstance() {
        try {
            return new SimpleScript(src, providers);
        } catch (ScriptException e) {
            e.printStackTrace();
            return null;
        }
    }
}
