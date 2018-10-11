package es.uvigo.esei.dai.hybridserver.pages.xsdpages;


public interface XSDPages {
	
	public String[] getUUIDs();

	public String get(String key);

	public void put(String key, String value);

	public void remove(String key);

}
