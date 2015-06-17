#!/usr/bin/env python
# -*- coding: utf-8 -*-

import ch.systemsx.cisd.openbis.generic.server.jython.api.v1.DataType as DataType

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
adenosineScriptName = "ADENOSINE_COUNT"
cytosineScriptName= "CYTOSINE_COUNT"
gcScriptName= "GC"
atScriptName= "AT"
guanosineScriptName= "GUANOSINE_COUNT"
lengthScriptName= "SEQUENCE_LENGTH"
nucelotideScriptName= "ONUCLEOTIDE_COUNT"
thymidineScriptName= "THYMIDINE_COUNT"
tmScriptName= "TM"

#
# Storage 
#
numberOfStorageGroups = 1

def getStorageGroupPropertyCodes():
    propertyCodes = [];
    for property in getStorageGroupDefinition():
        propertyCodes.append(property[0]);
    return propertyCodes;

def getStorageGroupDefinition():
    return [
    ["STORAGE_NAME",         "Physical Storage",        "location",         DataType.CONTROLLEDVOCABULARY,      "FREEZER",            "Storage Name",       None, None, False],
    ["STORAGE_ROW",          "Physical Storage",        "Storage Row",      DataType.INTEGER,                    None,                "Storage Row",        None, None, False],
    ["STORAGE_COLUMN",       "Physical Storage",        "Storage Column",   DataType.INTEGER,                    None,                "Storage Column",     None, None, False],
    ["STORAGE_BOX_NAME",     "Physical Storage",        "box label",        DataType.VARCHAR,                    None,                "Storage Box Name",   None, None, False],
    ["STORAGE_USER",         "Physical Storage",        "frozen by",        DataType.CONTROLLEDVOCABULARY,      "LAB_MEMBERS",    "Storage User Id",    None, None, False],
    ["STORAGE_BOX_POSITION", "Physical Storage",        "position",         DataType.CONTROLLEDVOCABULARY,       None,                "Storage Box Position",    None, None, False]
];

#
# Experiment Types
#
experimentDefinition = [
    ["NAME",                   "General", "Name",                     DataType.VARCHAR,             None,    "Name", None, None, False],
    ["EXPERIMENTAL_GOALS",     "General", "Experimental goals",       DataType.MULTILINE_VARCHAR,   None,    "Goal of the experiment", None, None, False],
    ["GRANT",                  "General", "Grant",                    DataType.VARCHAR,             None,    "grant name", None, None, False],
    ["START_DATE",             "General", "Start Date",               DataType.TIMESTAMP,           None,    "Start Date", None, None, False],
    ["END_DATE",               "General", "End Date",                 DataType.TIMESTAMP,           None,    "End Date", None, None, False],
    ["EXPERIMENTAL_RESULTS",   "General", "Experimental results",     DataType.MULTILINE_VARCHAR,   None,    "Brief summary of the results obtained", None, None, False],
    ["XMLCOMMENTS",            "Comments","Comments List",            DataType.XML,                 None,    "Several comments can be added by different users", commentsScriptName, None, False]
];

