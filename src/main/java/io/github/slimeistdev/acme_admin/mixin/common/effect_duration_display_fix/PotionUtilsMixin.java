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

package io.github.slimeistdev.acme_admin.mixin.common.effect_duration_display_fix;

import com.google.common.collect.Lists;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.github.slimeistdev.acme_admin.content.effects.utils.CustomMarkedMobEffectInstance;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.PotionUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.List;

@Mixin(PotionUtils.class)
public class PotionUtilsMixin {
    @WrapOperation(method = "addPotionTooltip(Lnet/minecraft/world/item/ItemStack;Ljava/util/List;F)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/alchemy/PotionUtils;getMobEffects(Lnet/minecraft/world/item/ItemStack;)Ljava/util/List;"))
    private static List<MobEffectInstance> markAsCustom(ItemStack stack, Operation<List<MobEffectInstance>> original) {
        CompoundTag compoundTag = stack.getTag();

        List<MobEffectInstance> list = Lists.<MobEffectInstance>newArrayList();
        list.addAll(PotionUtils.getPotion(compoundTag).getEffects());

        List<MobEffectInstance> custom = PotionUtils.getCustomEffects(compoundTag);

        custom.stream()
            .map(CustomMarkedMobEffectInstance::new)
            .forEach(list::add);

        return list;
    }

    @WrapOperation(method = "addPotionTooltip(Ljava/util/List;Ljava/util/List;F)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/effect/MobEffectUtil;formatDuration(Lnet/minecraft/world/effect/MobEffectInstance;F)Lnet/minecraft/network/chat/Component;"))
    private static Component fixCustomDuration(MobEffectInstance effect, float durationFactor, Operation<Component> original) {
        return original.call(effect, effect instanceof CustomMarkedMobEffectInstance ? 1.0f : durationFactor);
    }
}
