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

#
# Storage 
#
numberOfStorageGroups = 15
stogageGroupPropertyCodes = ["STORAGE_NAME", "STORAGE_ROW", "STORAGE_COLUMN", "STORAGE_BOX_NAME", "STORAGE_USER", "STORAGE_BOX_POSITION"] #This is duplicated below, needs cleanup
storageGroupDefinition = [
    ["STORAGE_NAME_0",         "Physical Storage 0",        "location",         DataType.CONTROLLEDVOCABULARY,      "FREEZER",            "Storage Name 0",       None, None],
    ["STORAGE_ROW_0",          "Physical Storage 0",        "Storage Row",      DataType.INTEGER,                    None,                "Storage Row 0",        None, None],
    ["STORAGE_COLUMN_0",       "Physical Storage 0",        "Storage Column",   DataType.INTEGER,                    None,                "Storage Column 0",     None, None],
    ["STORAGE_BOX_NAME_0",     "Physical Storage 0",        "box label",        DataType.VARCHAR,                    None,                "Storage Box Name 0",   None, None],
    ["STORAGE_USER_0",         "Physical Storage 0",        "frozen by",        DataType.CONTROLLEDVOCABULARY,      "ALL_LAB_MEMBERS",    "Storage User Id 0",    None, None],
    ["STORAGE_BOX_POSITION_0", "Physical Storage 0",        "position",         DataType.VARCHAR,                    None,                "Storage Box Position 0",    None, None]
];

#
# Experiment Types
#
experimentDefinition = [
    ["NAME",                   "General", "Name",                     DataType.VARCHAR,             None,    "Name", None, None],
    ["EXPERIMENTAL_GOALS",     "General", "Experimental goals",       DataType.MULTILINE_VARCHAR,   None,    "Goal of the experiment", None, None],
    ["GRANT",                  "General", "Grant",                    DataType.VARCHAR,             None,    "grant name", None, None],
    ["START_DATE",             "General", "Start Date",               DataType.TIMESTAMP,           None,    "Start Date", None, None],
    ["END_DATE",               "General", "End Date",                 DataType.TIMESTAMP,           None,    "End Date", None, None],
    ["EXPERIMENTAL_RESULTS",   "General", "Experimental results",     DataType.MULTILINE_VARCHAR,   None,    "Brief summary of the results obtained", None, None],
    ["XMLCOMMENTS",            "Comments","Comments List",            DataType.XML,                 None,    "Several comments can be added by different users", commentsScriptName, None]
];

