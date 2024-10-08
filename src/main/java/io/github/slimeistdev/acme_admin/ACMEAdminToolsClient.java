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

import io.github.slimeistdev.acme_admin.networking.ACMEClientNetworking;
import io.github.slimeistdev.acme_admin.registration.ACMEItems;
import io.github.slimeistdev.acme_admin.registration.ACMEParticleTypes;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.minecraft.world.item.alchemy.PotionUtils;

public class ACMEAdminToolsClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ACMEAdminTools.LOGGER.info("ACME Admin Tools is loading on the client!");

        ACMEClientNetworking.register();
        ACMEParticleTypes.registerClient();

        ColorProviderRegistry.ITEM.register((stack, tintIndex) -> tintIndex > 0 ? -1 : PotionUtils.getColor(stack), ACMEItems.ALCHEMICAL_LASER);
    }
}
