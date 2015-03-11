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
guanosineScriptName= "GUANOSINE_COUNT"
lengthScriptName= "SEQUENCE_LENGTH"
nucelotideScriptName= "ONUCLEOTIDE_COUNT"
thymidineScriptName= "THYMIDINE_COUNT"
tmScriptName= "TM"

#
# Storage 
#
numberOfStorageGroups = 65

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
    ["STORAGE_USER",         "Physical Storage",        "frozen by",        DataType.CONTROLLEDVOCABULARY,      "ALL_LAB_MEMBERS",    "Storage User Id",    None, None, False],
    ["STORAGE_BOX_POSITION", "Physical Storage",        "position",         DataType.VARCHAR,                    None,                "Storage Box Position",    None, None, False]
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
antibodyDefinition = [
    ["NAME",                           "General",                "antibody ID",                        DataType.VARCHAR,                    None,  "", None, None, False],
    ["ANTIBODY_ID_NR",                 "General",                "antibody_id_nr",                     DataType.VARCHAR,                    None,  "", None, None, False],
    ["ANTIGEN",                        "General",                "antigen",                            DataType.VARCHAR,                    None,  "", None, None, False],
    ["BARCODE",                        "General",                "barcode",                            DataType.VARCHAR,                    None,  "", None, None, False],
    ["BARCODE_LABEL",                  "General",                "barcode label",                      DataType.VARCHAR,                    None,  "", None, None, False],
    ["ANTIBODY_FACS_BLOCK",            "General",                "block facs",                         DataType.CONTROLLEDVOCABULARY,       "BLOCK",  "", None, None, False],
    ["ANTIBODY_IF_BLOCK",              "General",                "block IFF",                          DataType.CONTROLLEDVOCABULARY,       "BLOCK",  "", None, None, False],
    ["ANTIBODY_WB_BLOCK",              "General",                "block western",                      DataType.CONTROLLEDVOCABULARY,       "BLOCK",  "", None, None, False],
    ["CATALOGUE_NUMBER",               "General",                "catalog id #",                       DataType.VARCHAR,                    None,  "", None, None, False],
    ["CLASS",                          "General",                "class",                              DataType.CONTROLLEDVOCABULARY,       "CLASS",  "", None, None, False],
    ["CLONE",                          "General",                "clone",                              DataType.VARCHAR,                    None,  "", None, None, False],
    ["COMPANY",                        "General",                "company",                            DataType.CONTROLLEDVOCABULARY,       "COMPANY",  "", None, None, False],
    ["ANTIBODY_FACS_CONC",             "General",                "conc facs",                          DataType.CONTROLLEDVOCABULARY,       "CONCENTRATION_FACS",  "", None, None, False],
    ["ANTIBODY_IF_CONC",               "General",                "conc IFF",                           DataType.CONTROLLEDVOCABULARY,       "CONCENTRATION_FACS",  "", None, None, False],
    ["ANTIBODY_IP_CONC",               "General",                "conc ip",                            DataType.CONTROLLEDVOCABULARY,       "CONCENTRATION_IP",  "", None, None, False],
    ["ANTIBODY_WB_CONC",               "General",                "conc western",                       DataType.CONTROLLEDVOCABULARY,       "CONCENTRATION_WESTERN",  "", None, None, False],
    ["ANTIBODY_CONCENTRATION",         "General",                "concentration",                      DataType.VARCHAR,                    None,  "", None, None, False],
    ["ANTIBODY_CROSSREACTIVITY",       "General",                "crossreactivity",                    DataType.VARCHAR,                    None,  "", None, None, False],
    ["ANTIBODY_FACS_FIX",              "General",                "fix facs",                           DataType.CONTROLLEDVOCABULARY,       "FIX",  "", None, None, False],
    ["ANTIBODY_IF_FIX",                "General",                "fix IFF",                            DataType.CONTROLLEDVOCABULARY,       "FIX",  "", None, None, False],
    ["ANTIBODY_WB_FIX",                "General",                "fix western",                        DataType.CONTROLLEDVOCABULARY,       "FIX",  "", None, None, False],
    ["INVESTIGATOR",                   "General",                "investigator",                       DataType.CONTROLLEDVOCABULARY,       "ALL_LAB_MEMBERS",  "", None, None, False],
    ["LABEL",                          "General",                "label",                              DataType.CONTROLLEDVOCABULARY,       "LABEL",  "", None, None, False],
    ["LOTNUMBER",                      "General",                "lotnumber",                          DataType.VARCHAR,                    None,  "", None, None, False],
    ["MODIFIED_BY",                    "General",                "modified by",                        DataType.CONTROLLEDVOCABULARY,       "ALL_LAB_MEMBERS",  "", None, None, False],
    ["MONO_POLYCLONAL",                "General",                "mono/polyclonal",                    DataType.CONTROLLEDVOCABULARY,       "MONO-POLYCLONAL",  "", None, None, False],
    ["ANTIBODY_FACS_NOTES",            "General",                "notes FACS",                         DataType.VARCHAR,                    None,  "", None, None, False],
    ["ANTIBODY_IF_NOTES",              "General",                "notes IFF",                          DataType.VARCHAR,                    None,  "", None, None, False],
    ["ANTIBODY_IP_NOTES",              "General",                "notes IP",                           DataType.VARCHAR,                    None,  "", None, None, False],
    ["ANTIBODY_WB_NOTES",              "General",                "notes western",                      DataType.VARCHAR,                    None,  "", None, None, False],
    ["SERIAL_NUMBER",                  "General",                "serial number",                      DataType.INTEGER,                    None,  "", None, None, False],
    ["ANTIGEN_SIZE",                   "General",                "size",                               DataType.VARCHAR,                    None,  "", None, None, False],
    ["SOURCE",                         "General",                "source",                             DataType.CONTROLLEDVOCABULARY,       "SOURCE",  "", None, None, False],
    ["CREATION_DATE",                  "General",                "creation date",                      DataType.TIMESTAMP,                  None,  "", None, None, False],
    ["MODIFICATION_DATE",              "General",                "modification date",                  DataType.TIMESTAMP,                  None,  "", None, None, False],
    ["FROZEN",                         "General",                "frozen",                             DataType.TIMESTAMP,                  None,  "", None, None, False],
    ["BOX",                            "Storage",                "box",                                DataType.VARCHAR,                    None,  "", None, None, False],
    ["RACK",                           "Storage",                "rack",                               DataType.VARCHAR,                    None,  "", None, None, False],
    ["PUBLISHED_IN",                   "Reference",              "reference",                          DataType.VARCHAR,                    None,  "", None, None, False],
    ["COMMENTS",                       "Comments",               "info",                               DataType.VARCHAR,                    None,  "", None, None, False]
];

