package master;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;

public class ClientMaster extends Thread {
	
	private String ip;
	private int port;
	private String name;
	
	private Socket s;
	private OutputStream out;
	private DataOutputStream dos;
	private InputStream in;
	private DataInputStream dis;
	
	private boolean running;
	
	private ArrayList<ConnectedUser> ccServerUsers;
	
	public ClientMaster(String ip, int port){
		this.ip = ip;
		this.port = port;
	}
	/**
	 * Constructor for developing
	 */
	public ClientMaster(){
		this.ip = "localhost";
		this.port = 1999;
		this.name = "John Doe";
	}
	
	public void run(){
		//Start with setting up the connection
		initializeClient();
		//Continue with the handshake!
		running = doHandShake();
		
		byte packageID;
		
		//Continue listening for packages from the ccserver
		try {
			while(running && ((packageID = dis.readByte()) != -1)){
				switch(packageID){
					//Case 1 reserved for handshake
					//Heartbeat return
					case 2:
						// TODO
						//System.out.println("Heartbeat confirmed by server");
						break;
					//Master wants the slave to connect to him/her.
					case 3:
						// TODO
						//System.out.println("CCServer want you to connect to this master");
						break;
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}	
	
	private ArrayList<ConnectedUser> getConnectedList(){
		ArrayList<ConnectedUser> tempList = new ArrayList<ConnectedUser>();
		try {
			//Get how many rows the server will send
			int numberOfClients = dis.readInt();
			//Now read in all the users
			for(int i = 0; i < numberOfClients; i++){
				//Get the information and add it to the arraylist.
				tempList.add(new ConnectedUser(dis.readInt(), readString(), dis.readBoolean(), dis.readBoolean()));
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return tempList;
	}
	
	private boolean doHandShake(){
		byte packageID;
		byte content;
		
		//Star by sending the first Hello to the server
		try {
			//PackageID = 1
			dos.writeByte(1);
			//Content = 1
			dos.writeByte(1);
			//Get the answer from the server
			packageID = dis.readByte();
			content = dis.readByte();
			//Lets check that packageID = 1 and content = 2
			if(packageID == 1 && content == 2){
				//We are connected to a ccserver
				//Yes, continue sending the final answer to the server with our name
				//PackageID = 1
				dos.writeByte(1);
				writeString(name);
				return true;
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}
	/**
	 * Internal method that create the connection to the server and 
	 * creates all the streams.
	 */
	private void initializeClient(){
		running = true;	
		try {
			s = new Socket(ip,port);
			
			out = s.getOutputStream();
			dos = new DataOutputStream(out);
			
			in = s.getInputStream();
			dis = new DataInputStream(in);
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	private void writeString(String toWrite){
		try {
			byte[] data = toWrite.getBytes("UTF-8");
			dos.writeInt(data.length);
			dos.write(data);
		} catch (UnsupportedEncodingException e) {
			//parent.log.printlnErr("Wrong enconding on String in writeString()");
			e.printStackTrace();
		} catch (IOException e) {
			//parent.log.printlnErr("IO exception trying to send string over DataOutputStream");
			e.printStackTrace();
		}
	}
	private String readString(){
		try {
			int length = dis.readInt();
			byte data[] = new byte[length];
			dis.readFully(data);
			return new String(data,"UTF-8");
		} catch (IOException e) {
			//parent.log.printlnErr("IO exception trying to read string from DataInputStream");
			e.printStackTrace();
			return null;
		}
	}
	public static void main(String[] args) {
		new Thread(new ClientMaster()).start();
	}

}