#
# Sample Types
#
# antibodyDefinition = [
#     ["NAME",                           "General",                "Name",                        DataType.VARCHAR,                    None,  "", None, None, False],
#     ["ANITIGEN",                           "General",                "Antigen",                        DataType.VARCHAR,                    None,  "", None, None, False],
#     ["MONOPOLY",                           "General",                "MonoPoly",                        DataType.VARCHAR,                    None,  "", None, None, False],    
#     ["SPECIES",                           "General",                "Species",                        DataType.VARCHAR,                    None,  "", None, None, False],    
#     ["VENDOR",                           "General",                "Vendor",                        DataType.VARCHAR,                    None,  "", None, None, False],    
#     ["WBDIL",                           "General",                "WBDil",                        DataType.VARCHAR,                    None,  "", None, None, False],    
#     ["IFDIL",                           "General",                "IFDil",                        DataType.VARCHAR,                    None,  "", None, None, False],    
#     ["FORM",                           "General",                "Form",                        DataType.VARCHAR,                    None,  "", None, None, False],    
#     ["PRIMARY",                           "General",                "Primary",                        DataType.VARCHAR,                    None,  "", None, None, False],    
#     ["SECONDAR",                           "General",                "Secondary",                        DataType.VARCHAR,                    None,  "", None, None, False],    
#     ["COUPLED",                           "General",                "Coupled",                        DataType.VARCHAR,                    None,  "", None, None, False],    
#     ["BY",                           "General",                "By",                        DataType.VARCHAR,                    None,  "", None, None, False],
#     ["FEATURES",                           "General",                "Features",                        DataType.VARCHAR,                    None,  "", None, None, False],
#     ["FROMNUC",                           "General",                "FromNuc",                        DataType.INTEGER,                    None,  "", None, None, False],  
#     ["GROUP",                           "General",                "Group",                        DataType.VARCHAR,                    None,  "", None, None, False],
#     ["LABELS",                           "General",                "Labels",                        DataType.VARCHAR,                    None,  "", None, None, False],
#     ["LIST",                           "General",                "List",                        DataType.VARCHAR,                    None,  "", None, None, False],
#     ["OD",                           "General",                "OD",                        DataType.REAL,                    None,  "", None, None, False],
#     ["RECEIVED_DATE",                           "General",                "Date received",                        DataType.TIMESTAMP,                    None,  "", None, None, False],
#     ["REQUEST_DATE",                           "General",                "Date_requested",                        DataType.TIMESTAMP,                    None,  "", None, None, False],
#     ["SENSE",                           "General",                "Sense",                        DataType.VARCHAR,                    None,  "", None, None, False],
#     ["SEQUENCE",                           "General",                "Sequence",                        DataType.VARCHAR,                    None,  "", None, None, False],
#     ["TEXTSEQUENCE",                           "General",                "TextSequence",                        DataType.VARCHAR,                    None,  "", None, None, False],
#     ["TYPE",                           "General",                "TYPE",                        DataType.VARCHAR,                    None,  "", None, None, False],
#     ["TEXTSEQUENCE",                           "General",                "TextSequence",                        DataType.VARCHAR,                    None,  "", None, None, False],







# ];


