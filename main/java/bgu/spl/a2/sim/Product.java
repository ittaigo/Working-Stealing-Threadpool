package bgu.spl.a2.sim;

import bgu.spl.a2.sim.tools.Tool;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * A class that represents a product produced during the simulation.
 */
public class Product implements Serializable{
	long startId;
	long finalId;
	String name;
	private ArrayList<Product> productslist;
	private ArrayList<Tool> toolsList;


	public ArrayList<Tool> getTools() {
		return toolsList;
	}

	/**
	 * Constructor
	 *
	 * @param startId - Product start id
	 * @param name    - Product name
	 */
	public Product(long startId, String name) {
		this.name = name;
		this.startId = startId;
		finalId = startId;
		productslist = new ArrayList<Product>();
		toolsList = new ArrayList<Tool>();

	}

	/**
	 * @return The product name as a string
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return The product start ID as a long. start ID should never be changed.
	 */
	public long getStartId() {
		return startId;
	}

	/**
	 * @return The product final ID as a long.
	 * final ID is the ID the product received as the sum of all UseOn();
	 */
	public long getFinalId() {
		return finalId;
	}

	/**
	 * @return Returns all parts of this product as a List of Products
	 */
	public List<Product> getParts() {
		return productslist;
	}


	/**
	 * Add a new part to the product
	 *
	 * @param p - part to be added as a Product object
	 */
	public void addPart(Product p) {
		productslist.add(p);
	}

	public void addToFinalId(long id){
		finalId += id;
	}


}
