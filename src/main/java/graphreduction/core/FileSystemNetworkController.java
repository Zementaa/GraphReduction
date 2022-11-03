package main.java.graphreduction.core;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.neo4j.driver.Record;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;

/**
 * The filesystem and network controller is in charge of all the operations containing file system and network.
 * This includes opening a list, export to csv and checking if the socket is alive .
 *
 * @author Catherine Camier
 * @version 0.1.0
 */
public class FileSystemNetworkController {
	
	final Logger logger = LogManager.getLogger();

	/**
	 * Opens a list from the specified path to the file that contains the nodes that should be marked.
	 *
	 * @param pathToFile Path to the file
	 * @return List with the marked nodes
	 */
	public List<String> openList(String pathToFile){
		List<String> nodes = new ArrayList<>();
		try (BufferedReader br = new BufferedReader(new FileReader(pathToFile))) {
			String line;
			while ((line = br.readLine()) != null) {
				String value = line.split(",")[0];
				nodes.add(value);
			}
		} catch (FileNotFoundException e) {
			logger.info("FileNotFoundException - {}", e.getMessage());
		} catch (IOException e) {
			logger.info("IOException - {}", e.getMessage());
		}
		return nodes;
	}

	/**
	 * Exports the list of records to a csv-file with the specified name.
	 *
	 * @param records List of records
	 * @param fileName Name of the file that is created
	 */
	public void exportToCSV(List<Record> records, String fileName){
		long timestamp = System.currentTimeMillis();
		File csvOutputFile = new File("output/" + fileName + "/" + fileName + timestamp + ".csv");

		File directory = new File("output/" + fileName);
		if (! directory.exists()){
			directory.mkdir();
		}
		try (PrintWriter pw = new PrintWriter(csvOutputFile)) {
			logger.info(records);
			pw.println("name;score;marked;communityId;occur;relationships");
			records.stream().forEach(rec ->
				pw.println( rec.get("name") + ";" + rec.get("score").toString().replace(".", ",") + ";" + rec.get("marked") + ";" + rec.get("communityId") + ";" + rec.get("occur") + ";" + rec.get("relationships"))
			);
		}
		catch (FileNotFoundException e) {
			logger.info("FileNotFoundException - {}", e.getMessage());
		}
	}

	/**
	 * Checks if the socket is alive.
	 * <p>
	 * Neo4j needs to be running to execute any of the algorithms. This method checks, if the specified socket
	 * is alive.
	 *
	 * @param hostName the host name on which neo4j is running. Usually localhost.
	 * @param port the port that is used for the neo4j browser
	 * @return The boolean isAlive
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
			logger.info("SocketTimeoutException {}:{}. {}", hostName, port, exception.getMessage());
		} catch (IOException exception) {
			logger.info(
					"IOException - Unable to connect to {}:{}. {}", hostName, port, exception.getMessage());
		} 
		return isAlive;
	}
}
