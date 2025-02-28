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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import be.nabu.eai.developer.MainController;
import be.nabu.eai.developer.managers.base.BaseJAXBGUIManager;
import be.nabu.eai.developer.managers.util.EnumeratedSimpleProperty;
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
		buttons.setPadding(new Insets(10));
		buttons.setAlignment(Pos.CENTER);
		vbox.getChildren().add(buttons);
		Button upload = new Button("Update from file");
		upload.addEventHandler(ActionEvent.ANY, new EventHandler<ActionEvent>() {
			@SuppressWarnings({ "rawtypes", "unchecked" })
			@Override
			public void handle(ActionEvent arg0) {
				Set properties = new LinkedHashSet();
				properties.add(new SimpleProperty<byte[]>("WSDL", byte[].class, true));
				final SimplePropertyUpdater updater = new SimplePropertyUpdater(true, properties);
				EAIDeveloperUtils.buildPopup(MainController.getInstance(), updater, "Upload standalone WSDL or zip", new EventHandler<ActionEvent>() {
					@Override
					public void handle(ActionEvent arg0) {
						try {
							byte [] content = updater.getValue("WSDL");
							
							uploadWsdl(instance, content);
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
		Button download = new Button("Update from URI");
		download.addEventHandler(ActionEvent.ANY, new EventHandler<ActionEvent>() {
			@SuppressWarnings({ "rawtypes", "unchecked" })
			@Override
			public void handle(ActionEvent arg0) {
				SimpleProperty<URI> fileProperty = new SimpleProperty<URI>("URI", URI.class, true);
				final SimplePropertyUpdater updater = new SimplePropertyUpdater(true, new LinkedHashSet(Arrays.asList(new Property [] { fileProperty })));
				EAIDeveloperUtils.buildPopup(MainController.getInstance(), updater, "WSDL URI", new EventHandler<ActionEvent>() {
					@Override
					public void handle(ActionEvent arg0) {
						try {
							if (instance.getDirectory().getChild("main.wsdl") != null) {
								((ManageableContainer<?>) instance.getDirectory()).delete("main.wsdl");
							}
							URI uri = updater.getValue("URI");
							if (uri != null) {
								Resource child = instance.getDirectory().getChild("main.wsdl");
								if (child == null) {
									child = ((ManageableContainer<?>) instance.getDirectory()).create("main.wsdl", "application/xml");
								}
								WritableContainer<ByteBuffer> writable = ((WritableResource) child).getWritable();
								try {
									InputStream stream = uri.toURL().openStream();
									try {
										IOUtils.copyBytes(IOUtils.wrap(stream), writable);
									}
									finally {
										stream.close();
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
		buttons.getChildren().addAll(upload, download);
		vbox.prefWidthProperty().bind(scroll.widthProperty());
		scroll.setContent(vbox);
		AnchorPane anchorPane = new AnchorPane();
		display(instance, anchorPane);
		vbox.getChildren().add(anchorPane);
		pane.getChildren().add(scroll);
	}
	
	public void uploadWsdl(WSDLClient instance, byte[] content) throws IOException {
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
		
		boolean zipped = false;
		try {
			ZipInputStream zipInputStream = new ZipInputStream(new ByteArrayInputStream(content));
			if (zipInputStream.getNextEntry() != null) {
				zipped = true;
			}
		}
		catch (Exception e) {
			// it's not a zip file...
		}
		
		if (zipped) {
			String mainWsdl = null;
			Resource privateDirectory = instance.getDirectory().getChild(EAIResourceRepository.PRIVATE);
			if (privateDirectory == null) {
				privateDirectory = ((ManageableContainer<?>) instance.getDirectory()).create(EAIResourceRepository.PRIVATE, Resource.CONTENT_TYPE_DIRECTORY);
			}
			ResourceUtils.unzip(new ZipInputStream(new ByteArrayInputStream(content)), (ResourceContainer<?>) privateDirectory);
			// if you didn't enter a main wsdl, we assume there is only one wsdl, take that
			if (mainWsdl == null) {
				int count = 0;
				for (Resource resource : (ResourceContainer<?>) privateDirectory) {
					if (resource.getName().endsWith(".wsdl")) {
						mainWsdl = resource.getName();
						count++;
					}
				}
				if (count != 1) {
					mainWsdl = null;
				}
			}
			if (mainWsdl == null) {
				Set properties = new LinkedHashSet();
				EnumeratedSimpleProperty<String> e = new EnumeratedSimpleProperty<String>("Main WSDL Name", String.class, false);
				for (Resource resource : (ResourceContainer<?>) privateDirectory) {
					if (resource.getName().endsWith(".wsdl")) {
						e.addAll(resource.getName());
					}
				}
				properties.add(e);
				final SimplePropertyUpdater updater = new SimplePropertyUpdater(true, properties);
				EAIDeveloperUtils.buildPopup(MainController.getInstance(), updater, "Pick main WSDL", new EventHandler<ActionEvent>() {
					@Override
					public void handle(ActionEvent arg0) {
						String mainWsdl = updater.getValue("Main WSDL Name");
						if (mainWsdl != null) {
							try {
								writeWsdl(instance, mainWsdl);
							}
							catch (Exception e) {
								MainController.getInstance().notify(e);
							}
						}
					}
				});
			}
			else {
				writeWsdl(instance, mainWsdl);
			}
		}
		else {
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
	}

	private void writeWsdl(WSDLClient instance, String mainWsdl) throws IOException {
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
}
