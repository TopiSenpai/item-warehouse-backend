package de.anteiku.item.warehouse.objects;

public class WarehouseItem{

	private final int id;
	private final int warehouseId;
	private final String warehouseName;
	private final String name;
	private final int ownerId;
	private final String ownerName;
	private final int count;
	private final String description;
	private final String storagePlace;
	private final int categoryId;
	private final String categoryName;
	private final int conditionId;
	private final String conditionName;
	private final String imagePath;
	private final String purchasePlace;
	private final int purchasePrice;
	private final long purchaseDate;
	private final long updatedAt;
	private final long createdAt;

	public WarehouseItem(final int id, final int warehouseId, final String warehouseName, final String name, final int ownerId, final String ownerName, final int count, final String description, final String storagePlace, final int categoryId, final String categoryName, final int conditionId, final String conditionName, final String imagePath, final String purchasePlace, final int purchasePrice, final long purchaseDate, final long updatedAt, final long createdAt){
		this.id = id;
		this.warehouseId = warehouseId;
		this.warehouseName = warehouseName;
		this.name = name;
		this.ownerId = ownerId;
		this.ownerName = ownerName;
		this.count = count;
		this.description = description;
		this.storagePlace = storagePlace;
		this.categoryId = categoryId;
		this.categoryName = categoryName;
		this.conditionId = conditionId;
		this.conditionName = conditionName;
		this.imagePath = imagePath;
		this.purchasePlace = purchasePlace;
		this.purchasePrice = purchasePrice;
		this.purchaseDate = purchaseDate;
		this.updatedAt = updatedAt;
		this.createdAt = createdAt;
	}

	public int getId(){
		return id;
	}

	public int getWarehouseId(){
		return warehouseId;
	}

	public String getWarehouseName(){
		return warehouseName;
	}

	public String getName(){
		return name;
	}

	public int getOwnerId(){
		return ownerId;
	}

	public String getOwnerName(){
		return ownerName;
	}

	public int getCount(){
		return count;
	}

	public String getDescription(){
		return description;
	}

	public String getStoragePlace(){
		return storagePlace;
	}

	public int getCategoryId(){
		return categoryId;
	}

	public String getCategoryName(){
		return categoryName;
	}

	public int getConditionId(){
		return conditionId;
	}

	public String getConditionName(){
		return conditionName;
	}

	public String getImagePath(){
		return imagePath;
	}

	public String getPurchasePlace(){
		return purchasePlace;
	}

	public int getPurchasePrice(){
		return purchasePrice;
	}

	public long getPurchaseDate(){
		return purchaseDate;
	}

	public long getUpdatedAt(){
		return updatedAt;
	}

	public long getCreatedAt(){
		return createdAt;
	}

}
