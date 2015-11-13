#!/usr/bin/env python
# -*- coding: utf-8 -*-

#
# Helper Methods
#
def getVocabularyTermCodeForVocabularyAndTermLabel(vocabularyCode, termLabel):
    vocabulary = vocabularyDefinitions[vocabularyCode]
    for term in vocabulary:
        if term[1] == termLabel:
            #print repr("TERM NOT FOUND IN VOCABULARY LIST: " + unicode(vocabularyCode) + " : '" + unicode(termLabel) + "'")
            return term[0]
    return None

# "VOCABULARY_CODE" : { "TERM_CODE" : "OBJECT" }
createdVocabularyTerms = { }

def printCreatedTerms():
    print "--- Created Vocabulary Terms Report"
    for vocabularyCode in createdVocabularyTerms:
        print "Vocabulary [" + vocabularyCode + "]"
        for vocabularyTermCode in createdVocabularyTerms[vocabularyCode]:
            print "Term [" + vocabularyTermCode + "] Label: [" + repr(createdVocabularyTerms[vocabularyCode][vocabularyTermCode].getLabel()) + "]"
    print "---"

def getCreatedTerm(vocabularyCode, termCode):
    if vocabularyCode in createdVocabularyTerms:
        if termCode in createdVocabularyTerms[vocabularyCode]:
            return createdVocabularyTerms[vocabularyCode][termCode]
    return None

def addCreatedTerm(vocabularyCode, termCode, object):
    if vocabularyCode not in createdVocabularyTerms:
        createdVocabularyTerms[vocabularyCode] = {}
    createdVocabularyTerms[vocabularyCode][termCode] = object
    
def createVocabularyTerm(tr, vocabularyCode, termCode, termLabel):
    createdTerm = getCreatedTerm(vocabularyCode, termCode)
    if createdTerm is None:
        vocabulary = tr.getVocabularyForUpdate(vocabularyCode)
        
        for term in vocabulary.getTerms():
            if term.getCode() == termCode:
                createdTerm = term
        
        if createdTerm is None:
            createdTerm = tr.createNewVocabularyTerm()
            createdTerm.setCode(termCode)
            createdTerm.setLabel(termLabel)
            createdTerm.setOrdinal(vocabulary.getTerms().size())
            vocabulary.addTerm(createdTerm)
            addCreatedTerm(vocabularyCode, termCode, createdTerm)
    return createdTerm

#
# Vocabularies
#