#
# Sample Types
#
antibodyDefinition = [
    ["NAME",                           "General",                "antibody ID",                        DataType.VARCHAR,                    None,  "", None, None],
    ["ANTIBODY_ID_NR",                 "General",                "antibody_id_nr",                     DataType.VARCHAR,                    None,  "", None, None],
    ["ANTIGEN",                        "General",                "antigen",                            DataType.VARCHAR,                    None,  "", None, None],
    ["BARCODE",                        "General",                "barcode",                            DataType.VARCHAR,                    None,  "", None, None],
    ["BARCODE_LABEL",                  "General",                "barcode label",                      DataType.VARCHAR,                    None,  "", None, None],
    ["ANTIBODY_FACS_BLOCK",            "General",                "block facs",                         DataType.CONTROLLEDVOCABULARY,       "BLOCK",  "", None, None],
    ["ANTIBODY_IF_BLOCK",              "General",                "block IFF",                          DataType.CONTROLLEDVOCABULARY,       "BLOCK",  "", None, None],
    ["ANTIBODY_WB_BLOCK",              "General",                "block western",                      DataType.CONTROLLEDVOCABULARY,       "BLOCK",  "", None, None],
    ["BOX",                            "General",                "box",                                DataType.VARCHAR,                    None,  "", None, None],
    ["CATALOGUE_NUMBER",               "General",                "catalog id #",                       DataType.VARCHAR,                    None,  "", None, None],
    ["CLASS",                          "General",                "class",                              DataType.CONTROLLEDVOCABULARY,       "CLASS",  "", None, None],
    ["CLONE",                          "General",                "clone",                              DataType.VARCHAR,                    None,  "", None, None],
    ["COMPANY",                        "General",                "company",                            DataType.CONTROLLEDVOCABULARY,       "COMPANY",  "", None, None],
    ["ANTIBODY_FACS_CONC",             "General",                "conc facs",                          DataType.CONTROLLEDVOCABULARY,       "CONCENTRATION_FACS",  "", None, None],
    ["ANTIBODY_IF_CONC",               "General",                "conc IFF",                           DataType.CONTROLLEDVOCABULARY,       "CONCENTRATION_FACS",  "", None, None],
    ["ANTIBODY_IP_CONC",               "General",                "conc ip",                            DataType.CONTROLLEDVOCABULARY,       "CONCENTRATION_IP",  "", None, None],
    ["ANTIBODY_WB_CONC",               "General",                "conc western",                       DataType.CONTROLLEDVOCABULARY,       "CONCENTRATION_WESTERN",  "", None, None],
    ["ANTIBODY_CONCENTRATION",         "General",                "concentration",                      DataType.VARCHAR,                    None,  "", None, None],
    ["ANTIBODY_CROSSREACTIVITY",       "General",                "crossreactivity",                    DataType.VARCHAR,                    None,  "", None, None],
    ["ANTIBODY_FACS_FIX",              "General",                "fix facs",                           DataType.CONTROLLEDVOCABULARY,       "FIX",  "", None, None],
    ["ANTIBODY_IF_FIX",                "General",                "fix IFF",                            DataType.CONTROLLEDVOCABULARY,       "FIX",  "", None, None],
    ["ANTIBODY_WB_FIX",                "General",                "fix western",                        DataType.CONTROLLEDVOCABULARY,       "FIX",  "", None, None],
    ["COMMENTS",                       "Comments",               "info",                               DataType.VARCHAR,                    None,  "", None, None],
    ["INVESTIGATOR",                   "General",                "investigator",                       DataType.CONTROLLEDVOCABULARY,       "ALL_LAB_MEMBERS",  "", None, None],
    ["LABEL",                          "General",                "label",                              DataType.CONTROLLEDVOCABULARY,       "LABEL",  "", None, None],
    ["LOTNUMBER",                      "General",                "lotnumber",                          DataType.VARCHAR,                    None,  "", None, None],
    ["MODIFIED_BY",                    "General",                "modified by",                        DataType.CONTROLLEDVOCABULARY,       "ALL_LAB_MEMBERS",  "", None, None],
    ["MONO_POLYCLONAL",                "General",                "mono/polyclonal",                    DataType.CONTROLLEDVOCABULARY,       "MONO-POLYCLONAL",  "", None, None],
    ["ANTIBODY_FACS_NOTES",            "General",                "notes FACS",                         DataType.VARCHAR,                    None,  "", None, None],
    ["ANTIBODY_IF_NOTES",              "General",                "notes IFF",                          DataType.VARCHAR,                    None,  "", None, None],
    ["ANTIBODY_IP_NOTES",              "General",                "notes IP",                           DataType.VARCHAR,                    None,  "", None, None],
    ["ANTIBODY_WB_NOTES",              "General",                "notes western",                      DataType.VARCHAR,                    None,  "", None, None],
    ["RACK",                           "General",                "rack",                               DataType.VARCHAR,                    None,  "", None, None],
    ["PUBLISHED_IN",                   "Reference",              "reference",                          DataType.VARCHAR,                    None,  "", None, None],
    ["SERIAL_NUMBER",                  "General",                "serial number",                      DataType.INTEGER,                    None,  "", None, None],
    ["ANTIGEN_SIZE",                   "General",                "size",                               DataType.VARCHAR,                    None,  "", None, None],
    ["SOURCE",                         "General",                "source",                             DataType.CONTROLLEDVOCABULARY,       "SOURCE",  "", None, None],
    ["CREATION_DATE",                  "General",                "creation date",                      DataType.TIMESTAMP,                  None,  "", None, None],
    ["MODIFICATION_DATE",              "General",                "modification date",                  DataType.TIMESTAMP,                  None,  "", None, None],
    ["FROZEN",                         "General",                "frozen",                             DataType.TIMESTAMP,                  None,  "", None, None]
];


