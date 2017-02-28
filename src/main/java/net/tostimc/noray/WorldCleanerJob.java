package net.tostimc.noray;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

import lombok.RequiredArgsConstructor;

/**
 * @author Tim Biesenbeek
 */
@RequiredArgsConstructor
public class WorldCleanerJob implements Runnable {

	private static int counter;

	private final Chunk chunk;
	private final SqlWorldHelper worldHelper;

	private List<Location> blocksToReplace = new ArrayList<>();

	private void markBlock(SqlWorldHelper worldHelper, Block block) {
		worldHelper.addBlock(block);
		blocksToReplace.add(block.getLocation());
	}

	@Override
	public void run() {
		for (int x = 0; x < 16; x++) {
			for (int z = 0; z < 16; z++) {
				for (int y = 0; y < 255; y++) {
					Block block = chunk.getBlock(x, y, z);
					Arrays.stream(WorldCleaner.ORES) //
							.filter(material -> material == block.getType()) //
							.filter(material -> isHiddenBlock(block)) //
							.forEach(material -> markBlock(worldHelper, block));
				}
			}
		}
		worldHelper.save();
		Bukkit.getScheduler().scheduleSyncDelayedTask(PluginStarter.getInstance(), this::done);
	}

	private void done() {
		blocksToReplace.forEach(location -> location.getBlock().setType(Material.STONE));
	}

	private boolean isHiddenBlock(Block block) {
		boolean isHidden = true;
		try {
			for (BlockFace blockFace : PluginStarter.getInstance().getDirectNeighbours()) {
				Block neighbour = block.getRelative(blockFace);
				if (neighbour == null || neighbour.getType().isTransparent()
						|| Arrays.stream(WorldCleaner.OPAQUE).anyMatch(material -> material == neighbour.getType())) {
					isHidden = false;
					break;
				}
			}
			return isHidden;
		} catch (IllegalStateException ex) {
			Bukkit.getLogger().log(Level.FINEST, getClass().toString() + ":\t" + ex.toString());
			return false;
		}
	}
}
