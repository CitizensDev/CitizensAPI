package net.citizensnpcs.api.expr;

/**
 * A compiled expression that can be evaluated against a scope.
 */
public interface CompiledExpression {
    Object evaluate(ExpressionScope scope);

    default boolean evaluateAsBoolean(ExpressionScope scope) {
        Object result = evaluate(scope);
        if (result == null)
            return false;

        if (result instanceof Boolean)
            return (Boolean) result;

        if (result instanceof Number)
            return ((Number) result).doubleValue() != 0;

        if (result instanceof String)
            return !((String) result).isEmpty();

        return true;
    }

    default double evaluateAsNumber(ExpressionScope scope) {
        Object result = evaluate(scope);
        if (result == null)
            return 0;

        if (result instanceof Number)
            return ((Number) result).doubleValue();

        if (result instanceof Boolean)
            return ((Boolean) result) ? 1 : 0;

        if (result instanceof String) {
            try {
                return Double.parseDouble((String) result);
            } catch (NumberFormatException e) {
                return 0;
            }
        }
        return 0;
    }

    default String evaluateAsString(ExpressionScope scope) {
        Object result = evaluate(scope);
        return result == null ? "" : result.toString();
    }
}
