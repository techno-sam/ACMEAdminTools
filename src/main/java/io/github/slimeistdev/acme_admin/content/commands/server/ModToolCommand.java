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

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.github.slimeistdev.acme_admin.registration.ACMEItems;
import io.github.slimeistdev.acme_admin.utils.AuthUtils;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;

import java.util.Collection;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

public class ModToolCommand {
    public static ArgumentBuilder<CommandSourceStack, ?> register() {
        return literal("mod_tool")
            .requires(AuthUtils::isAuthorized)
            .then(argument("targets", EntityArgument.players())
                .then(literal("kick")
                    .executes(ModToolCommand::giveKickTool))
                .then(literal("ban")
                    .executes(ModToolCommand::giveBanTool)
                    .then(timedBan()))
            );
    }

    private static ArgumentBuilder<CommandSourceStack, ?> timedBan() {
        return argument("duration0", IntegerArgumentType.integer(1))
            .then(literal("minutes")
                .executes(ctx -> giveTimedBanTool(ctx, IntegerArgumentType.getInteger(ctx, "duration0")))
            )
            .then(literal("hours")
                .executes(ctx -> giveTimedBanTool(ctx, IntegerArgumentType.getInteger(ctx, "duration0") * 60))
                .then(argument("duration1", IntegerArgumentType.integer(0))
                    .then(literal("minutes")
                        .executes(ctx -> giveTimedBanTool(ctx, IntegerArgumentType.getInteger(ctx, "duration0") * 60 + IntegerArgumentType.getInteger(ctx, "duration1")))
                    )
                )
            )
            .then(literal("days")
                .executes(ctx -> giveTimedBanTool(ctx, IntegerArgumentType.getInteger(ctx, "duration0") * 60 * 24))
                .then(argument("duration1", IntegerArgumentType.integer(0))
                    .then(literal("hours")
                        .executes(ctx -> giveTimedBanTool(ctx, IntegerArgumentType.getInteger(ctx, "duration0") * 60 * 24 + IntegerArgumentType.getInteger(ctx, "duration1") * 60))
                        .then(argument("duration2", IntegerArgumentType.integer(0))
                            .then(literal("minutes")
                                .executes(ctx -> giveTimedBanTool(ctx, IntegerArgumentType.getInteger(ctx, "duration0") * 60 * 24 + IntegerArgumentType.getInteger(ctx, "duration1") * 60 + IntegerArgumentType.getInteger(ctx, "duration2")))
                            )
                        )
                    )
                    .then(literal("minutes")
                        .executes(ctx -> giveTimedBanTool(ctx, IntegerArgumentType.getInteger(ctx, "duration0") * 60 * 24 + IntegerArgumentType.getInteger(ctx, "duration1")))
                    )
                )
            );
    }

    private static int giveStack(CommandContext<CommandSourceStack> ctx, ItemStack stack) throws CommandSyntaxException {
        Collection<ServerPlayer> targets = EntityArgument.getPlayers(ctx, "targets");
        for (ServerPlayer serverPlayer : targets) {
            ItemStack copy = stack.copy();

            if (serverPlayer.getInventory().add(copy) && copy.isEmpty()) {
                copy.setCount(1);
                ItemEntity itemEntity = serverPlayer.drop(copy, false);
                if (itemEntity != null) {
                    itemEntity.makeFakeItem();
                }

                serverPlayer.level()
                    .playSound(
                        null,
                        serverPlayer.getX(),
                        serverPlayer.getY(),
                        serverPlayer.getZ(),
                        SoundEvents.ITEM_PICKUP,
                        SoundSource.PLAYERS,
                        0.2F,
                        ((serverPlayer.getRandom().nextFloat() - serverPlayer.getRandom().nextFloat()) * 0.7F + 1.0F) * 2.0F
                    );
                serverPlayer.containerMenu.broadcastChanges();
            } else {
                ItemEntity itemEntity = serverPlayer.drop(copy, false);
                if (itemEntity != null) {
                    itemEntity.setNoPickUpDelay();
                    itemEntity.setTarget(serverPlayer.getUUID());
                }
            }
        }

        if (targets.size() == 1) {
            ctx.getSource().sendSuccess(
                () -> Component.translatable("commands.give.success.single", 1, stack.getDisplayName(), targets.iterator().next().getDisplayName()),
                true
            );
        } else {
            ctx.getSource().sendSuccess(() -> Component.translatable("commands.give.success.single", 1, stack.getDisplayName(), targets.size()), true);
        }

        return targets.size();
    }

    private static int giveKickTool(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        return giveStack(ctx, ACMEItems.BOOT_ON_A_STICK.getDefaultInstance());
    }

    private static int giveBanTool(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        return giveStack(ctx, ACMEItems.BAN_HAMMER.getDefaultInstance());
    }

    private static int giveTimedBanTool(CommandContext<CommandSourceStack> ctx, int minutes) throws CommandSyntaxException {
        ItemStack stack = ACMEItems.BOOT_ON_A_STICK.getDefaultInstance();
        stack.getOrCreateTag().putInt("BanMinutes", minutes);
        return giveStack(ctx, stack);
    }
}
