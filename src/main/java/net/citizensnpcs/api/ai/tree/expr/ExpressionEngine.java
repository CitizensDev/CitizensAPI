package net.citizensnpcs.api.ai.tree.expr;

/**
 * Expression evaluation abstraction (e.g., Molang, JavaScript, Denizen).
 */
public interface ExpressionEngine {
    /**
     * Compiles an expression string into a reusable compiled form.
     *
     * @param expression
     *            the expression text
     * @return a compiled expression that can be evaluated multiple times
     */
    CompiledExpression compile(String expression) throws ExpressionCompileException;

    String getName();

    public static class ExpressionCompileException extends Exception {
        public ExpressionCompileException(String message) {
            super(message);
        }

        public ExpressionCompileException(String message, Throwable cause) {
            super(message, cause);
        }

        private static final long serialVersionUID = 1716686092053880737L;
    }
}
