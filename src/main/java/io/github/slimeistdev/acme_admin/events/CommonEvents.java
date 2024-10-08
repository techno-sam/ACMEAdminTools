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

package io.github.slimeistdev.acme_admin.events;

import io.github.slimeistdev.acme_admin.registration.ACMECommandsServer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.player.*;
import net.minecraft.world.InteractionResult;

import static io.github.slimeistdev.acme_admin.registration.ACMEMobEffects.inhibited;

public class CommonEvents {
    public static void register() {
        PlayerBlockBreakEvents.BEFORE.register((world, player, pos, state, entity) -> !inhibited(player));
        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> inhibited(player) ? InteractionResult.FAIL : InteractionResult.PASS);
        UseEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> inhibited(player) ? InteractionResult.FAIL : InteractionResult.PASS);
        AttackBlockCallback.EVENT.register((player, world, hand, pos, direction) -> inhibited(player) ? InteractionResult.FAIL : InteractionResult.PASS);
        AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> inhibited(player) ? InteractionResult.FAIL : InteractionResult.PASS);

        CommandRegistrationCallback.EVENT.register(ACMECommandsServer::register);
    }
}
