package net.swofty.commons.protocol.objects.punishment;

import net.swofty.commons.protocol.ProtocolObject;
import net.swofty.commons.protocol.Serializer;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashSet;
import java.util.Set;

public class GetAllBannedIdsProtocolObject
        extends ProtocolObject<GetAllBannedIdsProtocolObject.GetAllBannedIdsMessage,
        GetAllBannedIdsProtocolObject.GetAllBannedIdsResponse> {

    @Override
    public Serializer<GetAllBannedIdsMessage> getSerializer() {
        return new Serializer<>() {
            @Override
            public String serialize(GetAllBannedIdsMessage value) {
                return new JSONObject().toString();
            }

            @Override
            public GetAllBannedIdsMessage deserialize(String json) {
                return new GetAllBannedIdsMessage();
            }

            @Override
            public GetAllBannedIdsMessage clone(GetAllBannedIdsMessage value) {
                return value;
            }
        };
    }

    @Override
    public Serializer<GetAllBannedIdsResponse> getReturnSerializer() {
        return new Serializer<>() {
            @Override
            public String serialize(GetAllBannedIdsResponse value) {
                JSONObject json = new JSONObject();
                json.put("ids", new JSONArray(value.ids()));
                return json.toString();
            }

            @Override
            public GetAllBannedIdsResponse deserialize(String json) {
                JSONObject obj = new JSONObject(json);
                JSONArray arr = obj.getJSONArray("ids");
                Set<String> ids = new HashSet<>();
                for (int i = 0; i < arr.length(); i++) {
                    ids.add(arr.getString(i));
                }
                return new GetAllBannedIdsResponse(ids);
            }

            @Override
            public GetAllBannedIdsResponse clone(GetAllBannedIdsResponse value) {
                return value;
            }
        };
    }

    public record GetAllBannedIdsMessage() {}

    public record GetAllBannedIdsResponse(Set<String> ids) {}
}
