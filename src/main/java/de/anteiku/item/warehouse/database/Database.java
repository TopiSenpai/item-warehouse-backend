package de.anteiku.item.warehouse.database;

import de.anteiku.item.warehouse.objects.Warehouse;
import de.anteiku.item.warehouse.objects.WarehouseItem;
import de.anteiku.item.warehouse.utils.Trio;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

public class Database{

	private static final Logger LOG = LoggerFactory.getLogger(SQL.class);
	private static final String CHARS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

	public static void init(){
		SQL.createTable("sessions");
		SQL.createTable("users");
		SQL.createTable("warehouse_categories");
		SQL.createTable("warehouse_item_conditions");
		SQL.createTable("warehouse_items");
		SQL.createTable("warehouse_user_permissions");
		SQL.createTable("warehouses");
	}

	public static Trio<Integer, String, String> getUserLogin(String username){
		var stmt = SQL.prepStatement("SELECT user_id, user_password, user_salt FROM users WHERE user_name = ?");
		try{
			stmt.setString(1, username);
			var result = SQL.query(stmt);
			if(result != null && result.next()){
				return new Trio<>(result.getInt("user_id"), result.getString("user_password"), result.getString("user_salt"));
			}
		}
		catch(SQLException e){
			LOG.error("Error while getting login information", e);
		}
		return null;
	}

	public static int registerUser(String username, String hash, String salt){
		var stmt = SQL.prepStatement("INSERT INTO users (user_name, user_password, user_salt, user_updated_at, user_created_at) VALUES (?, ?, ?, ?, ?)", PreparedStatement.RETURN_GENERATED_KEYS);
		try{
			var currentTimeMillis = System.currentTimeMillis();
			stmt.setString(1, username);
			stmt.setString(2, hash);
			stmt.setString(3, salt);
			stmt.setLong(4, currentTimeMillis);
			stmt.setLong(5, currentTimeMillis);
			var result = SQL.executeWithResult(stmt);
			if(result != null && result.next()){
				return result.getInt("user_id");
			}
		}
		catch(SQLException | NullPointerException e){
			LOG.error("Error while getting warehouse permissions", e);
		}
		return -1;
	}

	public static String getUsername(int userId){
		var stmt = SQL.prepStatement("SELECT user_name FROM users WHERE user_id = ?");
		try{
			stmt.setInt(1, userId);
			var result = SQL.query(stmt);
			if(result != null && result.next()){
				return result.getString("user_name");
			}
		}
		catch(SQLException | NullPointerException e){
			LOG.error("Error while getting user_name", e);
		}
		return null;
	}

	public static int getUserWarehousePermission(int warehouseId, int userId){
		var stmt = SQL.prepStatement("SELECT wup_permissions FROM warehouse_user_permissions WHERE wup_warehouse = ? AND wup_user = ?");
		try{
			stmt.setInt(1, warehouseId);
			stmt.setInt(2, userId);
			var result = SQL.query(stmt);
			if(result != null && result.next()){
				return result.getInt("wup_permissions");
			}
		}
		catch(SQLException e){
			LOG.error("Error while getting warehouse permissions", e);
		}
		return -1;
	}

	public static Set<Warehouse> getWarehousesFromUserId(int userId){
		var stmt = SQL.prepStatement("SELECT * FROM warehouse_user_permissions JOIN warehouses ON wup_warehouse = warehouse_id WHERE wup_user = ?");
		try{
			stmt.setInt(1, userId);
			var result = SQL.query(stmt);
			var warehouses = new HashSet<Warehouse>();
			while(result != null && result.next()){
				warehouses.add(new Warehouse(result.getInt("warehouse_id"), result.getString("warehouse_name")));
			}
			return warehouses;
		}
		catch(SQLException | NullPointerException e){
			LOG.error("Error while getting warehouse permissions", e);
		}
		return null;
	}

	public static Set<WarehouseItem> getWarehouseItems(int warehouseId){
		var stmt = SQL.prepStatement("SELECT * FROM warehouse_items" +
				"JOIN warehouses ON wi_warehouse = warehouse_id" +
				"JOIN users ON wi_owner = user_id" +
				"WHERE wi_warehouse = ?");
		try{
			stmt.setInt(1, warehouseId);
			var result = SQL.query(stmt);
			var warehouseItems = new HashSet<WarehouseItem>();
			while(result != null && result.next()){
				warehouseItems.add(new WarehouseItem(result.getInt("wi_id"), warehouseId, result.getString("warehouse_name"), result.getString("wi_name"),
					result.getInt("wi_owner"), result.getString("user_name"), result.getInt("wi_count"), result.getString("wi_description"),
					result.getString("wi_storage_place"), result.getInt("wi_category"), "", result.getInt("wi_condition"), "",
					"", result.getString("wi_purchase_place"), result.getInt("wi_purchase_price"), result.getLong("wi_purchase_date"),
					result.getLong("wi_updated_at"), result.getLong("wi_created_at")
				));
			}
			return warehouseItems;
		}
		catch(SQLException | NullPointerException e){
			LOG.error("Error while getting warehouse permissions", e);
		}
		return null;
	}

