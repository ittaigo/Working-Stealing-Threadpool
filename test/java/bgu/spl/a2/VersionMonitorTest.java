package bgu.spl.a2;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by Ittai Gootwine on 12/12/2016.
 */
public class VersionMonitorTest {
    private VersionMonitor ver;
    private Thread t1, t2;


    @Before
    public void setUp() throws Exception {
        ver = new VersionMonitor();
        t1 = new Thread(()-> {
            int oldVersion = ver.getVersion();
            try {
                ver.await(ver.getVersion());
            } catch (InterruptedException e) {
                assertNotEquals(oldVersion, ver.getVersion());
            }
        });
        t2 = new Thread(()-> ver.inc());
    }

    @After
    public void tearDown() throws Exception {
        ver = null;
        t1= null;
        t2 = null;
    }

    @Test
    public void getVersion(){
        assertEquals(0, ver.getVersion());
    }

    @Test
    public void inc(){
        int v1= ver.getVersion();
        ver.inc();
        int v2 = ver.getVersion();
        assertEquals(v2,v1+1);
    }

    @Test
    public void await() throws Exception {
        t1.start();
        if(t1.getState()==Thread.State.WAITING)
            t2.start();
        else{
            fail();
        }
    }


}