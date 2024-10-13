package me.lebogo.simpleHarvesting;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

public final class SimpleHarvesting extends JavaPlugin {

    public static final TextComponent harvestModeComponent = Component.text("Harvest Mode").color(TextColor.color(0xFBFB54)).decoration(TextDecoration.ITALIC, false);
    public static final TextComponent enabledComponent = Component.text("Enabled").style(Style.style(TextColor.color(0x4FFB54)));
    public static final TextComponent disabledComponent = Component.text("Disabled").style(Style.style(TextColor.color(0xFB5454)));
    public static final TextComponent harvestModeEnabledComponent = harvestModeComponent.append(Component.text(": ")).append(enabledComponent);
    public static final TextComponent harvestModeDisabledComponent = harvestModeComponent.append(Component.text(": ")).append(disabledComponent);
    public static final TextComponent diameterComponent = Component.text("Diameter");

    public static final ItemStack WOODEN_HARVEST_HOE = new ItemStack(Material.WOODEN_HOE);
    public static final ItemStack STONE_HARVEST_HOE = new ItemStack(Material.STONE_HOE);
    public static final ItemStack IRON_HARVEST_HOE = new ItemStack(Material.IRON_HOE);
    public static final ItemStack GOLDEN_HARVEST_HOE = new ItemStack(Material.GOLDEN_HOE);
    public static final ItemStack DIAMOND_HARVEST_HOE = new ItemStack(Material.DIAMOND_HOE);
    public static final ItemStack NETHERITE_HARVEST_HOE = new ItemStack(Material.NETHERITE_HOE);


    @Override
    public void onEnable() {
        // Plugin startup logic
        getServer().getPluginManager().registerEvents(new PlayerInteractListener(), this);

        updateHarvestHoe(WOODEN_HARVEST_HOE, "Wooden Harvest Hoe", 3);
        updateHarvestHoe(STONE_HARVEST_HOE, "Stone Harvest Hoe", 5);
        updateHarvestHoe(IRON_HARVEST_HOE, "Iron Harvest Hoe", 7);
        updateHarvestHoe(GOLDEN_HARVEST_HOE, "Golden Harvest Hoe", 9);
        updateHarvestHoe(DIAMOND_HARVEST_HOE, "Diamond Harvest Hoe", 11);
        updateHarvestHoe(NETHERITE_HARVEST_HOE, "Netherite Harvest Hoe", 13);

        registerCraftingRecipes();

    }

    private void updateHarvestHoe(ItemStack itemStack, String displayName, int diameter) {
        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.displayName(Component.text(displayName).decoration(TextDecoration.ITALIC, false));
        itemMeta.lore(List.of(
                harvestModeDisabledComponent,
                Component.textOfChildren(
                                diameterComponent,
                                Component.text(": "),
                                Component.text(String.valueOf(diameter)).color(TextColor.color(0xffffff))
                        )
                        .decoration(TextDecoration.ITALIC, false)
                        .color(TextColor.color(0xFBFB54))
        ));
        itemMeta.setEnchantmentGlintOverride(true);
        itemStack.setItemMeta(itemMeta);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    private void registerCraftingRecipes() {
        List<String> shape = List.of(" M ", "MHM", " M ");

        getServer().addRecipe(new ShapedRecipe(new NamespacedKey(this, "wooden_harvest_hoe"), WOODEN_HARVEST_HOE)
                .shape(shape.get(0), shape.get(1), shape.get(2))
                .setIngredient('H', Material.WOODEN_HOE)
                .setIngredient('M', Material.STICK)
        );

        getServer().addRecipe(new ShapedRecipe(new NamespacedKey(this, "stone_harvest_hoe"), STONE_HARVEST_HOE)
                .shape(shape.get(0), shape.get(1), shape.get(2))
                .setIngredient('H', Material.STONE_HOE)
                .setIngredient('M', Material.COBBLESTONE)
        );

        getServer().addRecipe(new ShapedRecipe(new NamespacedKey(this, "iron_harvest_hoe"), IRON_HARVEST_HOE)
                .shape(shape.get(0), shape.get(1), shape.get(2))
                .setIngredient('H', Material.IRON_HOE)
                .setIngredient('M', Material.IRON_INGOT)
        );

        getServer().addRecipe(new ShapedRecipe(new NamespacedKey(this, "golden_harvest_hoe"), GOLDEN_HARVEST_HOE)
                .shape(shape.get(0), shape.get(1), shape.get(2))
                .setIngredient('H', Material.GOLDEN_HOE)
                .setIngredient('M', Material.GOLD_INGOT)
        );

        getServer().addRecipe(new ShapedRecipe(new NamespacedKey(this, "diamond_harvest_hoe"), DIAMOND_HARVEST_HOE)
                .shape(shape.get(0), shape.get(1), shape.get(2))
                .setIngredient('H', Material.DIAMOND_HOE)
                .setIngredient('M', Material.DIAMOND)
        );

        getServer().addRecipe(new ShapedRecipe(new NamespacedKey(this, "netherite_harvest_hoe"), NETHERITE_HARVEST_HOE)
                .shape(shape.get(0), shape.get(1), shape.get(2))
                .setIngredient('H', Material.NETHERITE_HOE)
                .setIngredient('M', Material.NETHERITE_INGOT)
        );
    }
}
