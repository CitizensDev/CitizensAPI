package net.citizensnpcs.api.trait.trait;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.entity.AbstractHorse;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Horse;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.minecart.StorageMinecart;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.exception.NPCLoadException;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.trait.TraitName;
import net.citizensnpcs.api.trait.trait.Equipment.EquipmentSlot;
import net.citizensnpcs.api.util.DataKey;
import net.citizensnpcs.api.util.ItemStorage;
import net.citizensnpcs.api.util.SpigotUtil.InventoryViewAPI;

/**
 * Represents an NPC's inventory.
 */
@TraitName("inventory")
public class Inventory extends Trait {
    private ItemStack[] contents;
    private boolean registeredListener;
    private org.bukkit.inventory.Inventory view;
    private final Set<InventoryViewAPI> viewers = new HashSet<>();

    public Inventory() {
        super("inventory");
        contents = new ItemStack[72];
    }

    /**
     * Gets the contents of an NPC's inventory.
     *
     * @return ItemStack array of an NPC's inventory contents
     */
    public ItemStack[] getContents() {
        if (view != null && !viewers.isEmpty())
            return view.getContents();
        saveContents(npc.getEntity());
        return contents;
    }

    public org.bukkit.inventory.Inventory getInventoryView() {
        return view;
    }

    @Override
    public void load(DataKey key) throws NPCLoadException {
        contents = parseContents(key);
    }

    @Override
    public void onDespawn() {
        saveContents(npc.getEntity());
    }

    @Override
    public void onSpawn() {
        setContents(contents);
        int size = npc.getEntity() instanceof Player ? 36
                : npc.getEntity() instanceof InventoryHolder
                        ? ((InventoryHolder) npc.getEntity()).getInventory().getSize()
                        : contents.length;
        int rem = size % 9;
        if (rem != 0) {
            size += 9 - rem; // round up to nearest multiple of 9
        }
        if (size > 54) {
            size = 54;
        }
        if (size < 9) {
            size = 9;
        }
        String name = npc.getName().length() >= 19 ? npc.getName().substring(0, 19) + "~" : npc.getName();
        view = Bukkit.createInventory(
                npc.getEntity() instanceof InventoryHolder ? (InventoryHolder) npc.getEntity() : null, size,
                name + "'s Inventory");
        for (int i = 0; i < view.getSize(); i++) {
            view.setItem(i, contents[i]);
        }
    }

    public void openInventory(Player sender) {
        saveContents(npc.getEntity());
        if (!registeredListener) {
            registeredListener = true;
            Bukkit.getPluginManager().registerEvents(new Listener() {
                @EventHandler(ignoreCancelled = true)
                public void inventoryCloseEvent(InventoryCloseEvent event) {
                    if (!viewers.contains(new InventoryViewAPI(event.getView())))
                        return;
                    ItemStack[] contents = event.getInventory().getContents();
                    for (int i = 0; i < Inventory.this.contents.length; i++) {
                        if (i >= contents.length) {
                            Inventory.this.contents[i] = null;
                            continue;
                        }
                        Inventory.this.contents[i] = contents[i];
                        if (i == 0 && npc.getEntity() instanceof LivingEntity) {
                            npc.getOrAddTrait(Equipment.class).set(EquipmentSlot.HAND, contents[i]);
                        }
                    }
                    if (npc.getEntity() instanceof InventoryHolder) {
                        if (SUPPORT_GET_STORAGE_CONTENTS) {
                            int maxSize = ((InventoryHolder) npc.getEntity()).getInventory()
                                    .getStorageContents().length;
                            ((InventoryHolder) npc.getEntity()).getInventory()
                                    .setStorageContents(Arrays.copyOf(contents, maxSize));
                        } else {
                            int maxSize = ((InventoryHolder) npc.getEntity()).getInventory().getContents().length;
                            ((InventoryHolder) npc.getEntity()).getInventory()
                                    .setContents(Arrays.copyOf(contents, maxSize));
                        }
                    }
                    viewers.remove(new InventoryViewAPI(event.getView()));
                }
            }, CitizensAPI.getPlugin());
        }
        for (int i = 0; i < view.getSize(); i++) {
            if (i >= contents.length)
                break;

            view.setItem(i, contents[i]);
        }
        viewers.add(new InventoryViewAPI(sender.openInventory(view)));
    }

