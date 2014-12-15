'''
This is handling bowtie-BAM files and extracts some properties from the BAM header and
the samtools flagstat command. The results are formatted and attached  as a property
to the openBIS DataSet.
Prerequisites are the DataSetType: ALIGNMENT and
the following properties assigned to the DataSetType mentioned above:
ALIGNMENT_SOFTWARE, ISSUED_COMMAND, SAMTOOLS_FLAGSTAT,
TOTAL_READS, MAPPED_READS

Obviously you need a working samtools binary

Uses 'flagstat' and 'view -H'
'''

import os
import fnmatch
import re
from ch.systemsx.cisd.openbis.generic.shared.api.v1.dto import SearchCriteria
from ch.systemsx.cisd.openbis.generic.shared.api.v1.dto import SearchSubCriteria

FOLDER='/links/shared/dsu/dss/register-bowtie/'
SAMTOOLS='/links/application/dsu/samtools/samtools'
BAM_PATTERN='*.bam'

matches = []
searchStrings = ['@PG']
programList = []

# -----------------------------------------------------------------------------

def process(transaction):

  incomingPath = transaction.getIncoming().getName()

  dataSet = transaction.createNewDataSet("ALIGNMENT")
  dataSet.setMeasuredData(False)

  matches.append(incomingPath)
  # Add the incoming file into the data set
  transaction.moveFile(incomingPath, dataSet)