# strainDefinition = [
#     ["CO2",                             "General",                  "%CO2",                             DataType.CONTROLLEDVOCABULARY,       "CO2",  "", None, None, False],
#     ["BACKGROUND",                      "General",                  "background",                       DataType.CONTROLLEDVOCABULARY,       "STRAIN_BACKGROUND",  "", None, None, False],
#     ["BARCODE",                         "General",                  "barcode",                          DataType.VARCHAR,                    None,  "", None, None, False],
#     ["BARCODE_LABEL",                   "General",                  "barcode label",                    DataType.VARCHAR,                    None,  "", None, None, False],
#     ["CIR",                             "General",                  "cir",                              DataType.CONTROLLEDVOCABULARY,       "CIR",  "", None, None, False],
#     ["CONC_INDUCER1",                   "General",                  "conc. inducer 1",                  DataType.VARCHAR,                    None,  "", None, None, False],
#     ["CONC_INDUCER2",                   "General",                  "conc. inducer 2",                  DataType.VARCHAR,                    None,  "", None, None, False],
#     ["CONC_INDUCER3",                   "General",                  "conc. inducer 3",                  DataType.VARCHAR,                    None,  "", None, None, False],
#     ["CONC1",                           "General",                  "conc.1",                           DataType.REAL,                       None,  "", None, None, False],
#     ["CONC_UNIT1",                      "General",                  "concentration unit 1",             DataType.VARCHAR,                    None,  "", None, None, False],
#     ["CONC2",                           "General",                  "conc.2",                           DataType.REAL,                       None,  "", None, None, False],
#     ["CONC_UNIT2",                      "General",                  "concentration unit 2",             DataType.CONTROLLEDVOCABULARY,       "CONC_UNITS",  "", None, None, False],
#     ["CONC3",                           "General",                  "conc.3",                           DataType.REAL,                       None,  "", None, None, False],
#     ["CONC_UNIT3",                      "General",                  "concentration unit 3",             DataType.CONTROLLEDVOCABULARY,       "CONC_UNITS",  "", None, None, False],
#     ["CREATION_DATE",                   "General",                  "creation date",                    DataType.TIMESTAMP,                  None,  "", None, None, False],
#     ["DATE",                            "General",                  "date",                             DataType.VARCHAR,                    None,  "", None, None, False],
#     ["DERIVATION",                      "General",                  "derivation",                       DataType.CONTROLLEDVOCABULARY,       "DERIVATION",  "", None, None, False],
#     ["EXPRESSED_PROTEIN1",              "General",                  "expressed protein 1",              DataType.VARCHAR,                    None,  "", None, None, False],
#     ["EXPRESSED_PROTEIN2",              "General",                  "expressed protein 2",              DataType.VARCHAR,                    None,  "", None, None, False],
#     ["EXPRESSED_PROTEIN3",              "General",                  "expressed protein 3",              DataType.VARCHAR,                    None,  "", None, None, False],
#     ["FREE_PLASMID",                    "General",                  "freeplasmid",                      DataType.VARCHAR,                    None,  "", None, None, False],
#     ["FREEZEDATE",                      "General",                  "freezedate",                       DataType.VARCHAR,                    None,  "", None, None, False],
#     ["FROZEN",                          "General",                  "frozen",                           DataType.TIMESTAMP,                  None,  "", None, None, False],
#     ["GAL",                             "General",                  "gal",                              DataType.CONTROLLEDVOCABULARY,       "GAL",  "", None, None, False],
#     ["GENE_EXPRESSION1",                "General",                  "gene expression 1",                DataType.VARCHAR,                    None,  "", None, None, False],
#     ["GENE_EXPRESSION2",                "General",                  "gene expression 2",                DataType.VARCHAR,                    None,  "", None, None, False],
#     ["GROWTH_TEMPERATURE",              "General",                  "growth temperature",               DataType.CONTROLLEDVOCABULARY,       "GROWTH_TEMPERATURE",  "", None, None, False],
#     ["INDUCER1",                        "General",                  "inducer 1",                        DataType.VARCHAR,                    None,  "", None, None, False],
#     ["INDUCER2",                        "General",                  "inducer 2",                        DataType.VARCHAR,                    None,  "", None, None, False],
#     ["INDUCER3",                        "General",                  "inducer 3",                        DataType.VARCHAR,                    None,  "", None, None, False],
#     ["COMMENTS",                        "General",                  "info",                             DataType.VARCHAR,                    None,  "", None, None, False],
#     ["INVESTIGATOR",                    "General",                  "investigator",                     DataType.CONTROLLEDVOCABULARY,       "ALL_LAB_MEMBERS",  "", None, None, False],
#     ["MARKERS",                         "General",                  "markers",                          DataType.VARCHAR,                    None,  "", None, None, False],
#     ["MAT",                             "General",                  "mat",                              DataType.CONTROLLEDVOCABULARY,       "MAT",  "", None, None, False],
#     ["MEDIUM",                          "General",                  "medium",                           DataType.CONTROLLEDVOCABULARY,       "MEDIUM",  "", None, None, False],
#     ["MEDIUM_SPECIALS",                 "General",                  "medium specials",                  DataType.VARCHAR,                    None,  "", None, None, False],
#     ["MODIFICATION_DATE",               "General",                  "modification date",                DataType.TIMESTAMP,                  None,  "", None, None, False],
#     ["MODIFIED_BY",                     "General",                  "modified by",                      DataType.CONTROLLEDVOCABULARY,       "ALL_LAB_MEMBERS",  "", None, None, False],
#     ["NUMBER",                          "General",                  "number",                           DataType.VARCHAR,                    None,  "", None, None, False],
#     ["STRAIN_OBTAINED_FROM",            "General",                  "obtained from",                    DataType.VARCHAR,                    None,  "", None, None, False],
#     ["ORF",                             "General",                  "orf",                              DataType.VARCHAR,                    None,  "", None, None, False],    
#     ["PAGE_NUMBER",                     "General",                  "page number",                      DataType.VARCHAR,                    None,  "", None, None, False],
#     ["PARENTAL_CELL",                   "General",                  "parental cell",                    DataType.VARCHAR,                    None,  "", None, None, False],
#     ["PASSAGE",                         "General",                  "passage",                          DataType.VARCHAR,                    None,  "", None, None, False],
#     ["PERSYEASTNUMBER",                 "General",                  "persyeastnumber",                  DataType.INTEGER,                    None,  "", None, None, False],
#     ["PETERYEASTNUMBER",                "General",                  "peteryeastnumber",                 DataType.INTEGER,                    None,  "", None, None, False],
#     ["PLASMID_ID1",                     "General",                  "plasmid ID 1",                     DataType.VARCHAR,                    None,  "", None, None, False],
#     ["PLASMID_ID2",                     "General",                  "plasmid ID 2",                     DataType.VARCHAR,                    None,  "", None, None, False],
#     ["PLASMID_ID3",                     "General",                  "plasmid ID 3",                     DataType.VARCHAR,                    None,  "", None, None, False],
#     ["PROMOTER1",                       "General",                  "promoter 1",                       DataType.VARCHAR,                    None,  "", None, None, False],
#     ["PROMOTER2",                       "General",                  "promoter 2",                       DataType.VARCHAR,                    None,  "", None, None, False],
#     ["PROMOTER3",                       "General",                  "promoter 3",                       DataType.VARCHAR,                    None,  "", None, None, False],
#     ["PUBLISHED_IN",                    "General",                  "reference",                        DataType.VARCHAR,                    None,  "", None, None, False],
#     ["SELECTION1",                      "General",                  "sel.1",                            DataType.VARCHAR,                    None,  "", None, None, False],
#     ["SELECTION2",                      "General",                  "sel.2",                            DataType.VARCHAR,                    None,  "", None, None, False],
#     ["SELECTION3",                      "General",                  "sel.3",                            DataType.VARCHAR,                    None,  "", None, None, False],
#     ["SELECTION",                       "General",                  "selection",                        DataType.CONTROLLEDVOCABULARY,       "SELECTION_YEAST",  "", None, None, False],
#     ["SERIAL_NUMBER",                   "General",                  "serial number",                    DataType.INTEGER,                    None,  "", None, None, False],
#     ["STRAIN_SOURCE",                   "General",                  "source",                           DataType.CONTROLLEDVOCABULARY,       "STRAIN_SOURCE",  "", None, None, False],
#     ["SPECIES",                         "General",                  "species",                          DataType.VARCHAR,                    None,  "", None, None, False],
#     ["STRAIN_ID_NR",                    "General",                  "strain_id_nr",                     DataType.VARCHAR,                    None,  "", None, None, False],
#     ["NAME",                            "General",                  "strain ID",                        DataType.VARCHAR,                    None,  "", None, None, False],
#     ["STRAIN_NAME",                     "General",                  "strain name",                      DataType.VARCHAR,                    None,  "", None, None, False]
# ];

