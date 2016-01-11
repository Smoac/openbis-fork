#!/usr/bin/env python
# -*- coding: utf-8 -*-

import ch.systemsx.cisd.openbis.generic.server.jython.api.v1.DataType as DataType

##
## Documentation
##
## Properties starting by "+" will be created in openBIS, but will not be read from FileMaker but if set programmatically will be put back in openBIS.
## Properties starting by "-" will not be created in openBIS but will be read from FileMaker.

#
# Helper Methods
#
def getPropertyDefinitionByCode(definition, code):
    for property in definition:
        if property[0] == code:
            return property
    return None

#
# Scripts
#
commentsScriptName = "COMMENTS";
gcScriptName= "GC"
atScriptName= "AT"
lengthScriptName= "SEQUENCE_LENGTH"
annotationsScriptName="ANNOTATIONS"
antibodyrefnumScriptName="ANTIBODY_REFNUM"
antibodynameScriptName="ANTIBODY_NAME"
chemicalrefnumScriptName="CHEMICAL_REFNUM"
oligonameScriptName="OLIGO_NAME"
oligorefnumScriptName="OLIGO_REFNUM"
oligosysnameScriptName = "SYS_NAME"
plasmidnameScriptName="PLASMID_NAME"
plasmidrefnumScriptName="PLASMID_REFNUM"
strainnameScriptName="STRAIN_NAME"
strainglycerolScriptName="STRAIN_GLYCEROL_STOCK_NUM"


#
# Experiment Types
#
experimentDefinition = [
    ["NAME",                   "General", "Name",                     DataType.MULTILINE_VARCHAR,             None,    "Name", None, None, False],
    ["EXPERIMENTAL_GOALS",     "General", "Experimental goals",       DataType.MULTILINE_VARCHAR,   None,    "Goal of the experiment", None, None, False],
    ["GRANT",                  "General", "Grant",                    DataType.MULTILINE_VARCHAR,             None,    "grant name", None, None, False],
    ["START_DATE",             "General", "Start Date",               DataType.TIMESTAMP,           None,    "Start Date", None, None, False],
    ["END_DATE",               "General", "End Date",                 DataType.TIMESTAMP,           None,    "End Date", None, None, False],
    ["EXPERIMENTAL_RESULTS",   "General", "Experimental results",     DataType.MULTILINE_VARCHAR,   None,    "Brief summary of the results obtained", None, None, False],
    ["XMLCOMMENTS",            "Comments","Comments List",            DataType.XML,                 None,    "Several comments can be added by different users", commentsScriptName, None, False]
];

