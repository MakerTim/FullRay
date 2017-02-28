package net.tostimc.noray;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;

/**
 * @author Tim Biesenbeek
 */
public class WorldReveal implements Listener {

	public void onBlockUpdate(Block block) {
		for (BlockFace blockFace : PluginStarter.getInstance().getDirectNeighbours()) {
			Block neighbour = block.getRelative(blockFace);
			if (neighbour != null) {
				Bukkit.getScheduler().runTaskAsynchronously(PluginStarter.getInstance(), () -> checkBlock(neighbour));
			}
		}
	}

	public void checkBlock(Block block) {
		SqlWorldHelper worldHelper = PluginStarter.getInstance().getWorldHelper(block.getWorld());
		Material material = worldHelper.getOriginalType(block.getLocation());
		if (material != null) {
			Bukkit.getScheduler().scheduleSyncDelayedTask(PluginStarter.getInstance(), () -> block.setType(material));
		}
	}

	@EventHandler
	public void onBlockUpdate(BlockPhysicsEvent event) {
		if (event.getBlock().getType().hasGravity()) {
			onBlockUpdate(event.getBlock());
		}
	}

	@EventHandler
	public void onBlockUpdate(BlockPlaceEvent event) {
		onBlockUpdate(event.getBlock());
	}

	@EventHandler
	public void onBlockUpdate(BlockMultiPlaceEvent event) {
		onBlockUpdate(event.getBlock());
	}

	@EventHandler
	public void onBlockUpdate(BlockExplodeEvent event) {
		onBlockUpdate(event.getBlock());
	}

	@EventHandler
	public void onBlockUpdate(BlockPistonExtendEvent event) {
		event.getBlocks().forEach(this::onBlockUpdate);
	}

	@EventHandler
	public void onBlockUpdate(BlockPistonRetractEvent event) {
		event.getBlocks().forEach(this::onBlockUpdate);
	}

	@EventHandler
	public void onBlockUpdate(BlockBreakEvent event) {
		onBlockUpdate(event.getBlock());
	}
}
