package be.nabu.eai.module.wsdl.client;

import be.nabu.libs.services.api.DefinedServiceInterface;
import be.nabu.libs.services.api.ServiceInterface;
import be.nabu.libs.types.api.ComplexType;
import be.nabu.libs.types.structure.Structure;
import be.nabu.libs.wsdl.api.BindingOperation;

public class WSDLInterface implements DefinedServiceInterface {

	private String id;
	private BindingOperation operation;

	public WSDLInterface(String id, BindingOperation operation) {
		this.id = id;
		this.operation = operation;
	}
	
	private Structure input, output;
	@Override
	public ComplexType getInputDefinition() {
		if (input == null) {
			input = new Structure();
			input.setName("input");
			if (operation.getOperation().getInput() != null && !operation.getOperation().getInput().getParts().isEmpty()) {
				input.setSuperType((ComplexType) operation.getOperation().getInput().getParts().get(0).getElement().getType());
			}
		}
		return input;
	}
	@Override
	public ComplexType getOutputDefinition() {
		if (output == null) {
			output = new Structure();
			output.setName("output");
			if (operation.getOperation().getOutput() != null && !operation.getOperation().getOutput().getParts().isEmpty()) {
				output.setSuperType(operation.getOperation().getOutput().getParts().get(0).getElement().getType());
			}
		}
		return output;
	}

	@Override
	public ServiceInterface getParent() {
		return null;
	}

	@Override
	public String getId() {
		return id;
	}

	public BindingOperation getOperation() {
		return operation;
	}

}
