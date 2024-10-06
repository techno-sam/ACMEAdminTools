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

package io.github.slimeistdev.acme_admin.content.commands.server;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import io.github.slimeistdev.acme_admin.base.argument_types.TagFilteredItemArgument;
import io.github.slimeistdev.acme_admin.content.effects.utils.StackEffectManipulator;
import io.github.slimeistdev.acme_admin.registration.ACMEItemTags;
import io.github.slimeistdev.acme_admin.utils.AuthUtils;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.ComponentArgument;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.ResourceArgument;
import net.minecraft.commands.arguments.item.ItemInput;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

public class PotionCommand {
    private static final SimpleCommandExceptionType ERROR_NO_VESSELS = new SimpleCommandExceptionType(Component.translatable("commands.acme_admin.potion.error.no_vessels"));

    public static ArgumentBuilder<CommandSourceStack, ?> register(CommandBuildContext context) {
        return literal("potion")
            .requires(AuthUtils::isAuthorized)
            .then(argument("targets", EntityArgument.players())
                .then($addEffect(context))
                .then($removeEffect(context))
                .then($mergeEffects())
                .then($transmuteVessel(context))
                .then($customColor())
                .then($customName())
            );
    }

    private static ArgumentBuilder<CommandSourceStack, ?> $addEffect(CommandBuildContext context) {
        return literal("add_effect")
            .then(argument("effect", ResourceArgument.resource(context, Registries.MOB_EFFECT))
                .executes(ctx -> run_addEffect(
                    ctx.getSource(),
                    EntityArgument.getPlayers(ctx, "targets"),
                    ResourceArgument.getMobEffect(ctx, "effect"),
                    null,
                    0,
                    true
                ))
                .then(
                    Commands.argument("seconds", IntegerArgumentType.integer(1, 1000000))
                        .executes(
                            commandContext -> run_addEffect(
                                commandContext.getSource(),
                                EntityArgument.getPlayers(commandContext, "targets"),
                                ResourceArgument.getMobEffect(commandContext, "effect"),
                                IntegerArgumentType.getInteger(commandContext, "seconds"),
                                0,
                                true
                            )
                        )
                        .then(
                            Commands.argument("amplifier", IntegerArgumentType.integer(0, 255))
                                .executes(
                                    commandContext -> run_addEffect(
                                        commandContext.getSource(),
                                        EntityArgument.getPlayers(commandContext, "targets"),
                                        ResourceArgument.getMobEffect(commandContext, "effect"),
                                        IntegerArgumentType.getInteger(commandContext, "seconds"),
                                        IntegerArgumentType.getInteger(commandContext, "amplifier"),
                                        true
                                    )
                                )
                                .then(
                                    Commands.argument("hideParticles", BoolArgumentType.bool())
                                        .executes(
                                            commandContext -> run_addEffect(
                                                commandContext.getSource(),
                                                EntityArgument.getPlayers(commandContext, "targets"),
                                                ResourceArgument.getMobEffect(commandContext, "effect"),
                                                IntegerArgumentType.getInteger(commandContext, "seconds"),
                                                IntegerArgumentType.getInteger(commandContext, "amplifier"),
                                                !BoolArgumentType.getBool(commandContext, "hideParticles")
                                            )
                                        )
                                )
                        )
                )
                .then(
                    Commands.literal("infinite")
                        .executes(
                            commandContext -> run_addEffect(
                                commandContext.getSource(),
                                EntityArgument.getPlayers(commandContext, "targets"),
                                ResourceArgument.getMobEffect(commandContext, "effect"),
                                -1,
                                0,
                                true
                            )
                        )
                        .then(
                            Commands.argument("amplifier", IntegerArgumentType.integer(0, 255))
                                .executes(
                                    commandContext -> run_addEffect(
                                        commandContext.getSource(),
                                        EntityArgument.getPlayers(commandContext, "targets"),
                                        ResourceArgument.getMobEffect(commandContext, "effect"),
                                        -1,
                                        IntegerArgumentType.getInteger(commandContext, "amplifier"),
                                        true
                                    )
                                )
                                .then(
                                    Commands.argument("hideParticles", BoolArgumentType.bool())
                                        .executes(
                                            commandContext -> run_addEffect(
                                                commandContext.getSource(),
                                                EntityArgument.getPlayers(commandContext, "targets"),
                                                ResourceArgument.getMobEffect(commandContext, "effect"),
                                                -1,
                                                IntegerArgumentType.getInteger(commandContext, "amplifier"),
                                                !BoolArgumentType.getBool(commandContext, "hideParticles")
                                            )
                                        )
                                )
                        )
                )
            );
    }

