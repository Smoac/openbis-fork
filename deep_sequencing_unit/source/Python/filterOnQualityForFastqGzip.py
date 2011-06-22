#!/usr/bin/python
'''
Usage: python filterOnQualityForFastqGzip.py <fastqfile.gz>
Reqires bcltofastq converted gzipped fastq file which are generated 
by the Illumina pipeline Casava 1.8+
Calculates the amount of chastity filtered reads in a gzipped fastq file
  
@author: Manuel Kohler
@copyright: ETH Zurich
@precondition: gzip, python 3.2
'''

import gzip 
import sys
import argparse


def parseCommandLine():
  parser = argparse.ArgumentParser(description='Counts the filtered and ' + 
                                  'non-filtered read of a gzipped fastq file')
  parser.add_argument('-f', '--file', dest='fastq_file', action='store',
                   required=True, help='Which fastq.gz file you want to process?')  

  args = parser.parse_args()
  return(args)

line_number = 1
new_line = 1
is_filtered = 0
width = 20

def formatNumber(n):
  return ('{:>20}'.format('{:,}'.format(n)))

args = parseCommandLine()

with gzip.open(args.fastq_file, 'rb') as file:
  for line in file:
    if (line_number == new_line):
      # fastq quadruples
      new_line = line_number + 4
      l = line.decode('utf8')
      if (l.split(':')[7] == 'Y'):
        is_filtered += 1
    line_number += 1

print('File: ' + args.fastq_file)
unfiltered = ((line_number - 1) / 4) - is_filtered
print(str(formatNumber(is_filtered)) + ' Number of filtered reads (BAD)')
print(str(formatNumber(int(unfiltered))) + ' Number of non-filtered reads (GOOD)')
print('\n')
