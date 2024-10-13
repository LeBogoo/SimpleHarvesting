package me.lebogo.simpleHarvesting;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class PlayerInteractListener implements Listener {
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        ItemStack item = event.getItem();
        Player player = event.getPlayer();

        if (item == null) return;
        if (!player.isSneaking()) return;

        ItemMeta itemMeta = item.getItemMeta();
        if (itemMeta == null) return;

        List<Component> lore = itemMeta.lore();
        if (lore == null) return;

        boolean harvestModeEnabled = !lore.isEmpty() && lore.getFirst().equals(SimpleHarvesting.harvestModeEnabledComponent);

        List<Component> newLore = new ArrayList<>(lore);
        newLore.set(0, harvestModeEnabled ? SimpleHarvesting.harvestModeDisabledComponent : SimpleHarvesting.harvestModeEnabledComponent);

        itemMeta.lore(newLore);
        item.setItemMeta(itemMeta);

        player.sendMessage(harvestModeEnabled ? SimpleHarvesting.harvestModeDisabledComponent : SimpleHarvesting.harvestModeEnabledComponent);
    }

    /**
     * Get all enabled harvest hoes from player inventory
     *
     * @param player player
     * @return list of enabled harvest hoes
     */
    public List<ItemStack> getEnabledHarvestHoes(Player player) {
        List<ItemStack> harvestHoes = new ArrayList<>();

        for (ItemStack itemStack : player.getInventory().getContents()) {
            if (itemStack == null) continue;

            ItemMeta itemMeta = itemStack.getItemMeta();
            if (itemMeta == null) continue;

            List<Component> lore = itemMeta.lore();
            if (lore == null) continue;

            for (Component component : lore) {

                if (component.equals(SimpleHarvesting.harvestModeEnabledComponent)) {
                    harvestHoes.add(itemStack);
                }
            }
        }

        return harvestHoes;
    }

    private int getHarvestHoeDiameter(ItemStack harvestHoe) {
        ItemMeta itemMeta = harvestHoe.getItemMeta();
        if (itemMeta == null) return 0;

        List<Component> lore = itemMeta.lore();
        if (lore == null) return 0;

        for (Component component : lore) {
            List<Component> componentList = component.children();
            if (componentList.isEmpty()) continue;

            if (SimpleHarvesting.diameterComponent.equals(componentList.getFirst())) {
                TextComponent textComponent = (TextComponent) componentList.getLast();
                return Integer.parseInt(textComponent.content());
            }
        }

        return -1;
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        // TODO - check if player got enabled tool in inventory


        int diameter = -2;

        for (ItemStack enabledHarvestHoe : getEnabledHarvestHoes(player)) {
            int currentDiameter = getHarvestHoeDiameter(enabledHarvestHoe);
            if (currentDiameter > diameter) {

                diameter = currentDiameter;
            }
        }

        if (diameter < 1) return;

        Location playerBlockLocation = event.getPlayer().getLocation().toBlockLocation();

        List<Block> cropBlocks = getCropBlocks(playerBlockLocation, diameter / 2.0f);

        Map<Material, Material> allowedCrops = Map.of(
                Material.WHEAT_SEEDS, Material.WHEAT,
                Material.CARROT, Material.CARROTS,
                Material.POTATO, Material.POTATOES,
                Material.BEETROOT_SEEDS, Material.BEETROOTS
        );


        ItemStack handItem = player.getInventory().getItemInMainHand();

        for (Block block : cropBlocks) {
            if (!allowedCrops.containsValue(block.getType())) continue;
            block.breakNaturally(true);
            block.getLocation().getNearbyEntities(1, 1, 1).forEach(entity -> {
                if (entity instanceof Item item) {
                    item.teleport(player.getLocation());
                    item.setPickupDelay(0);
                }
            });


        }

        List<Block> emptyFarmlandBlocks = getEmptyFarmlandBlocks(playerBlockLocation, (diameter / 2.0f) + 1f);
        for (Block block : emptyFarmlandBlocks) {
            Block blockAbove = block.getLocation().add(0, 1, 0).getBlock();
            Material handType = handItem.getType();
            // get placeable crop from seed hold in hand
            if (allowedCrops.containsKey(handType)) {
                Material cropType = allowedCrops.get(handType);
                blockAbove.setType(cropType);
                handItem.setAmount(handItem.getAmount() - 1);

                // if handitem is empty, get similar item from inventory and set it to hand item
                if (handItem.getAmount() == 0) {
                    for (ItemStack itemStack : player.getInventory().getContents()) {
                        if (itemStack == null) continue;
                        if (itemStack.getType().equals(handType) && itemStack.getAmount() != 0) {
                            player.getInventory().removeItem(itemStack);
                            player.getInventory().setItemInMainHand(itemStack);
                            handItem = itemStack;
                            break;
                        }
                    }
                }
            }
        }
    }


    private List<Block> getCropBlocks(Location location, float radius) {
        List<Block> blocks = new ArrayList<>();
        World world = location.getWorld();
        int x = location.getBlockX();
        int y = location.getBlockY();
        int z = location.getBlockZ();

        int ceil = (int) Math.ceil(radius);


        for (int i = -ceil; i <= ceil; i++) {
            for (int j = -ceil; j <= ceil; j++) {
                for (int k = -ceil; k <= ceil; k++) {
                    Block block = world.getBlockAt(x + i, y + j, z + k);
                    if (!(block.getBlockData() instanceof Ageable ageable)) continue;
                    if (ageable.getAge() != ageable.getMaximumAge()) continue;

                    blocks.add(block);
                }
            }
        }

        blocks.removeIf(block -> block.getLocation().distance(location) > radius);
        blocks.sort(Comparator.comparingDouble(block -> block.getLocation().distance(location)));

        return blocks;
    }

    private List<Block> getEmptyFarmlandBlocks(Location location, float radius) {
        List<Block> blocks = new ArrayList<>();
        World world = location.getWorld();
        int x = location.getBlockX();
        int y = location.getBlockY();
        int z = location.getBlockZ();

        int ceil = (int) Math.ceil(radius);

        for (int i = -ceil; i <= ceil; i++) {
            for (int j = -ceil; j <= ceil; j++) {
                for (int k = -ceil; k <= ceil; k++) {
                    Block block = world.getBlockAt(x + i, y + j, z + k);
                    if (!block.getType().equals(Material.FARMLAND)) continue;
                    if (block.getLocation().add(0, 1, 0).getBlock().getType() != Material.AIR) continue;
                    blocks.add(block);
                }
            }
        }

        blocks.removeIf(block -> block.getLocation().distance(location) > radius);
        blocks.sort(Comparator.comparingDouble(block -> block.getLocation().distance(location)));

        return blocks;
    }

}