oligoDefinition = [
    ["AT",                          "General",                  "AT%",                           DataType.INTEGER,                      None, "", atScriptName, None, False],
    ["BY",                          "General",                  "By%",                           DataType.VARCHAR,                      None, "", None, None, False],
    ["FROM_NUC",                          "General",                  "fromNuc%",                           DataType.INTEGER,                      None, "", None, None, False],
    ["GC",                          "General",                  "GC%",                           DataType.INTEGER,                      None, "", gcScriptName, None, False],
    ["GROUP",                          "General",                  "Group",                           DataType.VARCHAR,                      None, "", None, None, False],
    ["LABELS",                          "General",                  "Labels",                           DataType.VARCHAR,                      None, "", None, None, False],
    ["LENGTH",                          "General",                  "Length",                           DataType.INTEGER,                      None, "", lengthScriptName, None, False],
    ["LIST",                          "General",                  "List",                           DataType.VARCHAR,                      None, "", None, None, False],
    ["LOCATION",                          "General",                  "Location",                           DataType.VARCHAR,                      None, "", None, None, False],
    ["MMEXTCOEFF",                          "General",                  "mMExtCoeff",                           DataType.REAL,                      None, "", None, None, False],
    ["MOLWEIGHT",                          "General",                  "MolWeight",                           DataType.REAL,                      None, "", None, None, False],
    ["OD",                          "General",                  "OD",                           DataType.REAL,                      None, "", None, None, False],
    ["PICTURES",                          "General",                  "Pictures",                           DataType.VARCHAR,                      None, "", None, None, False],
    ["PMOL_UL",                          "General",                  "pMol_ul",                           DataType.REAL,                      None, "", None, None, False],
    ["RECEIVE_DATE",                          "General",                  "ReceiveDate",                           DataType.TIMESTAMP,                      None, "", None, None, False],
    ["REF_NUM",                          "General",                  "RefNum",                           DataType.VARCHAR,                      None, "", None, None, False],
    ["REQUEST_DATE",                          "General",                  "RequestDate",                           DataType.TIMESTAMP,                      None, "", None, None, False],
    ["SENSE",                          "General",                  "Sense",                           DataType.VARCHAR,                      None, "", None, None, False],
    ["SEQUENCE",                          "General",                  "Sequence",                           DataType.VARCHAR,                      None, "", None, None, False],
    ["STANDARD_TM",                          "General",                  "StandardTm",                           DataType.REAL,                      None, "", None, None, False],
    ["SYS_NAME",                          "General",                  "SysName",                           DataType.INTEGER,                      None, "", None, None, False],
    ["TEXT_SEQUENCE",                          "General",                  "TextSequence",                           DataType.VARCHAR,                      None, "", None, None, False],
    ["TONUC",                          "General",                  "toNuc",                           DataType.INTEGER,                      None, "", None, None, False],
    ["TYPE",                          "General",                  "Type",                           DataType.CONTROLLEDVOCABULARY,                      "OLIGO_TYPE", "", None, None, False],
    ["UC_NUMBER",                          "General",                  "UC Number",                           DataType.TEXT,                      None, "", None, None, False],
    ["UG_UL",                          "General",                  "ug_ul",                           DataType.REAL,                      None, "", None, None, False],
    ["VOLUME",                          "General",                  "Volume",                           DataType.REAL,                      None, "", None, None, False],
    ["STATUS_RECORDS",                          "General",                  "StatusRecords",                           DataType.INTEGER,                      None, "", None, None, False],
    ["CLICK_COUNTER",                          "General",                  "click counter",                           DataType.INTEGER,                      None, "", None, None, False],    
    ["CATEGORY",                          "General",                  "category",                           DataType.CONTROLLEDVOCABULARY,                      "OLIGO_CATEGORY", "", None, None, False],
    ["REORDERED",                          "General",                  "Reordered",                           DataType.VARCHAR,                      None, "", None, None, False],
    ["NOTES",                          "Comments",                  "Notes",                           DataType.VARCHAR,                      None, "", None, None, False]
];

