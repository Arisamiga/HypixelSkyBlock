package net.swofty.commons.protocol.objects.punishment;

import com.google.gson.Gson;
import net.swofty.commons.protocol.ProtocolObject;
import net.swofty.commons.protocol.Serializer;
import net.swofty.commons.punishment.PunishmentReason;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

import java.util.UUID;

public class GetActivePunishmentProtocolObject
        extends ProtocolObject<GetActivePunishmentProtocolObject.GetActivePunishmentMessage,
        GetActivePunishmentProtocolObject.GetActivePunishmentResponse> {

    @Override
    public Serializer<GetActivePunishmentMessage> getSerializer() {
        return new Serializer<>() {
            @Override
            public String serialize(GetActivePunishmentMessage value) {
                JSONObject json = new JSONObject();
                json.put("target", value.target().toString());
                return json.toString();
            }

            @Override
            public GetActivePunishmentMessage deserialize(String json) {
                JSONObject obj = new JSONObject(json);
                return new GetActivePunishmentMessage(
                        UUID.fromString(obj.getString("target"))
                );
            }

            @Override
            public GetActivePunishmentMessage clone(GetActivePunishmentMessage value) {
                return value;
            }
        };
    }

    @Override
    public Serializer<GetActivePunishmentResponse> getReturnSerializer() {
        return new Serializer<>() {
            @Override
            public String serialize(GetActivePunishmentResponse value) {
                JSONObject json = new JSONObject();
                json.put("found", value.found());
                json.put("type", value.type());
                json.put("banId", value.banId());
                json.put("reason", value.reason() != null ? new Gson().toJson(value.reason()) : null);
                json.put("expiresAt", value.expiresAt());
                return json.toString();
            }

            @Override
            public GetActivePunishmentResponse deserialize(String json) {
                JSONObject obj = new JSONObject(json);
                boolean found = obj.getBoolean("found");
                if (!found) {
                    return new GetActivePunishmentResponse(false, null, null, null, 0);
                }
                return new GetActivePunishmentResponse(
                        true,
                        obj.optString("type", null),
                        obj.optString("banId", null),
                        obj.isNull("reason") ? null : new Gson().fromJson(obj.getString("reason"), PunishmentReason.class),
                        obj.getLong("expiresAt")
                );
            }

            @Override
            public GetActivePunishmentResponse clone(GetActivePunishmentResponse value) {
                return value;
            }
        };
    }

    public record GetActivePunishmentMessage(
            @NotNull UUID target
    ) {}

    public record GetActivePunishmentResponse(
            boolean found,
            @Nullable String type,
            @Nullable String banId,
            @Nullable PunishmentReason reason,
            long expiresAt
    ) {}
}
