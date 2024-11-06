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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import nabu.protocols.http.client.Services;
import be.nabu.eai.repository.EAINode;
import be.nabu.eai.repository.EAIRepositoryUtils;
import be.nabu.eai.repository.api.ArtifactRepositoryManager;
import be.nabu.eai.repository.api.Entry;
import be.nabu.eai.repository.api.ModifiableEntry;
import be.nabu.eai.repository.api.Repository;
import be.nabu.eai.repository.managers.base.JAXBArtifactManager;
import be.nabu.eai.repository.resources.MemoryEntry;
import be.nabu.libs.http.api.client.HTTPClient;
import be.nabu.libs.resources.api.ResourceContainer;
import be.nabu.libs.services.ServiceRuntime;
import be.nabu.libs.services.wsdl.HTTPClientProvider;
import be.nabu.libs.services.wsdl.WSDLInterface;
import be.nabu.libs.services.wsdl.WSDLService;
import be.nabu.libs.services.wsdl.ws.WSSecurity;
import be.nabu.libs.types.api.ComplexType;
import be.nabu.libs.types.api.DefinedType;
import be.nabu.libs.types.api.SimpleType;
import be.nabu.libs.types.xml.XMLSchemaType;
import be.nabu.libs.validator.api.Validation;
import be.nabu.libs.wsdl.api.BindingOperation;

// currently root elements that have an anonymous complex type are not added, depending on how it is set up, this can mean that we are missing a lot of type information in the tree on the left
// we could consider automatically adding all complex types that are of root elements as a named entry?
public class WSDLClientManager extends JAXBArtifactManager<WSDLClientConfiguration, WSDLClient> implements ArtifactRepositoryManager<WSDLClient> {

	public WSDLClientManager() {
		super(WSDLClient.class);
	}

	@Override
	public Class<WSDLClient> getArtifactClass() {
		return WSDLClient.class;
	}

	@Override
	public List<Entry> addChildren(ModifiableEntry root, WSDLClient artifact) throws IOException {
		List<Entry> entries = new ArrayList<Entry>();
		((EAINode) root.getNode()).setLeaf(false);

		try {
			if (artifact.getDefinition() != null && artifact.getDefinition().getBindings() != null && !artifact.getDefinition().getBindings().isEmpty()) {
				// add all services as interfaces
				MemoryEntry interfaces = new MemoryEntry(artifact.getId(), root.getRepository(), root, null, root.getId() + ".interfaces", "interfaces");
				MemoryEntry services = new MemoryEntry(artifact.getId(), root.getRepository(), root, null, root.getId() + ".services", "services");
				for (BindingOperation operation : artifact.getDefinition().getBindings().get(0).getOperations()) {
					final String ifaceId = interfaces.getId() + "." + operation.getName();
					EAINode node = new EAINode();
					node.setArtifact(new WSDLInterface(ifaceId, operation));
					node.setLeaf(true);
					MemoryEntry entry = new MemoryEntry(artifact.getId(), services.getRepository(), interfaces, node, ifaceId, operation.getName());
					node.setEntry(entry);
					interfaces.addChildren(entry);
					entries.add(entry);
					
					WSDLService service = new WSDLService(services.getId() + "." + operation.getName(), operation, new HTTPClientProvider() {
						@Override
						public HTTPClient newHTTPClient(String transactionId) {
							try {
								return Services.getTransactionable(ServiceRuntime.getRuntime().getExecutionContext(), transactionId, artifact.getConfiguration().getHttpClient()).getClient();
							}
							catch (Exception e) {
								throw new RuntimeException(e);
							}
						}
					}, artifact.getConfiguration().getCharset());
					service.setPreemptiveAuthorizationType(artifact.getConfig().getPreemptiveAuthorizationType());
					service.setEndpoint(artifact.getConfig().getEndpoint());
					service.setUsername(artifact.getConfig().getUsername());
					service.setPassword(artifact.getConfig().getPassword());
					if (artifact.getConfig().getWsSecurity() != null) {
						WSSecurity wsSecurity = new WSSecurity();
						wsSecurity.setWsSecurityType(artifact.getConfig().getWsSecurity());
						if (artifact.getConfig().getWsSecurityTimeout() != null) {
							wsSecurity.setTimestampDuration(1000l * artifact.getConfig().getWsSecurityTimeout().toSeconds());
						}
						wsSecurity.setTimezone(artifact.getConfig().getTimezone());
						service.setExtensions(Arrays.asList(wsSecurity));
					}
					node = new EAINode();
					node.setArtifact(service);
					node.setLeaf(true);
					entry = new MemoryEntry(artifact.getId(), services.getRepository(), services, node, services.getId() + "." + operation.getName(), operation.getName());
					node.setEntry(entry);
					services.addChildren(entry);
					entries.add(entry);
				}
				// after a rather big update to the WSDL client to support more complex WSDLs, the WSDL Provider stubbing currently no longer works
				// however, at this point we have runProfiles to simulate responses so it can be argued we no longer need WSDL stubbing
				// for now we disable this feature rather than fixing it
//				root.addChildren(interfaces);
				root.addChildren(services);
				
				for (String namespace : artifact.getDefinition().getRegistry().getNamespaces()) {
					for (ComplexType type : artifact.getDefinition().getRegistry().getComplexTypes(namespace)) {
						if (type instanceof XMLSchemaType && type instanceof DefinedType) {
							addType(root, artifact, entries, (DefinedType) type);
						}
					}
					for (SimpleType<?> type : artifact.getDefinition().getRegistry().getSimpleTypes(namespace)) {
						if (type instanceof XMLSchemaType && type instanceof DefinedType) {
							addType(root, artifact, entries, (DefinedType) type);
						}
					}
				}
			}
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
		return entries;
	}

	private void addType(ModifiableEntry root, WSDLClient artifact, List<Entry> entries, DefinedType type) {
		String id = ((DefinedType) type).getId();
		if (id.startsWith(artifact.getId() + ".")) {
			String parentId = id.replaceAll("\\.[^.]+$", "");
			ModifiableEntry parent = EAIRepositoryUtils.getParent(root, id.substring(artifact.getId().length() + 1), false);
			EAINode node = new EAINode();
			node.setArtifact(type);
			node.setLeaf(true);
			MemoryEntry entry = new MemoryEntry(artifact.getId(), root.getRepository(), parent, node, id, id.substring(parentId.length() + 1));
			node.setEntry(entry);
			parent.addChildren(entry);
			entries.add(entry);
		}
	}

	@Override
	public List<Entry> removeChildren(ModifiableEntry parent, WSDLClient artifact) throws IOException {
		List<Entry> entries = new ArrayList<Entry>();
		Entry services = parent.getChild("services");
		if (services != null) {
			for (Entry service : services) {
				entries.add(service);
			}
			parent.removeChildren("services");
		}
		Entry types = parent.getChild("types");
		if (types != null) {
			for (Entry type : types) {
				entries.add(type);
			}
			parent.removeChildren("types");
		}
		return entries;
	}

	@Override
	protected WSDLClient newInstance(String id, ResourceContainer<?> container, Repository repository) {
		return new WSDLClient(id, container, repository);
	}
}
