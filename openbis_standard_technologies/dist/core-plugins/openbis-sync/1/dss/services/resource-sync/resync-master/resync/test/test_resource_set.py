import unittest
import re
from resync.resource import Resource
from resync.resource_set import ResourceSet,ResourceSetDupeError

class TestResourceSet(unittest.TestCase):

    def test01_add(self):
        rs = ResourceSet()
        self.assertEqual( len(rs), 0 )
        rs.add( Resource('a') )
        self.assertEqual( len(rs), 1 )
        rs.add( Resource('b') )
        self.assertEqual( len(rs), 2 )
        rs.add( Resource('c') )
        self.assertEqual( len(rs), 3 )

    def test02_order(self):
        rs = ResourceSet()
        rs.add( Resource('a2') )
        rs.add( Resource('a3') )
        rs.add( Resource('a1') )
        i = iter(rs)
        self.assertEqual( i.next().uri, 'a1' )
        self.assertEqual( i.next().uri, 'a2' )
        self.assertEqual( i.next().uri, 'a3' )
        self.assertRaises( StopIteration, i.next )

    def test03_dupe(self):
        rs = ResourceSet()
        self.assertEqual( len(rs), 0 )
        rs.add( Resource('a') )
        self.assertEqual( len(rs), 1 )
        self.assertRaises( ResourceSetDupeError, rs.add, Resource('a') )


if __name__ == '__main__':
    suite = unittest.TestLoader().loadTestsFromTestCase(TestResourceSet)
    unittest.TextTestRunner(verbosity=2).run(suite)
