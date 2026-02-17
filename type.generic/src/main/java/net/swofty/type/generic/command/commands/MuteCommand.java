package net.swofty.type.generic.command.commands;

import net.kyori.adventure.text.Component;
import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.arguments.Argument;
import net.minestom.server.command.builder.arguments.ArgumentString;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.suggestion.SuggestionEntry;
import net.swofty.commons.ServiceType;
import net.swofty.commons.StringUtility;
import net.swofty.commons.protocol.objects.punishment.PunishPlayerProtocolObject;
import net.swofty.commons.punishment.PunishmentReason;
import net.swofty.commons.punishment.PunishmentType;
import net.swofty.commons.punishment.template.MuteType;
import net.swofty.proxyapi.ProxyService;
import net.swofty.type.generic.command.CommandParameters;
import net.swofty.type.generic.command.HypixelCommand;
import net.swofty.type.generic.user.categories.Rank;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@CommandParameters(
        aliases = "mute tempmute",
        permission = Rank.STAFF,
        description = "Mute a player from the server.",
        usage = "/mute <player> [duration] <reason>",
        allowsConsole = true
)
public class MuteCommand extends HypixelCommand {

    @Override
    public void registerUsage(MinestomCommand command) {
        ArgumentString playerArg = ArgumentType.String("player");
        ArgumentString durationArg = ArgumentType.String("duration");
        Argument<String> reasonArg = ArgumentType.String("reason").setSuggestionCallback((sender, context, suggestion) -> {
            for (MuteType type : MuteType.values()) {
                suggestion.addEntry(new SuggestionEntry(type.name(), Component.text("§c" + type.getReason())));
            }
        });

        command.addSyntax((sender, context) -> {
            String playerName = context.get(playerArg);
            String duration = context.get(durationArg);
            MuteType type = MuteType.valueOf(context.get(reasonArg));

            CompletableFuture.runAsync(() -> {
                try {
                    UUID targetUuid = resolvePlayerUuid(sender, playerName, "mute");
                    long actualTime = StringUtility.parseDuration(duration);
                    long expiryTime = System.currentTimeMillis() + actualTime;
                    mutePlayer(sender, targetUuid, type, senderUuid(sender), actualTime, expiryTime, playerName);
                } catch (IOException e) {
                    sender.sendMessage("§cCould not find player: " + playerName);
                }
            });
        }, playerArg, durationArg, reasonArg);

        command.addSyntax((sender, context) -> {
            String playerName = context.get(playerArg);
            MuteType reason = MuteType.valueOf(context.get(reasonArg));

            CompletableFuture.runAsync(() -> {
                try {
                    mutePlayer(sender, resolvePlayerUuid(sender, playerName, "mute"), reason,
                            senderUuid(sender), 0, -1, playerName);
                } catch (IOException e) {
                    sender.sendMessage("§cCould not find player: " + playerName);
                }
            });
        }, playerArg, reasonArg);
    }

    private void mutePlayer(CommandSender sender, UUID targetUuid, MuteType type, UUID senderUuid,
                            long actualTime, long expiryTime, String playerName) {
        ProxyService punishmentService = new ProxyService(ServiceType.PUNISHMENT);
        PunishmentReason reason = new PunishmentReason(type);
        PunishPlayerProtocolObject.PunishPlayerMessage message = new PunishPlayerProtocolObject.PunishPlayerMessage(
                targetUuid,
                PunishmentType.MUTE.name(),
                reason,
                senderUuid,
                List.of(),
                actualTime > 0 ? expiryTime : -1
        );

        punishmentService.handleRequest(message).thenAccept(result -> {
            if (result instanceof PunishPlayerProtocolObject.PunishPlayerResponse response) {
                if (response.success()) {
                    sender.sendMessage("§aSuccessfully muted player §e" + playerName + "§a. §8Punishment ID: §7" + response.punishmentId());
                } else if (response.errorCode() == PunishPlayerProtocolObject.ErrorCode.ALREADY_PUNISHED) {
                    sender.sendMessage("§cThis player already has an active punishment. Punishment ID: §7" + response.errorMessage());
                } else {
                    sender.sendMessage("§cFailed to mute player: " + response.errorMessage());
                }
            }
        }).orTimeout(5, TimeUnit.SECONDS).exceptionally(_ -> {
            sender.sendMessage("§cCould not mute this player at this time. The punishment service may be offline.");
            return null;
        });
    }
}
