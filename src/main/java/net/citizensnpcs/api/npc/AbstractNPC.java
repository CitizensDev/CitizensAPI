/*
 * CitizensAPI
 * Copyright (C) 2012 CitizensDev <http://citizensnpcs.net>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package net.citizensnpcs.api.npc;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.abstraction.WorldVector;
import net.citizensnpcs.api.abstraction.entity.Entity;
import net.citizensnpcs.api.abstraction.entity.EntityFactory;
import net.citizensnpcs.api.abstraction.entity.LivingEntity;
import net.citizensnpcs.api.ai.AI;
import net.citizensnpcs.api.ai.SimpleAI;
import net.citizensnpcs.api.attachment.Attachment;
import net.citizensnpcs.api.event.NPCDespawnEvent;
import net.citizensnpcs.api.event.NPCRemoveEvent;
import net.citizensnpcs.api.event.NPCSpawnEvent;

import org.mozilla.javascript.ContextFactory.Listener;

import com.google.common.collect.Maps;

public abstract class AbstractNPC implements NPC {
    private final AI ai;
    private final Map<Class<? extends Attachment>, Attachment> attachments = Maps.newHashMap();
    private Entity entity;
    private EntityFactory factory;
    private final int id;
    protected String name;
    private final NPCRegistry registeredWith;
    private final List<Runnable> runnables = new ArrayList<Runnable>();

    protected AbstractNPC(NPCRegistry registry, String name) {
        this.name = name;
        this.id = registry.register(this);
        this.registeredWith = registry;
        this.ai = createAI();
    }

    protected void attach(Attachment attachment) {
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
            CitizensAPI.getServer().registerEvents(attachment);
        }
        attachments.put(attachment.getClass(), attachment);
    }

    @Override
    public Attachment attach(Class<? extends Attachment> clazz) {
        Attachment attached = getAttachmentFor(clazz);
        attach(attached);
        return attached;
    }

    protected AI createAI() {
        return new SimpleAI(this);
    }

    @Override
    public boolean despawn() {
        if (!isSpawned()) {
            return false;
        }
        CitizensAPI.getServer().callEvent(new NPCDespawnEvent(this));
        entity.remove();

        return true;
    }

    @Override
    public void destroy() {
        CitizensAPI.getServer().callEvent(new NPCRemoveEvent(this));
        runnables.clear();
        for (Attachment attached : attachments.values()) {
            if (attached instanceof Listener) {
                CitizensAPI.getServer().unregisterAll(attached);
            }
        }
        attachments.clear();
        registeredWith.deregister(this);
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
    public AI getAI() {
        return this.ai;
    }

    @Override
    public <T extends Attachment> T getAttachment(Class<T> clazz) {
        Attachment attached = attachments.get(clazz);
        if (attached == null) {
            attached = getAttachmentFor(clazz);
            attach(attached);
        }
        return clazz.cast(attached);
    }

    protected abstract Attachment getAttachmentFor(Class<? extends Attachment> clazz);

    @Override
    public LivingEntity getEntity() {
        return (LivingEntity) entity;
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public String getName() {
        return getStrippedName();
    }

    private String getStrippedName() {
        String stripped = name;
        for (int i = 0; i < COLORS.length(); ++i) {
            stripped = stripped.replaceAll("<" + COLORS.charAt(i) + ">", stripped);
        }
        return stripped;
    }

    @Override
    public boolean isAttached(Class<? extends Attachment> attachment) {
        return attachments.containsKey(attachment);
    }

    @Override
    public boolean isSpawned() {
        return entity != null;
    }

    @Override
    public void rename(String name) {
        this.name = name;
    }

    @Override
    public void setEntityFactory(EntityFactory factory) {
        this.factory = factory;
    }

    @Override
    public boolean spawn(WorldVector at) {
        if (at == null)
            throw new IllegalArgumentException("at cannot be null");
        if (factory == null)
            throw new IllegalStateException("no factory set");
        if (isSpawned())
            return false;
        NPCSpawnEvent spawnEvent = new NPCSpawnEvent(this, at);
        CitizensAPI.getServer().callEvent(spawnEvent);
        if (spawnEvent.isCancelled())
            return false;

        entity = factory.create(this, at);

        // Set the spawned state
        getAttachment(CurrentLocation.class).setLocation(at);

        // Modify NPC using traits after the entity has been created
        for (Attachment attached : attachments.values())
            attached.onSpawn();
        return true;
    }

    public void update() {
        for (int i = 0; i < runnables.size(); ++i)
            runnables.get(i).run();
        ai.update();
    }

    private static final String COLORS = "0123456789abcdefklmnor";
}
