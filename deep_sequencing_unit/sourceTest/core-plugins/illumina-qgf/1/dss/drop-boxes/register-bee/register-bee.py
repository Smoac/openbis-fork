from __future__ import with_statement  
import os
from ch.systemsx.cisd.openbis.generic.shared.api.v1.dto import SearchCriteria
from ch.systemsx.cisd.common.mail import EMailAddress

def process (transaction):


    DATA_SET_TYPE_ALIGNMENT = "ALIGNMENT"
    ETHZ_DOMAIN = "@ethz.ch"
    METADATA_FILE='metadata.properties'

    def searchDs(transaction, dscode):
        # search for the data set
        search_service = transaction.getSearchService();
        sc = SearchCriteria();
        sc.addMatchClause(SearchCriteria.MatchClause.createAttributeMatch(SearchCriteria.MatchClauseAttribute.CODE, dscode));
        myds = search_service.searchForDataSets(sc);
        assert (myds.size() == 1);
        return myds;

    # Search for parent data set of the same sample
    #dataSetSc = SearchCriteria()
    #dataSetSc.addMatchClause(SearchCriteria.MatchClause.createAttributeMatch(SearchCriteria.MatchClauseAttribute.TYPE, 'FASTQ_GZ'))
    #dataSetSc.addSubCriteria(SearchSubCriteria.createSampleCriteria(sc))
    #foundDataSets = search_service.searchForDataSets(dataSetSc)
    #if foundDataSets.size() > 0:
    #  dataSet.setParentDatasets([ds.getDataSetCode() for ds in foundDataSets])

    path = transaction.getIncoming().getAbsolutePath()

    myvars = {}
    with open(os.path.join(path, METADATA_FILE)) as myfile:
        for line in myfile:
            name, var = line.partition("=")[::2]
            myvars[name.strip()] = var
    print myvars
    userId = myvars['analysis_procedure'].split()[-1][1:-1]
    dsCode = myvars['storage_provider.parent.dataset.id']
    print userId
    print dsCode

    if (not "@" in userId):
	userId = userId + ETHZ_DOMAIN;

    userMail = EMailAddress(userId)

    existingDs = searchDs(transaction, dsCode);
    print existingDs
    firstDs = existingDs.get(0);

    newDataSet = transaction.createNewDataSet(DATA_SET_TYPE_ALIGNMENT);
    experiment = firstDs.getExperiment();

    newDataSet.setParentDatasets([ds.getDataSetCode() for ds in existingDs])
    newDataSet.setExperiment(experiment);
    transaction.moveFile(transaction.getIncoming().getAbsolutePath(), newDataSet);

    replyTo = EMailAddress("no-reply@bsse.ethz.ch")
    fromAddress = replyTo

    # transaction.getGlobalState().getMailClient().sendEmailMessage(subject, content, replyToOrNull, fromOrNull, recipients)
    transaction.getGlobalState().getMailClient().sendEmailMessage("openBIS Bee cluster job finished",
	    "Result data for " + dsCode + " got registered in openBIS.", replyTo, fromAddress, userMail);
