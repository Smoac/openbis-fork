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
boxLocationScriptName="BOX_LOCATION";
maxPlasmidMapIDScriptName="MAX_PLASMID_MAP_ID"
maxPlasmidIDScriptName="MAX_PLASMID_ID"
slotScriptName="SLOT"
PlasmidMapIDScriptName="PLASMID_MAP_ID"


seqFileDefinition = [
    ["NAME", "General", "Name", DataType.VARCHAR, None,    "Name", None, None, False],
    ["NOTES", "General information", "Notes", DataType.MULTILINE_VARCHAR, None, "Notes regarding the dataset", None, None,False],
    ["XMLCOMMENTS",    "Comments","Comments List",    DataType.XML,    None,    "Several comments can be added by different users", "COMMENTS_DATA_SET", None,False]
];


#
#Sample Types
#


strainDefinition = [
    ["NAME",                             "General",                  "plasmid name",                     DataType.VARCHAR,                  None,  "", None, None, False],      
    ["BOX_LOCATION",                     "General",                  "box location",                     DataType.INTEGER,                  None,  "", boxLocationScriptName, None, False],    
    ["CONSTRUCTION_METHOD",              "General",                  "constructionMethod",               DataType.MULTILINE_VARCHAR,        None,  "", None, None, False],
    ["CREATION_DATE",                    "General",                  "CreationDate",                     DataType.TIMESTAMP,                None,  "", None, None, False],       
    ["INVESTIGATOR",                     "General",                  "investigator",                     DataType.CONTROLLEDVOCABULARY,     "LAB_MEMBERS",  "", None, None, False],
    ["MODIFICATION_DATE",                "General",                  "ModificationDate",                 DataType.TIMESTAMP,                None,  "", None, None, False],    
    ["PLASMID_ID",                       "General",                  "plasmid_ID",                       DataType.INTEGER,                  None,  "", None, None, False],    
    ["BC_STRAIN_RESISTANCE",             "General",                  "resistance",                       DataType.CONTROLLEDVOCABULARY,     "BC_STRAIN_RESISTANCE",  "", None, None, False],
    ["SEQUENCE",                         "General",                  "sequence",                         DataType.MULTILINE_VARCHAR,        None,  "", None, None, False],
    ["SLOT",                             "General",                  "slot",                             DataType.INTEGER,                  None,  "", slotScriptName, None, False],
    ["STRAIN_VECTOR",                    "General",                  "vector",                           DataType.VARCHAR,                  None,  "", None, None, False],
    ["KEYWORDS",                         "General",                  "keywords",                         DataType.CONTROLLEDVOCABULARY,     "KEYWORDS",  "", None, None, False],
    ["BC_STRAIN_SOURCE",                 "General",                  "source",                           DataType.CONTROLLEDVOCABULARY,     "BC_STRAIN_SOURCE",  "", None, None, False],
    ["BC_STRAIN_ORGANISM",               "General",                  "organism",                         DataType.CONTROLLEDVOCABULARY,     "BC_STRAIN_ORGANISM",  "", None, None, False],
    ["PUBLICATION",                      "References",               "reference",                        DataType.MULTILINE_VARCHAR,        None,  "", None, None, False],    
    ["COMMENTS",                         "Comments",                 "comments",                         DataType.MULTILINE_VARCHAR,        None,  "", None, None, False],
    ["+ANNOTATIONS_STATE",               "Comments",                 "Annotations State",                DataType.XML,                      None,"Annotations State", annotationsScriptName, None, False]
];

oligoDefinition = [
    ["NAME",                             "General",                  "Primer_name",                      DataType.VARCHAR,                  None,  "", None, None, False],      
    ["SEQUENCE",                         "General",                  "Primer_Sequence",                  DataType.MULTILINE_VARCHAR,        None,  "", None, None, False],    
    ["OLIGO_TARGET",                     "General",                  "Target",                           DataType.MULTILINE_VARCHAR,        None,  "", None, None, False],       
    ["-PRIMER_NUMBER",                   "General",                  "Primer_number",                    DataType.INTEGER,                None,  "", None, None, False],    
    ["+ANNOTATIONS_STATE",               "Comments",                 "Annotations State",                DataType.XML,                      None,"Annotations State", annotationsScriptName, None, False]
];

DNADefinition = [
    ["NAME",                             "General",                  "plasmid name",                     DataType.VARCHAR,                  None,  "", None, None, False],      
    ["BOX_LOCATION",                     "General",                  "box location",                     DataType.INTEGER,                  None,  "", boxLocationScriptName, None, False],    
    ["CONSTRUCTION_METHOD",              "General",                  "constructionMethod",               DataType.MULTILINE_VARCHAR,        None,  "", None, None, False],
    ["CREATION_DATE",                    "General",                  "CreationDate",                     DataType.TIMESTAMP,                None,  "", None, None, False],       
    ["INVESTIGATOR",                     "General",                  "investigator",                     DataType.CONTROLLEDVOCABULARY,     "LAB_MEMBERS",  "", None, None, False],
    ["MODIFICATION_DATE",                "General",                  "ModificationDate",                 DataType.TIMESTAMP,                None,  "", None, None, False],    
    ["PLASMID_ID",                       "General",                  "plasmid_ID",                       DataType.INTEGER,                  None,  "", None, None, False],    
    ["DNA_SIZE",                         "General",                  "size",                             DataType.REAL,                  None,  "", None, None, False],    
    ["RESTRICTION_ENZYME",               "General",                  "Restriction enzymes",              DataType.VARCHAR,                  None,  "", None, None, False],    
    ["DNA_PURITY",                       "General",                  "purity",                           DataType.CONTROLLEDVOCABULARY,     "DNA_PURITY",  "", None, None, False],    
    ["PCR_POLYMERASE",                   "General",                  "PCR polymerase",                   DataType.CONTROLLEDVOCABULARY,     "PCR_POLYMERASE",  "", None, None, False],    
    ["DNA_TYP",                          "General",                  "DNA typ",                          DataType.CONTROLLEDVOCABULARY,     "DNA_TYP",  "", None, None, False],    
    ["DNA_MODIFICATIONS",                "General",                  "DNA modifications",                DataType.CONTROLLEDVOCABULARY,     "DNA_MODIFICATIONS",  "", None, None, False],    
    ["DNA_RESISTANCE",                   "General",                  "resistance",                       DataType.CONTROLLEDVOCABULARY,     "DNA_RESISTANCE",  "", None, None, False],
    ["SEQUENCE",                         "General",                  "Sequence",                         DataType.MULTILINE_VARCHAR,        None,  "", None, None, False],
    ["SLOT",                             "General",                  "slot",                             DataType.INTEGER,                  None,  "", slotScriptName, None, False],
    ["DNA_VECTOR",                        "General",                  "vector",                           DataType.VARCHAR,                  None,  "", None, None, False],
    ["PUBLICATION",                      "References",               "reference",                        DataType.MULTILINE_VARCHAR,        None,  "", None, None, False],    
    ["COMMENTS",                         "Comments",                 "comments",                         DataType.MULTILINE_VARCHAR,        None,  "", None, None, False],
    ["+ANNOTATIONS_STATE",               "Comments",                 "Annotations State",                DataType.XML,                      None,"Annotations State", annotationsScriptName, None, False]
];


