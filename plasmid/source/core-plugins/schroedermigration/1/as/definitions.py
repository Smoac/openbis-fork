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
# Sample Types
#

# Columns
# propertyCode, section, propertyLabel, dataType, vocabularyCode, propertyDescription, managedScript, dynamicScript, isMandatory, showInEditViews

CELL_LINE = [
    ["CO2",                             "General",                "%CO2",                                DataType.CONTROLLEDVOCABULARY,       "CO2",                "", None, None, False],
    ["ATCC_NUM",                        "General",                "ATCC No.",                            DataType.VARCHAR,                    None,                 "", None, None, False],
    ["NAME",                            "General",                "cell ID",                             DataType.VARCHAR,                    None,                 "", None, None, False],
    ["CELL_ID_COPY",                    "General",                "cell ID Copy",                        DataType.VARCHAR,                    None,                 "", None, None, False],
    ["CELL_ID_SEARCH",                  "General",                "cell ID search",                      DataType.VARCHAR,                    None,                 "", None, None, False],
    ["COMPARE_FIELD",                   "General",                "compare field",                       DataType.VARCHAR,                    None,                 "", None, None, False],
    ["CONC1",                           "General",                "conc.1",                              DataType.REAL,                       None,                 "", None, None, False],
    ["CONC_UNIT1",                      "General",                "concentration unit 1",                DataType.CONTROLLEDVOCABULARY,       "CONC_UNITS",         "", None, None, False],
    ["CONC2",                           "General",                "conc.2",                              DataType.REAL,                       None,                 "", None, None, False],
    ["CONC_UNIT2",                      "General",                "concentration unit 2",                DataType.CONTROLLEDVOCABULARY,       "CONC_UNITS",         "", None, None, False],
    ["CONC3",                           "General",                "conc.3",                              DataType.REAL,                       None,                 "", None, None, False],
    ["CONC_UNIT3",                      "General",                "concentration unit 3",                DataType.CONTROLLEDVOCABULARY,       "CONC_UNITS",         "", None, None, False],
    ["CREATION_DATE",                   "General",                "creation date",                       DataType.TIMESTAMP,                  None,                 "", None, None, False, False],
    ["DATE",                            "General",                "date",                                DataType.TIMESTAMP,                  None,                 "", None, None, False],
    ["FROZEN",                          "General",                "frozen",                              DataType.TIMESTAMP,                  None,                 "", None, None, False],
    ["GROWTH_TEMPERATURE",              "General",                "growth temperature",                  DataType.CONTROLLEDVOCABULARY,       "GROWTH_TEMPERATURE", "", None, None, False],
    ["INVESTIGATOR",                    "General",                "investigator",                        DataType.CONTROLLEDVOCABULARY,       "LAB_MEMBERS",        "", None, None, False],
    ["MEDIUM_FOR_CELL",                 "General",                "medium",                              DataType.CONTROLLEDVOCABULARY,       "MEDIUM",             "", None, None, False],
    ["MEDIUM_SPECIALS",                 "General",                "medium specials",                     DataType.VARCHAR,                    None,                 "", None, None, False],
    ["MODIFICATION_DATE",               "General",                "modification date",                   DataType.TIMESTAMP,                  None,                 "", None, None, False, False],
    ["MODIFIED_BY",                     "General",                "modified by",                         DataType.CONTROLLEDVOCABULARY,       "LAB_MEMBERS",        "", None, None, False, False],
    ["OBTAINED_FROM",                   "General",                "obtained from",                       DataType.CONTROLLEDVOCABULARY,       "CELL_OBTAINED_FROM", "", None, None, False],
    ["PAGE_NUMBER",                     "General",                "page number",                         DataType.VARCHAR,                    None,                 "", None, None, False],
    ["PARENTAL_CELL",                   "General",                "parental cell",                       DataType.VARCHAR,                    None,                 "", None, None, False, False],
    ["PASSAGE",                         "General",                "passage",                             DataType.VARCHAR,                    None,                 "", None, None, False],
    ["PLASMID_ID1",                     "General",                "plasmid ID 1",                        DataType.VARCHAR,                    None,                 "", None, None, False],
    ["PLASMID_ID2",                     "General",                "plasmid ID 2",                        DataType.VARCHAR,                    None,                 "", None, None, False],
    ["PLASMID_ID3",                     "General",                "plasmid ID 3",                        DataType.VARCHAR,                    None,                 "", None, None, False],   
    ["PUBLICATION",                     "General",                "reference",                           DataType.VARCHAR,                    None,                 "", None, None, False],    
    ["SELECTION_PLASMID1",              "General",                "sel.1",                               DataType.CONTROLLEDVOCABULARY,       "SELECTION",          "", None, None, False],
    ["SELECTION_PLASMID2",              "General",                "sel.2",                               DataType.CONTROLLEDVOCABULARY,       "SELECTION",          "", None, None, False],
    ["SELECTION_PLASMID3",              "General",                "sel.3",                               DataType.CONTROLLEDVOCABULARY,       "SELECTION",          "", None, None, False],    
    ["SERIAL_NUMBER",                   "General",                "serial number",                       DataType.INTEGER,                    None,                 "", None, None, False],
    ["SPECIES_FOR_CELL",                "General",                "species",                             DataType.CONTROLLEDVOCABULARY,       "CELL_SPECIES",       "", None, None, False],
    ["TUBES",                           "General",                "tubes",                               DataType.INTEGER,                    None,                 "", None, None, False],
    ["COMMENTS",                        "General",                "info",                                DataType.MULTILINE_VARCHAR,          None,                 "", None, None, False],
    ["XMLCOMMENTS",                     "Comments",               "Comments List",                       DataType.XML,                        None,  "Several comments can be added by different users", "COMMENTS_SAMPLE",         None, False, False],
    ["ANNOTATIONS_STATE",               "Comments",               "Annotations State",                   DataType.XML,                        None,  "Annotations State",                                "ANNOTATIONS_CELL_LINE",   None, False, False]
];

