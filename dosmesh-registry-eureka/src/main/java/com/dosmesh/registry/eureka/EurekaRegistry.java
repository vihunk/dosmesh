
package com.dosmesh.registry.eureka;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.dubbo.common.Constants;
import org.apache.dubbo.common.URL;
import org.apache.dubbo.common.utils.CollectionUtils;
import org.apache.dubbo.common.utils.UrlUtils;
import org.apache.dubbo.registry.NotifyListener;
import org.apache.dubbo.registry.support.FailbackRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.netflix.appinfo.ApplicationInfoManager;
import com.netflix.appinfo.CloudInstanceConfig;
import com.netflix.appinfo.DataCenterInfo.Name;
import com.netflix.appinfo.EurekaInstanceConfig;
import com.netflix.appinfo.InstanceInfo;
import com.netflix.appinfo.InstanceInfo.InstanceStatus;
import com.netflix.appinfo.LeaseInfo;
import com.netflix.appinfo.MyDataCenterInfo;
import com.netflix.appinfo.MyDataCenterInstanceConfig;
import com.netflix.discovery.DefaultEurekaClientConfig;
import com.netflix.discovery.DiscoveryClient;
import com.netflix.discovery.EurekaClient;

public class EurekaRegistry extends FailbackRegistry {

	private static final Logger logger = LoggerFactory.getLogger(EurekaRegistry.class);

	private EurekaClient discoveryClient;

	public EurekaRegistry(URL url) {
		super(url);
		if (url.isAnyHost()) {
			throw new IllegalStateException("registry address == null");
		}

		initEurekaDiscoveryClient(url);
	}

	private void initEurekaDiscoveryClient(URL url) {
		EurekaInstanceConfig eurekaInstanceConfig = new MyDataCenterInstanceConfig();
		InstanceInfo instanceInfo = new InstanceInfo(url.getServiceKey(), "test", "test-group", "localhost", "sid",
				null, null, "", "", "", "", "", "", 0, new MyDataCenterInfo(Name.MyOwn), "", InstanceStatus.UP,
				InstanceStatus.UP, InstanceStatus.UP, new LeaseInfo(30, 5, 0L, 0L, 0L, 5L, 5), true, null, 0L, 0L, null,
				"");

		ApplicationInfoManager applicationInfoManager = new ApplicationInfoManager(eurekaInstanceConfig, instanceInfo);
		// TODO
		discoveryClient = new DiscoveryClient(applicationInfoManager, new DefaultEurekaClientConfig());
	}

	@Override
	public void doRegister(URL url) {
	}

	@Override
	public void doUnregister(URL url) {
		// discoveryClient.shutdown();
	}

	@Override
	public void doSubscribe(URL url, NotifyListener listener) {

	}

	@Override
	public void doUnsubscribe(URL url, NotifyListener listener) {

	}

	@Override
	public boolean isAvailable() {
		try {
			return discoveryClient != null;
		} catch (Throwable t) {
			return false;
		}
	}

	/**
	 * Remove the expired providers(if clean is true), leave the multicast group and
	 * close the multicast socket.
	 */
	@Override
	public void destroy() {
		super.destroy();
		if (discoveryClient != null)
			discoveryClient.shutdown();
	}

	protected void subscribed(URL url, NotifyListener listener) {
		List<URL> urls = lookup(url);
		notify(url, listener, urls);
	}

	private List<URL> toList(Set<URL> urls) {
		List<URL> list = new ArrayList<URL>();
		if (CollectionUtils.isNotEmpty(urls)) {
			for (URL url : urls) {
				list.add(url);
			}
		}
		return list;
	}

	@Override
	public void register(URL url) {
		super.register(url);
		registered(url);
	}

	protected void registered(URL url) {

	}

	@Override
	public void unregister(URL url) {
		super.unregister(url);
		unregistered(url);
	}

	protected void unregistered(URL url) {

	}

	@Override
	public void subscribe(URL url, NotifyListener listener) {
		super.subscribe(url, listener);
		subscribed(url, listener);
	}

	@Override
	public void unsubscribe(URL url, NotifyListener listener) {
		super.unsubscribe(url, listener);
	}

	@Override
	public List<URL> lookup(URL url) {
		List<URL> urls = new ArrayList<>();
		Map<String, List<URL>> notifiedUrls = getNotified().get(url);
		if (notifiedUrls != null && notifiedUrls.size() > 0) {
			for (List<URL> values : notifiedUrls.values()) {
				urls.addAll(values);
			}
		}
		if (urls.isEmpty()) {
			List<URL> cacheUrls = getCacheUrls(url);
			if (CollectionUtils.isNotEmpty(cacheUrls)) {
				urls.addAll(cacheUrls);
			}
		}
		if (urls.isEmpty()) {
			for (URL u : getRegistered()) {
				if (UrlUtils.isMatch(url, u)) {
					urls.add(u);
				}
			}
		}
		if (Constants.ANY_VALUE.equals(url.getServiceInterface())) {
			for (URL u : getSubscribed().keySet()) {
				if (UrlUtils.isMatch(url, u)) {
					urls.add(u);
				}
			}
		}
		return urls;
	}

}