plasmidDefinition = [
    ["3END",                        "General",                  "3' end",                         DataType.VARCHAR,      None,  "", None, None, False],
    ["3END_INSERT",                        "General",                  "3' end insert",                         DataType.VARCHAR,      None,  "", None, None, False],
    ["3INSERT_MOD",                        "General",                  "3' insert modif.",                         DataType.VARCHAR,      None,  "", None, None, False],
    ["3MOD",                        "General",                  "3' Modif.",                         DataType.VARCHAR,      None,  "", None, None, False],
    ["5END",                        "General",                  "5' end",                         DataType.VARCHAR,      None,  "", None, None, False],
    ["5END_MOD",                        "General",                  "5' end insert",                         DataType.VARCHAR,      None,  "", None, None, False],
    ["5INSERT_MOD",                        "General",                  "5' insert modif.",                         DataType.VARCHAR,      None,  "", None, None, False],
    ["3MOD",                        "General",                  "5' Modif.",                         DataType.VARCHAR,      None,  "", None, None, False],
    ["BACTERIAL_STRAIN",                        "General",                  "Bacterial strain",                         DataType.VARCHAR,      None,  "", None, None, False],
    ["CONCENTRATION",                        "General",                  "Concentration",                         DataType.REAL,      None,  "", None, None, False],    
    ["DNA_PREPARED_ON",                        "General",                  "DNA prepared on",                         DataType.VARCHAR,      None,  "", None, None, False],
    ["DRUG_RES",                        "General",                  "Drug res.",                         DataType.VARCHAR,      None,  "", None, None, False],    
    ["GENE",                        "General",                  "Gene",                         DataType.VARCHAR,      None,  "", None, None, False],    
    ["INSERT",                        "General",                  "Insert",                         DataType.VARCHAR,      None,  "", None, None, False],    
    ["INSERT_SIZE",                        "General",                  "Insert size",                         DataType.REAL,      None,  "", None, None, False],    
    ["MUTAGENIC_OLIGO",                        "General",                  "Mutagenic oligo",                         DataType.VARCHAR,      None,  "", None, None, False],    
    ["MUTATION",                        "General",                  "Mutation",                         DataType.VARCHAR,      None,  "", None, None, False],    
    ["NOTEBOOK_REF",                        "General",                  "Note Book ref.",                         DataType.VARCHAR,      None,  "", None, None, False],    
    ["PKW_NUMBER",                        "General",                  "pKW Number",                         DataType.INTEGER,      None,  "", None, None, False],    
    ["NAME",                        "General",                  "Plasmid name",                         DataType.VARCHAR,      None,  "", None, None, False],    
    ["REF_NUM",                        "General",                  "Ref. N.",                         DataType.INTEGER,      None,  "", None, None, False],    
    ["REFERENCE",                        "General",                  "Reference",                         DataType.VARCHAR,      None,  "", None, None, False],    
    ["SELECTION_MARKER",                        "General",                  "Selection marker",                         DataType.VARCHAR,      None,  "", None, None, False],    
    ["SIZE",                        "General",                  "Size",                         DataType.REAL,      None,  "", None, None, False],    
    ["SIZE_PARENT",                        "General",                  "Size parent",                         DataType.REAL,      None,  "", None, None, False],    
    ["VECTOR",                        "General",                  "Vector",                         DataType.VARCHAR,      None,  "", None, None, False],    
    ["VECTOR_TYPE",                        "General",                  "Vector Type",                         DataType.VARCHAR,      None,  "", None, None, False]    
   ];

