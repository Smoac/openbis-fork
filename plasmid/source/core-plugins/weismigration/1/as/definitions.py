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
gcScriptName= "GC"
atScriptName= "AT"
lengthScriptName= "SEQUENCE_LENGTH"
annotationsScriptName="ANNOTATIONS"

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
#Sample Types
#
antibodyDefinition = [
    ["BY",                           "General",                "By",                        DataType.VARCHAR,                    None,  "", None, None, False],
    ["NOTES",                           "General",                "Notes",                        DataType.VARCHAR,                    None,  "", None, None, False],
    ["RECEIVE_DATE",                          "General",                  "ReceiveDate",                           DataType.TIMESTAMP,                      None, "", None, None, False],
    ["REF_NUM",                          "General",                  "RefNum",                           DataType.VARCHAR,                      None, "", None, None, False],
    ["REQUEST_DATE",                          "General",                  "RequestDate",                           DataType.TIMESTAMP,                      None, "", None, None, False],
    ["TYPE",                          "General",                  "Type",                           DataType.VARCHAR,                      None, "", None, None, False],
    ["ANTIGEN",                          "General",                  "Antigen",                           DataType.VARCHAR,                      None, "", None, None, False],   
    ["NAME",                          "General",                  "Name",                           DataType.VARCHAR,                      None, "", None, None, False],       
    ["STORAGE_LOCATION",                          "General",                  "StorageLocation",                           DataType.VARCHAR,                      None, "", None, None, False],       
    ["MONO_POLY",                          "General",                  "MonoPoly",                           DataType.VARCHAR,                      None, "", None, None, False],       
    ["SPECIES",                          "General",                  "Species",                           DataType.VARCHAR,                      None, "", None, None, False],       
    ["VENDOR",                          "General",                  "Vendor",                           DataType.VARCHAR,                      None, "", None, None, False],       
    ["WBDIL",                          "General",                  "WBDil",                           DataType.VARCHAR,                      None, "", None, None, False],       
    ["IFDIL",                          "General",                  "IFDil",                           DataType.VARCHAR,                      None, "", None, None, False],       
    ["FORM",                          "General",                  "Form",                           DataType.VARCHAR,                      None, "", None, None, False],       
    ["PRIMARY",                          "General",                  "Primary",                           DataType.VARCHAR,                      None, "", None, None, False],       
    ["SECONDARY",                          "General",                  "Secondary",                           DataType.VARCHAR,                      None, "", None, None, False],       
    ["COUPLED",                          "General",                  "Coupled",                           DataType.VARCHAR,                      None, "", None, None, False],       
    ["BOX",                          "General",                  "Box",                           DataType.VARCHAR,                      None, "", None, None, False],       
    ["EMPTY",                          "General",                  "empty?",                           DataType.VARCHAR,                      None, "", None, None, False],       
    ["CATALOG_NUM",                  "General",                 "Catalog#",                        DataType.VARCHAR,                   None,  "", None, None, False]
    ["ANNOTATIONS_STATE",                "Comments",                "Annotations State",                    DataType.XML,                    None,                                "Annotations State", annotationsScriptName, None]    
    
];


strainDefinition = [
    ["BASE_STRAIN",                             "General",                  "base strain",                             DataType.VARCHAR,       None,  "", None, None, False],
    ["COMMENTS",                             "General",                  "comments",                             DataType.VARCHAR,       None,  "", None, None, False],
    ["DATE_ENTERED",                             "General",                  "date entered",                             DataType.TIMESTAMP,       None,  "", None, None, False],    
    ["DISRUPTIONS",                             "General",                  "disruptions",                             DataType.VARCHAR,       None,  "", None, None, False],    
    ["FROZEN_STOCK",                             "General",                  "frozen stock",                             DataType.BOOLEAN,       None,  "", None, None, False],    
    ["GENOTYPE_CONFIRMATION",                             "General",                  "genotype confirmation",                             DataType.VARCHAR,       None,  "", None, None, False],    
    ["GLYCEROL_STOCK_NUMBER",                             "General",                  "glycerol stock number",                             DataType.INTEGER,       None,  "", None, None, False],    
    ["KWY_NUMBER",                             "General",                  "KWY number",                             DataType.VARCHAR,       None,  "", None, None, False],    
    ["MARKERS",                             "General",                  "markers",                             DataType.VARCHAR,       None,  "", None, None, False],    
    ["MAT",                             "General",                  "MAT",                             DataType.VARCHAR,       None,  "", None, None, False],    
    ["REMOVED",                             "General",                  "removed",                             DataType.VARCHAR,       None,  "", None, None, False],    
    ["UNMARKED_MUTATIONS",                             "General",                  "unmarked mutations",                             DataType.VARCHAR,       None,  "", None, None, False],    
    ["WHO_ENTERED",                             "General",                  "who entered",                             DataType.VARCHAR,       None,  "", None, None, False]    
    ["ANNOTATIONS_STATE",                "Comments",                "Annotations State",                    DataType.XML,                    None,                                "Annotations State", annotationsScriptName, None]    
];

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
    ["UC_NUMBER",                          "General",                  "UC Number",                           DataType.VARCHAR,                      None, "", None, None, False],
    ["UG_UL",                          "General",                  "ug_ul",                           DataType.REAL,                      None, "", None, None, False],
    ["VOLUME",                          "General",                  "Volume",                           DataType.REAL,                      None, "", None, None, False],
    ["STATUS_RECORDS",                          "General",                  "StatusRecords",                           DataType.INTEGER,                      None, "", None, None, False],
    ["CLICK_COUNTER",                          "General",                  "click counter",                           DataType.INTEGER,                      None, "", None, None, False],    
    ["CATEGORY",                          "General",                  "category",                           DataType.CONTROLLEDVOCABULARY,                      "OLIGO_CATEGORY", "", None, None, False],
    ["REORDERED",                          "General",                  "Reordered",                           DataType.VARCHAR,                      None, "", None, None, False],
    ["NOTES",                          "Comments",                  "Notes",                           DataType.VARCHAR,                      None, "", None, None, False]
    ["ANNOTATIONS_STATE",                "Comments",                "Annotations State",                    DataType.XML,                    None,                                "Annotations State", annotationsScriptName, None]
];

