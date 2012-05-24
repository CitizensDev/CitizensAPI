package net.citizensnpcs.api.abstraction;

public interface CommandSender {
    void sendMessage(String message);

    void useCommand(String cmd);

    String getName();

    boolean hasPermission(String perm);
}
