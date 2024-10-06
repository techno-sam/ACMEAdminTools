/*
 * ACME Admin Tools
 * Copyright (c) 2024 Sam Wagenaar and VivvyInks
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package io.github.slimeistdev.acme_admin.base.argument_types;

import com.google.gson.JsonObject;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.arguments.item.ItemInput;
import net.minecraft.commands.arguments.item.ItemParser;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

public class TagFilteredItemArgument implements ArgumentType<ItemInput> {
    private static final Collection<String> EXAMPLES = Arrays.asList("stick", "minecraft:stick", "stick{foo=bar}");
    private final HolderLookup<Item> items;
    private final TagKey<Item> filter;

    public TagFilteredItemArgument(CommandBuildContext context, TagKey<Item> filter) {
        this.items = new HolderLookup.Delegate<>(context.holderLookup(Registries.ITEM)) {
            @Override
            public @NotNull Optional<Holder.Reference<Item>> get(ResourceKey<Item> resourceKey) {
                return this.parent.get(resourceKey).filter(reference -> reference.is(filter));
            }

            @Override
            public @NotNull Stream<Holder.Reference<Item>> listElements() {
                return this.parent.listElements().filter(reference -> reference.is(filter));
            }
        };
        this.filter = filter;
    }

    public static TagFilteredItemArgument filteredItem(CommandBuildContext context, TagKey<Item> filter) {
        return new TagFilteredItemArgument(context, filter);
    }

    @Override
    public ItemInput parse(StringReader reader) throws CommandSyntaxException {
        ItemParser.ItemResult itemResult = ItemParser.parseForItem(this.items, reader);
        return new ItemInput(itemResult.item(), itemResult.nbt());
    }

    public static <S> ItemInput getItem(CommandContext<S> context, String name) {
        return context.getArgument(name, ItemInput.class);
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        return ItemParser.fillSuggestions(this.items, builder, false);
    }

    @Override
    public Collection<String> getExamples() {
        return EXAMPLES;
    }

    public static class Info implements ArgumentTypeInfo<TagFilteredItemArgument, Info.Template> {
        @Override
        public void serializeToNetwork(Template template, FriendlyByteBuf buffer) {
            buffer.writeResourceLocation(template.filter.location());
        }

        @Override
        public @NotNull Template deserializeFromNetwork(FriendlyByteBuf buffer) {
            TagKey<Item> filter = TagKey.create(Registries.ITEM, buffer.readResourceLocation());
            return new Template(filter);
        }

        @Override
        public void serializeToJson(Template template, JsonObject json) {
            json.addProperty("filter", template.filter.location().toString());
        }

        @Override
        public @NotNull Template unpack(TagFilteredItemArgument argument) {
            return new Template(argument.filter);
        }

        public final class Template implements ArgumentTypeInfo.Template<TagFilteredItemArgument> {
            final TagKey<Item> filter;

            public Template(TagKey<Item> filter) {
                this.filter = filter;
            }

            @Override
            public @NotNull TagFilteredItemArgument instantiate(CommandBuildContext context) {
                return TagFilteredItemArgument.filteredItem(context, this.filter);
            }

            @Override
            public @NotNull ArgumentTypeInfo<TagFilteredItemArgument, ?> type() {
                return Info.this;
            }
        }
    }
}
