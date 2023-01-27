package clients;

public class Client {
    private static final String BALANCER_IP = "127.0.0.1";
    private static final int BALANCER_PORT = 5252;

    public static void main(String[] args) {
        System.out.println(":: CLIENTS ::");
        ClientInstance clientInstance1 = new ClientInstance(BALANCER_IP, BALANCER_PORT, "Client 1");
        ClientInstance clientInstance2 = new ClientInstance(BALANCER_IP, BALANCER_PORT, "Client 2");
        ClientInstance clientInstance3 = new ClientInstance(BALANCER_IP, BALANCER_PORT, "Client 3");

        Thread client1Thread = new Thread (clientInstance1);
        Thread client2Thread = new Thread (clientInstance2);
        Thread client3Thread = new Thread (clientInstance3);

        client1Thread.start();
        client2Thread.start();
        client3Thread.start();
    }
}
