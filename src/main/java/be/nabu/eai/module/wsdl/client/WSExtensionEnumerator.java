package be.nabu.eai.module.wsdl.client;

import java.util.ArrayList;
import java.util.List;

import be.nabu.eai.api.Enumerator;
import be.nabu.eai.repository.EAIRepositoryUtils;
import be.nabu.eai.repository.EAIResourceRepository;
import be.nabu.libs.services.wsdl.api.WSExtension;

public class WSExtensionEnumerator implements Enumerator {

	@Override
	public List<?> enumerate() {
		List<Class<?>> values = new ArrayList<Class<?>>();
		for (Class<?> implementation : EAIRepositoryUtils.getImplementationsFor(EAIResourceRepository.getInstance().getClassLoader(), WSExtension.class)) {
			values.add(implementation);
		}
		return values;
	}

}