cellDefinition = [
    ["CO2",                             "General",                "%CO2",                                DataType.CONTROLLEDVOCABULARY,       "CO2",  "", None, None],
    ["ATCC_NUM",                        "General",                "ATCC No.",                            DataType.VARCHAR,                    None,  "", None, None],
    ["BARCODE",                         "General",                "barcode",                             DataType.VARCHAR,                    None,  "", None, None],
    ["BARCODE_LABEL",                   "General",                "barcode label",                       DataType.VARCHAR,                    None,  "", None, None],
    ["CELL_ID_NR",                      "General",                "cell_id_nr",                          DataType.VARCHAR,                    None,  "", None, None],
    ["CELL_ID_NR_COPY",                 "General",                "cell_id_nr Copy",                     DataType.VARCHAR,                    None,  "", None, None],
    ["NAME",                            "General",                "cell ID",                             DataType.VARCHAR,                    None,  "", None, None],
    ["CELL_ID_COPY",                    "General",                "cell ID Copy",                        DataType.VARCHAR,                    None,  "", None, None],
    ["CLONE",                           "General",                "clone #",                             DataType.INTEGER,                    None,  "", None, None],
    ["COMPARE_FIELD",                   "General",                "compare field",                       DataType.VARCHAR,                    None,  "", None, None],
    ["CONC1",                           "General",                "conc.1",                              DataType.REAL,                       None,  "", None, None],
    ["CONC_UNIT1",                      "General",                "concentration unit 1",                DataType.CONTROLLEDVOCABULARY,       "CONC_UNITS",  "", None, None],
    ["CONC2",                           "General",                "conc.2",                              DataType.REAL,                       None,  "", None, None],
    ["CONC_UNIT2",                      "General",                "concentration unit 2",                DataType.CONTROLLEDVOCABULARY,       "CONC_UNITS",  "", None, None],
    ["CONC3",                           "General",                "conc.3",                              DataType.REAL,                       None,  "", None, None],
    ["CONC_UNIT3",                      "General",                "concentration unit 3",                DataType.CONTROLLEDVOCABULARY,       "CONC_UNITS",  "", None, None],
    ["CONC4",                           "General",                "conc.4",                              DataType.REAL,                       None,  "", None, None],
    ["CONC_UNIT4",                      "General",                "concentration unit 4",                DataType.CONTROLLEDVOCABULARY,       "CONC_UNITS",  "", None, None],
    ["CREATION_DATE",                   "General",                "creation date",                       DataType.TIMESTAMP,                  None,  "", None, None],
    ["DATE",                            "General",                "date",                                DataType.TIMESTAMP,                  None,  "", None, None],
    ["FROZEN",                          "General",                "frozen",                              DataType.TIMESTAMP,                  None,  "", None, None],
    ["GROWTH_TEMPERATURE",              "General",                "growth temperature",                  DataType.CONTROLLEDVOCABULARY,       "GROWTH_TEMPERATURE",  "", None, None],
    ["INVESTIGATOR",                    "General",                "investigator",                        DataType.CONTROLLEDVOCABULARY,       "ALL_LAB_MEMBERS",  "", None, None],
    ["COMMENTS",                        "Comments",               "info",                                DataType.VARCHAR,                    None,  "", None, None],
    ["LINE_NAME",                       "General",                "line name",                           DataType.VARCHAR,                    None,  "", None, None],
    ["MEDIUM",                          "General",                "medium",                              DataType.CONTROLLEDVOCABULARY,       "MEDIUM",  "", None, None],
    ["MEDIUM_SPECIALS",                 "General",                "medium specials",                     DataType.VARCHAR,                    None,  "", None, None],
    ["MODIFICATION_DATE",               "General",                "modification date",                   DataType.TIMESTAMP,                  None,  "", None, None],
    ["MODIFIED_BY",                     "General",                "modified by",                         DataType.CONTROLLEDVOCABULARY,       "ALL_LAB_MEMBERS",  "", None, None],
    ["MYCOPLASM_TESTED",                "General",                "mycoplasm_tested",                    DataType.BOOLEAN,                    None,  "", None, None],
    ["OBTAINED_FROM",                   "General",                "obtained from",                       DataType.CONTROLLEDVOCABULARY,       "CELL_OBTAINED_FROM",  "", None, None],
    ["PAGE_NUMBER",                     "General",                "page number",                         DataType.VARCHAR,                    None,  "", None, None],
    ["PARENTAL_CELL",                   "General",                "parental cell",                       DataType.VARCHAR,                    None,  "", None, None],
    ["PASSAGE",                         "General",                "passage",                             DataType.VARCHAR,                    None,  "", None, None],
    ["PERS_CELL_NUMBER",                "General",                "PersCellNumber",                      DataType.VARCHAR,                    None,  "", None, None],
    ["PLASMID_ID1",                     "General",                "plasmid ID 1",                        DataType.VARCHAR,                    None,  "", None, None],
    ["PLASMID_ID2",                     "General",                "plasmid ID 2",                        DataType.VARCHAR,                    None,  "", None, None],
    ["PUBLISHED_IN",                    "Reference",              "reference",                           DataType.VARCHAR,                    None,  "", None, None],
    ["SELECTION_PLASMID1",              "General",                "sel.1",                               DataType.CONTROLLEDVOCABULARY,       "SELECTION",  "", None, None],
    ["SELECTION_PLASMID2",              "General",                "sel.2",                               DataType.CONTROLLEDVOCABULARY,       "SELECTION",  "", None, None],
    ["SELECTION_PLASMID3",              "General",                "sel.3",                               DataType.CONTROLLEDVOCABULARY,       "SELECTION",  "", None, None],    
    ["SELECTION_PLASMID4",              "General",                "sel.4",                               DataType.CONTROLLEDVOCABULARY,       "SELECTION",  "", None, None],
    ["SERIAL_NUMBER",                   "General",                "serial number",                       DataType.INTEGER,                    None,  "", None, None],
    ["SPECIES",                         "General",                "species",                             DataType.CONTROLLEDVOCABULARY,       "CELL_SPECIES",  "", None, None],
    ["VIRUS_ID1",                       "General",                "virus ID 1",                          DataType.VARCHAR,                    None,  "", None, None],
    ["VIRUS_ID2",                       "General",                "virus ID 2",                          DataType.VARCHAR,                    None,  "", None, None]
];

