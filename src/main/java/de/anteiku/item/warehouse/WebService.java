package de.anteiku.item.warehouse;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import de.anteiku.item.warehouse.database.Database;
import de.anteiku.item.warehouse.utils.Config;
import de.anteiku.item.warehouse.utils.Password;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Request;
import spark.Response;

import java.net.MalformedURLException;
import java.net.URL;

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
		post("/login", this::login);
		post("/register", this::register);
		path("/user", () -> {
			before("/*", this::checkLogin);
			get("/me", this::getUserInfo);
		});
		path("/warehouses", () -> {
			before("/*", (request, response) -> response.header("Content-Type", "application/json"));
			before("/*", this::checkLogin);
			path("/:warehouseId", () -> {
				before("/*", this::checkWarehousePermissionView);
				path("/items", () -> {
					get("/get", () -> this::getWarehouseItems);
					path("/:itemId", () -> {
						get("/get", () -> this::getWarehouseItem);
						before("/*", this::checkWarehousePermissionEdit);
						post("/set", () -> this::setWarehouseItem);
						delete("/delete", () -> this::deleteWarehouseItem);
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

	private String generateSession(int userId){
		var sessionId = Database.generateUniqueKey();
		Database.addSession(userId, sessionId);
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

	private void checkWarehousePermission(Request request, Response response, int permission){
		if(!request.requestMethod().equals("OPTIONS")){
			String key = request.headers("Authorization");
			if(Database.getUserWarehousePermission(Integer.parseInt(request.params(":warehouseId")), getUserFromRequest(request)) < 0){
				halt(403, error("You have no permission to view this warehouse"));
			}
		}
	}

	private int getUserFromRequest(Request request){
		return Database.getUserFromSession(request.headers("Authorization"));
	}

	private void checkWarehousePermissionView(Request request, Response response){
		checkWarehousePermission(request, response, 0);
	}

	private void checkWarehousePermissionEdit(Request request, Response response){
		checkWarehousePermission(request, response, 1);
	}

	private String getUserInfo(Request request, Response response){
		var userId = getUserFromRequest(request);
		Database.getWarehouses(userId);
		return "";
	}

	private String error(String error){
		return "{\"error\": \"" + error + "\"}";
	}

	private String response(String responseKey, String responseValue){
		return "{\"" + responseKey + "\": \"" + responseValue + "\"}";
	}

	private String response(String responseKey, int responseValue){
		return "{\"" + responseKey + "\": " + responseValue + "}";
	}

	private String response(String responseKey, boolean responseValue){
		return "{\"" + responseKey + ": " + responseValue + "}";
	}

}
