#!/usr/bin/env python
# Manuel Kohler, ETH Zuerich, 12/2010

# Creates a Wiggle file out of a BAM file

# Prerequisite:  BAM index file 
# Documentation says: 'If an index for a BAM file exists (.bai), it will be opened automatically.
# Without an index random access to reads via fetch() and pileup() is disabled.'

import sys, pysam
bamfile_name = sys.argv[1]
bamfile = pysam.Samfile(bamfile_name,'rb')
wigfile_name = bamfile_name.replace('.bam', '.wig')
wigfile = open(wigfile_name, 'w')
header_full = 'track type=wiggle_0 name=' + wigfile_name
variable_wig = 'variableStep chrom=chr'

print >> wigfile, header_full
for gene_names in range(bamfile.nreferences):
  # UCSC conformity
  if bamfile.getrname(gene_names) == 'dmel_mitochondrion_genome':
    print >> wigfile, variable_wig+'M'
  else:
    print >> wigfile, variable_wig+bamfile.getrname(gene_names)
  for pile in bamfile.pileup(bamfile.getrname(gene_names)):
    print >> wigfile, pile.pos, pile.n 

bamfile.close()
wigfile.close()
