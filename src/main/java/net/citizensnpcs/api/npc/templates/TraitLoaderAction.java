package net.citizensnpcs.api.npc.templates;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import org.bukkit.Bukkit;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.command.CommandContext;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.trait.TraitTemplateParser;
import net.citizensnpcs.api.trait.TraitTemplateParser.ShortTemplateParser;
import net.citizensnpcs.api.trait.TraitTemplateParser.TemplateParser;
import net.citizensnpcs.api.util.DataKey;
import net.citizensnpcs.api.util.Messaging;

public class TraitLoaderAction implements Consumer<NPC> {
    private final List<Function<NPC, Trait>> actions = Lists.newArrayList();

    public TraitLoaderAction(DataKey traits) {
        for (DataKey key : traits.getIntegerSubKeys()) {
            List<DataKey> list = ImmutableList.copyOf(key.getSubKeys());
            if (list.isEmpty()) {
                String shortTraitDescriptor = key.getString("").trim();
                String[] parts = shortTraitDescriptor.split(" ");
                String traitNamePartial = parts[0];
                TraitTemplateParser parser = CitizensAPI.getTraitFactory().getTemplateParser(traitNamePartial);
                if (parser == null) {
                    // TODO: this should be reported centrally instead
                    Messaging.severe("Unknown trait", traitNamePartial);
                    continue;
                }
                ShortTemplateParser stp = parser.getShortTemplateParser();
                if (stp != null) {
                    CommandContext ctx = new CommandContext(Bukkit.getConsoleSender(), parts);
                    actions.add(npc -> stp.apply(npc, ctx));
                }
            } else {
                if (!key.keyExists("name")) {
                    // TODO: this should be reported centrally instead
                    Messaging.severe("Missing trait name");
                    continue;
                }
                String traitName = key.getString("name");
                TraitTemplateParser parser = CitizensAPI.getTraitFactory().getTemplateParser(traitName);
                if (parser == null) {
                    // TODO: this should be reported centrally instead
                    Messaging.severe("Unknown trait", traitName);
                    continue;
                }
                TemplateParser tp = parser.getTemplateParser();
                if (tp != null) {
                    actions.add(npc -> tp.apply(npc, key));
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
