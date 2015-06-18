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
                                        ]
                          }