package vebTree;

import java.util.AbstractSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;

public class vebTreeSet extends AbstractSet<Long> {
    public static final long MIN_POSSIBLE_KEY = 0;
    public static final long MAX_POSSIBLE_KEY = (1L << 32) - 1L;
    public static final long NO_ELEMENT = -1;
    static final long DUMMY_MAX = MIN_POSSIBLE_KEY - 1;
    static final long DUMMY_MIN = MAX_POSSIBLE_KEY + 1;
    long minKey = DUMMY_MIN;
    long maxKey = DUMMY_MAX;

    /**
     * this instance is responsible for array of 2^k size
     */
    private final int k;
    private final int k2;
    private vebTreeSet summary = null;
    private vebTreeSet[] clusters = null;

    private int lowBits(long num){
        return (int)(num & ((1L << k2) - 1L));
    }

    private int highBits(long num){
        return (int)(num >> k2);
    }

    private long combineLowHigh(long low, long high){
        return (high << k2) | low;
    }

    private int size = 0;
    @Override
    public int size() {
        return size;
    }
    @Override
    public boolean isEmpty() {
        return 0 == size;
    }

    @Override
    public boolean contains(Object o) {
        return o instanceof Long && contains((long) o);
    }

    private boolean contains(long val){
        boolean retVal = minKey == val || maxKey == val;
        if (!retVal) {
            final int low = lowBits(val);
            final int hi = highBits(val);
            if (clusters != null && clusters[hi] != null)
                retVal = clusters[hi].contains(low);
        }
        return retVal;
    }

    @Override
    public boolean add(Long aLong) {
        return add(aLong.longValue());
    }
    boolean add(long val){
        if (val < 0 || val >= (1L << k))
            throw new UnsupportedOperationException("Adding element " + val +
                    " to (sub)vebTree which can contains only values in range [0; " +
                    (1L << k) + ").");
        return insert(val);
    }

    boolean insert(long val){
        boolean retVal = false;
        if (size == 0){
            minKey = maxKey = val;
            retVal = true;
        }
        else if (size == 1){
            if (val < minKey) {
                minKey = val;
                retVal = true;
            }
            if (val > maxKey){
                maxKey = val;
                retVal = true;
            }
        }
        else {
            if (val < minKey) {
                long t = val;
                val = minKey;
                minKey = t;
            }
            else if (val > maxKey){
                long t = val;
                val = maxKey;
                maxKey = t;
            }

            if (minKey < val && val < maxKey){
                if (summary == null) summary = new vebTreeSet(k2);
                if (clusters == null) clusters = new vebTreeSet[1 << k2];
                final int low = lowBits(val);
                final int hi = highBits(val);
                if (clusters[hi] == null){
                    clusters[hi] = new vebTreeSet(k - k2);
                    clusters[hi].insert((long)low);
                    summary.insert(hi);
                    retVal = true;
                } else {
                    retVal = clusters[hi].insert((long)low);
                }
            }
        }
        if (retVal) {
            ++size;
        }
        return retVal;
    }

    public vebTreeSet(){
        this(32);
    }
    vebTreeSet(int k){
        this.k = k;
        this.k2 = k / 2;
    }

    @Override
    public void clear() {
        minKey = DUMMY_MIN;
        maxKey = DUMMY_MAX;
        size = 0;
        summary = null;
        clusters = null;
    }

    @Override
    public boolean remove(Object o) {
        return o instanceof Long && remove(((Long) o).longValue());
    }

    @SuppressWarnings("Duplicates")
    boolean remove(long val){
        if (size < 2){
            if (val == minKey && val == maxKey) {
                clear();
                return true;
            }
            return false;
        }
        boolean retVal = false;
        if (size == 2){
            if (minKey == val || maxKey == val){
                minKey = maxKey = minKey ^ maxKey ^ val;
                retVal = true;
            }
        }
        else {
            if (minKey == val){
                minKey = combineLowHigh(clusters[(int)summary.minKey].minKey,summary.minKey);
                val = minKey;
            }
            else if (maxKey == val){
                maxKey = combineLowHigh(clusters[(int)summary.maxKey].maxKey,summary.maxKey);
                val = maxKey;
            }
            final int low = lowBits(val);
            final int hi = highBits(val);
            if (clusters[hi] != null){
                retVal = clusters[hi].remove(low);
                if (clusters[hi].isEmpty()){
                    clusters[hi] = null;
                    summary.remove(hi);
                }
            }
        }
        if (retVal){
            --size;
        }
        return retVal;
    }

    @SuppressWarnings("Duplicates")
    public long nextKey(long val){
        if (isEmpty() || val >= maxKey) return NO_ELEMENT;
        if (val < minKey) return minKey;
        long defaultRetVal = val < maxKey? maxKey : NO_ELEMENT;
        if (summary == null || clusters == null) return defaultRetVal;
        final int low = lowBits(val);
        int hi = highBits(val);
        if (clusters[hi] != null && low < clusters[hi].maxKey){
            return combineLowHigh(clusters[hi].nextKey(low), hi);
        }
        hi = (int)summary.nextKey(hi);
        if (hi == NO_ELEMENT) return defaultRetVal;
        return combineLowHigh(clusters[hi].minKey, hi);
    }




    @Override
    public Iterator<Long> iterator() {
        return new Iterator<Long>() {
            long prev = NO_ELEMENT;
            long cur = isEmpty()? NO_ELEMENT : minKey;

            @Override
            public boolean hasNext() {
                return cur != NO_ELEMENT;
            }

            @Override
            public Long next() {
                if (cur == NO_ELEMENT) throw new NoSuchElementException();
                prev = cur;
                cur = nextKey(cur);
                return prev;
            }

            @Override
            public void remove() {
                if (prev == NO_ELEMENT) throw new IllegalStateException();
                vebTreeSet.this.remove(prev);
                prev = NO_ELEMENT;
            }
        };
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        for (Object o : c) {
            if (!contains(o))
                return false;
        }
        return true;
    }
}