    private ItemStack[] parseContents(DataKey key) throws NPCLoadException {
        ItemStack[] contents = new ItemStack[72];
        for (DataKey slotKey : key.getIntegerSubKeys()) {
            contents[Integer.parseInt(slotKey.name())] = ItemStorage.loadItemStack(slotKey);
        }
        return contents;
    }

    @Override
    public void run() {
        if (viewers.isEmpty())
            return;
        Iterator<InventoryViewAPI> itr = viewers.iterator();
        while (itr.hasNext()) {
            InventoryViewAPI iview = itr.next();
            if (!iview.getPlayer().isValid()) {
                iview.close();
                itr.remove();
            }
        }
    }

    @Override
    public void save(DataKey key) {
        if (npc.isSpawned()) {
            saveContents(npc.getEntity());
        }
        int slot = 0;
        for (ItemStack item : contents) {
            // Clear previous items to avoid conflicts
            key.removeKey(String.valueOf(slot));
            if (item != null) {
                ItemStorage.saveItem(key.getRelative(String.valueOf(slot)), item);
            }
            slot++;
        }
    }

    private void saveContents(Entity entity) {
        if (entity == null)
            return;
        if (view != null && !viewers.isEmpty()) {
            contents = view.getContents();
        } else if (entity instanceof InventoryHolder) {
            if (SUPPORT_GET_STORAGE_CONTENTS) {
                contents = ((InventoryHolder) entity).getInventory().getStorageContents();
            } else {
                contents = ((InventoryHolder) entity).getInventory().getContents();
            }
        }
    }

    /**
     * Sets the contents of an NPC's inventory.
     *
     * @param contents
     *            ItemStack array to set as the contents of an NPC's inventory
     */
    public void setContents(ItemStack[] contents) {
        this.contents = Arrays.copyOf(contents, 72);
        org.bukkit.inventory.Inventory dest = null;
        int maxCopySize = -1;
        if (npc.getEntity() instanceof Player) {
            dest = ((Player) npc.getEntity()).getInventory();
            maxCopySize = 36;
        } else if (npc.getEntity() instanceof StorageMinecart) {
            dest = ((StorageMinecart) npc.getEntity()).getInventory();
        }
        if (SUPPORT_ABSTRACT_HORSE && npc.getEntity() instanceof AbstractHorse) {
            dest = ((AbstractHorse) npc.getEntity()).getInventory();
        } else if (!SUPPORT_ABSTRACT_HORSE && npc.getEntity() instanceof Horse) {
            dest = ((Horse) npc.getEntity()).getInventory();
        }
        if (dest == null)
            return;

        if (maxCopySize == -1) {
            maxCopySize = dest.getSize();
        }
        for (int i = 0; i < maxCopySize; i++) {
            if (i < contents.length) {
                dest.setItem(i, contents[i]);
            }
        }
        if (view == null)
            return;

        for (int i = 0; i < maxCopySize; i++) {
            if (i < contents.length && i < view.getSize()) {
                view.setItem(i, contents[i]);
            }
        }
    }

    public void setItem(int slot, ItemStack item) {
        if (item != null) {
            item = item.clone();
        }
        if (view != null && view.getSize() > slot) {
            view.setItem(slot, item);
        } else if (contents.length > slot) {
            contents[slot] = item;
        } else
            throw new IndexOutOfBoundsException();
        if (npc.getEntity() instanceof InventoryHolder) {
            ((InventoryHolder) npc.getEntity()).getInventory().setItem(slot, item);
        }
        if (slot == 0 && npc.getEntity() instanceof LivingEntity) {
            npc.getOrAddTrait(Equipment.class).set(EquipmentSlot.HAND, item);
        }
    }

    void setItemInHand(ItemStack item) {
        if (item != null) {
            item = item.clone();
        }
        if (view != null && view.getSize() > 0) {
            view.setItem(0, item);
        } else if (contents.length > 0) {
            contents[0] = item;
        }
    }

    @Override
    public String toString() {
        return "Inventory{" + Arrays.toString(contents) + "}";
    }

    private static boolean SUPPORT_ABSTRACT_HORSE = false;
    private static boolean SUPPORT_GET_STORAGE_CONTENTS = false;
    static {
        try {
            SUPPORT_ABSTRACT_HORSE = Class.forName("org.bukkit.entity.AbstractHorse") != null;
        } catch (Throwable e) {
        }
        try {
            SUPPORT_GET_STORAGE_CONTENTS = org.bukkit.inventory.Inventory.class.getMethod("getStorageContents") != null;
        } catch (Throwable e) {
        }
    }
}
