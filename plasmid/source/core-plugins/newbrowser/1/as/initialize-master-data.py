#
# Copyright 2014 ETH Zuerich, Scientific IT Services
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

# MasterDataRegistrationTransaction Class
import ch.systemsx.cisd.openbis.generic.server.jython.api.v1.DataType as DataType

##
## Help Methods
##
propertiesCache = {};
tr = service.transaction()

def createVocabularyWithTerms(vocabularyCode, terms):
	vocabulary = tr.createNewVocabulary(vocabularyCode);
	addTerms(vocabulary, terms);
	
def addTerms(vocabulary, terms):
	for term in terms:
		addTermWithLabel(vocabulary, term[0], term[1])
	
def addTermWithLabel(vocabulary, termCode, termLabel):
	newTerm = tr.createNewVocabularyTerm(termCode);
	newTerm.setLabel(termLabel);
	vocabulary.addTerm(newTerm);

def createExperimentTypeWithProperties(experimentTypeCode, description, properties):
	newExperiment = tr.getOrCreateNewExperimentType(experimentTypeCode);
	newExperiment.setDescription(description);
	addProperties(newExperiment, properties);

def addProperties(entity, properties):
	for property in properties:
		addProperty(entity, property[0], property[1], property[2], property[3], property[4]);

def addProperty(entity, propertyCode, section, propertyLabel, dataType, propertyDescription):
	property = None;
	
	if propertyCode in propertiesCache:
		property = propertiesCache[propertyCode];
	else:
		property = tr.getOrCreateNewPropertyType(propertyCode, dataType);
		property.setDescription(propertyDescription);
		property.setLabel(propertyLabel);
		propertiesCache[propertyCode] = property;
	
	propertyAssignment = tr.assignPropertyType(entity, property);
	propertyAssignment.setSection(section);

##
## Vocabularies
##

createVocabularyWithTerms("HOST", [
										["MOUSE", "mouse"],
										["RAT", "rat"],
										["GUINEA_PIG", "guinea pig"],
										["RABBIT", "rabbit"],
										["DONKEY", "donkey"]
									]);

createVocabularyWithTerms("DETECTION", [
										["HRP", "horseradish peroxydase"],
										["FLUORESCENCE", "fluorescent probe"]
									]);

createVocabularyWithTerms("STORAGE", [
										["RT", "room temperature"],
										["4", "+4 degrees"],
										["-20", "-20 degrees"],
										["-80", "-80 degrees"]
									]);

createVocabularyWithTerms("CLONALITY", [
										["MONOCLONAL", "monoclonal"],
										["POLYCLONAL", "polyclonal"],
										["UNKNOWN", "unknown"]
									]);

createVocabularyWithTerms("BACKBONE", [
										["PBLUESCRIPT_II_KS_PLUS", "pBluescript II KS +"],
										["PBSN", "pBSN"],
										["PSPPOLY_A", "pSPpoly(A)"],
										["PKERG10Y", "pKERG10y"],
										["PRS30Y", "pRS30y"],
										["PRS31Y", "pRS31y"],
										["PRS40Y", "pRS40y"],
										["PRS41Y", "pRS41y"],
										["PRS42Y", "pRS42y"],
										["PET22B", "pET22b"],
										["UNKNOWN", "unknown"],
										["PFA6", "pFA6"],
										["PGEX4T1", "pGEX4T1"],
										["PEG202", "pEG202"],
										["PJEXPRESS", "pJexpress"],
										["PJEXPRESS2", "pJexpress2"],
										["POLYLYS-PJEXPRESS2", "polyLys-pJexpress2"],
										["OSER", "OSER"]
									]);

createVocabularyWithTerms("BACTERIAL_ANTIBIOTIC_RESISTANCE", [
										["BLA", "bla"],
										["KAN", "kan"],
										["CAM", "cam"]
									]);

createVocabularyWithTerms("MARKER", [
										["URA3", "URA3"],
										["HIS3", "HIS3"],
										["LEU2", "LEU2"],
										["TRP1", "TRP1"],
										["MET15", "MET15"],
										["LYS2", "LYS2"],
										["ADE1", "ADE1"],
										["KANMX", "KanMX"],
										["NATMX", "NatMX"],
										["HYGMX", "HygMX"],
										["URA3MX", "Ura3MX"],
										["HIS3MX", "His3MX"],
										["BAR", "bar"],
										["CY_1", "Cy1"],
										["E_1", "e1"],
										["SB_1", "Sb1"],
										["W_1", "w1"],
										["Y1", "y1"],
									]);

