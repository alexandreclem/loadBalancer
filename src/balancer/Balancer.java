package balancer;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class Balancer {
    private static ServerSocket balancerSocket;
    private static final String BALANCER_IP = "127.0.0.1";
    private static final Integer BALANCER_PORT = 5252;

    // List of Servers
    private static final String SERVER_IP_1 = "127.0.0.1";
    private static final Integer SERVER_PORT_1 = 4040;
    private static final String SERVER_IP_2 = "127.0.0.1";
    private static final Integer SERVER_PORT_2 = 4141;
    private static final String SERVER_IP_3 = "127.0.0.1";
    private static final Integer SERVER_PORT_3 = 4242;

    private static final ArrayList<String> serversIP = new ArrayList<>();
    private static final ArrayList<Integer> serversPORT = new ArrayList<>();

    public static void main(String[] args) {
        System.out.println(":: BALANCER ::");

        // Putting the Server's IP/Port on their respective lists.
        serversIP.add(SERVER_IP_1);
        serversIP.add(SERVER_IP_2);
        serversIP.add(SERVER_IP_3);
        serversPORT.add(SERVER_PORT_1);
        serversPORT.add(SERVER_PORT_2);
        serversPORT.add(SERVER_PORT_3);

        // Creating the DispatcherSynchronize Thread.
        String dispatcherSynchronizeIP = "127.0.0.1";
        Integer dispatcherSynchronizePORT = 6600;
        DispatcherSynchronize dispatcherSynchronize = new DispatcherSynchronize(dispatcherSynchronizeIP, dispatcherSynchronizePORT, serversIP, serversPORT);
        Thread dispatcherSynchronizeThread = new Thread(dispatcherSynchronize);
        dispatcherSynchronizeThread.start();

        // Establishing Connection Balancer-Client & Creating the RequestHandler Thread to each Client
        try{
            balancerSocket = new ServerSocket(BALANCER_PORT);
        } catch (IOException e) {
            e.printStackTrace();
        }

        String requestHandlerIP = "127.0.0.1";
        Integer requestHandlerPORT = 5500;
        Integer clientIndex = 1;
        while (true) {
            try {
                Socket clientSocket = balancerSocket.accept();
                System.out.printf("Client %d Connected To RequestHandler %s:%d.\n", clientIndex, requestHandlerIP, requestHandlerPORT);

                RequestHandler clientRequestHandler = new RequestHandler(requestHandlerIP, requestHandlerPORT, clientSocket, serversIP, serversPORT);
                Thread clientRequestHandlerThread  = new Thread(clientRequestHandler);
                clientRequestHandlerThread.start();

                requestHandlerPORT = requestHandlerPORT + 1;
                clientIndex = clientIndex + 1;

            } catch (IOException e) {
                    e.printStackTrace();
            }
        }
    }
}