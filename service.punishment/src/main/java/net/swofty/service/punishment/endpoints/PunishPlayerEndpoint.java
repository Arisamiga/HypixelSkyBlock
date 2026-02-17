package net.swofty.service.punishment.endpoints;

import net.swofty.commons.impl.ServiceProxyRequest;
import net.swofty.commons.protocol.ProtocolObject;
import net.swofty.commons.protocol.objects.punishment.PunishPlayerProtocolObject;
import net.swofty.commons.proxy.ToProxyChannels;
import net.swofty.commons.punishment.*;
import net.swofty.service.generic.redis.ServiceEndpoint;
import net.swofty.service.punishment.ProxyRedis;
import org.json.JSONObject;
import org.tinylog.Logger;

import java.time.Instant;
import java.util.Optional;

public class PunishPlayerEndpoint implements ServiceEndpoint
        <PunishPlayerProtocolObject.PunishPlayerMessage,
                PunishPlayerProtocolObject.PunishPlayerResponse> {

    @Override
    public ProtocolObject<PunishPlayerProtocolObject.PunishPlayerMessage, PunishPlayerProtocolObject.PunishPlayerResponse> associatedProtocolObject() {
        return new PunishPlayerProtocolObject();
    }

    @Override
    public PunishPlayerProtocolObject.PunishPlayerResponse onMessage(ServiceProxyRequest message, PunishPlayerProtocolObject.PunishPlayerMessage messageObject) {
        PunishmentType punishmentType;
        try {
            punishmentType = PunishmentType.valueOf(messageObject.type());
        } catch (IllegalArgumentException e) {
            return new PunishPlayerProtocolObject.PunishPlayerResponse(false, null, PunishPlayerProtocolObject.ErrorCode.INVALID_TYPE, "The punishment type provided is invalid.");
        }

        Instant now = Instant.now();
        if (messageObject.expiresAt() > 0 && Instant.ofEpochMilli(messageObject.expiresAt()).isBefore(now)) {
            return new PunishPlayerProtocolObject.PunishPlayerResponse(false, null, PunishPlayerProtocolObject.ErrorCode.INVALID_EXPIRY, "The expiration time provided is invalid.");
        }

        boolean hasOverwriteTag = messageObject.tags() != null && messageObject.tags().contains(PunishmentTag.OVERWRITE);
        if (!hasOverwriteTag) {
            Optional<PunishmentRedis.ActivePunishment> existing = PunishmentRedis.getActive(messageObject.target());
            if (existing.isPresent()) {
                PunishmentRedis.ActivePunishment active = existing.get();
                PunishmentType existingType = PunishmentType.valueOf(active.type());
                if (existingType == punishmentType) {
                    return new PunishPlayerProtocolObject.PunishPlayerResponse(false, null,
                            PunishPlayerProtocolObject.ErrorCode.ALREADY_PUNISHED, active.banId());
                }
            }
        }

        PunishmentReason reason = messageObject.reason();
        PunishmentId id = PunishmentId.generateId();

        PunishmentRedis.saveActivePunishment(
                messageObject.target(),
                messageObject.type(),
                id.id(),
                reason,
                messageObject.expiresAt()
        );
        ProxyRedis.publishToProxy(ToProxyChannels.PUNISH_PLAYER, new JSONObject()
                .put("target", messageObject.target())
                .put("type", messageObject.type())
                .put("id", id.id())
                .put("reason_ban", reason.getBanType() != null ? reason.getBanType().name() : null)
                .put("reason_mute", reason.getMuteType() != null ? reason.getMuteType().name() : null)
                .put("staff", messageObject.staff())
                .put("issuedAt", now.toEpochMilli())
                .put("expiresAt", messageObject.expiresAt())
        );
        Logger.info("Issued {} punishment to {} for reason '{}' (expires at: {})",
                messageObject.type(),
                messageObject.target(),
                reason.getReasonString(),
                messageObject.expiresAt()
        );
        return new PunishPlayerProtocolObject.PunishPlayerResponse(true, id.id(), null, null);
    }
}
