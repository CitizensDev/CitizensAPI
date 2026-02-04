package net.citizensnpcs.api.ai.tree;

import java.time.Duration;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.bukkit.Location;
import org.bukkit.block.Block;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.ai.goals.WanderGoal;
import net.citizensnpcs.api.expr.ExpressionRegistry;
import net.citizensnpcs.api.expr.ExpressionRegistry.ExpressionValue;
import net.citizensnpcs.api.expr.ExpressionScope;
import net.citizensnpcs.api.expr.Memory;
import net.citizensnpcs.api.npc.BlockBreaker;
import net.citizensnpcs.api.npc.BlockBreaker.BlockBreakerConfiguration;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.util.DataKey;
import net.citizensnpcs.api.util.SpigotUtil;

/**
 * Registry for behavior factories that can be instantiated from {@link DataKey} configurations.
 */
public class BehaviorRegistry {
    private final Map<String, BehaviorFactory> behaviors = new HashMap<>();
    private final ExpressionRegistry expressions;
    private final BehaviorSignals signals;

    public BehaviorRegistry(ExpressionRegistry expressions) {
        this.expressions = expressions;
        this.signals = new BehaviorSignals();
        registerDefaults();
    }

    public BehaviorRegistry(ExpressionRegistry expressionRegistry, BehaviorSignals signals) {
        this.expressions = expressionRegistry;
        this.signals = signals;
        registerDefaults();
    }

    /**
     * Creates a behavior instance from the registry.
     *
     * @param name
     *            the behavior name
     * @param params
     *            the DataKey containing parameters
     * @param context
     *            the creation context
     * @return the created behavior, or null if not found
     */
    public Behavior createBehavior(String name, DataKey params, BehaviorContext context) {
        BehaviorFactory factory = behaviors.get(name.toLowerCase(Locale.ROOT));
        if (factory == null)
            return null;

        return factory.create(params, context);
    }

    public ExpressionRegistry getExpressionRegistry() {
        return expressions;
    }

    public BehaviorSignals getSignalManager() {
        return signals;
    }

    /**
     * Checks if a behavior is registered.
     *
     * @param name
     *            the behavior name
     * @return true if registered
     */
    public boolean hasBehavior(String name) {
        return behaviors.containsKey(name.toLowerCase(Locale.ROOT));
    }

    /**
     * Helper to parse duration with time unit support (e.g., "5s", "10m", "100t"). Falls back to expression evaluation
     * if not a valid duration string.
     */
    protected int parseDuration(String durationStr, ExpressionScope scope) {
        if (durationStr == null || durationStr.isEmpty())
            return 20;

        int ticks = SpigotUtil.parseTicks(durationStr);
        if (ticks >= 0)
            return ticks;

        ExpressionValue holder = expressions.parseValue(durationStr);
        return (int) holder.evaluateAsNumber(scope);
    }

    /**
     * Registers a behavior factory.
     *
     * @param name
     *            the behavior name
     * @param factory
     *            the factory to create behavior instances
     */
    public void registerBehavior(String name, BehaviorFactory factory) {
        behaviors.put(name.toLowerCase(), factory);
    }

