package net.citizensnpcs.api.abstraction;

public interface CommandSender {
    String getName();

    boolean hasPermission(String perm);

    void sendMessage(String message);

    void useCommand(String cmd);
}
