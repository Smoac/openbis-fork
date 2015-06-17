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



                       #      "CONCENTRATION_FACS" : [
                       #                      ["1_TO_10", "1 : 10"],
                       #                      ["1_TO_20", "1 : 20"],
                       #                      ["1_TO_30", "1 : 30"],
                       #                      ["1_TO_40", "1 : 40"],
                       #                      ["1_TO_50", "1 : 50"],
                       #                      ["1_TO_100", "1 : 100"],
                       #                      ["1_TO_200", "1 : 200"],
                       #                      ["1_TO_500", "1 : 500"],
                       #                      ["1_TO_1000", "1 : 1000"],
                       #                      ["1_TO_2000", "1 : 2000"],
                       #                      ["1_TO_3000", "1 : 3000"],
                       #                      ["NOT_TESTED", "not tested"],
                       #                      ["NOT_WORKING", "not working"]
                       #              ],
                       #      "CONCENTRATION_IP" : [
                       #                      ["1UL", "1 �l"],
                       #                      ["5UL", "5 �l"],
                       #                      ["10UL", "10 �l"],
                       #                      ["15UL", "15 �l"],
                       #                      ["20UL", "20 �l"],
                       #                      ["NOT_TESTED", "not tested"],
                       #                      ["NOT_WORKING", "not working"]
                       #              ],
                       #      "CONCENTRATION_WESTERN" : [
                       #                          ["1_TO_100", "1 : 100"],
                       #                          ["1_TO_500", "1 : 500"],
                       #                          ["1_TO_1000", "1 : 1000"],
                       #                          ["1_TO_2000", "1 : 2000"],
                       #                          ["1_TO_3000", "1 : 3000"],
                       #                          ["1_TO_5000", "1 : 5000"],
                       #                          ["1_TO_10000", "1 : 10000"],
                       #                          ["1_TO_20000", "1 : 20000"],
                       #                          ["1_TO_25000", "1 : 25000"],
                       #                          ["NOT_TESTED", "not tested"],
                       #                          ["NOT_WORKING", "not working"]
                       #              ],
                       #    "CURRENT_LAB_MEMBERS" : [
                       #                      ["CAROLINE_ZBINDEN", "Caroline Zbinden"],
                       #                      ["FRANK_VAN_DROGEN", "Frank Van Drogen"],
                       #                      ["INGRID_STOFFEL-STUDER", "Ingrid Stoffel-Studer"],
                       #                      ["MATTHIAS_PETER", "Matthias Peter"],
                       #                      ["REINHARD_DECHANT", "Reinhard Dechant"],
                       #                      ["WOJCIECH_PIWKO", "Wojciech Piwko"],
                       #                      ["JOCHEN_BECK", "Jochen Beck"]
                       #              ],
                       #    "FIX" : [
                       #          ["1H-RT", "1h, RT"],
                       #          ["PTEMF", "PTEMF"],
                       #          ["PFA", "PFA"],
                       #          ["MEOH", "MeOH"]
                       #              ],
                       #    "LABEL" : [
                       #          ["HRP", "hrp"],
                       #          ["AP", "ap"],
                       #          ["FITC", "fitc"],
                       #          ["CY3", "Cy3"],
                       #          ["ALEXA647", "Alexa 647"]
                       #              ],
                       #    "ANTIBODY_LOCATION" : [
                       #              ["SMALL_PETER_LAB", "Small Peter Lab"],
                       #              ["BIG_PETER_LAB", "Big Peter Lab"],
                       #              ["NEW_PETER_LAB", "New Peter Lab"],
                       #              ["MINUS20_C-FLOOR", "-20C C-Floor"]
                       #              ],
                       #    "MONO-POLYCLONAL" : [
                       #                      ["MONOCLONAL",  "Monoclonal"],
                       #                      ["POLYCLONAL",  "Polyclonal"],
                       #                      ["UNKNOWN", "Unknown"]
                       #              ],
                       #    "SOURCE" : [
                       #          ["MOUSE",  "Mouse"],
                       #          ["RABBIT",  "Rabbit"],
                       #          ["HAMSTER",  "Hamster"],
                       #          ["GOAT",  "Goat"],
                       #          ["RAT",  "Rat"],
                       #          ["CHICKEN",  "Chicken"],
                       #          ["SHEEP",  "Sheep"],
                       #          ["HUMAN",  "Human"],
                       #          ["GUINEA_PIG",  "Guinea pig"],
                       #          ["DONEKY",  "Donkey"],
                       #          ["UNKNOWN",  "Unknown"]
                       #              ],
                       #    "CELL_LOCATION" : [
                       #          ["FRIDGE",  "fridge"],
                       #          ["FREEZER",  "freezer"],
                       #          ["REFCO",  "refco"],
                       #          ["TANK",  "tank"]
                       #              ],
                       #    "SELECTION" : [
                       #          ["NEOMYCIN_G418",  "Neomycin/G418"],
                       #          ["HYGROMYCIN",  "Hygromycin"],
                       #          ["MPA",  "MPA"],
                       #          ["G418",  "G418"],
                       #          ["PUROMYCIN",  "puromycin"],
                       #          ["BLASTICIDIN",  "Blasticidin"],
                       #          ["ZEOCIN",  "Zeocin"] 
                       #              ],
                       #    "CO2" : [
                       #          ["5",  "5"],
                       #          ["7.5",  "7.5"]
                       #              ],
                       #    "GROWTH_TEMPERATURE" : [
                       #          ["37",  "37"],
                       #          ["28",  "28"],
                       #          ["34",  "34"]
                       #              ],
                       #    "MEDIUM" : [
                       #          ["RPMI1640",  "RPMI 1640"],
                       #          ["ISCOVES",  "Iscoves"],
                       #          ["DMEM",  "DMEM"],
                       #          ["LB",  "LB"],
                       #          ["DMEM_NUTRIENT",  "DMEM nutrient mixture F-12 HAM [D8437]"]
                       #              ], 
                       #    "CELL_OBTAINED_FROM" : [
                       #          ["FREIBURG",  "Freiburg"],
                       #          ["ATCC",  "ATCC"],
                       #          ["DANIEL_GERLICH",  "Daniel Gerlich"],
                       #          ["ERICH_NIGG",  "Erich Nigg"],
                       #          ["KREK",  "Krek"],
                       #          ["TONY_HEYMAN",  "Tony Heyman"],
                       #          ["THOMAS-WILD_ULRIKE-KUTAY",  "Thomas Wild/Ulrike Kutay"],
                       #          ["JAN_MICHAEL_PETERS",  "Jan-Michael Peters"],
                       #          ["M_BRANDEIS",  "M Brandeis"],
                       #          ["ARI_HELENIUS",  "Ari Helenius"],
                       #          ["ROMEO_RICCI",  "Romeo Ricci"]
                       #              ],  
                       #    "CELL_SPECIES" : [
                       #          ["HUMAN",  "human"],
                       #          ["MOUSE",  "mouse"],
                       #          ["RAT",  "rat"],
                       #          ["CHINESE_HAMSTER",  "chines hamster"],
                       #          ["HYBRIDOMA",  "hybridoma (human & mouse)"]                                
                       #              ],  
                       #    "CONC_UNITS" : [
                       #          ["MICROG_MICROL",  "�g/�l"],
                       #          ["MG_ML",  "mgl/ml"],
                       #          ["UG_ML",  "ug/ml"]                               
                       #              ],  
                       #    "STRAIN_SOURCE" : [
                       #          ["F_CHANG", "F. Chang"],
                       #          ["J_CHANT", "J. Chant"],
                       #          ["R_DESHAIES", "R. Deshaies"],
                       #          ["E_ELION", "E. Elion"],
                       #          ["P_HIETER", "P. Hieter"],
                       #          ["I_HERSKOWITZ", "I. Herskowitz"],
                       #          ["A_NEIMAN", "A. Neiman"],
                       #          ["P_SORGER", "P. Sorger"],
                       #          ["T_STEARNS", "T. Stearns"],
                       #          ["MARC_BONDEL", "Marc Bondel"],
                       #          ["G_SUMARA", "G. Sumara"],
                       #          ["MICHAEL_ALTMANN", "Michael Altmann"],
                       #          ["HETEROZYGOUS_DIPLOID_COLLECTION", "Heterozygous diploid collection"],
                       #          ["ANNE-CHRISTINE_BUTTY", "Anne-Christine Butty"],
                       #          ["ANNE_PAQUELET", "Anne Paquelet"],
                       #          ["YAGMUR-TURGAY_KUTAY-GROUP", "Yagmur Turgay - Kutay group"],
                       #          ["BARBAR_CATARIN", "Barbar Catarin"],
                       #          ["FRANK_VAN-DROGEN", "Frank van Drogen"],
                       #          ["HEMMO_MEYER", "Hemmo Meyer"],
                       #          ["JEAN-MARC_GALLAND", "Jean-Marc Galland"],
                       #          ["JEFF_PARVIN", "Jeff Parvin"],
                       #          ["MARIE-PIERRE_GULLI", "Marie-Pierre Gulli"],
                       #          ["MIKIKO_TAKAHASHI", "Mikiko Takahashi"],
                       #          ["SANDRA_HENCHOZ", "Sandra Henchoz"],
                       #          ["MALIKA_JAQUENOUD", "Malika Jaquenoud"],
                       #          ["GERDA_JORRITSMA", "Gerda Jorritsma"],
                       #          ["CELINE_LAFOURCADE", "Céline Lafourcade"],
                       #          ["CELINE_LONGARETTI", "Céline Longaretti"],
                       #          ["BRIAN_LUKE", "Brian Luke"],
                       #          ["GUY_NIEDERHAUSER", "Guy Niederhäuser"],
                       #          ["NICOLAS_PAGE", "Nicolas Pag�"],
                       #          ["NATHALIE_PERRINJAQUET", "Nathalie Perrinjaquet"],
                       #          ["MATTHIAS_PETER", "Matthias Peter"],
                       #          ["AUDREY_PETIT", "Audrey Petit"],
                       #          ["LIONEL_PINTARD", "Lionel Pintard"],
                       #          ["YUKIKO_SHIMADA", "Yukiko Shimada"],
                       #          ["PHILIPPE_WIGET", "Philippe Wiget"],
                       #          ["VOLKER_STUCKE", "Volker Stucke"],
                       #          ["KATRIN_PETER", "Katrin Peter"],
                       #          ["MARC_BLONDEL", "marc blondel"],
                       #          ["M_FUNK", "M. Funk"],
                       #          ["ATTILA_BECSKEI", "Attila Becskei"],
                       #          ["GWENAEL_RABUT", "Gwenael Rabut"],
                       #          ["PHILIP_JERMANN", "Philip Jermann"],
                       #          ["P_PRYCIAK", "P.Pryciak"],
                       #          ["LINDA_HICKE", "Linda Hicke"],
                       #          ["RZPD", "RZPD"],
                       #          ["OPENBIO", "OpenBio"],
                       #          ["F_POSAS", "F. Posas"],
                       #          ["HAPLOID_COLLECTION", "Haploid collection"],
                       #          ["MIRIAM_BORTFELD", "Miriam Bortfeld"],
                       #          ["EUROSCARF", "Euroscarf"],
                       #          ["TS_COLLECTION", "ts collection"],
                       #          ["CONZELMANN_LAB", "Conzelmann lab"],
                       #          ["SEBASTIAN_LEIDEL", "Sebastian Leidel"],
                       #          ["ANNA_DEPLAZES", "Anna Deplazes"],
                       #          ["BIBOU", "Bibou"],
                       #          ["MARC_SOHRMANN", "Marc sohrmann"]
                       #              ],
                       #    "SELECTION_YEAST" : [
                       #          ["NONE",  "None"],
                       #          ["URA",  "Ura"],
                       #          ["LEU",  "Leu"], 
                       #          ["TRP",  "Trp"], 
                       #          ["HIS",  "His"], 
                       #          ["VIABILITY",  "Viability"], 
                       #          ["NAT",  "nat"], 
                       #          ["KAN",  "kan"], 
                       #          ["KAN_NAT",  "kan + nat"], 
                       #          ["HIS_KAN",  "HIS + kan"], 
                       #          ["HIS_KAN_NAT",  "HIS + kan + nat"], 
                       #          ["HIS",  "HIS"], 
                       #          ["LEU_NAT",  "leu + nat"], 
                       #          ["ADE2",  "ade2"]
                       #              ],  
                       #   "DERIVATION" : [
                       #          ["GAMMA_INTEG",  "gamma integ"],
                       #          ["DELTA_INTEG",  "� integ"],
                       #          ["5FAO_LOOPOUT",  "5-FAO loopout"],
                       #          ["PLASMID_SHUFFLE",  "plasmid shuffle"],
                       #          ["TETRAD",  "tetrad"],
                       #          ["LOOP-IN_LOOP-OUT",  "loop-in/loop-out"],
                       #          ["GALPHO",  "GalpHO mating type switch"],
                       #          ["DELETION",  "Deletion"],
                       #          ["INTEGRATION",  "Integration"]
                       #              ],
                       #   "STRAIN_BACKGROUND" : [
                       #          ["W303",  "W303"],
                       #          ["K699",  "K699"],
                       #          ["K700",  "K700"],
                       #          ["S288C",  "S288C"],
                       #          ["1278",  "*1278*"],
                       #          ["EG123",  "EG123"],
                       #          ["DF5",  "DF5"],
                       #          ["SIGMA",  "Sigma"],
                       #          ["UNKNOWN",  "Don't know"],
                       #          ["BY7092",  "BY7092"],
                       #          ["BY5565",  "BY5565"],
                       #          ["BY4741",  "BY4741"]
                       #              ],
                       #  "CIR" : [
                       #          ["CIR_PLUS",  "cir+"],
                       #          ["CIR_DEG",  "cir�"],
                       #              ],
                       #  "GAL" : [
                       #          ["PLUS",  "Plus"],
                       #          ["PLUS_PLUS",  "Plus/Plus"],
                       #          ["GAL1",  "gal1"],
                       #          ["GAL1_GAL1",  "gal1/gal1"],      
                       #          ["GAL2",  "gal2"],                                 
                       #          ["GAL2_GAL2",  "gal2/gal2"],                                                         
                       #              ],
                       #  "MAT" : [
                       #          ["AT",  "@"],
                       #          ["A",  "a"],
                       #          ["ALPHA",  "alpha"],
                       #          ["DIPLOID",  "diploid"],      
                       #          ["UNKNOWN",  "unknown"]                                                      
                       #              ],
                       #  "OLIGO_ORGANISM" : [
                       #          ["BMV",  "BMV"],
                       #          ["HUMAN",  "human"],
                       #          ["MOUSE",  "mouse"],
                       #          ["RAT",  "rat"],      
                       #          ["XENOPUS",  "xenopus"],
                       #          ["DROSOPHILA",  "drosophila"],
                       #          ["YEAST",  "yeast"],
                       #          ["ECOLI",  "E.coli"],
                       #          ["SYNTHETIC",  "synthetic"],
                       #          ["HIV1",  "HIV-1"],
                       #          ["CELEGANS",  "C.elegans"],
                       #          ["MYCOPLASMA",  "mycoplasma"]
                       #              ],
                       #  "OLIGO_GRADE" : [
                       #          ["DESALTED",  "desalted"],
                       #          ["HPLC",  "hplc"],
                       #          ["PURITY",  "purity"],
                       #          ["UNKNOWN",  "?"]      
                       #              ],     
                       #  "PRIMER_POSITION" : [
                       #          ["FORWARD",  "forward"],
                       #          ["REVERSE",  "reverse"]
                       #              ],  
                       #  "OLIGO_COMPANY" : [
                       #          ["MWG",  "MWG"],
                       #          ["BIG",  "BIG"],
                       #          ["PHARMACIA",  "Pharmacia"],
                       #          ["IGLOI",  "Igloi"],      
                       #          ["INVITROGEN",  "invitrogen"],
                       #          ["MICROSYNTH",  "microsynth"]                                                     
                       #              ],                                       
                       #  "OLIGO_USAGE" : [
                       #          ["PCR",  "pcr"],
                       #          ["SEQUENCING",  "sequencing"],
                       #          ["HYBRIDIZE",  "hybridize"],
                       #          ["MUTAGENESIS",  "mutagenesis"],      
                       #          ["RT-PCR",  "Real-Time PCR"]
                       #              ],
                       #  "RESTRICTION" : [
                       #          ["AATII",  "AatII"],
                       #          ["ACCI",  "AccI"],
                       #          ["ACII",  "AciI"],
                       #          ["AFLII",  "AflII"],
                       #          ["AFLIII",  "AflIII"],
                       #          ["AGEI",  "AgeI"],
                       #          ["ALUI",  "AluI"],
                       #          ["ALWI",  "AlwI"],
                       #          ["ALWNI",  "AlwNI"],
                       #          ["APAI",  "ApaI"],
                       #          ["APALI",  "ApaLI"],
                       #          ["APOI",  "ApoI"],
                       #          ["ASCI",  "AscI"],
                       #          ["ASEI",  "AseI"],
                       #          ["AVAI",  "AvaI"],
                       #          ["AVAII",  "AvaII"],
                       #          ["AVRII",  "AvrII"],
                       #          ["BAMHI",  "BamHI"],
                       #          ["BANI",  "BanI"],
                       #          ["BANII",  "BanII"],
                       #          ["BBSI",  "BbsI"],
                       #          ["BBVI",  "BbvI"],
                       #          ["BCGI-1",  "BcgI-1"],
                       #          ["BCGI-2",  "BcgI-2"],
                       #          ["BCLI",  "BclI"],
                       #          ["BFAI",  "BfaI"],
                       #          ["BGLI",  "BglI"],
                       #          ["BGLII",  "BglII"],
                       #          ["BLPI",  "BlpI"],
                       #          ["BPMI",  "BpmI"],
                       #          ["BSAAI",  "BsaAI"],
                       #          ["BSABI",  "BsaBI"],
                       #          ["BSAHI",  "BsaHI"],
                       #          ["BSAI",  "BsaI"],
                       #          ["BSAJI",  "BsaJI"],
                       #          ["BSAWI",  "BsaWI"],
                       #          ["BSERI",  "BseRI"],
                       #          ["BSGI",  "BsgI"],
                       #          ["BSIEI",  "BsiEI"],
                       #          ["BSIHKAI",  "BsiHKAI"],
                       #          ["BSIWI",  "BsiWI"],
                       #          ["BSLI",  "BslI"],
                       #          ["BSMAI",  "BsmAI"],
                       #          ["BSMBI",  "BsmBI"],
                       #          ["BSMFI",  "BsmFI"],
                       #          ["BSMI",  "BsmI"],
                       #          ["BSOFI",  "BsoFI"],
                       #          ["BSP1286I",  "Bsp1286I"],
                       #          ["BSPEI",  "BspEI"],
                       #          ["BSPHI",  "BspHI"],
                       #          ["BSPMI",  "BspMI"],
                       #          ["BSRBI",  "BsrBI"],
                       #          ["BSRDI",  "BsrDI"],
                       #          ["BSRFI",  "BsrFI"],
                       #          ["BSRGI",  "BsrGI"],
                       #          ["BSRI",  "BsrI"],
                       #          ["BSSHII",  "BssHII"],
                       #          ["BST1107I",  "Bst1107I"],
                       #          ["BSTBI",  "BstBI"],
                       #          ["BSTEII",  "BstEII"],
                       #          ["BSTNI",  "BstNI"],
                       #          ["BSTUI",  "BstUI"],
                       #          ["BSTXI",  "BstXI"],
                       #          ["BSTYI",  "BstYI"],
                       #          ["BSU36I",  "Bsu36I"],
                       #          ["CAC8I",  "Cac8I"],
                       #          ["CLAI",  "ClaI"],
                       #          ["CVIJI",  "CviJI"],
                       #          ["DDEI",  "DdeI"],
                       #          ["DPNI",  "DpnI"],
                       #          ["DRAI",  "DraI"],
                       #          ["DRAIII",  "DraIII"],
                       #          ["DRDI",  "DrdI"],
                       #          ["DSAI",  "DsaI"],
                       #          ["EAEI",  "EaeI"],
                       #          ["EAGI",  "EagI"],
                       #          ["EAM1105I",  "Eam1105I"],
                       #          ["EARI",  "EarI"],
                       #          ["ECO47III",  "Eco47III"],
                       #          ["ECO57I",  "Eco57I"],
                       #          ["ECONI",  "EcoNI"],
                       #          ["ECOO109I",  "EcoO109I"],
                       #          ["ECORI",  "EcoRI"],
                       #          ["ECORV",  "EcoRV"],
                       #          ["FOKI",  "FokI"],
                       #          ["FSEI",  "FseI"],
                       #          ["FSPI",  "FspI"],
                       #          ["HAEII",  "HaeII"],
                       #          ["HAEIII",  "HaeIII"],
                       #          ["HGAI",  "HgaI"],
                       #          ["HHAI",  "HhaI"],
                       #          ["HINCII",  "HincII"],
                       #          ["HINDIII",  "HindIII"],
                       #          ["HINFI",  "HinfI"],
                       #          ["HPAI",  "HpaI"],
                       #          ["HPAII",  "HpaII"],
                       #          ["HPHI",  "HphI"],
                       #          ["KPNI",  "KpnI"],
                       #          ["MAEII",  "MaeII"],
                       #          ["MAEIII",  "MaeIII"],
                       #          ["MBOI",  "MboI"],
                       #          ["MBOII",  "MboII"],
                       #          ["MLUI",  "MluI"],
                       #          ["MNLI",  "MnlI"],
                       #          ["MSCI",  "MscI"],
                       #          ["MSEI",  "MseI"],
                       #          ["MSLI",  "MslI"],
                       #          ["MSPA1I",  "MspA1I"],
                       #          ["MUNI",  "MunI"],
                       #          ["MWOI",  "MwoI"],
                       #          ["NAEI",  "NaeI"],
                       #          ["NARI",  "NarI"],
                       #          ["NCII",  "NciI"],
                       #          ["NCOI",  "NcoI"],
                       #          ["NDEI",  "NdeI"],
                       #          ["NHEI",  "NheI"],
                       #          ["NLAIII",  "NlaIII"],
                       #          ["NLAIV",  "NlaIV"],
                       #          ["NOTI",  "NotI"],
                       #          ["NRUI",  "NruI"],
                       #          ["NSII",  "NsiI"],
                       #          ["NSPI",  "NspI"],
                       #          ["PACI",  "PacI"],
                       #          ["PFLMI",  "PflMI"],
                       #          ["PLEI",  "PleI"],
                       #          ["PMEI",  "PmeI"],
                       #          ["PMLI",  "PmlI"],
                       #          ["PPUMI",  "PpuMI"],
                       #          ["PSHAI",  "PshAI"],
                       #          ["PSP1406I",  "Psp1406I"],
                       #          ["PSTI",  "PstI"],
                       #          ["PVUI",  "PvuI"],
                       #          ["PVUII",  "PvuII"],
                       #          ["RSAI",  "RsaI"],
                       #          ["RSRII",  "RsrII"],
                       #          ["SACI",  "SacI"],
                       #          ["SACII",  "SacII"],
                       #          ["SALI",  "SalI"],
                       #          ["SAPI",  "SapI"],
                       #          ["SAU96I",  "Sau96I"],
                       #          ["SCAI",  "ScaI"],
                       #          ["SCRFI",  "ScrFI"],
                       #          ["SEXAI",  "SexAI"],
                       #          ["SFANI",  "SfaNI"],
                       #          ["SFCI",  "SfcI"],
                       #          ["SFII",  "SfiI"],
                       #          ["SGFI",  "SgfI"],
                       #          ["SGRAI",  "SgrAI"],
                       #          ["SMAI",  "SmaI"],
                       #          ["SNABI",  "SnaBI"],
                       #          ["SPEI",  "SpeI"],
                       #          ["SPHI",  "SphI"],
                       #          ["SRFI",  "SrfI"],
                       #          ["SSE8387I",  "Sse8387I"],
                       #          ["SSPI",  "SspI"],
                       #          ["STUI",  "StuI"],
                       #          ["STYI",  "StyI"],
                       #          ["SWAI",  "SwaI"],
                       #          ["TAQI",  "TaqI"],
                       #          ["TFII",  "TfiI"],
                       #          ["TSP45I",  "Tsp45I"],
                       #          ["TSP509I",  "Tsp509I"],
                       #          ["TTH111I",  "Tth111I"],
                       #          ["XBAI",  "XbaI"],
                       #          ["XCMI",  "XcmI"],
                       #          ["XHOI",  "XhoI"],
                       #          ["XMNI",  "XmnI"],
                       #          ["SACI_NHEI",  "sacI NheI"],
                       #          ["XMAI",  "XmaI"],
                       #          ["BAMHI",  "BamHI"],
                       #          ["CLAI",  "ClaI"],
                       #          ["PACI",  "PacI"],
                       #          ["NOTI",  "NotI"],
                       #          ["SALI",  "SalI"],
                       #          ["XHOI",  "XhoI"],
                       #          ["NONE",  "none"]
                       #      ],
                       #  "BACKBONE" : [
                       #          ["PCDNA3",  "pCDNA3"],
                       #          ["PABES-NEO2000",  "pABES-neo 2000"],    
                       #          ["PUC19",  "pUC 19"],                                
                       #          ["PMX-1",  "pMX-1"],
                       #          ["PRMHA-3",  "pRmHa-3"]
                       #      ],
                       #  "BACTERIAL_HOST" : [
                       #          ["DH10B",  "DH10B"],
                       #          ["JM110",  "JM110"],    
                       #          ["BL21",  "BL21"],                                
                       #          ["DH5A",  "DH5a"],
                       #          ["PB3",  "PB3"],
                       #          ["DB31",  "DB3.1"],
                       #          ["CCDB_SURVIVAL",  "ccdB survival"]
                       #      ],  
                       #  "BIOSAFETY_LEVEL" : [
                       #          ["S1",  "S1"],
                       #          ["S2",  "S2"],    
                       #          ["S3",  "S3"]        
                       #      ],
                       #  "SELECTION_IN_BACTERIA" : [
                       #          ["AMPICILLIN",  "Ampicillin"],
                       #          ["KANAMYCIN",  "Kanamycin"],    
                       #          ["SPECTINOMCIN",  "Spectinomcin"],
                       #          ["UNKNOWN",  "Unknown"]                                        
                       #      ],
                       #  "SELECTION_IN_EUKARYOTES" : [
                       #          ["G418_NEO",  "G418/neo"],
                       #          ["MPA",  "MPA"],    
                       #          ["HYGROMYCIN",  "Hygromycin"]                                 
                       #      ],                            
                       #  "SOURCE_OF_REPLICON" : [
                       #          ["CMV",  "CMV"]                                   
                       #      ],
                       #  "PLASMID_ORGANISM" : [
                       #          ["HUMAN",  "human"],
                       #          ["MOUSE",  "mouse"],
                       #          ["RAT",  "rat"],
                       #          ["XENOPUS",  "xenopus"],
                       #          ["DROSOPHILA",  "drosophila"],
                       #          ["YEAST",  "yeast"],
                       #          ["ECOLI",  "E.coli"],
                       #          ["SYNTHETIC",  "synthetic"]
                       #      ],
                       #  "PROMOTER" : [
                       #          ["ENDOGENOUS",  "endogenous"],
                       #          ["GAL1",  "gal1"],
                       #          ["GALL",  "galL"],
                       #          ["GALS",  "galS"],
                       #          ["TET",  "tet"],
                       #          ["ADH",  "adh"],
                       #          ["CYC",  "cyc"],
                       #          ["GPD",  "gpd"],
                       #          ["DASH",  "\"-\""],
                       #          ["CMV",  "cmv"],
                       #          ["SV40",  "SV40"],
                       #          ["TET_ON",  "tet on"],
                       #          ["TET_OFF",  "tet off"],
                       #          ["RIM1",  "rim1"]
                       #      ],
                       #  "EXON_OR_THREE_PRIME" : [
                       #          ["EXON",  "exon"],
                       #          ["3P",  "3'"],
                       #          ["5P",  "5'"]
                       #      ], 
                       #  "SIRNA_SPECIES" : [
                       #          ["HUMAN",  "Human"],
                       #          ["MOUSE",  "Mouse'"],
                       #      ], 
                       #  "SIRNA_COMPANY" : [
                       #          ["AMBION",  "Ambion"],
                       #          ["DHARMACON",  "Dharmacon'"],
                       #          ["INVITORGEN",  "Invitrogen'"],
                       #          ["MICROSYNTH",  "Microsynth'"],
                       #          ["QIAGEN",  "Qiagen'"],
                       #          ["GIFT",  "Gift'"]
                       #      ], 
                       # "SIRNA_FREEZER_LOCATION" : [
                       #          ["BIGLABFREEZER",  "BigLab freezer"],
                       #          ["SMALLLAB_BIGFREEZER",  "SmallLab Big freezer'"],
                       #          ["FREEZER_G17",  "Freezer G17'"],
                       #          ["G93-FRANK",  "G9.3--Frank'"],
                       #          ["FREEZER-G17_BOX_NATHALIE",  "Freezer G17 Box Nathalie'"],
                       #          ["BOX-THIBAULT-BIGFREEZER-SMALLLAB",  "Box thibault Big freezer small lab'"],
                       #          ["BOX-NATHALIE",  "Box Nathalie'"]
                       #      ], 
                       # "TRANSFECTION_AGENT" : [
                       #          ["FUGENE",  "Fugene"],
                       #          ["LIPOFECTAMINE",  "Lipofectamine'"],
                       #          ["OLIGOFECTAMINE",  "Oligofectamine'"],
                       #          ["RNAIMAX",  "RNAimax'"]
                       #      ],                             
                        "FREEZER" : [
                                ["BENCH-20",  "Bench -20�C"],
                                ["BENCH-80",  "Bench -80�C"],
                                ["BENCH-RT",  "Bench RT"],
                                ["BENCH",  "Bench"]
                                    ]
                          }