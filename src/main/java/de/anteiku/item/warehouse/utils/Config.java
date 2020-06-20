package de.anteiku.item.warehouse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Map;

public class Config{

	private static final Logger LOG = LoggerFactory.getLogger(Config.class);

	public static String WEBSITE_URL;

	public static String DB_HOST;
	public static String DB_PORT;
	public static String DB_DATABASE;
	public static String DB_USER;
	public static String DB_PASSWORD;

	public static void load(String filePath){
		Yaml yaml = new Yaml();
		try{
			Map<String, Object> config = yaml.load(new FileInputStream(new File(filePath)));

			WEBSITE_URL = String.valueOf(config.get("website_url"));
			DB_HOST = String.valueOf(config.get("db_host"));
			DB_PORT = String.valueOf(config.get("db_port"));
			DB_DATABASE = String.valueOf(config.get("db_database"));
			DB_USER = String.valueOf(config.get("db_user"));
			DB_PASSWORD = String.valueOf(config.get("db_password"));
		}
		catch(FileNotFoundException e){
			LOG.error("Error while reading config file", e);
		}
	}

}