vocabularyDefinitions = {
                           "LAB_MEMBERS" : [
                                          ["ELISA_DULTZ",   "Elisa Dultz"]
                                         ],
                       
  
                             "COMPANY" : [
                                          ["ABCAM",   "abcam"],
                                          ["ACROS_ORGANICS",   "Acros Organics"],
                                          ["ALDRICH",   "Aldrich"],
                                          ["AMBION",   "ambion"],
                                          ["AMERSHAM",   "Amersham"],
                                          ["AMSBIO",   "amsbio"],
                                          ["APPLICHEM",   "Applichem"],
                                          ["APPLIED_BIOSYSYTEMS",   "applied biosysytems"],
                                          ["BACTO",   "Bacto"],
                                          ["BD",   "BD"],
                                          ["BIO-RAD",   "Bio-Rad"],
                                          ["BIOCHEMICA",   "biochemica"],
                                          ["BIOEXPRESS",   "Bioexpress"],
                                          ["CALBIOCHEM",   "Calbiochem"],
                                          ["CAMBRIDGE_ISOTOPE_LABORATORIES_INC.",   "Cambridge Isotope Laboratories, Inc."],
                                          ["CARGILLE",   "Cargille"],
                                          ["CHEM_IMPEX_INTERNATIONAL",   "Chem Impex International"],
                                          ["CHEMICON",   "Chemicon"],
                                          ["CHEMIE_BRUNSCHWIG",   "chemie brunschwig"],
                                          ["CHEMIKA",   "chemika"],
                                          ["CYTOSKELETON",   "Cytoskeleton"],
                                          ["D-BIOL_SHOP_GIBCO",   "D-BIOL shop (Gibco)"],
                                          ["D-CHAB",   "D-CHAB"],
                                          ["DUPONT",   "DuPont"],
                                          ["EK_SCIENTIFIC",   "E&K Scientific"],
                                          ["ECHELON_BIOSCIENCES",   "Echelon Biosciences"],
                                          ["EMPTY",   "EMPTY"],
                                          ["ENZO",   "Enzo"],
                                          ["EUROBIO",   "eurobio"],
                                          ["FALTZ_BAUER",   "faltz bauer"],
                                          ["FISHER",   "Fisher"],
                                          ["FLUKA",   "Fluka"],
                                          ["FMC_BIOPRODUCTS",   "FMC BioProducts"],
                                          ["FOREMEDIUM",   "Foremedium"],
                                          ["GE_HEALTHCARE",   "GE healthcare"],
                                          ["GIBCO_BRL",   "Gibco BRL"],
                                          ["GIBCO_BRL_OR_ROCHE",   "Gibco BRL or Roche"],
                                          ["ICN",   "ICN"],
                                          ["ICN_AMSBIO",   "ICN amsbio"],
                                          ["INVITROGEN",   "Invitrogen"],
                                          ["INVITROGEN-MOLECULAR_PROBES",   "Invitrogen/Molecular Probes"],
                                          ["INVIVOGEN",   "Invivogen"],
                                          ["JTBAKER",   "J.T.Baker"],
                                          ["LC_LABORATORIES",   "LC Laboratories"],
                                          ["LIFE_TECHNOLOGIES",   "Life Technologies"],
                                          ["LONZA",   "lonza"],
                                          ["LUCERNA_CHEM",   "Lucerna Chem"],
                                          ["MEDIATECH_STOCKROOM",   "Mediatech / stockroom"],
                                          ["MERCK",   "merck"],
                                          ["MERCK_MILLIPORE",   "Merck Millipore"],
                                          ["MILLIPORE",   "Millipore"],
                                          ["MOLECULAR_PROBES",   "Molecular Probes"],
                                          ["MP_BIOMEDICALS",   "MP Biomedicals"],
                                          ["ND",   "n/d"],
                                          ["NONE",   "None"],
                                          ["ODYSSEY",   "odyssey"],
                                          ["PIERCE",   "Pierce"],
                                          ["PROMEGA",   "promega"],
                                          ["REHEIS",   "reheis"],
                                          ["RIEDEL-DE-HAEN",   "Riedel-de-Haen"],
                                          ["ROCHE",   "Roche"],
                                          ["SANTA_CRUZ_BIOTECHNOLOGY_SCB",   "Santa Cruz Biotechnology (SCB)"],
                                          ["SCHARLAU",   "scharlau"],
                                          ["SEARLE",   "searle"],
                                          ["SERVA",   "serva"],
                                          ["SFLUKA",   "SFluka"],
                                          ["SIEGFRIED_ZOFINGEN",   "Siegfried Zofingen"],
                                          ["SIGMA-ALDRICH",   "Sigma-Aldrich"],
                                          ["SIGMA-BHD_GRP",   "Sigma/BHD GRP"],
                                          ["THERMO-SCIENTIFIC",   "Thermo scientific"],
                                          ["USBIOLOGICAL",   "USBiological"],
                                          ["WERNER_BIOAGENTS",   "Werner BioAgents"],
                                          ["WITECAG",   "witecag"],
                                          ["UNKNOWN",   "unknown"],                                          
                                     ],
                              "OLIGO_CATEGORY" : [
                                         ["CHECKING",   "Checking"],
                                         ["CLONING",   "Cloning"],
                                         ["MUTAGENESIS",   "Mutagenesis"],
                                         ["PRINGLE",   "Pringle"],
                                         ["QPCR",   "qPCR"],  
                                         ["SEQUENCING",   "qPCR"],                                                                                  
                                      ],
                              "OLIGO_TYPE" : [
                                         ["DNA",   "DNA"]
                                      ],
                              "PLASMID_BACTERIAL_STRAIN" : [                         
                                              ["XL1BLUE",   "XL1blue"],
                                              ["XL10-GOLD",   "XL10-Gold"],
                                              ["XL1-BLUE",   "XL1-Blue"],
                                              ["TG2",   "TG2"],
                                              ["TG1",   "TG1"],
                                              ["SURE",   "SURE"],
                                              ["STBL3",   "STBL3"],
                                              ["STBL2",   "STBL2"],
                                              ["SOLR_CELLS",   "SOLR cells"],
                                              ["SG13",   "SG13"],
                                              ["SCS110",   "SCS110"],
                                              ["MC1061",   "MC1061"],
                                              ["MACH1",   "MACH1"],
                                              ["M15PREP4",   "M15prep4"],
                                              ["KC8",   "KC8"],
                                              ["HB101",   "HB101"],
                                              ["E_COLI",   "E.coli"],
                                              ["DH5ALPHA",   "DH5alpha"],
                                              ["DH10B",   "DH10B"],
                                              ["DH1",   "DH1"],
                                              ["CCDB_SURVIVAL",   "ccdB Survival"],
                                              ["BLR",   "BLR"],
                                              ["BL21_PLUS",   "BL21+"],
                                              ["BL21-LYSS",   "BL21[LysS]"],
                                              ["BL21",   "BL21"],
                                              ["UNKNOWN",   "???"]
                                              ],                          
                             "PLASMID_DRUG_RES" : [                                
                                                           ["AMP",   "Amp"],
                                                           ["AMP_MAYBE",   "AMP?"],
                                                           ["CAM",   "CAM"],
                                                           ["KAN",   "KAN"],
                                                           ["KAN_CAM",   "kan cam"],
                                                           ["KAN-CAM-CCDB_CELLS",   "KAN,CAM, ccdB cells"],
                                                           ["TET",   "tet"],
                                                           ["ZEO",   "Zeo"]
                                                    ],    

                             "PLASMID_SELECTION_MAKER" : [                            
                                                   ["URA3_ADE3",    "URA3, ADE3"],
                                                   ["ADE2",    "ADE2"],
                                                   ["CAURA",    "CaURA"],
                                                   ["CAURA3",    "CaURA3"],
                                                   ["CEN_HIS3",    "CEN, HIS3"],
                                                   ["G418",    "G418"],
                                                   ["HIS3",    "HIS3"],
                                                   ["HIS3_OR_LEU2",    "HIS3 or LEU2"],
                                                   ["HIS3MX",    "HIS3MX"],
                                                   ["HIS5",    "HIS5"],
                                                   ["HIS5MX",    "His5MX"],
                                                   ["HISMX",    "HisMX"],
                                                   ["HISMX6",    "HISMX6"],
                                                   ["HPHMX",    "HphMx"],
                                                   ["HYG",    "Hyg"],
                                                   ["HYGB",    "HYGB"],
                                                   ["HYGB_R",    "HygB R"],
                                                   ["KAN",    "kan"],
                                                   ["KANMX",    "KanMX"],
                                                   ["LEU",    "Leu"],
                                                   ["LEU2",    "LEU2"],
                                                   ["MET15",    "MET15"],
                                                   ["NAT",    "Nat"],
                                                   ["NATMX",    "NatMX"],
                                                   ["NEO",    "Neo"],
                                                   ["NOTHING",    "nothing"],
                                                   ["S_POMBE_HIS5",    "S. pombe His5"],
                                                   ["SPHIS5",    "SpHIS5"],
                                                   ["TRP1",    "TRP1"],
                                                   ["URA",    "URA"],
                                                   ["URA3",    "URA3"],
                                                   ["URA3_CA",    "URA3 (C.a.)"]
                                            ],
                         
                            "EXPERIMENTAL_READOUT":
                                    [
                                        ["FLOW_CYTOMETRY", "flow citometry", None],
                                        ["SORTING", "cell sorting", None],
                                        ["GROWTH", "growth", None],
                                        ["WESTERN_BLOTTING", "western blotting", None],
                                        ["RT_QPCR", "RT-qPCR", None]
                                    ],
                            "MACHINE":
                                    [
                                        ["LSRII_FORTESSA", "SRII Fortessa", None],
                                        ["TECAN_READER", "Tecan reader", None],
                                        ["BIOLECTOR", "BioLector", None],
                                        ["LICOR_ODYSSEY", "LI-COR Odyssey", None],
                                        ["TI_ECLIPSE", "TI Eclipse (Nikon)", None],
                                        ["SRX_101A", "Konica Minolta SRX-101A", None],
                                        ["LIGHT_CYCLER", "LightCycler 480", None]
                                    ],                        
                                 "FREEZER" : [
                                             ["BENCH-20",  "Bench -20�C"],
                                             ["BENCH-80",  "Bench -80�C"],
                                             ["BENCH-RT",  "Bench RT"],
                                             ["BENCH",  "Bench"]
                                        ],
                                 "MAT" : [
                                             ["ALPHA",  "a"],
                                             ["A",  "A"],
                                             ["A_ALPHA",  "a/A"],
                                             ["A_A",  "A/A"],
                                             ["ALPHA_ALPHA",  "a/a"]
                                        ]                         
                         
                          }