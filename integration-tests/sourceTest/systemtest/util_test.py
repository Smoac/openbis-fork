import unittest

import systemtest.util as util
from testcasewithfiles import TestCaseWithFiles


class UtilTest(TestCaseWithFiles):
    def test_readProperties(self):
        example = self.createPath("my.properties")
        with open(example, "w") as out:
            out.write("# a comment\n\n")
            out.write("      \n")
            out.write(" alpha = beta  \n")
            out.write("  non=\n")
            
        keyValuePairs = util.readProperties(example)
        
        self.assertEqual('beta', keyValuePairs['alpha'])
        self.assertEqual('', keyValuePairs['non'])
        self.assertEqual(2, len(keyValuePairs))
        
    def test_writeProperties(self):
        example = self.createPath("my.props")
        
        util.writeProperties(example, {'alpha': 4711, 'beta': 'hello'})
        
        with open(example, "r") as f:
            self.assertEqual(['alpha=4711\n', 'beta=hello\n'], sorted(f.readlines()))
        
if __name__ == '__main__':
    unittest.main()