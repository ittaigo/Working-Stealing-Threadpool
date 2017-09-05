/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bgu.spl.a2.sim;

import bgu.spl.a2.Deferred;
import bgu.spl.a2.WorkStealingThreadPool;
import bgu.spl.a2.sim.conf.ManufactoringPlan;
import bgu.spl.a2.sim.tasks.ManufactoringTask;
import bgu.spl.a2.sim.tools.GcdScrewDriver;
import bgu.spl.a2.sim.tools.NextPrimeHammer;
import bgu.spl.a2.sim.tools.RandomSumPliers;
import com.google.gson.Gson;

import java.io.*;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * A class describing the simulator for part 2 of the assignment
 */
public class Simulator {
    static GsonSource source;
    static Warehouse warehouse;
    static ArrayList<ManufactoringPlan> plans;
    private static GsonSource.GProduct[][] waves;
    private  static WorkStealingThreadPool pool;

    /**
     * Begin the simulation
     * Should not be called before attachWorkStealingThreadPool()
     */
    public static ConcurrentLinkedQueue<Product> start() throws InterruptedException {

        pool.start();

        AtomicInteger waveCounter=new AtomicInteger(0);
        AtomicBoolean isRunning=new AtomicBoolean(true);
        int taskCounter=0;

        ConcurrentLinkedQueue<Product> productsQueue = new ConcurrentLinkedQueue<>();

        while(waveCounter.get()<waves.length){
            AtomicInteger productQty=new AtomicInteger(0);
            if(isRunning.get()) {
                isRunning.set(false);
                if (waveCounter.get() < waves.length) {

                    for (GsonSource.GProduct product : waves[waveCounter.get()]) {
                        productQty.set(productQty.get()+product.qty);

                        for (int i = 0; i < product.qty; i++) {
                            Product newProduct = new Product(product.startId + i, product.product);
                            ManufactoringTask task = new ManufactoringTask(newProduct);
                            Deferred taskDef = task.getResult();
                            taskDef.whenResolved(() -> {
                                if (productQty.decrementAndGet() == 0) {
                                    waveCounter.incrementAndGet();
                                    isRunning.set(true);
                                }
                            });

                            pool.submit(task);
                            productsQueue.add(newProduct);
                        }
                    }
                }
            }
        }

        return productsQueue;
    }
    /**
     * attach a WorkStealingThreadPool to the Simulator, this WorkStealingThreadPool will be used to run the simulation
     *
     * @param myWorkStealingThreadPool - the WorkStealingThreadPool which will be used by the simulator
     */
    public static void attachWorkStealingThreadPool(WorkStealingThreadPool myWorkStealingThreadPool) {
        pool = myWorkStealingThreadPool;
    }

    public static Warehouse getWarehouse(){
        return warehouse;
    }

    public static void main(String[] args) {

        //file check
        if(args.length==0){
            System.out.println("cant find file");
        }

        //json
        Gson json = new Gson();
        try (BufferedReader Buffer = new BufferedReader(new FileReader(args[0]));){
            source = json.fromJson(Buffer, GsonSource.class);



            //poll
            attachWorkStealingThreadPool(new WorkStealingThreadPool(source.threads));


            //wareHouse
            warehouse=new Warehouse();

            //tools
            for(GsonSource.GTools toolIT:source.tools){
                switch (toolIT.tool) {
                    case "rs-pliers":
                        warehouse.addTool(new RandomSumPliers(),(int)(toolIT.qty));
                        break;

                    case "gs-driver":
                        warehouse.addTool(new GcdScrewDriver(),(int)(toolIT.qty));
                        break;

                    case "np-hammer":
                        warehouse.addTool(new NextPrimeHammer(),(int)(toolIT.qty));
                        break;
                }
            }

            //plans
            plans=new ArrayList<>();
            for(GsonSource.GPlans plan: source.plans){
                plans.add(new ManufactoringPlan(plan.product,plan.parts,plan.tools));
                warehouse.addPlan(new ManufactoringPlan(plan.product,plan.parts,plan.tools));
            }

            //  waves
            waves = source.waves;



        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("failed to read file");
        }
        ConcurrentLinkedQueue<Product> simulationResult = new ConcurrentLinkedQueue<>();
        try {
            simulationResult= start();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        try {
            pool.shutdown();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        try {
            FileOutputStream fout = new FileOutputStream("result.ser");
            ObjectOutputStream oos = new ObjectOutputStream(fout);
            oos.writeObject(simulationResult);
        } catch (IOException e){
            e.printStackTrace();
        }

    }


}

