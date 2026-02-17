package net.swofty.commons.punishment;

import net.kyori.adventure.text.Component;
import net.swofty.commons.StringUtility;

public final class PunishmentMessages {
    private PunishmentMessages() {}

    public static Component banMessage(PunishmentRedis.ActivePunishment punishment) {
        long expiresAt = punishment.expiresAt();
        PunishmentReason reason = punishment.reason();
        String banId = punishment.banId();

        long timeLeft = expiresAt - System.currentTimeMillis();
        String prettyTimeLeft = StringUtility.formatTimeLeft(timeLeft);

        String header;
        if (expiresAt <= 0) {
            header = "§cYou are permanently banned from this server!\n";
        } else {
            header = "§cYou are temporarily banned for §f" + prettyTimeLeft + " §cfrom this server!\n";
        }

        String findOutMore = "";
        if (reason.getBanType() != null && reason.getBanType().getUrl() != null) {
            findOutMore = "§7Find out more: §b" + reason.getBanType().getUrl() + "\n";
        }

        String footer = "§7Sharing your Ban ID may affect the processing of your appeal!";

        return Component.text(header + "\n§7Reason: §f" + reason.getReasonString() + "\n" + findOutMore + "\n§7Ban ID: §f" + banId + "\n" + footer);
    }

    public static Component muteMessage(PunishmentRedis.ActivePunishment punishment) {
        long expiresAt = punishment.expiresAt();
        PunishmentReason reason = punishment.reason();

        long timeLeft = expiresAt - System.currentTimeMillis();
        String prettyTimeLeft = StringUtility.formatTimeLeft(timeLeft);

        String line = "\n§c§m                                                     §r\n";

        String header;
        if (expiresAt <= 0) {
            header = "§cYou are permanently muted on this server!\n";
        } else {
            header = "§cYou are currently muted for " + reason.getReasonString() + "\n";
        }
        String time = "§7Your mute will expire in §c" + prettyTimeLeft + "\n\n";

        String urlInfo = "§7Find out more here: §fwww.hypixel.net/mutes\n";
        String footer = "§7Mute ID: §f" + punishment.banId();
        return Component.text(line + header + time + urlInfo + footer + line);
    }
}
