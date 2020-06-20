package de.anteiku.item.warehouse;

public class Pair<T, K>{

	private T t;
	private K k;

	public Pair(T t, K k){
		this.t = t;
		this.k = k;
	}

	public T getT(){
		return t;
	}

	public K getK(){
		return k;
	}
}
