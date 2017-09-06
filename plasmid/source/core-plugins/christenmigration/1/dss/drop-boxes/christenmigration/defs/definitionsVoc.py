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
                          "LAB_MEMBERS": [
                                            ["Beat_Christen", "Beat Christen"],
                                            ["Matthias_Christen", "Matthias Christen"],
                                            ["Luca_Del_Medico", "Luca Del Medico"],
                                            ["Dario_Cerletti", "Dario Cerletti"],
                                            ["Flavia_Tschan", "Flavia Tschan"],
                                            ["Carlos_Flores_Tinoco", "Carlos Flores Tinoco"],
                                            ["Bo_Zhou", "Bo Zhou"],
                                            ["Aaron_Abraham", "Aaron Abraham"],
                                            ["Martha_Gerber", "Martha Gerber"],
                                            ["Flora_Mauch", "Flora Mauch"],
                                            ["Mike_Fero", "Mike Fero"]
                                        ],

                         "BC_STRAIN_RESISTANCE":   [
                                                ["km", "km"],
                                                ["tet", "tet"],
                                                ["amp", "amp"],                                                
                                                ["chlor", "chlor"],                                                
                                                ["hyg", "hyg"],                                                
                                                ["strep", "strep"],                                                
                                                ["spec", "spec"],                                                
                                                ["carb", "carb"],                                                
                                                ["gent", "gent"],                                                
                                                ["nal", "nal"],
                                                ["rif", "rif"],                                                
                                                ["apr", "apr"]                                                                                                
                                            ],
                 
                        "KEYWORDS": [
                                                ["Tn5", "Tn5"],
                                                ["Tn10", "Tn10"],
                                                ["T-POP", "T-POP"],
                                                ["mudK", "mudK"],
                                                ["mudJ", "mudJ"],
                                                ["mariner", "mariner"],
                                                ["promotor_fusion", "promotor fusion"],
                                                ["protein_fusion", "protein fusion"],
                                                ["gfp_fusion", "gfp fusion"],
                                                ["lacZ", "lacZ"],
                                                ["phoA", "phoA"],
                                                ["gus", "gus"],
                                                ["neo", "neo"],
                                                ["sacB", "sacB"],
                                                ["His_tag", "His tag"]
                                         ],
                                                  
                         "BC_STRAIN_SOURCE":   [
                                                ["SGSC", "SGSC"],
                                                ["Kelly_Hughes", "Kelly Hughes"],
                                                ["Patrick_Viollier", "Patrick Viollier"],
                                                ["Matthias", "Matthias"],
                                                ["Andreas_Gassmann", "Andreas Gassmann"]
                                            ],
                         
                         "BC_STRAIN_ORGANISM":   [
                                                ["DH10B", "DH10B"],
                                                ["SM10", "SM10"],
                                                ["D5a", "D5a"],
                                                ["BL21", "BL21"],
                                                ["S17", "S17"],
                                                ["LT2", "LT2"],
                                                ["ccr", "ccr"],
                                                ["Top10", "Top10"],
                                                ["Top10F", "Top10F'"],
                                                ["other_Eco", "other Eco"],
                                                ["Sme", "Sme"],
                                                ["phage", "phage"],
                                                ["other", "other"]
                                            ],
                         
                         "DNA_PURITY": [
                                                ["columne", "columne"],
                                                ["gel", "gel"],
                                                ["EtOH", "EtOH"],
 
                                            ],
                         
                         "PCR_POLYMERASE": [
                                                ["Taq", "Taq"],
                                                ["Pfu", "Pfu"],
                                                ["Pwo", "Pwo"],
                                                ["high_fidelity", "high fidelity"]

                                            ],
                         
                         
                         "DNA_RESISTANCE": [
                                                ["km", "km"],
                                                ["tet", "tet"],
                                                ["amp", "amp"],                                                
                                                ["cm", "cm"],                                                
                                                ["hyg", "hyg"],                                                
                                                ["strep", "strep"],                                                
                                                ["spec", "spec"],                                                
                                                ["gent", "gent"]                                                

                                            ],
                         

                         "DNA_TYP": [
                                                ["gen_DNA", "gen DNA"],
                                                ["PCR", "PCR"],
                                                ["plasmid", "plasmid"],
                                                ["insert", "insert"],
                                                ["cosmid", "cosmid"]

                                            ],

                       
                         "DNA_MODIFICATIONS": [
                                                ["Methylated", "Methylated"],
                                                ["digested", "digested"],
                                                ["SAPed", "SAPed"]
 
                                            ]

                         
                          }