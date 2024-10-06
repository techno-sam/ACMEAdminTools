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

package io.github.slimeistdev.acme_admin.content.items;

import io.github.slimeistdev.acme_admin.utils.BanUtils;
import io.github.slimeistdev.acme_admin.utils.TooltipUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.Date;
import java.util.List;

public class BootOnAStickItem extends AbstractBanHammerItem {
    public BootOnAStickItem(Properties properties) {
        super(properties);
    }

    @Override
    protected void applyModerationAction(ItemStack stack, ServerPlayer target, Player attacker) {
        Integer banMinutes = getBanMinutes(stack);

        if (banMinutes == null) {
            BanUtils.kickPlayer(target, "Kicked by Boot on a Stick");
        } else {
            Date expireDate = new Date(System.currentTimeMillis() + banMinutes * 60000);
            BanUtils.banPlayer(target, attacker.getGameProfile().getName()+"["+ attacker.getStringUUID()+"]", "Found the sole of a Boot on a Stick", expireDate);
        }
    }

    @Override
    protected void appendCustomHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag isAdvanced) {
        Integer banMinutes = getBanMinutes(stack);

        if (banMinutes == null) for (int i = 0; i <= 2; i++) {
            tooltipComponents.add(Component.translatable("item.acme_admin.boot_on_a_stick.tooltip.kick."+i));
        } else for (int i = 0; i <= 2; i++) {
            tooltipComponents.add(Component.translatable("item.acme_admin.boot_on_a_stick.tooltip.ban."+i, TooltipUtils.constructTimeComponent(banMinutes)));
        }
    }

    private static @Nullable Integer getBanMinutes(ItemStack stack) {
        Integer banMinutes = null;
        if (stack.hasTag()) {
            CompoundTag tag = stack.getOrCreateTag();
            if (tag.contains("BanMinutes", Tag.TAG_INT))
                banMinutes = tag.getInt("BanMinutes");
        }
        return banMinutes;
    }
}
