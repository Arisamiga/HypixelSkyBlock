package net.swofty.types.generic.gui.inventory.inventories.sbmenu.levels;

import net.minestom.server.event.inventory.InventoryPreClickEvent;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.swofty.types.generic.gui.inventory.ItemStackCreator;
import net.swofty.types.generic.gui.inventory.SkyBlockInventoryGUI;
import net.swofty.types.generic.gui.inventory.item.GUIClickableItem;
import net.swofty.types.generic.gui.inventory.item.GUIItem;
import net.swofty.types.generic.levels.SkyBlockLevelRequirement;
import net.swofty.types.generic.levels.SkyBlockLevelUnlock;
import net.swofty.types.generic.user.SkyBlockPlayer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GUISkyBlockLevel extends SkyBlockInventoryGUI {
    private static final Map<Integer, List<Integer>> SLOTS_MAP = new HashMap<>(
            Map.of(
                    1, List.of(13),
                    2, List.of(12, 14),
                    3, List.of(11, 13, 15),
                    4, List.of(10, 12, 14, 16),
                    5, List.of(11, 12, 13, 14, 15)
            )
    );

    private final SkyBlockLevelRequirement levelRequirement;

    public GUISkyBlockLevel(SkyBlockLevelRequirement levelRequirement) {
        super("Level " + levelRequirement.asInt() + " Rewards", InventoryType.CHEST_4_ROW);

        this.levelRequirement = levelRequirement;
    }

    @Override
    public void onOpen(InventoryGUIOpenEvent e) {
        fill(ItemStackCreator.createNamedItemStack(Material.BLACK_STAINED_GLASS_PANE));
        set(GUIClickableItem.getCloseItem(31));
        set(GUIClickableItem.getGoBackItem(30, new GUISkyBlockLevels()));

        List<SkyBlockLevelUnlock> unlocks = levelRequirement.getUnlocks();
        List<Integer> slots = SLOTS_MAP.get(unlocks.size());

        for (int i = 0; i < unlocks.size(); i++) {
            SkyBlockLevelUnlock unlock = unlocks.get(i);
            set(new GUIItem(slots.get(i)) {
                @Override
                public ItemStack.Builder getItem(SkyBlockPlayer player) {
                    return unlock.getItemDisplay(player, levelRequirement.asInt());
                }
            });
        }

        updateItemStacks(e.inventory(), e.player());
    }

    @Override
    public boolean allowHotkeying() {
        return false;
    }

    @Override
    public void onBottomClick(InventoryPreClickEvent e) {

    }
}
