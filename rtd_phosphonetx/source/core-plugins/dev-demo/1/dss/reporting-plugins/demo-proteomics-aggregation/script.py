from ch.systemsx.cisd.openbis.generic.shared.api.v1.dto import SearchCriteria
from ch.systemsx.cisd.openbis.generic.shared.api.v1.dto import SearchSubCriteria
from ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria import MatchClause
from ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria import MatchClauseAttribute
from ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria import SearchOperator

BIO_EXPERIMENT = "Biological Experiment"
BIO_SAMPLE = "Biological Sample"
MS_SAMPLE = "MS Injection Sample"
SEARCH_EXPERIMENT = "Search Experiment"
ACCESION_NUMBER = "Accession Number"
DESCRIPTION = "Protein Description"

def countFiles(node):
    sum = 1
    if node.isDirectory():
        for child in node.getChildNodes():
            sum = sum + countFiles(child)
    return sum

def aggregate(parameters, tableBuilder):
    space = parameters.get('space').upper()
    searchCriteria = SearchCriteria()
    subCriteria = SearchCriteria()
    subCriteria.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.TYPE, "BIO*"))
    searchCriteria.addSubCriteria(SearchSubCriteria.createExperimentCriteria(subCriteria))
    bioSamples = searchService.searchForSamples(searchCriteria)
    
    searchCriteria = SearchCriteria()
    searchCriteria.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.TYPE, "MS_INJECTION"))
    subCriteria = SearchCriteria()
    subCriteria.operator = SearchOperator.MATCH_ANY_CLAUSES
    bioSample2ExperimentDict = {}
    msInjectionSample2bioSamplesDict = {}
    searchExperiment2msInjectionSamplesDict = {}
    searchExperimentsByPermIdDict = {}
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
                bioSamples = []
                for parent in sample.sample.parents:
                    if parent.identifier in bioSample2ExperimentDict:
                        bioSamples.append(parent.identifier)
                msInjectionSample2bioSamplesDict[sample.sampleIdentifier] = bioSamples
                subCriteria.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.CODE, sample.code))
            searchCriteria.addSubCriteria(SearchSubCriteria.createSampleParentCriteria(subCriteria))
            searchSamples = searchService.searchForSamples(searchCriteria)
            for sample in searchSamples:
                experiment = sample.experiment
                searchExperimentsByPermIdDict[experiment.permId] = experiment
                msInjectionSample = sample.sample.parents[0].identifer
        
    experiments = {}
    for sample in searchSamples:
#        print "search sample: " + sample.sampleIdentifier+" "+experiment.experimentIdentifier+" "+experiment.permId
        experiments[experiment.permId] = experiment
    
    print experiments.keys()
    protein = parameters.get('protein')
    result = queryService.select("proteomics-db", 
                                 """select e.perm_id, accession_number, description 
                                    from experiments as e join data_sets as d on d.expe_id = e.id 
                                    join proteins as p on p.dase_id = d.id 
                                    join identified_proteins as ip on ip.prot_id = p.id
                                    join sequences as s on ip.sequ_id = s.id
                                    join protein_references as pr on s.prre_id = pr.id 
                                    where accession_number like ?{1} or description like ?{1} 
                                    order by perm_id""", 
                                ['%' + protein + '%'])
    tableBuilder.addHeader(SEARCH_EXPERIMENT)
    tableBuilder.addHeader(ACCESION_NUMBER)
    tableBuilder.addHeader(DESCRIPTION)
    for resultRow in result:
        permId = resultRow.get('perm_id')
        if (permId in experiments):
            experiment = experiments[permId]
            row = tableBuilder.addRow()
            row.setCell(SEARCH_EXPERIMENT, experiment.experimentIdentifier)
            row.setCell(ACCESION_NUMBER, resultRow.get('accession_number'))
            row.setCell(DESCRIPTION, resultRow.get('description'))
