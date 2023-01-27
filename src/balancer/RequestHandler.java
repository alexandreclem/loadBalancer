package balancer;

import requests.RequestsQueue;
import requests.Request;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Random;

public class RequestHandler implements Runnable {
    private final String REQUEST_HANDLER_IP;
    private final Integer REQUEST_HANDLER_PORT;

    private InputStreamReader inputStreamReader;
    private OutputStreamWriter outputStreamWriter;

    // Connection Client-RequestHandler
    private Socket clientSocket;
    private BufferedReader requestHandlerClientInput;
    private BufferedWriter requestHandlerClientOutput;

    // Connection Server-RequestHandler
    private ServerSocket requestHandlerSocket;
    private BufferedReader requestHandlerServerInput;
    private BufferedWriter requestHandlerServerOutput;

    // List of Servers
    private ArrayList<String> serversIP = new ArrayList<>();
    private ArrayList<Integer> serversPORT = new ArrayList<>();

    private RequestsQueue requestsQueue = new RequestsQueue();

    public RequestHandler(String requestHandlerIP, Integer requestHandlerPORT, Socket clientSocket, ArrayList<String> serversIP , ArrayList<Integer> serversPORT) {
        this.REQUEST_HANDLER_IP = requestHandlerIP;
        this.REQUEST_HANDLER_PORT = requestHandlerPORT;
        this.clientSocket = clientSocket;
        this.serversIP = serversIP;
        this.serversPORT = serversPORT;

        try {
            // Socket to Wait for Connections From Servers
            requestHandlerSocket = new ServerSocket(REQUEST_HANDLER_PORT);

            // Reader & Writer for the connection with the Client
            inputStreamReader = new InputStreamReader(clientSocket.getInputStream());
            outputStreamWriter = new OutputStreamWriter(clientSocket.getOutputStream());
            requestHandlerClientInput = new BufferedReader(inputStreamReader);
            requestHandlerClientOutput = new BufferedWriter(outputStreamWriter);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        int circularSelection = new Random().nextInt(serversIP.size());
        while (true) {
            // Receiving Requests from Client
            try {
                // Receiving the Client's ID
                String clientID = requestHandlerClientInput.readLine();

                // Receiving the Client's Action
                String action = requestHandlerClientInput.readLine();

                // Receiving the Client's Number
                Integer number = null;
                if (action.equals("Write"))
                     number = Integer.parseInt(requestHandlerClientInput.readLine());

                // Selecting the Server
                if (circularSelection == serversIP.size())
                    circularSelection = 0;
                String luckyServerIP = serversIP.get(circularSelection % serversIP.size());
                int luckyServerPORT = serversPORT.get(circularSelection % serversPORT.size());

                // Adding the Request to the Queue
                Request request = new Request(clientID, action, number, REQUEST_HANDLER_IP, REQUEST_HANDLER_PORT, luckyServerIP, luckyServerPORT);
                requestsQueue.addRequest(request);

                System.out.println(request.getRequest());

                // Connection With the Server Defined by the Dispatcher
                Socket serverSocket = requestHandlerSocket.accept();

                // Reader & Writer for the connection with the Server
                inputStreamReader = new InputStreamReader(serverSocket.getInputStream());
                outputStreamWriter = new OutputStreamWriter(serverSocket.getOutputStream());
                requestHandlerServerInput = new BufferedReader(inputStreamReader);
                requestHandlerServerOutput = new BufferedWriter(outputStreamWriter);

                // Receiving the Server's Response & Forwarding to the Client
                String serverResponse;
                if (action.equals("Write")) {
                    serverResponse = requestHandlerServerInput.readLine();
                    requestHandlerClientOutput.write(serverResponse);
                    requestHandlerClientOutput.newLine();
                    requestHandlerClientOutput.flush();
                }
                else {
                    // Sending the size to the Client
                    Integer size = Integer.parseInt(requestHandlerServerInput.readLine());
                    requestHandlerClientOutput.write(String.valueOf(size));
                    requestHandlerClientOutput.newLine();
                    requestHandlerClientOutput.flush();
                    for (int i = 0; i < size; ++i) {
                        serverResponse = requestHandlerServerInput.readLine();
                        requestHandlerClientOutput.write(serverResponse);
                        requestHandlerClientOutput.newLine();
                        requestHandlerClientOutput.flush();
                    }
                }

                // Closing the Server-RequestHandler Connection
                requestHandlerServerInput.close();
                requestHandlerServerOutput.close();
                serverSocket.close();

                circularSelection = circularSelection + 1;

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
