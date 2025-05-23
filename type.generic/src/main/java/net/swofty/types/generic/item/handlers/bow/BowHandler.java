package net.swofty.types.generic.item.handlers.bow;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import net.swofty.types.generic.item.SkyBlockItem;
import net.swofty.types.generic.user.SkyBlockPlayer;

import java.util.function.BiConsumer;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class BowHandler {
    private final BiConsumer<SkyBlockPlayer, SkyBlockItem> shootHandler;
}
