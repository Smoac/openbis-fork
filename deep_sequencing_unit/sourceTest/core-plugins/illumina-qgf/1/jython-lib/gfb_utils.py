import os
import time
import fnmatch
from time import *
from datetime import *
from ch.systemsx.cisd.openbis.generic.shared.api.v1.dto import SearchCriteria

# HISEQ_DICT = {'HISEQ_4000': 'Illumina HiSeq 4000',
#                   'HISEQ_3000': 'Illumina HiSeq 3000',
#                   'HISEQ_2500': 'Illumina HiSeq 2500',
#                   'HISEQ_2000': 'Illumina HiSeq 2000',
#                   'HISEQ_X': 'Illumina HiSeq X'
#                   }
# NEXTSEQ_DICT = {'NEXTSEQ_500': 'Illumina NextSeq 500'}
# MISEQ_DICT = {'MISEQ': 'Illumina MiSeq'}
# UNIDENTIFIED_DICT = {'UNIDENTIFIED': 'Unidentified'}

# Instead of using Enum:
class Sequencers:
    HISEQ_4000, HISEQ_3000, HISEQ_2500, HISEQ_2000, HISEQ_X, NEXTSEQ_500, MISEQ , UNIDENTIFIED= \
        ('Illumina HiSeq 4000','Illumina HiSeq 3000','Illumina HiSeq 2500','Illumina HiSeq 2000',
         'Illumina HiSeq X', 'Illumina NextSeq 500', 'Illumina MiSeq', 'Unidentified')

HISEQ_LIST = [Sequencers.HISEQ_2000, Sequencers.HISEQ_2500, Sequencers.HISEQ_3000, Sequencers.HISEQ_4000, Sequencers.HISEQ_X]


def get_model(run_id):
    """
    Guesses the sequencer model from the run folder name

    Current Naming schema for Illumina run folders, as far as I know,
    no documentation found on this, Illumina introduced a field called
    <InstrumentID> on the NextSeq runParameters.xml. That might be an
    option for the future. Alternatively a combination of the fields
    <ApplicationName> and <ApplicationVersion>.

    MiSeq: 150130_M01761_0114_000000000-ACUR0
    NextSeq: 150202_NS500318_0047_AH3KLMBGXX
    HiSeq 2000: 130919_SN792_0281_BD2CHRACXX
    HiSeq 2500: 150203_D00535_0052_AC66RWANXX
    HiSeq 3000: 150724_J00121_0017_AH2VYMBBXX
    HiSeq 4000: 150210_K00111_0013_AH2372BBXX
    HiSeq X: 141121_ST-E00107_0356_AH00C3CCXX
    """
    date, machine_id, run_number, fc_string = os.path.basename(run_id).split("_")

    if machine_id.startswith("NS"):
        model = Sequencers.NEXTSEQ_500
    elif machine_id.startswith("M"):
        model = Sequencers.MISEQ
    elif machine_id.startswith("D"):
        model = Sequencers.HISEQ_2500
    elif machine_id.startswith("SN"):
        model = Sequencers.HISEQ_2000
    elif machine_id.startswith("J"):
        model = Sequencers.HISEQ_3000
    elif machine_id.startswith("K"):
        model = Sequencers.HISEQ_4000
    elif machine_id.startswith("ST"):
        model = Sequencers.HISEQ_X
    else:
        model = Sequencers.UNIDENTIFIED
    return model


def get_thread_properties(transaction):
    threadPropertyDict = {}
    threadProperties = transaction.getGlobalState().getThreadParameters().getThreadProperties()
    for key in threadProperties:
        try:
            threadPropertyDict[key] = threadProperties.getProperty(key)
        except:
            pass
    return threadPropertyDict


def create_openbis_timestamp(file):
    '''
    Creates a openBIS compatible time stamp of a file time stamp
    '''
    mtime = os.path.getmtime(file)
    lt = localtime(mtime)
    tz = localtime().tm_hour - gmtime().tm_hour
    return (strftime("%Y-%m-%d %H:%M:%S GMT" + "%+.2d" % tz + ":00", lt))


def create_openbis_timestamp_now():
  '''
  Create an openBIS conform timestamp
  '''
  tz=localtime()[3]-gmtime()[3]
  d=datetime.now()
  return d.strftime("%Y-%m-%d %H:%M:%S GMT"+"%+.2d" % tz+":00")


def search_unique_sample(transaction, sample_code): 

    search_service = transaction.getSearchService()
    sc = SearchCriteria()
    sc.addMatchClause(SearchCriteria.MatchClause.createAttributeMatch(SearchCriteria.MatchClauseAttribute.CODE, sample_code));
    found_sample = search_service.searchForSamples(sc)
    try:
        assert found_sample.size() == 1
    except AssertionError:
        print (str(found_sample.size()) + ' samples found which match the criterias: '+ sample_code)
    return found_sample


def get_file_names(path, pattern):
    '''
    Gets all files matching a PATTERN in a path recursively
    and returns the result as a list
    '''
    matches = []
    for root, dirnames, filenames in os.walk(path):
        for filename in fnmatch.filter(filenames, pattern):
            matches.append(os.path.join(root, filename))
    matches.sort()
    return(matches)

