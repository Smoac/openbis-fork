import unittest

class DropboxUnitTests(unittest.TestCase):

    def test_parse_incoming_dir_name(self):
        self.assertEqual(("batchName", "barCode"), parse_incoming_dirname("batchName barCode 2011-06-28"))
        self.assertEqual(("batchName", "barCode"), parse_incoming_dirname("batchName    barCode     2011-06-28"))
        
    def test_parse_incoming_dir_name(self):
        self.assertEqual(("batchName", "barCode"), parse_incoming_dirname("batchName barCode 2011-06-28"))
        self.assertEqual(("batchName", "barCode"), parse_incoming_dirname("batchName    barCode     2011-06-28"))        