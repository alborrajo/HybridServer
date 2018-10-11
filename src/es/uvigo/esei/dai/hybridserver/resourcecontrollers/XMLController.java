package es.uvigo.esei.dai.hybridserver.resourcecontrollers;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.UUID;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.sun.org.apache.xml.internal.utils.DefaultErrorHandler;

import es.uvigo.esei.dai.hybridserver.http.HTTPRequest;
import es.uvigo.esei.dai.hybridserver.http.HTTPResponse;
import es.uvigo.esei.dai.hybridserver.http.HTTPResponseStatus;
import es.uvigo.esei.dai.hybridserver.http.MIME;
import es.uvigo.esei.dai.hybridserver.pages.xmlpages.XMLPages;
import es.uvigo.esei.dai.hybridserver.pages.xsdpages.XSDPages;
import es.uvigo.esei.dai.hybridserver.pages.xsltpages.XSLTPages;

public class XMLController {

	private XMLPages pages;
	private XSDPages xsdPages;
	private XSLTPages xsltPages;
	
	public XMLController(XMLPages pages, XSDPages xsdPages, XSLTPages xsltPages) {
		this.pages = pages;
		this.xsdPages = xsdPages;
		this.xsltPages = xsltPages;
	}

	public HTTPResponse respondTo(HTTPRequest request) throws Exception {
		HTTPResponse response = new HTTPResponse();

		switch (request.getMethod()) {
		// If it's GET:
		case GET:
			// Set HTTP Response variables
			response.setVersion("HTTP/1.1");
			response.setStatus(HTTPResponseStatus.S200); // 200 OK
			response.putParameter("Content-Type", MIME.APPLICATION_XML.getMime());

			String content = ""; // content to display
			StringBuilder contentBuilder = new StringBuilder();

			if (request.getResourceParameters().containsKey("uuid")) {
				// If there's UUID on the parameters, get resource with said UUID
				content = pages.get(request.getResourceParameters().get("uuid"));

				if (content == null) {
					// Discard built request return a 404 one
					response = new HTTPResponse();
					response.setVersion("HTTP/1.1");
					response.setStatus(HTTPResponseStatus.S404); // 404 Not Found
					break;
				}
				
				// If there's XSLT on the parameters
				if(request.getResourceParameters().containsKey("xslt")) {
					
					// Obtain XML, XSD and XSLT
					String xml = content;
					
					String xslt = xsltPages.get(request.getResourceParameters().get("xslt"));
					
					String xsdUUID = xsltPages.getXSDUUID(request.getResourceParameters().get("xslt"));
					String xsd = xsdPages.get(xsdUUID);
					
					// Check if either the XSLT or the XSD don't exist
					if(xslt == null || xsd == null) {
						// Discard built request return a 404 one
						response = new HTTPResponse();
						response.setVersion("HTTP/1.1");
						response.setStatus(HTTPResponseStatus.S404); // 404 Not Found
						break;
					}
					
					
					// Validate XML with XSD 
					try {
						SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
			            Schema schema = schemaFactory.newSchema(new StreamSource(new StringReader(xsd)));
			            						
						DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
						factory.setValidating(false);
						factory.setNamespaceAware(true);
						factory.setSchema(schema);
						
						DocumentBuilder builder = factory.newDocumentBuilder();
						builder.setErrorHandler(new DefaultErrorHandler());
						
						builder.parse(new InputSource(new StringReader(xml)));
					} catch(SAXException e) {
			        	// INVALID: Discard built request return a 400 one
						response = new HTTPResponse();
						response.setVersion("HTTP/1.1");
						response.setStatus(HTTPResponseStatus.S400); // 400 Bad Request
						e.printStackTrace();
						break;
			        }
					
					
					// Transform XML withXSLT
					TransformerFactory tFactory = TransformerFactory.newInstance();
					Transformer transformer = tFactory.newTransformer(new StreamSource(new StringReader(xslt)));
					
					StringWriter writer = new StringWriter(); // Aquí se guardará el resultado de la transformación
					
					transformer.transform(
							new StreamSource(new StringReader(xml)),
							new StreamResult(writer)
							);
					
					// Send XML -> HTML response
					response.putParameter("Content-Type", MIME.TEXT_HTML.getMime()); // MIME: HTML
					content = writer.toString(); // Set transformed XML as content

				}
				
			} else {
				// Else, build an html list with links to every UUID'd page
				response.putParameter("Content-Type", MIME.TEXT_HTML.getMime());
				
				contentBuilder = new StringBuilder();

				contentBuilder.append("<html><body>");
				contentBuilder.append("<h1>XML</h1>");
				contentBuilder.append("<ul>");
				for (String pageEntry : pages.getUUIDs()) {
					contentBuilder.append("<li><a href=\"/xml?uuid=" + pageEntry + "\">" + pageEntry + "</a></li>");
				}
				contentBuilder.append("</ul>");
				contentBuilder.append("</body></html>");

				content = contentBuilder.toString();
			}

			response.setContent(content); // Display content
			break;

		// If it's POST
		case POST:
			String postContent = request.getResourceParameters().get("xml");

			if (postContent == null) { // If there is no xml content
				// Set HTTP response variables
				response.setVersion("HTTP/1.1");
				response.setStatus(HTTPResponseStatus.S400); // 400 Bad Request
				break; // Exit
			}

			// Else
			String newUUID = UUID.randomUUID().toString(); // Generate random UUID
			pages.put(newUUID, postContent); // Store POSTed page

			// Set HTTP response variables
			response.setVersion("HTTP/1.1");
			response.setStatus(HTTPResponseStatus.S200); // 200 OK
			response.putParameter("Content-Type", MIME.TEXT_HTML.getMime());
			String linkToNewPage = "<a href=\"xml?uuid=" + newUUID + "\">" + newUUID + "</a>";
			response.setContent(linkToNewPage);
			break;

		case DELETE:
			pages.remove(request.getResourceParameters().get("uuid")); // DELETE page with specified UUID

			// Set HTTP response variables
			response.setVersion("HTTP/1.1");
			response.setStatus(HTTPResponseStatus.S200); // 200 OK
			break;
		

		// If it's anything else
		default:
			// TODO: Delete this when there are no more cases left to be made
			// Discard built request return a 501 one
			response = new HTTPResponse();
			response.setVersion("HTTP/1.1");
			response.setStatus(HTTPResponseStatus.S501); // 501 Not Implemented
			break;
		}

		return response;
	}

}
