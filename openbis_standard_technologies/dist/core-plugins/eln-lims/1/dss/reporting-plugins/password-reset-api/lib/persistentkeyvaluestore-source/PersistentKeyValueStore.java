package ch.ethz.sis;

import java.io.Serializable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;



public class PersistentKeyValueStore {
	private static ConcurrentMap<String, Serializable> keyStore = new ConcurrentHashMap<>();
	
	public static void put(String key, Serializable value) {
		keyStore.put(key, value);
	}
	
	public static Serializable get(String key) {
		return keyStore.get(key);
	}
	
	public static void remove(String key) {
		keyStore.remove(key);
	}
	
	public static boolean containsKey(String key) {
		return keyStore.containsKey(key);
	}
}
