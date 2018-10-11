package es.uvigo.esei.dai.hybridserver;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;

import es.uvigo.esei.dai.hybridserver.http.HTTPRequest;
import es.uvigo.esei.dai.hybridserver.http.HTTPResponse;
import es.uvigo.esei.dai.hybridserver.http.HTTPResponseStatus;
import es.uvigo.esei.dai.hybridserver.http.MIME;
import es.uvigo.esei.dai.hybridserver.resourcecontrollers.HTMLController;
import es.uvigo.esei.dai.hybridserver.resourcecontrollers.XMLController;
import es.uvigo.esei.dai.hybridserver.resourcecontrollers.XSDController;
import es.uvigo.esei.dai.hybridserver.resourcecontrollers.XSLTController;

//SERVICE THREAD
class HybridServerServiceThread implements Runnable {
	
	private Socket socket;
	
	private HTMLController htmlController;
	private XMLController xmlController;
	private XSDController xsdController;
	private XSLTController xsltController;
	
	public HybridServerServiceThread(Socket socket, HTMLController htmlController, XMLController xmlController, XSDController xsdController, XSLTController xsltController) {
		this.socket = socket;
		
		this.htmlController = htmlController;
		this.xmlController = xmlController;
		this.xsdController = xsdController;
		this.xsltController = xsltController;
	}
	
	public void run() {
		try {			
			//Parsear peticion cliente
			System.out.println("PETICIÓN CLIENTE:");
			HTTPRequest request = new HTTPRequest(new InputStreamReader(socket.getInputStream()));
			System.out.println(request.toString()+"\n\n"); //TODO: Quitar esto
			
			
			// Responder al cliente
			System.out.println("\nRESPUESTA SERVIDOR:");

			//Build an HTTPResponse
			HTTPResponse response = new HTTPResponse(); //TODO: Quitar el new HTTPResponse() si averiguo como hacer que Java no se queje por variable no inicializada
			
			//Act depending on the request resource
			switch(request.getResourceName()) {
				case "":
					StringBuilder contentBuilder = new StringBuilder();

					contentBuilder.append("<html>\n");
					contentBuilder.append("<head><meta charset=\"UTF-8\"/></head>\n");
					contentBuilder.append("<body>\n");
					contentBuilder.append("<img alt=\"Logo por @paulaliebheart\" src=\"data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAEAAAABACAYAAACqaXHeAAAB7ElEQVR42u1bUbKDIAwMHa8YDpkcMu+n9KkVjYI0SPixOuiQdXcTkAIM3oKijzw51klzp5AAdxgxM7+PMdvnNboEHIDRAZiu3BRjMBsQkTgDmgIgItlj+v04CSySawibwYcQnusBWwzIXZ8DQbO+cQUQXWBLrADyqwYDctd7YMHrTgYM4wFrapd6QGzIHE+Dt0y7OskAzgAHwAFwAKbaD6QKuV/7DBOVoEvAAXAPUOtyT9taPVPl+YVLwAFwABwAzwIWGv1o9cgl4AC4B9xb/Wn7xR8tox0xQB70suUsA7LBn/0C260EhASCsU/hCPAZEyIBIS7GiEiAiHsvNRwBICl4huc3lQlaYgGRAJEAMwNzhMBLSTLz0QaO1DkAfG8dE3k/LL19y7tBCltQMaCG4VkGcfhCKKzp35vxFbJrUX7JiASY1nnfAgNSri/xn7RN9v88AiItzk/NBfCmdJgGtVW8tDBPNQA8G2zLnH+VQXGDAcWzwZ0S01TjjbEyFzLgFxIo8ZDI3L8EWjDAV4Se6AG3SKDDKq9vAGQnBWqKJL4jDbY0sT16X5GrOg3iDGVE+pSQp/SPy2jwHRIDmpOHaQ/QVok5MLZq/zYA8PrUbvbwOiD38hLFEG0uE2g8YO8fo86Ank2w5pxk+CWxP3oV0Kh1Bk+0AAAAAElFTkSuQmCC\"/>\n");
					contentBuilder.append("<h1>Hybrid Server</h1>\n");
					contentBuilder.append("<h2>Port "+socket.getLocalPort()+"</h2>\n");
					contentBuilder.append("<ul>\n");
					contentBuilder.append("<li><a href='/html'>html</a></li>\n");
					contentBuilder.append("<li><a href='/xml'>xml</a></li>\n");
					contentBuilder.append("<li><a href='/xsd'>xsd</a></li>\n");
					contentBuilder.append("<li><a href='/xslt'>xslt</a></li>\n");
					contentBuilder.append("</ul>\n");
					contentBuilder.append("</body>\n");
					contentBuilder.append("</html>\n");

					String content = contentBuilder.toString();

					response = new HTTPResponse();
					
					// Set HTTP Response variables
					response.setVersion("HTTP/1.1");
					response.setStatus(HTTPResponseStatus.S200); // 200 OK
					response.putParameter("Content-Type", MIME.TEXT_HTML.getMime());
					
					// Set content
					response.setContent(content);
					break;
					
				case "html":
					response = htmlController.respondTo(request);
					break;
					
				case "xml":
					response = xmlController.respondTo(request);
					break;
					
				case "xsd":
					response = xsdController.respondTo(request);
					break;
					
				case "xslt":
					response = xsltController.respondTo(request);
					break;
					
				default:
					// Discard built request return a 400 one
					response = new HTTPResponse();
					response.setVersion("HTTP/1.1");
					response.setStatus(HTTPResponseStatus.S400); // 400 Bad Request
					break;
			}
			
						
			OutputStream output = socket.getOutputStream();
			OutputStreamWriter outputWriter = new OutputStreamWriter(output);
			response.print(outputWriter);
			
			System.out.println(response.toString()); //TODO: Quitar esto
			
			outputWriter.flush();
			
		} catch (Exception e) {
			//If something happened, build a 500 (Internal Server Error) response
			HTTPResponse response = new HTTPResponse();
			response.setVersion("HTTP/1.1");
			response.setStatus(HTTPResponseStatus.S500);
			
			//Send it to the client
			//TODO: Ask if this could be moved to the **finally** block
			try {
				OutputStream output = socket.getOutputStream();
				OutputStreamWriter outputWriter = new OutputStreamWriter(output);
				response.print(outputWriter);
				
				System.out.println(response.toString()+"\n\n"); //TODO: Quitar esto
				
				outputWriter.flush();
			}
			catch(IOException ioe) {} //Whatever
			throw new RuntimeException(e);
		} finally {
			//Close socket even if some exception occurred
			try{socket.close();}
			catch(IOException e) {} //Whatever
		}

	}
}
