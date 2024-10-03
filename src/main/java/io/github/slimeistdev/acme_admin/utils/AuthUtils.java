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

package io.github.slimeistdev.acme_admin.utils;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.function.Supplier;

public class AuthUtils {
    public static boolean isAuthorized(Player player) {
        return player.hasPermissions(3);
    }

    public static void appendAuthTooltip(Level level, List<Component> tooltipComponents) {
        if (level.isClientSide) {
            Player[] player = {null};
            Supplier<Runnable> findPlayer = () -> () -> player[0] = ClientUtils.getLocalPlayer();
            findPlayer.get().run();

            if (player[0] != null) {
                if (!isAuthorized(player[0])) {
                    tooltipComponents.add(Component.translatable("tooltip.acme_admin.not_authorised"));
                }
            }
        }
    }
}
