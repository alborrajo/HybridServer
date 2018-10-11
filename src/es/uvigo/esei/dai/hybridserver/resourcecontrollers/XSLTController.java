package es.uvigo.esei.dai.hybridserver.resourcecontrollers;

import java.util.UUID;

import es.uvigo.esei.dai.hybridserver.http.HTTPRequest;
import es.uvigo.esei.dai.hybridserver.http.HTTPResponse;
import es.uvigo.esei.dai.hybridserver.http.HTTPResponseStatus;
import es.uvigo.esei.dai.hybridserver.http.MIME;
import es.uvigo.esei.dai.hybridserver.pages.xsdpages.XSDPages;
import es.uvigo.esei.dai.hybridserver.pages.xsltpages.XSLTPages;

public class XSLTController {

	private XSLTPages pages;
	private XSDPages xsdPages;
	
	public XSLTController(XSLTPages pages, XSDPages xsdPages) {
		this.pages = pages;
		this.xsdPages = xsdPages;
	}

	public HTTPResponse respondTo(HTTPRequest request) {
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
			} else {
				// Else, build a list with links to every UUID'd page
				response.putParameter("Content-Type", MIME.TEXT_HTML.getMime());
				contentBuilder = new StringBuilder();

				contentBuilder.append("<html><body>");
				contentBuilder.append("<h1>XSLT</h1>");
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
			String postContent = request.getResourceParameters().get("xslt");
			String associatedXSDuuid = request.getResourceParameters().get("xsd");
			
			if (postContent == null || associatedXSDuuid == null) { // If there is no xslt content or xsd uuid
				// Set HTTP response variables
				response.setVersion("HTTP/1.1");
				response.setStatus(HTTPResponseStatus.S400); // 400 Bad Request
				break; // Exit
			}
			
			
			String associatedXSD = xsdPages.get(associatedXSDuuid);
			
			if(associatedXSD == null) { // If the specified associated XSD doesn't exist
				// Set HTTP response variables
				response.setVersion("HTTP/1.1");
				response.setStatus(HTTPResponseStatus.S404); // 404 Not Found
				break; // Exit
			}

			
			// Else
			String newUUID = UUID.randomUUID().toString(); // Generate random UUID
			pages.put(newUUID, postContent, associatedXSDuuid); // Store POSTed page

			// Set HTTP response variables
			response.setVersion("HTTP/1.1");
			response.setStatus(HTTPResponseStatus.S200); // 200 OK
			response.putParameter("Content-Type", MIME.TEXT_HTML.getMime());
			String linkToNewPage = "<a href=\"xslt?uuid=" + newUUID + "\">" + newUUID + "</a>";
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
