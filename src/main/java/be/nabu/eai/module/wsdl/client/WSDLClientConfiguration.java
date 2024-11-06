/*
* Copyright (C) 2016 Alexander Verbruggen
*
* This program is free software: you can redistribute it and/or modify
* it under the terms of the GNU Lesser General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
* GNU Lesser General Public License for more details.
*
* You should have received a copy of the GNU Lesser General Public License
* along with this program. If not, see <https://www.gnu.org/licenses/>.
*/

package be.nabu.eai.module.wsdl.client;

import java.nio.charset.Charset;
import java.util.List;
import java.util.TimeZone;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import be.nabu.eai.api.EnvironmentSpecific;
import be.nabu.eai.module.http.client.HTTPClientArtifact;
import be.nabu.eai.repository.jaxb.ArtifactXMLAdapter;
import be.nabu.eai.repository.jaxb.CharsetAdapter;
import be.nabu.eai.repository.jaxb.TimeZoneAdapter;
import be.nabu.libs.http.api.WebAuthorizationType;
import be.nabu.libs.services.wsdl.api.WSSecurityType;
import be.nabu.libs.types.api.DefinedTypeRegistry;
import be.nabu.libs.types.api.annotation.Field;
import be.nabu.libs.types.base.Duration;

@XmlRootElement(name = "wsdlClient")
public class WSDLClientConfiguration {
	
	private List<String> predefinedPrefixes;
	private HTTPClientArtifact httpClient;
	private List<DefinedTypeRegistry> registries;
	private Charset charset;
	private String username, password;
	private boolean stringsOnly;
	private String endpoint;
	private WebAuthorizationType preemptiveAuthorizationType;
	private WSSecurityType wsSecurity;
	private Duration wsSecurityTimeout;
	private TimeZone timezone;
	// not yet implemented...
//	private List<Class<WSExtension>> extensions;
	
	// unclear how this would be used?
	@Deprecated
	public List<String> getPredefinedPrefixes() {
		return predefinedPrefixes;
	}
	public void setPredefinedPrefixes(List<String> predefinedPrefixes) {
		this.predefinedPrefixes = predefinedPrefixes;
	}
	
	@EnvironmentSpecific
	@XmlJavaTypeAdapter(value = ArtifactXMLAdapter.class)
	public HTTPClientArtifact getHttpClient() {
		return httpClient;
	}
	public void setHttpClient(HTTPClientArtifact httpClient) {
		this.httpClient = httpClient;
	}
	
	@XmlJavaTypeAdapter(value = ArtifactXMLAdapter.class)	
	public List<DefinedTypeRegistry> getRegistries() {
		return registries;
	}
	public void setRegistries(List<DefinedTypeRegistry> registries) {
		this.registries = registries;
	}
	
	@XmlJavaTypeAdapter(value = CharsetAdapter.class)
	public Charset getCharset() {
		return charset;
	}
	public void setCharset(Charset charset) {
		this.charset = charset;
	}
	
	@EnvironmentSpecific
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	
	@EnvironmentSpecific
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	
	public boolean isStringsOnly() {
		return stringsOnly;
	}
	public void setStringsOnly(boolean stringsOnly) {
		this.stringsOnly = stringsOnly;
	}
	
	@EnvironmentSpecific
	public String getEndpoint() {
		return endpoint;
	}
	public void setEndpoint(String endpoint) {
		this.endpoint = endpoint;
	}
	
	public WebAuthorizationType getPreemptiveAuthorizationType() {
		return preemptiveAuthorizationType;
	}
	public void setPreemptiveAuthorizationType(WebAuthorizationType preemptiveAuthorizationType) {
		this.preemptiveAuthorizationType = preemptiveAuthorizationType;
	}
	
	public WSSecurityType getWsSecurity() {
		return wsSecurity;
	}
	public void setWsSecurity(WSSecurityType wsSecurity) {
		this.wsSecurity = wsSecurity;
	}
	
	@Field(show = "wsSecurity != null")
	public Duration getWsSecurityTimeout() {
		return wsSecurityTimeout;
	}
	public void setWsSecurityTimeout(Duration wsSecurityTimeout) {
		this.wsSecurityTimeout = wsSecurityTimeout;
	}
	
	@XmlJavaTypeAdapter(value = TimeZoneAdapter.class)
	public TimeZone getTimezone() {
		return timezone;
	}
	public void setTimezone(TimeZone timezone) {
		this.timezone = timezone;
	}

}
