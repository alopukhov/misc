package rbTree;

import java.util.*;

public class RbTree<K, V> extends AbstractMap<K, V> {
    static class Node<K, V> {
        Node<K, V> lChild = null;
        Node<K, V> rChild = null;
        boolean red = false;
        K key;
        V value;

        Node(K key, V value, boolean red) {
            this.key = key;
            this.value = value;
            this.red = red;
        }
    }

    private static final Node LEAF = new Node<>(null, null, false);
    private Node<K, V> root;
    private final Comparator<? super K> comparator;
    private int size = 0;
    private long changes = 0;

    private int compare(K k1, K k2) {
        return comparator.compare(k1, k2);
    }

    @SuppressWarnings({"unchecked", "WeakerAccess"})
    public RbTree() {
        this((k1, k2) -> ((Comparable<? super K>) k1).compareTo(k2));
    }

    @SuppressWarnings({"unchecked", "WeakerAccess"})
    public RbTree(Comparator<? super K> comparator) {
        this.comparator = comparator;
        LEAF.lChild = LEAF;
        LEAF.rChild = LEAF;
        this.root = LEAF;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public boolean isEmpty() {
        return size == 0;
    }

    @Override
    public boolean containsKey(Object key) {
        @SuppressWarnings("unchecked")
        K k = (K) key;
        return findNodeEqual(k) != LEAF;
    }

    private Node<K, V> findNodeEqual(K key) {
        Node<K, V> ret = root;
        int c;
        while (ret != LEAF && (c = compare(key, ret.key)) != 0) {
            ret = (c < 0) ? ret.lChild : ret.rChild;
        }
        return ret;
    }

    @Override
    @SuppressWarnings("unchecked")
    public V get(Object key) {
        return findNodeEqual((K)key).value;
    }

    @SuppressWarnings("unchecked")
    private static <K, V> Node<K, V> createNode(K key, V value, boolean color) {
        final Node<K, V> ret = new Node<>(key, value, color);
        ret.lChild = LEAF;
        ret.rChild = LEAF;
        return ret;
    }

    private static <K, V> Node<K, V> parent(List<Node<K, V>> path) {
        return path.get(path.size() - 2);
    }
    private static <K, V> Node<K, V> grandParent(List<Node<K, V>> path) {
        return path.get(path.size() - 3);
    }
    private static <K, V> Node<K, V> otherChild(Node<K, V> node, Node notThis){
        return  (node.lChild != notThis)? node.lChild : node.rChild;
    }
    private static <K, V> Node<K, V> uncle(List<Node<K, V>> path) {
        final Node<K, V> g = grandParent(path);
        final Node<K, V> p = parent(path);
        return otherChild(g, p);
    }

    private static <K, V> Node<K, V> pop(List<Node<K, V>> path){
        return path.remove(path.size() - 1);
    }
    private static <K, V> Node<K, V> peek(List<Node<K, V>> path){
        return path.get(path.size() - 1);
    }
    
    private static <K, V> void rotateLeft(List<Node<K, V>> path) {
        final Node<K, V> b = pop(path);
        final Node<K, V> d = b.rChild;
        final Node<K, V> c = d.lChild;
        if (!path.isEmpty()){
            if (peek(path).lChild == b)
                peek(path).lChild = d;
            else
                peek(path).rChild = d;
        }
        b.rChild = c;
        d.lChild = b;
        path.add(d);
        path.add(b);
    }
    private static <K, V> void rotateRight(List<Node<K, V>> path) {
        final Node<K, V> d = pop(path);
        final Node<K, V> b = d.lChild;
        final Node<K, V> c = b.rChild;
        if (!path.isEmpty()){
            if (peek(path).lChild == d)
                peek(path).lChild = b;
            else
                peek(path).rChild = b;
        }
        b.rChild = d;
        d.lChild = c;
        path.add(b);
        path.add(d);
    }

    private final ArrayList<Node<K, V>> path = new ArrayList<>();
    @Override
    public V put(K key, V value) {
        if (key == null)
            throw new NullPointerException("Key must be not null");
        if (root == LEAF) {
            root = createNode(key, value, false);
            ++size;
            ++changes;
            return null;
        }
        path.add(root);
        int c = 0;
        Node<K, V> t;
        while ((t = peek(path)) != LEAF && (c = compare(key, t.key)) != 0) {
            path.add(c < 0 ? t.lChild : t.rChild);
        }
        if (t != LEAF) {
            V retVal = t.value;
            t.value = value;
            ++changes;
            path.clear();
            return retVal;
        }

        t = createNode(key, value, true);
        pop(path);
        if (c < 0)
            peek(path).lChild = t;
        else
            peek(path).rChild = t;
        path.add(t);
        boolean fixed = false;
        while (path.size() > 2 && !fixed) { // i.e. grandParent exists
            if (!parent(path).red) //parent is black
                fixed = true;
                //now we know parent is red
            else if (uncle(path).red) { //booth parent and uncle are red
                grandParent(path).red = true;
                parent(path).red = false;
                uncle(path).red = false;
                pop(path);
                pop(path); //now we balancing from grandpa which is red
            } else {
                if (grandParent(path).lChild == parent(path)){
                    if (parent(path).rChild == peek(path)){
                        pop(path);
                        rotateLeft(path);
                    }
                    parent(path).red = false;
                    grandParent(path).red = true;
                    pop(path);
                    pop(path);
                    rotateRight(path);
                    fixed = true;
                } else {
                    if (parent(path).lChild == peek(path)){
                        pop(path);
                        rotateRight(path);
                    }
                    parent(path).red = false;
                    grandParent(path).red = true;
                    pop(path);
                    pop(path);
                    rotateLeft(path);
                    fixed = true;
                }
            }
        }
        root = path.get(0);
        path.clear();
        root.red = false;
        ++size;
        ++changes;
        return null;
    }


    @Override
    @SuppressWarnings("unchecked")
    public void clear() {
        size = 0;
        ++changes;
        root = LEAF;
    }

    @Override
    @SuppressWarnings("unchecked")
    public V remove(Object key) {
        if (key == null)
            throw new NullPointerException();
        return erase((K)key);
    }

    @SuppressWarnings("Duplicates")
    private V erase(K key){
        path.add(root);
        int c;
        Node<K, V> t;
        while ((t = peek(path)) != LEAF && (c = compare(key, t.key)) != 0) {
            path.add(c < 0 ? t.lChild : t.rChild);
        }
        if (t == LEAF) {
            path.clear();
            return null;
        }
        V retVal = t.value;
        if (t.lChild != LEAF && t.rChild != LEAF){
            path.add(peek(path).rChild);
            Node<K, V> tt;
            while ((tt = peek(path).lChild) != LEAF)
                path.add(tt);
            tt = peek(path);
            t.key = tt.key;
            t.value = tt.value;
        }

        boolean fixed;
        if (path.size() == 1){
            path.set(0, otherChild(root, LEAF));
            fixed = true;
        }
        else {
            t = pop(path);
            Node<K, V> child = otherChild(t, LEAF);
            fixed = t.red;
            if (!t.red && child.red){
                fixed = true;
                child.red = false;
            }
            Node<K, V> p = peek(path);
            if (p.lChild == t)
                p.lChild = child;
            else
                p.rChild = child;
            path.add(child);
        }

        while (path.size() > 1 && !fixed){
            t = pop(path);
            if (peek(path).lChild == t){
                if (peek(path).rChild.red){
                    rotateLeft(path);
                    peek(path).red = true;
                    parent(path).red = false;
                }
                Node<K, V> p = peek(path);
                Node<K, V> s = otherChild(peek(path), t);
                if (!p.red && !s.lChild.red && !s.rChild.red){
                    if (s != LEAF) s.red = true;
                } else if (p.red && !s.lChild.red && !s.rChild.red) {
                    p.red = false;
                    if (s != LEAF) s.red = true;
                    fixed = true;
                } else if (s.lChild.red && !s.rChild.red){
                    s.red = true;
                    s.lChild.red = false;
                    path.add(s);
                    rotateRight(path);
                    pop(path);
                    pop(path);
                    path.add(t);
                } else {
                    s.red = p.red;
                    p.red = false;
                    s.rChild.red = false;
                    rotateLeft(path);
                    fixed = true;
                }
            } else {
                if (peek(path).lChild.red){
                    rotateRight(path);
                    peek(path).red = true;
                    parent(path).red = false;
                }
                Node<K, V> p = peek(path);
                Node<K, V> s = otherChild(peek(path), t);
                if (!p.red && !s.lChild.red && !s.rChild.red){
                    if (s != LEAF) s.red = true;
                } else if (p.red && !s.lChild.red && !s.rChild.red) {
                    p.red = false;
                    if (s != LEAF) s.red = true;
                    fixed = true;
                } else if (s.rChild.red && !s.lChild.red){
                    s.red = true;
                    s.rChild.red = false;
                    path.add(s);
                    rotateLeft(path);
                    pop(path);
                    pop(path);
                    path.add(t);
                } else {
                    s.red = p.red;
                    p.red = false;
                    s.lChild.red = false;
                    rotateRight(path);
                    fixed = true;
                }
            }
        }
        root = path.get(0);
        path.clear();
        root.red = false;
        --size;
        --changes;
        return retVal;
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        return new AbstractSet<Entry<K, V>>() {
            @Override
            public Iterator<Entry<K, V>> iterator() {
                return new Iterator<Entry<K, V>>() {
                    long changes = -1;
                    final Stack<Node<K, V>> st = new Stack<>();
                    final Stack<Boolean> goLeft = new Stack<>();
                    boolean initialized = false;
                    void init(){
                        if (initialized) return;
                        initialized = true;
                        st.push(root);
                        goLeft.push(true);
                        changes = RbTree.this.changes;
                    }


                    private Node<K, V> nextNode(){
                        Boolean toLeft = goLeft.pop();
                        Node<K, V> retVal;
                        if (toLeft){
                            goLeft.push(false);
                            while ((retVal = st.peek().lChild) != LEAF){
                                st.push(retVal);
                                goLeft.push(true);
                            }
                            goLeft.pop();
                        }
                        retVal = st.pop();
                        if (retVal.rChild != LEAF) {
                            st.push(retVal.rChild);
                            goLeft.push(true);
                        } else if (!goLeft.empty() && goLeft.peek()){
                            goLeft.pop();
                            goLeft.push(false);
                        }
                        return retVal;
                    }

                    @Override
                    public boolean hasNext() {
                        init();
                        if (RbTree.this.changes != this.changes)
                            throw new ConcurrentModificationException();
                        return !st.isEmpty();
                    }

                    @Override
                    public Entry<K, V> next() {
                        init();
                        if (RbTree.this.changes != this.changes)
                            throw new ConcurrentModificationException();
                        if (st.isEmpty())
                            throw new UnsupportedOperationException();
                        Node<K, V> cur = nextNode();

                        return new SimpleEntry<K, V>(cur.key, cur.value) {
                            @Override
                            public V setValue(V value) {
                                if (RbTree.this.changes != changes)
                                    throw new ConcurrentModificationException();
                                cur.value = value;
                                changes = ++RbTree.this.changes;
                                return super.setValue(value);
                            }
                        };
                    }

                    @Override
                    public void remove() {
                        throw new UnsupportedOperationException();
                    }
                };
            }

            @Override
            public int size() {
                return RbTree.this.size;

            }

            @Override
            @SuppressWarnings("unchecked")
            public boolean contains(Object o) {
                if (!(o instanceof Map.Entry))
                    return false;
                Entry<K, V> e = (Entry<K, V>)o;
                Node<K, V> n = findNodeEqual(e.getKey());
                return n != LEAF && (n.value == null && e.getValue() == null || n.value != null && n.value.equals(e.getValue()));
            }

            @Override
            public void clear() {
                RbTree.this.clear();
            }
        };
    }
}