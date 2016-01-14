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
                                          ["ELISA_DULTZ",   "E. Dultz"],
                                          ["A_KOEK",   "A. Koek"],
                                          ["A_MADRID",   "A. Madrid"],
                                          ["AC_STROM",   "A.C. Strom"],
                                          ["A_HIBBEL",   "A. Hibbel"],
                                          ["B_ZEITLER",   "B. Zeitler"],
                                          ["B_MONTPETIT",   "B. Montpetit"],
                                          ["C_BRUNE",   "C. Brune"],
                                          ["C_DERRER",   "C. Derrer"],
                                          ["C_SMOLKE",   "C. Smolke"],
                                          ["C_WEIRICH",   "C. Weirich"],
                                          ["C_MUGLER",   "C. Mugler"],
                                          ["E_GREEN",   "E. Green"],
                                          ["E_ONISHCHENKO",   "E. Onishchenko"],
                                          ["E_WEIDER",   "E. Weider"],
                                          ["J_CARROLL",   "J. Carroll"],
                                          ["J_TANG",   "J. Tang"],
                                          ["J_SODERHOLM",   "Jon Soderholm"],
                                          ["K_WEIS",   "K. Weis"],
                                          ["L_STANTON",   "L. Stanton"],
                                          ["M_BLOWER",   "M. Blower"],
                                          ["M_HERZOG",   "M. Herzog"],
                                          ["M_HOCHSTRASSER",   "M. Hochstrasser"],
                                          ["M_HONDELE",   "M. Hondele"],
                                          ["M_NACHURY",   "M. Nachury"],
                                          ["N_FISCHER",   "N. Fischer"],                                          
                                          ["L_CHAN",   "L. Chan"],                                          
                                          ["P_KALAB",   "P. Kalab"],                                          
                                          ["R_JOYNER",   "R. Joyner"],                                          
                                          ["R_SACHDEV",   "R. Sachdev"],                                          
                                          ["R_MANCINI",   "R. Mancini"],                                          
                                          ["S_MUNCHEL",   "S. Munchel"],                                          
                                          ["N_TAKIZAWA",   "N. Takizawa"],                                          
                                          ["S_BIRD",   "S. Bird"],                                          
                                          ["S_HEINRICH",   "S. Heinrich"],                                          
                                          ["S_RAJOO",   "S. Rajoo"],                                          
                                          ["Y_JIANG",   "Y. Jiang"],                                          
                                          ["Z_DOSSANI",   "Z. Dossani"],                                          
                                          ["B_ROBINSON",   "B. Robison"],                                          
                                          ["C_MCGOURTY",   "C. McGourty"],                                          
                                          ["SEE_COMMENTS",   "See comments"],                                          
                                         
                                         ],
                              "AB_CLONALITY" : [
                                         ["MONOCLONAL",   "monoclonal"],
                                         ["POLYCLONAL",   "polyclonal"],
                                      ],                       
                              "AB_SPECIES" : [
                                         ["HUMAN",   "Human"],
                                         ["RABBIT",   "Rabbit"],
                                         ["MOUSE",   "Mouse"],
                                         ["CHICKEN",   "Chicken"],
                                         ["DONKEY",   "Donkey"],  
                                         ["CAMEL",   "Camel"],   
                                         ["GOAT",   "Goat"],   
                                         ["SHEEP",   "Sheep"],   
                                         ["RAT",   "Rat"],   
                                         ["UNKWOWN",   "?"],   
                                         ["SEE_COMMENTS",   "see comments"],   
                                                                                                                        
                                      ],  
                              "AB_TYPE" : [
                                         ["IGG",   "IgG"],
                                         ["IGM",   "IgM"],
                                         ["IGY",   "IgY"],
                                         ["UNKNOWN",   "?"],
                                         ["SEE_COMMENTS",   "see comments"],  
                                                                                                                     
                                      ],                           
                              "AB_FORM" : [
                                         ["SERUM",   "serum"],
                                         ["AFFINITY_PURIFIED",   "affinity purified"],
                                         ["TISSUE_CULT_SUPERNATANT",   "tissue culture supernatant"],
                                         ["SEE_COMMENTS",   "see comments"],  
                                                                                                                     
                                      ],                                          
                              "AB_PRIMSEC" : [
                                         ["PRIMARY",   "primary"],
                                         ["SECONDAY",   "secondary"],
                                
                                      ], 
                              "AB_LOCATION" : [
                                         ["FOUR_DEG",   "4C"],
                                         ["MINUS_20",   "-20C"],
                                         ["MINUS_80",   "-80C"],                                         
                                         ["RT",   "RT"],                                         
                                         ["SEE_COMMENTS",   "see comments"],                                 
                                      ],                           
                              "CHEMICALS_LOCATION" : [
                                         ["CHEM_CABINET",   "Chemical cabinet"],
                                         ["CHEM_CABINET_E8",   "Chemical cabinet E8"],
                                         ["MINUS_20",   "-20C"],
                                         ["FOUR_DEG",   "4C"],
                                         ["MINUS_80",   "-80C"],
                                         ["SMALL_CHEMICALS",   "Small chemicals"],
                                         ["DYES",   "Dyes"],                                                                                  
                                         ["SEE_COMMENTS",   "see comments"],
                                
                                      ],  
                              "YEAST_BACKGROUND" : [
                                         ["UNKWNOWN",   "?"]
                                
                                      ],  
 
                              "OLIGO_CATEGORY" : [
                                         ["CHECKING",   "Checking"],
                                         ["CLONING",   "Cloning"],
                                         ["MUTAGENESIS",   "Mutagenesis"],
                                         ["PRINGLE",   "Pringle"],
                                         ["QPCR",   "qPCR"],  
                                         ["SEQUENCING",   "qPCR"],                                                                                  
                                      ],
                              "OLIGO_SENSE" : [
                                         ["SENSE",   "sense"],
                                         ["ANTISENSE",   "antisense"],
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
                             "PLASMID_VECTOR_TYPE" : [                                
                                                           ["BACTERIAL_EXPRESSION",   "Bacterial expression"],
                                                           ["YEAST_INTEGRATION",   "Yeast integration"],
                                                           ["PRINGLE",   "Pringle"],
                                                           ["MAMMALIAN_EXPRESSION",   "Mammalian expression"],
                                                           ["YEAST_CEN",   "Yeast CEN"],
                                                           ["YEAST_CENLESS",   "Yeast CEN-less with ARS"],
                                                           ["YEAST_2U",   "Yeast 2u"],
                                                           ["CLONING_INTERMEDIATE",   "Cloning intermediate"],
                                                           ["PLASMID",   "Plasmid"],
                                                           ["PHAGEMID",   "Phagemid"]
                                                           
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
                                             ["ALPHA_A",  "a/A"],
                                             ["A_A",  "A/A"],
                                             ["ALPHA_ALPHA",  "a/a"],
                                             ["UNKNOWN",  "?"],
                                        ]                         
                         
                          }