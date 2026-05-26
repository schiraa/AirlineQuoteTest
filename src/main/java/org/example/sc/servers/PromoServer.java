package org.example.sc.servers;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;

public class PromoServer {
	public static WireMockServer promoServer;

	public static void startServer(){
		promoServer = new WireMockServer(WireMockConfiguration.options ().dynamicPort ());
		promoServer.start();

	}

	public static void stopServer() {
		if(promoServer.isRunning ()) {
			promoServer.stop ();
		}
	}
}
