package es.uvigo.esei.dai.hybridserver;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.xml.ws.Endpoint;

import es.uvigo.esei.dai.hybridserver.pages.htmlpages.HTMLPagesDB;
import es.uvigo.esei.dai.hybridserver.pages.htmlpages.HTMLPagesP2P;
import es.uvigo.esei.dai.hybridserver.pages.xmlpages.XMLPagesDB;
import es.uvigo.esei.dai.hybridserver.pages.xmlpages.XMLPagesP2P;
import es.uvigo.esei.dai.hybridserver.pages.xsdpages.XSDPagesDB;
import es.uvigo.esei.dai.hybridserver.pages.xsdpages.XSDPagesP2P;
import es.uvigo.esei.dai.hybridserver.pages.xsltpages.XSLTPagesDB;
import es.uvigo.esei.dai.hybridserver.pages.xsltpages.XSLTPagesP2P;
import es.uvigo.esei.dai.hybridserver.resourcecontrollers.HTMLController;
import es.uvigo.esei.dai.hybridserver.resourcecontrollers.XMLController;
import es.uvigo.esei.dai.hybridserver.resourcecontrollers.XSDController;
import es.uvigo.esei.dai.hybridserver.resourcecontrollers.XSLTController;

public class HybridServer {
	private String webServiceURL;
	private int servicePort = 8888;
	private int threadNumber = 50;
	
	private Thread serverThread;
	private ExecutorService threadPool;
	private boolean stop;
	
	private HTMLController htmlController;
	private XMLController xmlController;
	private XSDController xsdController;
	private XSLTController xsltController;
	
	private HybridServerService pagesService;
	private Endpoint pagesServiceEndpoint;

	public HybridServer() {
		// Use default properties
		this((Properties) null);		
	}

	public HybridServer(Properties properties) {
		// Use specified properties
		if(properties == null) { //If it's empty (aka no properties)
			properties = new Properties();
			properties.setProperty("numClients", "50");
			properties.setProperty("port", "8888");
			properties.setProperty("db.url", "jdbc:mysql://localhost:3306/hstestdb");
			properties.setProperty("db.user", "hsdb");
			properties.setProperty("db.password", "hsdbpass");
		}
		
		threadNumber = Integer.parseInt(properties.getProperty("numClients"));
		servicePort = Integer.parseInt(properties.getProperty("port"));
		
		HTMLPagesDB htmlPages = new HTMLPagesDB(properties);
		XMLPagesDB xmlPages = new XMLPagesDB(properties);
		XSDPagesDB xsdPages = new XSDPagesDB(properties);
		XSLTPagesDB xsltPages = new XSLTPagesDB(properties);
				
		htmlController = new HTMLController(htmlPages);
		xmlController = new XMLController(xmlPages, xsdPages, xsltPages);
		xsdController = new XSDController(xsdPages);
		xsltController = new XSLTController(xsltPages, xsdPages);
	}

	public HybridServer(Configuration config) {
		// Loading config
		threadNumber = config.getNumClients();
		servicePort = config.getHttpPort();
		webServiceURL = config.getWebServiceURL();
		
		//	Objects to connect with the DB
		HTMLPagesDB htmlPages = new HTMLPagesDB(config);
		XMLPagesDB xmlPages = new XMLPagesDB(config);
		XSDPagesDB xsdPages = new XSDPagesDB(config);
		XSLTPagesDB xsltPages = new XSLTPagesDB(config);
		
		// Use *PagesDB for the service
		pagesService = new HybridServerServiceImpl(htmlPages, xmlPages, xsdPages, xsltPages);
				
		//	Controllers
		// Use *PagesP2P for the controllers
		
		HTMLPagesP2P htmlPagesP2P = new HTMLPagesP2P(htmlPages, config.getServers());
		XMLPagesP2P xmlPagesP2P = new XMLPagesP2P(xmlPages, config.getServers());
		XSDPagesP2P xsdPagesP2P = new XSDPagesP2P(xsdPages, config.getServers());
		XSLTPagesP2P xsltPagesP2P = new XSLTPagesP2P(xsltPages, config.getServers());
		
		htmlController = new HTMLController(htmlPagesP2P);
		xmlController = new XMLController(xmlPagesP2P, xsdPagesP2P, xsltPagesP2P);
		xsdController = new XSDController(xsdPagesP2P);
		xsltController = new XSLTController(xsltPagesP2P, xsdPagesP2P);
		
		// This is in order to avoid recursively calls from one server to the other.
		// For example, if server A doesn't find in its database certain HTML, it asks server B for it
		// If we used PagesP2P on the web service, if server B were unable to find that HTML, it would ask server A again for it
		// Resulting in an endless loop, crashing both servers and probably the network too
		
	}


	public int getPort() {
		return servicePort;
	}
	
	public void start() {
		
		// Web service creation
		try {
			pagesServiceEndpoint = Endpoint.publish(webServiceURL, pagesService);
		} catch(Exception e) {
			System.out.println("WARNING: Unable to publish web service");
			e.printStackTrace();
		}
		
		this.serverThread = new Thread() {
			@Override
			public void run() {
				try (final ServerSocket serverSocket = new ServerSocket(servicePort)) {
					threadPool = Executors.newFixedThreadPool(threadNumber);
					while(true) {
						Socket socket = serverSocket.accept();	
						
						if (stop) break;
						
						threadPool.execute(new HybridServerServiceThread(socket, htmlController, xmlController, xsdController, xsltController));
					}
				} catch (IOException e) {
					System.out.println("ERROR");
					e.printStackTrace();
				}
			}
		};

		this.stop = false;
		this.serverThread.start();
	}
	
	public void stop() {
		this.stop = true;
		
		// Stop web service
		try {pagesServiceEndpoint.stop();}
		catch(NullPointerException e) {} //Whatever
		
		// Wake server socket so it can be shut down
		try (Socket socket = new Socket("localhost", servicePort)) {
		} catch (IOException e) {}
		
		threadPool.shutdownNow();
		 
		try {
		  threadPool.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);
		} catch (InterruptedException e) {
		  e.printStackTrace();
		}
	}
}