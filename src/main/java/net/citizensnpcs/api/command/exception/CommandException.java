package net.citizensnpcs.api.command.exception;

import net.citizensnpcs.api.util.Messaging;

public class CommandException extends Exception {
    public CommandException() {
    }

    public CommandException(String message) {
        super(Messaging.tryTranslate(message));
    }

    public CommandException(String key, Object... replacements) {
        super(Messaging.tr(key, replacements));
    }

    public CommandException(Throwable t) {
        super(t);
    }

    @Override
    public Throwable fillInStackTrace() {
        return this;
    }

    private static final long serialVersionUID = 870638193072101739L;
}