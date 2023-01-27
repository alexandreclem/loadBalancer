package clients;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Random;

public class ClientInstance implements Runnable {
    private String clientID;

    private Socket clientSocket;
    private InputStreamReader inputStreamReader;
    private OutputStreamWriter outputStreamWriter;
    private BufferedReader clientInput;
    private BufferedWriter clientOutput;

    private final Integer SLEEP_TIME = 2500;

    public ClientInstance(String balancerIP, int balancerPORT, String clientID) {
        this.clientID = clientID;
        try {
            // Creating the Client-RequestHandler(Via Balancer) Connection
            this.clientSocket = new Socket(balancerIP, balancerPORT);

            // Reader & Writer for the connection with the RequestHandler
            this.inputStreamReader = new InputStreamReader(clientSocket.getInputStream());
            this.outputStreamWriter = new OutputStreamWriter(clientSocket.getOutputStream());
            this.clientInput = new BufferedReader(inputStreamReader);
            this.clientOutput = new BufferedWriter(outputStreamWriter);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /*
        Example:
            readRationalProb = "3/4"
            (Read) numerator = 3
            (Write) denominator = 4
            (Read) Even numbers = 3
            (Write) Odd numbers = 4 - 3 = 1
            arr = [0, 2, 4, 1]
    */
    public String actionSelector(String readRationalProb) {

        String[] ratio = readRationalProb.split("/");
        int numerator = Integer.parseInt(ratio[0]);
        int denominator = Integer.parseInt(ratio[1]);

        ArrayList<Integer> arr = new ArrayList<>();
        // Array receive 'numerator' even numbers.
        for (int i = 0; i < numerator; ++i)
            arr.add(2*i);

        // Array receive 'denominator' - 'numerator' (what's remaining) odd numbers.
        for (int i = 0; i < denominator - numerator; ++i)
            arr.add(2*i + 1);

        int index = new Random().nextInt(denominator);
        if (arr.get(index) % 2 == 0)
             return "Read";
        else
            return "Write";
    }

    public int numberSelector(int min, int max) {
        return new Random().nextInt(max - min + 1) + min;
    }

    @Override
    public void run() {
        while (true) {
            try {
                Thread.sleep(SLEEP_TIME);
                // Sending the Client's ID.
                clientOutput.write(clientID);
                clientOutput.newLine();
                clientOutput.flush();

                // Sending the Action
                String action = actionSelector("3/4");
                clientOutput.write(action);
                clientOutput.newLine();
                clientOutput.flush();

                // Sending the Random Number
                Integer randomNumber = null;
                if (action.equals("Write")) {
                    randomNumber = numberSelector(2, 1000000);
                    clientOutput.write(String.valueOf(randomNumber));
                    clientOutput.newLine();
                    clientOutput.flush();
                }

                // Server Response
                String response = "";
                StringBuilder sb = new StringBuilder();
                if (action.equals("Write")) {
                    response = clientInput.readLine();
                }
                else {
                    Integer size = Integer.parseInt(clientInput.readLine());
                    for (int i = 0; i < size; ++i) {
                        response = clientInput.readLine();
                        response = response + "\n";
                        sb.append(response);
                    }
                    response = sb.toString();
                }
                System.out.println(response);

            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
