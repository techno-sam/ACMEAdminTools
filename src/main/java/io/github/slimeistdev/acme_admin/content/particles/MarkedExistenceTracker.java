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

package io.github.slimeistdev.acme_admin.content.particles;

import net.minecraft.world.entity.LivingEntity;

import java.lang.ref.WeakReference;

public class MarkedExistenceTracker {
    private final WeakReference<LivingEntity> trackingEntity;
    private boolean active = true;

    public MarkedExistenceTracker(LivingEntity trackingEntity) {
        this.trackingEntity = new WeakReference<>(trackingEntity);
    }

    public boolean isActive() {
        if (!this.active) {
            return false;
        }

        LivingEntity entity = this.trackingEntity.get();
        if (entity == null || entity.isRemoved()) {
            markInactive();
            return false;
        }

        return true;
    }

    public void markInactive() {
        this.active = false;
        this.trackingEntity.enqueue();
    }
}
