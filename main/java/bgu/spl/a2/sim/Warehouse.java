package bgu.spl.a2.sim;

import bgu.spl.a2.sim.tools.GcdScrewDriver;
import bgu.spl.a2.sim.tools.NextPrimeHammer;
import bgu.spl.a2.sim.tools.RandomSumPliers;
import bgu.spl.a2.sim.tools.Tool;
import bgu.spl.a2.sim.conf.ManufactoringPlan;
import bgu.spl.a2.Deferred;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A class representing the warehouse in your simulation
 *
 * Note for implementors: you may add methods and synchronize any of the
 * existing methods in this class *BUT* you must be able to explain why the
 * synchronization is needed. In addition, the methods you add to this class can
 * only be private!!!
 *
 */
// TODO: 30/12/2016 check private, check data structure, check sync
public class Warehouse {
	AtomicInteger gsDriverCounter;
	AtomicInteger rsPliersCounter;
	AtomicInteger npHammerCounter;

	GcdScrewDriver gsDriver;
	RandomSumPliers rsPliers;
	NextPrimeHammer npHammer;

	ConcurrentLinkedQueue<Deferred<Tool>> gsDriverQueue;
	ConcurrentLinkedQueue<Deferred<Tool>> rsPliersQueue;
	ConcurrentLinkedQueue<Deferred<Tool>> npHammerQueue;

	ArrayList<ManufactoringPlan> plans;


	/**
	 * Constructor
	 */
	public Warehouse() {
		gsDriverCounter = new AtomicInteger(0);
		rsPliersCounter = new AtomicInteger(0);
		npHammerCounter = new AtomicInteger(0);

		gsDriver = new GcdScrewDriver();
		rsPliers = new RandomSumPliers();
		npHammer = new NextPrimeHammer();

		gsDriverQueue = new ConcurrentLinkedQueue<Deferred<Tool>>();
		rsPliersQueue = new ConcurrentLinkedQueue<Deferred<Tool>>();
		npHammerQueue = new ConcurrentLinkedQueue<Deferred<Tool>>();

		plans = new ArrayList<ManufactoringPlan>();

	}

	/**
	 * Tool acquisition procedure
	 * Note that this procedure is non-blocking and should return immediatly
	 *
	 * @param type - string describing the required tool
	 * @return a deferred promise for the  requested tool
	 */

	// acquireTool must be sync in order to make sure that a tool is acquired correctly by one thread
	public synchronized Deferred<Tool> acquireTool(String type) {
		Deferred<Tool> toolDef = new Deferred<Tool>();
		switch (type) {
			case "rs-pliers":
				if(rsPliersCounter.get()>0){
					rsPliersCounter.decrementAndGet();
					toolDef.resolve(rsPliers);
				}
				else{
					rsPliersQueue.add(toolDef);
				}
				return toolDef;

			case "gs-driver":
				if(gsDriverCounter.get()>0){
					gsDriverCounter.decrementAndGet();
					toolDef.resolve(gsDriver);
				}
				else{
					gsDriverQueue.add(toolDef);
				}
				return toolDef;

			case "np-hammer":
				if(npHammerCounter.get()>0){
					npHammerCounter.decrementAndGet();
					toolDef.resolve(npHammer);
				}
				else{
					npHammerQueue.add(toolDef);
				}
				return toolDef;
		}
		return toolDef;
	}


	/**
	 * Tool return procedure - releases a tool which becomes available in the warehouse upon completion.
	 *
	 * @param tool - The tool to be returned
	 */

	// releaseTool must be sync in order to make sure that a tool is acquired correctly by one thread
	public synchronized void releaseTool(Tool tool) {
		String type = tool.getType();
		Deferred<Tool> toolDef;
		switch (type) {
			case "rs-pliers":
				rsPliersCounter.incrementAndGet();
				toolDef = rsPliersQueue.poll();
				if(toolDef != null) {
					toolDef.resolve(rsPliers);
				}
				break;

			case "gs-driver":
				gsDriverCounter.incrementAndGet();
				toolDef = gsDriverQueue.poll();
				if(toolDef != null){
					toolDef.resolve(gsDriver);
				}
				break;

			case "np-hammer":
				npHammerCounter.incrementAndGet();
				toolDef = npHammerQueue.poll();
				if(toolDef != null) {
					toolDef.resolve(npHammer);
				}
				break;
		}
	}


	/**
	 * Getter for ManufactoringPlans
	 *
	 * @param product - a string with the product name for which a ManufactoringPlan is desired
	 * @return A ManufactoringPlan for product
	 */
	public ManufactoringPlan getPlan(String product) {
		for (ManufactoringPlan plan: plans) {
			if (plan.getProductName().equals(product)){
				return plan;
			}
		}
		return null;
	}

	/**
	 * Store a ManufactoringPlan in the warehouse for later retrieval
	 *
	 * @param plan - a ManufactoringPlan to be stored
	 */
	public void addPlan(ManufactoringPlan plan) {
		plans.add(plan);
	}


	/**
	 * Store a qty Amount of tools of type tool in the warehouse for later retrieval
	 *
	 * @param tool - type of tool to be stored
	 * @param qty  - amount of tools of type tool to be stored
	 */
	public void addTool(Tool tool, int qty) {
		String type = tool.getType();

		switch (type) {
			case "rs-pliers":
				rsPliersCounter.set(qty);
				break;

			case "gs-driver":
				gsDriverCounter.set(qty);
				break;

			case "np-hammer":
				npHammerCounter.set(qty);
				break;
		}

	}

}
