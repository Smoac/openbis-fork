#! /usr/bin/env python
'''
@copyright:
Copyright 2012 ETH Zuerich, CISD
 
 @license:
 Licensed under the Apache License, Version 2.0 (the 'License');
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at
  
  http://www.apache.org/licenses/LICENSE-2.0
   
   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an 'AS IS' BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.

   @author:
   Manuel Kohler

   @description:
   Calulates the crc32 checksum of a file and returns it in a hexadecimal number
'''

import zlib
from optparse import OptionParser


def parseOptions():
    parser = OptionParser(version='%prog 1.0')
    parser.add_option('-f', '--file', dest='file', 
    help='File for calculation of crc32 checksum',
    metavar='<file>') 

    (options, args) = parser.parse_args()
    if options.file is None:
      parser.print_help()
      exit(-1)
    return options


def crc(fileName):
  prev = 0
  for eachLine in open(fileName,"rb"):
    prev = zlib.crc32(eachLine, prev)
  return "%X"%(prev & 0xFFFFFFFF)

def main():

  myoptions = parseOptions()
  value = crc(myoptions.file)
  print(value)

if __name__ == "__main__":
  main()
