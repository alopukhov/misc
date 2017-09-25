package vebTree;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class vebTreeMapTest {
    @Test
    void SimpleTest(){
        Map<Long, Integer> m = new vebTreeMap<>();
        for (Integer t : new Integer[]{1, 4, 3, 3, 5, 7}){
            long c = t;
            m.put(c, (Integer)(1 + m.getOrDefault(c, 0)));
        }
        for (Object o : m.entrySet())
            System.out.println(o);
    }
}