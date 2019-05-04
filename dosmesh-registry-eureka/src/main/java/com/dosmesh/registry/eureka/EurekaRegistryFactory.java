package com.dosmesh.registry.eureka;
import org.apache.dubbo.common.URL;
import org.apache.dubbo.registry.Registry;
import org.apache.dubbo.registry.support.AbstractRegistryFactory;

public class EurekaRegistryFactory extends AbstractRegistryFactory {

	public Registry createRegistry(URL url) {
		return new EurekaRegistry(url);
	}

}