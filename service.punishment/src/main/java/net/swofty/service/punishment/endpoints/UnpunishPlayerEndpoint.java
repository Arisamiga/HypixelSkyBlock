package net.swofty.service.punishment.endpoints;

import net.swofty.commons.impl.ServiceProxyRequest;
import net.swofty.commons.protocol.ProtocolObject;
import net.swofty.commons.protocol.objects.punishment.UnpunishPlayerProtocolObject;
import net.swofty.commons.punishment.PunishmentRedis;
import net.swofty.service.generic.redis.ServiceEndpoint;
import org.tinylog.Logger;

import java.util.Optional;

public class UnpunishPlayerEndpoint implements ServiceEndpoint
        <UnpunishPlayerProtocolObject.UnpunishPlayerMessage,
                UnpunishPlayerProtocolObject.UnpunishPlayerResponse> {

    @Override
    public ProtocolObject<UnpunishPlayerProtocolObject.UnpunishPlayerMessage, UnpunishPlayerProtocolObject.UnpunishPlayerResponse> associatedProtocolObject() {
        return new UnpunishPlayerProtocolObject();
    }

    @Override
    public UnpunishPlayerProtocolObject.UnpunishPlayerResponse onMessage(ServiceProxyRequest message, UnpunishPlayerProtocolObject.UnpunishPlayerMessage messageObject) {
        Optional<PunishmentRedis.ActivePunishment> existing = PunishmentRedis.getActive(messageObject.target());
        if (existing.isEmpty()) {
            return new UnpunishPlayerProtocolObject.UnpunishPlayerResponse(false, "No active punishment found for this player.");
        }

        PunishmentRedis.revoke(messageObject.target()).join();
        Logger.info("Revoked punishment for {} by staff {}",
                messageObject.target(), messageObject.staff());
        return new UnpunishPlayerProtocolObject.UnpunishPlayerResponse(true, null);
    }
}
