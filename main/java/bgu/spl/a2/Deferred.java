package bgu.spl.a2;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * this class represents a deferred result i.e., an object that eventually will
 * be resolved to hold a result of some operation, the class allows for getting
 * the result once it is available and registering a callback that will be
 * called once the result is available.
 *
 * Note for implementors: you may add methods and synchronize any of the
 * existing methods in this class *BUT* you must be able to explain why the
 * synchronization is needed. In addition, the methods you add can only be
 * private, protected or package protected - in other words, no new public
 * methods
 *
 * @param <T> the result type
 */
public class Deferred<T> {
    private AtomicBoolean isResolved = new AtomicBoolean(false);
    private T value = null;
    private ConcurrentLinkedQueue<Runnable> callbackQueue = new ConcurrentLinkedQueue<>();

    /**
     * @return the resolved value if such exists (i.e., if this object has been
     * {@link #resolve(java.lang.Object)}ed yet
     * @throws IllegalStateException in the case where this method is called and
     *                               this object is not yet resolved
     */
    public T get() throws IllegalStateException {
        if (isResolved()) {
            return value;
        }
        throw new IllegalStateException("Obj is not yet resolved");
    }

    /**
     * @return true if this object has been resolved - i.e., if the method
     * {@link #resolve(java.lang.Object)} has been called on this object before.
     */
    public boolean isResolved() {
        return isResolved.get();

    }
    /**
     * resolve this deferred object - from now on, any call to the method
     * {@link #get()} should return the given value
     * <p>
     * Any callbacks that were registered to be notified when this object is
     * resolved via the {@link #whenResolved(java.lang.Runnable)} method should
     * be executed before this method returns
     *
     * @param value - the value to resolve this deferred object with
     * @throws IllegalStateException in the case where this object is already
     *                               resolved
     */

    public synchronized void resolve(T value)throws IllegalStateException {
        if (isResolved()) {
            throw new IllegalStateException("Obj is already resolved");
        }
        else {
            this.value = value;
            isResolved.set(true);
            while (!callbackQueue.isEmpty()){
                Runnable callBack = callbackQueue.poll();
                if(callBack!=null){
                    callBack.run();
                }
            }
        }
    }
    /**
     * add a callback to be called when this object is resolved. if while
     * calling this method the object is already resolved - the callback should
     * be called immediately
     * <p>
     * Note that in any case, the given callback should never get called more
     * than once, in addition, in order to avoid memory leaks - once the
     * callback got called, this object should not hold its reference any
     * longer.
     *
     * @param callback the callback to be called when the deferred object is
     *                 resolved
     */

    public synchronized void whenResolved(Runnable callback) {
        if(isResolved()){
            callback.run();
        }
        else{
            callbackQueue.add(callback);
        }
    }
}
