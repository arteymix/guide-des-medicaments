package org.diro.rxnav;

import static org.junit.Assert.*;

import org.junit.Test;

/**
 * Created by guillaume on
 * 15-04-08.
 */
public class RxClassTest {

    @Test
    public void testAllClasses() throws Exception {
        assertNotNull(RxClass.newInstance().allClasses(""));
    }
}