    private static ArgumentBuilder<CommandSourceStack, ?> $removeEffect(CommandBuildContext context) {
        return literal("remove_effect")
            .then(argument("effect", ResourceArgument.resource(context, Registries.MOB_EFFECT))
                .executes(ctx -> run_removeEffect(
                    ctx.getSource(),
                    EntityArgument.getPlayers(ctx, "targets"),
                    ResourceArgument.getMobEffect(ctx, "effect")
                ))
            );
    }

    private static ArgumentBuilder<CommandSourceStack, ?> $mergeEffects() {
        return literal("merge_effects")
            .executes(ctx -> run_mergeEffects(
                ctx.getSource(),
                EntityArgument.getPlayers(ctx, "targets")
            ));
    }

    private static ArgumentBuilder<CommandSourceStack, ?> $transmuteVessel(CommandBuildContext context) {
        return literal("transmute_vessel")
            .then(argument("into", TagFilteredItemArgument.filteredItem(context, ACMEItemTags.POTION_VESSELS))
                .executes(ctx -> run_transmuteVessel(
                    ctx.getSource(),
                    EntityArgument.getPlayers(ctx, "targets"),
                    TagFilteredItemArgument.getItem(ctx, "into")
                ))
            );
    }

    private static ArgumentBuilder<CommandSourceStack, ?> $customColor() {
        return literal("custom_color")
            .then(literal("reset")
                .executes(ctx -> run_customColor(
                    ctx.getSource(),
                    EntityArgument.getPlayers(ctx, "targets"),
                    null
                ))
            )
            .then(argument("red", IntegerArgumentType.integer(0, 255))
                .then(argument("green", IntegerArgumentType.integer(0, 255))
                    .then(argument("blue", IntegerArgumentType.integer(0, 255))
                        .executes(ctx -> run_customColor(
                            ctx.getSource(),
                            EntityArgument.getPlayers(ctx, "targets"),
                            IntegerArgumentType.getInteger(ctx, "red") << 16 |
                                IntegerArgumentType.getInteger(ctx, "green") << 8 |
                                IntegerArgumentType.getInteger(ctx, "blue")
                        ))
                    )
                )
            );
    }

    private static ArgumentBuilder<CommandSourceStack, ?> $customName() {
        return literal("custom_name")
            .then(argument("name", ComponentArgument.textComponent())
                .executes(ctx -> run_customName(
                    ctx.getSource(),
                    EntityArgument.getPlayers(ctx, "targets"),
                    ComponentArgument.getComponent(ctx, "name")
                ))
            );
    }

    private static @NotNull Component describeEffect(@NotNull Holder<MobEffect> effect) {
        return effect.unwrapKey()
            .map(ResourceKey::location)
            .map(rl -> Component.translatable("effect.%s.%s".formatted(rl.getNamespace(), rl.getPath())))
            .orElse(Component.literal("Unknown"));
    }

