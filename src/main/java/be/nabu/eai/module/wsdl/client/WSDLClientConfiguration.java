package be.nabu.eai.module.wsdl.client;

import java.nio.charset.Charset;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import be.nabu.eai.api.EnvironmentSpecific;
import be.nabu.eai.module.http.client.HTTPClientArtifact;
import be.nabu.eai.repository.jaxb.ArtifactXMLAdapter;
import be.nabu.eai.repository.jaxb.CharsetAdapter;
import be.nabu.libs.types.api.DefinedTypeRegistry;

@XmlRootElement(name = "wsdlClient")
public class WSDLClientConfiguration {
	
	private List<String> prefinedPrefixes;
	private HTTPClientArtifact httpClient;
	private List<DefinedTypeRegistry> registries;
	private Charset charset;
	private String username, password;
	private boolean stringsOnly;
	
	public List<String> getPrefinedPrefixes() {
		return prefinedPrefixes;
	}
	public void setPrefinedPrefixes(List<String> prefinedPrefixes) {
		this.prefinedPrefixes = prefinedPrefixes;
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

}
