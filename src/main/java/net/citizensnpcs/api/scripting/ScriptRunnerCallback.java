package net.citizensnpcs.api.scripting;

public class ScriptRunnerCallback implements CompileCallback {
    private final Object[] methodArgs;
    private final String methodToInvoke;

    public ScriptRunnerCallback() {
        this.methodToInvoke = null;
        this.methodArgs = null;
    }

    public ScriptRunnerCallback(String methodToInvoke) {
        this(methodToInvoke, null);
    }

    public ScriptRunnerCallback(String methodToInvoke, Object[] methodArgs) {
        if (methodToInvoke == null)
            throw new IllegalArgumentException("method cannot be null");
        this.methodToInvoke = methodToInvoke;
        this.methodArgs = methodArgs;
    }

    private void invokeMethodIfAvailable(Script script) {
        if (methodToInvoke == null)
            return;
        script.invoke(methodToInvoke, methodArgs);
    }

    @Override
    public void onCompileTaskFinished() {
    }

    @Override
    public void onScriptCompiled(ScriptFactory factory) {
        Script script = factory.newInstance();
        invokeMethodIfAvailable(script);
    }
}