createVocabularyWithTerms("STERILIZATION", [
										["AUTOCLAVATION", "autoclavation"],
										["FILTRATION", "filtration"],
										["NONE", "none"]
									]);

createVocabularyWithTerms("GENETIC_BACKGROUND", [
										["BY4743", "BY4743"],
										["BY4741", "BY4741"],
										["BY4742", "BY4742"],
										["CEN.PK2-1C", "CEN.PK2-1C"],
										["CEN.PK2-1D", "CEN.PK2-1D"],
										["CEN.PK2", "CEN.PK2"],
										["W303", "W303"],
										["W303-1A", "W303-1A"],
										["W303-1B", "W303-1B"],
										["S288C", "S288C"],
										["RM11", "RM11"],
										["RM11-A", "RM11-A"],
										["RM11-B", "RM11-B"],
										["UNKNOWN", "unknown"],
										["FY4", "FY4"]
									]);

createVocabularyWithTerms("MATING_TYPE", [
										["A", "a"],
										["ALPHA", "alpha"],
										["DIPLOID", "diploid"],
										["UNKNOWN", "unknown"]
									]);

createVocabularyWithTerms("BACKGROUND_SPECIFIC_MARKERS", [
										["MET15_LYS2", "met15- lys2-"],
										["MET15", "met15-"],
										["LYS2", "lys2-"],
										["TRP1_ADE2", "trp1- ade2-"],
										["TRP1", "trp1-"],
										["ADE2", "ade2-"],
										["MET15_TRP1", "met15- trp1-"],
										["HO_KAN", "ho::kanMX"],
										["NONE", "none"],
										["UNKNOWN", "unknown"],
										["MET15_LYS2_TRP1_ADE2", "met15- lys2- trp1- ade2-"],
										["LYS2_TRP1", "lys2- trp1-"],
										["MET15_LYS2_TRP1", "met15- lys2- trp1-"]
									]);

createVocabularyWithTerms("COMMON_MARKERS", [
										["URA3_HIS3_LEU2", "ura3- his3- leu2-"],
										["URA3_HIS3", "ura3- his3-"],
										["URA3_LEU2", "ura3- leu2-"],
										["URA3", "ura3-"],
										["HIS3_LEU2", "his3- leu2-"],
										["HIS3", "his3-"],
										["LEU2", "leu2-"],
										["NONE", "none"],
										["UNKNOWN", "unknown"]
									]);

createVocabularyWithTerms("ENDOGENOUS_PLASMID", [
										["CIR_PLUS", "cir+"],
										["CIR_ZERO", "cir0"],
										["UNKNOWN", "unknown"]
									]);

createVocabularyWithTerms("DIRECTION", [
										["FORWARD", "forward"],
										["REVERSE", "reverse"]
									]);

createVocabularyWithTerms("STRAND", [
										["DS", "double strand"],
										["SS", "single strand"]
									]);

createVocabularyWithTerms("RNA_TYPE", [
										["MIMIC", "mimic"],
										["INHIBITOR", "inhibitor"]
									]);

createVocabularyWithTerms("RNA_BACKBONE", [
										["LNA", "LNA"],
										["2_O_METHYL", "2-O-methylation"]
									]);

createVocabularyWithTerms("ORIGIN", [
										["CROSS", "cross"],
										["TRANSFORMATION", "transformation"],
										["SPORULATION", "transformation sporulation"],
										["NEGATIVE_SELECTION", "negative selection"],
										["TRANSFECTION", "transfection"]
									]);

createVocabularyWithTerms("CHECK", [
										["PCR", "PCR"],
										["MICROSCOPY", "microscopy"],
										["WB", "western blotting"],
										["SB", "southern blotting"],
										["PCR_MICROSCOPY", "PCR and microscopy"],
										["FLOWCYTOMETRY", "flow cytometry"],
										["PCR_FLOWCYTOMETRY", "PCR and flow cytometry"],
										["MORPHOLOGY", "morphology"],
										["OTHER", "other"],
										["NOTHING", "nothing"]
									]);

