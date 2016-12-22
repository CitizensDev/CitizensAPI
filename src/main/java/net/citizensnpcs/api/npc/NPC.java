package net.citizensnpcs.api.npc;

import java.util.UUID;

import net.citizensnpcs.api.exception.NotPlayerNPCException;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

import net.citizensnpcs.api.ai.GoalController;
import net.citizensnpcs.api.ai.Navigator;
import net.citizensnpcs.api.ai.speech.SpeechController;
import net.citizensnpcs.api.astar.Agent;
import net.citizensnpcs.api.event.DespawnReason;
import net.citizensnpcs.api.event.NPCDespawnEvent;
import net.citizensnpcs.api.npc.BlockBreaker.BlockBreakerConfiguration;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.trait.TraitFactory;
import net.citizensnpcs.api.util.DataKey;

/**
 * Represents an NPC with optional {@link Trait}s.
 */
public interface NPC extends Agent, Cloneable {
    /**
     * Adds a trait to this NPC. This will use the {@link TraitFactory} defined for this NPC to construct and attach a
     * trait using {@link #addTrait(Trait)}.
     *
     * @param trait
     *            The class of the trait to add
     */
    public void addTrait(Class<? extends Trait> trait);

    /**
     * Adds a trait to this NPC.
     *
     * @param trait
     *            Trait to add
     */
    public void addTrait(Trait trait);

    /**
     * @return A clone of the NPC. May not be an exact copy depending on the {@link Trait}s installed.
     */
    public NPC clone();

    /**
     * @return The metadata store of this NPC.
     */
    public MetadataStore data();

    /**
     * Despawns this NPC. This is equivalent to calling {@link #despawn(DespawnReason)} with
     * {@link DespawnReason#PLUGIN}.
     *
     * @return Whether this NPC was able to despawn
     */
    public boolean despawn();

    /**
     * Despawns this NPC.
     *
     * @param reason
     *            The reason for despawning, for use in {@link NPCDespawnEvent}
     * @return Whether this NPC was able to despawn
     */
    boolean despawn(DespawnReason reason);

    /**
     * Permanently removes this NPC and all data about it from the registry it's attached to.
     */
    public void destroy();

    /**
     * Faces a given {@link Location} if the NPC is spawned.
     */
    public void faceLocation(Location location);

    public BlockBreaker getBlockBreaker(Block targetBlock, BlockBreakerConfiguration config);

    /**
     * Gets the default {@link GoalController} of this NPC.
     *
     * @return Default goal controller
     */
    public GoalController getDefaultGoalController();

    /**
     * Gets the default {@link SpeechController} of this NPC.
     *
     * @return Default speech controller
     */
    public SpeechController getDefaultSpeechController();

    /**
     * Gets the Bukkit entity associated with this NPC. This may be <code>null</code> if {@link #isSpawned()} is false.
     *
     * @return Entity associated with this NPC
     */
    public Entity getEntity();

    /**
     * Gets the full name of this NPC.
     *
     * @return Full name of this NPC
     */
    public String getFullName();

    /**
     * Gets the unique ID of this NPC. This is not guaranteed to be globally unique across server sessions.
     *
     * @return ID of this NPC
     */
    public int getId();

    /**
     * Gets the name of this NPC with color codes stripped.
     *
     * @return Stripped name of this NPC
     */
    public String getName();

    /**
     * @return The {@link Navigator} of this NPC.
     */
    public Navigator getNavigator();

    /**
     * @return The {@link NPCRegistry} that created this NPC.
     */
    public NPCRegistry getOwningRegistry();

    /**
     * If the NPC is not spawned, then this method will return the last known location, or null if it has never been
     * spawned. Otherwise, it is equivalent to calling <code>npc.getBukkitEntity().getLocation()</code>.
     *
     * @return The stored location, or <code>null</code> if none was found.
     */
    public Location getStoredLocation();

    /**
     * Gets a trait from the given class. If the NPC does not currently have the trait then it will be created and
     * attached using {@link #addTrait(Class)} .
     *
     * @param trait
     *            Trait to get
     * @return Trait with the given name
     */
    public <T extends Trait> T getTrait(Class<T> trait);

    /**
     * Returns the currently attached {@link Trait}s
     *
     * @return An Iterable of the current traits
     */
    public Iterable<Trait> getTraits();

    /**
     * Gets the unique id of this NPC. This is guaranteed to be unique for all NPCs.
     *
     * @return The unique id
     */
    public UUID getUniqueId();

    /**
     * Checks if this NPC has the given trait.
     *
     * @param trait
     *            Trait to check
     * @return Whether this NPC has the given trait
     */
    public boolean hasTrait(Class<? extends Trait> trait);

    /**
     * Returns whether this NPC is flyable or not.
     *
     * @return Whether this NPC is flyable
     */
    public boolean isFlyable();

    /**
     * Gets whether this NPC is protected from damage, movement and other events that players and mobs use to change the
     * entity state of the NPC.
     *
     * @return Whether this NPC is protected
     */
    public boolean isProtected();

    /**
     * Gets whether this NPC is currently spawned.
     *
     * @return Whether this NPC is spawned
     */
    public boolean isSpawned();

