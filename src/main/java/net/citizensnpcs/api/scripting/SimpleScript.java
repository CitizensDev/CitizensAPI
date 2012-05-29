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

import javax.script.Bindings;
import javax.script.CompiledScript;
import javax.script.Invocable;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptException;

public class SimpleScript implements Script {
    private final Bindings bindings;
    private final ScriptEngine engine;
    private final Invocable invocable;
    @SuppressWarnings("unused")
    private final Object root;

    public SimpleScript(CompiledScript src, ContextProvider[] providers) throws ScriptException {
        this.engine = src.getEngine();
        this.invocable = (Invocable) engine;
        this.bindings = engine.createBindings();
        for (ContextProvider provider : providers)
            provider.provide(this);
        this.root = src.eval(bindings);
    }

    @Override
    public <T> T convertToInterface(Object obj, Class<T> expected) {
        if (obj == null || expected == null)
            throw new IllegalArgumentException("arguments should not be null");
        if (expected.isAssignableFrom(obj.getClass()))
            return expected.cast(obj);
        synchronized (engine) {
            Bindings old = engine.getBindings(ScriptContext.ENGINE_SCOPE);
            engine.setBindings(bindings, ScriptContext.ENGINE_SCOPE);
            T t = invocable.getInterface(expected);
            engine.setBindings(old, ScriptContext.ENGINE_SCOPE);
            return t;
        }
    }

    @Override
    public Object getAttribute(String name) {
        if (name == null)
            throw new IllegalArgumentException("name should not be null");
        return bindings.get(name);
    }

    @Override
    public Object invoke(Object instance, String name, Object... args) {
        if (instance == null || name == null)
            throw new IllegalArgumentException("instance and method name should not be null");
        try {
            synchronized (engine) {
                Bindings old = engine.getBindings(ScriptContext.ENGINE_SCOPE);
                engine.setBindings(bindings, ScriptContext.ENGINE_SCOPE);
                Object ret = invocable.invokeMethod(instance, name, args);
                engine.setBindings(old, ScriptContext.ENGINE_SCOPE);
                return ret;
            }
        } catch (ScriptException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Object invoke(String name, Object... args) {
        if (name == null)
            throw new IllegalArgumentException("name should not be null");
        try {
            synchronized (engine) {
                Bindings old = engine.getBindings(ScriptContext.ENGINE_SCOPE);
                engine.setBindings(bindings, ScriptContext.ENGINE_SCOPE);
                Object ret = invocable.invokeFunction(name, args);
                engine.setBindings(old, ScriptContext.ENGINE_SCOPE);
                return ret;
            }
        } catch (ScriptException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void setAttribute(String name, Object value) {
        if (name == null || value == null)
            throw new IllegalArgumentException("arguments should not be null");
        bindings.put(name, value);
    }
}