#
#Sample Types
#
antibodyDefinition = [
    ["+AB_REF_NUMBER",                          "General",                  "RefNumber",                           DataType.VARCHAR,                      None, "", None, antibodyrefnumScriptName, False],
    ["-REF_NUM",                          "General",                  "RefNum",                           DataType.VARCHAR,                      None, "", None, None, False],
    ["NAME",                          "General",                  "Name",                           DataType.MULTILINE_VARCHAR,                      None, "", None, None, False],       
    ["+AB_NAME",                          "General",                  "Antibody Name",                           DataType.VARCHAR,                      None, "", None, antibodynameScriptName, False],       
    ["ANTIGEN",                          "General",                  "Antigen",                           DataType.VARCHAR,                      None, "", None, None, False],   
    ["MONO_POLY",                          "General",                  "MonoPoly",                           DataType.CONTROLLEDVOCABULARY,                      "AB_CLONALITY", "", None, None, False],       
    ["SPECIES",                          "General",                  "Species",                           DataType.CONTROLLEDVOCABULARY,                      "AB_SPECIES", "", None, None, False],       
    ["AB_TYPE",                          "General",                  "Type",                           DataType.CONTROLLEDVOCABULARY,                      "AB_TYPE", "", None, None, False],
    ["FORM",                          "General",                  "Form",                           DataType.CONTROLLEDVOCABULARY,                      "AB_FORM", "", None, None, False],       
    ["PRIMARY",                          "General",                  "Primary",                           DataType.VARCHAR,                     None, "", None, None, False],       
    ["SECONDARY",                          "General",                  "Secondary",                           DataType.VARCHAR,                      None, "", None, None, False],       
    ["COUPLED",                          "General",                  "Coupled",                           DataType.VARCHAR,                      None, "", None, None, False],       
    ["VENDOR",                          "General",                  "Vendor",                           DataType.VARCHAR,                      None, "", None, None, False],       
    ["AB_CATALOG_NUM",                  "General",                 "Catalog#",                        DataType.VARCHAR,                   None,  "", None, None, False],    
    ["BY",                           "General",                "By",                        DataType.CONTROLLEDVOCABULARY,                    "LAB_MEMBERS",  "", None, None, False],
    ["REQUEST_DATE",                          "General",                  "RequestDate",                           DataType.TIMESTAMP,                      None, "", None, None, False],
    ["RECEIVE_DATE",                          "General",                  "ReceiveDate",                           DataType.TIMESTAMP,                      None, "", None, None, False],
    ["+ANNOTATIONS_STATE",                "Comments",                "Annotations State",                    DataType.XML,                    None, "Annotations State", annotationsScriptName, None, False],        
    ["NOTES",                           "Comments",                "Notes",                        DataType.MULTILINE_VARCHAR,                    None,  "", None, None, False],
    ["WBDIL",                          "Comments",                  "WBDil",                           DataType.VARCHAR,                      None, "", None, None, False],       
    ["IFDIL",                          "Comments",                  "IFDil",                           DataType.VARCHAR,                      None, "", None, None, False],          
    ["STORAGE_LOCATION",                          "Storage",                  "StorageLocation",                           DataType.CONTROLLEDVOCABULARY,                      "AB_LOCATION", "", None, None, False],           
    ["BOX",                          "Storage",                  "Box",                           DataType.VARCHAR,                      None, "", None, None, False],       
    ["AB_EMPTY",                          "Storage",                  "emtpy?",                           DataType.BOOLEAN,                      None, "", None, None, False],       
    
];


strainDefinition = [
    ["-NAME",                             "General",                  "KWY number",                             DataType.MULTILINE_VARCHAR,       None,  "", None, None, False],      
    ["+NAME_STRAIN",                             "General",                  "Strain name",                             DataType.VARCHAR,       None,  "", None, strainnameScriptName, False],          
    ["MAT",                             "General",                  "MAT",                             DataType.CONTROLLEDVOCABULARY,       "MAT",  "", None, None, False],    
    ["+GENOTYPE",                             "General",                  "Genotype",                             DataType.MULTILINE_VARCHAR,       None,  "", None, None, False],    
    ["BASE_STRAIN",                             "General",                  "base strain",                             DataType.MULTILINE_VARCHAR,       None,  "", None, None, False],
    ["+YEAST_BACKGROUND",                             "General",                  "yeast background",                             DataType.CONTROLLEDVOCABULARY,       "YEAST_BACKGROUND",  "", None, None, False],
    ["DERIVED_FROM",                             "General",                  "derived from",                             DataType.VARCHAR,       None,  "", None, None, False],       
    ["COMMENTS",                             "General",                  "comments",                             DataType.MULTILINE_VARCHAR,       None,  "", None, None, False],
    ["GENOTYPE_CONFIRMATION",                             "General",                  "genotype confirmation",                             DataType.MULTILINE_VARCHAR,       None,  "", None, None, False],    
    ["WHO_ENTERED",                             "General",                  "who entered",                             DataType.CONTROLLEDVOCABULARY,       "LAB_MEMBERS",  "", None, None, False],    
    ["REMOVED",                             "General",                  "removed",                             DataType.BOOLEAN,       None,  "", None, None, False],    
    ["+GLYCEROL_STOCK_NUMBER",                             "General",                  "Glycerol stock number",                             DataType.VARCHAR,       None,  "", None, strainglycerolScriptName, False],    
    ["DATE_ENTERED",                             "General",                  "date entered",                             DataType.TIMESTAMP,       None,  "", None, None, False],    
    ["+ANNOTATIONS_STATE",                "Comments",                "Annotations State",                    DataType.XML,                    None,                                "Annotations State", annotationsScriptName, None, False]
];

