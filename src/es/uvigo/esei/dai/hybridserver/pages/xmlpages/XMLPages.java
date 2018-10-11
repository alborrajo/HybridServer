package es.uvigo.esei.dai.hybridserver.pages.xmlpages;

public interface XMLPages {
	
	public String[] getUUIDs();

	public String get(String key);

	public void put(String key, String value);

	public void remove(String key);

}
