package es.uvigo.esei.dai.hybridserver;

import java.io.File;
//import java.util.LinkedHashMap; //Deprecated
import java.util.Properties;

public class Launcher {
	public static void main(String[] args) {
		
		Configuration config = new Configuration();
		
		switch (args.length) {
			case 0:
				// No arguments, do nothing
				break;
	
			case 1:
				// 1 argument: Read XML from file in arguments
				try {
					config = new XMLConfigurationLoader().load(new File(args[0]));
				} catch (Exception e) {
					e.printStackTrace();
					System.out.println("Error while loading the configuration file " + args[0]);
					System.exit(1);
				}
				break;
	
			default:
				// More than one argument, throw error and exit
				System.out.println("Usage:\n\tjava Launcher [configuration file]");
				System.exit(1);
		}

		// DEPRECATED
		// TODO: Delete
		Properties properties = null; // Lo dejo para que no pete
		/* 
		 * switch(args.length) { case 0: //No arguments, do nothing break;
		 * 
		 * case 1: //1 argument: Read Properties from file in arguments try { properties
		 * = new Properties(); properties.load(new FileInputStream(args[0])); } catch
		 * (IOException e) {
		 * System.out.println("Error while loading Properties file "+args[0]);
		 * System.exit(1); } break;
		 * 
		 * default: //More than one argument, throw error and exit
		 * System.out.println("Usage:\n\tjava Launcher [configuration file]");
		 * System.exit(1); }
		 */

		// Deprecated. TODO: Delete
		/*
		 * LinkedHashMap<String,String> pages = new LinkedHashMap<String,String>();
		 * pages.put("uuid1","<html><body><h1>Hybrid Server</h1></body></html>");
		 * pages.put(
		 * "uuid2","<html><body><h1>Hybrid Server 2: Revengeance</h1></body></html>");
		 * pages.put(
		 * "uuid3","<html><body><h1>Hybrid Server Returns</h1><h2>Hybrid Reservation</h2></body></html>"
		 * ); pages.put("uuid4",
		 * "<html><body><h1>DES</h1><h2>PA</h2><h3>CITO</h3></body></html>");
		 * HybridServer server = new HybridServer(pages);
		 */

		HybridServer server;
		
		if (config != null) {
			server = new HybridServer(config);
		}
		else if (properties != null) {
			server = new HybridServer(properties);
		} else {
			server = new HybridServer();
		}

		server.start();
	}
}
