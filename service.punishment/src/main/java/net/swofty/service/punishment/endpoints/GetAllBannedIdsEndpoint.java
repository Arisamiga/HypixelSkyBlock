package net.swofty.service.punishment.endpoints;

import net.swofty.commons.impl.ServiceProxyRequest;
import net.swofty.commons.protocol.ProtocolObject;
import net.swofty.commons.protocol.objects.punishment.GetAllBannedIdsProtocolObject;
import net.swofty.commons.punishment.PunishmentRedis;
import net.swofty.service.generic.redis.ServiceEndpoint;

import java.util.Set;

public class GetAllBannedIdsEndpoint implements ServiceEndpoint
        <GetAllBannedIdsProtocolObject.GetAllBannedIdsMessage,
                GetAllBannedIdsProtocolObject.GetAllBannedIdsResponse> {

    @Override
    public ProtocolObject<GetAllBannedIdsProtocolObject.GetAllBannedIdsMessage, GetAllBannedIdsProtocolObject.GetAllBannedIdsResponse> associatedProtocolObject() {
        return new GetAllBannedIdsProtocolObject();
    }

    @Override
    public GetAllBannedIdsProtocolObject.GetAllBannedIdsResponse onMessage(ServiceProxyRequest message, GetAllBannedIdsProtocolObject.GetAllBannedIdsMessage messageObject) {
        Set<String> ids = PunishmentRedis.getAllBannedPlayerIds();
        return new GetAllBannedIdsProtocolObject.GetAllBannedIdsResponse(ids);
    }
}
