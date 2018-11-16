package com.example.ciscovirl;

import com.tailf.ncs.ApplicationComponent;
import org.apache.log4j.Logger;
import java.net.URI;
import javax.websocket.*;
import com.google.gson.JsonObject;
import java.net.InetAddress;

@ClientEndpoint
public class VirlTracker implements ApplicationComponent {
    private static Logger LOGGER = Logger.getLogger(VirlTracker.class);
    volatile boolean shutdown = false;
	private static Object waitLock = new Object();
    public String url;
    public String token;
	WebSocketContainer container = null;
    Session session = null;

	private static void  wait4TerminateSignal() {
		synchronized(waitLock) {
			try {
			    waitLock.wait();
			} catch (InterruptedException e) {
//				LOGGER.info("VirlTracker running");
			}
		}
	}

	public void VirlTracker() {
		LOGGER.info("VirlTracker instantiated");
	}

	public void init() {
		LOGGER.info("VirlTracker initialized");
	}

	public void finish() {
		LOGGER.info("VirlTracker finished");
		if(session!=null){
			try {
				session.close();
			} catch (Exception e) {     
				LOGGER.info("VirlTracker interrupted: " + e);
			}
		}
		shutdown = true;
	}         


    @Override
	public void run() { 
		LOGGER.info("VirlTracker running");
		while (!shutdown) {
			try {
	            VirlComms comms = new VirlComms(InetAddress.getByName("172.31.21.204"), 19399, "GSVIRL", "C1sco123");
		        JsonObject trackerinfo = comms.requestJSONData(VirlComms.RequestType.POST, "/tracking", "tracking", null);
		        url = trackerinfo.get("url").getAsString();
		        token = trackerinfo.get("token").getAsString();
	            LOGGER.info("Creating WebSocket container");
	            WebSocketContainer container = ContainerProvider.getWebSocketContainer();
	            url = url+"?ws_token="+token;
	            LOGGER.info("Connecting to "+url+" with token "+token);
	            session = container.connectToServer(this, URI.create(url));
	            LOGGER.info("Connected");
				LOGGER.info("VirlTracker still running");
				wait4TerminateSignal();
			} catch (InterruptedException e) {
				LOGGER.info("VirlTracker interrupted: " + e.getMessage());
				Thread.currentThread().interrupt();
	            shutdown = true;
	        } catch (Exception e) {
	            LOGGER.info("VirlTracker Exception "+e);
	            shutdown = true;
	        }
		}
	}

    @OnMessage
    public void onMessage(String message) {
        LOGGER.info("WebSocket Message: "+message);
    }

    // public Tracker() throws Exception {
    // }
    // @OnOpen
    // public void onOpen(Session userSession) {
    //     LOGGER.info("opening websocket");
    //     this.userSession = userSession;
    // }
    // @OnClose
    // public void onClose(Session userSession, CloseReason reason) {
    //     LOGGER.info("closing websocket");
    //     this.userSession = null;
    // }


    // @Override
    // public void onMessage(String s) {
    //     final String message = s;
    //     runOnUiThread(new Runnable() {
    //         @Override
    //         public void run() {
    //             TextView textView = (TextView)findViewById(R.id.edittext_chatbox);
    //             textView.setText(textView.getText() + "\n" + message);
    //         }
    //     });
    // }
}
