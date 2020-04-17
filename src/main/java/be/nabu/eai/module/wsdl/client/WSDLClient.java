package be.nabu.eai.module.wsdl.client;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.text.ParseException;
import java.util.Properties;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import be.nabu.eai.repository.EAIResourceRepository;
import be.nabu.eai.repository.api.Repository;
import be.nabu.eai.repository.artifacts.jaxb.JAXBArtifact;
import be.nabu.libs.resources.ResourceReadableContainer;
import be.nabu.libs.resources.ResourceUtils;
import be.nabu.libs.resources.api.ReadableResource;
import be.nabu.libs.resources.api.Resource;
import be.nabu.libs.resources.api.ResourceContainer;
import be.nabu.libs.types.xml.ResourceResolver;
import be.nabu.libs.types.api.TypeRegistry;
import be.nabu.libs.wsdl.api.WSDLDefinition;
import be.nabu.libs.wsdl.parser.WSDLParser;
import be.nabu.utils.io.IOUtils;
import be.nabu.utils.xml.XMLUtils;

public class WSDLClient extends JAXBArtifact<WSDLClientConfiguration> {

	private WSDLDefinition definition;
	
	public WSDLClient(String id, ResourceContainer<?> directory, Repository repository) {
		super(id, directory, repository, "wsdl-client.xml", WSDLClientConfiguration.class);
	}

	public WSDLDefinition getDefinition() throws IOException, ParseException, SAXException, ParserConfigurationException {
		if (definition == null) {
			Resource child = getDirectory().getChild("main.wsdl");
			if (child != null) {
				synchronized(this) {
					if (definition == null) {
						InputStream input = IOUtils.toInputStream(((ReadableResource) child).getReadable());
						Document document;
						try {
							document = XMLUtils.toDocument(input, true);
						}
						finally {
							input.close();
						}
						WSDLParser parser = new WSDLParser(document, getConfiguration().isStringsOnly());
						parser.setId(getId());
						if (getConfiguration().getRegistries() != null) {
							for (TypeRegistry registry : getConfiguration().getRegistries()) {
								parser.register(registry);
							}
						}
						this.definition = parser.getDefinition();
					}
				}
			}
			else {
				child = getDirectory().getChild("main.properties");
				if (child != null) {
					Properties properties = new Properties();
					InputStream input = IOUtils.toInputStream(((ReadableResource) child).getReadable());
					try {
						properties.load(input);
					}
					finally {
						input.close();
					}
					final ResourceContainer<?> privateDirectory = (ResourceContainer<?>) getDirectory().getChild(EAIResourceRepository.PRIVATE);
					if (privateDirectory != null && properties.getProperty("wsdl") != null) {
						child = privateDirectory.getChild(properties.getProperty("wsdl"));
						input = IOUtils.toInputStream(((ReadableResource) child).getReadable());
						Document document;
						try {
							document = XMLUtils.toDocument(input, true);
						}
						finally {
							input.close();
						}
						WSDLParser parser = new WSDLParser(document, getConfiguration().isStringsOnly());
						parser.setId(getId());
						parser.setResolver(new ResourceResolver() {
							@Override
							public InputStream resolve(URI uri) throws IOException {
								// path always starts with a "/", we don't want to go back to the root, but start in the private directory
								Resource resolved = ResourceUtils.resolve(privateDirectory, uri.getPath());
								return resolved == null ? null : IOUtils.toInputStream(new ResourceReadableContainer((ReadableResource) resolved));
							}
							@Override
							public TypeRegistry resolve(String namespace) throws IOException {
								return null;
							}
						});
						if (getConfiguration().getRegistries() != null) {
							for (TypeRegistry registry : getConfiguration().getRegistries()) {
								parser.register(registry);
							}
						}
						this.definition = parser.getDefinition();
					}
				}
			}
		}
		return definition;
	}

}