	public static Warehouse addWarehouse(String warehouseName, int userId){
		var stmt = SQL.prepStatement("WITH new_warehouse AS (INSERT INTO warehouses (warehouse_name, warehouse_updated_at, warehouse_created_at) VALUES (?, ?, ?) RETURNING warehouse_id) " +
			"INSERT INTO warehouse_user_permissions (wup_warehouse, wup_user, wup_permissions, wup_updated_at, wup_created_at) VALUES ((SELECT warehouse_id FROM new_warehouse), ?, ?, ?, ?)", PreparedStatement.RETURN_GENERATED_KEYS);
		try{
			var currentTimeMillis = System.currentTimeMillis();
			stmt.setString(1, warehouseName);
			stmt.setLong(2, currentTimeMillis);
			stmt.setLong(3, currentTimeMillis);
			stmt.setInt(4, userId);
			stmt.setInt(5, 2);//Owner Permission
			stmt.setLong(6, currentTimeMillis);
			stmt.setLong(7, currentTimeMillis);
			var result = SQL.executeWithResult(stmt);
			if(result != null && result.next()){
				return new Warehouse(result.getInt(1), warehouseName);
			}
		}
		catch(SQLException | NullPointerException e){
			LOG.error("Error while adding warehouse", e);
		}
		return null;
	}

	public static int addWarehouseItem(int warehouseId, int userId, String name, int count, String description, String storagePlace, int category, int condition, String imagePath, String purchasePlace, int purchasePrice, long purchaseDate){
		var stmt = SQL.prepStatement("INSERT INTO warehouse_items (wi_warehouse, wi_name, wi_owner, wi_count, wi_description, wi_storage_place, wi_category, wi_condition, wi_image_path," +
				"wi_purchase_place, wi_purchase_price, wi_purchase_date, wi_updated_at, wi_created_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)", PreparedStatement.RETURN_GENERATED_KEYS);
		try{
			var currentTime = System.currentTimeMillis();
			stmt.setInt(1, warehouseId);
			stmt.setString(2, name);
			stmt.setInt(3, userId);
			stmt.setInt(4, count);
			stmt.setString(5, description);
			stmt.setString(6, storagePlace);
			stmt.setInt(7, category);
			stmt.setInt(8, condition);
			stmt.setString(9, imagePath);
			stmt.setString(10, purchasePlace);
			stmt.setInt(11, purchasePrice);
			stmt.setLong(12, purchaseDate);
			stmt.setLong(13, currentTime);
			stmt.setLong(14, currentTime);
			var result = SQL.executeWithResult(stmt);
			if(result != null && result.next()){
				return result.getInt(1);
			}
		}
		catch(SQLException | NullPointerException e){
			LOG.error("Error while adding warehouse", e);
		}
		return -1;
	}

	public static String generate(int length){
		StringBuilder builder = new StringBuilder();
		while(length-- != 0){
			builder.append(CHARS.charAt((int) (Math.random() * CHARS.length())));
		}
		return builder.toString();
	}

	public static boolean addSession(String sessionId, int userId){
		var stmt = SQL.prepStatement("INSERT INTO sessions (session_id, session_user_id, session_created_at) VALUES (?, ?, ?)");
		try{
			stmt.setString(1, sessionId);
			stmt.setInt(2, userId);
			stmt.setLong(3, System.currentTimeMillis());
			return SQL.execute(stmt);
		}
		catch(SQLException e){
			LOG.error("Error while adding session", e);
		}
		return false;
	}

	public static String generateUniqueKey(){
		String key;
		do{
			key = generate(64);
		}
		while(sessionExists(key));
		return key;
	}

	public static boolean sessionExists(String sessionId){
		var stmt = SQL.prepStatement("SELECT * FROM sessions WHERE session_id = ?");
		try{
			stmt.setString(1, sessionId);
			return SQL.exists(stmt);
		}
		catch(SQLException e){
			LOG.error("Error while adding session", e);
		}
		return false;
	}

	public static boolean deleteSession(String sessionId){
		var stmt = SQL.prepStatement("DELETE FROM sessions WHERE session_id = ?");
		try{
			stmt.setString(1, sessionId);
			return SQL.execute(stmt);
		}
		catch(SQLException e){
			LOG.error("Error while deleting session", e);
		}
		return false;
	}

	public static int getUserFromSession(String sessionId){
		var stmt = SQL.prepStatement("SELECT * FROM sessions WHERE session_id = ?");
		try{
			stmt.setString(1, sessionId);
			var result = SQL.query(stmt);
			if(result != null && result.next()){
				return result.getInt("session_user_id");
			}
		}
		catch(SQLException e){
			LOG.error("Error while getting session", e);
		}
		return -1;
	}

}
