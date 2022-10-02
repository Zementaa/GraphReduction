package main.java.graphreduction.core;

import org.neo4j.driver.Record;

import java.awt.*;
import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class FileSystemNetworkController {

	public List<String> openList(String pathToFile){
		List<String> nodes = new ArrayList<>();
		try (BufferedReader br = new BufferedReader(new FileReader(pathToFile))) {
			String line;
			while ((line = br.readLine()) != null) {
				String value = line.split(",")[0];
				nodes.add(value);
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return nodes;
	}

	public void exportToCSV(List<Record> records, String fileName){
		long timestamp = System.currentTimeMillis();
		File csvOutputFile = new File("output/" + fileName + "/" + fileName + timestamp + ".csv");

		File directory = new File("output/" + fileName);
		if (! directory.exists()){
			directory.mkdir();
		}
		try (PrintWriter pw = new PrintWriter(csvOutputFile)) {
			System.out.println(records);
			System.out.println(records.size());
			pw.println("name;score;marked;communityId;occur;relationships");
			records.stream().forEach(record -> {
				pw.println( record.get("name") + ";" + record.get("score").toString().replace(".", ",") + ";" + record.get("marked") + ";" + record.get("communityId") + ";" + record.get("occur") + ";" + record.get("relationships"));
			});
		}
		catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		assertTrue(csvOutputFile.exists());
	}

	/**
	 * Checks if the socket is alive.
	 * <p>
	 * Neo4j needs to be running to execute any of the algorithms. This method checks, if the specified socket
	 * is alive.
	 *
	 * @param hostName the host name on which neo4j is running. Usually localhost.
	 * @param port the port that is used for the neo4j browser
	 * @return isAlive
	 * @see Image
	 */
	public Boolean isSocketAlive(String hostName, int port) {
		boolean isAlive = false;

		SocketAddress socketAddress = new InetSocketAddress(hostName, port);
		Socket socket = new Socket();

		int timeout = 2000;

		try {
			socket.connect(socketAddress, timeout);
			socket.close();
			isAlive = true;

		} catch (SocketTimeoutException exception) {
			System.out.println("SocketTimeoutException " + hostName + ":" + port + ". " + exception.getMessage());
		} catch (IOException exception) {
			System.out.println(
					"IOException - Unable to connect to " + hostName + ":" + port + ". " + exception.getMessage());

		}
		return isAlive;
	}
}