cellDefinition = [
    ["CO2",                             "General",                "%CO2",                                DataType.CONTROLLEDVOCABULARY,       "CO2",  "", None, None, False],
    ["ATCC_NUM",                        "General",                "ATCC No.",                            DataType.VARCHAR,                    None,  "", None, None, False],
    ["BARCODE",                         "General",                "barcode",                             DataType.VARCHAR,                    None,  "", None, None, False],
    ["BARCODE_LABEL",                   "General",                "barcode label",                       DataType.VARCHAR,                    None,  "", None, None, False],
    ["CELL_ID_NR",                      "General",                "cell_id_nr",                          DataType.VARCHAR,                    None,  "", None, None, False],
    ["CELL_ID_NR_COPY",                 "General",                "cell_id_nr Copy",                     DataType.VARCHAR,                    None,  "", None, None, False],
    ["NAME",                            "General",                "cell ID",                             DataType.VARCHAR,                    None,  "", None, None, False],
    ["CELL_ID_COPY",                    "General",                "cell ID Copy",                        DataType.VARCHAR,                    None,  "", None, None, False],
    ["CLONE",                           "General",                "clone #",                             DataType.INTEGER,                    None,  "", None, None, False],
    ["COMPARE_FIELD",                   "General",                "compare field",                       DataType.VARCHAR,                    None,  "", None, None, False],
    ["CONC1",                           "General",                "conc.1",                              DataType.REAL,                       None,  "", None, None, False],
    ["CONC_UNIT1",                      "General",                "concentration unit 1",                DataType.CONTROLLEDVOCABULARY,       "CONC_UNITS",  "", None, None, False],
    ["CONC2",                           "General",                "conc.2",                              DataType.REAL,                       None,  "", None, None, False],
    ["CONC_UNIT2",                      "General",                "concentration unit 2",                DataType.CONTROLLEDVOCABULARY,       "CONC_UNITS",  "", None, None, False],
    ["CONC3",                           "General",                "conc.3",                              DataType.REAL,                       None,  "", None, None, False],
    ["CONC_UNIT3",                      "General",                "concentration unit 3",                DataType.CONTROLLEDVOCABULARY,       "CONC_UNITS",  "", None, None, False],
    ["CONC4",                           "General",                "conc.4",                              DataType.REAL,                       None,  "", None, None, False],
    ["CONC_UNIT4",                      "General",                "concentration unit 4",                DataType.CONTROLLEDVOCABULARY,       "CONC_UNITS",  "", None, None, False],
    ["CREATION_DATE",                   "General",                "creation date",                       DataType.TIMESTAMP,                  None,  "", None, None, False],
    ["DATE",                            "General",                "date",                                DataType.TIMESTAMP,                  None,  "", None, None, False],
    ["FROZEN",                          "General",                "frozen",                              DataType.TIMESTAMP,                  None,  "", None, None, False],
    ["GROWTH_TEMPERATURE",              "General",                "growth temperature",                  DataType.CONTROLLEDVOCABULARY,       "GROWTH_TEMPERATURE",  "", None, None, False],
    ["INVESTIGATOR",                    "General",                "investigator",                        DataType.CONTROLLEDVOCABULARY,       "ALL_LAB_MEMBERS",  "", None, None, False],
    ["LINE_NAME",                       "General",                "line name",                           DataType.VARCHAR,                    None,  "", None, None, False],
    ["MEDIUM",                          "General",                "medium",                              DataType.CONTROLLEDVOCABULARY,       "MEDIUM",  "", None, None, False],
    ["MEDIUM_SPECIALS",                 "General",                "medium specials",                     DataType.VARCHAR,                    None,  "", None, None, False],
    ["MODIFICATION_DATE",               "General",                "modification date",                   DataType.TIMESTAMP,                  None,  "", None, None, False],
    ["MODIFIED_BY",                     "General",                "modified by",                         DataType.CONTROLLEDVOCABULARY,       "ALL_LAB_MEMBERS",  "", None, None, False],
    ["MYCOPLASM_TESTED",                "General",                "mycoplasm_tested",                    DataType.BOOLEAN,                    None,  "", None, None, False],
    ["OBTAINED_FROM",                   "General",                "obtained from",                       DataType.CONTROLLEDVOCABULARY,       "CELL_OBTAINED_FROM",  "", None, None, False],
    ["PAGE_NUMBER",                     "General",                "page number",                         DataType.VARCHAR,                    None,  "", None, None, False],
    ["PARENTAL_CELL",                   "General",                "parental cell",                       DataType.VARCHAR,                    None,  "", None, None, False],
    ["PASSAGE",                         "General",                "passage",                             DataType.VARCHAR,                    None,  "", None, None, False],
    ["PERS_CELL_NUMBER",                "General",                "PersCellNumber",                      DataType.VARCHAR,                    None,  "", None, None, False],
    ["PLASMID_ID1",                     "General",                "plasmid ID 1",                        DataType.VARCHAR,                    None,  "", None, None, False],
    ["PLASMID_ID2",                     "General",                "plasmid ID 2",                        DataType.VARCHAR,                    None,  "", None, None, False],
    ["SELECTION_PLASMID1",              "General",                "sel.1",                               DataType.CONTROLLEDVOCABULARY,       "SELECTION",  "", None, None, False],
    ["SELECTION_PLASMID2",              "General",                "sel.2",                               DataType.CONTROLLEDVOCABULARY,       "SELECTION",  "", None, None, False],
    ["SELECTION_PLASMID3",              "General",                "sel.3",                               DataType.CONTROLLEDVOCABULARY,       "SELECTION",  "", None, None, False],    
    ["SELECTION_PLASMID4",              "General",                "sel.4",                               DataType.CONTROLLEDVOCABULARY,       "SELECTION",  "", None, None, False],
    ["SERIAL_NUMBER",                   "General",                "serial number",                       DataType.INTEGER,                    None,  "", None, None, False],
    ["SPECIES",                         "General",                "species",                             DataType.CONTROLLEDVOCABULARY,       "CELL_SPECIES",  "", None, None, False],
    ["VIRUS_ID1",                       "General",                "virus ID 1",                          DataType.VARCHAR,                    None,  "", None, None, False],
    ["VIRUS_ID2",                       "General",                "virus ID 2",                          DataType.VARCHAR,                    None,  "", None, None, False],
    ["PUBLISHED_IN",                    "Reference",              "reference",                           DataType.VARCHAR,                    None,  "", None, None, False],
    ["COMMENTS",                        "Comments",               "info",                                DataType.VARCHAR,                    None,  "", None, None, False]
];

