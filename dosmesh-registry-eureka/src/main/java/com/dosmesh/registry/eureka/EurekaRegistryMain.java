package com.dosmesh.registry.eureka;

import org.apache.dubbo.common.URL;

public class EurekaRegistryMain {

	public static void main(String[] args) {
		EurekaRegistry registry = new EurekaRegistry(URL.valueOf("http://192.168.1.103:8080/register-server/eureka"));
		try {
			Thread.sleep(1000000L);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
