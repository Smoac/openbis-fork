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
                                            ["TANIA_ROBERTS", "Tania Roberts"],
                                            ["Andreas_Meyer", "Andreas Meyer"],
                                            ["Sonja_Billerbeck", "Sonja Billerbeck"],
                                            ["Marcel_Walser", "Marcel Walser"],
                                            ["Angelika_Ress", "Angelika Ress"],
                                            ["Andreas_Bosshart", "Andreas Bosshart"],
                                            ["Alex_Nilsson", "Alex Nilsson"],
                                            ["Matthias_Bujara", "Matthias Bujara"],
                                            ["Michael_Eichenberger", "Michael Eichenberger"],
                                            ["Jaroslav_Michalko", "Jaroslav Michalko"],
                                            ["Sven_Panke", "Sven Panke"],
                                            ["Giorgia_Valesia", "Giorgia Valesia"],
                                            ["Stefan", "Stefan"],
                                            ["Sonja_Billerback", "Sonja Billerback"],
                                            ["Johannes_Haerle", "Johannes Haerle"],
                                            ["Alexandra_Schulz", "Alexandra Schulz"],
                                            ["Diana_Ostos_Rangel", "Diana Ostos Rangel"],
                                            ["Sabine_Oesterle", "Sabine Österle"],
                                            ["Katja_Becker", "Katja Becker"]

                                        ],
                         "STORAGE_NAMES":   [
                                                ["BENCH", "Bench"],
                                                ["DEFAULT_STORAGE", "Default Storage"]
                                            ],
                         
                         
                        "STORAGE_BOX_SIZE": [
                                                ["1X1", "1 x 1 box"],
                                                ["4X4", "4 x 4 box"],
                                                ["20X40", "20 x 40 box"]
                                        ]                         

                         
                          }