    /**
     * Loads the {@link NPC} from the given {@link DataKey}. This reloads all traits, respawns the NPC and sets it up
     * for execution. Should not be called often.
     *
     * @param key
     *            The root data key
     */
    public void load(DataKey key);

    /**
     * Removes a trait from this NPC.
     *
     * @param trait
     *            Trait to remove
     */
    public void removeTrait(Class<? extends Trait> trait);

    /**
     * Saves the {@link NPC} to the given {@link DataKey}. This includes all metadata, traits, and spawn information
     * that will allow it to respawn at a later time via {@link #load(DataKey)}.
     *
     * @param key
     *            The root data key
     */
    public void save(DataKey key);

    /**
     * Sets the {@link EntityType} of this NPC. Currently only accepts <em>living</em> entity types, with scope for
     * additional types in the future. The NPC will respawned if currently spawned, or will remain despawned otherwise.
     *
     * @param type
     *            The new mob type
     * @throws IllegalArgumentException
     *             If the type is not a living entity type
     */
    public void setBukkitEntityType(EntityType type);

    /**
     * Sets whether this NPC is <tt>flyable</tt> or not. Note that this is intended for normally <em>ground-based</em>
     * entities only - it will generally have no effect on mob types that were originally flyable.
     *
     * @param flyable
     */
    public void setFlyable(boolean flyable);

    /**
     * Sets the name of this NPC.
     *
     * @param name
     *            Name to give this NPC
     */
    public void setName(String name);

    /**
     * A helper method for using {@link #DEFAULT_PROTECTED_METADATA} to set the NPC as protected or not protected from
     * damage/entity target events. Equivalent to
     * <code>npc.data().set(NPC.DEFAULT_PROTECTED_METADATA, isProtected);</code>
     *
     * @param isProtected
     *            Whether the NPC should be protected
     */
    public void setProtected(boolean isProtected);

    /**
     * This method sets the skin of a player NPC given a player name
     *
     * @param playerName
     *            The name of a player
     */
    public void setSkin(String playerName) throws NotPlayerNPCException;

    /**
     * This method sets the skin of a player NPC given a player UUID
     *
     * @param playerUUID
     *            The UUID of a player
     */
    public void setSkin(UUID playerUUID) throws NotPlayerNPCException;

    /**
     * This method sets the skin of a player NPC given a OfflinePlayer
     *
     * @param player
     *            An OfflinePlayer
     */
    public void setSkin(OfflinePlayer player) throws NotPlayerNPCException;

    /**
     * This method sets the skin of a player NPC given a texture string and it's signature
     *
     * @param skin
     *            The skin string
     * @param signature
     *            The skin signature
     */
    public void setSkin(String skin, String signature) throws NotPlayerNPCException;

    /**
     * Attempts to spawn this NPC.
     *
     * @param location
     *            Location to spawn this NPC
     * @return Whether this NPC was able to spawn at the location
     */
    public boolean spawn(Location location);

    /**
     * An alternative to {{@link #getBukkitEntity().getLocation()} that teleports passengers as well.
     *
     * @param location
     *            The destination location
     * @param cause
     *            The cause for teleporting
     */
    public void teleport(Location location, TeleportCause cause);

    public static final String AMBIENT_SOUND_METADATA = "ambient-sound";
    public static final String COLLIDABLE_METADATA = "collidable";
    public static final String DAMAGE_OTHERS_METADATA = "damage-others";
    public static final String DEATH_SOUND_METADATA = "death-sound";
    public static final String DEFAULT_PROTECTED_METADATA = "protected";
    public static final String DROPS_ITEMS_METADATA = "drops-items";
    public static final String FLYABLE_METADATA = "flyable";
    public static final String GLOWING_COLOR_METADATA = "glowing-color";
    public static final String GLOWING_METADATA = "glowing";
    public static final String HURT_SOUND_METADATA = "hurt-sound";
    public static final String ITEM_DATA_METADATA = "item-type-data";
    public static final String ITEM_ID_METADATA = "item-type-id";
    public static final String LEASH_PROTECTED_METADATA = "protected-leash";
    public static final String MINECART_ITEM_DATA_METADATA = "minecart-item-data";
    public static final String MINECART_ITEM_METADATA = "minecart-item-name";
    public static final String MINECART_OFFSET_METADATA = "minecart-item-offset";
    public static final String NAMEPLATE_VISIBLE_METADATA = "nameplate-visible";
    public static final String PLAYER_SKIN_TEXTURE_PROPERTIES_METADATA = "player-skin-textures";
    public static final String PLAYER_SKIN_TEXTURE_PROPERTIES_SIGN_METADATA = "player-skin-signature";
    public static final String PLAYER_SKIN_USE_LATEST = "player-skin-use-latest";
    public static final String PLAYER_SKIN_UUID_METADATA = "player-skin-name";
    public static final String RESPAWN_DELAY_METADATA = "respawn-delay";
    public static final String SCOREBOARD_FAKE_TEAM_NAME_METADATA = "fake-scoreboard-team-name";
    public static final String SHOULD_SAVE_METADATA = "should-save";
    public static final String SILENT_METADATA = "silent-sounds";
    public static final String SWIMMING_METADATA = "swim";
    public static final String TARGETABLE_METADATA = "protected-target";
}