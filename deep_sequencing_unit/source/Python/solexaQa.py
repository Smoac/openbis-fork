#!/usr/bin/env python
'''
Python Wrapper for a perl based program.
Generates tile based quality graphics

Output: pdf 
@author: Manuel Kohler
@copyright: ETH Zurich
@precondition: Working pdftk and SolexaQA.pl binaries

'''

import subprocess
import shlex
import os
import sys
from optparse import OptionParser

SCRIPT = "/usr/local/dsu/bin/SolexaQA/SolexaQA.pl"
PDFTK = '/usr/local/dsu/pdftk-1.41/pdftk/pdftk'

parser = OptionParser()
parser.add_option("-p", "--path", dest="path", action="store", type="string",
                  help="Path of the fastq files", metavar="PATH")
parser.add_option("-v", dest="verbose", action="store_true",
                  help="tell me all")
(options, args) = parser.parse_args()
path = options.path
os.chdir(path)

listdir = os.listdir(path)
for file in listdir:
  if file.endswith('fastq'):
    args = SCRIPT + " -sanger " + path + "/" + file
    Newargs = shlex.split(args)
    p = subprocess.Popen(Newargs)
    p.wait()
    #convert -page A4 ETHZ_BSSE_110204_62Y8YAAXX_7.fastq.png  ETHZ_BSSE_110204_62Y8YAAXX_7.fastq_TileStatistics_A4.pdf
    convert = ['convert', '-page', 'A4', file + '.png', file + '.png.pdf']
    c = subprocess.Popen(convert)
    c.wait()
    #rm *.matrix *.segments *.quality *.png
    os.remove(file + '.matrix')
    os.remove(file + '.segments')
    os.remove(file + '.quality')
    os.remove(file + '.png')
   
    files = file + '.segments.hist.pdf', file + '.quality.pdf', file + '.png.pdf' 
    assemble = [ PDFTK, 'cat', 'output', file.replace('.', '_', 1) + '_SolexaQA.pdf']
    [assemble.insert(1, i) for i in files]
    a = subprocess.Popen(assemble)
    a.wait()
    [os.remove(f) for f in files] 
print("Finished...")