strainDefinition = [
    ["CO2",                             "General",                  "%CO2",                             DataType.CONTROLLEDVOCABULARY,       "CO2",  "", None, None, False],
    ["BACKGROUND",                      "General",                  "background",                       DataType.CONTROLLEDVOCABULARY,       "STRAIN_BACKGROUND",  "", None, None, False],
    ["BARCODE",                         "General",                  "barcode",                          DataType.VARCHAR,                    None,  "", None, None, False],
    ["BARCODE_LABEL",                   "General",                  "barcode label",                    DataType.VARCHAR,                    None,  "", None, None, False],
    ["CIR",                             "General",                  "cir",                              DataType.CONTROLLEDVOCABULARY,       "CIR",  "", None, None, False],
    ["CONC_INDUCER1",                   "General",                  "conc. inducer 1",                  DataType.VARCHAR,                    None,  "", None, None, False],
    ["CONC_INDUCER2",                   "General",                  "conc. inducer 2",                  DataType.VARCHAR,                    None,  "", None, None, False],
    ["CONC_INDUCER3",                   "General",                  "conc. inducer 3",                  DataType.VARCHAR,                    None,  "", None, None, False],
    ["CONC1",                           "General",                  "conc.1",                           DataType.REAL,                       None,  "", None, None, False],
    ["CONC_UNIT1",                      "General",                  "concentration unit 1",             DataType.VARCHAR,                    None,  "", None, None, False],
    ["CONC2",                           "General",                  "conc.2",                           DataType.REAL,                       None,  "", None, None, False],
    ["CONC_UNIT2",                      "General",                  "concentration unit 2",             DataType.CONTROLLEDVOCABULARY,       "CONC_UNITS",  "", None, None, False],
    ["CONC3",                           "General",                  "conc.3",                           DataType.REAL,                       None,  "", None, None, False],
    ["CONC_UNIT3",                      "General",                  "concentration unit 3",             DataType.CONTROLLEDVOCABULARY,       "CONC_UNITS",  "", None, None, False],
    ["CREATION_DATE",                   "General",                  "creation date",                    DataType.TIMESTAMP,                  None,  "", None, None, False],
    ["DATE",                            "General",                  "date",                             DataType.VARCHAR,                    None,  "", None, None, False],
    ["DERIVATION",                      "General",                  "derivation",                       DataType.CONTROLLEDVOCABULARY,       "DERIVATION",  "", None, None, False],
    ["EXPRESSED_PROTEIN1",              "General",                  "expressed protein 1",              DataType.VARCHAR,                    None,  "", None, None, False],
    ["EXPRESSED_PROTEIN2",              "General",                  "expressed protein 2",              DataType.VARCHAR,                    None,  "", None, None, False],
    ["EXPRESSED_PROTEIN3",              "General",                  "expressed protein 3",              DataType.VARCHAR,                    None,  "", None, None, False],
    ["FREE_PLASMID",                    "General",                  "freeplasmid",                      DataType.VARCHAR,                    None,  "", None, None, False],
    ["FREEZEDATE",                      "General",                  "freezedate",                       DataType.VARCHAR,                    None,  "", None, None, False],
    ["FROZEN",                          "General",                  "frozen",                           DataType.TIMESTAMP,                  None,  "", None, None, False],
    ["GAL",                             "General",                  "gal",                              DataType.CONTROLLEDVOCABULARY,       "GAL",  "", None, None, False],
    ["GENE_EXPRESSION1",                "General",                  "gene expression 1",                DataType.VARCHAR,                    None,  "", None, None, False],
    ["GENE_EXPRESSION2",                "General",                  "gene expression 2",                DataType.VARCHAR,                    None,  "", None, None, False],
    ["GROWTH_TEMPERATURE",              "General",                  "growth temperature",               DataType.CONTROLLEDVOCABULARY,       "GROWTH_TEMPERATURE",  "", None, None, False],
    ["INDUCER1",                        "General",                  "inducer 1",                        DataType.VARCHAR,                    None,  "", None, None, False],
    ["INDUCER2",                        "General",                  "inducer 2",                        DataType.VARCHAR,                    None,  "", None, None, False],
    ["INDUCER3",                        "General",                  "inducer 3",                        DataType.VARCHAR,                    None,  "", None, None, False],
    ["COMMENTS",                        "General",                  "info",                             DataType.VARCHAR,                    None,  "", None, None, False],
    ["INVESTIGATOR",                    "General",                  "investigator",                     DataType.CONTROLLEDVOCABULARY,       "ALL_LAB_MEMBERS",  "", None, None, False],
    ["MARKERS",                         "General",                  "markers",                          DataType.VARCHAR,                    None,  "", None, None, False],
    ["MAT",                             "General",                  "mat",                              DataType.CONTROLLEDVOCABULARY,       "MAT",  "", None, None, False],
    ["MEDIUM",                          "General",                  "medium",                           DataType.CONTROLLEDVOCABULARY,       "MEDIUM",  "", None, None, False],
    ["MEDIUM_SPECIALS",                 "General",                  "medium specials",                  DataType.VARCHAR,                    None,  "", None, None, False],
    ["MODIFICATION_DATE",               "General",                  "modification date",                DataType.TIMESTAMP,                  None,  "", None, None, False],
    ["MODIFIED_BY",                     "General",                  "modified by",                      DataType.CONTROLLEDVOCABULARY,       "ALL_LAB_MEMBERS",  "", None, None, False],
    ["NUMBER",                          "General",                  "number",                           DataType.VARCHAR,                    None,  "", None, None, False],
    ["STRAIN_OBTAINED_FROM",            "General",                  "obtained from",                    DataType.VARCHAR,                    None,  "", None, None, False],
    ["ORF",                             "General",                  "orf",                              DataType.VARCHAR,                    None,  "", None, None, False],    
    ["PAGE_NUMBER",                     "General",                  "page number",                      DataType.VARCHAR,                    None,  "", None, None, False],
    ["PARENTAL_CELL",                   "General",                  "parental cell",                    DataType.VARCHAR,                    None,  "", None, None, False],
    ["PASSAGE",                         "General",                  "passage",                          DataType.VARCHAR,                    None,  "", None, None, False],
    ["PERSYEASTNUMBER",                 "General",                  "persyeastnumber",                  DataType.INTEGER,                    None,  "", None, None, False],
    ["PETERYEASTNUMBER",                "General",                  "peteryeastnumber",                 DataType.INTEGER,                    None,  "", None, None, False],
    ["PLASMID_ID1",                     "General",                  "plasmid ID 1",                     DataType.VARCHAR,                    None,  "", None, None, False],
    ["PLASMID_ID2",                     "General",                  "plasmid ID 2",                     DataType.VARCHAR,                    None,  "", None, None, False],
    ["PLASMID_ID3",                     "General",                  "plasmid ID 3",                     DataType.VARCHAR,                    None,  "", None, None, False],
    ["PROMOTER1",                       "General",                  "promoter 1",                       DataType.VARCHAR,                    None,  "", None, None, False],
    ["PROMOTER2",                       "General",                  "promoter 2",                       DataType.VARCHAR,                    None,  "", None, None, False],
    ["PROMOTER3",                       "General",                  "promoter 3",                       DataType.VARCHAR,                    None,  "", None, None, False],
    ["PUBLISHED_IN",                    "General",                  "reference",                        DataType.VARCHAR,                    None,  "", None, None, False],
    ["SELECTION1",                      "General",                  "sel.1",                            DataType.VARCHAR,                    None,  "", None, None, False],
    ["SELECTION2",                      "General",                  "sel.2",                            DataType.VARCHAR,                    None,  "", None, None, False],
    ["SELECTION3",                      "General",                  "sel.3",                            DataType.VARCHAR,                    None,  "", None, None, False],
    ["SELECTION",                       "General",                  "selection",                        DataType.CONTROLLEDVOCABULARY,       "SELECTION_YEAST",  "", None, None, False],
    ["SERIAL_NUMBER",                   "General",                  "serial number",                    DataType.INTEGER,                    None,  "", None, None, False],
    ["STRAIN_SOURCE",                   "General",                  "source",                           DataType.CONTROLLEDVOCABULARY,       "STRAIN_SOURCE",  "", None, None, False],
    ["SPECIES",                         "General",                  "species",                          DataType.VARCHAR,                    None,  "", None, None, False],
    ["STRAIN_ID_NR",                    "General",                  "strain_id_nr",                     DataType.VARCHAR,                    None,  "", None, None, False],
    ["NAME",                            "General",                  "strain ID",                        DataType.VARCHAR,                    None,  "", None, None, False],
    ["STRAIN_NAME",                     "General",                  "strain name",                      DataType.VARCHAR,                    None,  "", None, None, False]
];

