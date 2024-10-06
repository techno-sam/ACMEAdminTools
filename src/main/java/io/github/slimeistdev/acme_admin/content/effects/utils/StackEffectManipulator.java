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

package io.github.slimeistdev.acme_admin.content.effects.utils;

import com.mojang.datafixers.util.Pair;
import io.github.slimeistdev.acme_admin.ACMEAdminTools;
import io.github.slimeistdev.acme_admin.registration.ACMEItemTags;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class StackEffectManipulator {
    private ItemStack stack;
    private final boolean mutable;
    private final @Nullable Consumer<ItemStack> applicator;
    private @Nullable Potion potion;
    private final Set<MobEffectInstance> effects = new LinkedHashSet<>();
    private @Nullable Integer customColor = null;

    public static StackEffectManipulator mutable(ItemStack stack, @NotNull Consumer<ItemStack> applicator) {
        return new StackEffectManipulator(stack, true, applicator);
    }

    public static StackEffectManipulator immutable(ItemStack stack) {
        return new StackEffectManipulator(stack, false, null);
    }

    public static @Nullable StackEffectManipulator mutableFromHand(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (stack.isEmpty()) return null;
        if (!stack.is(ACMEItemTags.POTION_VESSELS)) return null;

        return mutable(stack, s -> player.setItemInHand(hand, s));
    }

    public static @Nullable StackEffectManipulator immutableFromHande(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (stack.isEmpty()) return null;
        if (!stack.is(ACMEItemTags.POTION_VESSELS)) return null;

        return immutable(stack);
    }

    private StackEffectManipulator(ItemStack stack, boolean mutable, @Nullable Consumer<ItemStack> applicator) {
        this.stack = stack;
        this.mutable = mutable;
        this.applicator = applicator;
        this.potion = PotionUtils.getPotion(stack);
        this.effects.addAll(PotionUtils.getCustomEffects(stack));

        //noinspection DataFlowIssue
        if (stack.hasTag() && stack.getTag().contains("CustomPotionColor", Tag.TAG_INT)) {
            this.customColor = stack.getTag().getInt("CustomPotionColor");
        }
    }

    private boolean checkMutable() {
        if (!this.mutable) {
            ACMEAdminTools.LOGGER.warn("Attempted to modify an immutable StackEffectManipulator!");
            return false;
        }
        return true;
    }

    private LinkedHashSet<MobEffectInstance> getAllEffects() {
        LinkedHashSet<MobEffectInstance> allEffects = new LinkedHashSet<>();
        if (this.potion != null) {
            allEffects.addAll(this.potion.getEffects());
        }
        allEffects.addAll(this.effects);
        return allEffects;
    }

    private void collapsePotion() {
        if (this.potion != null) {
            Set<MobEffectInstance> potionEffects = new LinkedHashSet<>(this.potion.getEffects());
            potionEffects.addAll(this.effects);
            this.effects.clear();
            this.effects.addAll(potionEffects);
        }
    }

    public void addFrom(StackEffectManipulator other) {
        if (checkMutable()) {
            this.effects.addAll(other.getAllEffects());
            if (other.customColor != null) {
                this.customColor = other.customColor;
            }
            this.mergeEffects();
        }
    }

    public void addEffect(MobEffectInstance effect) {
        if (checkMutable()) {
            this.effects.add(effect);
            this.mergeEffects();
        }
    }

    private void mergeEffects() {
        // check if the potion needs to be collapsed
        if (this.potion != null) {
            Map<Pair<MobEffect, Integer>, MobEffectInstance> potionEffects = this.potion.getEffects().stream()
                .map(effect -> Pair.of(Pair.of(effect.getEffect(), effect.getAmplifier()), effect))
                .collect(Collectors.toMap(Pair::getFirst, Pair::getSecond));

            for (Iterator<MobEffectInstance> iterator = this.effects.iterator(); iterator.hasNext();) {
                MobEffectInstance effect = iterator.next();
                Pair<MobEffect, Integer> key = Pair.of(effect.getEffect(), effect.getAmplifier());
                if (potionEffects.containsKey(key)) {
                    if (potionEffects.get(key).getDuration() < effect.getDuration()) {
                        collapsePotion();
                        this.potion = null;
                        break;
                    } else {
                        iterator.remove();
                    }
                }
            }
        }

        Map<MobEffect, Integer> ordering = new HashMap<>();
        Map<Pair<MobEffect, Integer>, MobEffectInstance> effectMap = new HashMap<>();
        int i = 0;
        for (MobEffectInstance effect : this.effects) {
            ordering.put(effect.getEffect(), i++);

            Pair<MobEffect, Integer> key = Pair.of(effect.getEffect(), effect.getAmplifier());
            effectMap.compute(key, (k, existing) -> existing == null
                ? effect :
                new MobEffectInstance(
                    effect.getEffect(),
                    Math.max(existing.getDuration(), effect.getDuration()),
                    effect.getAmplifier(),
                    effect.isAmbient() && existing.isAmbient(),
                    effect.isVisible() || existing.isVisible(),
                    effect.showIcon() || existing.showIcon()
                )
            );
        }

        this.effects.clear();
        List<MobEffectInstance> sortedEffects = new ArrayList<>(effectMap.values());
        sortedEffects.sort(Comparator.comparingInt(effect -> ordering.get(effect.getEffect())));
        this.effects.addAll(effectMap.values()
            .stream()
            .sorted(Comparator.comparingInt(effect -> ordering.get(effect.getEffect())))
            .toList());
    }

    public boolean removeEffect(MobEffectInstance effect) {
        if (checkMutable()) {
            if (this.potion != null && this.potion.getEffects().contains(effect)) {
                collapsePotion();
                this.potion = null;
            }
            return this.effects.remove(effect);
        } else {
            return false;
        }
    }

    public int removeEffect(MobEffect effect) {
        if (checkMutable()) {
            if (this.potion != null && this.potion.getEffects().stream().anyMatch(instance -> instance.getEffect() == effect)) {
                collapsePotion();
                this.potion = null;
            }

            int originalSize = this.effects.size();
            this.effects.removeIf(instance -> instance.getEffect() == effect);
            return originalSize - this.effects.size();
        } else {
            return 0;
        }
    }

    public void setCustomColor(int color) {
        if (checkMutable()) {
            this.customColor = color;
        }
    }

    public void clearCustomColor() {
        if (checkMutable()) {
            this.customColor = null;
        }
    }

    private static @Nullable CompoundTag mergeTags(@Nullable CompoundTag priority, @Nullable CompoundTag fallback) {
        if (priority == null) {
            return fallback == null ? null : fallback.copy();
        } else if (fallback == null) {
            return priority.copy();
        } else {
            CompoundTag merged = fallback.copy();
            merged.merge(priority);
            return merged;
        }
    }

    public void setBackingItem(ItemStack newStack) {
        if (checkMutable()) {
            CompoundTag newTag = newStack.getTag();
            CompoundTag existingTag = this.stack.getTag();
            int count = this.stack.getCount();

            this.stack = newStack;
            this.stack.setCount(Math.min(count, this.stack.getMaxStackSize()));

            this.stack.setTag(mergeTags(newTag, existingTag));
            this.stack.removeTagKey("Potion");
            this.stack.removeTagKey("CustomPotionEffects");
            this.stack.removeTagKey("CustomPotionColor");
        }
    }

    public void setCustomName(Component name) {
        if (checkMutable()) {
            this.stack.setHoverName(name);
        }
    }

    public void apply() {
        if (checkMutable()) {
            this.stack.removeTagKey("Potion");
            this.stack.removeTagKey("CustomPotionEffects");
            this.stack.removeTagKey("CustomPotionColor");

            if (this.potion == Potions.WATER && !this.effects.isEmpty()) {
                this.potion = null;
            }

            if (this.potion != null) {
                PotionUtils.setPotion(this.stack, this.potion);
            }
            PotionUtils.setCustomEffects(this.stack, this.effects);
            if (this.customColor != null) {
                this.stack.getOrCreateTag().putInt("CustomPotionColor", this.customColor);
            }

            if (this.applicator != null) {
                this.applicator.accept(this.stack);
            }
        }
    }
}
