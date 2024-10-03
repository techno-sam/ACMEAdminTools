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
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

public class ACMECreativeTab {
    public static final ResourceKey<CreativeModeTab> ACME_TAB_KEY = ResourceKey.create(Registries.CREATIVE_MODE_TAB, ACMEAdminTools.asResource("acme_tab"));
    public static final CreativeModeTab ACME_TAB = FabricItemGroup.builder()
        .icon(() -> new ItemStack(ACMEItems.BAN_HAMMER))
        .title(Component.translatable("itemGroup.acme_admin"))
        .displayItems(new ACMECreativeTabGenerator())
        .build();

    static { // to prevent duplicate registration
        Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, ACME_TAB_KEY, ACME_TAB);
    }

    public static void register() {}

    private static class ACMECreativeTabGenerator implements CreativeModeTab.DisplayItemsGenerator {

        @Override
        public void accept(CreativeModeTab.ItemDisplayParameters params, CreativeModeTab.Output output) {
            output.accept(new ItemStack(ACMEItems.BAN_HAMMER));
            output.accept(new ItemStack(ACMEItems.BOOT_ON_A_STICK));
        }
    }
}
