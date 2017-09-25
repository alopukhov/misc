using System;
using System.Collections;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace AvlTree
{
    public sealed class AvlTree<T> : IEnumerable<T>
    {
        private readonly IComparer<T> comparer;
        private Node root;
        private int changes;

        private int _count = 0;
        public int Count
        {
            get { return _count; }
            private set
            {
                if (value != _count)
                {
                    _count = value;
                    ++changes;
                }
            }
        }

        public AvlTree(IComparer<T> comparer)
        {
            this.comparer = comparer;
            _count = 0;
        }
        public AvlTree() : this(Comparer<T>.Default)
        { }

        internal class Node
        {
            public readonly T data;
            public sbyte Diff
            {
                get
                {
                    sbyte ans = 0;
                    if (lchild != null) ans += lchild.height;
                    if (rchild != null) ans -= rchild.height;
                    return ans;
                }
            }
            public sbyte height;

            public Node parent;
            public Node lchild;
            public Node rchild;
            public Node(T data)
            {
                this.data = data;
                parent = lchild = rchild = null;
                height = 1;
            }

            public static sbyte GetHeight(Node t)
            {
                return ((t == null) ? (sbyte)0 : t.height);
            }
            public static void UpdateNode(Node t)
            {
                if (t == null) return;
                sbyte a = GetHeight(t.lchild);
                sbyte b = GetHeight(t.rchild);
                t.height = 1;
                if (a > b) t.height += a;
                else t.height += b;
            }
        }

        private void _balanceToUp(Node t)
        {
            if (t == null) root = null;
            while (t != null)
            {
                Node.UpdateNode(t);
                if (t.Diff == +2)
                {
                    if (t.lchild.Diff >= 0)
                    {
                        t = SmallRotateToRight(t);
                    }
                    else
                    {
                        t = LargeRotateToRight(t);
                    }
                }
                else if (t.Diff == -2)
                {
                    if (t.rchild.Diff <= 0)
                    {
                        t = SmallRotateToLeft(t);
                    }
                    else
                    {
                        t = LargeRotateToLeft(t);
                    }
                }
                if (t.parent == null)
                {
                    root = t;
                    return;
                }
                t = t.parent;
            }

        }

        public bool Insert(T data)
        {
            return _insert(ref data, ref root, null);
        }

        private bool _insert(ref T data, ref Node t, Node parent)
        {
            if (t == null)
            {
                t = new Node(data);
                t.parent = parent;
                _balanceToUp(t);
                ++Count;
                return true;
            }
            int s = comparer.Compare(data, t.data);
            if (s == 0) return false;
            else if (s < 0) return _insert(ref data, ref t.lchild, t);
            else return _insert(ref data, ref t.rchild, t);
        }

        public bool Erase(T data)
        {
            if (Count == 0) throw new InvalidOperationException();
            return _erase(ref data, ref root);
        }

        private bool _erase(ref T data, ref Node t)
        {
            if (t == null)
            {
                return false;
            }
            int s = comparer.Compare(data, t.data);
            if (s < 0) return _erase(ref data, ref t.lchild);
            else if (s > 0) return _erase(ref data, ref t.rchild);
            else //s == 0
            {
                --Count;
                if (t.height == 1) //is leaf
                {
                    Node parent = t.parent;
                    t.parent = t.lchild = t.rchild = null;
                    t = null;
                    _balanceToUp(parent);
                }
                else
                {
                    Node replace;
                    Node parent;
                    if (Node.GetHeight(t.lchild) > Node.GetHeight(t.rchild))
                    {
                        replace = GetMaxNode(t.lchild);
                        parent = replace.parent;

                        replace.parent = t.parent;
                        //t.parent.child will be assigned next when t = replace;
                        replace.rchild = t.rchild;
                        if (replace.rchild != null) replace.rchild.parent = replace;

                        if (t != parent)
                        {
                            parent.rchild = replace.lchild;
                            if (parent.rchild != null) parent.rchild.parent = parent;
                            replace.lchild = t.lchild;
                            if (replace.lchild != null) replace.lchild.parent = replace;
                        }
                        else
                        {
                            parent = replace;
                        }
                    }
                    else
                    {
                        replace = GetMinNode(t.rchild);
                        parent = replace.parent;

                        replace.parent = t.parent;
                        //t.parent.child will be assigned next when t = replace;
                        replace.lchild = t.lchild;
                        if (replace.lchild != null) replace.lchild.parent = replace;

                        if (t != parent)
                        {
                            parent.lchild = replace.rchild;
                            if (parent.lchild != null) parent.lchild.parent = parent;
                            replace.rchild = t.rchild;
                            if (replace.rchild != null) replace.rchild.parent = replace;
                        }
                        else
                        {
                            parent = replace;
                        }
                    }

                    t = replace;
                    /*if (replace.lchild == null) replace.lchild = ((replace == t.lchild)? null : t.lchild);
                    if (replace.rchild == null) replace.rchild = ((replace == t.rchild)? null : t.rchild);
                    replace.parent = t.parent;
                    if (replace.lchild != null) replace.lchild.parent = replace;
                    if (replace.rchild != null) replace.rchild.parent = replace;
                    if (parent == t) parent = replace;
                    t = replace;*/
                    _balanceToUp(parent);
                }
                return true;
            }

        }

        private static Node GetMaxNode(Node t)
        {
            while (t != null && t.rchild != null)
                t = t.rchild;
            return t;
        }
        private static Node GetMinNode(Node t)
        {
            while (t != null && t.lchild != null)
                t = t.lchild;
            return t;
        }

        private Node SmallRotateToLeft(Node a)
        {
            if (a == null || a.rchild == null) throw new InvalidOperationException();
            Node b = a.rchild;
            Node q = b.lchild;
            if (a.parent != null)
            {
                if (a.parent.lchild == a) a.parent.lchild = b;
                else a.parent.rchild = b;
            }

            b.parent = a.parent;
            b.lchild = a;

            a.parent = b;
            a.rchild = q;

            if (q != null) q.parent = a;
            Node.UpdateNode(a);
            Node.UpdateNode(b);

            return b;
        }

        private Node SmallRotateToRight(Node b)
        {
            if (b == null || b.lchild == null) throw new InvalidOperationException();
            Node a = b.lchild;
            Node q = a.rchild;
            if (b.parent != null)
            {
                if (b.parent.lchild == b) b.parent.lchild = a;
                else b.parent.rchild = a;
            }

            a.parent = b.parent;
            a.rchild = b;

            b.parent = a;
            b.lchild = q;

            if (q != null) q.parent = b;
            Node.UpdateNode(b);
            Node.UpdateNode(a);

            return a;
        }

        private Node LargeRotateToLeft(Node a)
        {
            if (a == null || a.rchild == null || a.rchild.lchild == null) throw new InvalidOperationException();
            Node b = a.rchild;
            Node c = b.lchild;
            Node q = c.lchild;
            Node r = c.rchild;
            if (a.parent != null)
            {
                if (a.parent.lchild == a) a.parent.lchild = c;
                else a.parent.rchild = c;
            }


            c.parent = a.parent;
            c.lchild = a;
            c.rchild = b;

            a.parent = c;
            a.rchild = q;

            b.parent = c;
            b.lchild = r;

            if (q != null) q.parent = a;
            if (r != null) r.parent = b;

            Node.UpdateNode(b);
            Node.UpdateNode(a);
            Node.UpdateNode(c);

            return c;
        }

        private Node LargeRotateToRight(Node a)
        {
            if (a == null || a.lchild == null || a.lchild.rchild == null) throw new InvalidOperationException();
            Node b = a.lchild;
            Node c = b.rchild;
            Node q = c.lchild;
            Node r = c.rchild;
            if (a.parent != null)
            {
                if (a.parent.lchild == a) a.parent.lchild = c;
                else a.parent.rchild = c;
            }

            c.parent = a.parent;
            c.lchild = b;
            c.rchild = a;

            a.parent = c;
            a.lchild = r;

            b.parent = c;
            b.rchild = q;

            if (q != null) q.parent = b;
            if (r != null) r.parent = a;

            Node.UpdateNode(b);
            Node.UpdateNode(a);
            Node.UpdateNode(c);

            return c;
        }

        private static Node GetNextNode(Node p)
        {
            if (p.rchild != null) return GetMinNode(p.rchild);
            while (p.parent != null && p.parent.lchild != p)
            {
                p = p.parent;
            }
            return p.parent;
        }
        private static Node GetPrevNode(Node p)
        {
            if (p.lchild != null) return GetMaxNode(p.lchild);
            while (p.parent != null && p.parent.rchild != p)
            {
                p = p.parent;
            }
            return p.parent;
        }

        public struct iterator
        {
            private readonly Node pointer;

            internal iterator(Node pointer)
            {
                this.pointer = pointer;
            }

            public bool IsPointerToFake
            {
                get
                {
                    return pointer == null;
                }
            }

            public static T operator !(iterator it)
            {
                return it.pointer.data;
            }
            public static iterator operator ++(iterator it)
            {
                return new iterator(GetNextNode(it.pointer));
            }
            public static iterator operator --(iterator it)
            {
                return new iterator(GetPrevNode(it.pointer));
            }

            public static iterator GetBeginIterator(AvlTree<T> tree)
            {
                return new iterator(GetMinNode(tree.root));
            }
            public static iterator GetBackIterator(AvlTree<T> tree)
            {
                return new iterator(GetMaxNode(tree.root));
            }
        }
        public iterator Begin()
        {
            return iterator.GetBeginIterator(this);
        }

        public iterator Back()
        {
            return iterator.GetBackIterator(this);
        }

        public iterator Find(T val)
        {
            Node t = root;
            while (t != null)
            {
                int s = comparer.Compare(val, t.data);
                if (s == 0) break;
                t = ((s < 0) ? t.lchild : t.rchild);
            }
            return new iterator(t);
        }

        public iterator LowerBound(T val)
        {
            Node best = null;
            Node cur = root;
            while (cur != null)
            {
                if (comparer.Compare(cur.data, val) < 0) cur = cur.rchild;
                else
                {
                    best = cur;
                    cur = cur.lchild;
                }
            }
            return new iterator(best);
        }
        public iterator UpperBound(T val)
        {
            Node best = null;
            Node cur = root;
            while (cur != null)
            {
                if (comparer.Compare(cur.data, val) <= 0) cur = cur.rchild;
                else
                {
                    best = cur;
                    cur = cur.lchild;
                }
            }
            return new iterator(best);
        }

        public class AvlTreeEnum : IEnumerator<T>
        {
            private iterator? it;
            private readonly int changesFingerprint;
            private readonly AvlTree<T> tree;
            public AvlTreeEnum(AvlTree<T> tree)
            {
                it = null;
                this.tree = tree;
                this.changesFingerprint = tree.changes;
            }

            T IEnumerator<T>.Current
            {
                get
                {
                    if (tree.changes != changesFingerprint) throw new InvalidOperationException();
                    return !it.Value;
                }
            }
            object IEnumerator.Current
            {
                get
                {
                    return ((IEnumerator<T>)this).Current;
                }
            }

            void IEnumerator.Reset()
            {
                it = null;
            }

            bool IEnumerator.MoveNext()
            {
                if (!it.HasValue)
                {
                    it = tree.Begin();
                }
                else
                {
                    ++it;
                }
                return !it.Value.IsPointerToFake;
            }

            void IDisposable.Dispose() { }
        }

        IEnumerator<T> IEnumerable<T>.GetEnumerator()
        {
            return new AvlTreeEnum(this);
        }

        IEnumerator IEnumerable.GetEnumerator()
        {
            return new AvlTreeEnum(this);
        }
#if DEBUG
        public void DebugPrintToConsole()
        {
            DebugPrintToConsole(root);
        }
        private void DebugPrintToConsole(Node n)
        {
            if (n == null) return;
            DebugPrintToConsole(n.lchild);
            Console.WriteLine(n.data.ToString());
            DebugPrintToConsole(n.rchild);
        }

        public bool checkTreeLinks()
        {
            if (root == null) return true;
            return checkTreeLinks(root);
        }

        private bool checkTreeLinks(Node t)
        {
            if (t.lchild != null)
            {
                if (t.lchild.parent != t) return false;
                if (!checkTreeLinks(t.lchild)) return false;
            }
            if (t.rchild != null)
            {
                if (t.rchild.parent != t) return false;
                if (!checkTreeLinks(t.rchild)) return false;
            }
            return true;
        }
#endif
    }
}
