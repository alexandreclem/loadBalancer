package servers;

public class Server {
    private static final String SERVER_IP_1 = "127.0.0.1";
    private static final Integer SERVER_PORT_1 = 4040;
    private static final String SERVER_IP_2 = "127.0.0.1";
    private static final Integer SERVER_PORT_2 = 4141;
    private static final String SERVER_IP_3 = "127.0.0.1";
    private static final Integer SERVER_PORT_3 = 4242;

    public static void main(String[] args) {
        System.out.println(":: SERVERS ::");
        ServerInstance serverInstance1 = new ServerInstance(SERVER_IP_1, SERVER_PORT_1);
        ServerInstance serverInstance2 = new ServerInstance(SERVER_IP_2, SERVER_PORT_2);
        ServerInstance serverInstance3 = new ServerInstance(SERVER_IP_3, SERVER_PORT_3);

        Thread server1Thread = new Thread (serverInstance1);
        Thread server2Thread = new Thread (serverInstance2);
        Thread server3Thread = new Thread (serverInstance3);

        server1Thread.start();
        server2Thread.start();
        server3Thread.start();
    }
}
