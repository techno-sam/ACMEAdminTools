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

import io.github.slimeistdev.acme_admin.ACMEAdminTools;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

public class ACMEDamageTypes {
    public static final ACMEDamageType
        KISS_OF_DEATH = acme("kiss_of_death"),
        DOOM = acme("doom"),
        VORPAL_SWORD = acme("vorpal_sword")
    ;

    public static void register() {}

    private static ACMEDamageType acme(String name) {
        return new ACMEDamageType(ResourceKey.create(Registries.DAMAGE_TYPE, ACMEAdminTools.asResource(name)));
    }
    
    public record ACMEDamageType(ResourceKey<DamageType> key) {
        public DamageSource create(Level world) {
            return new DamageSource(world.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(key));
        }

        public DamageSource create(Level world, Entity cause) {
            return new DamageSource(world.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(key), cause);
        }
    }
}
