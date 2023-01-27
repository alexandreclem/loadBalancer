package balancer;

import requests.Request;
import requests.RequestsQueue;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Objects;

public class DispatcherSynchronize implements Runnable {
    private String DISPATCHER_SYNCHRONIZE_IP ;
    private Integer DISPATCHER_SYNCHRONIZE_PORT;

    // Connection DispatcherSynchronize-Server
    private InputStreamReader inputStreamReader;
    private OutputStreamWriter outputStreamWriter;
    private Socket dispSyncServerSocket;
    private BufferedReader dispSyncServerInput;
    private BufferedWriter dispSyncServerOutput;

    private ArrayList<String> serversIP;
    private ArrayList<Integer> serversPORT;

    private RequestsQueue requestsQueue = new RequestsQueue();

    public DispatcherSynchronize(String dispatcherSynchronizeIP, Integer dispatcherSynchronizePORT, ArrayList<String> serversIP, ArrayList<Integer> serversPORT) {
        this.DISPATCHER_SYNCHRONIZE_IP = dispatcherSynchronizeIP;
        this.DISPATCHER_SYNCHRONIZE_PORT = dispatcherSynchronizePORT;
        this.serversIP = serversIP;
        this.serversPORT = serversPORT;
    }

    public String sendRequestToServer(Request request, String destServerIP, Integer destServerPORT, String signal, String mainServerResponse) {
        try {
            // Getting the Request Information
            String clientID = request.getClientID();
            String action = request.getAction();
            Integer number = request.getNumber();
            String requestHandlerIP = request.getRequestHandlerIP();
            Integer requestHandlerPORT = request.getRequestHandlerPORT();

            // Establishing connection with the Destination Server
            dispSyncServerSocket = new Socket(destServerIP, destServerPORT);

            // Reader & Writer for the connection with the Server
            inputStreamReader = new InputStreamReader(dispSyncServerSocket.getInputStream());
            outputStreamWriter = new OutputStreamWriter(dispSyncServerSocket.getOutputStream());
            dispSyncServerInput = new BufferedReader(inputStreamReader);
            dispSyncServerOutput = new BufferedWriter(outputStreamWriter);

            // Sending the Client's ID
            dispSyncServerOutput.write(clientID);
            dispSyncServerOutput.newLine();
            dispSyncServerOutput.flush();

            // Sending the Action
            dispSyncServerOutput.write(action);
            dispSyncServerOutput.newLine();
            dispSyncServerOutput.flush();

            // Sending the Number
            if (action.equals("Write")) {
                dispSyncServerOutput.write(String.valueOf(number));
                dispSyncServerOutput.newLine();
                dispSyncServerOutput.flush();
            }

            // Sending the RequestHandler IP
            dispSyncServerOutput.write(requestHandlerIP);
            dispSyncServerOutput.newLine();
            dispSyncServerOutput.flush();

            // Sending the RequestHandler PORT
            dispSyncServerOutput.write(String.valueOf(requestHandlerPORT));
            dispSyncServerOutput.newLine();
            dispSyncServerOutput.flush();

            // Sending the Signal to identify as the Main Server or not
            dispSyncServerOutput.write(signal);
            dispSyncServerOutput.newLine();
            dispSyncServerOutput.flush();

            // Sending the Main Server Response
            dispSyncServerOutput.write(mainServerResponse);
            dispSyncServerOutput.newLine();
            dispSyncServerOutput.flush();

            if (signal.equals("Main Server") && action.equals("Write"))
                mainServerResponse = dispSyncServerInput.readLine();

            // Closing the Server-Dispatcher Connection
            dispSyncServerInput.close();
            dispSyncServerOutput.close();
            dispSyncServerSocket.close();

        } catch(IOException e) {
            e.printStackTrace();
        }
        return mainServerResponse;
    }

    public void synchronize(Request request) {
        String mainServerIP = request.getServerIP();
        Integer mainServerPORT = request.getServerPort();
        String mainServerResponse = sendRequestToServer(request, mainServerIP, mainServerPORT, "Main Server", "");

        for (int i = 0; i < serversIP.size(); ++i) {
            if (!(serversIP.get(i).equals(mainServerIP) && Objects.equals(serversPORT.get(i), mainServerPORT)))
                sendRequestToServer(request, serversIP.get(i), serversPORT.get(i), "Not The Main Server", mainServerResponse);
        }
    }

    @Override
    public void run() {
        while (true) {
            if (requestsQueue.getSize() == 0)
                continue;

            Request request = requestsQueue.popRequest();
            if (request.getAction().equals("Write"))
                synchronize(request);
            else
                sendRequestToServer(request, request.getServerIP(), request.getServerPort(), "Main Server", "");
        }
    }
}
