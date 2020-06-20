package de.anteiku.item.warehouse;

import de.anteiku.item.warehouse.utils.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ItemWarehouse{

	private static final Logger LOG = LoggerFactory.getLogger(ItemWarehouse.class);

	private ItemWarehouse(){
		LOG.info("Starting Warehouse Backend...");
		Config.load("config.yml");
		new WebService(6969);
	}

	public static void main(String[] args){
		new ItemWarehouse();
	}

}
