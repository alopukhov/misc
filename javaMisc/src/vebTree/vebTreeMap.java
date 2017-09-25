package vebTree;

import java.util.*;

import static vebTree.vebTreeSet.DUMMY_MAX;
import static vebTree.vebTreeSet.DUMMY_MIN;

public class vebTreeMap<V> extends AbstractMap<Long, V>{
    public static long MIN_POSSIBLE_KEY = vebTreeSet.MIN_POSSIBLE_KEY;
    public static long MAX_POSSIBLE_KEY = vebTreeSet.MAX_POSSIBLE_KEY;
    public static final long NO_ELEMENT = vebTreeSet.NO_ELEMENT;
    private long minKey = DUMMY_MIN;
    private long maxKey = DUMMY_MAX;
    private V minVal = null;
    private V maxVal = null;

    private final int k;
    private final int k2;
    private vebTreeSet summary = null;
    private vebTreeMap<V>[] clusters = null;

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

    public vebTreeMap(){
        this(32);
    }
    private vebTreeMap(int k){
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
        minVal = null;
        maxVal = null;
    }

    @Override
    public boolean containsKey(Object key) {
        return key instanceof Long && containsKey(((Long) key).longValue());
    }

    private boolean containsKey(long key){
        boolean retVal = minKey == key || maxKey == key;
        if (!retVal) {
            final int low = lowBits(key);
            final int hi = highBits(key);
            if (clusters != null && clusters[hi] != null)
                retVal = clusters[hi].containsKey(low);
        }
        return retVal;
    }

    @Override
    public V get(Object key) {
        if (key instanceof Long) return get(((Long) key).longValue());
        return null;
    }

    private V get(long key){
        V retVal = null;
        if (key == minKey)
            retVal = minVal;
        else if (key == maxKey)
            retVal = maxVal;
        else {
            final int low = lowBits(key);
            final int hi = highBits(key);
            if (clusters != null && clusters[hi] != null)
                retVal = clusters[hi].get(low);
        }
        return retVal;
    }

    @Override
    public V put(Long key, V value) {
        return put(key.longValue(), value);
    }

    private V put(long key, V value) {
        if (key < 0 || key >= (1L << k))
            throw new UnsupportedOperationException("Adding key " + key +
                    " to (sub)vebTree which can contains only values in range [0; " +
                    (1L << k) + ").");
        final V retVal = get(key);
        insert(key, value);
        return retVal;
    }

    private boolean insert(long key, V value){
        boolean addedNewKey = false;
        if (size == 0){
            minKey = maxKey = key;
            minVal = maxVal = value;
            addedNewKey = true;
        } else if (key == minKey || key == maxKey){
            if (key == minKey){
                minVal = value;
            }
            if (key == maxKey) {
                maxVal = value;
            }
        }
        else if (size == 1){
            if (key < minKey) {
                minKey = key;
                minVal = value;
                addedNewKey = true;
            }
            else if (key > maxKey){
                maxKey = key;
                maxVal = value;
                addedNewKey = true;
            } else {
                //normally we shouldn't get here
                throw new RuntimeException("Unexpected inner state");
            }
        }
        else {
            if (key < minKey) {
                long tk = key;
                V tv = value;
                key = minKey;
                value = minVal;
                minKey = tk;
                minVal = tv;
            }
            else if (key > maxKey){
                long tk = key;
                V tv = value;
                key = maxKey;
                value = maxVal;
                maxKey = tk;
                maxVal = tv;
            }

            if (summary == null) summary = new vebTreeSet(k2);
            if (clusters == null) clusters = new vebTreeMap[1 << k2];
            final int low = lowBits(key);
            final int hi = highBits(key);
            if (clusters[hi] == null){
                clusters[hi] = new vebTreeMap<>(k - k2);
                clusters[hi].insert((long)low, value);
                summary.insert(hi);
                addedNewKey = true;
            } else {
                addedNewKey = clusters[hi].insert((long)low, value);
            }
        }
        if (addedNewKey) {
            ++size;
        }
        return addedNewKey;
    }

    @Override
    public V remove(Object key) {
        if (key instanceof Long) return remove(((Long) key).longValue());
        return null;
    }

    private V remove(long key){
        final V retVal = get(key);
        erase(key);
        return retVal;
    }

    @SuppressWarnings("Duplicates")
    private boolean erase(long key){
        if (size < 2){
            if (key == minKey && key == maxKey) {
                clear();
                return true;
            }
            return false;
        }

        boolean retVal = false;
        if (size == 2){
            if (minKey == key){
                minKey = maxKey;
                minVal = maxVal;
                retVal = true;
            }
            else if (maxKey == key){
                maxKey = minKey;
                maxVal = minVal;
                retVal = true;
            }
        }
        else {
            if (minKey == key){
                int i = (int) summary.minKey;
                this.minKey = combineLowHigh(clusters[i].minKey,summary.minKey);
                this.minVal = clusters[i].minVal;
                key = this.minKey;
            }
            else if (maxKey == key){
                int i = (int) summary.maxKey;
                this.maxKey = combineLowHigh(clusters[i].maxKey,summary.maxKey);
                this.maxVal = clusters[i].maxVal;
                key = this.maxKey;
            }
            final int low = lowBits(key);
            final int hi = highBits(key);
            if (clusters[hi] != null){
                retVal = clusters[hi].erase(low);
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
    public Set<Entry<Long, V>> entrySet() {
        return new AbstractSet<Entry<Long, V>>() {
            @Override
            public int size() {
                return vebTreeMap.this.size();
            }

            @Override
            public boolean isEmpty() {
                return vebTreeMap.this.isEmpty();
            }

            @Override
            public boolean contains(Object o) {
                if (!(o instanceof Map.Entry)) return false;
                Map.Entry entry = (Map.Entry)o;
                try {
                    long key = (Long) entry.getKey();
                    Object val = entry.getValue();
                    if (!vebTreeMap.this.containsKey(key))
                        return false;
                    V realVal = get(key);
                    return (val == null || realVal == null)?
                            val == realVal :
                            val.equals(realVal);
                } catch (ClassCastException unused){
                    return false;
                }
            }

            @Override
            public Iterator<Entry<Long, V>> iterator() {
                return new Iterator<Entry<Long, V>>() {
                    long prev = NO_ELEMENT;
                    long cur = isEmpty()? NO_ELEMENT : minKey;

                    @Override
                    public boolean hasNext() {
                        return cur != NO_ELEMENT;
                    }

                    @Override
                    public Entry<Long, V> next() {
                        if (cur == NO_ELEMENT) throw new NoSuchElementException();
                        prev = cur;
                        cur = nextKey(cur);
                        return new SimpleEntry<Long, V>(prev, get(prev)){
                            @Override
                            public V setValue(V value) {
                                vebTreeMap.this.insert(getKey(), value);
                                return super.setValue(value);
                            }
                        };
                    }
                };
            }

            @Override
            public boolean addAll(Collection<? extends Entry<Long, V>> c) {
                throw new UnsupportedOperationException();
            }

            @Override
            public boolean add(Entry<Long, V> longVEntry) {
                throw new UnsupportedOperationException();
            }

            @Override
            public boolean remove(Object o) {
                return false;
            }

            @Override
            public boolean containsAll(Collection<?> c) {
                for (Object o : c) {
                    if (!contains(o))
                        return false;
                }
                return true;
            }

            @Override
            public void clear() {
                vebTreeMap.this.clear();
            }
        };

    }
}
