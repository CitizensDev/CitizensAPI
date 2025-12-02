package net.citizensnpcs.api.expr;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.primitives.Doubles;

import net.citizensnpcs.api.expr.ExpressionEngine.ExpressionCompileException;

/**
 * Registry for expression engines and parser for expression syntax.
 *
 * Supports syntax
 * <ul>
 * <li>`expression` - default engine (molang)</li>
 * <li>js`expression` - javascript engine</li>
 * </ul>
 */
public class ExpressionRegistry {
    private String defaultEngineName = "molang";
    private final Map<String, ExpressionEngine> engines = new HashMap<>();

    public String applyDefaultExpressionMarkup(String expression) {
        if (isPossiblyExpression(expression))
            return expression;
        return '`' + expression + '`';
    }

    /**
     * Parses and compiles an expression string.
     *
     * Supports syntax
     * <ul>
     * <li>`expression` - default engine (molang)</li>
     * <li>js`expression` - javascript engine</li>
     * </ul>
     *
     * @param expr
     *            the expression string (including `...` wrapper)
     * @return the compiled expression
     */
    public CompiledExpression compile(String expr) throws ExpressionCompileException {
        if (defaultEngineName == null)
            throw new ExpressionCompileException("No expression engines registered");

        Matcher matcher = EXPRESSION_PATTERN.matcher(expr);
        if (!matcher.matches())
            throw new ExpressionCompileException("Invalid expression syntax: " + expr);

        String prefix = matcher.group(1).toLowerCase(Locale.ROOT);
        String expression = matcher.group(2);
        String language = defaultEngineName;

        if (engines.containsKey(prefix)) {
            language = prefix;
        }
        ExpressionEngine engine = engines.get(language);
        if (engine == null)
            throw new ExpressionCompileException("Unknown expression engine: " + language);

        return engine.compile(expression);
    }

    /**
     * Gets the default engine name.
     */
    public String getDefaultEngineName() {
        return defaultEngineName;
    }

    /**
     * Gets an engine by name.
     *
     * @param name
     *            the engine name
     * @return the engine, or null if not found
     */
    public ExpressionEngine getEngine(String name) {
        return engines.get(name.toLowerCase(Locale.ROOT));
    }

    /**
     * Checks if a string is possibly a valid expression.
     */
    public boolean isPossiblyExpression(String value) {
        return value != null && EXPRESSION_PATTERN.matcher(value).matches();
    }

    /**
     * Parses a value that might be an expression or a literal. If it's an expression, compiles and returns a
     * ValueHolder that evaluates it. If it's a literal, returns a ValueHolder that returns the literal.
     *
     * @param expr
     *            the value string
     * @return a ValueHolder for the value
     */
    public ExpressionValue parseValue(String expr) {
        if (isPossiblyExpression(expr)) {
            try {
                CompiledExpression compiled = compile(expr);
                return new ExpressionValue(compiled);
            } catch (ExpressionCompileException e) {
                e.printStackTrace();
                return new ExpressionValue(expr);
            }
        }
        return new ExpressionValue(expr);
    }

    /**
     * Registers an expression engine.
     *
     * @param engine
     *            the engine to register
     */
    public void registerEngine(ExpressionEngine engine) {
        engines.put(engine.getName().toLowerCase(Locale.ROOT), engine);
    }

    /**
     * Sets the default language to use when no prefix is specified.
     */
    public void setDefaultEngine(String language) {
        if (!engines.containsKey(language.toLowerCase(Locale.ROOT)))
            throw new IllegalArgumentException("Unknown engine: " + language);

        this.defaultEngineName = language.toLowerCase(Locale.ROOT);
    }

    public static class ExpressionValue {
        private final CompiledExpression expression;
        private final Object literal;

        ExpressionValue(CompiledExpression expression) {
            this.expression = expression;
            this.literal = null;
        }

        ExpressionValue(Object literal) {
            this.expression = null;
            this.literal = literal;
        }

        public Object evaluate(ExpressionScope scope) {
            if (expression != null) {
                return expression.evaluate(scope);
            }
            return literal;
        }

        public boolean evaluateAsBoolean(ExpressionScope scope) {
            if (expression != null)
                return expression.evaluateAsBoolean(scope);

            if (literal instanceof Boolean)
                return (Boolean) literal;

            if (literal instanceof Number)
                return ((Number) literal).doubleValue() != 0;

            if (literal instanceof String)
                return Boolean.parseBoolean((String) literal) || !((String) literal).isEmpty();

            return literal != null;
        }

        public double evaluateAsNumber(ExpressionScope scope) {
            if (expression != null)
                return expression.evaluateAsNumber(scope);

            if (literal instanceof Number)
                return ((Number) literal).doubleValue();

            if (literal instanceof String)
                return Doubles.tryParse(literal.toString());

            return 0;
        }

        public String evaluateAsString(ExpressionScope scope) {
            if (expression != null)
                return expression.evaluateAsString(scope);

            return literal == null ? "" : literal.toString();
        }

        public boolean isExpression() {
            return expression != null;
        }
    }

    private static final Pattern EXPRESSION_PATTERN = Pattern.compile("^([a-zA-Z_]*)\\`(.+)\\`$");
}
