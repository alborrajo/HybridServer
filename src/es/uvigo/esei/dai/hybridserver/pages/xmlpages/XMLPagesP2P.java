package es.uvigo.esei.dai.hybridserver.pages.xmlpages;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;

import es.uvigo.esei.dai.hybridserver.HybridServerService;
import es.uvigo.esei.dai.hybridserver.ServerConfiguration;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class XMLPagesP2P implements XMLPages {

	private XMLPages xmlPages;
	private List<ServerConfiguration> servers;
	
	public XMLPagesP2P(XMLPages xmlPages, List<ServerConfiguration> servers) {
		this.xmlPages = xmlPages;
		this.servers = servers;
	}
	
	@Override
	public String[] getUUIDs() {
		List<String> pages = new ArrayList<String>();
		pages.addAll(Arrays.asList(xmlPages.getUUIDs()));
		
		// REMOTE SERVERS
		try {for (ServerConfiguration server : servers) {
			// Connect to server
			try {
				URL url = new URL(server.getWsdl());
				QName name = new QName(server.getNamespace(), server.getService());
				Service service = Service.create(url, name);
				HybridServerService pagesService = service.getPort(HybridServerService.class);
				
				pages.addAll(Arrays.asList(pagesService.getXMLs()));
				
			} catch(Exception e) {System.out.println("WARNING: Error connecting to remote server "+server.getName());}
		
		}} catch(Exception e) {}
		
		return pages.toArray(new String[0]);

	}

	@Override
	public String get(String key) {
		String xml = xmlPages.get(key);
		
		if(xml != null) {return xml;}
		
		// If it couldn't be found on local DB
		// TRY ON REMOTE SERVERS
		try {for (ServerConfiguration server : servers) {
			// Connect to server
			try {
				URL url = new URL(server.getWsdl());
				QName name = new QName(server.getNamespace(), server.getService());
				Service service = Service.create(url, name);
				HybridServerService pagesService = service.getPort(HybridServerService.class);
				
				xml = pagesService.getXML(key);
				if(xml != null) {return xml;}
				
			} catch(Exception e) {
				System.out.println("WARNING: Error connecting to remote server "+server.getName());
			}
		
		}} catch(Exception e) {}
		
		return null; // If it couldn't be found on any server		
	}

	@Override
	public void put(String key, String value) {
		xmlPages.put(key, value);
	}

	@Override
	public void remove(String key) {
		xmlPages.remove(key);
	}

}