strainDefinitionRepetitions = [
    ["-DISRUPTIONS_1",         "Genotype",        "disruptions[1]",         DataType.VARCHAR,                     None, "",       None, None, False],
    ["-DISRUPTIONS_2",         "Genotype",        "disruptions[2]",         DataType.VARCHAR,                     None, "",       None, None, False],
    ["-DISRUPTIONS_3",         "Genotype",        "disruptions[3]",         DataType.VARCHAR,                     None, "",       None, None, False],
    ["-DISRUPTIONS_4",         "Genotype",        "disruptions[4]",         DataType.VARCHAR,                     None, "",       None, None, False],
    ["-DISRUPTIONS_5",         "Genotype",        "disruptions[5]",         DataType.VARCHAR,                     None, "",       None, None, False],
    ["-MARKERS_1",          "Genotype",        "markers[1]",      DataType.VARCHAR,                    None, "",        None, None, False],
    ["-MARKERS_2",          "Genotype",        "markers[2]",      DataType.VARCHAR,                    None, "",        None, None, False],
    ["-MARKERS_3",          "Genotype",        "markers[3]",      DataType.VARCHAR,                    None, "",        None, None, False],
    ["-MARKERS_4",          "Genotype",        "markers[4]",      DataType.VARCHAR,                    None, "",        None, None, False],
    ["-MARKERS_5",          "Genotype",        "markers[5]",      DataType.VARCHAR,                    None, "",        None, None, False],
    ["-UNMARKED_MUTATIONS_1",       "Genotype",        "\"unmarked mutations\"[1]",   DataType.VARCHAR,                    None, "",     None, None, False],
    ["-UNMARKED_MUTATIONS_2",       "Genotype",        "\"unmarked mutations\"[2]",   DataType.VARCHAR,                    None, "",     None, None, False],
    ["-UNMARKED_MUTATIONS_3",       "Genotype",        "\"unmarked mutations\"[3]",   DataType.VARCHAR,                    None, "",     None, None, False],
    ["-UNMARKED_MUTATIONS_4",       "Genotype",        "\"unmarked mutations\"[4]",   DataType.VARCHAR,                    None, "",     None, None, False],
    ["-UNMARKED_MUTATIONS_5",       "Genotype",        "\"unmarked mutations\"[5]",   DataType.VARCHAR,                    None, "",     None, None, False]
];

oligoDefinition = [
    ["-NAME",                          "Storage",                  "UC Number",                           DataType.MULTILINE_VARCHAR,                      None, "", None, None, False],
    ["+OLIGO_NAME",                          "General",                  "Oligo name",                           DataType.VARCHAR,                      None, "", None, oligonameScriptName, False],    
    ["+OLIGO_REF_NUM",                          "General",                  "RefNum",                           DataType.VARCHAR,                      None, "", None, oligorefnumScriptName, False],    
    ["SEQUENCE",                          "General",                  "Sequence",                           DataType.MULTILINE_VARCHAR,                      None, "", None, None, False],
    ["LENGTH",                          "General",                  "Length",                           DataType.INTEGER,                      None, "", None, lengthScriptName, False],
    ["GROUP",                          "General",                  "Group",                           DataType.VARCHAR,                      None, "", None, None, False],
    ["SENSE",                          "General",                  "Sense",                           DataType.CONTROLLEDVOCABULARY,                      "OLIGO_SENSE", "", None, None, False],
    ["CATEGORY",                          "General",                  "category",                           DataType.CONTROLLEDVOCABULARY,                      "OLIGO_CATEGORY", "", None, None, False],
    ["NOTES",                          "General",                  "Notes",                           DataType.MULTILINE_VARCHAR,                      None, "", None, None, False],
    ["REORDERED",                          "General",                  "Reordered",                           DataType.VARCHAR,                      None, "", None, None, False],
    ["LABELS",                          "General",                  "Labels",                           DataType.VARCHAR,                      None, "", None, None, False],
    ["BY",                          "General",                  "By",                           DataType.CONTROLLEDVOCABULARY,                      "LAB_MEMBERS", "", None, None, False],
    ["+ANNOTATIONS_STATE",                "Comments",                "Annotations State",                    DataType.XML,                    None,                                "Annotations State", annotationsScriptName, None, False],
    ["+XMLCOMMENTS",                     "Comments",                "Comments List",                        DataType.XML,                    None, "Several comments can be added by different users", commentsScriptName, None, False],
    ["+SYS_NAME",                          "Information",                  "SysName",                           DataType.VARCHAR,                      None, "", None, oligosysnameScriptName, False],
    ["OLIGO_TYPE",                          "Information",                  "Type",                           DataType.VARCHAR,                      None, "", None, None, False],
    ["STANDARD_TM",                          "Information",                  "StandardTm",                           DataType.REAL,                      None, "", None, None, False],
    ["AT",                          "Information",                  "AT%",                           DataType.INTEGER,                      None, "", None , atScriptName, False],
    ["GC",                          "Information",                  "GC%",                           DataType.INTEGER,                      None, "", None, gcScriptName, False],
    ["MMEXTCOEFF",                          "Information",                  "mMExtCoeff",                           DataType.REAL,                      None, "", None, None, False],
    ["MOLWEIGHT",                          "Information",                  "MolWeight",                           DataType.REAL,                      None, "", None, None, False],
    ["OD",                          "Information",                  "OD",                           DataType.REAL,                      None, "", None, None, False],
    ["REQUEST_DATE",                          "Information",                  "RequestDate",                           DataType.TIMESTAMP,                      None, "", None, None, False],
    ["RECEIVE_DATE",                          "Information",                  "ReceiveDate",                           DataType.TIMESTAMP,                      None, "", None, None, False],
    ["FROM_NUC",                          "Information",                  "fromNuc",                           DataType.INTEGER,                      None, "", None, None, False],
    ["TONUC",                          "Information",                  "toNuc",                           DataType.INTEGER,                      None, "", None, None, False],
    ["LIST",                          "Information",                  "List",                           DataType.VARCHAR,                      None, "", None, None, False],
    ["OLIGO_LOCATION",                          "Information",                  "Location",                           DataType.VARCHAR,                      None, "", None, None, False],
    ["PMOL_UL",                          "Information",                  "pMol_ul",                           DataType.REAL,                      None, "", None, None, False],
    ["UG_UL",                          "Information",                  "ug_ul",                           DataType.REAL,                      None, "", None, None, False],
    ["VOLUME",                          "Information",                  "Volume",                           DataType.REAL,                      None, "", None, None, False],
];