chemicalDefinition = [
    ["NAME",                         "General",                 "Chemical",                         DataType.VARCHAR,                   None,  "", None, None, False],
    ["ID",                           "General",                 "#",                         DataType.VARCHAR,                   None,  "", None, None, False],    
    ["COMPANY",                      "General",                 "Company",                          DataType.CONTROLLEDVOCABULARY,      "COMPANY",  "", None, None, False],
    ["DISSOLVED_IN",                 "General",                 "dissolved in",                     DataType.VARCHAR,                   None,  "", None, None, False],
    ["USED_FOR",                     "General",                 "Used for",                         DataType.VARCHAR,                   None,  "", None, None, False],
    ["EMPTY",                        "General",                 "empty?",                           DataType.BOOLEAN,                   None,  "", None, None, False],
    ["DATE_ORDERED",                 "General",                 "date ordered",                     DataType.TIMESTAMP,                 None,  "", None, None, False],
    ["LOT_NUM",                      "General",                 "Lot #",                            DataType.VARCHAR,                   None,  "", None, None, False],
    ["CATALOG_NUM",                  "General",                 "Catalog #",                        DataType.VARCHAR,                   None,  "", None, None, False],
    ["STOCK_CONCENTRATION",          "General",                 "Stock concentration",              DataType.VARCHAR,                   None,  "", None, None, False],
    ["USED_BY",                      "General",                 "used by",                          DataType.VARCHAR,                   None,  "", None, None, False],
    ["LOCATION",                     "Storage",                 "Location",                         DataType.VARCHAR,                   None,  "", None, None, False],       
    ["COMMENTS",                     "Comments",                "Comments",                         DataType.VARCHAR,                   None,  "", None, None, False],
];

RestrictionEnzymeDefinition = [
    ["NAME",                            "General",                  "Name",                             DataType.VARCHAR,                   None,  "", None, None, False],
    ["BLOCK",                           "Storage",                  "Block",                            DataType.VARCHAR,                   None,  "", None, None, False],
    ["POSITION",                        "Storage",                  "Position",                         DataType.VARCHAR,                   None,  "", None, None, False],
    ["COMMENT",                         "Comments",                  "Comment",                          DataType.VARCHAR,                   None,  "", None, None, False]
  
];


