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
import io.github.slimeistdev.acme_admin.content.effects.ACMEMobEffect;
import io.github.slimeistdev.acme_admin.content.effects.AntidoteEffect;
import io.github.slimeistdev.acme_admin.content.effects.DoomEffect;
import io.github.slimeistdev.acme_admin.content.effects.MarkedEffect;
import io.github.slimeistdev.acme_admin.utils.Utils;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ACMEMobEffects {

    public static final MobEffect
        INHIBITION = register(
            "inhibition",
            new ACMEMobEffect(MobEffectCategory.HARMFUL, 0x7D319E)
                .addAttributeModifier(Attributes.MOVEMENT_SPEED, "b2674fd2-5ed8-4ae7-80cb-4d5f96729942", -1F, AttributeModifier.Operation.MULTIPLY_TOTAL)
                .addAttributeModifier(Attributes.ATTACK_SPEED, "95540d68-d71e-4018-8564-3d565f0f74fb", -1F, AttributeModifier.Operation.MULTIPLY_TOTAL)
        ),
        DOOM = register(
            "doom",
            new DoomEffect(MobEffectCategory.HARMFUL, 0x9B1900)
        ),
        MARKED = register(
            "marked",
            new MarkedEffect(MobEffectCategory.HARMFUL, 0x00F71C)
        ),
        ANTIDOTE = register(
            "antidote",
                new AntidoteEffect(MobEffectCategory.BENEFICIAL, 0xA9D5F2)
        )
    ;

    public static void register() {}

    private static MobEffect register(String key, MobEffect effect) {
        if (!(effect instanceof ACMEMobEffect)) {
            if (Utils.isDevEnv()) {
                throw new IllegalArgumentException("MobEffect %s must be an instance of ACMEMobEffect".formatted(effect));
            } else {
                ACMEAdminTools.LOGGER.warn("MobEffect {} should be an instance of ACMEMobEffect, please report this", effect);
            }
        }
        return Registry.register(BuiltInRegistries.MOB_EFFECT, ACMEAdminTools.asResource(key), effect);
    }

    public static boolean isModerationEffect(MobEffect effect) {
        return effect == INHIBITION || effect == DOOM || effect == ANTIDOTE || effect == MARKED;
    }

    public static boolean inhibited(Player player) {
        return player.hasEffect(INHIBITION);
    }

    public static boolean safeRemoveAllEffects(LivingEntity entity, Function<LivingEntity, Boolean> original) {
        if (!(entity instanceof Player player)) {
            return original.apply(entity);
        }

        Map<MobEffect, MobEffectInstance> moderationEffects = player.getActiveEffectsMap().entrySet()
            .stream()
            .filter(entry -> isModerationEffect(entry.getKey()))
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        moderationEffects.forEach((effect, instance) -> player.getActiveEffectsMap().remove(effect, instance));

        try {
            return original.apply(entity);
        } finally { // Yep. This is safe. Really. Java is a confusing language.
            player.getActiveEffectsMap().putAll(moderationEffects);
        }
    }
}
