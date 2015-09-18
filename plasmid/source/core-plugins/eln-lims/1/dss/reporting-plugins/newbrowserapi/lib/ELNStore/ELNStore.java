package ch.ethz.ssdm.eln;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;



public class ELNStore {
	private static ConcurrentMap keyStore = new ConcurrentHashMap();
	
	public static void put(Object key, Object value) {
		keyStore.put(key, value);
	}
	
	public static Object get(Object key) {
		return keyStore.get(key);
	}
}
