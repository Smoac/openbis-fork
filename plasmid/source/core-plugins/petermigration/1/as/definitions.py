#!/usr/bin/env python

import ch.systemsx.cisd.openbis.generic.server.jython.api.v1.DataType as DataType

storageGroupDefinition = [
    ["STORAGE_NAMES_0",        "Physical Storage 0",        "antibody ID data box::location",         DataType.CONTROLLEDVOCABULARY,      "FREEZER",    "Storage Name", None],
    ["STORAGE_ROW_0",          "Physical Storage 0",        "Storage Row",                            DataType.INTEGER,                    None,                "Storage Row", None],
    ["STORAGE_COLUMN_0",       "Physical Storage 0",        "Storage Column",                         DataType.INTEGER,                    None,                "Storage Column", None],
    ["STORAGE_BOX_NAME_0",     "Physical Storage 0",        "antibody ID data box::box label",        DataType.VARCHAR,                    None,                "Storage Box Name", None],
    ["STORAGE_USER_0",         "Physical Storage 0",        "antibody ID data box::frozen by",        DataType.VARCHAR,                    None,                "Storage User Id", None],
    ["STORAGE_BOX_POSITION_0", "Physical Storage 0",        "antibody ID data box::position",         DataType.VARCHAR,                    None,                "Storage User Id", None]
];

antibodyDefinition = [
    ["NAME",                           "General",                "antibody ID",                        DataType.VARCHAR,                    None,  "", None],
    ["ANTIBODY_ID_NR",                 "General",                "antibody_id_nr",                     DataType.VARCHAR,                    None,  "", None],
    ["ANTIGEN",                        "General",                "antigen",                            DataType.VARCHAR,                    None,  "", None],
    ["BARCODE",                        "General",                "barcode",                            DataType.VARCHAR,                    None,  "", None],
    ["BARCODE_LABEL",                  "General",                "barcode label",                      DataType.VARCHAR,                    None,  "", None],
    ["ANTIBODY_FACS_BLOCK",            "General",                "block facs",                         DataType.CONTROLLEDVOCABULARY,       "BLOCK",  "", None],
    ["ANTIBODY_IF_BLOCK",              "General",                "block IFF",                          DataType.CONTROLLEDVOCABULARY,       "BLOCK",  "", None],
    ["ANTIBODY_WB_BLOCK",              "General",                "block western",                      DataType.CONTROLLEDVOCABULARY,       "BLOCK",  "", None],
    ["BOX",                            "General",                "box",                                DataType.VARCHAR,                    None,  "", None],
    ["CATALOGUE_NUMBER",               "General",                "catalog id #",                       DataType.VARCHAR,                    None,  "", None],
    ["CLASS",                          "General",                "class",                              DataType.CONTROLLEDVOCABULARY,       "CLASS",  "", None],
    ["CLONE",                          "General",                "clone",                              DataType.VARCHAR,                    None,  "", None],
    ["COMPANY",                        "General",                "company",                            DataType.CONTROLLEDVOCABULARY,       "COMPANY",  "", None],
    ["ANTIBODY_FACS_CONC",             "General",                "conc facs",                          DataType.CONTROLLEDVOCABULARY,       "CONCENTRATION_FACS",  "", None],
    ["ANTIBODY_IF_CONC",               "General",                "conc IFF",                           DataType.CONTROLLEDVOCABULARY,       "CONCENTRATION_FACS",  "", None],
    ["ANTIBODY_IP_CONC",               "General",                "conc ip",                            DataType.CONTROLLEDVOCABULARY,       "CONCENTRATION_IP",  "", None],
    ["ANTIBODY_WB_CONC",               "General",                "conc western",                       DataType.CONTROLLEDVOCABULARY,       "CONCENTRATION_WESTERN",  "", None],
    ["ANTIBODY_CONCENTRATION",         "General",                "concentration",                      DataType.VARCHAR,                    None,  "", None],
    ["ANTIBODY_CROSSREACTIVITY",       "General",                "crossreactivity",                    DataType.VARCHAR,                    None,  "", None],
    ["ANTIBODY_FACS_FIX",              "General",                "fix facs",                           DataType.CONTROLLEDVOCABULARY,       "FIX",  "", None],
    ["ANTIBODY_IF_FIX",                "General",                "fix IFF",                            DataType.CONTROLLEDVOCABULARY,       "FIX",  "", None],
    ["ANTIBODY_WB_FIX",                "General",                "fix western",                        DataType.CONTROLLEDVOCABULARY,       "FIX",  "", None],
    ["COMMENTS",                       "General",                "info",                               DataType.VARCHAR,                    None,  "", None],
    ["INVESTIGATOR",                   "General",                "investigator",                       DataType.CONTROLLEDVOCABULARY,       "ALL_LAB_MEMBERS",  "", None],
    ["LABEL",                          "General",                "label",                              DataType.CONTROLLEDVOCABULARY,       "LABEL",  "", None],
    ["LOTNUMBER",                      "General",                "lotnumber",                          DataType.VARCHAR,                    None,  "", None],
    ["MODIFIED_BY",                    "General",                "modified by",                        DataType.CONTROLLEDVOCABULARY,       "ALL_LAB_MEMBERS",  "", None],
    ["MONO_POLYCLONAL",                "General",                "mono/polyclonal",                    DataType.CONTROLLEDVOCABULARY,       "MONO-POLYCLONAL",  "", None],
    ["ANTIBODY_FACS_NOTES",            "General",                "notes FACS",                         DataType.VARCHAR,                    None,  "", None],
    ["ANTIBODY_IF_NOTES",              "General",                "notes IFF",                          DataType.VARCHAR,                    None,  "", None],
    ["ANTIBODY_IP_NOTES",              "General",                "notes IP",                           DataType.VARCHAR,                    None,  "", None],
    ["ANTIBODY_WB_NOTES",              "General",                "notes western",                      DataType.VARCHAR,                    None,  "", None],
    ["RACK",                           "General",                "rack",                               DataType.VARCHAR,                    None,  "", None],
    ["PUBLISHED_IN",                   "General",                "reference",                          DataType.VARCHAR,                    None,  "", None],
    ["SERIAL_NUMBER",                  "General",                "serial number",                      DataType.INTEGER,                    None,  "", None],
    ["ANTIGEN_SIZE",                   "General",                "size",                               DataType.VARCHAR,                    None,  "", None],
    ["SOURCE",                         "General",                "source",                             DataType.CONTROLLEDVOCABULARY,       "SOURCE",  "", None],
    ["CREATION_DATE",                  "General",                "creation date",                      DataType.TIMESTAMP,                  None,  "", None],
    ["MODIFICATION_DATE",              "General",                "modification date",                  DataType.TIMESTAMP,                  None,  "", None],
    ["FROZEN",                         "General",                "frozen",                             DataType.TIMESTAMP,                  None,  "", None]
];