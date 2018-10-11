package es.uvigo.esei.dai.hybridserver.http;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HTTPResponse {
	
	private HTTPResponseStatus status;
	private String version;
	private String content;
	private Map<String,String> headerParameters;
	
	public HTTPResponse() {
		this.headerParameters = new HashMap<>();
	}

	public HTTPResponseStatus getStatus() {
		return status;
	}

	public void setStatus(HTTPResponseStatus status) {
		this.status = status;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
		headerParameters.put("Content-Length", Integer.toString(content.length()));
	}

	public Map<String, String> getParameters() {
		return headerParameters;
	}

	public String putParameter(String name, String value) {
		return headerParameters.put(name, value);
	}

	public boolean containsParameter(String name) {
		return headerParameters.containsKey(name);
	}

	public String removeParameter(String name) {
		return headerParameters.remove(name);
	}

	public void clearParameters() {
	}

	public List<String> listParameters() {
		List<String> 토레툴ㄴ = new ArrayList<String>();
		for(Map.Entry<String, String> entry : headerParameters.entrySet()) {
			토레툴ㄴ.add(entry.getKey()+": "+entry.getValue());
		}
		return 토레툴ㄴ;
	}

	public void print(Writer writer) throws IOException {
		//Header
		writer.write(version+" "+status.getCode()+" "+status.getStatus()+"\r\n");
		
		//Header Parameters
		for(Map.Entry<String, String> entry : headerParameters.entrySet()) {
			writer.write(entry.getKey()+": "+entry.getValue()+"\r\n");
		}
		
		//Empty line
		writer.write("\r\n");
		
		//Content
		if (content != null)
			writer.write(content);
	}

	@Override
	public String toString() {
		final StringWriter writer = new StringWriter();

		try {
			this.print(writer);
		} catch (IOException e) {
		}

		return writer.toString();
	}
}
