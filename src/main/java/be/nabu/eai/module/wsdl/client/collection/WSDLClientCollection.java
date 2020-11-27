package be.nabu.eai.module.wsdl.client.collection;

import java.util.ArrayList;
import java.util.List;

import be.nabu.eai.developer.MainController;
import be.nabu.eai.developer.api.CollectionManager;
import be.nabu.eai.developer.collection.EAICollectionUtils;
import be.nabu.eai.developer.impl.CustomTooltip;
import be.nabu.eai.module.wsdl.client.WSDLClient;
import be.nabu.eai.repository.api.Entry;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.Button;

public class WSDLClientCollection implements CollectionManager {

	private Entry entry;

	public WSDLClientCollection(Entry entry) {
		this.entry = entry;
	}

	@Override
	public Entry getEntry() {
		return entry;
	}

	@Override
	public boolean hasSummaryView() {
		return true;
	}

	@Override
	public Node getSummaryView() {
		List<Button> buttons = new ArrayList<Button>();
		for (Entry child : entry) {
			if (child.isNode() && WSDLClient.class.isAssignableFrom(child.getNode().getArtifactClass())) {
				Button button = new Button();
				button.setGraphic(MainController.loadFixedSizeGraphic("icons/eye.png", 16));
				// if you only have one child it is probably named "swagger" which is good
				// if you have multiple, you probably gave them a descriptive name, also good!
				new CustomTooltip("View " + EAICollectionUtils.getPrettyName(child)).install(button);
				button.addEventHandler(ActionEvent.ANY, new EventHandler<ActionEvent>() {
					@Override
					public void handle(ActionEvent arg0) {
						MainController.getInstance().open(child.getId());
					}
				});
				buttons.add(button);
			}
		}
		// TODO: add a remove button for the entire connector
		buttons.add(EAICollectionUtils.newDeleteButton(entry, null));
		return EAICollectionUtils.newSummaryTile(entry, "wsdlclient-large.png", buttons.toArray(new Button[buttons.size()]));
	}

	
}
