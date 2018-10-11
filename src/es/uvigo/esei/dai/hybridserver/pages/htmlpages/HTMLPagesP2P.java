package es.uvigo.esei.dai.hybridserver.pages.htmlpages;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;

import es.uvigo.esei.dai.hybridserver.HybridServerService;
import es.uvigo.esei.dai.hybridserver.ServerConfiguration;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class HTMLPagesP2P implements HTMLPages {

	private HTMLPages htmlPages;
	private List<ServerConfiguration> servers;
	
	public HTMLPagesP2P(HTMLPages htmlPages, List<ServerConfiguration> servers) {
		this.htmlPages = htmlPages;
		this.servers = servers;
	}
	
	@Override
	public String[] getUUIDs() {
		
		List<String> toReturnList = new ArrayList<String>();
		toReturnList.addAll(Arrays.asList(htmlPages.getUUIDs()));
		
		// REMOTE SERVERS
		try {for (ServerConfiguration server : servers) {
			// Connect to server
			try {
				URL url = new URL(server.getWsdl());
				QName name = new QName(server.getNamespace(), server.getService());
				Service service = Service.create(url, name);
				HybridServerService pagesService = service.getPort(HybridServerService.class);
				
				toReturnList.addAll(Arrays.asList(pagesService.getHTMLs()));
				
			} catch(Exception e) {System.out.println("WARNING: Error connecting to remote server "+server.getName());}
		
		}} catch(Exception e) {}
		
		return toReturnList.toArray(new String[0]);

	}

	@Override
	public String get(String key) {
		String html = htmlPages.get(key);
		
		if(html != null) {return html;}
		
		// If it couldn't be found on local DB
		// TRY ON REMOTE SERVERS
		try {for (ServerConfiguration server : servers) {
			// Connect to server
			try {
				URL url = new URL(server.getWsdl());
				QName name = new QName(server.getNamespace(), server.getService());
				Service service = Service.create(url, name);
				HybridServerService pagesService = service.getPort(HybridServerService.class);
				
				html = pagesService.getHTML(key);
				if(html != null) {return html;}
				
			} catch(Exception e) {
				System.out.println("WARNING: Error connecting to remote server "+server.getName());
			}
		
		}} catch(Exception e) {}
		
		return null; // If it couldn't be found on any server		
	}

	@Override
	public void put(String key, String value) {
		htmlPages.put(key, value);
	}

	@Override
	public void remove(String key) {
		htmlPages.remove(key);
	}

}
