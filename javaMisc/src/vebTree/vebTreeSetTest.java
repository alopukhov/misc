package vebTree;

import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class vebTreeSetTest {
    private static void printIter(Iterator<?> i){
        while (i.hasNext()){
            System.out.print(i.next().toString() + " ");
        }
        System.out.println();
    }

    @Test
    void SimpleTest() {
        List<Long> l = new ArrayList<Long>();
        vebTreeSet vbt = new vebTreeSet();
        printIter(vbt.iterator());
        for (int i = 0; i < 1000; ++i) {
            vbt.add(i);
            l.add((long) i);
            assertTrue(vbt.containsAll(l));
            assertArrayEquals(l.toArray(), vbt.toArray());
        }
    }

    @Test
    void yetAnotherSimpleTest() {
        Set<Long> ts = new TreeSet<>();
        Set<Long> vbt = new vebTreeSet();
        Random r = new Random(777);
        final int ntest = 4;

        for (int iTest = 0; iTest < ntest; ++iTest) {
            for (int i = 0; i < 5000; ++i) {
                long t = r.nextInt(1024);
                boolean expectedAddRes = ts.add(t);
                assertEquals(expectedAddRes, vbt.add(t));
                assertEquals(ts.size(), vbt.size());
                assertTrue(ts.equals(vbt));
                assertTrue(vbt.equals(ts));
                assertArrayEquals(vbt.toArray(), ts.toArray());
            }
            List<Long> lst =  new ArrayList<Long>();
            vbt.iterator().forEachRemaining(lst::add);
            Collections.shuffle(lst);
            for(Long t : lst){
                assertTrue(ts.remove(t));
                assertTrue(vbt.remove(t));
                assertTrue(ts.equals(vbt));
                assertTrue(vbt.equals(ts));
                assertArrayEquals(vbt.toArray(), ts.toArray());
            }
            assertEquals(0,vbt.size());
        }
    }

    @Test
    void yetAnotherSimpleTest2() {
        Set<Long> ts = new TreeSet<>();
        Set<Long> vbt = new vebTreeSet();
        Random r = new Random(777);
        final int ntest = 1;

        for (int iTest = 0; iTest < ntest; ++iTest) {
            for (int i = 0; i < 5000; ++i) {
                long t = (r.nextLong() & 0xffffffffL) - 100L;
                boolean expectedAddRes = ts.add(t);
                assertEquals(expectedAddRes, vbt.add(t));
                assertEquals(ts.size(), vbt.size());
                assertTrue(ts.equals(vbt));
                assertTrue(vbt.equals(ts));
                assertArrayEquals(vbt.toArray(), ts.toArray());
            }
        }
    }
}