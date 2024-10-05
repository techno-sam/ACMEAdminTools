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

package io.github.slimeistdev.acme_admin.mixin.common.moderator_effect_sync;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.github.slimeistdev.acme_admin.content.effects.ModeratorOnlyEffect;
import io.github.slimeistdev.acme_admin.utils.AuthUtils;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundUpdateMobEffectPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.server.players.PlayerList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(PlayerList.class)
public class PlayerListMixin {
    @WrapOperation(method = "placeNewPlayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerGamePacketListenerImpl;send(Lnet/minecraft/network/protocol/Packet;)V"))
    private void restrictAndSendModeratorEffects(ServerGamePacketListenerImpl instance, Packet<?> packet, Operation<Void> original) {
        if (!(packet instanceof ClientboundUpdateMobEffectPacket mobEffectPacket) || !(mobEffectPacket.getEffect() instanceof ModeratorOnlyEffect)) {
            original.call(instance, packet);
            return;
        }

        // Send to all moderators
        for (ServerPlayer to : instance.getPlayer().getServer().getPlayerList().getPlayers()) {
            if (!AuthUtils.isAuthorized(to)) continue;

            to.connection.send(packet);
        }
    }
}
