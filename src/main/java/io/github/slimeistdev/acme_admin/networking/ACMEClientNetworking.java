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

package io.github.slimeistdev.acme_admin.networking;

import io.github.slimeistdev.acme_admin.mixin_ducks.client.AbstractClientPlayer_Duck;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.effect.MobEffectInstance;

import java.util.Objects;

@Environment(EnvType.CLIENT)
public class ACMEClientNetworking {
    public static void register() {
        ClientPlayNetworking.registerGlobalReceiver(ACMENetworkingConstants.MODERATOR_EFFECT_SYNC_S2C, ACMEClientNetworking::handleModeratorEffectSync);
    }

    private static void handleModeratorEffectSync(Minecraft client, ClientPacketListener handler, FriendlyByteBuf buf, PacketSender responseSender) {
        boolean add = buf.readBoolean();
        int playerId = buf.readVarInt();
        CompoundTag effectTag = buf.readNbt();

        if (client.level == null) return;

        MobEffectInstance effectInstance = MobEffectInstance.load(Objects.requireNonNull(effectTag));
        if (client.level.getEntity(playerId) instanceof AbstractClientPlayer_Duck player) {
            if (add) {
                player.acme_admin$addModeratorSyncedEffect(effectInstance);
            } else {
                player.acme_admin$removeModeratorSyncedEffect(effectInstance);
            }
        }
    }
}