plasmidDefinition = [
    ["3END",                        "General",                  "3' end",                         DataType.VARCHAR,      None,  "", None, None, False],
    ["3END_INSERT",                        "General",                  "3' end insert",                         DataType.VARCHAR,      None,  "", None, None, False],
    ["3INSERT_MOD",                        "General",                  "3' insert modif.",                         DataType.VARCHAR,      None,  "", None, None, False],
    ["3MOD",                        "General",                  "3' Modif.",                         DataType.VARCHAR,      None,  "", None, None, False],
    ["5END",                        "General",                  "5' end",                         DataType.VARCHAR,      None,  "", None, None, False],
    ["5END_MOD",                        "General",                  "5' end insert",                         DataType.VARCHAR,      None,  "", None, None, False],
    ["5INSERT_MOD",                        "General",                  "5' insert modif.",                         DataType.VARCHAR,      None,  "", None, None, False],
    ["5MOD",                        "General",                  "5' Modif.",                         DataType.VARCHAR,      None,  "", None, None, False],
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
    ["ANNOTATIONS_STATE",                "Comments",                "Annotations State",                    DataType.XML,                    None,                                "Annotations State", annotationsScriptName, None]
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
    ["ANNOTATIONS_STATE",                "Comments",                "Annotations State",                    DataType.XML,                    None,                                "Annotations State", annotationsScriptName, None]
];

RestrictionEnzymeDefinition = [
    ["NAME",                            "General",                  "Name",                             DataType.VARCHAR,                   None,  "", None, None, False],
    ["BLOCK",                           "Storage",                  "Block",                            DataType.VARCHAR,                   None,  "", None, None, False],
    ["POSITION",                        "Storage",                  "Position",                         DataType.VARCHAR,                   None,  "", None, None, False],
    ["COMMENT",                         "Comments",                  "Comment",                          DataType.VARCHAR,                   None,  "", None, None, False]
    ["ANNOTATIONS_STATE",                "Comments",                "Annotations State",                    DataType.XML,                    None,                                "Annotations State", annotationsScriptName, None]  
];

ExperimentalStepDefinition= [
    ["NAME",                             "General",                "Name",                                    DataType.MULTILINE_VARCHAR,        None,                                "Name", None, None],
    ["OWNER",                             "General",                "Owner",                                DataType.CONTROLLEDVOCABULARY,    "LAB_MEMBERS",                            "Who produced/owned the sample", None, None],
    ["EXPERIMENTAL_GOALS",                 "General",                "Experimental Goals",                    DataType.MULTILINE_VARCHAR,        None,                                "Goal of the experiment", None, None],
    ["EXPERIMENTAL_RESULTS",             "General",                "Experimental Results",                    DataType.MULTILINE_VARCHAR,        None,                                "Brief summary of the results obtained", None, None],
    ["START_DATE",             "General",                "Start date",                    DataType.TIMESTAMP,        None,                                "Date when the experimental step is started", None, None],
    ["END_DATE",             "General",                "End date",                    DataType.TIMESTAMP,        None,                                "Date when the experimental step is completed", None, None],
    ["EXPERIMENTAL_READOUT",             "Readout details",        "Experimental readout",                    DataType.CONTROLLEDVOCABULARY,    "EXPERIMENTAL_READOUT",                "Experimental readout used in the experiment", None, None],
    ["MACHINE",                         "Readout details",        "Machine",                                DataType.CONTROLLEDVOCABULARY,    "MACHINE",                            "Machine used to perform the experiment", None, None],
    ["FREEFORM_TABLE_STATE",             "Readout details",        "Freeform Table State",                    DataType.MULTILINE_VARCHAR,        None,                                "Table describing how the order of samples measured in the experiments", None, None],
    ["PUBLICATION",                     "Comments",                "Publication",                            DataType.MULTILINE_VARCHAR,        None,                                "Publication from where the information was first found OR technical sheet given by the manufacturer", None, None],
    ["NOTES",                             "Comments",             "Notes",                                DataType.MULTILINE_VARCHAR,        None,                                "Notes", None, None],
    ["XMLCOMMENTS",                     "Comments",                "Comments List",                        DataType.XML,                    None,                                "Several comments can be added by different users", commentsSampleScriptName, None],
    ["ANNOTATIONS_STATE",                "Comments",                "Annotations State",                    DataType.XML,                    None,                                "Annotations State", annotationsScriptName, None]
];