oligoDefinition = [
    ["AMOUNT",                          "General",                  "amount",                           DataType.REAL,                      None, "", None, None, False],
    ["BARCODE",                         "General",                  "barcode",                          DataType.VARCHAR,                   None, "", None, None, False],
    ["BARCODE_LABEL",                   "General",                  "barcode label",                    DataType.VARCHAR,                   None, "", None, None, False],
    ["OLIGO_COMPANY",                   "General",                  "company",                          DataType.CONTROLLEDVOCABULARY,      "OLIGO_COMPANY", "", None, None, False],
    ["CONCENTRATION",                   "General",                  "concentration",                    DataType.REAL,                      None, "", None, None, False],
    ["CONCENTRATION_UNIT",              "General",                  "concentration unit",               DataType.CONTROLLEDVOCABULARY,      "CONC_UNITS", "", None, None, False],
    ["CREATION_DATE",                   "General",                  "creation date",                    DataType.TIMESTAMP,                 None, "", None, None, False],
    ["DATE",                            "General",                  "date",                             DataType.VARCHAR,                   None, "", None, None, False],
    ["FROZEN",                          "General",                  "frozen",                           DataType.TIMESTAMP,                 None, "", None, None, False],
    ["GENE_LOCUS",                      "General",                  "gene locus",                       DataType.VARCHAR,                   None, "", None, None, False],
    ["GRADE",                           "General",                  "grade",                            DataType.CONTROLLEDVOCABULARY,      "OLIGO_GRADE", "", None, None, False],
    ["COMMENTS",                        "General",                  "Info",                             DataType.VARCHAR,                   None, "", None, None, False],
    ["INVESTIGATOR",                    "General",                  "investigator",                     DataType.CONTROLLEDVOCABULARY,      "ALL_LAB_MEMBERS", "", None, None, False],
    ["MODIFICATION_DATE",               "General",                  "modification date",                DataType.TIMESTAMP,                 None, "", None, None, False],
    ["MODIFIED_BY",                     "General",                  "modified by",                      DataType.CONTROLLEDVOCABULARY,      "ALL_LAB_MEMBERS", "", None, None, False],
    ["OD",                              "General",                  "od",                               DataType.INTEGER,                   None, "", None, None, False],
    ["NAME",                            "General",                  "oligo ID",                         DataType.INTEGER,                   None, "", None, None, False],
    ["OLIGO_ID_NR",                     "General",                  "oligo_id_nr",                      DataType.VARCHAR,                   None, "", None, None, False],
    ["ORDER_DATE",                      "General",                  "order date",                       DataType.VARCHAR,                   None, "", None, None, False],
    ["OLIGO_ORGANISM",                  "General",                  "organism",                         DataType.CONTROLLEDVOCABULARY,      "OLIGO_ORGANISM", "", None, None, False],
    ["PAGE_NUMBER",                     "General",                  "page number",                      DataType.VARCHAR,                   None, "", None, None, False],
    ["PRIMER_PARTNER",                  "General",                  "primer partner",                   DataType.VARCHAR,                   None, "", None, None, False],
    ["PRIMER_POSITION",                 "General",                  "primer position",                  DataType.CONTROLLEDVOCABULARY,      "PRIMER_POSITION", "", None, None, False],
    ["PUBLISHED_IN",                    "General",                  "PublishedIn",                      DataType.VARCHAR,                   None, "", None, None, False],
    ["RESTRICTION_SITE",                "General",                  "restriction site",                 DataType.CONTROLLEDVOCABULARY,      "RESTRICTION", "", None, None, False],
    ["SEQUENCE",                        "General",                  "sequence",                         DataType.VARCHAR,                   None, "", None, None, False],
    ["SERIAL_NUMBER",                   "General",                  "serial number",                    DataType.VARCHAR,                   None, "", None, None, False],
    ["USAGE",                           "General",                  "usage",                            DataType.CONTROLLEDVOCABULARY,      "OLIGO_USAGE", "", None, None, False],
    ["ADENOSINE",                       "Calculated fields",        "adenosine",                        DataType.INTEGER,                   None, "", adenosineScriptName, None, False],
    ["CYTOSINE",                        "Calculated fields",        "cytosine",                         DataType.INTEGER,                   None, "", cytosineScriptName, None, False],
    ["GUANOSINE",                       "Calculated fields",        "guanosine",                        DataType.INTEGER,                   None, "", guanosineScriptName, None, False],
    ["THYMIDINE",                       "Calculated fields",        "thymidine",                        DataType.INTEGER,                   None, "", thymidineScriptName, None, False],
    ["LENGTH",                          "Calculated fields",        "length",                           DataType.INTEGER,                   None, "", lengthScriptName, None, False],
    ["GC",                              "Calculated fields",        "gc",                               DataType.REAL,                      None, "", gcScriptName, None, False],
    ["TM",                              "Calculated fields",        "tm",                               DataType.REAL,                      None, "", tmScriptName, None, False],
    ["O_NUCLEOTIDE",                    "Calculated fields",        "O nucleotide",                     DataType.INTEGER,                   None, "", nucelotideScriptName, None, False]
];

