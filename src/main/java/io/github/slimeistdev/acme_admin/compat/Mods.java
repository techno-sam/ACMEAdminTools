/*
 * Steam 'n' Rails
 * Copyright (c) 2022-2024 The Railways Team
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

package io.github.slimeistdev.acme_admin.compat;

import net.fabricmc.loader.api.FabricLoader;

import java.util.Locale;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * For compatibility with and without another mod present, we have to define load conditions of the specific code
 */
public enum Mods {
	BANHAMMER
	;

	public final boolean isLoaded;
	public final String id;

	Mods() {
		this.id = name().toLowerCase(Locale.ROOT);
		this.isLoaded = isModLoaded(id);
	}

	Mods(String id) {
		this.id = id;
		this.isLoaded = isModLoaded(id);
	}

	/**
	 * Simple hook to run code if a mod is installed
	 * @param toRun will be run only if the mod is loaded
	 * @return Optional.empty() if the mod is not loaded, otherwise an Optional of the return value of the given supplier
	 */
	public <T> Optional<T> runIfInstalled(Supplier<Supplier<T>> toRun) {
		if (isLoaded)
			return Optional.of(toRun.get().get());
		return Optional.empty();
	}

	/**
	 * Simple hook to execute code if a mod is installed
	 * @param toExecute will be executed only if the mod is loaded
	 */
	public void executeIfInstalled(Supplier<Runnable> toExecute) {
		if (isLoaded) {
			toExecute.get().run();
		}
	}

	/**
	 * Simple hook to execute code if a mod is installed
	 * @param ifInstalled will be executed if the mod is loaded
	 * @param orElse will be executed if the mod is not loaded
	 */
	public void executeIfInstalled(Supplier<Runnable> ifInstalled, Supplier<Runnable> orElse) {
		if (isLoaded) {
			ifInstalled.get().run();
		} else {
			orElse.get().run();
		}
	}

	public static boolean isModLoaded(String id) {
		return FabricLoader.getInstance().isModLoaded(id);
	}
}
