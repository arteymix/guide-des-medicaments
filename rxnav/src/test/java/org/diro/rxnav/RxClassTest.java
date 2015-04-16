package org.diro.rxnav;

import org.junit.Test;

import static org.junit.Assert.*;

public class RxClassTest {

    @Test
    public void testAllClasses() throws Exception {
        assertNotNull(RxClass.newInstance().allClasses(""));
    }

    @Test
    public void testAllClasses() throws Exception {
        assertNotNull(RxClass.newInstance().findClassById(""));
    }

    @Test
    public void testAllClasses() throws Exception {
        assertNotNull(RxClass.newInstance().getSpellingSuggestions(""));
    }

}
