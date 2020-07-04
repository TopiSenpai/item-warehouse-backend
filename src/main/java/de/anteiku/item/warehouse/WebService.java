package de.anteiku.item.warehouse;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import de.anteiku.item.warehouse.database.Database;
import de.anteiku.item.warehouse.objects.Warehouse;
import de.anteiku.item.warehouse.objects.WarehouseItem;
import de.anteiku.item.warehouse.utils.Config;
import de.anteiku.item.warehouse.utils.Password;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Request;
import spark.Response;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import static spark.Spark.*;
import static spark.Spark.post;

public class WebService{

	private static final Logger LOG = LoggerFactory.getLogger(WebService.class);

	private final String originUrl;

	// permission 0 = view, 1 = edit, 2 = owner

	public WebService(int port){
		LOG.info("Starting WebService on port: " + port + "...");
		URL url = null;
		try{
			url = new URL(Config.WEBSITE_URL);
		}
		catch(MalformedURLException e){
			LOG.error("Invalid redirect Url provided", e);
		}
		originUrl = String.format("%s://%s", url.getProtocol(), url.getHost());

		port(port);
		options("/*", this::cors);
		before(this::corsHeaders);
		before("/*", (request, response) -> response.header("Content-Type", "application/json"));
		post("/login", this::login);
		post("/register", this::register);
		get("/me", this::me);
		path("/warehouses", () -> {
			before("/*", this::checkLogin);
			get("/", this::getWarehouses);
			post("/", this::addWarehouse);
			path("/:warehouseId", () -> {
				before("/*", this::checkWarehousePermissionView);
				path("/", () -> {
					//post("/", this::addWarehouseItem);
					before("/*", this::checkWarehousePermissionOwner);
					//patch("/", this::changeWarehouse);
					//delete("/", this::deleteWarehouse);
				});
				path("/items", () -> {
					path("/", () -> {
						before("/*", this::checkWarehousePermissionEdit);
						post("/", this::addWarehouseItem);
						//patch("/", this::changeWarehouseItem);
						//delete("/", this::deleteWarehouseItem);
					});
					get("/", this::getWarehouseItems);
					//delete("/", this::deleteWarehouseItems);
					path("/:itemId", () -> {
						//get("/", this::getWarehouseItem);
						before("/*", this::checkWarehousePermissionEdit);
						//patch("/", this::setWarehouseItem);
						//delete("/", this::deleteWarehouseItem);
					});
				});
			});
		});
	}

	private String cors(Request request, Response response){
		String accessControlRequestHeaders = request.headers("Access-Control-Request-Headers");
		if(accessControlRequestHeaders != null){
			response.header("Access-Control-Allow-Headers", accessControlRequestHeaders);
		}
		String accessControlRequestMethod = request.headers("Access-Control-Request-Method");
		if(accessControlRequestMethod != null){
			response.header("Access-Control-Allow-Methods", accessControlRequestMethod);
		}
		return "OK";
	}

	private void corsHeaders(Request request, Response response){
		response.header("Access-Control-Allow-Origin", originUrl);
		response.header("Access-Control-Allow-Methods", "GET, POST, PATCH, PUT, DELETE, OPTIONS");
		response.header("Access-Control-Allow-Headers", "Origin, Content-Type, X-Auth-Token");
	}

	private String generateSession(int userId){
		var sessionId = Database.generateUniqueKey();
		Database.addSession(sessionId, userId);
		return sessionId;
	}

	private void checkLogin(Request request, Response response){
		if(!request.requestMethod().equals("OPTIONS")){
			String key = request.headers("Authorization");
			if(key == null || !Database.sessionExists(key)){
				halt(401, error("Please login to continue"));
			}
		}
	}

	private int getUserFromRequest(Request request){
		return Database.getUserFromSession(request.headers("Authorization"));
	}

	private String register(Request request, Response response){
		JsonObject json = JsonParser.parseString(request.body()).getAsJsonObject();
		String username = json.get("username").getAsString();
		String password = json.get("password").getAsString();
		//TODO check username & password for stuff
		String salt = Password.generateSalt(32);
		String hash = Password.hashPassword(password, salt);
		int userId = Database.registerUser(username, hash, salt);
		if(userId == -1){
			response.status(400);
			return error("User could not created");
		}
		return response("session_id", generateSession(userId));
	}

	private String login(Request request, Response response){
		JsonObject json = JsonParser.parseString(request.body()).getAsJsonObject();
		String username = json.get("username").getAsString();
		String password = json.get("password").getAsString();
		var loginInfo = Database.getUserLogin(username);
		if(!Password.checkPassword(password, loginInfo.getC(), loginInfo.getB())){
			response.status(401);
			return error("password or username incorrect");
		}
		return response("session_id", generateSession(loginInfo.getA()));
	}

	private String me(Request request, Response response){
		int userId = getUserFromRequest(request);
		String userName = Database.getUsername(userId);
		var warehouses = Database.getWarehousesFromUserId(userId);
		var warehouseStrings = new HashSet<String>();
		if(warehouses != null){
			for(Warehouse warehouse : warehouses){
				warehouseStrings.add(String.format("{\"id\": %s, \"name\": \"%s\"}", warehouse.getId(), warehouse.getName()));
			}
		}
		return "{\"username\": \"" + userName + "\", \"warehouses\": " + "[" + String.join(", ", warehouseStrings) + "]" + "}";
	}

