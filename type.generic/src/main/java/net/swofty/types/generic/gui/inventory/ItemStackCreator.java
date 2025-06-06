package net.swofty.types.generic.gui.inventory;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.minestom.server.entity.PlayerSkin;
import net.minestom.server.item.ItemComponent;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.item.component.AttributeList;
import net.minestom.server.item.component.HeadProfile;
import net.minestom.server.tag.Tag;
import net.minestom.server.utils.Unit;
import net.swofty.commons.StringUtility;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Utility class for creating {@link ItemStack} builders with various customizations,
 * including item materials, names, lore, and player skins.
 */
public class ItemStackCreator {

    /**
     * Creates an {@link ItemStack.Builder} with a specified material and custom name.
     *
     * @param material the material of the item stack
     * @param name     the custom name of the item stack
     * @return an {@link ItemStack.Builder} with the specified properties
     */
    public static ItemStack.Builder createNamedItemStack(Material material, String name) {
        return clearAttributes(ItemStack.builder(material)
                .set(ItemComponent.CUSTOM_NAME, Component.text(name).decoration(TextDecoration.ITALIC, false))
                .set(ItemComponent.HIDE_ADDITIONAL_TOOLTIP, Unit.INSTANCE));
    }

    /**
     * Clears attribute modifiers from the given {@link ItemStack.Builder}.
     *
     * @param builder the {@link ItemStack.Builder} to clear attributes from
     * @return the modified {@link ItemStack.Builder}
     */
    public static ItemStack.Builder clearAttributes(ItemStack.Builder builder) {
        builder.set(ItemComponent.ATTRIBUTE_MODIFIERS, new AttributeList(List.of(), false));
        return builder;
    }

    /**
     * Creates an {@link ItemStack.Builder} with a specified material and an empty custom name.
     *
     * @param material the material of the item stack
     * @return an {@link ItemStack.Builder} with the specified material and an empty name
     */
    public static ItemStack.Builder createNamedItemStack(Material material) {
        return createNamedItemStack(material, "");
    }

    /**
     * Creates an {@link ItemStack.Builder} with a single lore line.
     *
     * @param name     the name of the item stack
     * @param color    the color to apply to the lore
     * @param material the material of the item stack
     * @param data     additional data for the item
     * @param amount   the amount of items in the stack
     * @param lore     the lore to display
     * @return an {@link ItemStack.Builder} with the specified properties
     */
    public static ItemStack.Builder getSingleLoreStack(String name, String color, Material material, short data, int amount, String lore) {
        List<String> l = new ArrayList<>();
        for (String line : StringUtility.splitByWordAndLength(lore, 30)) {
            l.add(color + line);
        }
        return getStack(name, material, amount, l.toArray(new String[]{}));
    }

    /**
     * Creates an {@link ItemStack.Builder} with specified name, material, amount, and lore.
     *
     * @param name     the name of the item stack
     * @param material the material of the item stack
     * @param amount   the amount of items in the stack
     * @param lore     the lore of the item stack
     * @return an {@link ItemStack.Builder} with the specified properties
     */
    public static ItemStack.Builder getStack(String name, Material material, int amount, String... lore) {
        return getStack(name, material, amount, Arrays.asList(lore));
    }

    /**
     * Updates the lore of the given {@link ItemStack.Builder} with the specified lore lines.
     *
     * @param builder the {@link ItemStack.Builder} to update
     * @param lore    the new lore lines to set
     * @return the updated {@link ItemStack.Builder}
     */
    public static ItemStack.Builder updateLore(ItemStack.Builder builder, List<String> lore) {
        List<String> copiedLore = new ArrayList<>();
        for (String s : lore) {
            copiedLore.add(color(s));
        }

        return clearAttributes(builder.set(ItemComponent.LORE, copiedLore.stream()
                        .map(line -> Component.text(line).decoration(TextDecoration.ITALIC, false))
                        .collect(Collectors.toList()))
                .set(ItemComponent.HIDE_ADDITIONAL_TOOLTIP, Unit.INSTANCE));
    }

    /**
     * Marks the given {@link ItemStack.Builder} as not editable.
     *
     * @param builder the {@link ItemStack.Builder} to modify
     * @return the modified {@link ItemStack.Builder}
     */
    public static ItemStack.Builder setNotEditable(ItemStack.Builder builder) {
        return builder.set(Tag.Boolean("uneditable"), true);
    }

    /**
     * Applies an enchantment glint to the given {@link ItemStack.Builder}.
     *
     * @param builder the {@link ItemStack.Builder} to modify
     * @return the modified {@link ItemStack.Builder}
     */
    public static ItemStack.Builder enchant(ItemStack.Builder builder) {
        return clearAttributes(builder.set(ItemComponent.ENCHANTMENT_GLINT_OVERRIDE, true)
                .set(ItemComponent.HIDE_ADDITIONAL_TOOLTIP, Unit.INSTANCE));
    }

    /**
     * Creates an {@link ItemStack.Builder} from an existing {@link ItemStack}.
     *
     * @param stack the original {@link ItemStack} to create a builder from
     * @return an {@link ItemStack.Builder} with the properties of the original stack
     */
    public static ItemStack.Builder getFromStack(ItemStack stack) {
        return clearAttributes(ItemStack.builder(stack.material())
                .amount(stack.amount())
                .set(ItemComponent.LORE, stack.get(ItemComponent.LORE))
                .set(ItemComponent.CUSTOM_NAME, stack.get(ItemComponent.CUSTOM_NAME))
                .set(ItemComponent.CUSTOM_DATA, stack.get(ItemComponent.CUSTOM_DATA)));
    }

