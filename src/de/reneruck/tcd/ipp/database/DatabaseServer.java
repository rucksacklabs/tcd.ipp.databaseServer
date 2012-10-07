package de.reneruck.tcd.ipp.database;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

import de.reneruck.tcd.datamodel.Statics;
import de.reneruck.tcd.datamodel.Transition;

public class DatabaseServer extends Thread {

	private boolean running = false;
	private ServerSocket socket;
	private Queue<Transition> dbQueue = new LinkedBlockingQueue<Transition>(new LinkedList<Transition>());
	private DatabaseQueryHandler queryHandler;
	private List<Connection> connections = new ArrayList<Connection>();

	public DatabaseServer() {
		this.queryHandler = new DatabaseQueryHandler(this.dbQueue);
		this.queryHandler.setRunning(true);
		this.queryHandler.start();
	}
	
	@Override
	public void run() {
		while (this.running) {
			try {
				if (this.socket != null && this.socket.isBound()) {
					readFromSocket();
				} else {
					bind();
				}
				Thread.sleep(500);
			} catch (IOException e) {
				e.fillInStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	private void readFromSocket() {

		try {
			System.out.println("waiting for incoming connection");
			Socket connection = this.socket.accept();
			System.out.println("Connection received from " + connection.getInetAddress().getHostName());
			Connection conn = new Connection(connection);
			conn.start();
			this.connections.add(conn);
		} catch (IOException e) {
			System.err.println("Cannot read from channel " + e.getMessage());
		}
	}

	private void bind() throws IOException {
		this.socket = new ServerSocket(Statics.DB_SERVER_PORT, 10);
	}

	public boolean isRunning() {
		return running;
	}

	public void setRunning(boolean running) {
		this.running = running;
	}

}