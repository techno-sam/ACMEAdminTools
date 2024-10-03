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

package io.github.slimeistdev.acme_admin;

import io.github.slimeistdev.acme_admin.registration.ACMEDamageTypes;
import io.github.slimeistdev.acme_admin.registration.ModSetup;
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageType;

import java.util.concurrent.CompletableFuture;

public class ACMEAdminToolsDataGenerator implements DataGeneratorEntrypoint {
	@Override
	public void onInitializeDataGenerator(FabricDataGenerator generator) {
		ModSetup.init();

        FabricDataGenerator.Pack pack = generator.createPack();

		pack.addProvider(DamageTagGenerator::new);
	}

	private static final class DamageTagGenerator extends FabricTagProvider<DamageType> {
		public DamageTagGenerator(FabricDataOutput output, CompletableFuture<HolderLookup.Provider> registriesFuture) {
			super(output, Registries.DAMAGE_TYPE, registriesFuture);
		}

		@Override
		protected void addTags(HolderLookup.Provider arg) {
			String[] bypasses = new String[] {
				"armor",
				"effects",
				"enchantments",
				"resistance",
				"cooldown"
			};

			for (String bypass : bypasses) {
				getOrCreateTagBuilder(TagKey.create(Registries.DAMAGE_TYPE, new ResourceLocation("bypasses_"+bypass)))
					.add(ACMEDamageTypes.KISS_OF_DEATH);
			}
		}
	}
}
