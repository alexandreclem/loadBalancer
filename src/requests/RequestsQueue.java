package requests;

import java.util.concurrent.ConcurrentLinkedQueue;

public class RequestsQueue {
    private static ConcurrentLinkedQueue<Request> requestsQueue = new ConcurrentLinkedQueue<Request>();

    public RequestsQueue() {

    }

    public void addRequest(Request request) {
        requestsQueue.offer(request);
    }

    public int getSize() {
        return requestsQueue.size();
    }

    public Request popRequest() {
        return requestsQueue.poll();
    }
}
