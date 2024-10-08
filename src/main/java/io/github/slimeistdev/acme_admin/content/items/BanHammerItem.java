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

import io.github.slimeistdev.acme_admin.impl.v0.causes.BanHammerBanCause;
import io.github.slimeistdev.acme_admin.utils.BanUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class BanHammerItem extends AbstractBanHammerItem {
    public BanHammerItem(Properties properties) {
        super(properties);
    }

    @Override
    protected void applyModerationAction(ItemStack stack, ServerPlayer target, Player attacker) {
        BanUtils.banPlayer(
            target,
            attacker.getGameProfile().getName()+"["+ attacker.getStringUUID()+"]",
            new BanHammerBanCause(attacker, "Crushed by a Ban Hammer", stack)
        );
    }

    @Override
    public void appendCustomHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag isAdvanced) {
        for (int i = 0; i <=2; i++) {
            tooltipComponents.add(Component.translatable("item.acme_admin.ban_hammer.tooltip."+i));
        }
    }
}
