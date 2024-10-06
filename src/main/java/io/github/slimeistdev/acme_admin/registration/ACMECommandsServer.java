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

import com.mojang.brigadier.CommandDispatcher;
import io.github.slimeistdev.acme_admin.content.commands.server.ModToolCommand;
import io.github.slimeistdev.acme_admin.content.commands.server.PotionCommand;
import io.github.slimeistdev.acme_admin.content.commands.server.ReloadCommandsCommand;
import io.github.slimeistdev.acme_admin.utils.Utils;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

import static net.minecraft.commands.Commands.literal;

public class ACMECommandsServer {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext context, Commands.CommandSelection selection) {
        var acmeCommand = literal("acme")
            .then(ModToolCommand.register())
            .then(PotionCommand.register(context));

        if (Utils.isDevEnv()) {
            acmeCommand = acmeCommand
                .then(ReloadCommandsCommand.register(dispatcher, context, selection, ACMECommandsServer::register));
        }

        dispatcher.register(acmeCommand);
    }
}
