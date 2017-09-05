package bgu.spl.a2;

import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * this class represents a single work stealing processor, it is
 * {@link Runnable} so it is suitable to be executed by threads.
 *
 * Note for implementors: you may add methods and synchronize any of the
 * existing methods in this class *BUT* you must be able to explain why the
 * synchronization is needed. In addition, the methods you add can only be
 * private, protected or package protected - in other words, no new public
 * methods
 *
 */
public class Processor implements Runnable {

    private final WorkStealingThreadPool pool;
    private final int id;

    /**
     * constructor for this class
     *
     * IMPORTANT:
     * 1) this method is package protected, i.e., only classes inside
     * the same package can access it - you should *not* change it to
     * public/private/protected
     *
     * 2) you may not add other constructors to this class
     * nor you allowed to add any other parameter to this constructor - changing
     * this may cause automatic tests to fail..
     *
     * @param id - the processor id (every processor need to have its own unique
     * id inside its thread pool)
     * @param pool - the thread pool which owns this processor
     */
    /*package*/ Processor(int id, WorkStealingThreadPool pool) {
        this.id = id;
        this.pool = pool;
    }

    @Override
    public void run() {
        while(!Thread.currentThread().isInterrupted()){
            ConcurrentLinkedDeque<Task<?>> myQueue = pool.getQueue(id);
            if(!myQueue.isEmpty()){
                try{
                    Task<?> task = myQueue.removeFirst();
                    if(task != null){
                        task.handle(this);

                    }
                }
                catch(NoSuchElementException e){
                    Thread.currentThread().interrupt();


                }
            }
            else{
                if(!steal()){
                    try {
                        pool.monitor.await(this.pool.monitor.getVersion());
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            }

        }
    }

    public boolean steal(){
        AtomicBoolean stealing = new AtomicBoolean(false);
        int threadsNum = pool.threads.length;
        int nextSize;



        for(int nextId=(this.id+1)%(threadsNum); nextId!=this.id && !stealing.get() ; nextId = (nextId+1)%(threadsNum)){
            nextSize=pool.tasksQueue[nextId].size();
            if(!pool.tasksQueue[nextId].isEmpty()){
                for(int i=0;i<=nextSize/2;i++){
                    try {
                        Task tmpTask = pool.tasksQueue[nextId].pollLast();
                        if (tmpTask!=null) pool.tasksQueue[this.id].addFirst(tmpTask);
                        stealing.set(true);
                    }
                    catch (NoSuchElementException e){
                       
                    }
                }
            }

        }
        return stealing.get();
    }


    public void addTasksToQueue(Task<?>... tasks) {
        ConcurrentLinkedDeque myQueue = pool.getQueue(id);
        for (Task<?> task: tasks ) {
            myQueue.addFirst(task);

        }
        pool.monitor.inc();
    }
}
