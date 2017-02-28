package net.tostimc.noray;

import java.sql.*;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;

import lombok.Getter;

/**
 * @author Tim Biesenbeek
 */
public class SqlWorldHelper {

	@Getter
	private final String worldName;
	private Connection connection;
	private PreparedStatement insertBlock;
	private PreparedStatement getBlock;
	private PreparedStatement removeBlock;

	public SqlWorldHelper(World world) {
		this.worldName = world.getName();
		init();
	}

	private void init() {
		setupConnection();
	}

	public void addBlock(org.bukkit.block.Block block) {
		addBlock(block.getX(), block.getY(), block.getZ(), block.getType().toString());
	}

	public void addBlock(int x, int y, int z, String type) {
		synchronized (insertBlock) {
			try {
				insertBlock.clearParameters();
				insertBlock.setInt(1, x);
				insertBlock.setInt(2, y);
				insertBlock.setInt(3, z);
				insertBlock.setString(4, type);
				insertBlock.execute();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}

	public Material getOriginalType(Location location) {
		return getOriginalType(location.getBlockX(), location.getBlockY(), location.getBlockZ());
	}

	public Material getOriginalType(int x, int y, int z) {
		synchronized (getBlock) {
			try {
				getBlock.clearParameters();
				getBlock.setInt(1, x);
				getBlock.setInt(2, y);
				getBlock.setInt(3, z);
				ResultSet rs = getBlock.executeQuery();
				if (rs.next()) {
					String material = rs.getString(1);
					removeBlock(x, y, z);
					return Material.valueOf(material);
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		return null;
	}

	public void removeBlock(Location location) {
		removeBlock(location.getBlockX(), location.getBlockY(), location.getBlockZ());
	}

	public void removeBlock(int x, int y, int z) {
		synchronized (removeBlock) {
			try {
				removeBlock.clearParameters();
				removeBlock.setInt(1, x);
				removeBlock.setInt(2, y);
				removeBlock.setInt(3, z);
				removeBlock.execute();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}

	public boolean isSameWorld(World world) {
		return worldName.equals(world.getName());
	}

	public void save() {
		try {
			if (!connection.getAutoCommit()) {
				connection.commit();
			}
		} catch (SQLException ignored) {
		}
	}

	public void close() {
		save();
		try {
			connection.close();
		} catch (SQLException ex) {
			ex.printStackTrace();
		}
	}

	private void setupConnection() {
		connection = null;
		try {
			Class.forName("org.sqlite.JDBC");
			connection = DriverManager.getConnection("jdbc:sqlite:" + worldName + "/ores.db");
			Statement statement = connection.createStatement();
			String createTable = "CREATE TABLE IF NOT EXISTS ores (\n" + //
					"  X INTEGER(8),\n" + //
					"  Y INTEGER(8),\n" + //
					"  Z INTEGER(8),\n" + //
					"  Material VARCHAR(30),\n" + //
					"  PRIMARY KEY (X, Y, Z)\n" + //
					")";
			statement.executeUpdate(createTable);
			statement.close();

			connection.setAutoCommit(false);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		try {
			insertBlock = connection.prepareStatement("INSERT INTO ores VALUES (?, ?, ?, ?);");
			getBlock = connection.prepareStatement("SELECT Material FROM ores WHERE X = ? AND Y = ? AND Z = ?;");
			removeBlock = connection.prepareStatement("DELETE FROM ores WHERE X = ? AND Y = ? AND Z = ?;");
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
}
