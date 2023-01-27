package servers;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;

public class ServerInstance implements Runnable {
    private String SERVER_IP ;
    private Integer SERVER_PORT;

    // Connection DispatcherSynchronize-Server
    private ServerSocket serverSocket;
    private InputStreamReader inputStreamReader;
    private OutputStreamWriter outputStreamWriter;
    private BufferedReader serverInput;
    private BufferedWriter serverOutput;

    // Connection Server-RequestHandler
    private Socket serverRequestHandlerSocket;
    private BufferedReader serverRequestHandlerInput;
    private BufferedWriter serverRequestHandlerOutput;

    public ServerInstance(String serverIP, Integer serverPORT) {
        System.out.println("Server " +  serverIP + ":" + serverPORT + " is Running.");

        this.SERVER_IP = serverIP;
        this.SERVER_PORT = serverPORT;
        try {
            // Socket for Connection with the DispatcherSynchronize
            serverSocket = new ServerSocket(SERVER_PORT);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        while (true) {
            try {
                // Connection DispatcherSynchronize-Server
                Socket dispatcherSocket = serverSocket.accept();

                // Reader & Writer for the connection with the Dispatcher
                inputStreamReader = new InputStreamReader(dispatcherSocket.getInputStream());
                outputStreamWriter = new OutputStreamWriter(dispatcherSocket.getOutputStream());
                serverInput = new BufferedReader(inputStreamReader);
                serverOutput = new BufferedWriter(outputStreamWriter);

                // Receiving the Client's ID
                String clientID = serverInput.readLine();

                // Receiving the Action
                String action = serverInput.readLine();

                // Receiving the Number
                Integer number = null;
                if (action.equals("Write"))
                    number = Integer.parseInt(serverInput.readLine());

                // Receiving the RequestHandler IP
                String requestHandlerIP = serverInput.readLine();

                // Receiving the RequestHandler PORT
                Integer requestHandlerPORT = Integer.parseInt(serverInput.readLine());

                // Receiving the Signal
                String signal = serverInput.readLine();

                // Receiving the MainServerResponse
                String mainServerResponse = serverInput.readLine();

                // Processing the Request
                String fileName = "../data/" + SERVER_IP + "_" + SERVER_PORT + ".txt";
                File file = new File(fileName);                    
                file.getParentFile().mkdirs(); 
                file.createNewFile(); 

                String serverResponse = ""; // If Write
                ArrayList<String> serverListResponse = new ArrayList<String>(); // If Read                

                if (signal.equals("Main Server") && action.equals("Write")) {
                    boolean isPrime = true;
                    for (int i = 2; i < number; ++i) {
                        if (number % i == 0) {
                            isPrime = false;
                            break;
                        }
                    }
                    if (isPrime)
                        serverResponse = clientID + ": The number " + number + " IS PRIME.\n";
                    else
                        serverResponse = clientID + ": The number " + number + " IS NOT PRIME.\n";

                    // Write to File
                    FileWriter fileWriter = new FileWriter(file, true);
                    fileWriter.write(serverResponse);
                    fileWriter.close();
                }
                else if (signal.equals("Main Server") && action.equals("Read")){
                    // Read the File
                    Scanner fileScanner = new Scanner(file);

                    serverListResponse.add("::BEGIN OF REQUESTS::");
                    while (fileScanner.hasNextLine()) {
                        String line = fileScanner.nextLine();
                        String lineSplit[] = line.split(":", 2);
                        if (lineSplit[0].equals(clientID))
                            serverListResponse.add("|" + line + "|" + "");
                    }
                    serverListResponse.add("::END OF REQUESTS::");
                    fileScanner.close();
                }
                else if(!signal.equals("Main Server")) {
                    // Write to File
                    FileWriter fileWriter = new FileWriter(file, true);
                    fileWriter.write(mainServerResponse + "\n");
                    fileWriter.close();
                }

                if (signal.equals("Main Server")) {
                    if (action.equals("Write")) {
                        // Sending the Main Server Response
                        serverOutput.write(serverResponse);
                        serverOutput.newLine();
                        serverOutput.flush();
                    }

                    // Closing the DispatcherSynchronize-Server Connection
                    serverInput.close();
                    serverOutput.close();
                    dispatcherSocket.close();

                    // Establishing connection with the RequestHandler of the Client
                    serverRequestHandlerSocket = new Socket(requestHandlerIP, requestHandlerPORT);

                    // Reader & Writer for the connection with the RequestHandler
                    inputStreamReader = new InputStreamReader(serverRequestHandlerSocket.getInputStream());
                    outputStreamWriter = new OutputStreamWriter(serverRequestHandlerSocket.getOutputStream());
                    serverRequestHandlerInput = new BufferedReader(inputStreamReader);
                    serverRequestHandlerOutput = new BufferedWriter(outputStreamWriter);

                    // Sending the Response
                    if (action.equals("Write")) {
                        serverRequestHandlerOutput.write(serverResponse);
                        serverRequestHandlerOutput.newLine();
                        serverRequestHandlerOutput.flush();
                    }
                    else {
                        // Sending the Response Size
                        serverRequestHandlerOutput.write(String.valueOf(serverListResponse.size()));
                        serverRequestHandlerOutput.newLine();
                        serverRequestHandlerOutput.flush();
                        for (int i = 0; i < serverListResponse.size(); ++i) {
                            serverRequestHandlerOutput.write(serverListResponse.get(i));
                            serverRequestHandlerOutput.newLine();
                            serverRequestHandlerOutput.flush();
                        }
                    }

                    // Closing the Server-RequestHandler Connection
                    serverRequestHandlerInput.close();
                    serverRequestHandlerOutput.close();
                    serverRequestHandlerSocket.close();
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
