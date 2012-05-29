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

import java.util.concurrent.Callable;

import com.google.common.util.concurrent.Callables;

public class ObjectProvider implements ContextProvider {
    private final String name;
    private final Callable<Object> provider;

    public ObjectProvider(String name, Callable<Object> provider) {
        if (provider == null)
            throw new IllegalArgumentException("provider cannot be null");
        if (name == null)
            throw new IllegalArgumentException("name cannot be null");
        this.name = name;
        this.provider = provider;
    }

    public ObjectProvider(String name, Object obj) {
        if (obj == null)
            throw new IllegalArgumentException("provided object cannot be null");
        if (name == null)
            throw new IllegalArgumentException("name cannot be null");
        this.name = name;
        this.provider = Callables.returning(obj);
    }

    @Override
    public void provide(Script script) {
        Object res = null;
        try {
            res = provider.call();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (res == null)
            return;
        script.setAttribute(name, res);
    }
}
