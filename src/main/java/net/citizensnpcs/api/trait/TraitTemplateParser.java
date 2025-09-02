package net.citizensnpcs.api.trait;

import java.util.function.BiFunction;

import net.citizensnpcs.api.command.CommandContext;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.util.DataKey;

public interface TraitTemplateParser {
    ShortTemplateParser getShortTemplateParser();

    TemplateParser getTemplateParser();

    public static interface ShortTemplateParser extends BiFunction<NPC, CommandContext, Trait> {
    }

    public static interface TemplateParser extends BiFunction<NPC, DataKey, Trait> {
    }
}
