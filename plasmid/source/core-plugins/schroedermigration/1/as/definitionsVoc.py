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
                                      ["Timm_Schroeder","Timm Schroeder"],
                                      ["Hanna_Eilken","Hanna Eilken"],
                                      ["Masae_Sato","Masae Sato"],
                                      ["Thomas_Magg","Thomas Magg"],
                                      ["Andrea_Hermann","Andrea Hermann"],
                                      ["Heike_Partenheimer","Heike Partenheimer"],
                                      ["ISF-H","ISF-H"],
                                      ["Angelika_Roth","Angelika Roth"],
                                      ["Inge_Baas","Inge Baas"],
                                      ["Erin_Drew","Erin Drew"],
                                      ["Ingo_Burtscher_Hanna_Eilken","Ingo Burtscher/Hanna Eilken"],
                                      ["Matthias_Munding","Matthias Munding"],
                                      ["Michael_Rieger","Michael Rieger"],
                                      ["Masaki_Shigeta","Masaki Shigeta"],
                                      ["Annemieke_IJpenberg","Annemieke IJpenberg"],
                                      ["Max_Endele","Max Endele"],
                                      ["Philipp_Hoppe","Philipp Hoppe"],
                                      ["Adam_Filipczyk","Adam Filipczyk"],
                                      ["Simon_Hastreiter","Simon Hastreiter"],
                                      ["Joost_van_den_Berg","Joost van den Berg"],
                                      ["Konstantinos_Kokkaliaris","Konstantinos Kokkaliaris"],
                                      ["Konstantinos","Konstantinos"],
                                      ["Nadine_Moritz","Nadine Moritz"],
                                      ["Christian_Raithel","Christian Raithel"],
                                      ["Martin_Etzrodt","Martin Etzrodt"],
                                      ["Andreas_Reimann","Andreas Reimann"],
                                      ["Paul_Bourgine","Paul Bourgine"],
                                      ["Laura_Skylaki","Laura Skylaki"],
                                      ["Ren_Shimamoto","Ren Shimamoto"],
                                      ["Weijia_Wang","Weijia Wang"],
                                      ["RS","RS"]
                                    ],

                          "SELECTION" : [
                                ["NEOMYCIN_G418",  "Neomycin/G418"],
                                ["PUROMYCIN",  "Puromycin"],
                                ["HYGROMYCIN",  "Hygromycin"],
                                ["BLASTICIDIN",  "Blasticidin"],
                                ["UNKNOWN",  "?"],                                
                                ["FACS",  "FACS"] 
                                    ],
                          "CO2" : [
                                ["5",  "5"],
                                ["7.5",  "7.5"]
                                    ],
                          "GROWTH_TEMPERATURE" : [
                                ["37",  "37"],
                                ["32",  "32"],
                                ["28",  "28"],                                
                                ["33",  "33"]
                                    ],
                          "MEDIUM" : [
                                ["UNKNOWN","?"],
                                ["alphaMEM","alphaMEM"],
                                ["aMEM","aMEM"],
                                ["aMEM_FGF2","aMEM + FGF2"],
                                ["aMEM_CM_FGF2","aMEM CM + FGF2"],
                                ["DMEM","DMEM"],
                                ["DMEM_high_glucose","DMEM (high glucose)"],
                                ["DMEM_high_Glucose_LGlutamin_Pyruvate","DMEM high Glucose + L-Glutamin + Pyruvate"],
                                ["DMEM_IMDM_or","DMEM or IMDM or .... (not critical)"],
                                ["DMSO_FBS","DMSO + FBS"],
                                ["EB5_maintanance_medium","EB5 mainanance medium"],
                                ["EB5_maintanance_medium","EB5 maintanance medium"],
                                ["EB5_maintanance","EB5 maintanance"],
                                ["EB5_maintanance","EB5 maintanence"],
                                ["ES_cell_medium","ES cell medium"],
                                ["ES_cell_medium_FCS11_LIF","ES cell medium (FCS11, LIF)"],
                                ["ES_cell_medium_FSC11_LIF","ES cell medium (FSC11, LIF)"],
                                ["Glasgow_Minimum_Gibco","Glasgow Minimum (Gibco)"],
                                ["Glasgow_Minimum_Gibco","Glasgow Minimum Gibco"],
                                ["Glasgow_Minimum_Gibco","Glasgow Minimun Gibco"],
                                ["high_glucose_DMEM","high glucose DMEM"],
                                ["high_glucose_DMEM","high-glucose DMEM"],
                                ["IMDM_10DMSO","IMDM / 10% DMSO"],
                                ["IMDM_310-320mosm_kg","IMDM 310-320mosm/kg"],
                                ["Knock-In_Gibco","Knock-In Gibco"],
                                ["Knock-out_Medium_Gibco","Knock-out  Medium  (Gibco)"],
                                ["Knock-Out_DMEM","Knock-Out DMEM"],
                                ["maintenance_medium","maintenance medium"],
                                ["MEGM_Mammary_Epithelial_BulletKit","MEGM Mammary Epithelial BulletKit"],
                                ["RPMI_1640","RPMI 1640"],
                                ["RPMI_wo_Phenol_Red","RPMI w/o Phenol Red"],
                                ["SFEM","SFEM"],
                                ["SL_2i_Or_N2B27_2i","SL+2i or N2B27+2i"],
                                ["without_gelatine_and_feeder_knockout_DMEM_15FCS","without gelatine and feeder, knockout DMEM, 15% FCS, 2mM Gln, NEAA, LIF, PenStrep, 1,5x10-4 MTG"],
                                    ], 
                          "CELL_OBTAINED_FROM" : [
                                ["Elaine_Spooncer","Elaine Spooncer"],
                                ["Ursula_Just","Ursula Just"],
                                ["Ralf_Mozikat_GSF_Munich","Ralf Mozikat, GSF, Munich"],
                                ["Kuroda_K","Kuroda K"],
                                ["Hueltner_GSF","Hueltner? GSF?"],
                                ["ShinIchi_Nishikawa","ShinIchi Nishikawa"],
                                ["Mulligan_R","Mulligan R"],
                                ["Milner_L","Milner L"],
                                ["Kempkes_B","Kempkes B"],
                                ["Kempkes_B_Weinmaster_G","Kempkes B, Weinmaster G"],
                                ["Honjo_T","Honjo T"],
                                ["Nolan","Nolan"],
                                ["Ursula_Just_Hamburg","Ursula Just, ? Hamburg"],
                                ["Honjo_Tasuku","Honjo Tasuku"],
                                ["Dan_Tenen","Dan Tenen"],
                                ["Fumio_Matsuzaki","Fumio Matsuzaki"],
                                ["Sakurai_Era","Sakurai / Era"],
                                ["Igor_Samochvalov","Igor Samochvalov"],
                                ["Hitoshi_Niwa","Hitoshi Niwa"],
                                ["Sakurai_in_Kobe","Sakurai in Kobe"],
                                ["Minetaro_Ogawa_Kumamoto_University","Minetaro Ogawa, Kumamoto University"],
                                ["Gerald_de_Haan","Gerald de Haan"],
                                ["Carmen_Ebel_AVM","Carmen Ebel AVM"],
                                ["Eric_Bouhassira","Eric Bouhassira"],
                                ["Masui","Masui"],
                                ["Heikos_group","Heikos group"],
                                ["Kateri_Moore","Kateri Moore"],
                                ["Ralf_Kuhn","Ralf Kühn"],
                                ["Ralph_Kuhn","Ralph Kühn"],
                                ["Inge_Baas","Inge Baas"],
                                ["AG_Ruhland","AG Ruhland"],
                                ["Igor_Samokhvalov","Igor Samokhvalov"],
                                ["p11_stock_AH","p11 stock (AH)"],
                                ["Aileen_Smith","Aileen Smith"],
                                ["Hans_Scholer","Hans Schöler"],
                                ["Dietrich_Lesinski","Dietrich Lesinski"],
                                ["Ian_Chambers","Ian Chambers"],
                                ["Lemischka","Lemischka"],
                                ["EUCOMM","EUCOMM"],
                                ["Andrea_Beyerle_ILBD","Andrea Beyerle (ILBD)"],
                                ["Konstantinos_Anastassiadis","Konstantinos Anastassiadis"],
                                ["Adam_Filipczyk","Adam Filipczyk"],
                                ["Hideo_Yagita","Hideo Yagita"],
                                ["Robert_Oostendorp","Robert Oostendorp"],
                                ["Thomas_Graf","Thomas Graf"],
                                ["Andrew_Elefanty_Ed_Stanley","Andrew Elefanty & Ed Stanley"],
                                ["Clontech","Clontech"],
                                ["Adam_Filipczyk_Konstantinos_Anastassiadis","Adam Filipczyk, Konstantinos Anastassiadis"],
                                ["Klinikum_Rechts_der_Isar","Klinikum Rechts der Isar"],
                                ["ATCC","ATCC"],
                                ["Daisuke_Sugiyama","Daisuke Sugiyama"],
                                ["Alessio_Zippo","Alessio Zippo"],
                                ["Vyas_Paresh_Oxford","Vyas Paresh, Oxford"],
                                ["Ivan_Martin","Ivan Martin"],
                                ["Paul_Bourgine","Paul Bourgine"],
                                ["AR","AR"],
                                ["Gadue_Paul","Gadue, Paul"],
                                ["Antonius_Rolink","Antonius Rolink"],
                                ["Eszter_Posfai_Janet_Rossant","Eszter Posfai/Janet Rossant"],
                                ["Cell_Biolabs_inc.","Cell Biolabs, inc."],
                                ["Simon_Hastreiter","Simon Hastreiter"],
                                ["CiRA","CiRA"],
                                ["Gibco","Gibco"],
                                ["PrimCell","PrimCell"],
                                ["E_Friman","E. Friman"],
                                ["CiRA_Yamada_lab","CiRA, Yamada lab"]
                                    ],  
                          "CELL_SPECIES" : [
                                ["HUMAN",  "human"],
                                ["MOUSE",  "mouse"],
                                ["MOUSE_MAYBE",  "mouse ?"]
                                    ],  
                          "CONC_UNITS" : [
                                ["MICROG_MICROL",  "�g/�l"],
                                ["MG_ML",  "mgl/ml"],
                                ["UG_ML",  "ug/ml"]                               
                                    ],  
                        "FREEZER" : [
                                ["N2",  "N2"],
                                ["MINUS140_R5.38",  "-140°C R5.38"],
                                    ],
                         "STORAGE_BOX_SIZE" : [
                                ["10X10", "10x10"]
                            ],
                          }