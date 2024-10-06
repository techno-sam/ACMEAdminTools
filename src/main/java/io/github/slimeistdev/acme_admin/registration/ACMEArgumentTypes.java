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
import io.github.slimeistdev.acme_admin.base.argument_types.TagFilteredItemArgument;
import net.fabricmc.fabric.api.command.v2.ArgumentTypeRegistry;

public class ACMEArgumentTypes {
    public static void register() {
        ArgumentTypeRegistry.registerArgumentType(
            ACMEAdminTools.asResource("tag_filtered_item"),
            TagFilteredItemArgument.class,
            new TagFilteredItemArgument.Info()
        );
    }
}