    /**
     * Registers default built-in behaviors.
     */
    protected void registerDefaults() {
        // Wait behavior - waits for a number of ticks
        registerBehavior("wait", (params, context) -> {
            String durationStr = params != null ? params.getString("") : "20";
            ExpressionRegistry.ExpressionValue durationHolder = expressions.parseValue(durationStr);

            return new Behavior() {
                private int ticksRemaining;

                @Override
                public void reset() {
                    ticksRemaining = SpigotUtil.parseTicks(durationHolder.evaluateAsString(context.getScope()));
                }

                @Override
                public BehaviorStatus run() {
                    if (--ticksRemaining <= 0)
                        return BehaviorStatus.SUCCESS;

                    return BehaviorStatus.RUNNING;
                }

                @Override
                public boolean shouldExecute() {
                    ticksRemaining = parseDuration(durationStr, context.getScope());
                    return ticksRemaining > 0;
                }
            };
        });

        // Emits a signal for other behaviors to receive
        registerBehavior("emit_signal", (params, context) -> {
            String signalName = context.getArgOrParam(0, "", params, "");
            if (signalName.isEmpty() && params != null) {
                signalName = params.getString("");
            }
            ExpressionValue value = expressions.parseValue(signalName);

            return (InstantBehavior) () -> {
                String signal = value.evaluateAsString(context.getScope());
                signals.emit(context.getNPC(), signal);
                return BehaviorStatus.SUCCESS;
            };
        });

        // Emits a signal to a specific NPC by ID or UUID
        registerBehavior("emit_signal_to", (params, context) -> {
            String npcIdStr = context.getArgOrParam(0, "npc", params, null);
            String signalName = context.getArgOrParam(1, "signal", params, null);
            if (npcIdStr == null || signalName == null)
                return null;

            ExpressionRegistry.ExpressionValue npcIdHolder = expressions.parseValue(npcIdStr);
            ExpressionRegistry.ExpressionValue signalHolder = expressions.parseValue(signalName);

            return (InstantBehavior) () -> {
                Object idValue = npcIdHolder.evaluate(context.getScope());
                NPC npc = null;

                if (idValue instanceof String) {
                    String idString = (String) idValue;
                    try {
                        UUID uuid = UUID.fromString(idString);
                        npc = CitizensAPI.getNPCRegistry().getByUniqueIdGlobal(uuid);
                    } catch (IllegalArgumentException e) {
                        try {
                            int npcId = Integer.parseInt(idString);
                            npc = CitizensAPI.getNPCRegistry().getById(npcId);
                        } catch (NumberFormatException ignored) {
                        }
                    }
                } else if (idValue instanceof Number) {
                    int npcId = ((Number) idValue).intValue();
                    npc = CitizensAPI.getNPCRegistry().getById(npcId);
                }
                if (npc == null)
                    return BehaviorStatus.FAILURE;
                signals.emitToNPC(npc.getUniqueId(), signalHolder.evaluateAsString(context.getScope()));
                return BehaviorStatus.SUCCESS;
            };
        });

        // Emits a signal that all NPCs can receive
        registerBehavior("emit_global_signal", (params, context) -> {
            String signalName = context.getArgOrParam(0, "", params, "");
            if (signalName.isEmpty() && params != null) {
                signalName = params.getString("");
            }
            ExpressionRegistry.ExpressionValue signalHolder = expressions.parseValue(signalName);

            return (InstantBehavior) () -> {
                String signal = signalHolder.evaluateAsString(context.getScope());
                signals.emitGlobal(signal);
                return BehaviorStatus.SUCCESS;
            };
        });

        // Blocks until a signal is received
        registerBehavior("wait_for_signal", (params, context) -> {
            String signalName = context.getArgOrParam(0, "", params, "");
            if (signalName.isEmpty() && params != null) {
                signalName = params.getString("");
            }
            ExpressionRegistry.ExpressionValue signalHolder = expressions.parseValue(signalName);

            return new Behavior() {
                private String currentSignal;
                private BehaviorSignals.SignalListener listener;
                private boolean signalReceived = false;

                @Override
                public void reset() {
                    signalReceived = false;
                    if (listener != null && currentSignal != null) {
                        signals.unlisten(context.getNPC(), currentSignal, listener);
                        listener = null;
                        currentSignal = null;
                    }
                }

                @Override
                public BehaviorStatus run() {
                    if (signalReceived) {
                        signals.unlisten(context.getNPC(), currentSignal, listener);
                        listener = null;
                        return BehaviorStatus.SUCCESS;
                    }
                    return BehaviorStatus.RUNNING;
                }

                @Override
                public boolean shouldExecute() {
                    currentSignal = signalHolder.evaluateAsString(context.getScope());
                    listener = () -> signalReceived = true;
                    signals.listen(context.getNPC(), currentSignal, listener);
                    return true;
                }
            };
        });

        // Always succeeds
        registerBehavior("succeed", (params, context) -> (InstantBehavior) () -> BehaviorStatus.SUCCESS);

        // Repeats x times
        registerBehavior("repeat", (params, context) -> {
            String countStr = context.getArgOrParam(0, "count", params, "1");
            ExpressionValue countHolder = CitizensAPI.getExpressionRegistry().parseValue(countStr);

            if (params == null || !params.hasSubKeys())
                return null;

            return new Behavior() {
                private int remaining;
                private int total;

                @Override
                public void reset() {
                    total = (int) countHolder.evaluateAsNumber(context.getScope());
                    remaining = total;
                }

                @Override
                public BehaviorStatus run() {
                    if (remaining <= 0)
                        return BehaviorStatus.SUCCESS;

                    remaining--;
                    return BehaviorStatus.RUNNING;
                }

                @Override
                public boolean shouldExecute() {
                    total = (int) countHolder.evaluateAsNumber(context.getScope());
                    remaining = total;
                    return total > 0;
                }
            };
        });

        // Always fails
        registerBehavior("fail", (params, context) -> (InstantBehavior) () -> BehaviorStatus.FAILURE);

        // Sets a memory variable
        registerBehavior("set", (params, context) -> {
            String key = context.getArgOrParam(0, "key", params, null);
            String valueStr = context.getArgOrParam(1, "value", params, null);
            if (key == null || valueStr == null)
                return null;

            ExpressionRegistry.ExpressionValue valueHolder = expressions.parseValue(valueStr);

            return (InstantBehavior) () -> {
                Object value = valueHolder.evaluate(context.getScope());
                context.getMemory().set(key, value);
                return BehaviorStatus.SUCCESS;
            };
        });

        // Waits until a condition becomes true
        registerBehavior("wait_until", (params, context) -> {
            String conditionStr = context.getArgOrParam(0, "condition", params, null);
            if (conditionStr == null)
                return null;

            ExpressionRegistry.ExpressionValue conditionHolder = expressions.parseValue(conditionStr);

            return new Behavior() {
                @Override
                public void reset() {
                }

                @Override
                public BehaviorStatus run() {
                    if (conditionHolder.evaluateAsBoolean(context.getScope()))
                        return BehaviorStatus.SUCCESS;

                    return BehaviorStatus.RUNNING;
                }

                @Override
                public boolean shouldExecute() {
                    return true;
                }
            };
        });

        // Rate limits execution using memory
        // Usage: cooldown key 5s, cooldown key 10m, cooldown key {expression}
        registerBehavior("cooldown", (params, context) -> {
            String key = context.getArgOrParam(0, "key", params, null);
            String durationStr = context.getArgOrParam(1, "duration", params, "1s");
            if (key == null)
                return null;
            ExpressionRegistry.ExpressionValue durationHolder = expressions.parseValue(durationStr);
            String cooldownKey = "cooldown." + key;

            return (InstantBehavior) () -> {
                long lastUsed = (long) context.getMemory().getNumber(cooldownKey, 0);
                Duration duration = SpigotUtil.parseDuration(durationHolder.evaluateAsString(context.getScope()),
                        TimeUnit.MILLISECONDS);

                if (System.currentTimeMillis() - lastUsed >= duration.toMillis()) {
                    context.getMemory().set(cooldownKey, System.currentTimeMillis());
                    return BehaviorStatus.SUCCESS;
                }
                return BehaviorStatus.FAILURE;
            };
        });

        // Forgets a memory variable
        registerBehavior("forget", (params, context) -> {
            String key = context.getArgOrParam(0, "key", params, null);
            if (key == null)
                return null;

            return (InstantBehavior) () -> {
                context.getMemory().remove(key);
                return BehaviorStatus.SUCCESS;
            };
        });

        // Clears all memory
        registerBehavior("clear_memory", (params, context) -> {
            return (InstantBehavior) () -> {
                context.getMemory().clear();
                return BehaviorStatus.SUCCESS;
            };
        });

        // Walkto behavior - walks to coordinates
        // Inline usage: walkto 100 64 200 speed=1.5 range=50
        // Named usage:
        // walkto:
        // x: 100
        // y: 64
        // z: 200
        // speed: 1.5
        // range: 50
        // distance_margin: 2
        registerBehavior("walkto", (params, context) -> {
            String xStr = null;
            String yStr = null;
            String zStr = null;
            String speedStr = null;
            String rangeStr = null;
            String distanceMarginStr = null;

            String[] args = context.getArgs();
            if (args != null && args.length >= 3) {
                xStr = args[0];
                yStr = args[1];
                zStr = args[2];

                for (int i = 3; i < args.length; i++) {
                    String arg = args[i];
                    if (arg.contains("=")) {
                        String[] parts = arg.split("=", 2);
                        String key = parts[0].toLowerCase();
                        String value = parts[1];

                        switch (key) {
                            case "speed":
                                speedStr = value;
                                break;
                            case "range":
                                rangeStr = value;
                                break;
                            case "distance_margin":
                            case "margin":
                                distanceMarginStr = value;
                                break;
                        }
                    }
                }
            } else if (params != null) {
                xStr = params.getString("x", null);
                yStr = params.getString("y", null);
                zStr = params.getString("z", null);
                speedStr = params.getString("speed", null);
                rangeStr = params.getString("range", null);
                distanceMarginStr = params.getString("distance_margin", null);
            }
            if (xStr == null || yStr == null || zStr == null)
                return null;

            ExpressionRegistry.ExpressionValue xHolder = expressions.parseValue(xStr);
            ExpressionRegistry.ExpressionValue yHolder = expressions.parseValue(yStr);
            ExpressionRegistry.ExpressionValue zHolder = expressions.parseValue(zStr);
            ExpressionRegistry.ExpressionValue speedHolder = speedStr != null ? expressions.parseValue(speedStr) : null;
            ExpressionRegistry.ExpressionValue rangeHolder = rangeStr != null ? expressions.parseValue(rangeStr) : null;
            ExpressionRegistry.ExpressionValue distanceMarginHolder = distanceMarginStr != null
                    ? expressions.parseValue(distanceMarginStr)
                    : null;

            return new Behavior() {
                private boolean started = false;

                @Override
                public void reset() {
                    started = false;
                }

                @Override
                public BehaviorStatus run() {
                    NPC npc = context.getNPC();
                    if (!started) {
                        double x = xHolder.evaluateAsNumber(context.getScope());
                        double y = yHolder.evaluateAsNumber(context.getScope());
                        double z = zHolder.evaluateAsNumber(context.getScope());
                        Location target = new Location(npc.getStoredLocation().getWorld(), x, y, z);

                        npc.getNavigator().setTarget(target);
                        if (speedHolder != null) {
                            npc.getNavigator().getLocalParameters()
                                    .speedModifier((float) speedHolder.evaluateAsNumber(context.getScope()));
                        }
                        if (rangeHolder != null) {
                            npc.getNavigator().getLocalParameters()
                                    .range((float) rangeHolder.evaluateAsNumber(context.getScope()));
                        }
                        if (distanceMarginHolder != null) {
                            npc.getNavigator().getLocalParameters()
                                    .distanceMargin(distanceMarginHolder.evaluateAsNumber(context.getScope()));
                        }
                        started = true;
                    }
                    return npc.getNavigator().isNavigating() ? BehaviorStatus.RUNNING : BehaviorStatus.SUCCESS;
                }

                @Override
                public boolean shouldExecute() {
                    return context.getNPC().isSpawned() && !context.getNPC().getNavigator().isNavigating();
                }
            };
        });

        // Wander behavior - random walk within radius
        registerBehavior("wander", (params, context) -> {
            Map<String, String> parsedArgs = context.parseArgs(1, params);
            String radiusStr = context.getFromParsedArgs(parsedArgs, "0", "radius");
            String pathfindStr = context.getFromParsedArgs(parsedArgs, "1", "pathfind");
            if (radiusStr == null) {
                radiusStr = "10";
            }
            ExpressionRegistry.ExpressionValue radiusHolder = expressions.parseValue(radiusStr);
            ExpressionRegistry.ExpressionValue pathfindHolder = expressions.parseValue(pathfindStr);

            return new Behavior() {
                private WanderGoal wander = null;

                @Override
                public void reset() {
                    wander = null;
                }

                @Override
                public BehaviorStatus run() {
                    NPC npc = context.getNPC();
                    if (!npc.isSpawned())
                        return BehaviorStatus.FAILURE;

                    if (wander == null) {
                        int radius = (int) radiusHolder.evaluateAsNumber(context.getScope());
                        wander = WanderGoal.builder(npc).xrange(radius).yrange(radius)
                                .pathfind(pathfindHolder.evaluateAsBoolean(context.getScope())).build();
                    }
                    return wander.run();
                }

                @Override
                public boolean shouldExecute() {
                    return context.getNPC().isSpawned();
                }
            };
        });

        // Break-block behavior - break a block at coordinates using BlockBreaker
        registerBehavior("break_block", (params, context) -> {
            Map<String, String> parsedArgs = context.parseArgs(3, params);
            String xStr = context.getFromParsedArgs(parsedArgs, "0", "x");
            String yStr = context.getFromParsedArgs(parsedArgs, "1", "y");
            String zStr = context.getFromParsedArgs(parsedArgs, "2", "z");
            String radiusStr = context.getFromParsedArgs(parsedArgs, "3", "radius");

            if (xStr == null || yStr == null || zStr == null) {
                return null;
            }
            if (radiusStr == null) {
                radiusStr = "3";
            }
            ExpressionRegistry.ExpressionValue xHolder = expressions.parseValue(xStr);
            ExpressionRegistry.ExpressionValue yHolder = expressions.parseValue(yStr);
            ExpressionRegistry.ExpressionValue zHolder = expressions.parseValue(zStr);
            ExpressionRegistry.ExpressionValue radiusHolder = expressions.parseValue(radiusStr);

            return new Behavior() {
                private BlockBreaker breaker = null;

                @Override
                public void reset() {
                    if (breaker != null) {
                        breaker.reset();
                        breaker = null;
                    }
                }

                @Override
                public BehaviorStatus run() {
                    NPC npc = context.getNPC();
                    if (!npc.isSpawned())
                        return BehaviorStatus.FAILURE;

                    if (breaker == null) {
                        int x = (int) xHolder.evaluateAsNumber(context.getScope());
                        int y = (int) yHolder.evaluateAsNumber(context.getScope());
                        int z = (int) zHolder.evaluateAsNumber(context.getScope());
                        double radius = radiusHolder.evaluateAsNumber(context.getScope());

                        Block target = npc.getEntity().getWorld().getBlockAt(x, y, z);
                        if (target.isEmpty())
                            return BehaviorStatus.SUCCESS;

                        BlockBreakerConfiguration cfg = new BlockBreakerConfiguration();
                        if (radius > 0) {
                            cfg.radius(radius);
                        }
                        breaker = npc.getBlockBreaker(target, cfg);
                    }
                    return breaker.run();
                }

                @Override
                public boolean shouldExecute() {
                    return context.getNPC().isSpawned();
                }
            };
        });
    }

