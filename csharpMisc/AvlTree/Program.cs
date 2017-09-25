using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace AvlTree
{
    class Program
    {
        static void Main(string[] args)
        {
            AvlTree<int> t = new AvlTree<int>();
            for (int i = 0; i < 10; ++i)
                t.Insert(i);
            foreach (var c in t)
                Console.WriteLine(c);
        }
    }
}
