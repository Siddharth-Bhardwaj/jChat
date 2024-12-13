import client.Client;

public class StartClient {

	public static void main(String[] args) {
		new Thread(new Client()).start();
	}

}