    private static int run_addEffect(
        @NotNull CommandSourceStack source,
        @NotNull Collection<ServerPlayer> targets,
        @NotNull Holder<MobEffect> effect,
        @Nullable Integer seconds,
        int amplifier,
        boolean showParticles
    ) throws CommandSyntaxException {
        MobEffect mobEffect = effect.value();
        int count = 0;
        ServerPlayer lastSuccess = null;
        int ticks;

        if (seconds != null) {
            if (mobEffect.isInstantenous()) {
                ticks = seconds;
            } else if (seconds == -1) {
                ticks = -1;
            } else {
                ticks = seconds * 20;
            }
        } else if (mobEffect.isInstantenous()) {
            ticks = 1;
        } else {
            ticks = 600;
        }

        for (ServerPlayer target : targets) {
            StackEffectManipulator manipulator = StackEffectManipulator.mutableFromHand(target, InteractionHand.MAIN_HAND);
            if (manipulator == null && target.getMainHandItem().isEmpty()) {
                target.setItemInHand(InteractionHand.MAIN_HAND, Items.POTION.getDefaultInstance());
                manipulator = StackEffectManipulator.mutableFromHand(target, InteractionHand.MAIN_HAND);
            }
            if (manipulator != null) {
                manipulator.addEffect(new MobEffectInstance(mobEffect, ticks, amplifier, false, showParticles));
                manipulator.apply();

                count++;
                lastSuccess = target;
            }
        }

        if (count == 0) {
            throw ERROR_NO_VESSELS.create();
        } else if (count == 1) {
            Component finalLastSuccess = lastSuccess.getDisplayName();
            source.sendSuccess(
                () -> Component.translatable("commands.acme_admin.potion.add_effect.success.single", describeEffect(effect), finalLastSuccess),
                true
            );
        } else {
            int finalCount = count;
            source.sendSuccess(
                () -> Component.translatable("commands.acme_admin.potion.add_effect.success.multiple", describeEffect(effect), finalCount),
                true
            );
        }

        return count;
    }

    private static int run_removeEffect(
        @NotNull CommandSourceStack source,
        @NotNull Collection<ServerPlayer> targets,
        @NotNull Holder<MobEffect> effect
    ) throws CommandSyntaxException {
        MobEffect mobEffect = effect.value();
        int count = 0;
        ServerPlayer lastSuccess = null;

        for (ServerPlayer target : targets) {
            StackEffectManipulator manipulator = StackEffectManipulator.mutableFromHand(target, InteractionHand.MAIN_HAND);
            if (manipulator != null) {
                manipulator.removeEffect(mobEffect);
                manipulator.apply();

                count++;
                lastSuccess = target;
            }
        }

        if (count == 0) {
            throw ERROR_NO_VESSELS.create();
        } else if (count == 1) {
            Component finalLastSuccess = lastSuccess.getDisplayName();
            source.sendSuccess(() -> Component.translatable("commands.acme_admin.potion.remove_effect.success.single", describeEffect(effect), finalLastSuccess), true);
        } else {
            int finalCount = count;
            source.sendSuccess(() -> Component.translatable("commands.acme_admin.potion.remove_effect.success.multiple", describeEffect(effect), finalCount), true);
        }

        return count;
    }

    private static int run_mergeEffects(
        @NotNull CommandSourceStack source,
        @NotNull Collection<ServerPlayer> targets
    ) throws CommandSyntaxException {
        int count = 0;
        ServerPlayer lastSuccess = null;

        for (ServerPlayer target : targets) {
            StackEffectManipulator targetManipulator = StackEffectManipulator.mutableFromHand(target, InteractionHand.MAIN_HAND);
            if (targetManipulator == null && target.getMainHandItem().isEmpty()) {
                target.setItemInHand(InteractionHand.MAIN_HAND, Items.POTION.getDefaultInstance());
                targetManipulator = StackEffectManipulator.mutableFromHand(target, InteractionHand.MAIN_HAND);
            }
            StackEffectManipulator sourceManipulator = StackEffectManipulator.immutableFromHande(target, InteractionHand.OFF_HAND);

            if (targetManipulator != null && sourceManipulator != null) {
                targetManipulator.addFrom(sourceManipulator);
                targetManipulator.apply();

                count++;
                lastSuccess = target;
            }
        }

        if (count == 0) {
            throw ERROR_NO_VESSELS.create();
        } else if (count == 1) {
            Component finalLastSuccess = lastSuccess.getDisplayName();
            source.sendSuccess(
                () -> Component.translatable("commands.acme_admin.potion.merge_effects.success.single", finalLastSuccess),
                true
            );
        } else {
            int finalCount = count;
            source.sendSuccess(
                () -> Component.translatable("commands.acme_admin.potion.merge_effects.success.multiple", finalCount),
                true
            );
        }

        return count;
    }