plasmidDefinition = [
    ["-NAME",                        "General",                  "pKW Number",                         DataType.MULTILINE_VARCHAR,      None,  "", None, None, False],    
    ["+PL_NAME",                        "General",                  "pKW Number Calculated",                         DataType.VARCHAR,      None,  "", None, plasmidnameScriptName, False],    
    ["PLASMID_NAME",                        "General",                  "Plasmid name",                         DataType.VARCHAR,      None,  "", None, None, False],    
    ["BACTERIAL_STRAIN",                        "General",                  "Bacterial strain",                         DataType.CONTROLLEDVOCABULARY,      "PLASMID_BACTERIAL_STRAIN",  "", None, None, False],
    ["BY",                        "General",                  "By",                         DataType.CONTROLLEDVOCABULARY,      "LAB_MEMBERS",  "", None, None, False],
    ["NOTEBOOK_REF",                        "General",                  "Note Book ref.",                         DataType.VARCHAR,      None,  "", None, None, False],    
    ["REFERENCE",                        "General",                  "Reference",                         DataType.VARCHAR,      None,  "", None, None, False],    
    ["VECTOR",                        "General",                  "Vector",                         DataType.VARCHAR,      None,  "", None, None, False],    
    ["GENE",                        "General",                  "Gene",                         DataType.VARCHAR,      None,  "", None, None, False],    
    ["SELECTION_MARKER",                        "General",                  "Selection marker",                         DataType.CONTROLLEDVOCABULARY,      "PLASMID_SELECTION_MAKER",  "", None, None, False],    
    ["DRUG_RES",                        "General",                  "Drug res.",                         DataType.CONTROLLEDVOCABULARY,      "PLASMID_DRUG_RES",  "", None, None, False],    
    ["SIZE",                        "General",                  "Size",                         DataType.REAL,      None,  "", None, None, False],    
    ["CONCENTRATION",                        "General",                  "Concentration",                         DataType.REAL,      None,  "", None, None, False],    
    ["DNA_PREPARED_ON",                        "General",                  "DNA prepared on",                         DataType.VARCHAR,      None,  "", None, None, False],
    ["VECTOR_TYPE",                        "General",                  "Vector Type",                         DataType.CONTROLLEDVOCABULARY,      "PLASMID_VECTOR_TYPE",  "", None, None, False],    
    ["+ANNOTATIONS_STATE",                "Comments",                "Annotations State",                    DataType.XML,                    None,                                "Annotations State", annotationsScriptName, None, False],
    ["COMMENT",                        "Comments",                  "Comment",                         DataType.MULTILINE_VARCHAR,      None,  "", None, None, False],
    ["PARENT_PLASMID",                        "Cloning Information",                  "Parent plasmid",                         DataType.VARCHAR,      None,  "", None, None, False],        
    ["SIZE_PARENT",                        "Cloning Information",                  "Size parent",                         DataType.REAL,      None,  "", None, None, False],    
    ["5END",                        "Cloning Information",                  "5' end",                         DataType.VARCHAR,      None,  "", None, None, False],
    ["3END",                        "Cloning Information",                  "3' end",                         DataType.VARCHAR,      None,  "", None, None, False],
    ["5MOD",                        "Cloning Information",                  "5' Modif.",                         DataType.VARCHAR,      None,  "", None, None, False],
    ["3MOD",                        "Cloning Information",                  "3' Modif.",                         DataType.VARCHAR,      None,  "", None, None, False],
    ["INSERT",                        "Cloning Information",                  "Insert",                         DataType.VARCHAR,      None,  "", None, None, False],    
    ["INSERT_SIZE",                        "Cloning Information",                  "Insert size",                         DataType.REAL,      None,  "", None, None, False],    
    ["5INSERT_MOD",                        "Cloning Information",                  "5' insert modif.",                         DataType.VARCHAR,      None,  "", None, None, False],
    ["3INSERT_MOD",                        "Cloning Information",                  "3' insert modif.",                         DataType.VARCHAR,      None,  "", None, None, False],
    ["5END_MOD",                        "Cloning Information",                  "5' end insert",                         DataType.VARCHAR,      None,  "", None, None, False],
    ["3END_INSERT",                        "Cloning Information",                  "3' end insert",                         DataType.VARCHAR,      None,  "", None, None, False],
    ["PCR_3_OLIGO",                        "Cloning Information",                  "PCR 3' oligo",                         DataType.VARCHAR,      None,  "", None, None, False],    
    ["PCR_5_OLIGO",                        "Cloning Information",                  "PCR 5' oligo",                         DataType.VARCHAR,      None,  "", None, None, False],            
    ["MUTAGENIC_OLIGO",                        "Cloning Information",                  "Mutagenic oligo",                         DataType.VARCHAR,      None,  "", None, None, False],    
    ["-REF_NUM",                        "Storage",                  "Ref. N.",                         DataType.INTEGER,      None,  "", None, None, False],    
    ["+PL_REF_NUM",                        "Storage",                  "Ref. N.",                         DataType.INTEGER,      None,  "", None, plasmidrefnumScriptName, False],    
    ["MUTATION",                        "General",                  "Mutation",                         DataType.VARCHAR,      None,  "", None, None, False],    
   ];

