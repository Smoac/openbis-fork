'''
This is handling bowtie-BAM files and extracts some properties from the BAM header and
the samtools flagstat command. The results are formatted and attached  as a property
to the openBIS DataSet.
Prerequisites are the DataSetType: ALIGNMENT and
the following properties assigned to the DataSetType mentioned above:
ALIGNMENT_SOFTWARE, ISSUED_COMMAND, SAMTOOLS_FLAGSTAT,
TOTAL_READS, MAPPED_READS

Obviously you need a working samtools binary

Note: 
print statements go to: ~openbis/sprint/datastore_server/log/startup_log.txt
'''

import os
from ch.systemsx.cisd.openbis.generic.shared.api.v1.dto import SearchCriteria

FOLDER='/net/bs-dsu-data/array0/dsu/dss/incoming-jython-alignment/'
SAMTOOLS='/usr/local/dsu/samtools/samtools'

# Create a "transaction" -- a way of grouping operations together so they all
# happen or none of them do.
transaction = service.transaction()
 
# Create a data set and set type
dataSet = transaction.createNewDataSet("ALIGNMENT")
dataSet.setMeasuredData(False)

incomingPath = incoming.getAbsolutePath()

# Get the incoming name 
name = incoming.getName()
# expected incoming Name, e.g.:ETHZ_BSSE_110429_63558AAXX_1_sorted.bam
split=name.split("_")
sample=split[2]+ '_'+ split[3] + ':' + split[4] 

# Extract values from a samtools view and set the results as DataSet properties 
# Command: samtools view -H ETHZ_BSSE_110429_63558AAXX_1_sorted.bam
arguments = SAMTOOLS + ' view -H ' + FOLDER + name
#print('Arguments: '+ arguments)
cmdResult=os.popen(arguments).read()
properties=cmdResult.split("\n")[-2].split('\t')
aligner=(properties[1].split(':')[1].upper() +  '_' + properties[2].split(':')[1])
command=properties[3]

arguments = SAMTOOLS + ' flagstat ' + FOLDER + name
cmdResult=os.popen(arguments).read()
totalReads=cmdResult.split('\n')[0].split(' ')[0]
mappedReads=cmdResult.split('\n')[2].split(' ')[0]

dataSet.setPropertyValue("ALIGNMENT_SOFTWARE", aligner)
dataSet.setPropertyValue("ISSUED_COMMAND", command)
dataSet.setPropertyValue("SAMTOOLS_FLAGSTAT", cmdResult)
dataSet.setPropertyValue("TOTAL_READS", totalReads)
dataSet.setPropertyValue("MAPPED_READS", mappedReads)
 
# Add the incoming file into the data set
transaction.moveFile(incomingPath, dataSet)

# Get the search service
search_service = transaction.getSearchService()

# Search for the sample
sc = SearchCriteria()
sc.addMatchClause(SearchCriteria.MatchClause.createAttributeMatch(SearchCriteria.MatchClauseAttribute.CODE, sample));
foundSamples = search_service.searchForSamples(sc)

if foundSamples.size() > 0:
  dataSet.setSample(foundSamples[0])
