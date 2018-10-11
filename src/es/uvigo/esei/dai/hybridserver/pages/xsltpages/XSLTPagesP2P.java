package es.uvigo.esei.dai.hybridserver.pages.xsltpages;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;

import es.uvigo.esei.dai.hybridserver.HybridServerService;
import es.uvigo.esei.dai.hybridserver.ServerConfiguration;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class XSLTPagesP2P implements XSLTPages {
	
	private XSLTPages xsltPages;
	private List<ServerConfiguration> servers;
	
	public XSLTPagesP2P(XSLTPages xsltPages, List<ServerConfiguration> servers) {
		this.xsltPages = xsltPages;
		this.servers = servers;
	}

	@Override
	public String[] getUUIDs() {
		List<String> pages = new ArrayList<String>();
		pages.addAll(Arrays.asList(xsltPages.getUUIDs()));
		
		// REMOTE SERVERS
		try {for (ServerConfiguration server : servers) {
			// Connect to server
			try {
				URL url = new URL(server.getWsdl());
				QName name = new QName(server.getNamespace(), server.getService());
				Service service = Service.create(url, name);
				HybridServerService pagesService = service.getPort(HybridServerService.class);
				
				pages.addAll(Arrays.asList(pagesService.getXSLTs()));
				
			} catch(Exception e) {System.out.println("WARNING: Error connecting to remote server "+server.getName());}
		
		}} catch(Exception e) {}
		
		System.out.println("HUAH "+pages.size());
		
		return pages.toArray(new String[0]);
	}

	@Override
	public String get(String key) {
		String xslt = xsltPages.get(key);
		
		if(xslt != null) {return xslt;}
		
		// If it couldn't be found on local DB
		// TRY ON REMOTE SERVERS
		try {for (ServerConfiguration server : servers) {
			// Connect to server
			try {
				URL url = new URL(server.getWsdl());
				QName name = new QName(server.getNamespace(), server.getService());
				Service service = Service.create(url, name);
				HybridServerService pagesService = service.getPort(HybridServerService.class);
				
				xslt = pagesService.getXSLT(key);
				if(xslt != null) {return xslt;}
				
			} catch(Exception e) {
				System.out.println("WARNING: Error connecting to remote server "+server.getName());
			}
		
		}} catch(Exception e) {}
		
		return null; // If it couldn't be found on any server
	}

	@Override
	public void put(String key, String value, String xsd) {
		xsltPages.put(key, value, xsd);		
	}

	@Override
	public void remove(String key) {
		xsltPages.remove(key);
	}

	@Override
	public String getXSDUUID(String key) {
		String xslt = xsltPages.getXSDUUID(key);
		
		if(xslt != null) {return xslt;}
		
		// If it couldn't be found on local DB
		// TRY ON REMOTE SERVERS
		try {for (ServerConfiguration server : servers) {
			// Connect to server
			try {
				URL url = new URL(server.getWsdl());
				QName name = new QName(server.getNamespace(), server.getService());
				Service service = Service.create(url, name);
				HybridServerService pagesService = service.getPort(HybridServerService.class);
				
				xslt = pagesService.getXSDofXSLT(key);
				if(xslt != null) {return xslt;}
				
			} catch(Exception e) {
				System.out.println("WARNING: Error connecting to remote server "+server.getName());
			}
		
		}} catch(Exception e) {}
		
		return null; // If it couldn't be found on any server
	}

}
