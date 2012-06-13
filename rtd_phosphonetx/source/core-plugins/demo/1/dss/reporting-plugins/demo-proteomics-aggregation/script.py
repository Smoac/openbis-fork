from ch.systemsx.cisd.openbis.generic.shared.api.v1.dto import SearchCriteria
from ch.systemsx.cisd.openbis.generic.shared.api.v1.dto import SearchSubCriteria
from ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria import MatchClause
from ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria import MatchClauseAttribute

EXPERIMENT = "Experiment"
ACCESION_NUMBER = "Accession Number"
DESCRIPTION = "Protein Description"

def countFiles(node):
    sum = 1
    if node.isDirectory():
        for child in node.getChildNodes():
            sum = sum + countFiles(child)
    return sum

def aggregate(parameters, tableBuilder):
#    space = parameters.get('space-code')
    protein = parameters.get('protein')
#    searchCriteria = SearchCriteria()
#    subCriteria = SearchCriteria()
#    subCriteria.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.CODE, experimentCode))
#    searchCriteria.addSubCriteria(SearchSubCriteria.createExperimentCriteria(subCriteria))
#    dataSets = searchService.searchForDataSets(searchCriteria)
    
    result = queryService.select("proteomics-db", 
                                 """select e.perm_id, accession_number, description 
                                    from experiments as e join data_sets as d on d.expe_id = e.id 
                                    join proteins as p on p.dase_id = d.id 
                                    join identified_proteins as ip on ip.prot_id = p.id
                                    join sequences as s on ip.sequ_id = s.id
                                    join protein_references as pr on s.prre_id = pr.id 
                                    where accession_number like ?{1} or description like ?{1}""", ['%' + protein + '%'])
    tableBuilder.addHeader(EXPERIMENT)
    tableBuilder.addHeader(ACCESION_NUMBER)
    tableBuilder.addHeader(DESCRIPTION)
    for resultRow in result:
        row = tableBuilder.addRow()
        row.setCell(EXPERIMENT, resultRow.get('perm_id'))
        row.setCell(ACCESION_NUMBER, resultRow.get('accession_number'))
        row.setCell(DESCRIPTION, resultRow.get('description'))
