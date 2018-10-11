package es.uvigo.esei.dai.hybridserver.pages.xsltpages;

public interface XSLTPages {
	
	public String[] getUUIDs();
	
	public String get(String key);
	
	public String getXSDUUID(String key);

	public void put(String key, String value, String xsd);

	public void remove(String key);

}
