import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.function.Consumer;

public abstract class ClientNetwork {
	
	private ConnThread connthread = new ConnThread();
	private Consumer<Serializable> callback;
	
	public ClientNetwork(Consumer<Serializable> callback) {
		this.callback = callback;
		connthread.setDaemon(true);
	}
	
	// start the thread connthread
	public void startConn() throws Exception{
		connthread.start();
	}
	
	// send messages
	public void send(Serializable data) throws Exception{
		connthread.out.writeObject(data);
	}
	
	// close the socket in thread connthread
	public void closeConn() throws Exception{
		connthread.socket.close();
	}
	
	// abstract methods
	abstract protected boolean isServer();
	abstract protected String getIP();
	abstract protected int getPort();
	
	class ConnThread extends Thread{
		private Socket socket;
		private ObjectOutputStream out;
		
		public void run() {
			try{
				
				// client stuff
				Socket socket = new Socket(getIP(), getPort());
				ObjectOutputStream out = new ObjectOutputStream( socket.getOutputStream());
				ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
				
				this.socket = socket;
				this.out = out;
				socket.setTcpNoDelay(true);
				
				while(true) {
					Serializable data = (Serializable) in.readObject();
					callback.accept(data);
				}
			}
			catch(Exception e) {
				callback.accept("connection Closed");
			}
		}
	}
}