plasmidDefinition = [
    ["BACKBONE",                        "General",                  "backbone",                         DataType.CONTROLLEDVOCABULARY,      "BACKBONE",  "", None, None, False],
    ["BACKUP_STORAGE",                  "General",                  "backup storage",                   DataType.VARCHAR,                   None,  "", None, None, False],
    ["BACTERIAL_HOST",                  "General",                  "bacterial host",                   DataType.CONTROLLEDVOCABULARY,      "BACTERIAL_HOST",  "", None, None, False],
    ["BARCODE",                         "General",                  "barcode",                          DataType.VARCHAR,                   None,  "", None, None, False],
    ["BARCODE_LABEL",                   "General",                  "barcode label",                    DataType.VARCHAR,                   None,  "", None, None, False],
    ["BIOSAFETY_LEVEL",                 "General",                  "biosafety level",                  DataType.CONTROLLEDVOCABULARY,      "BIOSAFETY_LEVEL",  "", None, None, False],
    ["BOXNR",                           "General",                  "boxNr",                            DataType.VARCHAR,                   None,  "", None, None, False],
    ["CONCENTRATION_UNIT",              "General",                  "concentration unit",               DataType.CONTROLLEDVOCABULARY,      "CONC_UNITS",  "", None, None, False],
    ["CONSTRUCTION_METHOD",             "General",                  "construction method",              DataType.VARCHAR,                   None,  "", None, None, False],
    ["CREATION_DATE",                   "General",                  "creation date",                    DataType.TIMESTAMP,                 None,  "", None, None, False],
    ["CTERMTAG",                        "General",                  "Ctermtag",                         DataType.VARCHAR,                   None,  "", None, None, False],
    ["DATE",                            "General",                  "date",                             DataType.VARCHAR,                   None,  "", None, None, False],
    ["DATE_OF_CONSTRUCTION",            "General",                  "date of construction",             DataType.TIMESTAMP,                 None,  "", None, None, False],
    ["DERIVATIVE_OF",                   "General",                  "Derivative of",                    DataType.VARCHAR,                   None,  "", None, None, False],
    ["ECOLI_STRAIN",                    "General",                  "E.coli strain",                    DataType.VARCHAR,                   None,  "", None, None, False],
    ["EXCISE_INSERT",                   "General",                  "excise insert",                    DataType.VARCHAR,                   None,  "", None, None, False],
    ["EXCISE_INSERT2",                  "General",                  "excise insert 2",                  DataType.VARCHAR,                   None,  "", None, None, False],
    ["FREEZEDATE",                      "General",                  "freezedate",                       DataType.VARCHAR,                   None,  "", None, None, False],
    ["FROZEN",                          "General",                  "frozen",                           DataType.TIMESTAMP,                 None,  "", None, None, False],
    ["FUNCTIONAL_ELEMENTS_INSERT",      "General",                  "functional elements insert",       DataType.VARCHAR,                   None,  "", None, None, False],
    ["HYPERCARD_FILE",                  "General",                  "hypercard file",                   DataType.VARCHAR,                   None,  "", None, None, False],
    ["INFO",                            "General",                  "info",                             DataType.VARCHAR,                   None,  "", None, None, False],
    ["INSERT",                          "General",                  "insert",                           DataType.VARCHAR,                   None,  "", None, None, False],
    ["INSERTION_SITE",                  "General",                  "insertion site",                   DataType.VARCHAR,                   None,  "", None, None, False],
    ["INVESTIGATOR",                    "General",                  "investigator",                     DataType.CONTROLLEDVOCABULARY,      "ALL_LAB_MEMBERS",  "", None, None, False],
    ["LINEARIZE",                       "General",                  "linearize",                        DataType.VARCHAR,                   None,  "", None, None, False],
    ["MARKER",                          "General",                  "Marker",                           DataType.VARCHAR,                   None,  "", None, None, False],
    ["MODIFICATION_DATE",               "General",                  "modification date",                DataType.VARCHAR,                   None,  "", None, None, False],
    ["MODIFIED_BY",                     "General",                  "modified by",                      DataType.CONTROLLEDVOCABULARY,      "ALL_LAB_MEMBERS",  "", None, None, False],
    ["NAME_OF_PARENTAL_CLONE",          "General",                  "name of parental clone",           DataType.VARCHAR,                   None,  "", None, None, False],
    ["NAME_OF_VECTOR",                  "General",                  "name of vector",                   DataType.VARCHAR,                   None,  "", None, None, False],
    ["NTERMTAG",                        "General",                  "Ntermtag",                         DataType.VARCHAR,                   None,  "", None, None, False],
    ["NUCLEOTIDE_SEQUENCE",             "General",                  "nucleotide sequence",              DataType.VARCHAR,                   None,  "", None, None, False],
    ["OLIGOS_USED",                     "General",                  "oligos used",                      DataType.VARCHAR,                   None,  "", None, None, False],
    ["ORGANISM",                        "General",                  "organism",                         DataType.VARCHAR,                   None,  "", None, None, False],
    ["ORIGIN_OF_REPLICATION",           "General",                  "origin of replication",            DataType.VARCHAR,                   None,  "", None, None, False],
    ["OTHER_MARKER",                    "General",                  "other marker",                     DataType.VARCHAR,                   None,  "", None, None, False],
    ["PAGE_NUMBER",                     "General",                  "page number",                      DataType.VARCHAR,                   None,  "", None, None, False],
    ["PERSPLASMIDNUM",                  "General",                  "persPlasmidnum",                   DataType.REAL,                      None,  "", None, None, False],
    ["PETERPLASMIDNUM",                 "General",                  "PeterPlasmidNum",                  DataType.INTEGER,                   None,  "", None, None, False],
    ["NAME",                            "General",                  "plasmid ID",                       DataType.VARCHAR,                   None,  "", None, None, False],
    ["PLASMID_ID_NR",                   "General",                  "plasmid_id_nr",                    DataType.VARCHAR,                   None,  "", None, None, False],
    ["PLASMIDNAME",                     "General",                  "plasmidname",                      DataType.VARCHAR,                   None,  "", None, None, False],
    ["PRIMERS",                         "General",                  "primers",                          DataType.VARCHAR,                   None,  "", None, None, False],
    ["PROMOTER",                        "General",                  "promoter",                         DataType.CONTROLLEDVOCABULARY,      "PROMOTER",  "", None, None, False],
    ["SELECTION_IN_BACTERIA",           "General",                  "selection in bacteria",            DataType.CONTROLLEDVOCABULARY,      "SELECTION_IN_BACTERIA",  "", None, None, False],
    ["SELECTION_IN_EUKARYOTES",         "General",                  "selection in eukaryotes",          DataType.CONTROLLEDVOCABULARY,      "SELECTION_IN_EUKARYOTES",  "", None, None, False],
    ["SEQUENCE_FILE_NAME",              "General",                  "sequence file name",               DataType.VARCHAR,                   None,  "", None, None, False],
    ["SEQUENCING_DATE",                 "General",                  "sequencing date",                  DataType.TIMESTAMP,                 None,  "", None, None, False],
    ["SERIAL_NUMBER",                   "General",                  "serial number",                    DataType.INTEGER,                   None,  "", None, None, False],
    ["SHELF",                           "General",                  "shelf",                            DataType.VARCHAR,                   None,  "", None, None, False],
    ["SIZE_INSERT",                     "General",                  "size insert",                      DataType.REAL,                      None,  "", None, None, False],
    ["SIZE_OF_INSERT",                  "General",                  "size of insert",                   DataType.VARCHAR,                   None,  "", None, None, False],
    ["SIZE_VECTOR",                     "General",                  "size vector",                      DataType.REAL,                      None,  "", None, None, False],
    ["PLASMID_SOURCE",                  "General",                  "source",                           DataType.VARCHAR,                   None,  "", None, None, False],
    ["PLASMID_SPECIES",                 "General",                  "species",                          DataType.CONTROLLEDVOCABULARY,      "PLASMID_ORGANISM",  "", None, None, False],
    #["TUBES",                           "General",                 "TUBES",                            DataType.VARCHAR,                   None,  "", None, None, False],
    ["URL",                             "Reference",                "url",                              DataType.VARCHAR,                   None,  "", None, None, False],
    ["PUBLISHED_IN",                    "Reference",                "published in",                     DataType.VARCHAR,                   None,  "", None, None, False]
];

