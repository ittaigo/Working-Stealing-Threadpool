package bgu.spl.a2;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Describes a monitor that supports the concept of versioning - its idea is
 * simple, the monitor has a version number which you can receive via the method
 * {@link #getVersion()} once you have a version number, you can call
 * {@link #await(int)} with this version number in order to wait until this
 * version number changes.
 *
 * you can also increment the version number by one using the {@link #inc()}
 * method.
 *
 * Note for implementors: you may add methods and synchronize any of the
 * existing methods in this class *BUT* you must be able to explain why the
 * synchronization is needed. In addition, the methods you add can only be
 * private, protected or package protected - in other words, no new public
 * methods
 */
public class VersionMonitor {

    private volatile AtomicInteger version=new AtomicInteger(0);


    public int getVersion() {
        return version.get();

    }
    // inc and wait must be synchronized to make sure only one thread is running the functions
    public synchronized void inc() {
        version.incrementAndGet();
        notifyAll();
    }
    // inc and wait must be synchronized to make sure only one thread is running the functions
    public synchronized void await(int version) throws InterruptedException {
        while(version==getVersion()){
            wait();
        }

    }
}
