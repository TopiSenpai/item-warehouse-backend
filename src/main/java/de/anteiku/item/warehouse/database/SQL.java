package de.anteiku.item.warehouse.database;

import com.zaxxer.hikari.HikariDataSource;
import de.anteiku.item.warehouse.utils.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;

public class SQL{

	private static final Logger LOG = LoggerFactory.getLogger(SQL.class);

	private static HikariDataSource dataSource;

	static{
		try{
			dataSource = new HikariDataSource();
			dataSource.setDriverClassName("org.postgresql.ds.PGSimpleDataSource");

			dataSource.setJdbcUrl("jdbc:postgresql://" + Config.DB_HOST + ":" + Config.DB_PORT + "/" + Config.DB_DATABASE);
			dataSource.setUsername(Config.DB_USER);
			dataSource.setPassword(Config.DB_PASSWORD);

			dataSource.setMinimumIdle(100);
			dataSource.setMaximumPoolSize(2000);
			dataSource.setAutoCommit(false);
			dataSource.setLoginTimeout(3);

		}
		catch(SQLException e) {
			LOG.error("Error while initializing database connection", e);
		}
	}

	public static Connection getConnection(){
		try{
			return dataSource.getConnection();
		}
		catch(SQLException e){
			LOG.error("Error while fetching connection from datasource", e);
		}
		return null;
	}

	public static void use(String db){
		execute("USE " + db + ";");
	}

	public static boolean execute(String query){
		try{
			LOG.debug(query);
			return getConnection().createStatement().execute(query);
		}
		catch(SQLException e){
			LOG.error("Error while executing sql command", e);
		}
		return false;
	}

	public static int update(String query){
		try{
			LOG.debug(query);
			return getConnection().createStatement().executeUpdate(query);
		}
		catch(SQLException e){
			LOG.error("Error while executing sql command", e);
		}
		return -1;
	}

	public static ResultSet query(String query){
		try{
			Statement statement = getConnection().createStatement();
			LOG.debug(query);
			return statement.executeQuery(query);
		}
		catch(SQLException e){
			LOG.error("Error while querying sql command", e);
		}
		return null;
	}

	public static boolean exists(String query){
		try{
			return query(query).next();
		}
		catch(SQLException e){
			LOG.error("Error while checking if sql entry exists", e);
		}
		return false;
	}


}