chemicalDefinition = [
    ["NAME",                         "General",                 "Chemical",                         DataType.MULTILINE_VARCHAR,                   None,  "", None, None, False],
    ["-ID",                           "General",                 "#",                         DataType.VARCHAR,                   None,  "", None, None, False],    
    ["+CHEM_ID",                           "General",                 "#",                         DataType.VARCHAR,                   None,  "", None, chemicalrefnumScriptName, False],    
    ["COMPANY",                      "General",                 "Company",                          DataType.VARCHAR,      None,  "", None, None, False],
    ["DISSOLVED_IN",                 "General",                 "dissolved in",                     DataType.VARCHAR,                   None,  "", None, None, False],
    ["USED_FOR",                     "General",                 "Used for",                         DataType.MULTILINE_VARCHAR,                   None,  "", None, None, False],
    ["CHEM_EMPTY",                        "General",                 "empty?",                           DataType.BOOLEAN,                   None,  "", None, None, False],
    ["DATE_ORDERED",                 "General",                 "date ordered",                     DataType.VARCHAR,                 None,  "", None, None, False],
    ["LOT_NUM",                      "General",                 "Lot #",                            DataType.VARCHAR,                   None,  "", None, None, False],
    ["CHEM_CATALOG_NUM",                  "General",                 "Catalog #",                        DataType.VARCHAR,                   None,  "", None, None, False],
    ["STOCK_CONCENTRATION",          "General",                 "Stock concentration",              DataType.VARCHAR,                   None,  "", None, None, False],
    ["USED_BY",                      "General",                 "used by",                          DataType.CONTROLLEDVOCABULARY,                   "LAB_MEMBERS",  "", None, None, False],
    ["CHEMICAL_LOCATION",                      "Storage",                 "Location",                          DataType.CONTROLLEDVOCABULARY,                   "CHEMICALS_LOCATION",  "", None, None, False],  
    ["COMMENTS",                     "Comments",                "Comments",                         DataType.MULTILINE_VARCHAR,                   None,  "", None, None, False],
    ["+ANNOTATIONS_STATE",                "Comments",                "Annotations State",                    DataType.XML,                    None, "Annotations State", annotationsScriptName, None, False]
];

