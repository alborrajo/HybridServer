package es.uvigo.esei.dai.hybridserver;

import javax.jws.WebMethod;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.jws.soap.SOAPBinding.Style;

@WebService(serviceName="HybridServerService", targetNamespace="http://hybridserver.dai.esei.uvigo.es/")
@SOAPBinding(style = Style.RPC)
public interface HybridServerService {
	
	@WebMethod
	public String[] getHTMLs();
	
	@WebMethod
	public String getHTML(String key);
	
	
	@WebMethod
	public String[] getXMLs();
	
	@WebMethod
	public String getXML(String key);
	
	
	@WebMethod
	public String[] getXSDs();
	
	@WebMethod
	public String getXSD(String key);
	
	
	@WebMethod
	public String[] getXSLTs();
	
	@WebMethod
	public String getXSLT(String key);
	
	@WebMethod
	public String getXSDofXSLT(String key);
}