    private static int run_transmuteVessel(
        @NotNull CommandSourceStack source,
        @NotNull Collection<ServerPlayer> targets,
        @NotNull ItemInput into
    ) throws CommandSyntaxException {
        int count = 0;
        ServerPlayer lastSuccess = null;
        ItemStack intoStack = into.createItemStack(1, false);

        for (ServerPlayer target : targets) {
            StackEffectManipulator manipulator = StackEffectManipulator.mutableFromHand(target, InteractionHand.MAIN_HAND);
            if (manipulator != null) {
                manipulator.setBackingItem(intoStack.copy());
                manipulator.apply();

                count++;
                lastSuccess = target;
            }
        }

        if (count == 0) {
            throw ERROR_NO_VESSELS.create();
        } else if (count == 1) {
            Component finalLastSuccess = lastSuccess.getDisplayName();
            source.sendSuccess(
                () -> Component.translatable("commands.acme_admin.potion.transmute_vessel.success.single", intoStack.getDisplayName(), finalLastSuccess),
                true
            );
        } else {
            int finalCount = count;
            source.sendSuccess(
                () -> Component.translatable("commands.acme_admin.potion.transmute_vessel.success.multiple", intoStack.getDisplayName(), finalCount),
                true
            );
        }

        return count;
    }

    private static int run_customColor(
        @NotNull CommandSourceStack source,
        @NotNull Collection<ServerPlayer> targets,
        @Nullable Integer color
    ) throws CommandSyntaxException {
        int count = 0;
        ServerPlayer lastSuccess = null;

        for (ServerPlayer target : targets) {
            StackEffectManipulator manipulator = StackEffectManipulator.mutableFromHand(target, InteractionHand.MAIN_HAND);
            if (manipulator != null) {
                if (color == null) {
                    manipulator.clearCustomColor();
                } else {
                    manipulator.setCustomColor(color);
                }
                manipulator.apply();

                count++;
                lastSuccess = target;
            }
        }

        if (count == 0) {
            throw ERROR_NO_VESSELS.create();
        } else {
            if (color == null) {
                if (count == 1) {
                    Component finalLastSuccess = lastSuccess.getDisplayName();
                    source.sendSuccess(
                        () -> Component.translatable("commands.acme_admin.potion.custom_color.reset.success.single", finalLastSuccess),
                        true
                    );
                } else {
                    int finalCount = count;
                    source.sendSuccess(
                        () -> Component.translatable("commands.acme_admin.potion.custom_color.reset.success.multiple", finalCount),
                        true
                    );
                }
            } else {
                String hexColor = String.format("#%06X", color);
                if (count == 1) {
                    Component finalLastSuccess = lastSuccess.getDisplayName();
                    source.sendSuccess(
                        () -> Component.translatable("commands.acme_admin.potion.custom_color.set.success.single", finalLastSuccess, hexColor),
                        true
                    );
                } else {
                    int finalCount = count;
                    source.sendSuccess(
                        () -> Component.translatable("commands.acme_admin.potion.custom_color.set.success.multiple", finalCount, hexColor),
                        true
                    );
                }
            }
        }

        return count;
    }

    private static int run_customName(
        @NotNull CommandSourceStack source,
        @NotNull Collection<ServerPlayer> targets,
        @NotNull Component name
    ) throws CommandSyntaxException {
        int count = 0;
        ServerPlayer lastSuccess = null;

        for (ServerPlayer target : targets) {
            StackEffectManipulator manipulator = StackEffectManipulator.mutableFromHand(target, InteractionHand.MAIN_HAND);
            if (manipulator != null) {
                manipulator.setCustomName(name);
                manipulator.apply();

                count++;
                lastSuccess = target;
            }
        }

        if (count == 0) {
            throw ERROR_NO_VESSELS.create();
        } else {
            if (count == 1) {
                Component finalLastSuccess = lastSuccess.getDisplayName();
                source.sendSuccess(
                    () -> Component.translatable("commands.acme_admin.potion.custom_name.success.single", finalLastSuccess, name),
                    true
                );
            } else {
                int finalCount = count;
                source.sendSuccess(
                    () -> Component.translatable("commands.acme_admin.potion.custom_name.success.multiple", finalCount, name),
                    true
                );
            }
        }

        return count;
    }
}