RestrictionEnzymeDefinition = [
    ["NAME",                            "General",                  "Name",                             DataType.MULTILINE_VARCHAR,                   None,  "", None, None, False],
    ["COMMENT",                         "Comments",                  "Comment",                          DataType.MULTILINE_VARCHAR,                   None,  "", None, None, False],
    ["+ANNOTATIONS_STATE",                "Comments",                "Annotations State",                    DataType.XML,                    None, "Annotations State", annotationsScriptName, None, False],  
    ["BLOCK",                           "Storage",                  "Block",                            DataType.VARCHAR,                   None,  "", None, None, False],
    ["POSITION",                        "Storage",                  "Position",                         DataType.VARCHAR,                   None,  "", None, None, False]
];

ExperimentalStepDefinition= [
    ["NAME",                             "General",                "Name",                                    DataType.MULTILINE_VARCHAR,        None,"Name", None, None, False],
    ["OWNER",                             "General",                "Owner",                                DataType.CONTROLLEDVOCABULARY,    "LAB_MEMBERS", "Who produced/owned the sample", None, None, False],
    ["EXPERIMENTAL_GOALS",                 "General",                "Experimental Goals",                    DataType.MULTILINE_VARCHAR,        None,  "Goal of the experiment", None, None, False],
    ["EXPERIMENTAL_RESULTS",             "General",                "Experimental Results",                    DataType.MULTILINE_VARCHAR,        None,"Brief summary of the results obtained", None, None, False],
    ["START_DATE",             "General",                "Start date",                    DataType.TIMESTAMP,        None,"Date when the experimental step is started", None, None, False],
    ["END_DATE",             "General",                "End date",                    DataType.TIMESTAMP,        None, "Date when the experimental step is completed", None, None, False],
    ["EXPERIMENTAL_READOUT",             "Readout details",        "Experimental readout",                    DataType.CONTROLLEDVOCABULARY,    "EXPERIMENTAL_READOUT", "Experimental readout used in the experiment", None, None, False],
    ["MACHINE",                         "Readout details",        "Machine",                                DataType.CONTROLLEDVOCABULARY,    "MACHINE","Machine used to perform the experiment", None, None, False],
    ["FREEFORM_TABLE_STATE",             "Readout details",        "Freeform Table State",                    DataType.MULTILINE_VARCHAR,        None,  "Table describing how the order of samples measured in the experiments", None, None, False],
    ["PUBLICATION",                     "Comments",                "Publication",                            DataType.MULTILINE_VARCHAR,        None, "Publication from where the information was first found OR technical sheet given by the manufacturer", None, None, False],
    ["NOTES",                             "Comments",             "Notes",                                DataType.MULTILINE_VARCHAR,        None,"Notes", None, None, False],
    ["XMLCOMMENTS",                     "Comments",                "Comments List",                        DataType.XML,                    None, "Several comments can be added by different users", commentsScriptName, None, False],
    ["ANNOTATIONS_STATE",                "Comments",                "Annotations State",                    DataType.XML,                    None, "Annotations State", annotationsScriptName, None, False]
];

DefaultDatasetDefinition=[
        ["NAME", "General", "Name", DataType.MULTILINE_VARCHAR, None,    "Name", None, None,False],
        ["NOTES", "General information", "Notes", DataType.MULTILINE_VARCHAR, None, "Notes regarding the dataset", None, None,False],
        ["XMLCOMMENTS",    "Comments","Comments List",    DataType.XML,    None,    "Several comments can be added by different users", commentsScriptName, None,False]
    ]
