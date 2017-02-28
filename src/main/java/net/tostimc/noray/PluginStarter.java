package net.tostimc.noray;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import lombok.Getter;

public class PluginStarter extends JavaPlugin implements Listener {
	@Getter
	private static PluginStarter instance;
	@Getter
	// Just a DAO, but called helper
	private List<SqlWorldHelper> worldHelpers = new ArrayList<>();
	@Getter
	// Limit the threads to be 50 at the same tim
	private ExecutorService threadpool = Executors.newFixedThreadPool(50);
	@Getter
	// Up, Down, Left, Right, Front, Back
	private final BlockFace[] directNeighbours = new BlockFace[]{BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST,
			BlockFace.WEST, BlockFace.UP, BlockFace.DOWN};

	@Override
	public void onEnable() {
		instance = this;

		System.out.println("Register world cleaner");
		Bukkit.getPluginManager().registerEvents(new WorldCleaner(), this);
		Bukkit.getPluginManager().registerEvents(new WorldReveal(), this);
	}

	@Override
	public void onDisable() {
		super.onDisable();
		threadpool.shutdown();
		worldHelpers.forEach(SqlWorldHelper::close);
		try {
			threadpool.awaitTermination(60L, TimeUnit.SECONDS);
		} catch (InterruptedException ex) {
			ex.printStackTrace();
		}
	}

	// Small getter for Dao <-> World
	public SqlWorldHelper getWorldHelper(World world) {
		return worldHelpers.stream() //
				.filter(helper -> helper.isSameWorld(world)).findFirst() //
				.orElseGet(() -> {
					SqlWorldHelper newWorldHelper = new SqlWorldHelper(world);
					PluginStarter.getInstance().getWorldHelpers().add(newWorldHelper);
					return newWorldHelper;
				});
	}
}
