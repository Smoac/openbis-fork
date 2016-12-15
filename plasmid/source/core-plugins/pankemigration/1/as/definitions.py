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
annotationsScriptName="ANNOTATIONS"
geneticModificationsScriptName = "GENETIC_MODIFICATIONS";

seqFileDefinition = [
    ["NAME", "General", "Name", DataType.VARCHAR, None,    "Name", None, None, False],
    ["NOTES", "General information", "Notes", DataType.MULTILINE_VARCHAR, None, "Notes regarding the dataset", None, None,False],
    ["XMLCOMMENTS",    "Comments","Comments List",    DataType.XML,    None,    "Several comments can be added by different users", "COMMENTS_DATA_SET", None,False]
];


#
#Sample Types
#


strainDefinition = [
    ["NAME",                             "General",                  "Name",                             DataType.VARCHAR,       None,  "", None, None, False],      
    ["BBPL_NUM",                         "General",                  "bBPL Number",                             DataType.VARCHAR,       None,  "", None, None, False],    
    ["PLASMID",                          "General",                  "Plasmid",                             DataType.VARCHAR,       None,  "", None, None, False],    
    ["GENOTYPE",                         "General",                  "Genotype",                             DataType.MULTILINE_VARCHAR,       None,  "", None, None, False],
    ["SOURCE",                           "General",                  "Source",                             DataType.VARCHAR,       None,  "", None, None, False],
    ["DEPOSITOR",                        "General",                  "Depositor",                             DataType.CONTROLLEDVOCABULARY,       "LAB_MEMBERS",  "", None, None, False],       
    ["REFERENCE",                        "General",                  "Reference",                             DataType.MULTILINE_VARCHAR,       None,  "", None, None, False],
    ["OTHER_COLLECTION_NUMBER",          "General",                  "Other Collection Number",                             DataType.VARCHAR,       None,  "", None, None, False],    
    ["MEDIUM",                           "General",                  "Medium",                             DataType.VARCHAR,       None,  "", None, None, False],    
    ["RESISTANCE",                       "General",                  "Resistance",                             DataType.VARCHAR,       None,  "", None, None, False],    
    ["REMARKS",                          "General",                  "Application/Remarks",                             DataType.MULTILINE_VARCHAR,       None,  "", None, None, False],    
    ["DATE",                             "General",                  "Date",                             DataType.TIMESTAMP,       None,  "", None, None, False],    
    ["OTHER",                            "General",                  "Other",                             DataType.MULTILINE_VARCHAR,       None,  "", None, None, False],    
    ["-BPL_NUMBER",                       "General",                  "BPL Number",                             DataType.VARCHAR,       None,  "", None, None, False],    
    ["+GENETIC_MODIFICATIONS",           "Genotype",                 "Genetic modifications",            DataType.MULTILINE_VARCHAR,        None,   "", None, geneticModificationsScriptName, False],
    ["+STORAGE_NAMES",                   "Physical Storage",         "Storage Name",            DataType.CONTROLLEDVOCABULARY,        "STORAGE_NAMES",    "Storage Name", None, None, False],
    ["+STORAGE_ROW",                     "Physical Storage",         "Storage Row",            DataType.INTEGER,                    None,                "Storage Row", None, None, False],
    ["+STORAGE_COLUMN",                  "Physical Storage",         "Storage Column",        DataType.INTEGER,                    None,                "Storage Column", None, None, False],
    ["STORAGE_BOX_NAME",                 "Physical Storage",         "Box",        DataType.VARCHAR,                    None,                "Storage Box Name", None, None, False],
    ["+STORAGE_BOX_SIZE",                "Physical Storage",         "Storage Box Size",        DataType.CONTROLLEDVOCABULARY,        "STORAGE_BOX_SIZE",    "Storage Box Size", None, None, False],
    ["+STORAGE_USER",                    "Physical Storage",         "Storage User Id",        DataType.VARCHAR,                    None,                "Storage User Id", None, None, False],
    ["STORAGE_POSITION",                 "Physical Storage",         "Location",        DataType.VARCHAR,                    None,                "Storage Position", None, None, False],
    ["+ANNOTATIONS_STATE",               "Comments",                " Annotations State",                    DataType.XML,                    None,"Annotations State", annotationsScriptName, None, False]
];