    /**
     * Creates an {@link ItemStack.Builder} with the specified name, material, amount, and lore list.
     *
     * @param name     the name of the item stack
     * @param material the material of the item stack
     * @param amount   the amount of items in the stack
     * @param lore     the list of lore lines for the item stack
     * @return an {@link ItemStack.Builder} with the specified properties
     */
    public static ItemStack.Builder getStack(String name, Material material, int amount, List<String> lore) {
        List<String> copiedLore = new ArrayList<>();
        for (String s : lore) {
            copiedLore.add(color(s));
        }

        return clearAttributes(ItemStack.builder(material).amount(amount).set(ItemComponent.LORE, copiedLore.stream()
                        .map(line -> Component.text(line).decoration(TextDecoration.ITALIC, false))
                        .collect(Collectors.toList()))
                .set(ItemComponent.CUSTOM_NAME, Component.text(name).decoration(TextDecoration.ITALIC, false))
                .set(ItemComponent.HIDE_ADDITIONAL_TOOLTIP, Unit.INSTANCE));
    }

    /**
     * Creates an {@link ItemStack.Builder} for a player head with the specified properties.
     *
     * @param name    the name of the item stack
     * @param texture the texture URL of the player skin
     * @param amount  the amount of items in the stack
     * @param lore    the lore of the item stack
     * @return an {@link ItemStack.Builder} for a player head with the specified properties
     */
    public static ItemStack.Builder getStackHead(String name, String texture, int amount, String... lore) {
        return getStackHead(name, texture, amount, Arrays.asList(lore));
    }

    /**
     * Creates an {@link ItemStack.Builder} for a player head with the specified name and texture.
     *
     * @param name    the name of the item stack
     * @param texture the texture URL of the player skin
     * @return an {@link ItemStack.Builder} for a player head
     */
    public static ItemStack.Builder getStackHead(String name, String texture) {
        return getStackHead(name, texture, 1, new ArrayList<>());
    }

    /**
     * Creates an {@link ItemStack.Builder} for a player head with the specified texture.
     *
     * @param texture the texture URL of the player skin
     * @return an {@link ItemStack.Builder} for a player head
     */
    public static ItemStack.Builder getStackHead(String texture) {
        return getStackHead("", texture, 1, new ArrayList<>());
    }

    /**
     * Creates an {@link ItemStack.Builder} for a player head using a {@link PlayerSkin}.
     *
     * @param name   the name of the item stack
     * @param skin   the {@link PlayerSkin} to use for the head
     * @param amount the amount of items in the stack
     * @param lore   the lore of the item stack
     * @return an {@link ItemStack.Builder} for a player head
     */
    public static ItemStack.Builder getStackHead(String name, PlayerSkin skin, int amount, String... lore) {
        return getStackHead(name, skin, amount, Arrays.asList(lore));
    }

    /**
     * Creates an {@link ItemStack.Builder} for a player head with a specified name, texture, amount, and lore list.
     *
     * @param name    the name of the item stack
     * @param texture the texture URL of the player skin
     * @param amount  the amount of items in the stack
     * @param lore    the list of lore lines for the item stack
     * @return an {@link ItemStack.Builder} for a player head with the specified properties
     */
    public static ItemStack.Builder getStackHead(String name, String texture, int amount, List<String> lore) {
        List<String> copiedLore = new ArrayList<>();
        for (String s : lore) {
            copiedLore.add(color(s));
        }

        JSONObject json = new JSONObject();
        json.put("isPublic", true);
        json.put("signatureRequired", false);
        json.put("textures", new JSONObject().put("SKIN",
                new JSONObject().put("url", "http://textures.minecraft.net/texture/" + texture).put("metadata", new JSONObject().put("model", "slim"))));

        String texturesEncoded = Base64.getEncoder().encodeToString(json.toString().getBytes());

        return ItemStack.builder(Material.PLAYER_HEAD)
                .set(ItemComponent.LORE, copiedLore.stream()
                        .map(line -> Component.text(line).decoration(TextDecoration.ITALIC, false))
                        .collect(Collectors.toList()))
                .set(ItemComponent.CUSTOM_NAME, Component.text(name).decoration(TextDecoration.ITALIC, false))
                .set(ItemComponent.HIDE_ADDITIONAL_TOOLTIP, Unit.INSTANCE)
                .set(ItemComponent.PROFILE, new HeadProfile(new PlayerSkin(texturesEncoded, null)))
                .amount(amount);
    }

    /**
     * Creates an {@link ItemStack.Builder} for a player head with a specified name, {@link PlayerSkin}, amount, and lore list.
     *
     * @param name    the name of the item stack
     * @param skin    the {@link PlayerSkin} to use for the head
     * @param amount  the amount of items in the stack
     * @param lore    the list of lore lines for the item stack
     * @return an {@link ItemStack.Builder} for a player head with the specified properties
     */
    public static ItemStack.Builder getStackHead(String name, PlayerSkin skin, int amount, List<String> lore) {
        List<String> copiedLore = new ArrayList<>();
        for (String s : lore) {
            copiedLore.add(color(s));
        }

        return clearAttributes(ItemStack.builder(Material.PLAYER_HEAD)
                .set(ItemComponent.LORE, copiedLore.stream()
                        .map(line -> Component.text(line).decoration(TextDecoration.ITALIC, false))
                        .collect(Collectors.toList()))
                .set(ItemComponent.CUSTOM_NAME, Component.text(name).decoration(TextDecoration.ITALIC, false))
                .set(ItemComponent.HIDE_ADDITIONAL_TOOLTIP, Unit.INSTANCE)
                .set(ItemComponent.PROFILE, new HeadProfile(skin))
                .amount(amount));
    }

    /**
     * Replaces color codes in the given string with Minecraft color codes.
     *
     * @param string the input string with color codes
     * @return the string with color codes replaced
     */
    public static String color(String string) {
        return string.replace("&", "§");
    }
}