    /**
     * Context passed to behavior factories during creation. Provides access to NPC, expression scope, memory, and
     * argument parsing utilities.
     */
    public static class BehaviorContext {
        private String[] args;
        private final ExpressionRegistry expressionRegistry;
        private final Memory memory;
        private final NPC npc;
        private final ExpressionScope scope;

        public BehaviorContext(NPC npc, ExpressionScope scope, ExpressionRegistry expressionRegistry, Memory memory) {
            this.npc = npc;
            this.scope = scope;
            this.expressionRegistry = expressionRegistry;
            this.memory = memory;
        }

        public String getArgOrParam(int index, String name, DataKey params, String defaultValue) {
            if (args != null && index < args.length)
                return args[index];

            if (params != null)
                return params.getString(name, defaultValue);

            return defaultValue;
        }

        public String[] getArgs() {
            return args;
        }

        public ExpressionRegistry getExpressionRegistry() {
            return expressionRegistry;
        }

        public String getFromParsedArgs(Map<String, String> parsedArgs, String... keys) {
            for (String key : keys) {
                String value = parsedArgs.get(key);
                if (value != null) {
                    return value;
                }
            }
            return null;
        }

        public Memory getMemory() {
            return memory;
        }

        public NPC getNPC() {
            return npc;
        }

        public ExpressionScope getScope() {
            return scope;
        }

        public Map<String, String> parseArgs(int positionalCount, DataKey params) {
            Map<String, String> result = new HashMap<>();

            if (args != null && args.length >= positionalCount) {
                for (int i = 0; i < positionalCount && i < args.length; i++) {
                    result.put(String.valueOf(i), args[i]);
                }
                for (int i = positionalCount; i < args.length; i++) {
                    String arg = args[i];
                    if (arg.contains("=")) {
                        String[] parts = arg.split("=", 2);
                        result.put(parts[0].toLowerCase(), parts[1]);
                    }
                }
            } else if (params != null) {
                for (DataKey sub : params.getSubKeys()) {
                    result.put(sub.name(), params.getString(sub.name()));
                }
            }
            return result;
        }

        public void setArgs(String[] args) {
            this.args = args;
        }
    }

    @FunctionalInterface
    public interface BehaviorFactory {
        /**
         * Creates a behavior instance.
         *
         * @param params
         *            the DataKey containing parameters (may be null)
         * @param context
         *            the creation context with NPC and scope
         * @return the created behavior, or null if creation fails
         */
        Behavior create(DataKey params, BehaviorContext context);
    }
}
