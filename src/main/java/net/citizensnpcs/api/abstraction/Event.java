package net.citizensnpcs.api.abstraction;

public abstract class Event {
    private String name;

    protected Event() {
    }

    public String getEventName() {
        if (name == null) {
            name = getClass().getSimpleName();
        }
        return name;
    }
}
