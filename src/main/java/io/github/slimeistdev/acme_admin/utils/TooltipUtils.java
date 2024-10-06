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

import java.util.ArrayList;
import java.util.List;

public class TooltipUtils {
    private static Component timePartComponent(String unit, int value) {
        String key = "tooltip.acme_admin.duration." + unit;
        return value == 1
            ? Component.translatable(key + ".1")
            : Component.translatable(key, value);
    }

    public static Component constructTimeComponent(final int minutes) {
        int minutes$ = minutes;
        int hours = minutes$ / 60;
        int days = hours / 24;

        minutes$ %= 60;
        hours %= 24;

        List<Component> components = new ArrayList<>();

        if (days > 0) {
            components.add(timePartComponent("days", days));
        }
        if (hours > 0) {
            components.add(timePartComponent("hours", hours));
        }
        if (minutes$ > 0) {
            components.add(timePartComponent("minutes", minutes$));
        }

        return switch (components.size()) {
            case 0 -> timePartComponent("minutes", 0);
            case 1 -> components.get(0);
            case 2 -> Component.translatable("tooltip.acme_admin.duration.join.2", components.get(0), components.get(1));
            case 3 -> Component.translatable("tooltip.acme_admin.duration.join.3", components.get(0), components.get(1), components.get(2));
            default -> timePartComponent("minutes", minutes);
        };
    }
}