strainDefinition = [
    ["CO2",                             "General",                  "%CO2",                             DataType.CONTROLLEDVOCABULARY,       "CO2",  "", None, None],
    ["BACKGROUND",                      "General",                  "background",                       DataType.CONTROLLEDVOCABULARY,       "STRAIN_BACKGROUND",  "", None, None],
    ["BARCODE",                         "General",                  "barcode",                          DataType.VARCHAR,                    None,  "", None, None],
    ["BARCODE_LABEL",                   "General",                  "barcode label",                    DataType.VARCHAR,                    None,  "", None, None],
    ["CIR",                             "General",                  "cir",                              DataType.CONTROLLEDVOCABULARY,       "CIR",  "", None, None],
    ["CONC_INDUCER1",                   "General",                  "conc. inducer 1",                  DataType.VARCHAR,                    None,  "", None, None],
    ["CONC_INDUCER2",                   "General",                  "conc. inducer 2",                  DataType.VARCHAR,                    None,  "", None, None],
    ["CONC_INDUCER3",                   "General",                  "conc. inducer 3",                  DataType.VARCHAR,                    None,  "", None, None],
    ["CONC1",                           "General",                  "conc.1",                           DataType.REAL,                       None,  "", None, None],
    ["CONC_UNIT1",                      "General",                  "concentration unit 1",             DataType.VARCHAR,                    None,  "", None, None],
    ["CONC2",                           "General",                  "conc.2",                           DataType.REAL,                       None,  "", None, None],
    ["CONC_UNIT2",                      "General",                  "concentration unit 2",             DataType.CONTROLLEDVOCABULARY,       "CONC_UNITS",  "", None, None],
    ["CONC3",                           "General",                  "conc.3",                           DataType.REAL,                       None,  "", None, None],
    ["CONC_UNIT3",                      "General",                  "concentration unit 3",             DataType.CONTROLLEDVOCABULARY,       "CONC_UNITS",  "", None, None],
    ["CREATION_DATE",                   "General",                  "creation date",                    DataType.TIMESTAMP,                  None,  "", None, None],
    ["DATE",                            "General",                  "date",                             DataType.VARCHAR,                    None,  "", None, None],
    ["DERIVATION",                      "General",                  "derivation",                       DataType.CONTROLLEDVOCABULARY,       "DERIVATION",  "", None, None],
    ["EXPRESSED_PROTEIN1",              "General",                  "expressed protein 1",              DataType.VARCHAR,                    None,  "", None, None],
    ["EXPRESSED_PROTEIN2",              "General",                  "expressed protein 2",              DataType.VARCHAR,                    None,  "", None, None],
    ["EXPRESSED_PROTEIN3",              "General",                  "expressed protein 3",              DataType.VARCHAR,                    None,  "", None, None],
    ["FREE_PLASMID",                    "General",                  "freeplasmid",                      DataType.VARCHAR,                    None,  "", None, None],
    ["FREEZEDATE",                      "General",                  "freezedate",                       DataType.VARCHAR,                    None,  "", None, None],
    ["FROZEN",                          "General",                  "frozen",                           DataType.TIMESTAMP,                  None,  "", None, None],
    ["GAL",                             "General",                  "gal",                              DataType.CONTROLLEDVOCABULARY,       "GAL",  "", None, None],
    ["GENE_EXPRESSION1",                "General",                  "gene expression 1",                DataType.VARCHAR,                    None,  "", None, None],
    ["GENE_EXPRESSION2",                "General",                  "gene expression 2",                DataType.VARCHAR,                    None,  "", None, None],
    ["GROWTH_TEMPERATURE",              "General",                  "growth temperature",               DataType.CONTROLLEDVOCABULARY,       "GROWTH_TEMPERATURE",  "", None, None],
    ["INDUCER1",                        "General",                  "inducer 1",                        DataType.VARCHAR,                    None,  "", None, None],
    ["INDUCER2",                        "General",                  "inducer 2",                        DataType.VARCHAR,                    None,  "", None, None],
    ["INDUCER3",                        "General",                  "inducer 3",                        DataType.VARCHAR,                    None,  "", None, None],
    ["COMMENTS",                        "General",                  "info",                             DataType.VARCHAR,                    None,  "", None, None],
    ["INVESTIGATOR",                    "General",                  "investigator",                     DataType.CONTROLLEDVOCABULARY,       "ALL_LAB_MEMBERS",  "", None, None],
    ["MARKERS",                         "General",                  "markers",                          DataType.VARCHAR,                    None,  "", None, None],
    ["MAT",                             "General",                  "mat",                              DataType.CONTROLLEDVOCABULARY,       "MAT",  "", None, None],
    ["MEDIUM",                          "General",                  "medium",                           DataType.CONTROLLEDVOCABULARY,       "MEDIUM",  "", None, None],
    ["MEDIUM_SPECIALS",                 "General",                  "medium specials",                  DataType.VARCHAR,                    None,  "", None, None],
    ["MODIFICATION_DATE",               "General",                  "modification date",                DataType.TIMESTAMP,                  None,  "", None, None],
    ["MODIFIED_BY",                     "General",                  "modified by",                      DataType.CONTROLLEDVOCABULARY,       "ALL_LAB_MEMBERS",  "", None, None],
    ["NUMBER",                          "General",                  "number",                           DataType.VARCHAR,                    None,  "", None, None],
    ["OBTAINED_FROM",                   "General",                  "obtained from",                    DataType.VARCHAR,                    None,  "", None, None],
    ["ORF",                             "General",                  "orf",                              DataType.VARCHAR,                    None,  "", None, None],    
    ["PAGE_NUMBER",                     "General",                  "page number",                      DataType.VARCHAR,                    None,  "", None, None],
    ["PARENTAL_CELL",                   "General",                  "parental cell",                    DataType.VARCHAR,                    None,  "", None, None],
    ["PASSAGE",                         "General",                  "passage",                          DataType.VARCHAR,                    None,  "", None, None],
    ["PERSYEASTNUMBER",                 "General",                  "persyeastnumber",                  DataType.INTEGER,                    None,  "", None, None],
    ["PETERYEASTNUMBER",                "General",                  "peteryeastnumber",                 DataType.INTEGER,                    None,  "", None, None],
    ["PLASMID_ID1",                     "General",                  "plasmid ID 1",                     DataType.VARCHAR,                    None,  "", None, None],
    ["PLASMID_ID2",                     "General",                  "plasmid ID 2",                     DataType.VARCHAR,                    None,  "", None, None],
    ["PLASMID_ID3",                     "General",                  "plasmid ID 3",                     DataType.VARCHAR,                    None,  "", None, None],
    ["PROMOTER1",                       "General",                  "promoter 1",                       DataType.VARCHAR,                    None,  "", None, None],
    ["PROMOTER2",                       "General",                  "promoter 2",                       DataType.VARCHAR,                    None,  "", None, None],
    ["PROMOTER3",                       "General",                  "promoter 3",                       DataType.VARCHAR,                    None,  "", None, None],
    ["PUBLISHED_IN",                    "General",                  "reference",                        DataType.VARCHAR,                    None,  "", None, None],
    ["SELECTION1",                      "General",                  "sel.1",                            DataType.VARCHAR,                    None,  "", None, None],
    ["SELECTION2",                      "General",                  "sel.2",                            DataType.VARCHAR,                    None,  "", None, None],
    ["SELECTION3",                      "General",                  "sel.3",                            DataType.VARCHAR,                    None,  "", None, None],
    ["SELECTION",                       "General",                  "selection",                        DataType.CONTROLLEDVOCABULARY,       "SELECTION_YEAST",  "", None, None],
    ["SERIAL_NUMBER",                   "General",                  "serial number",                    DataType.INTEGER,                    None,  "", None, None],
    ["STRAIN_SOURCE",                   "General",                  "source",                           DataType.CONTROLLEDVOCABULARY,       "STRAIN_SOURCE",  "", None, None],
    ["SPECIES",                         "General",                  "species",                          DataType.VARCHAR,                    None,  "", None, None],
    ["STRAIN_ID_NR",                    "General",                  "strain_id_nr",                     DataType.VARCHAR,                    None,  "", None, None],
    ["NAME",                            "General",                  "strain ID",                        DataType.VARCHAR,                    None,  "", None, None],
    ["STRAIN_NAME",                     "General",                  "strain name",                      DataType.VARCHAR,                    None,  "", None, None]
];