STORAGE_POSITION = [
    ["STORAGE_CODE",                  "Physical Storage",        "Storage Code",              DataType.VARCHAR,                    None,                "Storage Code",            None, None, False, False],
    ["STORAGE_RACK_ROW",              "Physical Storage",        "Storage Rack Row",          DataType.INTEGER,                    None,                "Storage Rack Row",        None, None, False, False],
    ["STORAGE_RACK_COLUMN",           "Physical Storage",        "Storage Rack Column",       DataType.INTEGER,                    None,                "Storage Rack Column",     None, None, False, False],
    ["STORAGE_BOX_NAME",              "Physical Storage",        "Storage Box Name",          DataType.VARCHAR,                    None,                "Storage Box Name",        None, None, False, False],
    ["STORAGE_BOX_SIZE",              "Physical Storage",        "Storage Box Size",          DataType.CONTROLLEDVOCABULARY,       "STORAGE_BOX_SIZE",  "Storage Box Size",        None, None, False, False],
    ["STORAGE_BOX_POSITION",          "Physical Storage",        "Storage Box Position",      DataType.VARCHAR,                    None,                "Storage Box Position",    None, None, False, False],
    ["STORAGE_USER",                  "Physical Storage",        "Storage User Id",           DataType.VARCHAR,                    None,                "Storage User Id",         None, None, False, False],
    ["*LOCATION",                     "General",                 "location",                  DataType.VARCHAR,                    None,                "",                        None, None, False],
    ["*BOX_NAME",                     "General",                 "box label",                 DataType.VARCHAR,                    None,                "",                        None, None, False],
    ["*DRAWER",                       "General",                 "drawer",                    DataType.VARCHAR,                    None,                "",                        None, None, False],
    ["*BOX_SIZE",                     "General",                 "box size",                  DataType.VARCHAR,                    None,                "",                        None, None, False],
    ["*OWNER",                        "General",                 "owner",                     DataType.VARCHAR,                    None,                "",                        None, None, False],
    ["*POSITION",                     "General",                 "position",                  DataType.VARCHAR,                    None,                "",                        None, None, False],
    ["*CELL_ID",                      "General",                 "cell ID",                   DataType.VARCHAR,                    None,                "",                        None, None, False],
    ["BOX_NR",                        "General",                 "box nr.",                   DataType.INTEGER,                    None,                "",                        None, None, False],
    ["INVESTIGATOR",                  "General",                 "investigator",              DataType.CONTROLLEDVOCABULARY,       "LAB_MEMBERS",       "",                        None, None, False],
    ["FROZEN",                        "General",                 "frozen",                    DataType.TIMESTAMP,                  None,                "",                        None, None, False, False],
    ["CONCENTRATION",                 "General",                 "concentration",             DataType.REAL,                       None,                "",                        None, None, False],
    ["COMMENTS",                      "General",                 "info",                      DataType.MULTILINE_VARCHAR,          None,                "",                        None, None, False],
    ["FROZEN_BY",                     "General",                 "frozen by",                 DataType.VARCHAR,                    None,                "",                        None, None, False],
    ["COLOUR",                        "General",                 "colour",                    DataType.VARCHAR,                    None,                "",                        None, None, False],
    ["COLOUR_A1",                     "General",                 "colour A1",                 DataType.VARCHAR,                    None,                "",                        None, None, False],
    ["COLOUR_REST",                   "General",                 "colour rest",               DataType.VARCHAR,                    None,                "",                        None, None, False],
    ["COLOUR_BOX_NR",                 "General",                 "colour box nr",             DataType.VARCHAR,                    None,                "",                        None, None, False],
    ["CREATION_DATE",                 "General",                 "creation date",             DataType.VARCHAR,                    None,                "",                        None, None, False, False],
    ["MODIFICATION_DATE",             "General",                 "modification date",         DataType.VARCHAR,                    None,                "",                        None, None, False, False],
    ["MODIFIED_BY",                   "General",                 "modified by",               DataType.VARCHAR,                    None,                "",                        None, None, False, False],
    ["CLONE_",                        "General",                 "clone#",                    DataType.VARCHAR,                    None,                "",                        None, None, False],
    ["PASSAGE",                       "General",                 "passage",                   DataType.VARCHAR,                    None,                "",                        None, None, False],
    ["BOX_LABEL_VARIABLE",            "General",                 "box label variable",        DataType.VARCHAR,                    None,                "",                        None, None, False, False],
    ["OWNER_VARIABLE",                "General",                 "owner variable",            DataType.VARCHAR,                    None,                "",                        None, None, False, False],
    ["XMLCOMMENTS",                   "Comments",                "Comments List",             DataType.XML,                        None,                "Several comments can be added by different users", "COMMENTS_SAMPLE", None, False, False],
    ["ANNOTATIONS_STATE",             "Comments",                "Annotations State",         DataType.XML,                        None,                "Annotations State", "ANNOTATIONS_STORAGE_POSITION", None, False, False]                 
];
