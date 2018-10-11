package es.uvigo.esei.dai.hybridserver.http;

import java.io.IOException;

import java.io.Reader;
import java.net.URLDecoder;

import java.io.BufferedReader;
import java.util.LinkedHashMap;
import java.util.Map;

public class HTTPRequest {
	
	private HTTPRequestMethod method;
	private String resourceChain;
	private String resourceName;
	private Map<String,String> resourceParameters;
	private String HttpVersion;
	
	private Map<String,String> headerParameters;
	
	private String content;
	
	public HTTPRequest(Reader reader) throws IOException, HTTPParseException {
		BufferedReader bReader = new BufferedReader(reader);
		String line = "";
		
		try {
			//	FIRST LINE
			//METHOD resourceChain HTTPVersion
			line = bReader.readLine();
			String[] lineParameters = line.split(" ");
			  
			//METHOD
			this.method = HTTPRequestMethod.valueOf(lineParameters[0]);
			
			//resourceChain
			this.resourceChain = lineParameters[1];
			//resourceName?resourceParameters
			String[] resource = lineParameters[1].split("\\?");
			//If resourceName is something like /index.php, remove initial /
			if(resource[0].charAt(0) == '/') {this.resourceName = resource[0].substring(1);}
			else {this.resourceName = resource[0];}
			
			//Check if there are resourceParameters (resource array length is > 1)
			resourceParameters = new LinkedHashMap<>();
			if(resource.length > 1) {
				//resourceParameter=value&resourceParameter=value
				String[] resourceParameters = resource[1].split("&");
				for(String parameter : resourceParameters) {
					//resourceParameter=value
					String[] parameterAndValue = parameter.split("=");
					
					try {this.resourceParameters.put(parameterAndValue[0],parameterAndValue[1]);}
					catch(ArrayIndexOutOfBoundsException e) {
						this.resourceParameters.put(parameterAndValue[0],"");
					}
				}
			}
			
			//HTTPVersion
			this.HttpVersion = lineParameters[2];
		} catch(Exception e) {
			throw new HTTPParseException("Error parseando la primera línea de la petición HTTP\n\t"+line,e);
		}
		
		//	HEADER PARAMETER LINES
		//Parameter: value
		headerParameters = new LinkedHashMap<>();
		while(bReader.ready() && !(line = bReader.readLine()).isEmpty()) { //Read until there's an empty line
			try {
				String [] parameterAndValue = line.split(": "); 
				this.headerParameters.put(parameterAndValue[0],parameterAndValue[1]);
			} catch(Exception e) {
				throw new HTTPParseException("Error parseando el siguiente parametro de la cabecera de la petición\n"+line, e);
			}
		}
		
		
		// CONTENT
		if(headerParameters.containsKey("Content-Length")) { //Check if Request contains content at all before trying to parse
			//Make a char buffer as long as the content (as specified by the header parameters)
			char[] readBuffer = new char[this.getContentLength()];
			bReader.read(readBuffer, 0, readBuffer.length); //Read it all
			
			this.content = String.valueOf(readBuffer);
						
			//If the request is POST, get resource parameters from content
			if(this.method.equals(HTTPRequestMethod.POST)) {
							
				this.resourceParameters = new LinkedHashMap<>();
				String[] lines = this.content.split("&");
								
				for(String contentLine : lines) {
					try {
						String[] parameterAndValue = contentLine.split("=");
						
						// Decode x-www-form-urlencoded
						String value = URLDecoder.decode(parameterAndValue[1], "UTF-8");//"";
						//if (headerParameters.get("Content-Type") != null && headerParameters.get("Content-Type").startsWith(MIME.FORM.getMime())) {
						//   value = URLDecoder.decode(parameterAndValue[1], "UTF-8");
						//}
						
						this.resourceParameters.put(parameterAndValue[0], value);
						
					} catch(Exception e) {
						throw new HTTPParseException("Error parseando el siguiente parametro del contenido de la petición\n"+contentLine, e);
					}
				}
				
				if (headerParameters.get("Content-Type") != null && headerParameters.get("Content-Type").startsWith(MIME.FORM.getMime())) {
					this.content = URLDecoder.decode(this.content, "UTF-8");
				}
				
				
			}
			
		}
		
	}

	public HTTPRequestMethod getMethod() {
		return method;
	}

	public String getResourceChain() {
		return resourceChain;
	}

	public String[] getResourcePath() {
		if(resourceName.isEmpty()) {return new String[] {};} //If the string is empty, return an empty array
		else {return resourceName.split("/");} //Else, return the string, split by the slashes (/)
		//This is done so empty strings don't return an array containing one empty string
	}

	public String getResourceName() {
		return resourceName;
	}

	public Map<String, String> getResourceParameters() {
		return resourceParameters;
	}

	public String getHttpVersion() {
		return HttpVersion;
	}

	public Map<String, String> getHeaderParameters() {
		return headerParameters;
	}

	public String getContent() {
		return content;
	}

	public int getContentLength() {
		try {
			return Integer.parseInt(headerParameters.get("Content-Length"));
		} catch (NumberFormatException e) {
			return 0;
		}
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder(this.getMethod().name()).append(' ').append(this.getResourceChain())
				.append(' ').append(this.getHttpVersion()).append("\r\n");

		for (Map.Entry<String, String> param : this.getHeaderParameters().entrySet()) {
			sb.append(param.getKey()).append(": ").append(param.getValue()).append("\r\n");
		}

		if (this.getContentLength() > 0) {
			sb.append("\r\n").append(this.getContent());
		}

		return sb.toString();
	}
}
