package be.nabu.eai.module.wsdl.client;

import java.io.IOException;
import java.util.List;

import be.nabu.eai.developer.MainController;
import be.nabu.eai.developer.managers.base.BaseJAXBGUIManager;
import be.nabu.eai.repository.resources.RepositoryEntry;
import be.nabu.libs.property.api.Property;
import be.nabu.libs.property.api.Value;

public class WSDLClientGUIManager extends BaseJAXBGUIManager<WSDLClientConfiguration, WSDLClient> {

	public WSDLClientGUIManager() {
		super("WSDL Client", WSDLClient.class, new WSDLClientManager(), WSDLClientConfiguration.class);
	}

	@Override
	protected List<Property<?>> getCreateProperties() {
		return null;
	}

	@Override
	protected WSDLClient newInstance(MainController controller, RepositoryEntry entry, Value<?>... values) throws IOException {
		return new WSDLClient(entry.getId(), entry.getContainer(), entry.getRepository());
	}

	public String getCategory() {
		return "Services";
	}
}
