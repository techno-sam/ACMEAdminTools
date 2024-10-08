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
import io.github.slimeistdev.acme_admin.content.items.AlchemicalLaserItem;
import io.github.slimeistdev.acme_admin.content.items.BanHammerItem;
import io.github.slimeistdev.acme_admin.content.items.BootOnAStickItem;
import io.github.slimeistdev.acme_admin.content.items.VorpalSwordItem;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;

@SuppressWarnings("unused")
public class ACMEItems {
    public static final BanHammerItem BAN_HAMMER = register("ban_hammer", new BanHammerItem(new Item.Properties().stacksTo(1).rarity(Rarity.EPIC)));
    public static final BootOnAStickItem BOOT_ON_A_STICK = register("boot_on_a_stick", new BootOnAStickItem(new Item.Properties().stacksTo(1).rarity(Rarity.EPIC)));
    public static final VorpalSwordItem VORPAL_SWORD = register("vorpal_sword", new VorpalSwordItem(new Item.Properties().rarity(Rarity.EPIC)));
    public static final AlchemicalLaserItem ALCHEMICAL_LASER = register("alchemical_laser", new AlchemicalLaserItem(new Item.Properties().stacksTo(1).rarity(Rarity.EPIC)));

    public static void register() {}

    private static <T extends Item> T register(String name, T item) {
        return Registry.register(BuiltInRegistries.ITEM, ACMEAdminTools.asResource(name), item);
    }
}
