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


strainDefinition = [
    ["NAME",                             "General",                  "Name",                             DataType.MULTILINE_VARCHAR,       None,  "", None, None, False],      
    ["BBPL_NUM",                         "General",                  "bBPL Number",                             DataType.VARCHAR,       None,  "", None, None, False],    
    ["PLASMID",                          "General",                  "Plasmid",                             DataType.VARCHAR,       None,  "", None, None, False],    
    ["GENOTYPE",                         "General",                  "Genotype",                             DataType.MULTILINE_VARCHAR,       None,  "", None, None, False],
    ["SOURCE",                           "General",                  "Source",                             DataType.VARCHAR,       None,  "", None, None, False],
    ["DEPOSITOR",                        "General",                  "Depositor",                             DataType.VARCHAR,       None,  "", None, None, False],       
    ["REFERENCE",                        "General",                  "Reference",                             DataType.MULTILINE_VARCHAR,       None,  "", None, None, False],
    ["OTHER_COLLECTION_NUMBER",          "General",                  "Other Collection Number",                             DataType.VARCHAR,       None,  "", None, None, False],    
    ["MEDIUM",                           "General",                  "Medium",                             DataType.VARCHAR,       None,  "", None, None, False],    
    ["RESISTANCE",                       "General",                  "Resistance",                             DataType.VARCHAR,       None,  "", None, None, False],    
    ["REMARKS",                          "General",                  "Application/Remarks",                             DataType.MULTILINE_VARCHAR,       None,  "", None, None, False],    
    ["DATE",                             "General",                  "Date",                             DataType.TIMESTAMP,       None,  "", None, None, False],    
    ["BPL_NUMBER",                       "General",                  "BPL Number",                             DataType.VARCHAR,       None,  "", None, None, False],    
    ["OTHER",                            "General",                  "Other",                             DataType.MULTILINE_VARCHAR,       None,  "", None, None, False],    
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
