#!/usr/local/dsu/Python-3.2/python

'''
Just a litlle helper function which can rename files given a regex and replace
this with another string

'''

import os
import re
import argparse

def parseCommandLine():
  parser = argparse.ArgumentParser(description='Renames files and removes a given ' + 
                                  'regex from the file name')
  parser.add_argument('-d', '--directory', dest='dir', action='store',
                   required=True, help='Directory conatining the files')  
  parser.add_argument('-r', '--regex', dest='regular_expression', action='store',
                    required=True, type=str, help='Regex which should be removed from file name')
  parser.add_argument('-w', '--with', dest='replacement', action='store',
                    required=True, type=str, help='replacement string')

  args = parser.parse_args()
  return(args)


def ren(old, replace, replacement):
  new = re.sub(replace, replacement, old)
  if (new != old):
    print ("rename: " + old + " to " + new)
    os.rename(old, new)
  else:
    print(old + " left unchanged!")

args = parseCommandLine()

os.chdir(args.dir)
list_of_files = os.listdir(args.dir)
list_of_files.sort()
[ren(file, args.regular_expression, args.replacement) for file in list_of_files ]
print('*** ' + str(len(list_of_files)) + " files processed ***")
