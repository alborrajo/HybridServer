package es.uvigo.esei.dai.hybridserver;

import javax.jws.WebService;

import es.uvigo.esei.dai.hybridserver.pages.htmlpages.*;
import es.uvigo.esei.dai.hybridserver.pages.xmlpages.*;
import es.uvigo.esei.dai.hybridserver.pages.xsdpages.*;
import es.uvigo.esei.dai.hybridserver.pages.xsltpages.*;

@WebService(
		endpointInterface = "es.uvigo.esei.dai.hybridserver.HybridServerService",
		serviceName="HybridServerService",
		targetNamespace="http://hybridserver.dai.esei.uvigo.es/"
		)
public class HybridServerServiceImpl implements HybridServerService {

	HTMLPages htmlPages;
	XMLPages xmlPages;
	XSDPages xsdPages;
	XSLTPages xsltPages;
	
	public HybridServerServiceImpl(HTMLPages htmlPages, XMLPages xmlPages, XSDPages xsdPages, XSLTPages xsltPages) {
		this.htmlPages = htmlPages;
		this.xmlPages = xmlPages;
		this.xsdPages = xsdPages;
		this.xsltPages = xsltPages;
	}
	
	@Override
	public String[] getHTMLs() {
		return htmlPages.getUUIDs();
	}

	@Override
	public String getHTML(String key) {
		return htmlPages.get(key);
	}

	@Override
	public String[] getXMLs() {
		return xmlPages.getUUIDs();
	}

	@Override
	public String getXML(String key) {
		return xmlPages.get(key);
	}

	@Override
	public String[] getXSDs() {
		return xsdPages.getUUIDs();
	}

	@Override
	public String getXSD(String key) {
		return xsdPages.get(key);
	}

	@Override
	public String[] getXSLTs() {
		return xsltPages.getUUIDs();
	}

	@Override
	public String getXSLT(String key) {
		return xsltPages.get(key);
	}

	@Override
	public String getXSDofXSLT(String key) {
		return xsltPages.getXSDUUID(key);
	}

}