createVocabularyWithTerms("PROTOCOL_TYPE", [
										["DNA", "DNA method"],
										["RNA", "RNA method"],
										["PROTEINS", "proteins method"],
										["YEAST_BASICS", "yeast basic method"],
										["BACTERIA_BASICS", "bacteria basic method"],
										["FLUORESCENCE_MICROSCOPY", "fluorescence microscopy method"],
										["FLOW_CYTOMETRY", "flow cytometry method"],
										["CELL_SORTING", "cell sorting method"],
										["CELL_LINE_BASICS", "cell line basics"]
									]);

createVocabularyWithTerms("TEMPLATE", [
										["DNA", "DNA"],
										["RNA", "RNA"],
										["BACTERIA_COLONY", "bacteria colony"],
										["YEAST_COLONY", "yeast colony"]
									]);

createVocabularyWithTerms("YES_NO", [
										["YES", "yes"],
										["NO", "no"],
										["UNKNOWN", "unknown"]
									]);

createVocabularyWithTerms("MEMBRANE", [
										["PVDF", "PVDF"],
										["NITROCELLULOSE", "nitrocellulose"],
										["PVDF_NITROCELLULOSE", "Either PVDF or nitrocellulose"]
									]);

createVocabularyWithTerms("SPECIES", [
										["HOMO", "Homo sapiens"],
										["MOUSE", "Mus musculus"],
										["RAT", "Rattus norvegicus"],
										["PIG", "Sus scrofa"],
										["DROSOPHILA_MELANOGASTER", "Drosophila melanogaster"]
									]);

createVocabularyWithTerms("CELL_MEDIUM", [
										["RPMI", "rpmi"],
										["1640", "1640"],
										["ISCOVES", "iscoves"],
										["DMEM", "DMEM"],
										["DMEM_NUTRIENT_MIXTURE_F-12_HAM", "DMEM nutrient mixture F-12 HAM"],
										["DMEM_HIGH_GLUC", "DMEM high glucose"],
										["DMEM_LOW_GLUC", "DMEM low glucose"]
									]);

createVocabularyWithTerms("OWNER", [
										["FILL_ME_1", "Fill me with the people of your lab"],
										["FILL_ME_2", "Fill me with the people of your lab 2"]
									]);

createVocabularyWithTerms("CELL_TYPE", [
										["FIBROBLAST", "fibroblast"],
										["NEURON", "neuron"]
									]);

createVocabularyWithTerms("ORGANISM", [
										["BACTERIA", "Bacteria"],
										["BUDDING_YEAST", "Saccharomyces cerevisiae"],
										["MAMMALIAN", "mammalian"],
										["DROSOPHILA_MELANOGASTER", "Drosophila melanogaster"]
									]);

createVocabularyWithTerms("EXPERIMENTAL_READOUT", [
										["FLOW_CYTOMETRY", "flow citometry"],
										["SORTING", "cell sorting"],
										["GROWTH", "growth"],
										["WESTERN_BLOTTING", "western blottong"],
										["RT_QPCR", "RT-qPCR"]
									]);

createVocabularyWithTerms("MACHINE", [
										["LSRII_FORTESSA", "SRII Fortessa"],
										["TECAN_READER", "Tecan reader"],
										["BIOLECTOR", "BioLector"],
										["LICOR_ODYSSEY", "LI-COR Odyssey"],
										["TI_ECLIPSE", "TI Eclipse (Nikon)"],
										["SRX_101A", "Konica Minolta SRX-101A"],
										["LIGHT_CYCLER", "LightCycler 480"]
									]);
##
## Property Types
##
NOTES = tr.getOrCreateNewPropertyType("NOTES", DataType.MULTILINE_VARCHAR);
NOTES.setDescription("Notes regarding the dataset");
NOTES.setLabel("Notes");

