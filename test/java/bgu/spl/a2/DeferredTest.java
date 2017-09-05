package bgu.spl.a2;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.After;
import org.junit.Test;


/**
 * Created by snir on 12/12/16.
 */
public class DeferredTest {

    Deferred <Integer> def;
    boolean res;

    @Before
    public void setUp() {
         def=new Deferred<>();
         res=false;
    }

    @After
    public void tearDown()  {
        this.def=null;
        this.res=false;
    }

    @Test(expected = IllegalStateException.class)
    public void get()throws IllegalStateException {
        def.get();
    }

    @Test
    public void isResolved()  {
        assertFalse("Need to be false ",def.isResolved());
    }

    @Test
    public void isResolvedAfterResolve()  {
        def.resolve(3);
        assertTrue("Need to be True ",def.isResolved());
    }

    @Test
    public void resolve()  {
       def.resolve(3);
       assertEquals("Check if Equal 3",3,def.get().intValue());
    }

    @Test (expected = IllegalStateException.class)
    public void resolveAfterResolve() throws IllegalStateException{
        def.resolve(3);
        def.resolve(2);
    }


    @Test
    public void whenResolved() throws Exception {
        def.whenResolved(() -> res = true);

        def.resolve(7);
        assertTrue("Need to be True",res);
    }

    @Test
    public void whenResolvedAfterResolve() throws Exception {
        def.resolve(6);

        res = false;
        def.whenResolved(() -> res = true);

        assertTrue(res);
    }


}