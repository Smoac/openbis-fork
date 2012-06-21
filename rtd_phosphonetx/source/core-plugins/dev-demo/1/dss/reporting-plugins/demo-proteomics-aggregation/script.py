from java.util import ArrayList
from ch.systemsx.cisd.openbis.generic.shared.api.v1.dto import SearchCriteria
from ch.systemsx.cisd.openbis.generic.shared.api.v1.dto import SearchSubCriteria
from ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria import MatchClause
from ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria import MatchClauseAttribute
from ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria import SearchOperator

BIO_EXPERIMENT = "Biological Experiment"
BIO_SAMPLE = "Biological Sample"
MS_SAMPLE = "MS Injection Sample"
SEARCH_EXPERIMENT = "Search Experiment"
SEARCH_EXPERIMENT_PERM_ID = "Search Experiment Perm ID"
ACCESION_NUMBER = "Accession Number"
DESCRIPTION = "Protein Description"

def countFiles(node):
    sum = 1
    if node.isDirectory():
        for child in node.getChildNodes():
            sum = sum + countFiles(child)
    return sum


def gatherExperimentsAndSamples(space):
    bioSample2ExperimentDict = {}
    msInjectionSample2bioSamplesDict = {}
    searchExperiment2msInjectionSamplesDict = {}
    searchExperimentsByPermIdDict = {}
    searchCriteria = SearchCriteria()
    subCriteria = SearchCriteria()
    subCriteria.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.TYPE, "BIO*"))
    searchCriteria.addSubCriteria(SearchSubCriteria.createExperimentCriteria(subCriteria))
    bioSamples = searchService.searchForSamples(searchCriteria)
    searchCriteria = SearchCriteria()
    searchCriteria.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.TYPE, "MS_INJECTION"))
    subCriteria = SearchCriteria()
    subCriteria.operator = SearchOperator.MATCH_ANY_CLAUSES
    for sample in bioSamples:
        if space == sample.space:
            bioSample2ExperimentDict[sample.sampleIdentifier] = sample.experiment.experimentIdentifier
            subCriteria.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.CODE, sample.code))
    
    if len(bioSample2ExperimentDict) > 0:
        searchCriteria.addSubCriteria(SearchSubCriteria.createSampleParentCriteria(subCriteria))
        msInjectionSamples = searchService.searchForSamples(searchCriteria)
        if len(msInjectionSamples) > 0:
            searchCriteria = SearchCriteria()
            searchCriteria.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.TYPE, "SEARCH"))
            subCriteria = SearchCriteria()
            subCriteria.operator = SearchOperator.MATCH_ANY_CLAUSES
            for sample in msInjectionSamples:
                msInjectionSample2bioSamplesDict[sample.sampleIdentifier] = sample.parentSampleIdentifiers
                subCriteria.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.CODE, sample.code))
            searchCriteria.addSubCriteria(SearchSubCriteria.createSampleParentCriteria(subCriteria))
            searchSamples = searchService.searchForSamples(searchCriteria)
            for sample in searchSamples:
                experiment = sample.experiment
                searchExperimentsByPermIdDict[experiment.permId] = experiment
                expId = experiment.experimentIdentifier
                for msInjectionSample in sample.parentSampleIdentifiers:
                    if expId in searchExperiment2msInjectionSamplesDict:
                        searchExperiment2msInjectionSamplesDict[expId].append(msInjectionSample)
                    else:
                        searchExperiment2msInjectionSamplesDict[expId] = [msInjectionSample]
    return bioSample2ExperimentDict, msInjectionSample2bioSamplesDict, \
           searchExperiment2msInjectionSamplesDict, searchExperimentsByPermIdDict
           
def createPermIdList(searchExperimentsByPermIdDict):
    permIds = ""
    for exp in searchExperimentsByPermIdDict.values():
        if len(permIds) > 0:
            permIds = permIds + ", "
        permIds = permIds + "'" + exp.permId + "'"
    return permIds

def aggregate(parameters, tableBuilder):
    space = parameters.get('space').upper()
    bioSample2ExperimentDict, msInjectionSample2bioSamplesDict, \
            searchExperiment2msInjectionSamplesDict, searchExperimentsByPermIdDict \
            = gatherExperimentsAndSamples(space)
    
    tableBuilder.addHeader(BIO_EXPERIMENT)
    tableBuilder.addHeader(BIO_SAMPLE)
    tableBuilder.addHeader(MS_SAMPLE)
    tableBuilder.addHeader(SEARCH_EXPERIMENT)
    tableBuilder.addHeader(SEARCH_EXPERIMENT_PERM_ID)
    tableBuilder.addHeader(ACCESION_NUMBER)
    tableBuilder.addHeader(DESCRIPTION)
    if len(searchExperimentsByPermIdDict) == 0:
        return
    protein = '%' + parameters.get('protein') + '%'
    permIds = createPermIdList(searchExperimentsByPermIdDict)
    result = queryService.select("proteomics-db", 
                                 """select e.perm_id, accession_number, description 
                                    from experiments as e join data_sets as d on d.expe_id = e.id 
                                    join proteins as p on p.dase_id = d.id 
                                    join identified_proteins as ip on ip.prot_id = p.id
                                    join sequences as s on ip.sequ_id = s.id
                                    join protein_references as pr on s.prre_id = pr.id 
                                    where e.perm_id in (""" 
                                    + permIds + 
                                    """) and (accession_number like ?{1} or description like ?{1}) 
                                    order by perm_id""", 
                                [protein])
    for resultRow in result:
        permId = resultRow.get('perm_id')
        if (permId in searchExperimentsByPermIdDict):
            experiment = searchExperimentsByPermIdDict[permId]
            expId = experiment.experimentIdentifier
            msInjectionSamples = searchExperiment2msInjectionSamplesDict[expId]
            for msInjectionSample in msInjectionSamples:
                bioSamples = msInjectionSample2bioSamplesDict[msInjectionSample]
                for bioSample in bioSamples:
                    bioExperiment = bioSample2ExperimentDict[bioSample]
                    row = tableBuilder.addRow()
                    row.setCell(BIO_EXPERIMENT, bioExperiment)
                    row.setCell(BIO_SAMPLE, bioSample)
                    row.setCell(MS_SAMPLE, msInjectionSample)
                    row.setCell(SEARCH_EXPERIMENT, expId)
                    row.setCell(SEARCH_EXPERIMENT_PERM_ID, permId)
                    row.setCell(ACCESION_NUMBER, resultRow.get('accession_number'))
                    row.setCell(DESCRIPTION, resultRow.get('description'))
    result.close()
