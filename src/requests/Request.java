package requests;

public class Request {
		private final String clientID;
		private final String action;
		private final Integer number;
		private final String requestHandlerIP;
		private final Integer requestHandlerPORT;
		private final String serverIP;
		private final Integer serverPORT;

		public Request(String clientID, String action, Integer number, String requestHandlerIP, Integer requestHandlerPORT, String serverIP, Integer serverPort) {
			this.clientID = clientID;
			this.action = action;
			this.number = number;
			this.requestHandlerIP = requestHandlerIP;
			this.requestHandlerPORT = requestHandlerPORT;
			this.serverIP = serverIP;
			this.serverPORT = serverPort;
		}

		public String getClientID() {
			return clientID;
		}

		public String getAction() {
			return action;
		}

		public Integer getNumber() {
			return number;
		}

		public String getRequestHandlerIP() {
			return requestHandlerIP;
		}

		public Integer getRequestHandlerPORT() {
			return requestHandlerPORT;
		}

		public String getServerIP() {
		return serverIP;
	}

		public Integer getServerPort() {
			return serverPORT;
		}

		public String getRequest()
		{
			String str;
			str = String.format("|%s|%s|%d|%s|%d|%s|%d|", clientID, action, number, requestHandlerIP, requestHandlerPORT, serverIP, serverPORT);
			return str;
		}
}