oligoDefinition = [
    ["NAME",                             "General",                  "Name",                             DataType.VARCHAR,       None,  "", None, None, False],      
    ["DESCRIPTION",                         "General",                  "Description",                             DataType.MULTILINE_VARCHAR,       None,  "", None, None, False],    
    ["DEPOSITOR",                        "General",                  "Depositor",                             DataType.CONTROLLEDVOCABULARY,       "LAB_MEMBERS",  "", None, None, False],       
    ["DATE",                             "General",                  "Date",                             DataType.TIMESTAMP,       None,  "", None, None, False],    
    ["REMARKS",                          "General",                  "Application/Remarks",                             DataType.MULTILINE_VARCHAR,       None,  "", None, None, False],    
    ["SEQUENCE",                        "General",                  "Sequence",                             DataType.VARCHAR,       None,  "", None, None, False],
    ["ALTERNATE_OL_NAME",          "General",                  "Alternate Oligo Name",                             DataType.VARCHAR,       None,  "", None, None, False],    
    ["OBPL_NUMBER",                       "General",                  "oBPL Number",                             DataType.VARCHAR,       None,  "", None, None, False],    
    ["+STORAGE_NAMES",                   "Physical Storage",         "Storage Name",            DataType.CONTROLLEDVOCABULARY,        "STORAGE_NAMES",    "Storage Name", None, None, False],
    ["+STORAGE_ROW",                     "Physical Storage",         "Storage Row",            DataType.INTEGER,                    None,                "Storage Row", None, None, False],
    ["+STORAGE_COLUMN",                  "Physical Storage",         "Storage Column",        DataType.INTEGER,                    None,                "Storage Column", None, None, False],
    ["STORAGE_BOX_NAME",                 "Physical Storage",         "Box",        DataType.VARCHAR,                    None,                "Storage Box Name", None, None, False],
    ["+STORAGE_BOX_SIZE",                "Physical Storage",         "Storage Box Size",        DataType.CONTROLLEDVOCABULARY,        "STORAGE_BOX_SIZE",    "Storage Box Size", None, None, False],
    ["+STORAGE_USER",                    "Physical Storage",         "Storage User Id",        DataType.VARCHAR,                    None,                "Storage User Id", None, None, False],
    ["STORAGE_POSITION",                 "Physical Storage",         "Location",        DataType.VARCHAR,                    None,                "Storage Position", None, None, False],
    ["+ANNOTATIONS_STATE",               "Comments",                " Annotations State",                    DataType.XML,                    None,"Annotations State", annotationsScriptName, None, False]
];

plasmidDefinition = [
    ["NAME",                             "General",                  "Name",                             DataType.VARCHAR,       None,  "", None, None, False],      
    ["DESCRIPTION",                         "General",                  "Description",                             DataType.MULTILINE_VARCHAR,       None,  "", None, None, False],    
    ["SOURCE",                           "General",                  "Source",                             DataType.VARCHAR,       None,  "", None, None, False],
    ["DEPOSITOR",                        "General",                  "Depositor",                             DataType.CONTROLLEDVOCABULARY,       "LAB_MEMBERS",  "", None, None, False],       
    ["DATE",                             "General",                  "Date",                             DataType.TIMESTAMP,       None,  "", None, None, False],    
    ["REFERENCE",                        "General",                  "Reference",                             DataType.MULTILINE_VARCHAR,       None,  "", None, None, False],
    ["DERIVATIVE_OF",                    "General",                  "Derivative of",                             DataType.VARCHAR,       None,  "", None, None, False],    
    ["RESISTANCE",                       "General",                  "Resistance",                             DataType.VARCHAR,       None,  "", None, None, False],    
    ["REMARKS",                          "General",                  "Application/Remarks",                             DataType.MULTILINE_VARCHAR,       None,  "", None, None, False],    
    ["ALTERNATE_PL_NUMBER",              "General",                  "Alternate Plasmid Number",                             DataType.VARCHAR,       None,  "", None, None, False],    
    ["PL_NUMBER",                        "General",                  "Plasmid Number",                             DataType.INTEGER,       None,  "", None, None, False],    
    ["ORIGIN",                            "General",                  "Origin",                             DataType.VARCHAR,       None,  "", None, None, False],    
    ["VERIFIED_BY",                            "General",                  "Verified by:",                             DataType.VARCHAR,       None,  "", None, None, False],    
    ["VERIFIED_BY_2",                            "General",                  "Verified by 2",                             DataType.VARCHAR,       None,  "", None, None, False],    
    ["VERIFIED_BY_3",                            "General",                  "Verified by 3",                             DataType.VARCHAR,       None,  "", None, None, False],    
    ["VERIFIED_BY_4",                            "General",                  "Verified by 4",                             DataType.VARCHAR,       None,  "", None, None, False],    
    ["GLYCEROL",                            "General",                  "Glycerol",                             DataType.VARCHAR,       None,  "", None, None, False],    
    ["PROMOTER",                            "General",                  "Promoter",                             DataType.VARCHAR,       None,  "", None, None, False],    
    ["-SEQUENCE",                            "General",                  "Sequence",                             DataType.VARCHAR,       None,  "", None, None, False],    
    ["+STORAGE_NAMES",                   "Physical Storage",         "Storage Name",            DataType.CONTROLLEDVOCABULARY,        "STORAGE_NAMES",    "Storage Name", None, None, False],
    ["+STORAGE_ROW",                     "Physical Storage",         "Storage Row",            DataType.INTEGER,                    None,                "Storage Row", None, None, False],
    ["+STORAGE_COLUMN",                  "Physical Storage",         "Storage Column",        DataType.INTEGER,                    None,                "Storage Column", None, None, False],
    ["STORAGE_BOX_NAME",                 "Physical Storage",         "Box",        DataType.VARCHAR,                    None,                "Storage Box Name", None, None, False],
    ["+STORAGE_BOX_SIZE",                "Physical Storage",         "Storage Box Size",        DataType.CONTROLLEDVOCABULARY,        "STORAGE_BOX_SIZE",    "Storage Box Size", None, None, False],
    ["+STORAGE_USER",                    "Physical Storage",         "Storage User Id",        DataType.VARCHAR,                    None,                "Storage User Id", None, None, False],
    ["STORAGE_POSITION",                 "Physical Storage",         "Location",        DataType.VARCHAR,                    None,                "Storage Position", None, None, False],
    ["+ANNOTATIONS_STATE",               "Comments",                " Annotations State",                    DataType.XML,                    None,"Annotations State", annotationsScriptName, None, False]
];