oligoDefinition = [
    #["ADENOSINE",                       "General",                  "adenosine",                        DataType.XML,                       None, "", adenineScriptName],
    ["AMOUNT",                          "General",                  "amount",                           DataType.REAL,                      None, "", None, None],
    ["BARCODE",                         "General",                  "barcode",                          DataType.VARCHAR,                   None, "", None, None],
    ["BARCODE_LABEL",                   "General",                  "barcode label",                    DataType.VARCHAR,                   None, "", None, None],
    ["COMPANY",                         "General",                  "company",                          DataType.CONTROLLEDVOCABULARY,      "OLIGO_COMPANY", "", None, None],
    ["CONCENTRATION",                   "General",                  "concentration",                    DataType.REAL,                      None, "", None, None],
    ["CONCENTRATION_UNIT",              "General",                  "concentration unit",               DataType.CONTROLLEDVOCABULARY,      "CONC_UNITS", "", None, None],
    ["CREATION_DATE",                   "General",                  "creation date",                    DataType.TIMESTAMP,                 None, "", None, None],
   # ["CYTOSINE",                        "General",                  "cytosine",                         DataType.XML,                       None, "", cytosineScriptName],
    ["DATE",                            "General",                  "date",                             DataType.VARCHAR,                   None, "", None, None],
    ["FROZEN",                          "General",                  "frozen",                           DataType.TIMESTAMP,                 None, "", None, None],
    #["GC",                              "General",                  "gc",                               DataType.XML,                       None, "", gcScriptName],
    ["GENE_LOCUS",                      "General",                  "gene locus",                       DataType.VARCHAR,                   None, "", None, None],
    ["GRADE",                           "General",                  "grade",                            DataType.CONTROLLEDVOCABULARY,      "OLIGO_GRADE", "", None, None],
    #["GUANOSINE",                       "General",                  "guanosine",                        DataType.XML,                       None, "", guanosineScriptName],
    ["COMMENTS",                        "General",                  "Info",                             DataType.VARCHAR,                   None, "", None, None],
    ["INVESTIGATOR",                    "General",                  "investigator",                     DataType.CONTROLLEDVOCABULARY,      "ALL_LAB_MEMBERS", "", None, None],
    #["LENGTH",                          "General",                  "length",                           DataType.XML,                       None, "", lengthScriptName],
    ["MODIFICATION_DATE",               "General",                  "modification date",                 DataType.TIMESTAMP,                 None, "", None, None],
    ["MODIFIED_BY",                     "General",                  "modified by",                       DataType.CONTROLLEDVOCABULARY,      "ALL_LAB_MEMBERS", "", None, None],
    #["O_NUCLEOTIDE",                    "General",                  "O nucleotide",                     DataType.XML,                       None, "", nucelotideScriptName],
    ["OD",                              "General",                  "od",                               DataType.INTEGER,                   None, "", None, None],
    ["NAME",                            "General",                  "oligo ID",                         DataType.INTEGER,                   None, "", None, None],
    ["OLIGO_ID_NR",                     "General",                  "oligo_id_nr",                      DataType.VARCHAR,                   None, "", None, None],
    ["ORDER_DATE",                      "General",                  "order date",                       DataType.VARCHAR,                   None, "", None, None],
    ["ORGANISM",                        "General",                  "organism",                         DataType.CONTROLLEDVOCABULARY,      "OLIGO_ORGANISM", "", None, None],
    ["PAGE_NUMBER",                     "General",                  "page number",                      DataType.VARCHAR,                   None, "", None, None],
    ["PRIMER_PARTNER",                  "General",                  "primer partner",                   DataType.VARCHAR,                   None, "", None, None],
    ["PRIMER_POSITION",                 "General",                  "primer position",                  DataType.CONTROLLEDVOCABULARY,      "PRIMER_POSITION", "", None, None],
    ["PUBLISHED_IN",                    "General",                  "PublishedIn",                      DataType.VARCHAR,                   None, "", None, None],
    ["RESTRICTION_SITE",                "General",                  "restriction site",                 DataType.CONTROLLEDVOCABULARY,      "RESTRICTION", "", None, None],
    ["SEQUENCE",                        "General",                  "sequence",                         DataType.VARCHAR,                   None, "", None, None],
    ["SERIAL_NUMBER",                   "General",                  "serial number",                    DataType.VARCHAR,                   None, "", None, None],
    #["THYMIDINE",                       "General",                  "thymidine",                        DataType.XML,                       None, "", thymidineScriptName],
    #["TM",                              "General",                  "tm",                               DataType.XML,                       None, "", tmScriptName],
    ["USAGE",                           "General",                  "usage",                            DataType.CONTROLLEDVOCABULARY,      "OLIGO_USAGE", "", None, None]

];

