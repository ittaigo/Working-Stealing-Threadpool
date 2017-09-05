package bgu.spl.a2.sim.tasks;

import bgu.spl.a2.Deferred;
import bgu.spl.a2.Task;
import bgu.spl.a2.sim.Product;
import bgu.spl.a2.sim.Simulator;
import bgu.spl.a2.sim.conf.ManufactoringPlan;
import bgu.spl.a2.sim.tools.Tool;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Ittai Gootwine on 29/12/2016.
 */
public class ManufactoringTask extends Task<Product> {
    private Product part;

    public ManufactoringTask(Product part) {
        this.part = part;

    }

    @Override
    protected void start() {
        List<Task<Product>> tasks = new ArrayList<>();
        ManufactoringPlan plan = Simulator.getWarehouse().getPlan(part.getName());
        String[] parts = plan.getParts();
        if(parts.length>0) {
            for (String product : parts) {
                Product newProduct = new Product(part.getStartId() + 1, product);

                part.addPart(newProduct);
                ManufactoringTask newTask = new ManufactoringTask(newProduct);
                tasks.add(newTask);
                spawn(newTask);
            }
            whenResolved(tasks, () -> {
                for (String tool : plan.getTools()) {
                    Deferred<Tool> def = Simulator.getWarehouse().acquireTool(tool);
                    def.whenResolved(() -> {
                        long result = def.get().useOn(part);
                        part.addToFinalId(result);
                        Simulator.getWarehouse().releaseTool(def.get());
                    });
                }
                complete(part);
            });
        }
        else {
            complete(part);
        }
    }
}
