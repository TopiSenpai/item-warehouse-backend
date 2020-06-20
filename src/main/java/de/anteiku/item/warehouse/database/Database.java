package de.anteiku.item.warehouse.database;

import de.anteiku.item.warehouse.utils.Trio;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.ResultSet;
import java.sql.SQLException;

public class Database{

	private static final Logger LOG = LoggerFactory.getLogger(SQL.class);
	private static final String CHARS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

	public static Trio<Integer, String, String> getUserLogin(String username){
		var result = SQL.query("SELECT id, password, salt FROM users WHERE username = '" + username + "'");
		try{
			if(result.next()){
				return new Trio<Integer, String, String>(result.getInt("id"), result.getString("password"), result.getString("salt"));
			}
		}
		catch(SQLException e){
			LOG.error("Error while getting login information", e);
		}
		return null;
	}

	public static int getUserWarehousePermission(int warehouseId, int userId){
		var result = SQL.query("SELECT wup_permissions FROM warehouse_user_permissions WHERE wup_warehouse = '" + warehouseId + "' AND wup_user = '" + userId + "'");
		try{
			if(result.next()){
				return result.getInt("wup_permissions");
			}
		}
		catch(SQLException e){
			LOG.error("Error while getting warehouse permissions", e);
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

	public static void addSession(int user, String sessionId){
		SQL.execute("INSERT INTO sessions (session_id, user_id) VALUES ('" + sessionId + "', '" + user + "');");
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
		return SQL.exists("SELECT * FROM sessions WHERE session_id = '" + sessionId + "';");
	}

	public static boolean deleteSession(String sessionId){
		return SQL.execute("DELETE FROM sessions WHERE session_id = '" + sessionId + "';");
	}

	public static int getUserFromSession(String sessionId){
		ResultSet result = SQL.query("SELECT * FROM sessions WHERE session_id = '" + sessionId + "';");
		try{
			if(result.next()){
				return result.getInt("user_id");
			}
		}
		catch(SQLException e){
			LOG.error("Error while getting session", e);
		}
		return -1;
	}

}
