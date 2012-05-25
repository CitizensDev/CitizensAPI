package net.citizensnpcs.api.npc;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.attachment.Attachment;
import net.citizensnpcs.api.event.NPCRemoveEvent;

import org.mozilla.javascript.ContextFactory.Listener;

import com.google.common.collect.Maps;

public abstract class AbstractNPC implements NPC {
    protected int id; // TODO: id could be settable via NPCRegistry
                      // implementations and protected methods?
    private String name;
    protected final List<Runnable> runnables = new ArrayList<Runnable>();
    protected final Map<Class<? extends Attachment>, Attachment> attachments = Maps.newHashMap();

    protected AbstractNPC(String name) {
        this.name = name;
    }

    @Override
    public void attach(Class<? extends Attachment> clazz) {
        attach(getAttachmentFor(clazz));
    }

    private void attach(Attachment attachment) {
        if (attachment == null) {
            System.err.println("[Citizens] Cannot register a null trait. Was it registered properly?");
            return;
        }

        if (attachment instanceof Runnable) {
            runnables.add((Runnable) attachment);
            if (attachments.containsKey(attachment.getClass()))
                runnables.remove(attachments.get(attachment.getClass()));
        }

        if (attachment instanceof Listener) {
            CitizensAPI.getServer().registerEvents((Listener) attachment);
        }
        attachments.put(attachment.getClass(), attachment);
    }

    @Override
    public String getFullName() {
        return name;
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public String getName() {
        return name.replaceAll("0123456789abcdefgh", "");
    }

    @Override
    public <T extends Attachment> T getAttachment(Class<T> clazz) {
        Attachment attached = attachments.get(clazz);
        if (attached == null) {
            attached = getAttachmentFor(clazz);
            attach(attached);
        }
        return attached != null ? clazz.cast(attached) : null;
    }

    protected abstract Attachment getAttachmentFor(Class<? extends Attachment> clazz);

    @Override
    public boolean isAttached(Class<? extends Attachment> attachment) {
        return attachments.containsKey(attachment);
    }

    @Override
    public void remove() {
        CitizensAPI.getServer().callEvent(new NPCRemoveEvent(this));
        runnables.clear();
        for (Attachment attached : attachments.values()) {
            if (attached instanceof Listener) {
                CitizensAPI.getServer().unregisterAll((Listener) attached);
            }
        }
        attachments.clear();
    }

    @Override
    public void detach(Class<? extends Attachment> attachment) {
        Attachment t = attachments.remove(attachment);
        if (t != null) {
            if (t instanceof Runnable)
                runnables.remove(t);
            t.onRemove();
        }
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    public void update() {
        for (int i = 0; i < runnables.size(); ++i)
            runnables.get(i).run();
    }
}
