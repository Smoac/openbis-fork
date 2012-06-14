#!/usr/bin/env python
'''
Creates a Wiggle file out of a BAM file

Prerequisites:
- BAM index file, create with 'samtools index <bamfile.bam>'
- BAM file must be sorted, sort with 'samtools sort <bamfile.bam> bamfile.sorted'

Documentation says: 'If an index for a BAM file exists (.bai), it will be opened automatically.
Without an index random access to reads via fetch() and pileup() is disabled.'

@author: Manuel Kohler
@copyright: ETH Zurich
@precondition:  Working pysam egg
'''

import sys, pysam

#def bam2wig():
#  '''Usage: python bam2wig <bamfile>'''  
#  
#  if sys.argv[1] == '':
#    print(my_function.__doc__)
#    sys.exit(1)
#  else:
bamfile_name = sys.argv[1]
#  
try:
    with pysam.Samfile(bamfile_name, 'rb') as bamfile:
        wigfile_name = bamfile_name.replace('.bam', '.wig')
        wigfile = open(wigfile_name, 'w')

        header_full = 'track type=wiggle_0 name=' + wigfile_name
        variable_wig = 'variableStep chrom='

        print >> wigfile, header_full
        print ('Number of genes: ') + str(bamfile.nreferences)
        for gene_names in range(bamfile.nreferences):
          try:
            print (bamfile.getrname(gene_names))
            '''UCSC conformity'''
            if bamfile.getrname(gene_names) == 'dmel_mitochondrion_genome':
              print >> wigfile, variable_wig + "M"
            else:
              print >> wigfile, variable_wig + bamfile.getrname(gene_names)
            for pile in bamfile.pileup(bamfile.getrname(gene_names)):
              print >> wigfile, pile.pos + 1, pile.n
          except ValueError as valerr:
            print ('Value error ' + str(valerr))
except IOError as err:
    print (bam_file + 'not found! ' + str(err))