##
## Experiment Types
##
createExperimentTypeWithProperties("ANTIBODY", "BOX TO HOLD SAMPLES OF THIS TYPE FOR ORGANIZATIONAL PURPOSES", []);
createExperimentTypeWithProperties("BACTERIA", "BOX TO HOLD SAMPLES OF THIS TYPE FOR ORGANIZATIONAL PURPOSES", []);
createExperimentTypeWithProperties("CHEMICAL", "BOX TO HOLD SAMPLES OF THIS TYPE FOR ORGANIZATIONAL PURPOSES", []);
createExperimentTypeWithProperties("ENZYME", "BOX TO HOLD SAMPLES OF THIS TYPE FOR ORGANIZATIONAL PURPOSES", []);
createExperimentTypeWithProperties("MEDIA", "BOX TO HOLD SAMPLES OF THIS TYPE FOR ORGANIZATIONAL PURPOSES", []);
createExperimentTypeWithProperties("OLIGO", "BOX TO HOLD SAMPLES OF THIS TYPE FOR ORGANIZATIONAL PURPOSES", []);
createExperimentTypeWithProperties("RNA", "BOX TO HOLD SAMPLES OF THIS TYPE FOR ORGANIZATIONAL PURPOSES", []);
createExperimentTypeWithProperties("PLASMID", "BOX TO HOLD SAMPLES OF THIS TYPE FOR ORGANIZATIONAL PURPOSES", []);
createExperimentTypeWithProperties("SOLUTION_BUFFER", "BOX TO HOLD SAMPLES OF THIS TYPE FOR ORGANIZATIONAL PURPOSES", []);
createExperimentTypeWithProperties("YEAST", "BOX TO HOLD SAMPLES OF THIS TYPE FOR ORGANIZATIONAL PURPOSES", []);
createExperimentTypeWithProperties("CELL_LINE", "BOX TO HOLD SAMPLES OF THIS TYPE FOR ORGANIZATIONAL PURPOSES", []);
createExperimentTypeWithProperties("FLY", "BOX TO HOLD SAMPLES OF THIS TYPE FOR ORGANIZATIONAL PURPOSES", []);

createExperimentTypeWithProperties("GENERAL_PROTOCOL", "BOX TO HOLD SAMPLES OF THIS TYPE FOR ORGANIZATIONAL PURPOSES", []);
createExperimentTypeWithProperties("PCR_PROTOCOL", "BOX TO HOLD SAMPLES OF THIS TYPE FOR ORGANIZATIONAL PURPOSES", []);
createExperimentTypeWithProperties("WESTERN_BLOTTING_PROTOCOL", "BOX TO HOLD SAMPLES OF THIS TYPE FOR ORGANIZATIONAL PURPOSES", []);

createExperimentTypeWithProperties("DEFAULT_EXPERIMENT", "Default Experiment", [
	["NAME", "General", "Name", DataType.VARCHAR, "Name"],
	["EXPERIMENTAL_GOALS", "General", "Experimental goals", DataType.MULTILINE_VARCHAR, "Goal of the experiment"],
	["GRANT", "General", "Grant", DataType.VARCHAR, "grant name"],
	["START_DATE", "General", "Start Date", DataType.TIMESTAMP, "Start Date"],
	["END_DATE", "General", "End Date", DataType.TIMESTAMP, "End Date"],
	["EXPERIMENTAL_RESULTS", "General", "Experimental results", DataType.MULTILINE_VARCHAR, "Brief summary of the results obtained"]
]);

##
## Dataset
##
ELN_PREVIEW = tr.getOrCreateNewDataSetType("ELN_PREVIEW")
ELN_PREVIEW.setDescription("ELN Preview");
ELN_PREVIEW.setDataSetKind("PHYSICAL");

SEQ_FILE = tr.getOrCreateNewDataSetType("SEQ_FILE")
SEQ_FILE.setDescription("");
SEQ_FILE.setDataSetKind("PHYSICAL");

SEQ_FILE_NOTES = tr.assignPropertyType(SEQ_FILE, NOTES);
SEQ_FILE_NOTES.setSection("General information");

RAW_DATA = tr.getOrCreateNewDataSetType("RAW_DATA")
RAW_DATA.setDescription("");
RAW_DATA.setDataSetKind("PHYSICAL");

RAW_DATA_NOTES = tr.assignPropertyType(RAW_DATA, NOTES);
RAW_DATA_NOTES.setSection("General information");