	private String addWarehouse(Request request, Response response){
		JsonObject json = JsonParser.parseString(request.body()).getAsJsonObject();
		String warehouseName = json.get("warehouse_name").getAsString();
		var userId = getUserFromRequest(request);
		Warehouse warehouse = Database.addWarehouse(warehouseName, userId);
		if(warehouse == null){
			return error("Warehouse could not created");
		}
		return simpleResponse("warehosue", String.format("{\"id:\" %d, \"name:\" \"%s\"}", warehouse.getId(), warehouse.getName()));
	}

	private String getWarehouses(Request request, Response response){
		var userId = getUserFromRequest(request);
		var warehouses = Database.getWarehousesFromUserId(userId);
		var warehouseStrings = new HashSet<String>();
		if(warehouses == null){
			return error("Error white getting warehouses");
		}
		for(Warehouse warehouse : warehouses){
			warehouseStrings.add(String.format("{\"id\": %s, \"name\": \"%s\"}", warehouse.getId(), warehouse.getName()));
		}
		return arrayResponse("warehouses", String.join(", ", warehouseStrings));
	}

	private String getWarehouseItems(Request request, Response response){
		int warehouseId = Integer.parseInt(request.params(":warehouseId"));
		Set<WarehouseItem> items = Database.getWarehouseItems(warehouseId);
		Set<String> itemStrings = new HashSet<>();
		if(items == null){
			return error("Error white getting warehouse items");
		}
		for(WarehouseItem item : items){
			itemStrings.add(String.format("{\"id\": %d, \"warehouse_id\": %d, \"warehouse_name\": \"%s\", \"name\": \"%s\", \"owner_id\": %d, \"owner_name\": \"%s\"," +
				"\"count\": %d, \"description\": \"%s\", \"storage_place\": \"%s\", \"category_id\": %d, \"category_name\": \"%s\", \"condition_id\": %d, \"condition_name\": \"%s\", " +
				"\"image_path\": \"%s\", \"purchase_place\": \"%s\", \"purchase_price\": %d, \"purchase_date\": %d, \"updated_at\": %d, \"created_at\": %d, }", item.getId(),
				item.getWarehouseId(), item.getWarehouseName(), item.getName(), item.getOwnerId(), item.getOwnerName(), item.getCount(), item.getDescription(), item.getStoragePlace(),
				item.getCategoryId(), item.getCategoryName(), item.getConditionId(), item.getConditionName(), item.getImagePath(), item.getPurchasePlace(), item.getPurchasePrice(),
				item.getPurchaseDate(), item.getUpdatedAt(), item.getCreatedAt()
			));
		}
		return arrayResponse("warehosue_items", String.join(", ", itemStrings));
	}

	private String addWarehouseItem(Request request, Response response){
		int warehouseId = Integer.parseInt(request.params(":warehouseId"));
		JsonObject json = JsonParser.parseString(request.body()).getAsJsonObject();
		String name = json.get("item_name").getAsString();
		int count = json.get("item_count").getAsInt();
		String description = json.get("item_description").getAsString();
		String storagePlace = json.get("item_storage_place").getAsString();
		int category = json.get("item_category").getAsInt();
		int condition = json.get("item_condition").getAsInt();
		String imagePath = json.get("item_imagePath").getAsString();
		String purchasePlace = json.get("item_purchase_place").getAsString();
		int purchasePrice = json.get("item_purchase_price").getAsInt();
		long purchaseDate = json.get("item_purchase_date").getAsLong();
		var userId = getUserFromRequest(request);
		int itemId = Database.addWarehouseItem(warehouseId, userId, name, count, description, storagePlace, category, condition, imagePath, purchasePlace, purchasePrice, purchaseDate);
		if(itemId == -1){
			return error("WarehouseItem could not created");
		}
		return simpleResponse("warehosue_item", String.format("{\"id:\" %d, \"name:\" \"%s\"}", itemId, name));
	}

	/* Permission Check */

	private void checkWarehousePermission(Request request, int permission){
		if(!request.requestMethod().equals("OPTIONS") && Database.getUserWarehousePermission(Integer.parseInt(request.params(":warehouseId")), getUserFromRequest(request)) < permission){
			halt(403, error("You don't have the required permission for this warehouse"));
		}
	}

	private void checkWarehousePermissionView(Request request, Response response){
		checkWarehousePermission(request, 0);
	}

	private void checkWarehousePermissionEdit(Request request, Response response){
		checkWarehousePermission(request, 1);
	}

	private void checkWarehousePermissionOwner(Request request, Response response){
		checkWarehousePermission(request, 2);
	}

	/* Result Parsing */

	private String error(String error){
		return "{\"error\": \"" + error + "\"}";
	}

	private String response(String responseKey, String responseValue){
		return "{\"" + responseKey + "\": \"" + responseValue + "\"}";
	}

	private String simpleResponse(String responseKey, String responseValue){
		return "{\"" + responseKey + "\": " + responseValue + "}";
	}

	private String arrayResponse(String responseKey, String responseValue){
		return "{\"" + responseKey + "\": [" + responseValue + "]}";
	}

	private String response(String responseKey, int responseValue){
		return "{\"" + responseKey + "\": " + responseValue + "}";
	}

	private String response(String responseKey, boolean responseValue){
		return "{\"" + responseKey + ": " + responseValue + "}";
	}

}
