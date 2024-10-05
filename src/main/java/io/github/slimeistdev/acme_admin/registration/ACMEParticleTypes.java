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

package io.github.slimeistdev.acme_admin.registration;

import com.mojang.serialization.Codec;
import io.github.slimeistdev.acme_admin.ACMEAdminTools;
import io.github.slimeistdev.acme_admin.content.particles.MarkedFootprintParticle;
import io.github.slimeistdev.acme_admin.content.particles.MarkedFootprintParticleOptions;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

public class ACMEParticleTypes {

    public static final ParticleType<MarkedFootprintParticleOptions> MARKED_FOOTPRINT = register("marked_footprint", true, MarkedFootprintParticleOptions.DESERIALIZER, $ -> MarkedFootprintParticleOptions.CODEC);

    public static void register() {}

    @SuppressWarnings({"deprecation", "SameParameterValue"})
    private static <T extends ParticleOptions> ParticleType<T> register(
        String id,
        boolean overrideLimiter,
        ParticleOptions.Deserializer<T> deserializer,
        Function<ParticleType<T>, Codec<T>> codecFactory
    ) {
        return register(id, new ParticleType<T>(overrideLimiter, deserializer) {
            @Override
            public @NotNull Codec<T> codec() {
                return codecFactory.apply(this);
            }
        });
    }

    private static <T extends ParticleType<?>> T register(String id, T particleType) {
        return Registry.register(BuiltInRegistries.PARTICLE_TYPE, ACMEAdminTools.asResource(id), particleType);
    }

    @Environment(EnvType.CLIENT)
    public static void registerClient() {
        ParticleFactoryRegistry.getInstance().register(MARKED_FOOTPRINT, MarkedFootprintParticle.Provider::new);
    }
}
