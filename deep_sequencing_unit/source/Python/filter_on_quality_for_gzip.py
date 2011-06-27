#!/usr/local/dsu/Python-3.2/python
'''
Usage: python filter_on_quality_for_gzip.py <fastqfilei.gz>
Reqires bcltofastq converted gzipped fastq file which are generated 
by the Illumina pipeline Casava 1.8+
  
@author: Manuel Kohler
@copyright: ETH Zurich
@precondition: gzip
'''

import gzip 
import sys
import argparse


def parseCommandLine():
  parser = argparse.ArgumentParser(description='Counts the filtered and ' + 
                                  'non-filtered read of a gzipped fastq file')
  parser.add_argument('-i', '--input_file', dest='fastq_file', action='store',
                   required=True, help='Which fastq.gz file you want to process?')  
  parser.add_argument('-o', '--output', dest='output', action='store', default='fastq_stats',
                  type=str, help='Output file name')
  parser.add_argument('-f', '--format', dest='format', action='store', default='txt',
                   type=str, choices=['txt', 'json', 'both'], help='Output format')

  args = parser.parse_args()
  return(args)

line_number = 1
new_line = 1
is_filtered = 0

def formatNumber(n):
  return ('{:>20}'.format('{:,}'.format(n)))

def calulatePercentage(v1, v2):
  return round(100 * (v1 / (v1 + v2)), 2)

args = parseCommandLine()

with gzip.open(args.fastq_file, 'rb') as file:
  for line in file:
    if (line_number == new_line):
      # fastq quadruples
      new_line = line_number + 4
      l = line.decode('utf8')
      #print(l.split(':')[7]) 
      if (l.split(':')[7] == 'Y'):
        is_filtered += 1
    line_number += 1

unfiltered = ((line_number - 1) / 4) - is_filtered
print('File: ' + args.fastq_file)
print(str(formatNumber(int(unfiltered))) + ' number of non-filtered reads (GOOD)')
print(str(formatNumber(is_filtered)) + ' number of filtered reads (BAD)')
print(str(formatNumber(calulatePercentage(unfiltered, is_filtered))) + ' % of non-filtered reads (GOOD)')
print(str(formatNumber(calulatePercentage(is_filtered, unfiltered))) + ' % of filtered reads (BAD)')