plasmidDefinition = [
    ["BACKBONE",                        "General",                  "backbone",                         DataType.CONTROLLEDVOCABULARY,      "BACKBONE",  "", None, None],
    ["BACKUP_STORAGE",                  "General",                  "backup storage",                   DataType.VARCHAR,                   None,  "", None, None],
    ["BACTERIAL_HOST",                  "General",                  "bacterial host",                   DataType.CONTROLLEDVOCABULARY,      "BACTERIAL_HOST",  "", None, None],
    ["BARCODE",                         "General",                  "barcode",                          DataType.VARCHAR,                   None,  "", None, None],
    ["BARCODE_LABEL",                   "General",                  "barcode label",                    DataType.VARCHAR,                   None,  "", None, None],
    ["BIOSAFETY_LEVEL",                 "General",                  "biosafety level",                  DataType.CONTROLLEDVOCABULARY,      "BIOSAFETY_LEVEL",  "", None, None],
    ["BOXNR",                           "General",                  "boxNr",                            DataType.VARCHAR,                   None,  "", None, None],
    ["CONCENTRATION_UNIT",              "General",                  "concentration unit",               DataType.CONTROLLEDVOCABULARY,      "CONC_UNITS",  "", None, None],
    ["CONSTRUCTION_METHOD",             "General",                  "construction method",              DataType.VARCHAR,                   None,  "", None, None],
    ["CREATION_DATE",                   "General",                  "creation date",                    DataType.TIMESTAMP,                 None,  "", None, None],
    ["CTERMTAG",                        "General",                  "Ctermtag",                         DataType.VARCHAR,                   None,  "", None, None],
    ["DATE",                            "General",                  "date",                             DataType.VARCHAR,                   None,  "", None, None],
    ["DATE_OF_CONSTRUCTION",            "General",                  "date of construction",             DataType.TIMESTAMP,                 None,  "", None, None],
    ["DERIVATIVE_OF",                   "General",                  "Derivative of",                    DataType.VARCHAR,                   None,  "", None, None],
    ["ECOLI_STRAIN",                    "General",                  "E.coli strain",                    DataType.VARCHAR,                   None,  "", None, None],
    ["EXCISE_INSERT",                   "General",                  "excise insert",                    DataType.VARCHAR,                   None,  "", None, None],
    ["EXCISE_INSERT2",                  "General",                  "excise insert 2",                  DataType.VARCHAR,                   None,  "", None, None],
    ["FREEZEDATE",                      "General",                  "freezedate",                       DataType.VARCHAR,                   None,  "", None, None],
    ["FROZEN",                          "General",                  "frozen",                           DataType.TIMESTAMP,                 None,  "", None, None],
    ["FUNCTIONAL_ELEMENTS_INSERT",      "General",                  "functional elements insert",       DataType.VARCHAR,                   None,  "", None, None],
    ["HYPERCARD_FILE",                  "General",                  "hypercard file",                   DataType.VARCHAR,                   None,  "", None, None],
    ["INFO",                            "General",                  "info",                             DataType.VARCHAR,                   None,  "", None, None],
    ["INSERT",                          "General",                  "insert",                           DataType.VARCHAR,                   None,  "", None, None],
    ["INSERTION_SITE",                  "General",                  "insertion site",                   DataType.VARCHAR,                   None,  "", None, None],
    ["INVESTIGATOR",                    "General",                  "investigator",                     DataType.CONTROLLEDVOCABULARY,      "ALL_LAB_MEMBERS",  "", None, None],
    ["LINEARIZE",                       "General",                  "linearize",                        DataType.VARCHAR,                   None,  "", None, None],
    ["MARKER",                          "General",                  "Marker",                           DataType.VARCHAR,                   None,  "", None, None],
    ["MODIFICATION_DATE",               "General",                  "modification date",                DataType.VARCHAR,                   None,  "", None, None],
    ["MODIFIED_BY",                     "General",                  "modified by",                      DataType.CONTROLLEDVOCABULARY,      "ALL_LAB_MEMBERS",  "", None, None],
    ["NAME_OF_PARENTAL_CLONE",          "General",                  "name of parental clone",           DataType.VARCHAR,                   None,  "", None, None],
    ["NAME_OF_VECTOR",                  "General",                  "name of vector",                   DataType.VARCHAR,                   None,  "", None, None],
    ["NTERMTAG",                        "General",                  "Ntermtag",                         DataType.VARCHAR,                   None,  "", None, None],
    ["NUCLEOTIDE_SEQUENCE",             "General",                  "nucleotide sequence",              DataType.VARCHAR,                   None,  "", None, None],
    ["OLIGOS_USED",                     "General",                  "oligos used",                      DataType.VARCHAR,                   None,  "", None, None],
    ["ORGANISM",                        "General",                  "organism",                         DataType.VARCHAR,                   None,  "", None, None],
    ["ORIGIN_OF_REPLICATION",           "General",                  "origin of replication",            DataType.VARCHAR,                   None,  "", None, None],
    ["OTHER_MARKER",                    "General",                  "other marker",                     DataType.VARCHAR,                   None,  "", None, None],
    ["PAGE_NUMBER",                     "General",                  "page number",                      DataType.VARCHAR,                   None,  "", None, None],
    ["PERSPLASMIDNUM",                  "General",                  "persPlasmidnum",                   DataType.INTEGER,                   None,  "", None, None],
    ["PETERPLASMIDNUM",                 "General",                  "PeterPlasmidNum",                  DataType.INTEGER,                   None,  "", None, None],
    ["PLASMID_ID",                      "General",                  "plasmid ID",                       DataType.VARCHAR,                   None,  "", None, None],
    ["PLASMID_ID_NR",                   "General",                  "plasmid_id_nr",                    DataType.VARCHAR,                   None,  "", None, None],
    ["PLASMIDNAME",                     "General",                  "plasmidname",                      DataType.VARCHAR,                   None,  "", None, None],
    ["PRIMERS",                         "General",                  "primers",                          DataType.VARCHAR,                   None,  "", None, None],
    ["PROMOTER",                        "General",                  "promoter",                         DataType.CONTROLLEDVOCABULARY,      "PROMOTER",  "", None, None],
    ["PUBLISHED_IN",                    "General",                  "published in",                     DataType.VARCHAR,                   None,  "", None, None],
    ["SELECTION_IN_BACTERIA",           "General",                  "selection in bacteria",            DataType.CONTROLLEDVOCABULARY,      "SELECTION_IN_BACTERIA",  "", None, None],
    ["SELECTION_IN_EUKARYOTES",         "General",                  "selection in eukaryotes",          DataType.CONTROLLEDVOCABULARY,      "SELECTION_IN_EUKARYOTES",  "", None, None],
    ["SEQUENCE_FILE_NAME",              "General",                  "sequence file name",               DataType.VARCHAR,                   None,  "", None, None],
    ["SEQUENCING_DATE",                 "General",                  "sequencing date",                  DataType.TIMESTAMP,                 None,  "", None, None],
    ["SERIAL_NUMBER",                   "General",                  "serial number",                    DataType.INTEGER,                   None,  "", None, None],
    ["SHELF",                           "General",                  "shelf",                            DataType.VARCHAR,                   None,  "", None, None],
    ["SIZE_INSERT",                     "General",                  "size insert",                      DataType.REAL,                      None,  "", None, None],
    ["SIZE_OF_INSERT",                  "General",                  "size of insert",                   DataType.VARCHAR,                   None,  "", None, None],
    ["SIZE_VECTOR",                     "General",                  "size vector",                      DataType.REAL,                      None,  "", None, None],
    ["SOURCE",                          "General",                  "source",                           DataType.VARCHAR,                   None,  "", None, None],
    ["SPECIES",                         "General",                  "species",                          DataType.CONTROLLEDVOCABULARY,      "PLASMID_ORGANISM",  "", None, None],
    #["TUBES",                           "General",                 "TUBES",                            DataType.VARCHAR,                   None,  "", None, None],
    ["URL",                             "General",                  "url",                              DataType.VARCHAR,                   None,  "", None, None]
];

