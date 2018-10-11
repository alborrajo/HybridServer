package es.uvigo.esei.dai.hybridserver.pages.xsdpages;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;

import es.uvigo.esei.dai.hybridserver.HybridServerService;
import es.uvigo.esei.dai.hybridserver.ServerConfiguration;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class XSDPagesP2P implements XSDPages {
	
	private XSDPages xsdPages;
	private List<ServerConfiguration> servers;
	
	public XSDPagesP2P(XSDPages xsdPages, List<ServerConfiguration> servers) {
		this.xsdPages = xsdPages;
		this.servers = servers;
	}

	@Override
	public String[] getUUIDs() {
		List<String> pages = new ArrayList<String>();
		pages.addAll(Arrays.asList(xsdPages.getUUIDs()));
		
		// REMOTE SERVERS
		try {for (ServerConfiguration server : servers) {
			// Connect to server
			try {
				URL url = new URL(server.getWsdl());
				QName name = new QName(server.getNamespace(), server.getService());
				Service service = Service.create(url, name);
				HybridServerService pagesService = service.getPort(HybridServerService.class);
				
				pages.addAll(Arrays.asList(pagesService.getXSDs()));
				
			} catch(Exception e) {System.out.println("WARNING: Error connecting to remote server "+server.getName());}
		
		}} catch(Exception e) {}
		
		return pages.toArray(new String[0]);
	}

	@Override
	public String get(String key) {
		String xsd = xsdPages.get(key);
		
		if(xsd != null) {return xsd;}
		
		// If it couldn't be found on local DB
		// TRY ON REMOTE SERVERS
		try {for (ServerConfiguration server : servers) {
			// Connect to server
			try {
				URL url = new URL(server.getWsdl());
				QName name = new QName(server.getNamespace(), server.getService());
				Service service = Service.create(url, name);
				HybridServerService pagesService = service.getPort(HybridServerService.class);
				
				xsd = pagesService.getXSD(key);
				if(xsd != null) {return xsd;}
				
			} catch(Exception e) {
				System.out.println("WARNING: Error connecting to remote server "+server.getName());
			}
		
		}} catch(Exception e) {}
		
		return null; // If it couldn't be found on any server
	}

	@Override
	public void put(String key, String value) {
		xsdPages.put(key, value);
	}

	@Override
	public void remove(String key) {
		xsdPages.remove(key);
	}

}
