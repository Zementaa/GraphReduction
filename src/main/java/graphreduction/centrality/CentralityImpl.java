package main.java.graphreduction.centrality;

import java.util.List;

import org.neo4j.driver.Driver;
import org.neo4j.driver.Record;
import org.neo4j.driver.Session;
import org.neo4j.driver.Values;

public abstract class CentralityImpl implements Centrality {
	private Driver driver;

	public CentralityImpl(Driver driver) {
		super();
		this.setDriver(driver);
	}

	public Driver getDriver() {
		return driver;
	}

	public void setDriver(Driver driver) {
		this.driver = driver;
	}

}
