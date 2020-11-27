package be.nabu.eai.module.wsdl.client.collection;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import be.nabu.eai.developer.CollectionActionImpl;
import be.nabu.eai.developer.MainController;
import be.nabu.eai.developer.api.CollectionAction;
import be.nabu.eai.developer.api.CollectionManager;
import be.nabu.eai.developer.api.CollectionManagerFactory;
import be.nabu.eai.developer.collection.EAICollectionUtils;
import be.nabu.eai.developer.util.EAIDeveloperUtils;
import be.nabu.eai.module.wsdl.client.WSDLClient;
import be.nabu.eai.module.wsdl.client.WSDLClientGUIManager;
import be.nabu.eai.module.wsdl.client.WSDLClientManager;
import be.nabu.eai.repository.CollectionImpl;
import be.nabu.eai.repository.api.Entry;
import be.nabu.eai.repository.resources.RepositoryEntry;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;

public class WSDLClientCollectionFactory implements CollectionManagerFactory {

	@Override
	public CollectionManager getCollectionManager(Entry entry) {
		for (Entry child : entry) {
			if (child.isNode() && WSDLClient.class.equals(child.getNode().getArtifactClass())) {
				return new WSDLClientCollection(entry);
			}
		}
		return null;
	}

	@Override
	public List<CollectionAction> getActionsFor(Entry entry) {
		List<CollectionAction> actions = new ArrayList<CollectionAction>();
		if (EAICollectionUtils.isProject(entry)) {
			actions.add(new CollectionActionImpl(EAICollectionUtils.newActionTile("wsdlclient-large.png", "Add WSDL", "Load a third party WSDL."), new javafx.event.EventHandler<ActionEvent>() {
				@Override
				public void handle(ActionEvent arg0) {
					WSDLClientCreate wsdlClientCreate = new WSDLClientCreate();
					EAIDeveloperUtils.buildPopup("Connect via WSDL", "WSDL", wsdlClientCreate, new EventHandler<ActionEvent>() {
						@Override
						public void handle(ActionEvent arg0) {
							byte [] content = wsdlClientCreate.getContent();
							String name = wsdlClientCreate.getName();
							// only proceed if we actually have a URI
							if (content != null && name != null && !name.trim().isEmpty()) {
								try {
									RepositoryEntry connectorEntry = getSwaggerEntry((RepositoryEntry) entry, wsdlClientCreate.getName());
									WSDLClient wsdlClient = new WSDLClient(connectorEntry.getId(), connectorEntry.getContainer(), connectorEntry.getRepository());
									
									new WSDLClientManager().save(connectorEntry, wsdlClient);
									new WSDLClientGUIManager().uploadWsdl(wsdlClient, content);
									EAIDeveloperUtils.created(wsdlClient.getId());
									
									MainController.getInstance().open(wsdlClient.getId());
								}
								catch (Exception e) {
									MainController.getInstance().notify(e);
								}
							}
						}
					}, false).show();
				}
			}));
		}
		return actions;
	}

	public static Entry getConnectorsEntry(RepositoryEntry project) throws IOException {
		Entry child = EAIDeveloperUtils.mkdir(project, "connectors");
		if (!child.isCollection()) {
			CollectionImpl collection = new CollectionImpl();
			collection.setType("folder");
			collection.setName("Connectors");
			collection.setSmallIcon("connector/connector-small.png");
			collection.setMediumIcon("connector/connector-medium.png");
			collection.setLargeIcon("connector/connector-large.png");
			((RepositoryEntry) child).setCollection(collection);
			((RepositoryEntry) child).saveCollection();
		}
		return child;
	}
	
	public static Entry getConnectorEntry(RepositoryEntry project, String name) throws IOException {
		String normalize = EAICollectionUtils.normalize(name);
		Entry child = EAIDeveloperUtils.mkdir((RepositoryEntry) getConnectorsEntry(project), normalize);
		if (!child.isCollection()) {
			CollectionImpl collection = new CollectionImpl();
			collection.setType("connector");
			collection.setSubType("swagger");
			if (!normalize.equals(name)) {
				collection.setName(name);
			}
			((RepositoryEntry) child).setCollection(collection);
			((RepositoryEntry) child).saveCollection();
		}
		return child;
	}
	
	public static RepositoryEntry getSwaggerEntry(RepositoryEntry project, String name) throws IOException {
		Entry connectorEntry = getConnectorEntry(project, name);
		return ((RepositoryEntry) connectorEntry).createNode("wsdl", new WSDLClientManager(), true);
	}
}
