package net.citizensnpcs.api.ai.tree.expr;

import java.util.HashMap;
import java.util.Map;

import javax.script.Bindings;
import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.script.SimpleBindings;

public class JSR223Engine implements ExpressionEngine {
    private final boolean compilable;
    private final javax.script.ScriptEngine engine;
    private final String name;

    /**
     * Creates a script engine with the given language name.
     *
     * @param language
     *            the script engine name (e.g., "js", "javascript", "groovy")
     * @throws IllegalArgumentException
     *             if the engine is not available
     */
    public JSR223Engine(String language) {
        ScriptEngineManager manager = new ScriptEngineManager();
        this.engine = manager.getEngineByName(language);
        if (this.engine == null) {
            throw new IllegalArgumentException("Script engine not found: " + language);
        }
        this.name = language.toLowerCase();
        this.compilable = engine instanceof Compilable;
    }

    @Override
    public CompiledExpression compile(String expression) throws ExpressionCompileException {
        if (compilable) {
            try {
                CompiledScript compiled = ((Compilable) engine).compile(expression);
                return new JSR223CompiledExpression(compiled);
            } catch (ScriptException e) {
                throw new ExpressionCompileException("Failed to compile script: " + expression, e);
            }
        }
        return new JSR223InterpretedExpression(engine, expression);
    }

    @Override
    public String getName() {
        return name;
    }

    private static class JSR223CompiledExpression implements CompiledExpression {
        private final CompiledScript compiled;

        JSR223CompiledExpression(CompiledScript compiled) {
            this.compiled = compiled;
        }

        @Override
        public Object evaluate(ExpressionScope scope) {
            try {
                Bindings bindings = createBindings(scope);
                return compiled.eval(bindings);
            } catch (ScriptException e) {
                return null;
            }
        }
    }

    private static class JSR223InterpretedExpression implements CompiledExpression {
        private final javax.script.ScriptEngine engine;
        private final String expression;

        JSR223InterpretedExpression(javax.script.ScriptEngine engine, String expression) {
            this.engine = engine;
            this.expression = expression;
        }

        @Override
        public Object evaluate(ExpressionScope scope) {
            try {
                Bindings bindings = createBindings(scope);
                return engine.eval(expression, bindings);
            } catch (ScriptException e) {
                return null;
            }
        }
    }

    private static Bindings createBindings(ExpressionScope scope) {
        Bindings bindings = new SimpleBindings();

        for (String name : scope.getVariableNames()) {
            Object value = scope.get(name);
            if (value != null) {
                if (name.contains(".")) {
                    String[] parts = name.split("\\.");
                    Map<String, Object> current = bindings;
                    for (int i = 0; i < parts.length - 1; i++) {
                        Map<String, Object> next = (Map<String, Object>) current.get(parts[i]);
                        if (next == null) {
                            next = new HashMap<>();
                            current.put(parts[i], next);
                        }
                        current = next;
                    }
                    current.put(parts[parts.length - 1], value);
                } else {
                    bindings.put(name, value);
                }
            }
        }
        return bindings;
    }

    public static JSR223Engine javascript() {
        ScriptEngineManager manager = new ScriptEngineManager();
        ScriptEngine engine = manager.getEngineByName("nashorn");
        if (engine == null) {
            engine = manager.getEngineByName("graal.js");
        }
        if (engine == null) {
            engine = manager.getEngineByName("js");
        }
        if (engine == null) {
            throw new IllegalStateException("No JavaScript engine available");
        }
        return new JSR223Engine("js");
    }
}
