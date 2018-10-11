<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:configuration="http://www.esei.uvigo.es/dai/hybridserver">

	<xsl:output method="html" indent="yes" encoding="utf-8" />

	<xsl:template match="/">
		<xsl:text disable-output-escaping="yes">&lt;!DOCTYPE HTML&gt;</xsl:text>
		<html>
			<head>
				<title>Configuration</title>
			</head>
			<body>
				<h1>Configuration</h1>
				
				<xsl:apply-templates select="configuration:configuration/configuration:connections" />
				<xsl:apply-templates select="configuration:configuration/configuration:database" />
				<xsl:apply-templates select="configuration:configuration/configuration:servers" />
			</body>
		</html>
	</xsl:template>
	
	
	<xsl:template match="configuration:connections">
		<h2>Connections</h2>
		
		<p><b>http</b>: <xsl:value-of select="configuration:http"/></p>
		<p><b>webservice</b>: <xsl:value-of select="configuration:webservice"/></p>
		<p><b>numClients</b>: <xsl:value-of select="configuration:numClients"/></p>
	</xsl:template>
	
	
	<xsl:template match="configuration:database">
		<h2>Database</h2>
		
		<p><b>user</b>: <xsl:value-of select="configuration:user"/></p>
		<p><b>password</b>: <xsl:value-of select="configuration:password"/></p>
		<p><b>url</b>: <xsl:value-of select="configuration:url"/></p>
	</xsl:template>
	
	
	<xsl:template match="configuration:servers">
		<h2>Servers</h2>
		<xsl:apply-templates select="configuration:server" />
	</xsl:template>
	
	<xsl:template match="configuration:server">
		<h3><xsl:value-of select="@name"/></h3>
		<p><b>wsdl</b>: <xsl:value-of select="@wsdl"/></p>
		<p><b>namespace</b>: <xsl:value-of select="@namespace"/></p>
		<p><b>service</b>: <xsl:value-of select="@service"/></p>
		<p><b>httpAddress</b>: <xsl:value-of select="@httpAddress"/></p>
	</xsl:template>
	
</xsl:stylesheet>

