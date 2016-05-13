package be.nabu.eai.module.wsdl.client;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.zip.ZipInputStream;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import be.nabu.eai.developer.MainController;
import be.nabu.eai.developer.managers.base.BaseJAXBGUIManager;
import be.nabu.eai.developer.managers.util.SimpleProperty;
import be.nabu.eai.developer.managers.util.SimplePropertyUpdater;
import be.nabu.eai.developer.util.EAIDeveloperUtils;
import be.nabu.eai.repository.EAIResourceRepository;
import be.nabu.eai.repository.resources.RepositoryEntry;
import be.nabu.libs.property.api.Property;
import be.nabu.libs.property.api.Value;
import be.nabu.libs.resources.ResourceUtils;
import be.nabu.libs.resources.api.ManageableContainer;
import be.nabu.libs.resources.api.Resource;
import be.nabu.libs.resources.api.ResourceContainer;
import be.nabu.libs.resources.api.WritableResource;
import be.nabu.utils.io.IOUtils;
import be.nabu.utils.io.api.ByteBuffer;
import be.nabu.utils.io.api.WritableContainer;

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
	
	@Override
	public void display(MainController controller, AnchorPane pane, WSDLClient instance) {
		ScrollPane scroll = new ScrollPane();
		AnchorPane.setBottomAnchor(scroll, 0d);
		AnchorPane.setTopAnchor(scroll, 0d);
		AnchorPane.setLeftAnchor(scroll, 0d);
		AnchorPane.setRightAnchor(scroll, 0d);
		VBox vbox = new VBox();
		HBox buttons = new HBox();
		vbox.getChildren().add(buttons);
		Button button = new Button("Upload WSDL");
		buttons.getChildren().add(button);
		button.addEventHandler(ActionEvent.ANY, new EventHandler<ActionEvent>() {
			@SuppressWarnings({ "rawtypes", "unchecked" })
			@Override
			public void handle(ActionEvent arg0) {
				final SimplePropertyUpdater zippedUpdater = new SimplePropertyUpdater(true, new LinkedHashSet(Arrays.asList(new Property [] { new SimpleProperty<Boolean>("Zip?", Boolean.class, true) })));
				EAIDeveloperUtils.buildPopup(MainController.getInstance(), zippedUpdater, "Is standalone WSDL or zipped?", new EventHandler<ActionEvent>() {
					@Override
					public void handle(ActionEvent arg0) {
						final Boolean zipped = zippedUpdater.getValue("Zip?") == null ? false : zippedUpdater.getValue("Zip?");
						Set properties = new LinkedHashSet();
						properties.add(new SimpleProperty<byte[]>(zipped ? "Zip" : "WSDL", byte[].class, true));
						if (zipped != null && zipped) {
							properties.add(new SimpleProperty<String>("Main WSDL Name", String.class, false));	
						}
						final SimplePropertyUpdater updater = new SimplePropertyUpdater(true, properties);
						EAIDeveloperUtils.buildPopup(MainController.getInstance(), updater, "Upload WSDL", new EventHandler<ActionEvent>() {
							@Override
							public void handle(ActionEvent arg0) {
								try {
									// remove current
									if (instance.getDirectory().getChild(EAIResourceRepository.PRIVATE) != null) {
										((ManageableContainer<?>) instance.getDirectory()).delete(EAIResourceRepository.PRIVATE);
									}
									if (instance.getDirectory().getChild("main.wsdl") != null) {
										((ManageableContainer<?>) instance.getDirectory()).delete("main.wsdl");
									}
									if (instance.getDirectory().getChild("main.properties") != null) {
										((ManageableContainer<?>) instance.getDirectory()).delete("main.properties");
									}
									
									if (zipped) {
										byte [] content = updater.getValue("Zip");
										String mainWsdl = updater.getValue("Main WSDL Name");
										Resource privateDirectory = instance.getDirectory().getChild(EAIResourceRepository.PRIVATE);
										if (privateDirectory == null) {
											privateDirectory = ((ManageableContainer<?>) instance.getDirectory()).create(EAIResourceRepository.PRIVATE, Resource.CONTENT_TYPE_DIRECTORY);
										}
										ResourceUtils.unzip(new ZipInputStream(new ByteArrayInputStream(content)), (ResourceContainer<?>) privateDirectory);
										// if you didn't enter a main wsdl, we assume there is only one wsdl, take that
										if (mainWsdl == null) {
											for (Resource resource : (ResourceContainer<?>) privateDirectory) {
												if (resource.getName().endsWith(".wsdl")) {
													mainWsdl = resource.getName();
												}
											}
										}
										if (mainWsdl == null) {
											throw new RuntimeException("Could not find the main wsdl");
										}
										Resource child = instance.getDirectory().getChild("main.properties");
										if (child == null) {
											child = ((ManageableContainer<?>) instance.getDirectory()).create("main.properties", "text/plain");
										}
										Properties properties = new Properties();
										properties.put("wsdl", mainWsdl);
										WritableContainer<ByteBuffer> writable = ((WritableResource) child).getWritable();
										try {
											properties.store(IOUtils.toOutputStream(writable), "");
										}
										finally {
											writable.close();
										}
									}
									else {
										byte [] content = updater.getValue("WSDL");
										Resource child = instance.getDirectory().getChild("main.wsdl");
										if (child == null) {
											child = ((ManageableContainer<?>) instance.getDirectory()).create("main.wsdl", "text/xml");
										}
										WritableContainer<ByteBuffer> writable = ((WritableResource) child).getWritable();
										try {
											if (content.length != writable.write(IOUtils.wrap(content, true))) {
												throw new RuntimeException("Could not write the entire wsdl");
											}
										}
										finally {
											writable.close();
										}
									}
									MainController.getInstance().setChanged();
									MainController.getInstance().refresh(instance.getId());
								}
								catch (Exception e) {
									throw new RuntimeException(e);
								}
							}
						});
					}
				});
			}
		});
		vbox.prefWidthProperty().bind(scroll.widthProperty());
		scroll.setContent(vbox);
		AnchorPane anchorPane = new AnchorPane();
		display(instance, anchorPane);
		vbox.getChildren().add(anchorPane);
		pane.getChildren().add(scroll);
	}
	
}
