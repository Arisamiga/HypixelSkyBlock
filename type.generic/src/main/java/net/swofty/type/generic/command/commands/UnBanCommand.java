package net.swofty.type.generic.command.commands;

import net.minestom.server.command.builder.arguments.Argument;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.suggestion.SuggestionEntry;
import net.minestom.server.utils.mojang.MojangUtils;
import net.swofty.commons.ServiceType;
import net.swofty.commons.protocol.objects.punishment.UnpunishPlayerProtocolObject;
import net.swofty.commons.punishment.PunishmentRedis;
import net.swofty.proxyapi.ProxyService;
import net.swofty.type.generic.command.CommandParameters;
import net.swofty.type.generic.command.HypixelCommand;
import net.swofty.type.generic.user.categories.Rank;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@CommandParameters(
        description = "Unban a player from the server.",
        usage = "/unban <player>",
        aliases = "unban pardon unbanip pardonip",
        permission = Rank.STAFF,
        allowsConsole = true
)
public class UnBanCommand extends HypixelCommand {

    @Override
    public void registerUsage(MinestomCommand command) {
        Argument<String> argument = ArgumentType.String("player").setSuggestionCallback((sender, context, suggestion) -> {
            if (!PunishmentRedis.isInitialized()) return;
            Set<String> ids = PunishmentRedis.getAllBannedPlayerIds();
            for (String playerId : ids) {
                suggestion.addEntry(new SuggestionEntry(playerId));
            }
        });

        command.addSyntax((sender, context) -> {
            String playerName = context.get(argument);

            CompletableFuture.runAsync(() -> {
                try {
                    var targetUuid = resolvePlayerUuid(sender, playerName, "unban");
                    ProxyService punishmentService = new ProxyService(ServiceType.PUNISHMENT);
                    var message = new UnpunishPlayerProtocolObject.UnpunishPlayerMessage(
                            targetUuid, senderUuid(sender)
                    );

                    punishmentService.handleRequest(message).thenAccept(result -> {
                        if (result instanceof UnpunishPlayerProtocolObject.UnpunishPlayerResponse response) {
                            if (response.success()) {
                                sender.sendMessage("§aSuccessfully unbanned player: " + playerName);
                            } else {
                                sender.sendMessage("§c" + response.errorMessage());
                            }
                        }
                    }).orTimeout(5, TimeUnit.SECONDS).exceptionally(_ -> {
                        sender.sendMessage("§cCould not unban this player at this time. The punishment service may be offline.");
                        return null;
                    });
                } catch (IOException e) {
                    sender.sendMessage("§cCould not find player: " + playerName);
                }
            });
        }, argument);
    }
}
