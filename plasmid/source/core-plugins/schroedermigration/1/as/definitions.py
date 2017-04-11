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


#
# Storage 
#

def getStorageGroupPropertyCodes():
    propertyCodes = [];
    for property in getStorageGroupDefinition():
        propertyCodes.append(property[0]);
    return propertyCodes;

def getStorageGroupDefinition():
    return [
    ["STORAGE_NAME",         "Physical Storage",        "location",         DataType.CONTROLLEDVOCABULARY,      "FREEZER",            "Storage Name",           None, None, False],
    ["STORAGE_ROW",          "Physical Storage",        "drawer",           DataType.INTEGER,                    None,                "Storage Row",            None, None, False],
    ["STORAGE_COLUMN",       "Physical Storage",        "drawer",           DataType.INTEGER,                    None,                "Storage Column",         None, None, False],
    ["STORAGE_BOX_NAME",     "Physical Storage",        "box nr.",          DataType.VARCHAR,                    None,                "Storage Box Name",       None, None, False],
    ["STORAGE_USER",         "Physical Storage",        "frozen by",        DataType.VARCHAR,                    None,                "Storage User Id",        None, None, False],
    ["STORAGE_BOX_SIZE",     "Physical Storage",        "box size",         DataType.CONTROLLEDVOCABULARY,      "STORAGE_BOX_SIZE",   "Storage Box size",       None, None, False],
    ["STORAGE_BOX_POSITION", "Physical Storage",        "position",         DataType.VARCHAR,                    None,                "Storage Box Position",   None, None, False]
];



#
# Sample Types
#

cellDefinition = [
    ["CO2",                             "General",                "%CO2",                                DataType.CONTROLLEDVOCABULARY,       "CO2",  "", None, None, False],
    ["ATCC_NUM",                        "General",                "ATCC No.",                            DataType.VARCHAR,                    None,  "", None, None, False],
    ["NAME",                            "General",                "cell ID",                             DataType.VARCHAR,                    None,  "", None, None, False],
    ["CELL_ID_COPY",                    "General",                "cell ID Copy",                        DataType.VARCHAR,                    None,  "", None, None, False],
    ["CELL_ID_SEARCH",                  "General",                "cell ID search",                      DataType.VARCHAR,                    None,  "", None, None, False],
    ["COMPARE_FIELD",                   "General",                "compare field",                       DataType.VARCHAR,                    None,  "", None, None, False],
    ["CONC1",                           "General",                "conc.1",                              DataType.REAL,                       None,  "", None, None, False],
    ["CONC_UNIT1",                      "General",                "concentration unit 1",                DataType.CONTROLLEDVOCABULARY,       "CONC_UNITS",  "", None, None, False],
    ["CONC2",                           "General",                "conc.2",                              DataType.REAL,                       None,  "", None, None, False],
    ["CONC_UNIT2",                      "General",                "concentration unit 2",                DataType.CONTROLLEDVOCABULARY,       "CONC_UNITS",  "", None, None, False],
    ["CONC3",                           "General",                "conc.3",                              DataType.REAL,                       None,  "", None, None, False],
    ["CONC_UNIT3",                      "General",                "concentration unit 3",                DataType.CONTROLLEDVOCABULARY,       "CONC_UNITS",  "", None, None, False],
    ["CREATION_DATE",                   "General",                "creation date",                       DataType.TIMESTAMP,                  None,  "", None, None, False],
    ["DATE",                            "General",                "date",                                DataType.TIMESTAMP,                  None,  "", None, None, False],
    ["FROZEN",                          "General",                "frozen",                              DataType.TIMESTAMP,                  None,  "", None, None, False],
    ["GROWTH_TEMPERATURE",              "General",                "growth temperature",                  DataType.CONTROLLEDVOCABULARY,       "GROWTH_TEMPERATURE",  "", None, None, False],
    ["INVESTIGATOR",                    "General",                "investigator",                        DataType.CONTROLLEDVOCABULARY,       "LAB_MEMBERS",  "", None, None, False],
    ["MEDIUM",                          "General",                "medium",                              DataType.CONTROLLEDVOCABULARY,       "MEDIUM",  "", None, None, False],
    ["MEDIUM_SPECIALS",                 "General",                "medium specials",                     DataType.VARCHAR,                    None,  "", None, None, False],
    ["MODIFICATION_DATE",               "General",                "modification date",                   DataType.TIMESTAMP,                  None,  "", None, None, False],
    ["MODIFIED_BY",                     "General",                "modified by",                         DataType.CONTROLLEDVOCABULARY,       "ALL_LAB_MEMBERS",  "", None, None, False],
    ["OBTAINED_FROM",                   "General",                "obtained from",                       DataType.CONTROLLEDVOCABULARY,       "CELL_OBTAINED_FROM",  "", None, None, False],
    ["PAGE_NUMBER",                     "General",                "page number",                         DataType.VARCHAR,                    None,  "", None, None, False],
    ["PARENTAL_CELL",                   "General",                "parental cell",                       DataType.VARCHAR,                    None,  "", None, None, False],
    ["PASSAGE",                         "General",                "passage",                             DataType.VARCHAR,                    None,  "", None, None, False],
    ["PLASMID_ID1",                     "General",                "plasmid ID 1",                        DataType.VARCHAR,                    None,  "", None, None, False],
    ["PLASMID_ID2",                     "General",                "plasmid ID 2",                        DataType.VARCHAR,                    None,  "", None, None, False],
    ["PLASMID_ID3",                     "General",                "plasmid ID 3",                        DataType.VARCHAR,                    None,  "", None, None, False],   
    ["PUBLICATION",                     "General",                "reference",                           DataType.VARCHAR,                    None,  "", None, None, False],    
    ["SELECTION_PLASMID1",              "General",                "sel.1",                               DataType.CONTROLLEDVOCABULARY,       "SELECTION",  "", None, None, False],
    ["SELECTION_PLASMID2",              "General",                "sel.2",                               DataType.CONTROLLEDVOCABULARY,       "SELECTION",  "", None, None, False],
    ["SELECTION_PLASMID3",              "General",                "sel.3",                               DataType.CONTROLLEDVOCABULARY,       "SELECTION",  "", None, None, False],    
    ["SERIAL_NUMBER",                   "General",                "serial number",                       DataType.INTEGER,                    None,  "", None, None, False],
    ["SPECIES",                         "General",                "species",                             DataType.CONTROLLEDVOCABULARY,       "CELL_SPECIES",  "", None, None, False],
    ["TUBES",                           "Storage",                "tubes",                               DataType.INTEGER,                    None,  "", None, None, False],
    ["COMMENTS",                        "Comments",               "info",                                DataType.MULTILINEVARCHAR,           None,  "", None, None, False]
];


