package net.tostimc.noray;

import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkPopulateEvent;

public class WorldCleaner implements Listener {

	public static final Material[] ORES = new Material[]{//
			Material.COAL_ORE, //
			Material.DIAMOND_ORE, //
			Material.EMERALD_ORE, //
			Material.GOLD_ORE, //
			Material.IRON_ORE, //
			Material.LAPIS_ORE, //
			Material.QUARTZ_ORE, //
			Material.REDSTONE_ORE//
	};
	public static final Material[] OPAQUE = new Material[]{//
			Material.WATER, //
			Material.STATIONARY_WATER, //
			Material.LAVA, //
			Material.STATIONARY_LAVA, //
			Material.FENCE, //
			Material.NETHER_FENCE, //
			Material.IRON_FENCE, //
			Material.CHEST, //
			Material.WEB, //
			Material.SMOOTH_STAIRS, //
			Material.NETHER_BRICK_STAIRS //
	};

	@EventHandler
	public void onChunkGenerate(ChunkPopulateEvent event) {
		Chunk chunk = event.getChunk();
		SqlWorldHelper worldHelper = PluginStarter.getInstance().getWorldHelper(chunk.getWorld());
		WorldCleanerJob job = new WorldCleanerJob(chunk, worldHelper);
		PluginStarter.getInstance().getThreadpool().submit(job);
	}

}