chemicalDefinition = [
    ["ARTICLE",                         "General",                  "Article",                          DataType.VARCHAR,                   None,  "", None, None],
    ["COMPANY",                         "General",                  "Company",                          DataType.CONTROLLEDVOCABULARY,      "COMPANY",  "", None, None],
    ["ARTICLE_NUM",                     "General",                  "Article Number",                   DataType.VARCHAR,                   None,  "", None, None],
    ["LOCATION",                        "General",                  "Location",                         DataType.VARCHAR,                   None,  "", None, None],   
    ["RECORD_NUMBER",                   "General",                  "RecordNumber",                     DataType.VARCHAR,                   None,  "", None, None]
];

siRNADefinition = [
    ["INVESTIGATOR",                    "General",                  "investigator",                     DataType.CONTROLLEDVOCABULARY,      "ALL_LAB_MEMBERS",  "", None, None],
    ["GENE_LOCUS",                      "General",                  "gene locus",                       DataType.VARCHAR,                   None,  "", None, None],
    ["EXON_OR_THREE_PRIME",             "General",                  "exon or three prime",              DataType.VARCHAR,                   None,  "", None, None],
    ["EXON",                            "General",                  "exon #",                           DataType.VARCHAR,                   None,  "", None, None],
    ["3_PRIME_POSITION",                "General",                  "3' position",                      DataType.VARCHAR,                   None,  "", None, None],
    ["SPECIES",                         "General",                  "species",                          DataType.VARCHAR,                   None,  "", None, None],
    ["SEQUENCE",                        "General",                  "SEQUENCE",                         DataType.VARCHAR,                   None,  "", None, None],
    ["COMPANY",                         "General",                  "Company",                          DataType.VARCHAR,                   None,  "", None, None],
    ["STOCK_CONCENTRATION",             "General",                  "Stock concentration",              DataType.VARCHAR,                   None,  "", None, None],
    ["WORKING_CONCENTRATION",           "General",                  "Working concentration",            DataType.VARCHAR,                   None,  "", None, None],
    ["EFFICIENCY",                      "General",                  "Efficiency",                       DataType.VARCHAR,                   None,  "", None, None],
    ["EFFICIENCY_QPCR",                 "General",                  "Efficiency_qPCR",                  DataType.VARCHAR,                   None,  "", None, None],
    ["EFFICIENCY_WESTERN",              "General",                  "Efficiency_Western",               DataType.VARCHAR,                   None,  "", None, None],
    ["EFFICIENCY_OTHER",                "General",                  "Efficiency_other",                 DataType.VARCHAR,                   None,  "", None, None],
    ["OFF_TARGET_EFFECTS",              "General",                  "Off target effects",               DataType.VARCHAR,                   None,  "", None, None],
    ["INFO",                            "General",                  "Info",                             DataType.VARCHAR,                   None,  "", None, None],
    ["SPECIFIC_SPLICE_VARIANT",         "General",                  "Specific splice variant",          DataType.VARCHAR,                   None,  "", None, None],
    ["TRANSFECTION_AGENT",              "General",                  "Transfection agent",               DataType.VARCHAR,                   None,  "", None, None],
    ["PUBLISHED",                       "General",                  "Published",                        DataType.VARCHAR,                   None,  "", None, None],
    ["LIBRARY",                         "General",                  "Library",                          DataType.VARCHAR,                   None,  "", None, None],
    ["CHARACTERIZED_BY_COMPANY",        "General",                  "Characterized by company",         DataType.VARCHAR,                   None,  "", None, None],
    ["NUMBERINPOOL",                    "General",                  "NumberINpool",                     DataType.VARCHAR,                   None,  "", None, None],
    ["TEST",                            "General",                  "Test",                             DataType.VARCHAR,                   None,  "", None, None],
    ["CHEMICAL_MODIFICATION",           "General",                  "Chemical modification",            DataType.VARCHAR,                   None,  "", None, None],
    ["OLIGO_NAME",                      "General",                  "Oligo name",                       DataType.VARCHAR,                   None,  "", None, None],
    ["SIRNA_OLIGONUMBER",               "General",                  "siRNA_OligoNumber",                DataType.VARCHAR,                   None,  "", None, None],
    ["FREEZER_LOCATION",                "General",                  "Freezer Location",                 DataType.VARCHAR,                   None,  "", None, None],
    ["DRAWER",                          "General",                  "Drawer",                           DataType.VARCHAR,                   None,  "", None, None]
];

#
# Data Set Types
#
documentDefinition = [
    ["NOTES", "General information", "Notes", DataType.MULTILINE_VARCHAR, None, "Notes regarding the dataset", None, None]
];
