package de.anteiku.item.warehouse.objects;

public class Warehouse{

	private final int id;
	private final String name;

	public Warehouse(int id, String name){
		this.id = id;
		this.name = name;
	}

	public int getId(){
		return id;
	}

	public String getName(){
		return name;
	}

}
