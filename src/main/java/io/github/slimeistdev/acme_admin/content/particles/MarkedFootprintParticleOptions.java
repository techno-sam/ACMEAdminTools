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

package io.github.slimeistdev.acme_admin.content.particles;

import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.slimeistdev.acme_admin.registration.ACMEParticleTypes;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;

public record MarkedFootprintParticleOptions(int ownerId, int survivalSeconds) implements ParticleOptions {
    @SuppressWarnings("deprecation")
    public static final Deserializer<MarkedFootprintParticleOptions> DESERIALIZER = new Deserializer<MarkedFootprintParticleOptions>() {
        private static final SimpleCommandExceptionType CANNOT_CREATE_FROM_COMMAND = new SimpleCommandExceptionType(new LiteralMessage("Cannot create marked footprint from command"));

        @Override
        public @NotNull MarkedFootprintParticleOptions fromCommand(ParticleType<MarkedFootprintParticleOptions> particleType, StringReader reader) throws CommandSyntaxException {
            throw CANNOT_CREATE_FROM_COMMAND.create();
        }

        @Override
        public @NotNull MarkedFootprintParticleOptions fromNetwork(ParticleType<MarkedFootprintParticleOptions> particleType, FriendlyByteBuf buffer) {
            return new MarkedFootprintParticleOptions(buffer.readVarInt(), buffer.readVarInt());
        }
    };

    public static final Codec<MarkedFootprintParticleOptions> CODEC = RecordCodecBuilder.create(i -> i.group(
        Codec.INT.fieldOf("ownerId").forGetter(MarkedFootprintParticleOptions::ownerId),
        Codec.INT.fieldOf("survivalSeconds").forGetter(MarkedFootprintParticleOptions::survivalSeconds)
    ).apply(i, MarkedFootprintParticleOptions::new));

    @Override
    public @NotNull ParticleType<?> getType() {
        return ACMEParticleTypes.MARKED_FOOTPRINT;
    }

    @Override
    public void writeToNetwork(FriendlyByteBuf buffer) {
        buffer.writeVarInt(this.ownerId);
        buffer.writeVarInt(this.survivalSeconds);
    }

    @Override
    public @NotNull String writeToString() {
        return String.format(
            Locale.ROOT,
            "%s %d %d",
            BuiltInRegistries.PARTICLE_TYPE.getKey(this.getType()),
            this.ownerId,
            this.survivalSeconds
        );
    }
}
