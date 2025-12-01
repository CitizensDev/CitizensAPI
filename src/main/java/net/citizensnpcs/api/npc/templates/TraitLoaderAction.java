package net.citizensnpcs.api.npc.templates;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import org.bukkit.Bukkit;

import com.google.common.collect.Lists;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.command.CommandContext;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.npc.templates.TemplateRegistry.TemplateErrorReporter;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.trait.TraitTemplateParser;
import net.citizensnpcs.api.trait.TraitTemplateParser.ShortTemplateParser;
import net.citizensnpcs.api.trait.TraitTemplateParser.TemplateParser;
import net.citizensnpcs.api.trait.TraitTemplateParser.TraitParserContext;
import net.citizensnpcs.api.util.DataKey;

public class TraitLoaderAction implements Consumer<NPC> {
    private final List<Function<NPC, Trait>> actions = Lists.newArrayList();

    public TraitLoaderAction(TemplateErrorReporter errors, TemplateWorkspace workspace, DataKey traits) {
        for (DataKey key : traits.getIntegerSubKeys()) {
            if (key.hasSubKeys()) {
                if (!key.keyExists("name")) {
                    errors.addError(key.getPath() + ": Missing trait name");
                    continue;
                }
                String traitName = key.getString("name");
                TraitTemplateParser parser = CitizensAPI.getTraitFactory().getTemplateParser(traitName);
                if (parser == null) {
                    errors.addError(key.getPath() + ": Unknown trait " + traitName);
                    continue;
                }
                TemplateParser tp = parser.getTemplateParser();
                if (tp != null) {
                    actions.add(npc -> tp.apply(new TraitParserContext(npc, workspace), key));
                }
            } else {
                String shortTraitDescriptor = key.getString("").trim();
                String[] parts = shortTraitDescriptor.split(" ");
                String traitNamePartial = parts[0];
                TraitTemplateParser parser = CitizensAPI.getTraitFactory().getTemplateParser(traitNamePartial);
                if (parser == null) {
                    errors.addError(key.getPath() + ": Unknown trait " + traitNamePartial);
                    continue;
                }
                ShortTemplateParser stp = parser.getShortTemplateParser();
                if (stp != null) {
                    CommandContext ctx = new CommandContext(Bukkit.getConsoleSender(), parts);
                    actions.add(npc -> stp.apply(new TraitParserContext(npc, workspace), ctx));
                }
            }
        }
    }

    @Override
    public void accept(NPC npc) {
        for (Function<NPC, Trait> action : actions) {
            Trait trait = action.apply(npc);
            if (trait != null) {
                npc.addTrait(trait);
            }
        }
    }
}
