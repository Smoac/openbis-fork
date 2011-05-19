#!/usr/bin/python
'''
Concatenates several pdfs in a given directory depending
on the lane number in the file name. 
This number is defined as the last number in the file name
separated by '_'
The concatenated file will be named:
flow_cell + '_' + lane + '_quality.pdf'

@author: Manuel Kohler
@copyright: ETH Zurich
@precondition: Working pdftk installation
'''

import os
import fnmatch
import subprocess
from optparse import OptionParser

PDFTK = '/usr/local/dsu/pdftk-1.41/pdftk/pdftk'
LANES = 8

class pdfList(list):
  def __init__ (self, lane, pdfs=[]):
    list.__init__([])
    self.lane = lane
    self.extend(pdfs)

def options():
  parser = OptionParser()
  parser.add_option("-p", "--path", dest="path", action="store", type="string",
                  help="Path of the fastq files", metavar="PATH")
  (options, args) = parser.parse_args()
  return (options.path)

def concatenatePdfs(file_list, lane):
  # take the first list element as a reference for the final pdf name
  flow_cell = '_'.join(file_list[0].split('_', 4)[0:4])
  print(flow_cell)
  new_name = flow_cell + '_' + lane + '_quality.pdf'
  args = [PDFTK, 'cat', 'output', new_name]
  [args.insert(1, i) for i in file_list]
  a = subprocess.Popen(args)
  a.wait()
  [os.remove(file) for file in file_list]

def createPdfList (lane):
  path = options()
  # create pdf list depending on lane
  ll = 'lane' + lane
  vars()[ll] = pdfList(lane)

  for file in os.listdir(path):
    if fnmatch.fnmatch(file, '*.pdf'):
      for l in (file.split('_')[4]):
          if (l == lane):
            (vars()[ll]).append(file)
  vars()[ll].sort()
  return (vars()[ll])

def __main__():
  list_of_pdfs = []
  for i in range(1, LANES + 1):
    l = createPdfList(str(i))
    list_of_pdfs.append(l)
  
  [concatenatePdfs(list_of_pdfs[i], list_of_pdfs[i].lane) for i in range(len(list_of_pdfs))]    
  print("DONE...")

__main__()
