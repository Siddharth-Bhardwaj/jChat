import server.Server;

public class StartServer {
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		new Thread(new Server()).start();
	}
}
