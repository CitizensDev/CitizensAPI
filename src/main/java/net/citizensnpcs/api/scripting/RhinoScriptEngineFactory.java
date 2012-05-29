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

import java.util.List;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;

public class RhinoScriptEngineFactory implements ScriptEngineFactory {
    private final List<String> extensions = ImmutableList.of("js", "ecmascript", "javascript");
    private final List<String> mimeTypes = ImmutableList.of("application/javascript", "text/javascript",
            "application/ecmascript", "text/javascript");
    private final List<String> names = ImmutableList.of("rhino", "javascript", "JavaScript", "ECMAScript", "js");

    @Override
    public String getEngineName() {
        return "Mozilla Rhino (Citizens)";
    }

    @Override
    public String getEngineVersion() {
        return "version 1.7 release 3";
    }

    @Override
    public List<String> getExtensions() {
        return extensions;
    }

    @Override
    public String getLanguageName() {
        return "ECMAScript";
    }

    @Override
    public String getLanguageVersion() {
        return "1.8";
    }

    @Override
    public String getMethodCallSyntax(String obj, String m, String... args) {
        return obj + "." + m + "(" + Joiner.on(",").join(args) + ")";
    }

    @Override
    public List<String> getMimeTypes() {
        return mimeTypes;
    }

    @Override
    public List<String> getNames() {
        return names;
    }

    @Override
    public String getOutputStatement(String toDisplay) {
        return "print(\"" + toDisplay.replace("\"", "\\\"").replace("\\", "\\\\") + "\")";
    }

    @Override
    public Object getParameter(String key) {
        if (key.equals(ScriptEngine.NAME) || key.equals(ScriptEngine.ENGINE))
            return getEngineName();
        if (key.equals(ScriptEngine.ENGINE_VERSION))
            return getEngineVersion();
        if (key.equals(ScriptEngine.LANGUAGE))
            return getLanguageName();
        if (key.equals(ScriptEngine.LANGUAGE_VERSION))
            return getLanguageVersion();
        if (key.equals("THREADING")) {
            return "MULTITHREADED";
        }
        throw new IllegalArgumentException("Invalid key");
    }

    @Override
    public String getProgram(String... statements) {
        return Joiner.on(";").join(statements);
    }

    @Override
    public ScriptEngine getScriptEngine() {
        return new RhinoScriptEngine().setScriptEngineFactory(this);
    }
}