chemicalDefinition = [
    ["ARTICLE",                         "General",                  "Article",                          DataType.VARCHAR,                   None,  "", None, None, False],
    ["COMPANY",                         "General",                  "Company",                          DataType.CONTROLLEDVOCABULARY,      "COMPANY",  "", None, None, False],
    ["ARTICLE_NUM",                     "General",                  "Article Number",                   DataType.VARCHAR,                   None,  "", None, None, False],
    ["RECORD_NUMBER",                   "General",                  "RecordNumber",                     DataType.VARCHAR,                   None,  "", None, None, False],
    ["LOCATION",                        "Location",                  "Location",                         DataType.VARCHAR,                   None,  "", None, None, False]   
];

siRNADefinition = [
    ["INVESTIGATOR",                    "General",                  "investigator",                     DataType.CONTROLLEDVOCABULARY,      "ALL_LAB_MEMBERS",  "", None, None, False],
    ["GENE_LOCUS",                      "General",                  "gene locus",                       DataType.VARCHAR,                   None,  "", None, None, False],
    ["EXON_OR_THREE_PRIME",             "General",                  "exon or three prime",              DataType.CONTROLLEDVOCABULARY,      "EXON_OR_THREE_PRIME",  "", None, None, False],
    ["EXON",                            "General",                  "exon #",                           DataType.INTEGER,                   None,  "", None, None, False],
    ["3_PRIME_POSITION",                "General",                  "3' position",                      DataType.VARCHAR,                   None,  "", None, None, False],
    ["SIRNA_SPECIES",                   "General",                  "species",                          DataType.CONTROLLEDVOCABULARY,      "SIRNA_SPECIES",  "", None, None, False],
    ["SEQUENCE",                        "General",                  "SEQUENCE",                         DataType.VARCHAR,                   None,  "", None, None, False],
    ["SIRNA_COMPANY",                   "General",                  "Company",                          DataType.CONTROLLEDVOCABULARY,      "SIRNA_COMPANY",  "", None, None, False],
    ["STOCK_CONCENTRATION",             "General",                  "Stock concentration",              DataType.REAL,                      None,  "", None, None, False],
    ["WORKING_CONCENTRATION",           "General",                  "Working concentration",            DataType.REAL,                      None,  "", None, None, False],
    ["EFFICIENCY",                      "General",                  "Efficiency",                       DataType.VARCHAR,                   None,  "", None, None, False],
    ["EFFICIENCY_QPCR",                 "General",                  "Efficiency_qPCR",                  DataType.VARCHAR,                   None,  "", None, None, False],
    ["EFFICIENCY_WESTERN",              "General",                  "Efficiency_Western",               DataType.VARCHAR,                   None,  "", None, None, False],
    ["EFFICIENCY_OTHER",                "General",                  "Efficiency_other",                 DataType.VARCHAR,                   None,  "", None, None, False],
    ["OFF_TARGET_EFFECTS",              "General",                  "Off target effects",               DataType.VARCHAR,                   None,  "", None, None, False],
    ["INFO",                            "General",                  "Info",                             DataType.VARCHAR,                   None,  "", None, None, False],
    ["SPECIFIC_SPLICE_VARIANT",         "General",                  "Specific splice variant",          DataType.VARCHAR,                   None,  "", None, None, False],
    ["TRANSFECTION_AGENT",              "General",                  "Transfection agent",               DataType.CONTROLLEDVOCABULARY,      "TRANSFECTION_AGENT",  "", None, None, False],
    ["LIBRARY",                         "General",                  "Library",                          DataType.VARCHAR,                   None,  "", None, None, False],
    ["CHARACTERIZED_BY_COMPANY",        "General",                  "Characterized by company",         DataType.VARCHAR,                   None,  "", None, None, False],
    ["NUMBERINPOOL",                    "General",                  "NumberINpool",                     DataType.INTEGER,                   None,  "", None, None, False],
    ["TEST",                            "General",                  "Test",                             DataType.VARCHAR,                   None,  "", None, None, False],
    ["CHEMICAL_MODIFICATION",           "General",                  "Chemical modification",            DataType.VARCHAR,                   None,  "", None, None, False],
    ["OLIGO_NAME",                      "General",                  "Oligo name",                       DataType.VARCHAR,                   None,  "", None, None, False],
    ["SIRNA_OLIGONUMBER",               "General",                  "siRNA_OligoNumber",                DataType.VARCHAR,                   None,  "", None, None, False],
    ["SIRNA_FREEZER_LOCATION",          "General",                  "Freezer Location",                 DataType.CONTROLLEDVOCABULARY,      "SIRNA_FREEZER_LOCATION",  "", None, None, False],
    ["DRAWER",                          "General",                  "Drawer",                           DataType.VARCHAR,                   None,  "", None, None, False],
    ["PUBLISHED",                       "Reference",                "Published",                        DataType.VARCHAR,                   None,  "", None, None, False]
];

#
# Data Set Types
#
documentDefinition = [
    ["CREATION_DATE",                   "General",                  "date created",                     DataType.TIMESTAMP,                 None,  "", None, None, False],
    ["MODIFICATION_DATE",               "General",                  "date modified",                    DataType.TIMESTAMP,                 None,  "", None, None, False],
    ["INFO",                            "General",                  "info",                             DataType.VARCHAR,                   None,  "", None, None, False],
    ["ID_NR",                           "General",                  "id_nr",                            DataType.VARCHAR,                   None,  "", None, None, False],
    ["FILE",                            "General",                  "file",                             DataType.VARCHAR,                   None,  "", None, None, False],
    ["SERIAL",                          "General",                  "serial",                           DataType.INTEGER,                   None,  "", None, None, False]
 
];
