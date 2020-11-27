package be.nabu.eai.module.wsdl.client.collection;

import be.nabu.libs.types.api.annotation.ComplexTypeDescriptor;
import be.nabu.libs.types.api.annotation.Field;

@ComplexTypeDescriptor(propOrder = {"name", "content"})
public class WSDLClientCreate {
	
	private String name;
	private byte [] content;
	
	@Field(minOccurs = 1)
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	
	@Field(comment = "Upload a WSDL file, if you need to upload multiple files at once (e.g. xsds, additional wsdls,...) use a zip file.", minOccurs = 1)
	public byte[] getContent() {
		return content;
	}
	public void setContent(byte[] content) {
		this.content = content;
	}
	
}
