/**
 *  HybridServer
 *  Copyright (C) 2017 Miguel Reboiro-Jato
 *  
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *  
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package es.uvigo.esei.dai.hybridserver;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.sun.org.apache.xml.internal.utils.DefaultErrorHandler;

public class XMLConfigurationLoader {
	public Configuration load(File xmlFile) throws Exception {
		// Construcción del schema
		SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
		Schema schema = schemaFactory.newSchema(new File("configuration.xsd"));
		
		// Construcción del parser del documento. Se establece el esquema y se
		// activa la validación y comprobación de namespaces
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setValidating(false);
		factory.setNamespaceAware(true);
		factory.setSchema(schema);
		
		// Se añade el manejador de errores
		DocumentBuilder builder = factory.newDocumentBuilder();
		builder.setErrorHandler(new DefaultErrorHandler());
		Document configurationDocument = builder.parse(xmlFile);
		
		// --------------------------------
						
		// Crear lista de servers
		List<ServerConfiguration> servers = new ArrayList<ServerConfiguration>();
		
		// Por cada nodo SERVER dentro del nodo SERVERS
		NodeList configurationDocumentServersNode = configurationDocument.getElementsByTagName("servers").item(0).getChildNodes();
		for(int i=0; i < configurationDocumentServersNode.getLength(); i++) {
			Node serverNode = configurationDocumentServersNode.item(i);
			if(serverNode.getNodeType() == Node.ELEMENT_NODE) {
				Element serverElement = (Element) serverNode;
				
				servers.add(new ServerConfiguration(
					serverElement.getAttribute("name"),
					serverElement.getAttribute("wsdl"),
					serverElement.getAttribute("namespace"),
					serverElement.getAttribute("service"),
					serverElement.getAttribute("httpAddress")
				));
			}
		}
		
		// Convertir Document a Configuration
		// TODO: Que no sea tan feo
		return new Configuration(
			Integer.parseInt(((Element) configurationDocument.getElementsByTagName("http").item(0)).getFirstChild().getNodeValue()),
			Integer.parseInt(((Element) configurationDocument.getElementsByTagName("numClients").item(0)).getFirstChild().getNodeValue()),
			((Element) configurationDocument.getElementsByTagName("webservice").item(0)).getFirstChild().getNodeValue(),
			
			((Element) configurationDocument.getElementsByTagName("user").item(0)).getFirstChild().getNodeValue(),
			((Element) configurationDocument.getElementsByTagName("password").item(0)).getFirstChild().getNodeValue(),
			((Element) configurationDocument.getElementsByTagName("url").item(0)).getFirstChild().getNodeValue(),
			
			servers
			);
			
	}
}
