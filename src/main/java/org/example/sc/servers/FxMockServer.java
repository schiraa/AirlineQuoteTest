package org.example.sc.servers;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;

public  class FxMockServer {
	public static WireMockServer fxServer;
	public static  void startServer() {

		 fxServer = new WireMockServer (
				 WireMockConfiguration.options().DYNAMIC_PORT
		);

		fxServer.start();

	}

	public static void stopServer() {
		if(fxServer.isRunning ()) {
			fxServer.stop ();
		}
	}
}
