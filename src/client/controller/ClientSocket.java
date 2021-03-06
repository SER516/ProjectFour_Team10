package client.controller;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Logger;
import client.model.Parameters;
import javax.websocket.*;

import org.codehaus.jettison.json.JSONException;

import client.service.*;

@ClientEndpoint (
    configurator=ClientConfigurator.class,
    decoders=JsonDecoder.class
)

/**
 * @author Group10
 * @version 1.0
 * ClientSocket class: Create & maintain connection between 
 * 					   client and server
 */
public class ClientSocket extends Thread {

    private Logger logger = Logger.getLogger(ClientSocket.class.getName());
    private ClientWindowController ctrl;
    public static Parameters param;

    static {
        param = new Parameters();
    }

    private String address;
    private int port;
    private String context;
    private String page;

    /**
     * Initialize client socket
     * @param ctrl Client window controller object
     * @param address IP address of client
     * @param port Port number for client
     * @param context Context for client
     * @param page Page string for client
     */
    public ClientSocket(ClientWindowController ctrl, String address, int port,
    						String context, String page) {
    	this.ctrl = ctrl;
    	this.address = address;
    	this.port = port;
    	this.context = context;
    	this.page = page;
    }

    /**
     * Log information when open is called
     * @param session
     */
    @OnOpen
    public void open(Session session) {
        logger.info("Client webSocket is opening ...");
        this.ctrl.setStatus(2, 0.0);
    }

    /**
     * Call when receive message
     * @param message : Message to send
     * @param session : session object to maintain connection
     * @throws JSONException
     * @throws IOException
     */
    @OnMessage
    public void onMessage(Parameters message, Session session) throws 
    				JSONException, IOException {
        param.setEye(message.getEye());
        param.setLowerFace(message.getLowerFace());
        param.setUpperFace(message.getUpperFace());
        param.setPerformance(message.getPerformance());
        param.setTime(message.getTime());
        ctrl.update(param);
    }

    /**
     * Log information when session is closed
     */
    @OnClose
    public void onClose() {
        logger.info("WebSocket closed");
        ctrl.setStatus(0, 0.0);
    	MessageBox msgBox = new MessageBox();
    	msgBox.windowPop("Connection closed");
    }

    /**
     * Call when meet error
     * @param session : connection between client and server
     * @param t : throwable to handle exceptions
     */
    @OnError
    public void onError(Session session, Throwable t) {
    	ctrl.setStatus(0, 0.0);
    	MessageBox msgBox = new MessageBox();
    	msgBox.windowPop("Connection error");
        t.printStackTrace();
    }

    /**
     * Async run connect
     * This is main method for client thread which will create
     * websocket to connect to server
     */
    public void run() {
    	CountDownLatch latch = new CountDownLatch(1);
        ctrl.container = ContainerProvider.getWebSocketContainer();
        String uri = "ws://" + address + ":" + port + "/" + 
        				context + "/" + page;
        try {
            ctrl.container.connectToServer(this, new URI(uri));
            latch.await();
        } catch (DeploymentException | URISyntaxException | 
        		InterruptedException | IOException e) {
        	ctrl.setStatus(0, 0.0);
            throw new RuntimeException(e);
        }
    }
}
