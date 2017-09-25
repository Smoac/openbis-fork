# -*- coding: utf-8 -*-
import ch.systemsx.cisd.openbis.generic.server.jython.api.v1.DataType as DataType

tr = service.transaction()


file_type_FASTQ_PHRED_64 = tr.createNewFileFormatType('FASTQ_PHRED_64')
file_type_FASTQ_PHRED_64.setDescription('FastQ Format with PHRED+64 quality values (as deliverd by Illumina GA Pipeline >= 1.3)')

file_type_HDF5 = tr.createNewFileFormatType('HDF5')
file_type_HDF5.setDescription('Hierarchical Data Format File, version 5')

file_type_PROPRIETARY = tr.createNewFileFormatType('PROPRIETARY')
file_type_PROPRIETARY.setDescription('Proprietary Format File')

file_type_SRF = tr.createNewFileFormatType('SRF')
file_type_SRF.setDescription('Sequence Read Format File')

file_type_TIFF = tr.createNewFileFormatType('TIFF')
file_type_TIFF.setDescription('TIFF File')

file_type_TSV = tr.createNewFileFormatType('TSV')
file_type_TSV.setDescription('Tab Separated Values File')

file_type_XML = tr.createNewFileFormatType('XML')
file_type_XML.setDescription('XML File')

vocabulary_STORAGE_FORMAT = tr.createNewVocabulary('STORAGE_FORMAT')
vocabulary_STORAGE_FORMAT.setDescription('The on-disk storage format of a data set')
vocabulary_STORAGE_FORMAT.setUrlTemplate(None)
vocabulary_STORAGE_FORMAT.setManagedInternally(True)
vocabulary_STORAGE_FORMAT.setInternalNamespace(True)
vocabulary_STORAGE_FORMAT.setChosenFromList(True)

vocabulary_term_STORAGE_FORMAT_PROPRIETARY = tr.createNewVocabularyTerm('PROPRIETARY')
vocabulary_term_STORAGE_FORMAT_PROPRIETARY.setDescription(None)
vocabulary_term_STORAGE_FORMAT_PROPRIETARY.setLabel(None)
vocabulary_term_STORAGE_FORMAT_PROPRIETARY.setOrdinal(1)
vocabulary_STORAGE_FORMAT.addTerm(vocabulary_term_STORAGE_FORMAT_PROPRIETARY)

vocabulary_term_STORAGE_FORMAT_BDS_DIRECTORY = tr.createNewVocabularyTerm('BDS_DIRECTORY')
vocabulary_term_STORAGE_FORMAT_BDS_DIRECTORY.setDescription(None)
vocabulary_term_STORAGE_FORMAT_BDS_DIRECTORY.setLabel(None)
vocabulary_term_STORAGE_FORMAT_BDS_DIRECTORY.setOrdinal(2)
vocabulary_STORAGE_FORMAT.addTerm(vocabulary_term_STORAGE_FORMAT_BDS_DIRECTORY)

vocabulary_AFFILIATION = tr.createNewVocabulary('AFFILIATION')
vocabulary_AFFILIATION.setDescription('Where will the data be shipped after analysis and where will the bill be sent to')
vocabulary_AFFILIATION.setUrlTemplate(None)
vocabulary_AFFILIATION.setManagedInternally(False)
vocabulary_AFFILIATION.setInternalNamespace(False)
vocabulary_AFFILIATION.setChosenFromList(True)

vocabulary_term_AFFILIATION_NOVARTIS = tr.createNewVocabularyTerm('NOVARTIS')
vocabulary_term_AFFILIATION_NOVARTIS.setDescription(None)
vocabulary_term_AFFILIATION_NOVARTIS.setLabel('Novartis')
vocabulary_term_AFFILIATION_NOVARTIS.setOrdinal(6)
vocabulary_AFFILIATION.addTerm(vocabulary_term_AFFILIATION_NOVARTIS)

vocabulary_term_AFFILIATION_ETHZ_NON_BSSE = tr.createNewVocabularyTerm('ETHZ_NON_BSSE')
vocabulary_term_AFFILIATION_ETHZ_NON_BSSE.setDescription(None)
vocabulary_term_AFFILIATION_ETHZ_NON_BSSE.setLabel('ETHZ non-BSSE')
vocabulary_term_AFFILIATION_ETHZ_NON_BSSE.setOrdinal(9)
vocabulary_AFFILIATION.addTerm(vocabulary_term_AFFILIATION_ETHZ_NON_BSSE)

vocabulary_term_AFFILIATION_OTHER = tr.createNewVocabularyTerm('OTHER')
vocabulary_term_AFFILIATION_OTHER.setDescription(None)
vocabulary_term_AFFILIATION_OTHER.setLabel('Other')
vocabulary_term_AFFILIATION_OTHER.setOrdinal(8)
vocabulary_AFFILIATION.addTerm(vocabulary_term_AFFILIATION_OTHER)

vocabulary_term_AFFILIATION_BSSE = tr.createNewVocabularyTerm('BSSE')
vocabulary_term_AFFILIATION_BSSE.setDescription(None)
vocabulary_term_AFFILIATION_BSSE.setLabel('BSSE')
vocabulary_term_AFFILIATION_BSSE.setOrdinal(7)
vocabulary_AFFILIATION.addTerm(vocabulary_term_AFFILIATION_BSSE)

vocabulary_term_AFFILIATION_UNI_ZUERICH = tr.createNewVocabularyTerm('UNI_ZUERICH')
vocabulary_term_AFFILIATION_UNI_ZUERICH.setDescription(None)
vocabulary_term_AFFILIATION_UNI_ZUERICH.setLabel(None)
vocabulary_term_AFFILIATION_UNI_ZUERICH.setOrdinal(11)
vocabulary_AFFILIATION.addTerm(vocabulary_term_AFFILIATION_UNI_ZUERICH)

vocabulary_term_AFFILIATION_BIOCENTER_BASEL = tr.createNewVocabularyTerm('BIOCENTER_BASEL')
vocabulary_term_AFFILIATION_BIOCENTER_BASEL.setDescription(None)
vocabulary_term_AFFILIATION_BIOCENTER_BASEL.setLabel('Biocenter Basel')
vocabulary_term_AFFILIATION_BIOCENTER_BASEL.setOrdinal(3)
vocabulary_AFFILIATION.addTerm(vocabulary_term_AFFILIATION_BIOCENTER_BASEL)

vocabulary_term_AFFILIATION_PHIX = tr.createNewVocabularyTerm('PHIX')
vocabulary_term_AFFILIATION_PHIX.setDescription(None)
vocabulary_term_AFFILIATION_PHIX.setLabel(None)
vocabulary_term_AFFILIATION_PHIX.setOrdinal(5)
vocabulary_AFFILIATION.addTerm(vocabulary_term_AFFILIATION_PHIX)

vocabulary_term_AFFILIATION_UNI_BERN = tr.createNewVocabularyTerm('UNI_BERN')
vocabulary_term_AFFILIATION_UNI_BERN.setDescription(None)
vocabulary_term_AFFILIATION_UNI_BERN.setLabel(None)
vocabulary_term_AFFILIATION_UNI_BERN.setOrdinal(10)
vocabulary_AFFILIATION.addTerm(vocabulary_term_AFFILIATION_UNI_BERN)

vocabulary_term_AFFILIATION_FMI = tr.createNewVocabularyTerm('FMI')
vocabulary_term_AFFILIATION_FMI.setDescription(None)
vocabulary_term_AFFILIATION_FMI.setLabel('FMI')
vocabulary_term_AFFILIATION_FMI.setOrdinal(2)
vocabulary_AFFILIATION.addTerm(vocabulary_term_AFFILIATION_FMI)

vocabulary_term_AFFILIATION_UNIVERSITY_HOSPITAL_ZURICH = tr.createNewVocabularyTerm('UNIVERSITY_HOSPITAL_ZURICH')
vocabulary_term_AFFILIATION_UNIVERSITY_HOSPITAL_ZURICH.setDescription(None)
vocabulary_term_AFFILIATION_UNIVERSITY_HOSPITAL_ZURICH.setLabel(None)
vocabulary_term_AFFILIATION_UNIVERSITY_HOSPITAL_ZURICH.setOrdinal(12)
vocabulary_AFFILIATION.addTerm(vocabulary_term_AFFILIATION_UNIVERSITY_HOSPITAL_ZURICH)

vocabulary_term_AFFILIATION_UNIVERSITY_BASEL = tr.createNewVocabularyTerm('UNIVERSITY_BASEL')
vocabulary_term_AFFILIATION_UNIVERSITY_BASEL.setDescription(None)
vocabulary_term_AFFILIATION_UNIVERSITY_BASEL.setLabel('University of Basel')
vocabulary_term_AFFILIATION_UNIVERSITY_BASEL.setOrdinal(4)
vocabulary_AFFILIATION.addTerm(vocabulary_term_AFFILIATION_UNIVERSITY_BASEL)

vocabulary_AGILENT_KIT = tr.createNewVocabulary('AGILENT_KIT')
vocabulary_AGILENT_KIT.setDescription(None)
vocabulary_AGILENT_KIT.setUrlTemplate(None)
vocabulary_AGILENT_KIT.setManagedInternally(False)
vocabulary_AGILENT_KIT.setInternalNamespace(False)
vocabulary_AGILENT_KIT.setChosenFromList(True)

vocabulary_term_AGILENT_KIT_AGILENT_DNA_KIT_1000 = tr.createNewVocabularyTerm('AGILENT_DNA_KIT_1000')
vocabulary_term_AGILENT_KIT_AGILENT_DNA_KIT_1000.setDescription(None)
vocabulary_term_AGILENT_KIT_AGILENT_DNA_KIT_1000.setLabel(None)
vocabulary_term_AGILENT_KIT_AGILENT_DNA_KIT_1000.setOrdinal(1)
vocabulary_AGILENT_KIT.addTerm(vocabulary_term_AGILENT_KIT_AGILENT_DNA_KIT_1000)

vocabulary_term_AGILENT_KIT_AGILENT_HIGH_SENSITIVITY_DNA_KIT = tr.createNewVocabularyTerm('AGILENT_HIGH_SENSITIVITY_DNA_KIT')
vocabulary_term_AGILENT_KIT_AGILENT_HIGH_SENSITIVITY_DNA_KIT.setDescription(None)
vocabulary_term_AGILENT_KIT_AGILENT_HIGH_SENSITIVITY_DNA_KIT.setLabel(None)
vocabulary_term_AGILENT_KIT_AGILENT_HIGH_SENSITIVITY_DNA_KIT.setOrdinal(2)
vocabulary_AGILENT_KIT.addTerm(vocabulary_term_AGILENT_KIT_AGILENT_HIGH_SENSITIVITY_DNA_KIT)

vocabulary_ALIGNMENT_SOFTWARE = tr.createNewVocabulary('ALIGNMENT_SOFTWARE')
vocabulary_ALIGNMENT_SOFTWARE.setDescription('If an alignment is requested, which software package should be use?')
vocabulary_ALIGNMENT_SOFTWARE.setUrlTemplate(None)
vocabulary_ALIGNMENT_SOFTWARE.setManagedInternally(False)
vocabulary_ALIGNMENT_SOFTWARE.setInternalNamespace(False)
vocabulary_ALIGNMENT_SOFTWARE.setChosenFromList(True)

vocabulary_term_ALIGNMENT_SOFTWARE_BOWTIE = tr.createNewVocabularyTerm('BOWTIE')
vocabulary_term_ALIGNMENT_SOFTWARE_BOWTIE.setDescription(None)
vocabulary_term_ALIGNMENT_SOFTWARE_BOWTIE.setLabel(None)
vocabulary_term_ALIGNMENT_SOFTWARE_BOWTIE.setOrdinal(9)
vocabulary_ALIGNMENT_SOFTWARE.addTerm(vocabulary_term_ALIGNMENT_SOFTWARE_BOWTIE)

vocabulary_term_ALIGNMENT_SOFTWARE_BWA = tr.createNewVocabularyTerm('BWA')
vocabulary_term_ALIGNMENT_SOFTWARE_BWA.setDescription(None)
vocabulary_term_ALIGNMENT_SOFTWARE_BWA.setLabel(None)
vocabulary_term_ALIGNMENT_SOFTWARE_BWA.setOrdinal(6)
vocabulary_ALIGNMENT_SOFTWARE.addTerm(vocabulary_term_ALIGNMENT_SOFTWARE_BWA)

vocabulary_term_ALIGNMENT_SOFTWARE_BOWTIE_0127 = tr.createNewVocabularyTerm('BOWTIE_0.12.7')
vocabulary_term_ALIGNMENT_SOFTWARE_BOWTIE_0127.setDescription(None)
vocabulary_term_ALIGNMENT_SOFTWARE_BOWTIE_0127.setLabel(None)
vocabulary_term_ALIGNMENT_SOFTWARE_BOWTIE_0127.setOrdinal(7)
vocabulary_ALIGNMENT_SOFTWARE.addTerm(vocabulary_term_ALIGNMENT_SOFTWARE_BOWTIE_0127)

vocabulary_term_ALIGNMENT_SOFTWARE_ELAND = tr.createNewVocabularyTerm('ELAND')
vocabulary_term_ALIGNMENT_SOFTWARE_ELAND.setDescription(None)
vocabulary_term_ALIGNMENT_SOFTWARE_ELAND.setLabel(None)
vocabulary_term_ALIGNMENT_SOFTWARE_ELAND.setOrdinal(3)
vocabulary_ALIGNMENT_SOFTWARE.addTerm(vocabulary_term_ALIGNMENT_SOFTWARE_ELAND)

vocabulary_term_ALIGNMENT_SOFTWARE_NOT_NEEDED = tr.createNewVocabularyTerm('NOT_NEEDED')
vocabulary_term_ALIGNMENT_SOFTWARE_NOT_NEEDED.setDescription(None)
vocabulary_term_ALIGNMENT_SOFTWARE_NOT_NEEDED.setLabel(None)
vocabulary_term_ALIGNMENT_SOFTWARE_NOT_NEEDED.setOrdinal(1)
vocabulary_ALIGNMENT_SOFTWARE.addTerm(vocabulary_term_ALIGNMENT_SOFTWARE_NOT_NEEDED)

vocabulary_term_ALIGNMENT_SOFTWARE_MAQ = tr.createNewVocabularyTerm('MAQ')
vocabulary_term_ALIGNMENT_SOFTWARE_MAQ.setDescription(None)
vocabulary_term_ALIGNMENT_SOFTWARE_MAQ.setLabel(None)
vocabulary_term_ALIGNMENT_SOFTWARE_MAQ.setOrdinal(5)
vocabulary_ALIGNMENT_SOFTWARE.addTerm(vocabulary_term_ALIGNMENT_SOFTWARE_MAQ)

vocabulary_term_ALIGNMENT_SOFTWARE_NOVOALIGN = tr.createNewVocabularyTerm('NOVOALIGN')
vocabulary_term_ALIGNMENT_SOFTWARE_NOVOALIGN.setDescription(None)
vocabulary_term_ALIGNMENT_SOFTWARE_NOVOALIGN.setLabel(None)
vocabulary_term_ALIGNMENT_SOFTWARE_NOVOALIGN.setOrdinal(8)
vocabulary_ALIGNMENT_SOFTWARE.addTerm(vocabulary_term_ALIGNMENT_SOFTWARE_NOVOALIGN)

vocabulary_BARCODES = tr.createNewVocabulary('BARCODES')
vocabulary_BARCODES.setDescription('Index 1 for Illumina Indexing')
vocabulary_BARCODES.setUrlTemplate(None)
vocabulary_BARCODES.setManagedInternally(False)
vocabulary_BARCODES.setInternalNamespace(False)
vocabulary_BARCODES.setChosenFromList(True)

vocabulary_term_BARCODES_TCGGCAA = tr.createNewVocabularyTerm('TCGGCAA')
vocabulary_term_BARCODES_TCGGCAA.setDescription(None)
vocabulary_term_BARCODES_TCGGCAA.setLabel('DNA Adapter 48')
vocabulary_term_BARCODES_TCGGCAA.setOrdinal(90)
vocabulary_BARCODES.addTerm(vocabulary_term_BARCODES_TCGGCAA)

vocabulary_term_BARCODES_TATAATA = tr.createNewVocabularyTerm('TATAATA')
vocabulary_term_BARCODES_TATAATA.setDescription(None)
vocabulary_term_BARCODES_TATAATA.setLabel('DNA Adapter 44')
vocabulary_term_BARCODES_TATAATA.setOrdinal(86)
vocabulary_BARCODES.addTerm(vocabulary_term_BARCODES_TATAATA)

vocabulary_term_BARCODES_CTAGCTA = tr.createNewVocabularyTerm('CTAGCTA')
vocabulary_term_BARCODES_CTAGCTA.setDescription(None)
vocabulary_term_BARCODES_CTAGCTA.setLabel('DNA Adapter 38')
vocabulary_term_BARCODES_CTAGCTA.setOrdinal(80)
vocabulary_BARCODES.addTerm(vocabulary_term_BARCODES_CTAGCTA)

vocabulary_term_BARCODES_GTGAAAC = tr.createNewVocabularyTerm('GTGAAAC')
vocabulary_term_BARCODES_GTGAAAC.setDescription('Illumina, Nextera or Scriptseq')
vocabulary_term_BARCODES_GTGAAAC.setLabel('Index19 GTGAAAC')
vocabulary_term_BARCODES_GTGAAAC.setOrdinal(18)
vocabulary_BARCODES.addTerm(vocabulary_term_BARCODES_GTGAAAC)

vocabulary_term_BARCODES_CGTACGT = tr.createNewVocabularyTerm('CGTACGT')
vocabulary_term_BARCODES_CGTACGT.setDescription('Illumina, Nextera or Scriptseq')
vocabulary_term_BARCODES_CGTACGT.setLabel('Index22 CGTACGT')
vocabulary_term_BARCODES_CGTACGT.setOrdinal(21)
vocabulary_BARCODES.addTerm(vocabulary_term_BARCODES_CGTACGT)

vocabulary_term_BARCODES_CGAGGCTG = tr.createNewVocabularyTerm('CGAGGCTG')
vocabulary_term_BARCODES_CGAGGCTG.setDescription('Nextera DNA')
vocabulary_term_BARCODES_CGAGGCTG.setLabel('Index1 (i7) N710 CGAGGCTG')
vocabulary_term_BARCODES_CGAGGCTG.setOrdinal(34)
vocabulary_BARCODES.addTerm(vocabulary_term_BARCODES_CGAGGCTG)

vocabulary_term_BARCODES_TCATTCA = tr.createNewVocabularyTerm('TCATTCA')
vocabulary_term_BARCODES_TCATTCA.setDescription(None)
vocabulary_term_BARCODES_TCATTCA.setLabel('DNA Adapter 45')
vocabulary_term_BARCODES_TCATTCA.setOrdinal(87)
vocabulary_BARCODES.addTerm(vocabulary_term_BARCODES_TCATTCA)

vocabulary_term_BARCODES_TAGCTTA = tr.createNewVocabularyTerm('TAGCTTA')
vocabulary_term_BARCODES_TAGCTTA.setDescription('Illumina, Nextera or Scriptseq')
vocabulary_term_BARCODES_TAGCTTA.setLabel('Index10 TAGCTTA')
vocabulary_term_BARCODES_TAGCTTA.setOrdinal(10)
vocabulary_BARCODES.addTerm(vocabulary_term_BARCODES_TAGCTTA)

vocabulary_term_BARCODES_AGCATAA = tr.createNewVocabularyTerm('AGCATAA')
vocabulary_term_BARCODES_AGCATAA.setDescription(None)
vocabulary_term_BARCODES_AGCATAA.setLabel('20 AGCATAA')
vocabulary_term_BARCODES_AGCATAA.setOrdinal(42)
vocabulary_BARCODES.addTerm(vocabulary_term_BARCODES_AGCATAA)

vocabulary_term_BARCODES_TTCGTCA = tr.createNewVocabularyTerm('TTCGTCA')
vocabulary_term_BARCODES_TTCGTCA.setDescription(None)
vocabulary_term_BARCODES_TTCGTCA.setLabel('17 TTCGTCA')
vocabulary_term_BARCODES_TTCGTCA.setOrdinal(40)
vocabulary_BARCODES.addTerm(vocabulary_term_BARCODES_TTCGTCA)

vocabulary_term_BARCODES_TTACTTA = tr.createNewVocabularyTerm('TTACTTA')
vocabulary_term_BARCODES_TTACTTA.setDescription(None)
vocabulary_term_BARCODES_TTACTTA.setLabel('74 TTACTT')
vocabulary_term_BARCODES_TTACTTA.setOrdinal(58)
vocabulary_BARCODES.addTerm(vocabulary_term_BARCODES_TTACTTA)

vocabulary_term_BARCODES_GAAGCCA = tr.createNewVocabularyTerm('GAAGCCA')
vocabulary_term_BARCODES_GAAGCCA.setDescription(None)
vocabulary_term_BARCODES_GAAGCCA.setLabel('52 GAAGCC')
vocabulary_term_BARCODES_GAAGCCA.setOrdinal(50)
vocabulary_BARCODES.addTerm(vocabulary_term_BARCODES_GAAGCCA)

vocabulary_term_BARCODES_CTCTCTAC = tr.createNewVocabularyTerm('CTCTCTAC')
vocabulary_term_BARCODES_CTCTCTAC.setDescription('Nextera DNA')
vocabulary_term_BARCODES_CTCTCTAC.setLabel('Index1 (i7) N707 CTCTCTAC')
vocabulary_term_BARCODES_CTCTCTAC.setOrdinal(31)
vocabulary_BARCODES.addTerm(vocabulary_term_BARCODES_CTCTCTAC)

vocabulary_term_BARCODES_CGGAATA = tr.createNewVocabularyTerm('CGGAATA')
vocabulary_term_BARCODES_CGGAATA.setDescription(None)
vocabulary_term_BARCODES_CGGAATA.setLabel('DNA Adapter 37')
vocabulary_term_BARCODES_CGGAATA.setOrdinal(79)
vocabulary_BARCODES.addTerm(vocabulary_term_BARCODES_CGGAATA)

vocabulary_term_BARCODES_GTAGAGA = tr.createNewVocabularyTerm('GTAGAGA')
vocabulary_term_BARCODES_GTAGAGA.setDescription(None)
vocabulary_term_BARCODES_GTAGAGA.setLabel('DNA Adapter 17')
vocabulary_term_BARCODES_GTAGAGA.setOrdinal(62)
vocabulary_BARCODES.addTerm(vocabulary_term_BARCODES_GTAGAGA)

vocabulary_term_BARCODES_CCGTCCA = tr.createNewVocabularyTerm('CCGTCCA')
vocabulary_term_BARCODES_CCGTCCA.setDescription(None)
vocabulary_term_BARCODES_CCGTCCA.setLabel('DNA Adapter 16')
vocabulary_term_BARCODES_CCGTCCA.setOrdinal(61)
vocabulary_BARCODES.addTerm(vocabulary_term_BARCODES_CCGTCCA)

vocabulary_term_BARCODES_CTTGTAA = tr.createNewVocabularyTerm('CTTGTAA')
vocabulary_term_BARCODES_CTTGTAA.setDescription('Illumina, Nextera or Scriptseq')
vocabulary_term_BARCODES_CTTGTAA.setLabel('Index12 CTTGTAA')
vocabulary_term_BARCODES_CTTGTAA.setOrdinal(12)
vocabulary_BARCODES.addTerm(vocabulary_term_BARCODES_CTTGTAA)

vocabulary_term_BARCODES_AGTCAAC = tr.createNewVocabularyTerm('AGTCAAC')
vocabulary_term_BARCODES_AGTCAAC.setDescription(None)
vocabulary_term_BARCODES_AGTCAAC.setLabel('Index13 AGTCAAC')
vocabulary_term_BARCODES_AGTCAAC.setOrdinal(91)
vocabulary_BARCODES.addTerm(vocabulary_term_BARCODES_AGTCAAC)

vocabulary_term_BARCODES_CTGACCA = tr.createNewVocabularyTerm('CTGACCA')
vocabulary_term_BARCODES_CTGACCA.setDescription(None)
vocabulary_term_BARCODES_CTGACCA.setLabel('50 CTGACC')
vocabulary_term_BARCODES_CTGACCA.setOrdinal(49)
vocabulary_BARCODES.addTerm(vocabulary_term_BARCODES_CTGACCA)

vocabulary_term_BARCODES_TGACCAA = tr.createNewVocabularyTerm('TGACCAA')
vocabulary_term_BARCODES_TGACCAA.setDescription('Illumina, Nextera or Scriptseq')
vocabulary_term_BARCODES_TGACCAA.setLabel('Index4 TGACCAA')
vocabulary_term_BARCODES_TGACCAA.setOrdinal(4)
vocabulary_BARCODES.addTerm(vocabulary_term_BARCODES_TGACCAA)

vocabulary_term_BARCODES_ATCACGA = tr.createNewVocabularyTerm('ATCACGA')
vocabulary_term_BARCODES_ATCACGA.setDescription('Illumina, Nextera or Scriptseq')
vocabulary_term_BARCODES_ATCACGA.setLabel('Index1 ATCACGA')
vocabulary_term_BARCODES_ATCACGA.setOrdinal(1)
vocabulary_BARCODES.addTerm(vocabulary_term_BARCODES_ATCACGA)

vocabulary_term_BARCODES_TCCCGAA = tr.createNewVocabularyTerm('TCCCGAA')
vocabulary_term_BARCODES_TCCCGAA.setDescription(None)
vocabulary_term_BARCODES_TCCCGAA.setLabel('DNA Adapter 46')
vocabulary_term_BARCODES_TCCCGAA.setOrdinal(88)
vocabulary_BARCODES.addTerm(vocabulary_term_BARCODES_TCCCGAA)

vocabulary_term_BARCODES_CAAAAGA = tr.createNewVocabularyTerm('CAAAAGA')
vocabulary_term_BARCODES_CAAAAGA.setDescription(None)
vocabulary_term_BARCODES_CAAAAGA.setLabel('DNA Adapter 28')
vocabulary_term_BARCODES_CAAAAGA.setOrdinal(70)
vocabulary_BARCODES.addTerm(vocabulary_term_BARCODES_CAAAAGA)

vocabulary_term_BARCODES_CGTACTAG = tr.createNewVocabularyTerm('CGTACTAG')
vocabulary_term_BARCODES_CGTACTAG.setDescription('Nextera DNA')
vocabulary_term_BARCODES_CGTACTAG.setLabel('Index1 (i7) N702 CGTACTAG')
vocabulary_term_BARCODES_CGTACTAG.setOrdinal(26)
vocabulary_BARCODES.addTerm(vocabulary_term_BARCODES_CGTACTAG)

vocabulary_term_BARCODES_GGCTACA = tr.createNewVocabularyTerm('GGCTACA')
vocabulary_term_BARCODES_GGCTACA.setDescription('Illumina, Nextera or Scriptseq')
vocabulary_term_BARCODES_GGCTACA.setLabel('Index11 GGCTACA')
vocabulary_term_BARCODES_GGCTACA.setOrdinal(11)
vocabulary_BARCODES.addTerm(vocabulary_term_BARCODES_GGCTACA)

vocabulary_term_BARCODES_ATGTCAA = tr.createNewVocabularyTerm('ATGTCAA')
vocabulary_term_BARCODES_ATGTCAA.setDescription(None)
vocabulary_term_BARCODES_ATGTCAA.setLabel('DNA Adapter 15')
vocabulary_term_BARCODES_ATGTCAA.setOrdinal(60)
vocabulary_BARCODES.addTerm(vocabulary_term_BARCODES_ATGTCAA)

vocabulary_term_BARCODES_GAGTGGA = tr.createNewVocabularyTerm('GAGTGGA')
vocabulary_term_BARCODES_GAGTGGA.setDescription('Illumina, Nextera or Scriptseq')
vocabulary_term_BARCODES_GAGTGGA.setLabel('Index23 GAGTGGA')
vocabulary_term_BARCODES_GAGTGGA.setOrdinal(22)
vocabulary_BARCODES.addTerm(vocabulary_term_BARCODES_GAGTGGA)

vocabulary_term_BARCODES_GGACTCCT = tr.createNewVocabularyTerm('GGACTCCT')
vocabulary_term_BARCODES_GGACTCCT.setDescription('Nextera DNA')
vocabulary_term_BARCODES_GGACTCCT.setLabel('Index1 (i7) N705 GGACTCCT')
vocabulary_term_BARCODES_GGACTCCT.setOrdinal(29)
vocabulary_BARCODES.addTerm(vocabulary_term_BARCODES_GGACTCCT)

vocabulary_term_BARCODES_CTATACA = tr.createNewVocabularyTerm('CTATACA')
vocabulary_term_BARCODES_CTATACA.setDescription(None)
vocabulary_term_BARCODES_CTATACA.setLabel('DNA Adapter 39')
vocabulary_term_BARCODES_CTATACA.setOrdinal(81)
vocabulary_BARCODES.addTerm(vocabulary_term_BARCODES_CTATACA)

vocabulary_term_BARCODES_TCCTGAGC = tr.createNewVocabularyTerm('TCCTGAGC')
vocabulary_term_BARCODES_TCCTGAGC.setDescription('Nextera DNA')
vocabulary_term_BARCODES_TCCTGAGC.setLabel('Index1 (i7) N704 TCCTGAGC')
vocabulary_term_BARCODES_TCCTGAGC.setOrdinal(28)
vocabulary_BARCODES.addTerm(vocabulary_term_BARCODES_TCCTGAGC)

vocabulary_term_BARCODES_CACTCAA = tr.createNewVocabularyTerm('CACTCAA')
vocabulary_term_BARCODES_CACTCAA.setDescription(None)
vocabulary_term_BARCODES_CACTCAA.setLabel('DNA Adapter 32')
vocabulary_term_BARCODES_CACTCAA.setOrdinal(74)
vocabulary_BARCODES.addTerm(vocabulary_term_BARCODES_CACTCAA)

vocabulary_term_BARCODES_CAGATCA = tr.createNewVocabularyTerm('CAGATCA')
vocabulary_term_BARCODES_CAGATCA.setDescription('Illumina, Nextera or Scriptseq')
vocabulary_term_BARCODES_CAGATCA.setLabel('Index7 CAGATCA')
vocabulary_term_BARCODES_CAGATCA.setOrdinal(7)
vocabulary_BARCODES.addTerm(vocabulary_term_BARCODES_CAGATCA)

vocabulary_term_BARCODES_CGATGTA = tr.createNewVocabularyTerm('CGATGTA')
vocabulary_term_BARCODES_CGATGTA.setDescription('Illumina, Nextera or Scriptseq')
vocabulary_term_BARCODES_CGATGTA.setLabel('Index2 CGATGTA')
vocabulary_term_BARCODES_CGATGTA.setOrdinal(2)
vocabulary_BARCODES.addTerm(vocabulary_term_BARCODES_CGATGTA)

vocabulary_term_BARCODES_AGTTCCG = tr.createNewVocabularyTerm('AGTTCCG')
vocabulary_term_BARCODES_AGTTCCG.setDescription('Illumina, Nextera or Scriptseq')
vocabulary_term_BARCODES_AGTTCCG.setLabel('Index14 AGTTCCG')
vocabulary_term_BARCODES_AGTTCCG.setOrdinal(14)
vocabulary_BARCODES.addTerm(vocabulary_term_BARCODES_AGTTCCG)

vocabulary_term_BARCODES_TAAGGCGA = tr.createNewVocabularyTerm('TAAGGCGA')
vocabulary_term_BARCODES_TAAGGCGA.setDescription('Nextera DNA')
vocabulary_term_BARCODES_TAAGGCGA.setLabel('Index1 (i7) N701 TAAGGCGA')
vocabulary_term_BARCODES_TAAGGCGA.setOrdinal(25)
vocabulary_BARCODES.addTerm(vocabulary_term_BARCODES_TAAGGCGA)

vocabulary_term_BARCODES_GACGACA = tr.createNewVocabularyTerm('GACGACA')
vocabulary_term_BARCODES_GACGACA.setDescription(None)
vocabulary_term_BARCODES_GACGACA.setLabel('DNA Adapter 41')
vocabulary_term_BARCODES_GACGACA.setOrdinal(83)
vocabulary_BARCODES.addTerm(vocabulary_term_BARCODES_GACGACA)

vocabulary_term_BARCODES_CACGATA = tr.createNewVocabularyTerm('CACGATA')
vocabulary_term_BARCODES_CACGATA.setDescription(None)
vocabulary_term_BARCODES_CACGATA.setLabel('DNA Adapter 31')
vocabulary_term_BARCODES_CACGATA.setOrdinal(73)
vocabulary_BARCODES.addTerm(vocabulary_term_BARCODES_CACGATA)

vocabulary_term_BARCODES_CGCTCGA = tr.createNewVocabularyTerm('CGCTCGA')
vocabulary_term_BARCODES_CGCTCGA.setDescription(None)
vocabulary_term_BARCODES_CGCTCGA.setLabel('43 CGCTCG')
vocabulary_term_BARCODES_CGCTCGA.setOrdinal(46)
vocabulary_BARCODES.addTerm(vocabulary_term_BARCODES_CGCTCGA)

vocabulary_term_BARCODES_ACTGATA = tr.createNewVocabularyTerm('ACTGATA')
vocabulary_term_BARCODES_ACTGATA.setDescription('Illumina, Nextera or Scriptseq')
vocabulary_term_BARCODES_ACTGATA.setLabel('Index25 ACTGATA')
vocabulary_term_BARCODES_ACTGATA.setOrdinal(23)
vocabulary_BARCODES.addTerm(vocabulary_term_BARCODES_ACTGATA)

vocabulary_term_BARCODES_AATGCGA = tr.createNewVocabularyTerm('AATGCGA')
vocabulary_term_BARCODES_AATGCGA.setDescription(None)
vocabulary_term_BARCODES_AATGCGA.setLabel('Lib AATGCGA')
vocabulary_term_BARCODES_AATGCGA.setOrdinal(92)
vocabulary_BARCODES.addTerm(vocabulary_term_BARCODES_AATGCGA)

vocabulary_term_BARCODES_GTCCGCA = tr.createNewVocabularyTerm('GTCCGCA')
vocabulary_term_BARCODES_GTCCGCA.setDescription('Illumina, Nextera or Scriptseq')
vocabulary_term_BARCODES_GTCCGCA.setLabel('Index18 GTCCGCA')
vocabulary_term_BARCODES_GTCCGCA.setOrdinal(17)
vocabulary_BARCODES.addTerm(vocabulary_term_BARCODES_GTCCGCA)

vocabulary_term_BARCODES_CGTACGA = tr.createNewVocabularyTerm('CGTACGA')
vocabulary_term_BARCODES_CGTACGA.setDescription(None)
vocabulary_term_BARCODES_CGTACGA.setLabel('DNA Adapter 22')
vocabulary_term_BARCODES_CGTACGA.setOrdinal(66)
vocabulary_BARCODES.addTerm(vocabulary_term_BARCODES_CGTACGA)

vocabulary_term_BARCODES_GTGAAAA = tr.createNewVocabularyTerm('GTGAAAA')
vocabulary_term_BARCODES_GTGAAAA.setDescription(None)
vocabulary_term_BARCODES_GTGAAAA.setLabel('DNA Adapter 19')
vocabulary_term_BARCODES_GTGAAAA.setOrdinal(63)
vocabulary_BARCODES.addTerm(vocabulary_term_BARCODES_GTGAAAA)

vocabulary_term_BARCODES_GTTTCGA = tr.createNewVocabularyTerm('GTTTCGA')
vocabulary_term_BARCODES_GTTTCGA.setDescription(None)
vocabulary_term_BARCODES_GTTTCGA.setLabel('DNA Adapter 21')
vocabulary_term_BARCODES_GTTTCGA.setOrdinal(65)
vocabulary_BARCODES.addTerm(vocabulary_term_BARCODES_GTTTCGA)

vocabulary_term_BARCODES_TAATCGA = tr.createNewVocabularyTerm('TAATCGA')
vocabulary_term_BARCODES_TAATCGA.setDescription(None)
vocabulary_term_BARCODES_TAATCGA.setLabel('DNA Adapter 42')
vocabulary_term_BARCODES_TAATCGA.setOrdinal(84)
vocabulary_BARCODES.addTerm(vocabulary_term_BARCODES_TAATCGA)

vocabulary_term_BARCODES_CAGATGA = tr.createNewVocabularyTerm('CAGATGA')
vocabulary_term_BARCODES_CAGATGA.setDescription(None)
vocabulary_term_BARCODES_CAGATGA.setLabel('28 CAGATG')
vocabulary_term_BARCODES_CAGATGA.setOrdinal(44)
vocabulary_BARCODES.addTerm(vocabulary_term_BARCODES_CAGATGA)

vocabulary_term_BARCODES_ACTTGAA = tr.createNewVocabularyTerm('ACTTGAA')
vocabulary_term_BARCODES_ACTTGAA.setDescription('Illumina, Nextera or Scriptseq')
vocabulary_term_BARCODES_ACTTGAA.setLabel('Index8 ACTTGAA')
vocabulary_term_BARCODES_ACTTGAA.setOrdinal(8)
vocabulary_BARCODES.addTerm(vocabulary_term_BARCODES_ACTTGAA)

vocabulary_term_BARCODES_GCCAATA = tr.createNewVocabularyTerm('GCCAATA')
vocabulary_term_BARCODES_GCCAATA.setDescription('Illumina, Nextera or Scriptseq')
vocabulary_term_BARCODES_GCCAATA.setLabel('Index6 GCCAATA')
vocabulary_term_BARCODES_GCCAATA.setOrdinal(6)
vocabulary_BARCODES.addTerm(vocabulary_term_BARCODES_GCCAATA)

vocabulary_term_BARCODES_AAGACTA = tr.createNewVocabularyTerm('AAGACTA')
vocabulary_term_BARCODES_AAGACTA.setDescription(None)
vocabulary_term_BARCODES_AAGACTA.setLabel('02 AAGACT')
vocabulary_term_BARCODES_AAGACTA.setOrdinal(37)
vocabulary_BARCODES.addTerm(vocabulary_term_BARCODES_AAGACTA)

vocabulary_term_BARCODES_TACAGCA = tr.createNewVocabularyTerm('TACAGCA')
vocabulary_term_BARCODES_TACAGCA.setDescription(None)
vocabulary_term_BARCODES_TACAGCA.setLabel('DNA Adapter 43')
vocabulary_term_BARCODES_TACAGCA.setOrdinal(85)
vocabulary_BARCODES.addTerm(vocabulary_term_BARCODES_TACAGCA)

vocabulary_term_BARCODES_GCTGAAA = tr.createNewVocabularyTerm('GCTGAAA')
vocabulary_term_BARCODES_GCTGAAA.setDescription(None)
vocabulary_term_BARCODES_GCTGAAA.setLabel('59 GCTGAA')
vocabulary_term_BARCODES_GCTGAAA.setOrdinal(51)
vocabulary_BARCODES.addTerm(vocabulary_term_BARCODES_GCTGAAA)

vocabulary_term_BARCODES_AGATACA = tr.createNewVocabularyTerm('AGATACA')
vocabulary_term_BARCODES_AGATACA.setDescription('Illumina, Nextera or Scriptseq')
vocabulary_term_BARCODES_AGATACA.setLabel('Index13 AGATAC')
vocabulary_term_BARCODES_AGATACA.setOrdinal(13)
vocabulary_BARCODES.addTerm(vocabulary_term_BARCODES_AGATACA)

vocabulary_term_BARCODES_TATCGTA = tr.createNewVocabularyTerm('TATCGTA')
vocabulary_term_BARCODES_TATCGTA.setDescription(None)
vocabulary_term_BARCODES_TATCGTA.setLabel('66 TATCGT')
vocabulary_term_BARCODES_TATCGTA.setOrdinal(55)
vocabulary_BARCODES.addTerm(vocabulary_term_BARCODES_TATCGTA)

vocabulary_term_BARCODES_CCGTCCC = tr.createNewVocabularyTerm('CCGTCCC')
vocabulary_term_BARCODES_CCGTCCC.setDescription('Illumina, Nextera or Scriptseq')
vocabulary_term_BARCODES_CCGTCCC.setLabel('Index16 CCGTCCC')
vocabulary_term_BARCODES_CCGTCCC.setOrdinal(16)
vocabulary_BARCODES.addTerm(vocabulary_term_BARCODES_CCGTCCC)

vocabulary_term_BARCODES_ATTCCTT = tr.createNewVocabularyTerm('ATTCCTT')
vocabulary_term_BARCODES_ATTCCTT.setDescription('Illumina, Nextera or Scriptseq')
vocabulary_term_BARCODES_ATTCCTT.setLabel('Index27 ATTCCTT')
vocabulary_term_BARCODES_ATTCCTT.setOrdinal(24)
vocabulary_BARCODES.addTerm(vocabulary_term_BARCODES_ATTCCTT)

vocabulary_term_BARCODES_ATACGCA = tr.createNewVocabularyTerm('ATACGCA')
vocabulary_term_BARCODES_ATACGCA.setDescription(None)
vocabulary_term_BARCODES_ATACGCA.setLabel('20 ATACGC')
vocabulary_term_BARCODES_ATACGCA.setOrdinal(43)
vocabulary_BARCODES.addTerm(vocabulary_term_BARCODES_ATACGCA)

vocabulary_term_BARCODES_GCGCTGA = tr.createNewVocabularyTerm('GCGCTGA')
vocabulary_term_BARCODES_GCGCTGA.setDescription(None)
vocabulary_term_BARCODES_GCGCTGA.setLabel('66 GCGCTGA')
vocabulary_term_BARCODES_GCGCTGA.setOrdinal(54)
vocabulary_BARCODES.addTerm(vocabulary_term_BARCODES_GCGCTGA)

vocabulary_term_BARCODES_TCGAAGA = tr.createNewVocabularyTerm('TCGAAGA')
vocabulary_term_BARCODES_TCGAAGA.setDescription(None)
vocabulary_term_BARCODES_TCGAAGA.setLabel('DNA Adapter 47')
vocabulary_term_BARCODES_TCGAAGA.setOrdinal(89)
vocabulary_BARCODES.addTerm(vocabulary_term_BARCODES_TCGAAGA)

vocabulary_term_BARCODES_GTGGCCA = tr.createNewVocabularyTerm('GTGGCCA')
vocabulary_term_BARCODES_GTGGCCA.setDescription(None)
vocabulary_term_BARCODES_GTGGCCA.setLabel('DNA Adapter 20')
vocabulary_term_BARCODES_GTGGCCA.setOrdinal(64)
vocabulary_BARCODES.addTerm(vocabulary_term_BARCODES_GTGGCCA)

vocabulary_term_BARCODES_CTCAGAA = tr.createNewVocabularyTerm('CTCAGAA')
vocabulary_term_BARCODES_CTCAGAA.setDescription(None)
vocabulary_term_BARCODES_CTCAGAA.setLabel('DNA Adapter 40')
vocabulary_term_BARCODES_CTCAGAA.setOrdinal(82)
vocabulary_BARCODES.addTerm(vocabulary_term_BARCODES_CTCAGAA)

vocabulary_term_BARCODES_CACCGGA = tr.createNewVocabularyTerm('CACCGGA')
vocabulary_term_BARCODES_CACCGGA.setDescription(None)
vocabulary_term_BARCODES_CACCGGA.setLabel('DNA Adapter 30')
vocabulary_term_BARCODES_CACCGGA.setOrdinal(72)
vocabulary_BARCODES.addTerm(vocabulary_term_BARCODES_CACCGGA)

vocabulary_term_BARCODES_GGTAGCA = tr.createNewVocabularyTerm('GGTAGCA')
vocabulary_term_BARCODES_GGTAGCA.setDescription(None)
vocabulary_term_BARCODES_GGTAGCA.setLabel('DNA Adapter 24')
vocabulary_term_BARCODES_GGTAGCA.setOrdinal(67)
vocabulary_BARCODES.addTerm(vocabulary_term_BARCODES_GGTAGCA)

vocabulary_term_BARCODES_CCATGAA = tr.createNewVocabularyTerm('CCATGAA')
vocabulary_term_BARCODES_CCATGAA.setDescription(None)
vocabulary_term_BARCODES_CCATGAA.setLabel('50 CCATGAA')
vocabulary_term_BARCODES_CCATGAA.setOrdinal(48)
vocabulary_BARCODES.addTerm(vocabulary_term_BARCODES_CCATGAA)

vocabulary_term_BARCODES_CAGAGAGG = tr.createNewVocabularyTerm('CAGAGAGG')
vocabulary_term_BARCODES_CAGAGAGG.setDescription('Nextera DNA')
vocabulary_term_BARCODES_CAGAGAGG.setLabel('Index1 (i7) N708 CAGAGAGG')
vocabulary_term_BARCODES_CAGAGAGG.setOrdinal(32)
vocabulary_BARCODES.addTerm(vocabulary_term_BARCODES_CAGAGAGG)

vocabulary_term_BARCODES_ACAGTGA = tr.createNewVocabularyTerm('ACAGTGA')
vocabulary_term_BARCODES_ACAGTGA.setDescription('Illumina, Nextera or Scriptseq')
vocabulary_term_BARCODES_ACAGTGA.setLabel('Index5 ACAGTGA')
vocabulary_term_BARCODES_ACAGTGA.setOrdinal(5)
vocabulary_BARCODES.addTerm(vocabulary_term_BARCODES_ACAGTGA)

vocabulary_term_BARCODES_GCTACGCT = tr.createNewVocabularyTerm('GCTACGCT')
vocabulary_term_BARCODES_GCTACGCT.setDescription('Nextera DNA')
vocabulary_term_BARCODES_GCTACGCT.setLabel('Index1 (i7) N709 GCTACGCT')
vocabulary_term_BARCODES_GCTACGCT.setOrdinal(33)
vocabulary_BARCODES.addTerm(vocabulary_term_BARCODES_GCTACGCT)

vocabulary_term_BARCODES_GTAGAGGA = tr.createNewVocabularyTerm('GTAGAGGA')
vocabulary_term_BARCODES_GTAGAGGA.setDescription('Nextera DNA')
vocabulary_term_BARCODES_GTAGAGGA.setLabel('Index1 (i7) N712 GTAGAGGA')
vocabulary_term_BARCODES_GTAGAGGA.setOrdinal(36)
vocabulary_BARCODES.addTerm(vocabulary_term_BARCODES_GTAGAGGA)

vocabulary_term_BARCODES_AGGCAGAA = tr.createNewVocabularyTerm('AGGCAGAA')
vocabulary_term_BARCODES_AGGCAGAA.setDescription('Nextera DNA')
vocabulary_term_BARCODES_AGGCAGAA.setLabel('Index1 (i7) N703 AGGCAGAA')
vocabulary_term_BARCODES_AGGCAGAA.setOrdinal(27)
vocabulary_BARCODES.addTerm(vocabulary_term_BARCODES_AGGCAGAA)

vocabulary_term_BARCODES_TCCTACA = tr.createNewVocabularyTerm('TCCTACA')
vocabulary_term_BARCODES_TCCTACA.setDescription(None)
vocabulary_term_BARCODES_TCCTACA.setLabel('68 TCCTAC')
vocabulary_term_BARCODES_TCCTACA.setOrdinal(56)
vocabulary_BARCODES.addTerm(vocabulary_term_BARCODES_TCCTACA)

vocabulary_term_BARCODES_TCTATAA = tr.createNewVocabularyTerm('TCTATAA')
vocabulary_term_BARCODES_TCTATAA.setDescription(None)
vocabulary_term_BARCODES_TCTATAA.setLabel('70 TCTATA')
vocabulary_term_BARCODES_TCTATAA.setOrdinal(57)
vocabulary_BARCODES.addTerm(vocabulary_term_BARCODES_TCTATAA)

vocabulary_term_BARCODES_TTAGGCA = tr.createNewVocabularyTerm('TTAGGCA')
vocabulary_term_BARCODES_TTAGGCA.setDescription('Illumina, Nextera or Scriptseq')
vocabulary_term_BARCODES_TTAGGCA.setLabel('Index3 TTAGGCA')
vocabulary_term_BARCODES_TTAGGCA.setOrdinal(3)
vocabulary_BARCODES.addTerm(vocabulary_term_BARCODES_TTAGGCA)

vocabulary_term_BARCODES_CCAACAA = tr.createNewVocabularyTerm('CCAACAA')
vocabulary_term_BARCODES_CCAACAA.setDescription(None)
vocabulary_term_BARCODES_CCAACAA.setLabel('DNA Adapter 36')
vocabulary_term_BARCODES_CCAACAA.setOrdinal(78)
vocabulary_BARCODES.addTerm(vocabulary_term_BARCODES_CCAACAA)

vocabulary_term_BARCODES_ATGTCAG = tr.createNewVocabularyTerm('ATGTCAG')
vocabulary_term_BARCODES_ATGTCAG.setDescription('Illumina, Nextera or Scriptseq')
vocabulary_term_BARCODES_ATGTCAG.setLabel('Index15 ATGTCAG')
vocabulary_term_BARCODES_ATGTCAG.setOrdinal(15)
vocabulary_BARCODES.addTerm(vocabulary_term_BARCODES_ATGTCAG)

vocabulary_term_BARCODES_CAGGCGA = tr.createNewVocabularyTerm('CAGGCGA')
vocabulary_term_BARCODES_CAGGCGA.setDescription(None)
vocabulary_term_BARCODES_CAGGCGA.setLabel('DNA Adapter 33')
vocabulary_term_BARCODES_CAGGCGA.setOrdinal(75)
vocabulary_BARCODES.addTerm(vocabulary_term_BARCODES_CAGGCGA)

vocabulary_term_BARCODES_ATGAGCA = tr.createNewVocabularyTerm('ATGAGCA')
vocabulary_term_BARCODES_ATGAGCA.setDescription(None)
vocabulary_term_BARCODES_ATGAGCA.setLabel('DNA Adapter 26')
vocabulary_term_BARCODES_ATGAGCA.setOrdinal(68)
vocabulary_BARCODES.addTerm(vocabulary_term_BARCODES_ATGAGCA)

vocabulary_term_BARCODES_GTTTCGG = tr.createNewVocabularyTerm('GTTTCGG')
vocabulary_term_BARCODES_GTTTCGG.setDescription('Illumina, Nextera or Scriptseq')
vocabulary_term_BARCODES_GTTTCGG.setLabel('Index21 GTTTCGG')
vocabulary_term_BARCODES_GTTTCGG.setOrdinal(20)
vocabulary_BARCODES.addTerm(vocabulary_term_BARCODES_GTTTCGG)

vocabulary_term_BARCODES_ATTCCTA = tr.createNewVocabularyTerm('ATTCCTA')
vocabulary_term_BARCODES_ATTCCTA.setDescription(None)
vocabulary_term_BARCODES_ATTCCTA.setLabel('DNA Adapter 27')
vocabulary_term_BARCODES_ATTCCTA.setOrdinal(69)
vocabulary_BARCODES.addTerm(vocabulary_term_BARCODES_ATTCCTA)

vocabulary_term_BARCODES_CGTATTA = tr.createNewVocabularyTerm('CGTATTA')
vocabulary_term_BARCODES_CGTATTA.setDescription(None)
vocabulary_term_BARCODES_CGTATTA.setLabel('45 CGTATT')
vocabulary_term_BARCODES_CGTATTA.setOrdinal(47)
vocabulary_BARCODES.addTerm(vocabulary_term_BARCODES_CGTATTA)

vocabulary_term_BARCODES_CCGAATA = tr.createNewVocabularyTerm('CCGAATA')
vocabulary_term_BARCODES_CCGAATA.setDescription(None)
vocabulary_term_BARCODES_CCGAATA.setLabel('34 CCGAAT')
vocabulary_term_BARCODES_CCGAATA.setOrdinal(45)
vocabulary_BARCODES.addTerm(vocabulary_term_BARCODES_CCGAATA)

vocabulary_term_BARCODES_CATGGCA = tr.createNewVocabularyTerm('CATGGCA')
vocabulary_term_BARCODES_CATGGCA.setDescription(None)
vocabulary_term_BARCODES_CATGGCA.setLabel('DNA Adapter 34')
vocabulary_term_BARCODES_CATGGCA.setOrdinal(76)
vocabulary_BARCODES.addTerm(vocabulary_term_BARCODES_CATGGCA)

vocabulary_term_BARCODES_GTCGCGA = tr.createNewVocabularyTerm('GTCGCGA')
vocabulary_term_BARCODES_GTCGCGA.setDescription(None)
vocabulary_term_BARCODES_GTCGCGA.setLabel('64 GTCGCG')
vocabulary_term_BARCODES_GTCGCGA.setOrdinal(52)
vocabulary_BARCODES.addTerm(vocabulary_term_BARCODES_GTCGCGA)

vocabulary_term_BARCODES_GATCAGA = tr.createNewVocabularyTerm('GATCAGA')
vocabulary_term_BARCODES_GATCAGA.setDescription('Illumina, Nextera or Scriptseq')
vocabulary_term_BARCODES_GATCAGA.setLabel('Index9 GATCAGA')
vocabulary_term_BARCODES_GATCAGA.setOrdinal(9)
vocabulary_BARCODES.addTerm(vocabulary_term_BARCODES_GATCAGA)

vocabulary_term_BARCODES_CATTTTA = tr.createNewVocabularyTerm('CATTTTA')
vocabulary_term_BARCODES_CATTTTA.setDescription(None)
vocabulary_term_BARCODES_CATTTTA.setLabel('DNA Adapter 35')
vocabulary_term_BARCODES_CATTTTA.setOrdinal(77)
vocabulary_BARCODES.addTerm(vocabulary_term_BARCODES_CATTTTA)

vocabulary_term_BARCODES_AGGTTGA = tr.createNewVocabularyTerm('AGGTTGA')
vocabulary_term_BARCODES_AGGTTGA.setDescription(None)
vocabulary_term_BARCODES_AGGTTGA.setLabel('17 AGGTTG')
vocabulary_term_BARCODES_AGGTTGA.setOrdinal(39)
vocabulary_BARCODES.addTerm(vocabulary_term_BARCODES_AGGTTGA)

vocabulary_term_BARCODES_TAAGATA = tr.createNewVocabularyTerm('TAAGATA')
vocabulary_term_BARCODES_TAAGATA.setDescription(None)
vocabulary_term_BARCODES_TAAGATA.setLabel('65 TAAGAT')
vocabulary_term_BARCODES_TAAGATA.setOrdinal(53)
vocabulary_BARCODES.addTerm(vocabulary_term_BARCODES_TAAGATA)

vocabulary_term_BARCODES_AAGAGGCA = tr.createNewVocabularyTerm('AAGAGGCA')
vocabulary_term_BARCODES_AAGAGGCA.setDescription('Nextera DNA')
vocabulary_term_BARCODES_AAGAGGCA.setLabel('Index1 (i7) N711 AAGAGGCA')
vocabulary_term_BARCODES_AAGAGGCA.setOrdinal(35)
vocabulary_BARCODES.addTerm(vocabulary_term_BARCODES_AAGAGGCA)

vocabulary_term_BARCODES_GTGGCCT = tr.createNewVocabularyTerm('GTGGCCT')
vocabulary_term_BARCODES_GTGGCCT.setDescription('Illumina, Nextera or Scriptseq')
vocabulary_term_BARCODES_GTGGCCT.setLabel('Index20 GTGGCCT')
vocabulary_term_BARCODES_GTGGCCT.setOrdinal(19)
vocabulary_BARCODES.addTerm(vocabulary_term_BARCODES_GTGGCCT)

vocabulary_term_BARCODES_TTCCGAA = tr.createNewVocabularyTerm('TTCCGAA')
vocabulary_term_BARCODES_TTCCGAA.setDescription(None)
vocabulary_term_BARCODES_TTCCGAA.setLabel('75 TTCCGA')
vocabulary_term_BARCODES_TTCCGAA.setOrdinal(59)
vocabulary_BARCODES.addTerm(vocabulary_term_BARCODES_TTCCGAA)

vocabulary_term_BARCODES_ACTTCAA = tr.createNewVocabularyTerm('ACTTCAA')
vocabulary_term_BARCODES_ACTTCAA.setDescription(None)
vocabulary_term_BARCODES_ACTTCAA.setLabel('10 ACTTCA')
vocabulary_term_BARCODES_ACTTCAA.setOrdinal(38)
vocabulary_BARCODES.addTerm(vocabulary_term_BARCODES_ACTTCAA)

vocabulary_term_BARCODES_TAGGCATG = tr.createNewVocabularyTerm('TAGGCATG')
vocabulary_term_BARCODES_TAGGCATG.setDescription('Nextera DNA')
vocabulary_term_BARCODES_TAGGCATG.setLabel('Index1 (i7) N706 TAGGCATG')
vocabulary_term_BARCODES_TAGGCATG.setOrdinal(30)
vocabulary_BARCODES.addTerm(vocabulary_term_BARCODES_TAGGCATG)

vocabulary_term_BARCODES_CAACTAA = tr.createNewVocabularyTerm('CAACTAA')
vocabulary_term_BARCODES_CAACTAA.setDescription(None)
vocabulary_term_BARCODES_CAACTAA.setLabel('DNA Adapter 29')
vocabulary_term_BARCODES_CAACTAA.setOrdinal(71)
vocabulary_BARCODES.addTerm(vocabulary_term_BARCODES_CAACTAA)

vocabulary_CASAVA_VERSION = tr.createNewVocabulary('CASAVA_VERSION')
vocabulary_CASAVA_VERSION.setDescription('Post analyzing software')
vocabulary_CASAVA_VERSION.setUrlTemplate(None)
vocabulary_CASAVA_VERSION.setManagedInternally(False)
vocabulary_CASAVA_VERSION.setInternalNamespace(False)
vocabulary_CASAVA_VERSION.setChosenFromList(True)

vocabulary_term_CASAVA_VERSION_17 = tr.createNewVocabularyTerm('1.7')
vocabulary_term_CASAVA_VERSION_17.setDescription(None)
vocabulary_term_CASAVA_VERSION_17.setLabel(None)
vocabulary_term_CASAVA_VERSION_17.setOrdinal(2)
vocabulary_CASAVA_VERSION.addTerm(vocabulary_term_CASAVA_VERSION_17)

vocabulary_term_CASAVA_VERSION_18 = tr.createNewVocabularyTerm('1.8')
vocabulary_term_CASAVA_VERSION_18.setDescription(None)
vocabulary_term_CASAVA_VERSION_18.setLabel(None)
vocabulary_term_CASAVA_VERSION_18.setOrdinal(1)
vocabulary_CASAVA_VERSION.addTerm(vocabulary_term_CASAVA_VERSION_18)

vocabulary_CLUSTER_GENERATION_KIT_VERSION = tr.createNewVocabulary('CLUSTER_GENERATION_KIT_VERSION')
vocabulary_CLUSTER_GENERATION_KIT_VERSION.setDescription('Version of the Cluster Generation Kit')
vocabulary_CLUSTER_GENERATION_KIT_VERSION.setUrlTemplate(None)
vocabulary_CLUSTER_GENERATION_KIT_VERSION.setManagedInternally(False)
vocabulary_CLUSTER_GENERATION_KIT_VERSION.setInternalNamespace(False)
vocabulary_CLUSTER_GENERATION_KIT_VERSION.setChosenFromList(True)

vocabulary_term_CLUSTER_GENERATION_KIT_VERSION_TRUSEQ_CBOT_HS_V25 = tr.createNewVocabularyTerm('TRUSEQ_CBOT_HS_V2.5')
vocabulary_term_CLUSTER_GENERATION_KIT_VERSION_TRUSEQ_CBOT_HS_V25.setDescription(None)
vocabulary_term_CLUSTER_GENERATION_KIT_VERSION_TRUSEQ_CBOT_HS_V25.setLabel('TruSeq cBot-HS v2.5')
vocabulary_term_CLUSTER_GENERATION_KIT_VERSION_TRUSEQ_CBOT_HS_V25.setOrdinal(2)
vocabulary_CLUSTER_GENERATION_KIT_VERSION.addTerm(vocabulary_term_CLUSTER_GENERATION_KIT_VERSION_TRUSEQ_CBOT_HS_V25)

vocabulary_term_CLUSTER_GENERATION_KIT_VERSION_V2 = tr.createNewVocabularyTerm('V2')
vocabulary_term_CLUSTER_GENERATION_KIT_VERSION_V2.setDescription(None)
vocabulary_term_CLUSTER_GENERATION_KIT_VERSION_V2.setLabel(None)
vocabulary_term_CLUSTER_GENERATION_KIT_VERSION_V2.setOrdinal(16)
vocabulary_CLUSTER_GENERATION_KIT_VERSION.addTerm(vocabulary_term_CLUSTER_GENERATION_KIT_VERSION_V2)

vocabulary_term_CLUSTER_GENERATION_KIT_VERSION_TRUSEQ_CBOT_HS_V3 = tr.createNewVocabularyTerm('TRUSEQ_CBOT_HS_V3')
vocabulary_term_CLUSTER_GENERATION_KIT_VERSION_TRUSEQ_CBOT_HS_V3.setDescription(None)
vocabulary_term_CLUSTER_GENERATION_KIT_VERSION_TRUSEQ_CBOT_HS_V3.setLabel('TruSeq cBot-HS v3')
vocabulary_term_CLUSTER_GENERATION_KIT_VERSION_TRUSEQ_CBOT_HS_V3.setOrdinal(1)
vocabulary_CLUSTER_GENERATION_KIT_VERSION.addTerm(vocabulary_term_CLUSTER_GENERATION_KIT_VERSION_TRUSEQ_CBOT_HS_V3)

vocabulary_term_CLUSTER_GENERATION_KIT_VERSION_TRUSEQ_CS_GA_V5 = tr.createNewVocabularyTerm('TRUSEQ_CS_GA_V5')
vocabulary_term_CLUSTER_GENERATION_KIT_VERSION_TRUSEQ_CS_GA_V5.setDescription(None)
vocabulary_term_CLUSTER_GENERATION_KIT_VERSION_TRUSEQ_CS_GA_V5.setLabel('TrueSeq CS-GA v5')
vocabulary_term_CLUSTER_GENERATION_KIT_VERSION_TRUSEQ_CS_GA_V5.setOrdinal(8)
vocabulary_CLUSTER_GENERATION_KIT_VERSION.addTerm(vocabulary_term_CLUSTER_GENERATION_KIT_VERSION_TRUSEQ_CS_GA_V5)

vocabulary_term_CLUSTER_GENERATION_KIT_VERSION_V5 = tr.createNewVocabularyTerm('V5')
vocabulary_term_CLUSTER_GENERATION_KIT_VERSION_V5.setDescription(None)
vocabulary_term_CLUSTER_GENERATION_KIT_VERSION_V5.setLabel(None)
vocabulary_term_CLUSTER_GENERATION_KIT_VERSION_V5.setOrdinal(14)
vocabulary_CLUSTER_GENERATION_KIT_VERSION.addTerm(vocabulary_term_CLUSTER_GENERATION_KIT_VERSION_V5)

vocabulary_term_CLUSTER_GENERATION_KIT_VERSION_TRUSEQ_CBOT_GA_V2 = tr.createNewVocabularyTerm('TRUSEQ_CBOT_GA_V2')
vocabulary_term_CLUSTER_GENERATION_KIT_VERSION_TRUSEQ_CBOT_GA_V2.setDescription(None)
vocabulary_term_CLUSTER_GENERATION_KIT_VERSION_TRUSEQ_CBOT_GA_V2.setLabel('TrueSeq cBot-GA v2')
vocabulary_term_CLUSTER_GENERATION_KIT_VERSION_TRUSEQ_CBOT_GA_V2.setOrdinal(7)
vocabulary_CLUSTER_GENERATION_KIT_VERSION.addTerm(vocabulary_term_CLUSTER_GENERATION_KIT_VERSION_TRUSEQ_CBOT_GA_V2)

vocabulary_term_CLUSTER_GENERATION_KIT_VERSION_V4 = tr.createNewVocabularyTerm('V4')
vocabulary_term_CLUSTER_GENERATION_KIT_VERSION_V4.setDescription(None)
vocabulary_term_CLUSTER_GENERATION_KIT_VERSION_V4.setLabel(None)
vocabulary_term_CLUSTER_GENERATION_KIT_VERSION_V4.setOrdinal(15)
vocabulary_CLUSTER_GENERATION_KIT_VERSION.addTerm(vocabulary_term_CLUSTER_GENERATION_KIT_VERSION_V4)

vocabulary_CLUSTER_STATION = tr.createNewVocabulary('CLUSTER_STATION')
vocabulary_CLUSTER_STATION.setDescription('Cluster Station')
vocabulary_CLUSTER_STATION.setUrlTemplate(None)
vocabulary_CLUSTER_STATION.setManagedInternally(False)
vocabulary_CLUSTER_STATION.setInternalNamespace(False)
vocabulary_CLUSTER_STATION.setChosenFromList(True)

vocabulary_term_CLUSTER_STATION_CBOT = tr.createNewVocabularyTerm('CBOT')
vocabulary_term_CLUSTER_STATION_CBOT.setDescription(None)
vocabulary_term_CLUSTER_STATION_CBOT.setLabel('cBot')
vocabulary_term_CLUSTER_STATION_CBOT.setOrdinal(1)
vocabulary_CLUSTER_STATION.addTerm(vocabulary_term_CLUSTER_STATION_CBOT)

vocabulary_term_CLUSTER_STATION_SPUTNIK = tr.createNewVocabularyTerm('SPUTNIK')
vocabulary_term_CLUSTER_STATION_SPUTNIK.setDescription(None)
vocabulary_term_CLUSTER_STATION_SPUTNIK.setLabel('Sputnik')
vocabulary_term_CLUSTER_STATION_SPUTNIK.setOrdinal(2)
vocabulary_CLUSTER_STATION.addTerm(vocabulary_term_CLUSTER_STATION_SPUTNIK)

vocabulary_term_CLUSTER_STATION_ISS = tr.createNewVocabularyTerm('ISS')
vocabulary_term_CLUSTER_STATION_ISS.setDescription(None)
vocabulary_term_CLUSTER_STATION_ISS.setLabel('ISS (CS2)')
vocabulary_term_CLUSTER_STATION_ISS.setOrdinal(3)
vocabulary_CLUSTER_STATION.addTerm(vocabulary_term_CLUSTER_STATION_ISS)

vocabulary_CLUSTER_STATION_SOFTWARE_VERSION = tr.createNewVocabulary('CLUSTER_STATION_SOFTWARE_VERSION')
vocabulary_CLUSTER_STATION_SOFTWARE_VERSION.setDescription(None)
vocabulary_CLUSTER_STATION_SOFTWARE_VERSION.setUrlTemplate(None)
vocabulary_CLUSTER_STATION_SOFTWARE_VERSION.setManagedInternally(False)
vocabulary_CLUSTER_STATION_SOFTWARE_VERSION.setInternalNamespace(False)
vocabulary_CLUSTER_STATION_SOFTWARE_VERSION.setChosenFromList(True)

vocabulary_term_CLUSTER_STATION_SOFTWARE_VERSION_NOT_KNOWN = tr.createNewVocabularyTerm('NOT_KNOWN')
vocabulary_term_CLUSTER_STATION_SOFTWARE_VERSION_NOT_KNOWN.setDescription(None)
vocabulary_term_CLUSTER_STATION_SOFTWARE_VERSION_NOT_KNOWN.setLabel(None)
vocabulary_term_CLUSTER_STATION_SOFTWARE_VERSION_NOT_KNOWN.setOrdinal(6)
vocabulary_CLUSTER_STATION_SOFTWARE_VERSION.addTerm(vocabulary_term_CLUSTER_STATION_SOFTWARE_VERSION_NOT_KNOWN)

vocabulary_term_CLUSTER_STATION_SOFTWARE_VERSION_GALAXY_06 = tr.createNewVocabularyTerm('GALAXY_0.6')
vocabulary_term_CLUSTER_STATION_SOFTWARE_VERSION_GALAXY_06.setDescription('Old Cluster Station')
vocabulary_term_CLUSTER_STATION_SOFTWARE_VERSION_GALAXY_06.setLabel('Galaxy 0.6 Build 98')
vocabulary_term_CLUSTER_STATION_SOFTWARE_VERSION_GALAXY_06.setOrdinal(3)
vocabulary_CLUSTER_STATION_SOFTWARE_VERSION.addTerm(vocabulary_term_CLUSTER_STATION_SOFTWARE_VERSION_GALAXY_06)

vocabulary_term_CLUSTER_STATION_SOFTWARE_VERSION_CBOT_14360 = tr.createNewVocabularyTerm('CBOT_1.4.36.0')
vocabulary_term_CLUSTER_STATION_SOFTWARE_VERSION_CBOT_14360.setDescription(None)
vocabulary_term_CLUSTER_STATION_SOFTWARE_VERSION_CBOT_14360.setLabel('cBot 1.4.36.0')
vocabulary_term_CLUSTER_STATION_SOFTWARE_VERSION_CBOT_14360.setOrdinal(1)
vocabulary_CLUSTER_STATION_SOFTWARE_VERSION.addTerm(vocabulary_term_CLUSTER_STATION_SOFTWARE_VERSION_CBOT_14360)

vocabulary_CONTROL_LANE = tr.createNewVocabulary('CONTROL_LANE')
vocabulary_CONTROL_LANE.setDescription(None)
vocabulary_CONTROL_LANE.setUrlTemplate(None)
vocabulary_CONTROL_LANE.setManagedInternally(False)
vocabulary_CONTROL_LANE.setInternalNamespace(False)
vocabulary_CONTROL_LANE.setChosenFromList(True)

vocabulary_term_CONTROL_LANE_0 = tr.createNewVocabularyTerm('0')
vocabulary_term_CONTROL_LANE_0.setDescription(None)
vocabulary_term_CONTROL_LANE_0.setLabel(None)
vocabulary_term_CONTROL_LANE_0.setOrdinal(10)
vocabulary_CONTROL_LANE.addTerm(vocabulary_term_CONTROL_LANE_0)

vocabulary_term_CONTROL_LANE_6 = tr.createNewVocabularyTerm('6')
vocabulary_term_CONTROL_LANE_6.setDescription(None)
vocabulary_term_CONTROL_LANE_6.setLabel(None)
vocabulary_term_CONTROL_LANE_6.setOrdinal(6)
vocabulary_CONTROL_LANE.addTerm(vocabulary_term_CONTROL_LANE_6)

vocabulary_term_CONTROL_LANE_4 = tr.createNewVocabularyTerm('4')
vocabulary_term_CONTROL_LANE_4.setDescription(None)
vocabulary_term_CONTROL_LANE_4.setLabel(None)
vocabulary_term_CONTROL_LANE_4.setOrdinal(4)
vocabulary_CONTROL_LANE.addTerm(vocabulary_term_CONTROL_LANE_4)

vocabulary_term_CONTROL_LANE_2 = tr.createNewVocabularyTerm('2')
vocabulary_term_CONTROL_LANE_2.setDescription(None)
vocabulary_term_CONTROL_LANE_2.setLabel(None)
vocabulary_term_CONTROL_LANE_2.setOrdinal(2)
vocabulary_CONTROL_LANE.addTerm(vocabulary_term_CONTROL_LANE_2)

vocabulary_term_CONTROL_LANE_8 = tr.createNewVocabularyTerm('8')
vocabulary_term_CONTROL_LANE_8.setDescription(None)
vocabulary_term_CONTROL_LANE_8.setLabel(None)
vocabulary_term_CONTROL_LANE_8.setOrdinal(8)
vocabulary_CONTROL_LANE.addTerm(vocabulary_term_CONTROL_LANE_8)

vocabulary_term_CONTROL_LANE_NONE = tr.createNewVocabularyTerm('NONE')
vocabulary_term_CONTROL_LANE_NONE.setDescription(None)
vocabulary_term_CONTROL_LANE_NONE.setLabel(None)
vocabulary_term_CONTROL_LANE_NONE.setOrdinal(9)
vocabulary_CONTROL_LANE.addTerm(vocabulary_term_CONTROL_LANE_NONE)

vocabulary_term_CONTROL_LANE_3 = tr.createNewVocabularyTerm('3')
vocabulary_term_CONTROL_LANE_3.setDescription(None)
vocabulary_term_CONTROL_LANE_3.setLabel(None)
vocabulary_term_CONTROL_LANE_3.setOrdinal(3)
vocabulary_CONTROL_LANE.addTerm(vocabulary_term_CONTROL_LANE_3)

vocabulary_term_CONTROL_LANE_5 = tr.createNewVocabularyTerm('5')
vocabulary_term_CONTROL_LANE_5.setDescription(None)
vocabulary_term_CONTROL_LANE_5.setLabel(None)
vocabulary_term_CONTROL_LANE_5.setOrdinal(5)
vocabulary_CONTROL_LANE.addTerm(vocabulary_term_CONTROL_LANE_5)

vocabulary_term_CONTROL_LANE_1 = tr.createNewVocabularyTerm('1')
vocabulary_term_CONTROL_LANE_1.setDescription(None)
vocabulary_term_CONTROL_LANE_1.setLabel(None)
vocabulary_term_CONTROL_LANE_1.setOrdinal(1)
vocabulary_CONTROL_LANE.addTerm(vocabulary_term_CONTROL_LANE_1)

vocabulary_term_CONTROL_LANE_7 = tr.createNewVocabularyTerm('7')
vocabulary_term_CONTROL_LANE_7.setDescription(None)
vocabulary_term_CONTROL_LANE_7.setLabel(None)
vocabulary_term_CONTROL_LANE_7.setOrdinal(7)
vocabulary_CONTROL_LANE.addTerm(vocabulary_term_CONTROL_LANE_7)

vocabulary_CYCLES = tr.createNewVocabulary('CYCLES')
vocabulary_CYCLES.setDescription('Number of cycles')
vocabulary_CYCLES.setUrlTemplate(None)
vocabulary_CYCLES.setManagedInternally(False)
vocabulary_CYCLES.setInternalNamespace(False)
vocabulary_CYCLES.setChosenFromList(True)

vocabulary_term_CYCLES_101 = tr.createNewVocabularyTerm('101')
vocabulary_term_CYCLES_101.setDescription(None)
vocabulary_term_CYCLES_101.setLabel(None)
vocabulary_term_CYCLES_101.setOrdinal(4)
vocabulary_CYCLES.addTerm(vocabulary_term_CYCLES_101)

vocabulary_term_CYCLES_51 = tr.createNewVocabularyTerm('51')
vocabulary_term_CYCLES_51.setDescription(None)
vocabulary_term_CYCLES_51.setLabel(None)
vocabulary_term_CYCLES_51.setOrdinal(6)
vocabulary_CYCLES.addTerm(vocabulary_term_CYCLES_51)

vocabulary_term_CYCLES_58 = tr.createNewVocabularyTerm('58')
vocabulary_term_CYCLES_58.setDescription(None)
vocabulary_term_CYCLES_58.setLabel(None)
vocabulary_term_CYCLES_58.setOrdinal(7)
vocabulary_CYCLES.addTerm(vocabulary_term_CYCLES_58)

vocabulary_term_CYCLES_100 = tr.createNewVocabularyTerm('100')
vocabulary_term_CYCLES_100.setDescription(None)
vocabulary_term_CYCLES_100.setLabel(None)
vocabulary_term_CYCLES_100.setOrdinal(12)
vocabulary_CYCLES.addTerm(vocabulary_term_CYCLES_100)

vocabulary_term_CYCLES_151 = tr.createNewVocabularyTerm('151')
vocabulary_term_CYCLES_151.setDescription(None)
vocabulary_term_CYCLES_151.setLabel(None)
vocabulary_term_CYCLES_151.setOrdinal(11)
vocabulary_CYCLES.addTerm(vocabulary_term_CYCLES_151)

vocabulary_term_CYCLES_49 = tr.createNewVocabularyTerm('49')
vocabulary_term_CYCLES_49.setDescription(None)
vocabulary_term_CYCLES_49.setLabel(None)
vocabulary_term_CYCLES_49.setOrdinal(15)
vocabulary_CYCLES.addTerm(vocabulary_term_CYCLES_49)

vocabulary_term_CYCLES_50 = tr.createNewVocabularyTerm('50')
vocabulary_term_CYCLES_50.setDescription(None)
vocabulary_term_CYCLES_50.setLabel(None)
vocabulary_term_CYCLES_50.setOrdinal(2)
vocabulary_CYCLES.addTerm(vocabulary_term_CYCLES_50)

vocabulary_term_CYCLES_150 = tr.createNewVocabularyTerm('150')
vocabulary_term_CYCLES_150.setDescription(None)
vocabulary_term_CYCLES_150.setLabel(None)
vocabulary_term_CYCLES_150.setOrdinal(14)
vocabulary_CYCLES.addTerm(vocabulary_term_CYCLES_150)

vocabulary_term_CYCLES_36 = tr.createNewVocabularyTerm('36')
vocabulary_term_CYCLES_36.setDescription(None)
vocabulary_term_CYCLES_36.setLabel(None)
vocabulary_term_CYCLES_36.setOrdinal(1)
vocabulary_CYCLES.addTerm(vocabulary_term_CYCLES_36)

vocabulary_term_CYCLES_108 = tr.createNewVocabularyTerm('108')
vocabulary_term_CYCLES_108.setDescription(None)
vocabulary_term_CYCLES_108.setLabel(None)
vocabulary_term_CYCLES_108.setOrdinal(13)
vocabulary_CYCLES.addTerm(vocabulary_term_CYCLES_108)

vocabulary_term_CYCLES_76 = tr.createNewVocabularyTerm('76')
vocabulary_term_CYCLES_76.setDescription(None)
vocabulary_term_CYCLES_76.setLabel(None)
vocabulary_term_CYCLES_76.setOrdinal(3)
vocabulary_CYCLES.addTerm(vocabulary_term_CYCLES_76)

vocabulary_term_CYCLES_125 = tr.createNewVocabularyTerm('125')
vocabulary_term_CYCLES_125.setDescription(None)
vocabulary_term_CYCLES_125.setLabel(None)
vocabulary_term_CYCLES_125.setOrdinal(10)
vocabulary_CYCLES.addTerm(vocabulary_term_CYCLES_125)

vocabulary_END_TYPE = tr.createNewVocabulary('END_TYPE')
vocabulary_END_TYPE.setDescription('Sequencing method')
vocabulary_END_TYPE.setUrlTemplate(None)
vocabulary_END_TYPE.setManagedInternally(False)
vocabulary_END_TYPE.setInternalNamespace(False)
vocabulary_END_TYPE.setChosenFromList(True)

vocabulary_term_END_TYPE_PAIRED_END = tr.createNewVocabularyTerm('PAIRED_END')
vocabulary_term_END_TYPE_PAIRED_END.setDescription(None)
vocabulary_term_END_TYPE_PAIRED_END.setLabel(None)
vocabulary_term_END_TYPE_PAIRED_END.setOrdinal(3)
vocabulary_END_TYPE.addTerm(vocabulary_term_END_TYPE_PAIRED_END)

vocabulary_term_END_TYPE_SINGLE_READ = tr.createNewVocabularyTerm('SINGLE_READ')
vocabulary_term_END_TYPE_SINGLE_READ.setDescription(None)
vocabulary_term_END_TYPE_SINGLE_READ.setLabel(None)
vocabulary_term_END_TYPE_SINGLE_READ.setOrdinal(2)
vocabulary_END_TYPE.addTerm(vocabulary_term_END_TYPE_SINGLE_READ)

vocabulary_EXPERIMENT_DESIGN = tr.createNewVocabulary('EXPERIMENT_DESIGN')
vocabulary_EXPERIMENT_DESIGN.setDescription('General Intent')
vocabulary_EXPERIMENT_DESIGN.setUrlTemplate(None)
vocabulary_EXPERIMENT_DESIGN.setManagedInternally(False)
vocabulary_EXPERIMENT_DESIGN.setInternalNamespace(False)
vocabulary_EXPERIMENT_DESIGN.setChosenFromList(True)

vocabulary_term_EXPERIMENT_DESIGN_OTHER = tr.createNewVocabularyTerm('OTHER')
vocabulary_term_EXPERIMENT_DESIGN_OTHER.setDescription(None)
vocabulary_term_EXPERIMENT_DESIGN_OTHER.setLabel('Other')
vocabulary_term_EXPERIMENT_DESIGN_OTHER.setOrdinal(9)
vocabulary_EXPERIMENT_DESIGN.addTerm(vocabulary_term_EXPERIMENT_DESIGN_OTHER)

vocabulary_term_EXPERIMENT_DESIGN_BINDING_SITE_IDENTIFICATION = tr.createNewVocabularyTerm('BINDING_SITE_IDENTIFICATION')
vocabulary_term_EXPERIMENT_DESIGN_BINDING_SITE_IDENTIFICATION.setDescription(None)
vocabulary_term_EXPERIMENT_DESIGN_BINDING_SITE_IDENTIFICATION.setLabel('Binding Site Identification')
vocabulary_term_EXPERIMENT_DESIGN_BINDING_SITE_IDENTIFICATION.setOrdinal(1)
vocabulary_EXPERIMENT_DESIGN.addTerm(vocabulary_term_EXPERIMENT_DESIGN_BINDING_SITE_IDENTIFICATION)

vocabulary_term_EXPERIMENT_DESIGN_SEQUENCE_ENRICHMENT = tr.createNewVocabularyTerm('SEQUENCE_ENRICHMENT')
vocabulary_term_EXPERIMENT_DESIGN_SEQUENCE_ENRICHMENT.setDescription(None)
vocabulary_term_EXPERIMENT_DESIGN_SEQUENCE_ENRICHMENT.setLabel('Sequence Enrichment')
vocabulary_term_EXPERIMENT_DESIGN_SEQUENCE_ENRICHMENT.setOrdinal(7)
vocabulary_EXPERIMENT_DESIGN.addTerm(vocabulary_term_EXPERIMENT_DESIGN_SEQUENCE_ENRICHMENT)

vocabulary_term_EXPERIMENT_DESIGN_TRANSCRIPT_IDENTIFICATION = tr.createNewVocabularyTerm('TRANSCRIPT_IDENTIFICATION')
vocabulary_term_EXPERIMENT_DESIGN_TRANSCRIPT_IDENTIFICATION.setDescription(None)
vocabulary_term_EXPERIMENT_DESIGN_TRANSCRIPT_IDENTIFICATION.setLabel('Transcript Identification')
vocabulary_term_EXPERIMENT_DESIGN_TRANSCRIPT_IDENTIFICATION.setOrdinal(8)
vocabulary_EXPERIMENT_DESIGN.addTerm(vocabulary_term_EXPERIMENT_DESIGN_TRANSCRIPT_IDENTIFICATION)

vocabulary_term_EXPERIMENT_DESIGN_EXPRESSION = tr.createNewVocabularyTerm('EXPRESSION')
vocabulary_term_EXPERIMENT_DESIGN_EXPRESSION.setDescription(None)
vocabulary_term_EXPERIMENT_DESIGN_EXPRESSION.setLabel('Expression')
vocabulary_term_EXPERIMENT_DESIGN_EXPRESSION.setOrdinal(6)
vocabulary_EXPERIMENT_DESIGN.addTerm(vocabulary_term_EXPERIMENT_DESIGN_EXPRESSION)

vocabulary_term_EXPERIMENT_DESIGN_DIFFERENTIAL_SPLICING = tr.createNewVocabularyTerm('DIFFERENTIAL_SPLICING')
vocabulary_term_EXPERIMENT_DESIGN_DIFFERENTIAL_SPLICING.setDescription(None)
vocabulary_term_EXPERIMENT_DESIGN_DIFFERENTIAL_SPLICING.setLabel('Differential Splicing')
vocabulary_term_EXPERIMENT_DESIGN_DIFFERENTIAL_SPLICING.setOrdinal(5)
vocabulary_EXPERIMENT_DESIGN.addTerm(vocabulary_term_EXPERIMENT_DESIGN_DIFFERENTIAL_SPLICING)

vocabulary_term_EXPERIMENT_DESIGN_COMPARATIVE_GENOMIC_HYBRIDIZATION = tr.createNewVocabularyTerm('COMPARATIVE_GENOMIC_HYBRIDIZATION')
vocabulary_term_EXPERIMENT_DESIGN_COMPARATIVE_GENOMIC_HYBRIDIZATION.setDescription(None)
vocabulary_term_EXPERIMENT_DESIGN_COMPARATIVE_GENOMIC_HYBRIDIZATION.setLabel('Comparative Genomic Hybridization')
vocabulary_term_EXPERIMENT_DESIGN_COMPARATIVE_GENOMIC_HYBRIDIZATION.setOrdinal(3)
vocabulary_EXPERIMENT_DESIGN.addTerm(vocabulary_term_EXPERIMENT_DESIGN_COMPARATIVE_GENOMIC_HYBRIDIZATION)

vocabulary_term_EXPERIMENT_DESIGN_DIFFERENTIAL_EXPRESSION = tr.createNewVocabularyTerm('DIFFERENTIAL_EXPRESSION')
vocabulary_term_EXPERIMENT_DESIGN_DIFFERENTIAL_EXPRESSION.setDescription(None)
vocabulary_term_EXPERIMENT_DESIGN_DIFFERENTIAL_EXPRESSION.setLabel('Differential Expression')
vocabulary_term_EXPERIMENT_DESIGN_DIFFERENTIAL_EXPRESSION.setOrdinal(4)
vocabulary_EXPERIMENT_DESIGN.addTerm(vocabulary_term_EXPERIMENT_DESIGN_DIFFERENTIAL_EXPRESSION)

vocabulary_term_EXPERIMENT_DESIGN_CHROMATIN_MARKS = tr.createNewVocabularyTerm('CHROMATIN_MARKS')
vocabulary_term_EXPERIMENT_DESIGN_CHROMATIN_MARKS.setDescription(None)
vocabulary_term_EXPERIMENT_DESIGN_CHROMATIN_MARKS.setLabel('Chromatin Marks')
vocabulary_term_EXPERIMENT_DESIGN_CHROMATIN_MARKS.setOrdinal(2)
vocabulary_EXPERIMENT_DESIGN.addTerm(vocabulary_term_EXPERIMENT_DESIGN_CHROMATIN_MARKS)

vocabulary_INDEX2 = tr.createNewVocabulary('INDEX2')
vocabulary_INDEX2.setDescription('Index 2 for Illumina Dual Indexing')
vocabulary_INDEX2.setUrlTemplate(None)
vocabulary_INDEX2.setManagedInternally(False)
vocabulary_INDEX2.setInternalNamespace(False)
vocabulary_INDEX2.setChosenFromList(True)

vocabulary_term_INDEX2_ACTGCATA = tr.createNewVocabularyTerm('ACTGCATA')
vocabulary_term_INDEX2_ACTGCATA.setDescription('Nextera DNA')
vocabulary_term_INDEX2_ACTGCATA.setLabel('Index2 (i5) N506 ACTGCATA')
vocabulary_term_INDEX2_ACTGCATA.setOrdinal(6)
vocabulary_INDEX2.addTerm(vocabulary_term_INDEX2_ACTGCATA)

vocabulary_term_INDEX2_TATCCTCT = tr.createNewVocabularyTerm('TATCCTCT')
vocabulary_term_INDEX2_TATCCTCT.setDescription('Nextera DNA')
vocabulary_term_INDEX2_TATCCTCT.setLabel('Index2 (i5) N503 TATCCTCT')
vocabulary_term_INDEX2_TATCCTCT.setOrdinal(3)
vocabulary_INDEX2.addTerm(vocabulary_term_INDEX2_TATCCTCT)

vocabulary_term_INDEX2_GTAAGGAG = tr.createNewVocabularyTerm('GTAAGGAG')
vocabulary_term_INDEX2_GTAAGGAG.setDescription('Nextera DNA')
vocabulary_term_INDEX2_GTAAGGAG.setLabel('Index2 (i5) N505 GTAAGGAG')
vocabulary_term_INDEX2_GTAAGGAG.setOrdinal(5)
vocabulary_INDEX2.addTerm(vocabulary_term_INDEX2_GTAAGGAG)

vocabulary_term_INDEX2_AGAGTAGA = tr.createNewVocabularyTerm('AGAGTAGA')
vocabulary_term_INDEX2_AGAGTAGA.setDescription('Nextera DNA')
vocabulary_term_INDEX2_AGAGTAGA.setLabel('Index2 (i5) N504 AGAGTAGA')
vocabulary_term_INDEX2_AGAGTAGA.setOrdinal(4)
vocabulary_INDEX2.addTerm(vocabulary_term_INDEX2_AGAGTAGA)

vocabulary_term_INDEX2_CTCTCTAT = tr.createNewVocabularyTerm('CTCTCTAT')
vocabulary_term_INDEX2_CTCTCTAT.setDescription('Nextera DNA')
vocabulary_term_INDEX2_CTCTCTAT.setLabel('Index2 (i5) N502 CTCTCTAT')
vocabulary_term_INDEX2_CTCTCTAT.setOrdinal(2)
vocabulary_INDEX2.addTerm(vocabulary_term_INDEX2_CTCTCTAT)

vocabulary_term_INDEX2_CTAAGCCT = tr.createNewVocabularyTerm('CTAAGCCT')
vocabulary_term_INDEX2_CTAAGCCT.setDescription('Nextera DNA')
vocabulary_term_INDEX2_CTAAGCCT.setLabel('Index2 (i5) N508 CTAAGCCT')
vocabulary_term_INDEX2_CTAAGCCT.setOrdinal(8)
vocabulary_INDEX2.addTerm(vocabulary_term_INDEX2_CTAAGCCT)

vocabulary_term_INDEX2_AAGGAGTA = tr.createNewVocabularyTerm('AAGGAGTA')
vocabulary_term_INDEX2_AAGGAGTA.setDescription('Nextera DNA')
vocabulary_term_INDEX2_AAGGAGTA.setLabel('Index2 (i5) N507 AAGGAGTA')
vocabulary_term_INDEX2_AAGGAGTA.setOrdinal(7)
vocabulary_INDEX2.addTerm(vocabulary_term_INDEX2_AAGGAGTA)

vocabulary_term_INDEX2_TAGATCGC = tr.createNewVocabularyTerm('TAGATCGC')
vocabulary_term_INDEX2_TAGATCGC.setDescription('Nextera DNA')
vocabulary_term_INDEX2_TAGATCGC.setLabel('Index2 (i5) N501 TAGATCGC')
vocabulary_term_INDEX2_TAGATCGC.setOrdinal(1)
vocabulary_INDEX2.addTerm(vocabulary_term_INDEX2_TAGATCGC)

vocabulary_KIT = tr.createNewVocabulary('KIT')
vocabulary_KIT.setDescription('Illumina Kit used for preparation')
vocabulary_KIT.setUrlTemplate(None)
vocabulary_KIT.setManagedInternally(False)
vocabulary_KIT.setInternalNamespace(False)
vocabulary_KIT.setChosenFromList(True)

vocabulary_term_KIT_AGILENT_SURESELECT_ENRICHMENTSYSTEM = tr.createNewVocabularyTerm('AGILENT_SURESELECT_ENRICHMENTSYSTEM')
vocabulary_term_KIT_AGILENT_SURESELECT_ENRICHMENTSYSTEM.setDescription(None)
vocabulary_term_KIT_AGILENT_SURESELECT_ENRICHMENTSYSTEM.setLabel('Agilent_SureSelect_EnrichmentSystem')
vocabulary_term_KIT_AGILENT_SURESELECT_ENRICHMENTSYSTEM.setOrdinal(20)
vocabulary_KIT.addTerm(vocabulary_term_KIT_AGILENT_SURESELECT_ENRICHMENTSYSTEM)

vocabulary_term_KIT_NEXTERA_DNA_SAMPLE_PREP_KIT_BUFFER_HMW = tr.createNewVocabularyTerm('NEXTERA_DNA_SAMPLE_PREP_KIT_BUFFER_HMW')
vocabulary_term_KIT_NEXTERA_DNA_SAMPLE_PREP_KIT_BUFFER_HMW.setDescription(None)
vocabulary_term_KIT_NEXTERA_DNA_SAMPLE_PREP_KIT_BUFFER_HMW.setLabel('Nextera Genomic DNA Sample Preparation Kit BufferHMW(Epicentre)')
vocabulary_term_KIT_NEXTERA_DNA_SAMPLE_PREP_KIT_BUFFER_HMW.setOrdinal(19)
vocabulary_KIT.addTerm(vocabulary_term_KIT_NEXTERA_DNA_SAMPLE_PREP_KIT_BUFFER_HMW)

vocabulary_term_KIT_GENOMICDNA_SAMPLE_PREP = tr.createNewVocabularyTerm('GENOMICDNA_SAMPLE_PREP')
vocabulary_term_KIT_GENOMICDNA_SAMPLE_PREP.setDescription(None)
vocabulary_term_KIT_GENOMICDNA_SAMPLE_PREP.setLabel('Illumina Genomic DNA Sample Preparation Kit')
vocabulary_term_KIT_GENOMICDNA_SAMPLE_PREP.setOrdinal(3)
vocabulary_KIT.addTerm(vocabulary_term_KIT_GENOMICDNA_SAMPLE_PREP)

vocabulary_term_KIT_NONE = tr.createNewVocabularyTerm('NONE')
vocabulary_term_KIT_NONE.setDescription(None)
vocabulary_term_KIT_NONE.setLabel('None (Already prepared)')
vocabulary_term_KIT_NONE.setOrdinal(23)
vocabulary_KIT.addTerm(vocabulary_term_KIT_NONE)

vocabulary_term_KIT_NEBNEXT_DNA_SAMPLE_PREP_MASTER_MIX_SET1 = tr.createNewVocabularyTerm('NEBNEXT_DNA_SAMPLE_PREP_MASTER_MIX_SET1')
vocabulary_term_KIT_NEBNEXT_DNA_SAMPLE_PREP_MASTER_MIX_SET1.setDescription(None)
vocabulary_term_KIT_NEBNEXT_DNA_SAMPLE_PREP_MASTER_MIX_SET1.setLabel('NEB Genomic DNA Sample Preparation Kit')
vocabulary_term_KIT_NEBNEXT_DNA_SAMPLE_PREP_MASTER_MIX_SET1.setOrdinal(2)
vocabulary_KIT.addTerm(vocabulary_term_KIT_NEBNEXT_DNA_SAMPLE_PREP_MASTER_MIX_SET1)

vocabulary_term_KIT_PAIRED_END_DNA_SAMPLE_PREP = tr.createNewVocabularyTerm('PAIRED_END_DNA_SAMPLE_PREP')
vocabulary_term_KIT_PAIRED_END_DNA_SAMPLE_PREP.setDescription(None)
vocabulary_term_KIT_PAIRED_END_DNA_SAMPLE_PREP.setLabel('Paired End DNA Sample Prep Oligo Kit')
vocabulary_term_KIT_PAIRED_END_DNA_SAMPLE_PREP.setOrdinal(14)
vocabulary_KIT.addTerm(vocabulary_term_KIT_PAIRED_END_DNA_SAMPLE_PREP)

vocabulary_term_KIT_CHIP_SEQ_SAMPLE_PREP = tr.createNewVocabularyTerm('CHIP_SEQ_SAMPLE_PREP')
vocabulary_term_KIT_CHIP_SEQ_SAMPLE_PREP.setDescription(None)
vocabulary_term_KIT_CHIP_SEQ_SAMPLE_PREP.setLabel('ChIP-Seq Sample Preparation Kit')
vocabulary_term_KIT_CHIP_SEQ_SAMPLE_PREP.setOrdinal(1)
vocabulary_KIT.addTerm(vocabulary_term_KIT_CHIP_SEQ_SAMPLE_PREP)

vocabulary_term_KIT_POLYA_SCRIPTSEQ_MRNASEQ_KIT = tr.createNewVocabularyTerm('POLYA_SCRIPTSEQ_MRNA-SEQ_KIT')
vocabulary_term_KIT_POLYA_SCRIPTSEQ_MRNASEQ_KIT.setDescription(None)
vocabulary_term_KIT_POLYA_SCRIPTSEQ_MRNASEQ_KIT.setLabel('PolyA(Beads) ScriptSeq mRNA-Seq_Epicentre-kit')
vocabulary_term_KIT_POLYA_SCRIPTSEQ_MRNASEQ_KIT.setOrdinal(17)
vocabulary_KIT.addTerm(vocabulary_term_KIT_POLYA_SCRIPTSEQ_MRNASEQ_KIT)

vocabulary_term_KIT_MRNA_SEQ_SAMPLE_PREP = tr.createNewVocabularyTerm('MRNA_SEQ_SAMPLE_PREP')
vocabulary_term_KIT_MRNA_SEQ_SAMPLE_PREP.setDescription(None)
vocabulary_term_KIT_MRNA_SEQ_SAMPLE_PREP.setLabel('mRNA-Seq Sample Preparation Kit')
vocabulary_term_KIT_MRNA_SEQ_SAMPLE_PREP.setOrdinal(15)
vocabulary_KIT.addTerm(vocabulary_term_KIT_MRNA_SEQ_SAMPLE_PREP)

vocabulary_term_KIT_TRUSEQ_DNA_SAMPLE_PREP_KIT = tr.createNewVocabularyTerm('TRUSEQ_DNA_SAMPLE_PREP_KIT')
vocabulary_term_KIT_TRUSEQ_DNA_SAMPLE_PREP_KIT.setDescription(None)
vocabulary_term_KIT_TRUSEQ_DNA_SAMPLE_PREP_KIT.setLabel('TruSeq_DNA_SamplePrepKit_Illumina')
vocabulary_term_KIT_TRUSEQ_DNA_SAMPLE_PREP_KIT.setOrdinal(22)
vocabulary_KIT.addTerm(vocabulary_term_KIT_TRUSEQ_DNA_SAMPLE_PREP_KIT)

vocabulary_term_KIT_TRUSEQRNA_SAMPLE_PREP_KIT = tr.createNewVocabularyTerm('TRUSEQRNA_SAMPLE_PREP_KIT')
vocabulary_term_KIT_TRUSEQRNA_SAMPLE_PREP_KIT.setDescription(None)
vocabulary_term_KIT_TRUSEQRNA_SAMPLE_PREP_KIT.setLabel('TruSeq_RNA_SamplePrepKit_Illumina')
vocabulary_term_KIT_TRUSEQRNA_SAMPLE_PREP_KIT.setOrdinal(21)
vocabulary_KIT.addTerm(vocabulary_term_KIT_TRUSEQRNA_SAMPLE_PREP_KIT)

vocabulary_term_KIT_NEXTERA_DNA_SAMPLE_PREP_KITS = tr.createNewVocabularyTerm('NEXTERA_DNA_SAMPLE_PREP_KITS')
vocabulary_term_KIT_NEXTERA_DNA_SAMPLE_PREP_KITS.setDescription(None)
vocabulary_term_KIT_NEXTERA_DNA_SAMPLE_PREP_KITS.setLabel('Nextera Genomic DNA Sample Preparation Kit BufferLMW(Epicentre)')
vocabulary_term_KIT_NEXTERA_DNA_SAMPLE_PREP_KITS.setOrdinal(18)
vocabulary_KIT.addTerm(vocabulary_term_KIT_NEXTERA_DNA_SAMPLE_PREP_KITS)

vocabulary_term_KIT_RIBOZERO_SCRIPTSEQ_MRNASEQ_KIT = tr.createNewVocabularyTerm('RIBOZERO_SCRIPTSEQ_MRNA-SEQ_KIT')
vocabulary_term_KIT_RIBOZERO_SCRIPTSEQ_MRNASEQ_KIT.setDescription(None)
vocabulary_term_KIT_RIBOZERO_SCRIPTSEQ_MRNASEQ_KIT.setLabel('RiboZero ScriptSeq mRNA-Seq_Epicentre-kit')
vocabulary_term_KIT_RIBOZERO_SCRIPTSEQ_MRNASEQ_KIT.setOrdinal(16)
vocabulary_KIT.addTerm(vocabulary_term_KIT_RIBOZERO_SCRIPTSEQ_MRNASEQ_KIT)

vocabulary_MACS_VERSION = tr.createNewVocabulary('MACS_VERSION')
vocabulary_MACS_VERSION.setDescription('Used MACS version for Peak Calling')
vocabulary_MACS_VERSION.setUrlTemplate(None)
vocabulary_MACS_VERSION.setManagedInternally(False)
vocabulary_MACS_VERSION.setInternalNamespace(False)
vocabulary_MACS_VERSION.setChosenFromList(True)

vocabulary_term_MACS_VERSION_1371 = tr.createNewVocabularyTerm('1.3.7.1')
vocabulary_term_MACS_VERSION_1371.setDescription(None)
vocabulary_term_MACS_VERSION_1371.setLabel('macs 1.3.7.1 (Oktoberfest, bug fixed #1)')
vocabulary_term_MACS_VERSION_1371.setOrdinal(4)
vocabulary_MACS_VERSION.addTerm(vocabulary_term_MACS_VERSION_1371)

vocabulary_term_MACS_VERSION_140RC2 = tr.createNewVocabularyTerm('1.4.0RC2')
vocabulary_term_MACS_VERSION_140RC2.setDescription(None)
vocabulary_term_MACS_VERSION_140RC2.setLabel('macs14 1.4.0rc2 20110214 (Valentine)')
vocabulary_term_MACS_VERSION_140RC2.setOrdinal(3)
vocabulary_MACS_VERSION.addTerm(vocabulary_term_MACS_VERSION_140RC2)

vocabulary_MISMATCH_IN_INDEX = tr.createNewVocabulary('MISMATCH_IN_INDEX')
vocabulary_MISMATCH_IN_INDEX.setDescription('Mismatch in Index allowed')
vocabulary_MISMATCH_IN_INDEX.setUrlTemplate(None)
vocabulary_MISMATCH_IN_INDEX.setManagedInternally(False)
vocabulary_MISMATCH_IN_INDEX.setInternalNamespace(False)
vocabulary_MISMATCH_IN_INDEX.setChosenFromList(True)

vocabulary_term_MISMATCH_IN_INDEX_ONE = tr.createNewVocabularyTerm('ONE')
vocabulary_term_MISMATCH_IN_INDEX_ONE.setDescription(None)
vocabulary_term_MISMATCH_IN_INDEX_ONE.setLabel(None)
vocabulary_term_MISMATCH_IN_INDEX_ONE.setOrdinal(2)
vocabulary_MISMATCH_IN_INDEX.addTerm(vocabulary_term_MISMATCH_IN_INDEX_ONE)

vocabulary_term_MISMATCH_IN_INDEX_NONE = tr.createNewVocabularyTerm('NONE')
vocabulary_term_MISMATCH_IN_INDEX_NONE.setDescription(None)
vocabulary_term_MISMATCH_IN_INDEX_NONE.setLabel(None)
vocabulary_term_MISMATCH_IN_INDEX_NONE.setOrdinal(1)
vocabulary_MISMATCH_IN_INDEX.addTerm(vocabulary_term_MISMATCH_IN_INDEX_NONE)

vocabulary_NANO_DROP = tr.createNewVocabulary('NANO_DROP')
vocabulary_NANO_DROP.setDescription('Device for measuring the total amount of genetic material contained in the probe')
vocabulary_NANO_DROP.setUrlTemplate(None)
vocabulary_NANO_DROP.setManagedInternally(False)
vocabulary_NANO_DROP.setInternalNamespace(False)
vocabulary_NANO_DROP.setChosenFromList(True)

vocabulary_term_NANO_DROP_CONCND3300 = tr.createNewVocabularyTerm('CONCND3300')
vocabulary_term_NANO_DROP_CONCND3300.setDescription(None)
vocabulary_term_NANO_DROP_CONCND3300.setLabel('Conc Nano Drop 3300')
vocabulary_term_NANO_DROP_CONCND3300.setOrdinal(1)
vocabulary_NANO_DROP.addTerm(vocabulary_term_NANO_DROP_CONCND3300)

vocabulary_term_NANO_DROP_CONCND1000 = tr.createNewVocabularyTerm('CONCND1000')
vocabulary_term_NANO_DROP_CONCND1000.setDescription(None)
vocabulary_term_NANO_DROP_CONCND1000.setLabel('Conc Nano Drop 1000')
vocabulary_term_NANO_DROP_CONCND1000.setOrdinal(2)
vocabulary_NANO_DROP.addTerm(vocabulary_term_NANO_DROP_CONCND1000)

vocabulary_term_NANO_DROP_CONCND2000 = tr.createNewVocabularyTerm('CONCND2000')
vocabulary_term_NANO_DROP_CONCND2000.setDescription(None)
vocabulary_term_NANO_DROP_CONCND2000.setLabel('Conc Nano Drop 2000')
vocabulary_term_NANO_DROP_CONCND2000.setOrdinal(3)
vocabulary_NANO_DROP.addTerm(vocabulary_term_NANO_DROP_CONCND2000)

vocabulary_NCBI_TAXONOMY = tr.createNewVocabulary('NCBI_TAXONOMY')
vocabulary_NCBI_TAXONOMY.setDescription(None)
vocabulary_NCBI_TAXONOMY.setUrlTemplate('http://www.ncbi.nlm.nih.gov/Taxonomy/Browser/wwwtax.cgi?id=$term$')
vocabulary_NCBI_TAXONOMY.setManagedInternally(False)
vocabulary_NCBI_TAXONOMY.setInternalNamespace(False)
vocabulary_NCBI_TAXONOMY.setChosenFromList(True)

vocabulary_term_NCBI_TAXONOMY_9940 = tr.createNewVocabularyTerm('9940')
vocabulary_term_NCBI_TAXONOMY_9940.setDescription('Genbank common name: sheep\nInherited blast name: even-toed ungulates')
vocabulary_term_NCBI_TAXONOMY_9940.setLabel('Ovis aries')
vocabulary_term_NCBI_TAXONOMY_9940.setOrdinal(32)
vocabulary_NCBI_TAXONOMY.addTerm(vocabulary_term_NCBI_TAXONOMY_9940)

vocabulary_term_NCBI_TAXONOMY_35525 = tr.createNewVocabularyTerm('35525')
vocabulary_term_NCBI_TAXONOMY_35525.setDescription(None)
vocabulary_term_NCBI_TAXONOMY_35525.setLabel('Daphnia Magna')
vocabulary_term_NCBI_TAXONOMY_35525.setOrdinal(38)
vocabulary_NCBI_TAXONOMY.addTerm(vocabulary_term_NCBI_TAXONOMY_35525)

vocabulary_term_NCBI_TAXONOMY_4896 = tr.createNewVocabularyTerm('4896')
vocabulary_term_NCBI_TAXONOMY_4896.setDescription('Genbank common name: fission yeast\nInherited blast name: ascomycetes')
vocabulary_term_NCBI_TAXONOMY_4896.setLabel('Schizosaccharomyces pombe')
vocabulary_term_NCBI_TAXONOMY_4896.setOrdinal(25)
vocabulary_NCBI_TAXONOMY.addTerm(vocabulary_term_NCBI_TAXONOMY_4896)

vocabulary_term_NCBI_TAXONOMY_3702 = tr.createNewVocabularyTerm('3702')
vocabulary_term_NCBI_TAXONOMY_3702.setDescription('Genbank common name: thale cress\nInherited blast name: eudicots')
vocabulary_term_NCBI_TAXONOMY_3702.setLabel('Arabidopsis thaliana')
vocabulary_term_NCBI_TAXONOMY_3702.setOrdinal(28)
vocabulary_NCBI_TAXONOMY.addTerm(vocabulary_term_NCBI_TAXONOMY_3702)

vocabulary_term_NCBI_TAXONOMY_9823 = tr.createNewVocabularyTerm('9823')
vocabulary_term_NCBI_TAXONOMY_9823.setDescription('Genbank common name: pig\nInherited blast name: even-toed ungulates')
vocabulary_term_NCBI_TAXONOMY_9823.setLabel('Sus scrofa')
vocabulary_term_NCBI_TAXONOMY_9823.setOrdinal(31)
vocabulary_NCBI_TAXONOMY.addTerm(vocabulary_term_NCBI_TAXONOMY_9823)

vocabulary_term_NCBI_TAXONOMY_3573 = tr.createNewVocabularyTerm('3573')
vocabulary_term_NCBI_TAXONOMY_3573.setDescription('Genbank common name: campions\nInherited blast name: eudicots')
vocabulary_term_NCBI_TAXONOMY_3573.setLabel(' Silene')
vocabulary_term_NCBI_TAXONOMY_3573.setOrdinal(42)
vocabulary_NCBI_TAXONOMY.addTerm(vocabulary_term_NCBI_TAXONOMY_3573)

vocabulary_term_NCBI_TAXONOMY_99287 = tr.createNewVocabularyTerm('99287')
vocabulary_term_NCBI_TAXONOMY_99287.setDescription('Inherited blast name: enterobacteria')
vocabulary_term_NCBI_TAXONOMY_99287.setLabel('Salmonella enterica subsp. enterica serovar Typhimurium str. LT2')
vocabulary_term_NCBI_TAXONOMY_99287.setOrdinal(24)
vocabulary_NCBI_TAXONOMY.addTerm(vocabulary_term_NCBI_TAXONOMY_99287)

vocabulary_term_NCBI_TAXONOMY_562 = tr.createNewVocabularyTerm('562')
vocabulary_term_NCBI_TAXONOMY_562.setDescription('Inherited blast name: enterobacteria')
vocabulary_term_NCBI_TAXONOMY_562.setLabel('Escherichia coli')
vocabulary_term_NCBI_TAXONOMY_562.setOrdinal(8)
vocabulary_NCBI_TAXONOMY.addTerm(vocabulary_term_NCBI_TAXONOMY_562)

vocabulary_term_NCBI_TAXONOMY_282301 = tr.createNewVocabularyTerm('282301')
vocabulary_term_NCBI_TAXONOMY_282301.setDescription('Inherited blast name: flatworms')
vocabulary_term_NCBI_TAXONOMY_282301.setLabel('Macrostomum lignano')
vocabulary_term_NCBI_TAXONOMY_282301.setOrdinal(23)
vocabulary_NCBI_TAXONOMY.addTerm(vocabulary_term_NCBI_TAXONOMY_282301)

vocabulary_term_NCBI_TAXONOMY_8153 = tr.createNewVocabularyTerm('8153')
vocabulary_term_NCBI_TAXONOMY_8153.setDescription('cichlid fish')
vocabulary_term_NCBI_TAXONOMY_8153.setLabel('Haplochromis burtoni')
vocabulary_term_NCBI_TAXONOMY_8153.setOrdinal(34)
vocabulary_NCBI_TAXONOMY.addTerm(vocabulary_term_NCBI_TAXONOMY_8153)

vocabulary_term_NCBI_TAXONOMY_6669 = tr.createNewVocabularyTerm('6669')
vocabulary_term_NCBI_TAXONOMY_6669.setDescription('Genbank common name: common water flea\nInherited blast name: crustaceans')
vocabulary_term_NCBI_TAXONOMY_6669.setLabel('Daphnia pulex')
vocabulary_term_NCBI_TAXONOMY_6669.setOrdinal(7)
vocabulary_NCBI_TAXONOMY.addTerm(vocabulary_term_NCBI_TAXONOMY_6669)

vocabulary_term_NCBI_TAXONOMY_8113 = tr.createNewVocabularyTerm('8113')
vocabulary_term_NCBI_TAXONOMY_8113.setDescription('Genbank common name: cichlids\nInherited blast name: bony fishes')
vocabulary_term_NCBI_TAXONOMY_8113.setLabel('Cichlidae')
vocabulary_term_NCBI_TAXONOMY_8113.setOrdinal(20)
vocabulary_NCBI_TAXONOMY.addTerm(vocabulary_term_NCBI_TAXONOMY_8113)

vocabulary_term_NCBI_TAXONOMY_7227 = tr.createNewVocabularyTerm('7227')
vocabulary_term_NCBI_TAXONOMY_7227.setDescription('Genbank common name: fruit fly\nInherited blast name: flies')
vocabulary_term_NCBI_TAXONOMY_7227.setLabel('Drosophila melanogaster')
vocabulary_term_NCBI_TAXONOMY_7227.setOrdinal(3)
vocabulary_NCBI_TAXONOMY.addTerm(vocabulary_term_NCBI_TAXONOMY_7227)

vocabulary_term_NCBI_TAXONOMY_623 = tr.createNewVocabularyTerm('623')
vocabulary_term_NCBI_TAXONOMY_623.setDescription('Inherited blast name: enterobacteria')
vocabulary_term_NCBI_TAXONOMY_623.setLabel('Shigella flexneri')
vocabulary_term_NCBI_TAXONOMY_623.setOrdinal(9)
vocabulary_NCBI_TAXONOMY.addTerm(vocabulary_term_NCBI_TAXONOMY_623)

vocabulary_term_NCBI_TAXONOMY_3569 = tr.createNewVocabularyTerm('3569')
vocabulary_term_NCBI_TAXONOMY_3569.setDescription('Genbank common name: clove pink')
vocabulary_term_NCBI_TAXONOMY_3569.setLabel('Dianthus')
vocabulary_term_NCBI_TAXONOMY_3569.setOrdinal(41)
vocabulary_NCBI_TAXONOMY.addTerm(vocabulary_term_NCBI_TAXONOMY_3569)

vocabulary_term_NCBI_TAXONOMY_4932 = tr.createNewVocabularyTerm('4932')
vocabulary_term_NCBI_TAXONOMY_4932.setDescription("Genbank common name: baker's yeast\nInherited blast name: ascomycetes")
vocabulary_term_NCBI_TAXONOMY_4932.setLabel('Saccharomyces cerevisiae')
vocabulary_term_NCBI_TAXONOMY_4932.setOrdinal(5)
vocabulary_NCBI_TAXONOMY.addTerm(vocabulary_term_NCBI_TAXONOMY_4932)

vocabulary_term_NCBI_TAXONOMY_7955 = tr.createNewVocabularyTerm('7955')
vocabulary_term_NCBI_TAXONOMY_7955.setDescription('Genbank common name: zebrafish\nInherited blast name: bony fishes')
vocabulary_term_NCBI_TAXONOMY_7955.setLabel('Danio rerio')
vocabulary_term_NCBI_TAXONOMY_7955.setOrdinal(27)
vocabulary_NCBI_TAXONOMY.addTerm(vocabulary_term_NCBI_TAXONOMY_7955)

vocabulary_term_NCBI_TAXONOMY_13068 = tr.createNewVocabularyTerm('13068')
vocabulary_term_NCBI_TAXONOMY_13068.setDescription(None)
vocabulary_term_NCBI_TAXONOMY_13068.setLabel('Forficula auricularia (earwig)')
vocabulary_term_NCBI_TAXONOMY_13068.setOrdinal(40)
vocabulary_NCBI_TAXONOMY.addTerm(vocabulary_term_NCBI_TAXONOMY_13068)

vocabulary_term_NCBI_TAXONOMY_10116 = tr.createNewVocabularyTerm('10116')
vocabulary_term_NCBI_TAXONOMY_10116.setDescription('Genbank common name: Norway rat\nInherited blast name: rodents')
vocabulary_term_NCBI_TAXONOMY_10116.setLabel('Rattus norvegicus')
vocabulary_term_NCBI_TAXONOMY_10116.setOrdinal(6)
vocabulary_NCBI_TAXONOMY.addTerm(vocabulary_term_NCBI_TAXONOMY_10116)

vocabulary_term_NCBI_TAXONOMY_225322 = tr.createNewVocabularyTerm('225322')
vocabulary_term_NCBI_TAXONOMY_225322.setDescription('Bacterium which infects Daphnia\nPasteuria ramosa Metchnikoff 1888 (Approved Lists 1980) emend. Starr et al. 1986\nEbert D, Rainey P, Embley TM, Scholz D. Development, life\n cycle, ultrastructure and phylogenetic position of Pasteuria ramosa Metchnikoff 1888: rediscovery of an obligate\n endoparasite of Daphnia magna Straus. Philos Trans R Soc\n Lond Ser B. 1996;351:1689.')
vocabulary_term_NCBI_TAXONOMY_225322.setLabel('Pasteuria ramosa')
vocabulary_term_NCBI_TAXONOMY_225322.setOrdinal(29)
vocabulary_NCBI_TAXONOMY.addTerm(vocabulary_term_NCBI_TAXONOMY_225322)

vocabulary_term_NCBI_TAXONOMY_4897 = tr.createNewVocabularyTerm('4897')
vocabulary_term_NCBI_TAXONOMY_4897.setDescription('Inherited blast name: ascomycetes')
vocabulary_term_NCBI_TAXONOMY_4897.setLabel('Schizosaccharomyces japonicus')
vocabulary_term_NCBI_TAXONOMY_4897.setOrdinal(26)
vocabulary_NCBI_TAXONOMY.addTerm(vocabulary_term_NCBI_TAXONOMY_4897)

vocabulary_term_NCBI_TAXONOMY_32644 = tr.createNewVocabularyTerm('32644')
vocabulary_term_NCBI_TAXONOMY_32644.setDescription('Inherited blast name: unclassified')
vocabulary_term_NCBI_TAXONOMY_32644.setLabel('unidentified')
vocabulary_term_NCBI_TAXONOMY_32644.setOrdinal(50)
vocabulary_NCBI_TAXONOMY.addTerm(vocabulary_term_NCBI_TAXONOMY_32644)

vocabulary_term_NCBI_TAXONOMY_9606 = tr.createNewVocabularyTerm('9606')
vocabulary_term_NCBI_TAXONOMY_9606.setDescription('Genbank common name: human\nInherited blast name: primates')
vocabulary_term_NCBI_TAXONOMY_9606.setLabel('Homo sapiens')
vocabulary_term_NCBI_TAXONOMY_9606.setOrdinal(2)
vocabulary_NCBI_TAXONOMY.addTerm(vocabulary_term_NCBI_TAXONOMY_9606)

vocabulary_term_NCBI_TAXONOMY_9913 = tr.createNewVocabularyTerm('9913')
vocabulary_term_NCBI_TAXONOMY_9913.setDescription('Genbank common name: cattle\nInherited blast name: even-toed ungulates')
vocabulary_term_NCBI_TAXONOMY_9913.setLabel('Bos taurus')
vocabulary_term_NCBI_TAXONOMY_9913.setOrdinal(30)
vocabulary_NCBI_TAXONOMY.addTerm(vocabulary_term_NCBI_TAXONOMY_9913)

vocabulary_term_NCBI_TAXONOMY_10847 = tr.createNewVocabularyTerm('10847')
vocabulary_term_NCBI_TAXONOMY_10847.setDescription('Inherited blast name: viruses')
vocabulary_term_NCBI_TAXONOMY_10847.setLabel('Enterobacteria phage phiX174')
vocabulary_term_NCBI_TAXONOMY_10847.setOrdinal(18)
vocabulary_NCBI_TAXONOMY.addTerm(vocabulary_term_NCBI_TAXONOMY_10847)

vocabulary_term_NCBI_TAXONOMY_6239 = tr.createNewVocabularyTerm('6239')
vocabulary_term_NCBI_TAXONOMY_6239.setDescription('Inherited blast name: nematodes')
vocabulary_term_NCBI_TAXONOMY_6239.setLabel('Caenorhabditis elegans')
vocabulary_term_NCBI_TAXONOMY_6239.setOrdinal(4)
vocabulary_NCBI_TAXONOMY.addTerm(vocabulary_term_NCBI_TAXONOMY_6239)

vocabulary_term_NCBI_TAXONOMY_10090 = tr.createNewVocabularyTerm('10090')
vocabulary_term_NCBI_TAXONOMY_10090.setDescription('Genbank common name: house mouse\nInherited blast name: rodents')
vocabulary_term_NCBI_TAXONOMY_10090.setLabel('Mus musculus')
vocabulary_term_NCBI_TAXONOMY_10090.setOrdinal(1)
vocabulary_NCBI_TAXONOMY.addTerm(vocabulary_term_NCBI_TAXONOMY_10090)

vocabulary_term_NCBI_TAXONOMY_190650 = tr.createNewVocabularyTerm('190650')
vocabulary_term_NCBI_TAXONOMY_190650.setDescription('Inherited blast name: a-proteobacteria')
vocabulary_term_NCBI_TAXONOMY_190650.setLabel('Caulobacter crescentus CB15')
vocabulary_term_NCBI_TAXONOMY_190650.setOrdinal(19)
vocabulary_NCBI_TAXONOMY.addTerm(vocabulary_term_NCBI_TAXONOMY_190650)

vocabulary_term_NCBI_TAXONOMY_481459 = tr.createNewVocabularyTerm('481459')
vocabulary_term_NCBI_TAXONOMY_481459.setDescription('Genbank common name: three-spined stickleback\nInherited blast name: bony fishes')
vocabulary_term_NCBI_TAXONOMY_481459.setLabel('Gasterosteus aculeatus aculeatus')
vocabulary_term_NCBI_TAXONOMY_481459.setOrdinal(21)
vocabulary_NCBI_TAXONOMY.addTerm(vocabulary_term_NCBI_TAXONOMY_481459)

vocabulary_term_NCBI_TAXONOMY_9925 = tr.createNewVocabularyTerm('9925')
vocabulary_term_NCBI_TAXONOMY_9925.setDescription('Genbank common name: goat\nInherited blast name: even-toed ungulates')
vocabulary_term_NCBI_TAXONOMY_9925.setLabel('Capra hircus')
vocabulary_term_NCBI_TAXONOMY_9925.setOrdinal(33)
vocabulary_NCBI_TAXONOMY.addTerm(vocabulary_term_NCBI_TAXONOMY_9925)

vocabulary_term_NCBI_TAXONOMY_4081 = tr.createNewVocabularyTerm('4081')
vocabulary_term_NCBI_TAXONOMY_4081.setDescription('Common Name: tomato')
vocabulary_term_NCBI_TAXONOMY_4081.setLabel('Solanum lycopersicum (tomato)')
vocabulary_term_NCBI_TAXONOMY_4081.setOrdinal(39)
vocabulary_NCBI_TAXONOMY.addTerm(vocabulary_term_NCBI_TAXONOMY_4081)

vocabulary_term_NCBI_TAXONOMY_61818 = tr.createNewVocabularyTerm('61818')
vocabulary_term_NCBI_TAXONOMY_61818.setDescription('bony fish')
vocabulary_term_NCBI_TAXONOMY_61818.setLabel('Amphilophus (nicaraguan)')
vocabulary_term_NCBI_TAXONOMY_61818.setOrdinal(35)
vocabulary_NCBI_TAXONOMY.addTerm(vocabulary_term_NCBI_TAXONOMY_61818)

vocabulary_PIPELINE_VERSION = tr.createNewVocabulary('PIPELINE_VERSION')
vocabulary_PIPELINE_VERSION.setDescription('With which pipeline version has the data been analyzed?')
vocabulary_PIPELINE_VERSION.setUrlTemplate(None)
vocabulary_PIPELINE_VERSION.setManagedInternally(False)
vocabulary_PIPELINE_VERSION.setInternalNamespace(False)
vocabulary_PIPELINE_VERSION.setChosenFromList(True)

vocabulary_term_PIPELINE_VERSION_11242 = tr.createNewVocabularyTerm('1.12.4.2')
vocabulary_term_PIPELINE_VERSION_11242.setDescription('Real Time Analysis in combination with HCS 1.4.8')
vocabulary_term_PIPELINE_VERSION_11242.setLabel('RTA 1.12.4.2 (HiSeq 2000)')
vocabulary_term_PIPELINE_VERSION_11242.setOrdinal(2)
vocabulary_PIPELINE_VERSION.addTerm(vocabulary_term_PIPELINE_VERSION_11242)

vocabulary_term_PIPELINE_VERSION_10 = tr.createNewVocabularyTerm('1.0')
vocabulary_term_PIPELINE_VERSION_10.setDescription(None)
vocabulary_term_PIPELINE_VERSION_10.setLabel(None)
vocabulary_term_PIPELINE_VERSION_10.setOrdinal(26)
vocabulary_PIPELINE_VERSION.addTerm(vocabulary_term_PIPELINE_VERSION_10)

vocabulary_term_PIPELINE_VERSION_RTA_16 = tr.createNewVocabularyTerm('RTA_1.6')
vocabulary_term_PIPELINE_VERSION_RTA_16.setDescription('Real Time Analysis 1.6')
vocabulary_term_PIPELINE_VERSION_RTA_16.setLabel('RTA 1.6')
vocabulary_term_PIPELINE_VERSION_RTA_16.setOrdinal(13)
vocabulary_PIPELINE_VERSION.addTerm(vocabulary_term_PIPELINE_VERSION_RTA_16)

vocabulary_term_PIPELINE_VERSION_132 = tr.createNewVocabularyTerm('1.3.2')
vocabulary_term_PIPELINE_VERSION_132.setDescription(None)
vocabulary_term_PIPELINE_VERSION_132.setLabel(None)
vocabulary_term_PIPELINE_VERSION_132.setOrdinal(23)
vocabulary_PIPELINE_VERSION.addTerm(vocabulary_term_PIPELINE_VERSION_132)

vocabulary_term_PIPELINE_VERSION_14 = tr.createNewVocabularyTerm('1.4')
vocabulary_term_PIPELINE_VERSION_14.setDescription(None)
vocabulary_term_PIPELINE_VERSION_14.setLabel(None)
vocabulary_term_PIPELINE_VERSION_14.setOrdinal(22)
vocabulary_PIPELINE_VERSION.addTerm(vocabulary_term_PIPELINE_VERSION_14)

vocabulary_term_PIPELINE_VERSION_RTA_15 = tr.createNewVocabularyTerm('RTA_1.5')
vocabulary_term_PIPELINE_VERSION_RTA_15.setDescription(None)
vocabulary_term_PIPELINE_VERSION_RTA_15.setLabel('RTA 1.5')
vocabulary_term_PIPELINE_VERSION_RTA_15.setOrdinal(14)
vocabulary_PIPELINE_VERSION.addTerm(vocabulary_term_PIPELINE_VERSION_RTA_15)

vocabulary_term_PIPELINE_VERSION_19 = tr.createNewVocabularyTerm('1.9')
vocabulary_term_PIPELINE_VERSION_19.setDescription('Real Time Analysis 1.9')
vocabulary_term_PIPELINE_VERSION_19.setLabel('RTA 1.9 (GA IIx)')
vocabulary_term_PIPELINE_VERSION_19.setOrdinal(5)
vocabulary_PIPELINE_VERSION.addTerm(vocabulary_term_PIPELINE_VERSION_19)

vocabulary_term_PIPELINE_VERSION_1124 = tr.createNewVocabularyTerm('1.12.4')
vocabulary_term_PIPELINE_VERSION_1124.setDescription(None)
vocabulary_term_PIPELINE_VERSION_1124.setLabel('RTA 1.12.4 (HiSeq 2000)')
vocabulary_term_PIPELINE_VERSION_1124.setOrdinal(3)
vocabulary_PIPELINE_VERSION.addTerm(vocabulary_term_PIPELINE_VERSION_1124)

vocabulary_term_PIPELINE_VERSION_RTA = tr.createNewVocabularyTerm('RTA')
vocabulary_term_PIPELINE_VERSION_RTA.setDescription(None)
vocabulary_term_PIPELINE_VERSION_RTA.setLabel(None)
vocabulary_term_PIPELINE_VERSION_RTA.setOrdinal(30)
vocabulary_PIPELINE_VERSION.addTerm(vocabulary_term_PIPELINE_VERSION_RTA)

vocabulary_term_PIPELINE_VERSION_RTA_14150 = tr.createNewVocabularyTerm('RTA_1.4.15.0')
vocabulary_term_PIPELINE_VERSION_RTA_14150.setDescription(None)
vocabulary_term_PIPELINE_VERSION_RTA_14150.setLabel('RTA 1.4.15.0')
vocabulary_term_PIPELINE_VERSION_RTA_14150.setOrdinal(17)
vocabulary_PIPELINE_VERSION.addTerm(vocabulary_term_PIPELINE_VERSION_RTA_14150)

vocabulary_term_PIPELINE_VERSION_112 = tr.createNewVocabularyTerm('1.12')
vocabulary_term_PIPELINE_VERSION_112.setDescription('Real Time Analysis 1.12 HiSeq 2000')
vocabulary_term_PIPELINE_VERSION_112.setLabel('RTA 1.12')
vocabulary_term_PIPELINE_VERSION_112.setOrdinal(4)
vocabulary_PIPELINE_VERSION.addTerm(vocabulary_term_PIPELINE_VERSION_112)

vocabulary_term_PIPELINE_VERSION_15 = tr.createNewVocabularyTerm('1.5')
vocabulary_term_PIPELINE_VERSION_15.setDescription(None)
vocabulary_term_PIPELINE_VERSION_15.setLabel(None)
vocabulary_term_PIPELINE_VERSION_15.setOrdinal(18)
vocabulary_PIPELINE_VERSION.addTerm(vocabulary_term_PIPELINE_VERSION_15)

vocabulary_term_PIPELINE_VERSION_11348 = tr.createNewVocabularyTerm('1.13.48')
vocabulary_term_PIPELINE_VERSION_11348.setDescription(None)
vocabulary_term_PIPELINE_VERSION_11348.setLabel('RTA 1.13.48')
vocabulary_term_PIPELINE_VERSION_11348.setOrdinal(1)
vocabulary_PIPELINE_VERSION.addTerm(vocabulary_term_PIPELINE_VERSION_11348)

vocabulary_term_PIPELINE_VERSION_PRE_10 = tr.createNewVocabularyTerm('PRE_1.0')
vocabulary_term_PIPELINE_VERSION_PRE_10.setDescription('Before Pipeline Version 1.0')
vocabulary_term_PIPELINE_VERSION_PRE_10.setLabel(None)
vocabulary_term_PIPELINE_VERSION_PRE_10.setOrdinal(27)
vocabulary_PIPELINE_VERSION.addTerm(vocabulary_term_PIPELINE_VERSION_PRE_10)

vocabulary_term_PIPELINE_VERSION_RTA_18 = tr.createNewVocabularyTerm('RTA_1.8')
vocabulary_term_PIPELINE_VERSION_RTA_18.setDescription('Real Time Analysis 1.8')
vocabulary_term_PIPELINE_VERSION_RTA_18.setLabel('RTA 1.8')
vocabulary_term_PIPELINE_VERSION_RTA_18.setOrdinal(6)
vocabulary_PIPELINE_VERSION.addTerm(vocabulary_term_PIPELINE_VERSION_RTA_18)

vocabulary_term_PIPELINE_VERSION_NONE = tr.createNewVocabularyTerm('NONE')
vocabulary_term_PIPELINE_VERSION_NONE.setDescription(None)
vocabulary_term_PIPELINE_VERSION_NONE.setLabel(None)
vocabulary_term_PIPELINE_VERSION_NONE.setOrdinal(29)
vocabulary_PIPELINE_VERSION.addTerm(vocabulary_term_PIPELINE_VERSION_NONE)

vocabulary_REQUIRED_LANES = tr.createNewVocabulary('REQUIRED_LANES')
vocabulary_REQUIRED_LANES.setDescription('Amount of lanes needed for this probe')
vocabulary_REQUIRED_LANES.setUrlTemplate(None)
vocabulary_REQUIRED_LANES.setManagedInternally(False)
vocabulary_REQUIRED_LANES.setInternalNamespace(False)
vocabulary_REQUIRED_LANES.setChosenFromList(True)

vocabulary_term_REQUIRED_LANES_7 = tr.createNewVocabularyTerm('7')
vocabulary_term_REQUIRED_LANES_7.setDescription(None)
vocabulary_term_REQUIRED_LANES_7.setLabel(None)
vocabulary_term_REQUIRED_LANES_7.setOrdinal(8)
vocabulary_REQUIRED_LANES.addTerm(vocabulary_term_REQUIRED_LANES_7)

vocabulary_term_REQUIRED_LANES_13 = tr.createNewVocabularyTerm('13')
vocabulary_term_REQUIRED_LANES_13.setDescription(None)
vocabulary_term_REQUIRED_LANES_13.setLabel(None)
vocabulary_term_REQUIRED_LANES_13.setOrdinal(15)
vocabulary_REQUIRED_LANES.addTerm(vocabulary_term_REQUIRED_LANES_13)

vocabulary_term_REQUIRED_LANES_8 = tr.createNewVocabularyTerm('8')
vocabulary_term_REQUIRED_LANES_8.setDescription(None)
vocabulary_term_REQUIRED_LANES_8.setLabel(None)
vocabulary_term_REQUIRED_LANES_8.setOrdinal(10)
vocabulary_REQUIRED_LANES.addTerm(vocabulary_term_REQUIRED_LANES_8)

vocabulary_term_REQUIRED_LANES_14 = tr.createNewVocabularyTerm('14')
vocabulary_term_REQUIRED_LANES_14.setDescription(None)
vocabulary_term_REQUIRED_LANES_14.setLabel(None)
vocabulary_term_REQUIRED_LANES_14.setOrdinal(16)
vocabulary_REQUIRED_LANES.addTerm(vocabulary_term_REQUIRED_LANES_14)

vocabulary_term_REQUIRED_LANES_3 = tr.createNewVocabularyTerm('3')
vocabulary_term_REQUIRED_LANES_3.setDescription(None)
vocabulary_term_REQUIRED_LANES_3.setLabel(None)
vocabulary_term_REQUIRED_LANES_3.setOrdinal(4)
vocabulary_REQUIRED_LANES.addTerm(vocabulary_term_REQUIRED_LANES_3)

vocabulary_term_REQUIRED_LANES_4 = tr.createNewVocabularyTerm('4')
vocabulary_term_REQUIRED_LANES_4.setDescription(None)
vocabulary_term_REQUIRED_LANES_4.setLabel(None)
vocabulary_term_REQUIRED_LANES_4.setOrdinal(5)
vocabulary_REQUIRED_LANES.addTerm(vocabulary_term_REQUIRED_LANES_4)

vocabulary_term_REQUIRED_LANES_16 = tr.createNewVocabularyTerm('16')
vocabulary_term_REQUIRED_LANES_16.setDescription(None)
vocabulary_term_REQUIRED_LANES_16.setLabel(None)
vocabulary_term_REQUIRED_LANES_16.setOrdinal(18)
vocabulary_REQUIRED_LANES.addTerm(vocabulary_term_REQUIRED_LANES_16)

vocabulary_term_REQUIRED_LANES_5 = tr.createNewVocabularyTerm('5')
vocabulary_term_REQUIRED_LANES_5.setDescription(None)
vocabulary_term_REQUIRED_LANES_5.setLabel(None)
vocabulary_term_REQUIRED_LANES_5.setOrdinal(6)
vocabulary_REQUIRED_LANES.addTerm(vocabulary_term_REQUIRED_LANES_5)

vocabulary_term_REQUIRED_LANES_2 = tr.createNewVocabularyTerm('2')
vocabulary_term_REQUIRED_LANES_2.setDescription(None)
vocabulary_term_REQUIRED_LANES_2.setLabel(None)
vocabulary_term_REQUIRED_LANES_2.setOrdinal(3)
vocabulary_REQUIRED_LANES.addTerm(vocabulary_term_REQUIRED_LANES_2)

vocabulary_term_REQUIRED_LANES_15 = tr.createNewVocabularyTerm('15')
vocabulary_term_REQUIRED_LANES_15.setDescription(None)
vocabulary_term_REQUIRED_LANES_15.setLabel(None)
vocabulary_term_REQUIRED_LANES_15.setOrdinal(17)
vocabulary_REQUIRED_LANES.addTerm(vocabulary_term_REQUIRED_LANES_15)

vocabulary_term_REQUIRED_LANES_6 = tr.createNewVocabularyTerm('6')
vocabulary_term_REQUIRED_LANES_6.setDescription(None)
vocabulary_term_REQUIRED_LANES_6.setLabel(None)
vocabulary_term_REQUIRED_LANES_6.setOrdinal(7)
vocabulary_REQUIRED_LANES.addTerm(vocabulary_term_REQUIRED_LANES_6)

vocabulary_term_REQUIRED_LANES_1 = tr.createNewVocabularyTerm('1')
vocabulary_term_REQUIRED_LANES_1.setDescription(None)
vocabulary_term_REQUIRED_LANES_1.setLabel(None)
vocabulary_term_REQUIRED_LANES_1.setOrdinal(2)
vocabulary_REQUIRED_LANES.addTerm(vocabulary_term_REQUIRED_LANES_1)

vocabulary_term_REQUIRED_LANES_11 = tr.createNewVocabularyTerm('11')
vocabulary_term_REQUIRED_LANES_11.setDescription(None)
vocabulary_term_REQUIRED_LANES_11.setLabel(None)
vocabulary_term_REQUIRED_LANES_11.setOrdinal(13)
vocabulary_REQUIRED_LANES.addTerm(vocabulary_term_REQUIRED_LANES_11)

vocabulary_term_REQUIRED_LANES_10 = tr.createNewVocabularyTerm('10')
vocabulary_term_REQUIRED_LANES_10.setDescription(None)
vocabulary_term_REQUIRED_LANES_10.setLabel(None)
vocabulary_term_REQUIRED_LANES_10.setOrdinal(12)
vocabulary_REQUIRED_LANES.addTerm(vocabulary_term_REQUIRED_LANES_10)

vocabulary_term_REQUIRED_LANES_12 = tr.createNewVocabularyTerm('12')
vocabulary_term_REQUIRED_LANES_12.setDescription(None)
vocabulary_term_REQUIRED_LANES_12.setLabel(None)
vocabulary_term_REQUIRED_LANES_12.setOrdinal(14)
vocabulary_REQUIRED_LANES.addTerm(vocabulary_term_REQUIRED_LANES_12)

vocabulary_term_REQUIRED_LANES_9 = tr.createNewVocabularyTerm('9')
vocabulary_term_REQUIRED_LANES_9.setDescription(None)
vocabulary_term_REQUIRED_LANES_9.setLabel(None)
vocabulary_term_REQUIRED_LANES_9.setOrdinal(11)
vocabulary_REQUIRED_LANES.addTerm(vocabulary_term_REQUIRED_LANES_9)

vocabulary_SAMPLE_LIBRARY_SELECTION = tr.createNewVocabulary('SAMPLE_LIBRARY_SELECTION')
vocabulary_SAMPLE_LIBRARY_SELECTION.setDescription('Describes whether any method was used to select and/or enrich the material being sequenced.')
vocabulary_SAMPLE_LIBRARY_SELECTION.setUrlTemplate(None)
vocabulary_SAMPLE_LIBRARY_SELECTION.setManagedInternally(False)
vocabulary_SAMPLE_LIBRARY_SELECTION.setInternalNamespace(False)
vocabulary_SAMPLE_LIBRARY_SELECTION.setChosenFromList(True)

vocabulary_term_SAMPLE_LIBRARY_SELECTION_RANDOM_PCR = tr.createNewVocabularyTerm('RANDOM_PCR')
vocabulary_term_SAMPLE_LIBRARY_SELECTION_RANDOM_PCR.setDescription('Source material was selected by randomly generated primers')
vocabulary_term_SAMPLE_LIBRARY_SELECTION_RANDOM_PCR.setLabel('RANDOM PCR')
vocabulary_term_SAMPLE_LIBRARY_SELECTION_RANDOM_PCR.setOrdinal(11)
vocabulary_SAMPLE_LIBRARY_SELECTION.addTerm(vocabulary_term_SAMPLE_LIBRARY_SELECTION_RANDOM_PCR)

vocabulary_term_SAMPLE_LIBRARY_SELECTION_RESTRICTION_DIGEST = tr.createNewVocabularyTerm('RESTRICTION_DIGEST')
vocabulary_term_SAMPLE_LIBRARY_SELECTION_RESTRICTION_DIGEST.setDescription('Restriction enzyme digestion')
vocabulary_term_SAMPLE_LIBRARY_SELECTION_RESTRICTION_DIGEST.setLabel('Restriction Digest')
vocabulary_term_SAMPLE_LIBRARY_SELECTION_RESTRICTION_DIGEST.setOrdinal(3)
vocabulary_SAMPLE_LIBRARY_SELECTION.addTerm(vocabulary_term_SAMPLE_LIBRARY_SELECTION_RESTRICTION_DIGEST)

vocabulary_term_SAMPLE_LIBRARY_SELECTION_CHIP = tr.createNewVocabularyTerm('CHIP')
vocabulary_term_SAMPLE_LIBRARY_SELECTION_CHIP.setDescription('Chromatin immunoprecipitation')
vocabulary_term_SAMPLE_LIBRARY_SELECTION_CHIP.setLabel('ChIP')
vocabulary_term_SAMPLE_LIBRARY_SELECTION_CHIP.setOrdinal(1)
vocabulary_SAMPLE_LIBRARY_SELECTION.addTerm(vocabulary_term_SAMPLE_LIBRARY_SELECTION_CHIP)

vocabulary_term_SAMPLE_LIBRARY_SELECTION_CFS = tr.createNewVocabularyTerm('CF-S')
vocabulary_term_SAMPLE_LIBRARY_SELECTION_CFS.setDescription('Cot-filtered single/low-copy genomic DNA')
vocabulary_term_SAMPLE_LIBRARY_SELECTION_CFS.setLabel('CF-S')
vocabulary_term_SAMPLE_LIBRARY_SELECTION_CFS.setOrdinal(13)
vocabulary_SAMPLE_LIBRARY_SELECTION.addTerm(vocabulary_term_SAMPLE_LIBRARY_SELECTION_CFS)

vocabulary_term_SAMPLE_LIBRARY_SELECTION_UNSPECIFIED = tr.createNewVocabularyTerm('UNSPECIFIED')
vocabulary_term_SAMPLE_LIBRARY_SELECTION_UNSPECIFIED.setDescription(None)
vocabulary_term_SAMPLE_LIBRARY_SELECTION_UNSPECIFIED.setLabel('unspecified')
vocabulary_term_SAMPLE_LIBRARY_SELECTION_UNSPECIFIED.setOrdinal(18)
vocabulary_SAMPLE_LIBRARY_SELECTION.addTerm(vocabulary_term_SAMPLE_LIBRARY_SELECTION_UNSPECIFIED)

vocabulary_term_SAMPLE_LIBRARY_SELECTION_CFH = tr.createNewVocabularyTerm('CF-H')
vocabulary_term_SAMPLE_LIBRARY_SELECTION_CFH.setDescription('Cot-filtered highly repetitive genomic DNA')
vocabulary_term_SAMPLE_LIBRARY_SELECTION_CFH.setLabel('CF-H')
vocabulary_term_SAMPLE_LIBRARY_SELECTION_CFH.setOrdinal(15)
vocabulary_SAMPLE_LIBRARY_SELECTION.addTerm(vocabulary_term_SAMPLE_LIBRARY_SELECTION_CFH)

vocabulary_term_SAMPLE_LIBRARY_SELECTION_RANDOM = tr.createNewVocabularyTerm('RANDOM')
vocabulary_term_SAMPLE_LIBRARY_SELECTION_RANDOM.setDescription('Random shearing only')
vocabulary_term_SAMPLE_LIBRARY_SELECTION_RANDOM.setLabel('RANDOM')
vocabulary_term_SAMPLE_LIBRARY_SELECTION_RANDOM.setOrdinal(5)
vocabulary_SAMPLE_LIBRARY_SELECTION.addTerm(vocabulary_term_SAMPLE_LIBRARY_SELECTION_RANDOM)

vocabulary_term_SAMPLE_LIBRARY_SELECTION_HMPR = tr.createNewVocabularyTerm('HMPR')
vocabulary_term_SAMPLE_LIBRARY_SELECTION_HMPR.setDescription('Hypo-methylated partial restriction digest')
vocabulary_term_SAMPLE_LIBRARY_SELECTION_HMPR.setLabel('HMPR')
vocabulary_term_SAMPLE_LIBRARY_SELECTION_HMPR.setOrdinal(8)
vocabulary_SAMPLE_LIBRARY_SELECTION.addTerm(vocabulary_term_SAMPLE_LIBRARY_SELECTION_HMPR)

vocabulary_term_SAMPLE_LIBRARY_SELECTION_MNASE = tr.createNewVocabularyTerm('MNASE')
vocabulary_term_SAMPLE_LIBRARY_SELECTION_MNASE.setDescription('Micrococcal Nuclease (MNase) digestion')
vocabulary_term_SAMPLE_LIBRARY_SELECTION_MNASE.setLabel('MNase')
vocabulary_term_SAMPLE_LIBRARY_SELECTION_MNASE.setOrdinal(2)
vocabulary_SAMPLE_LIBRARY_SELECTION.addTerm(vocabulary_term_SAMPLE_LIBRARY_SELECTION_MNASE)

vocabulary_term_SAMPLE_LIBRARY_SELECTION_5METHYLCYTIDINE_ANTIBODY = tr.createNewVocabularyTerm('5-METHYLCYTIDINE_ANTIBODY')
vocabulary_term_SAMPLE_LIBRARY_SELECTION_5METHYLCYTIDINE_ANTIBODY.setDescription('Methylated DNA immunoprecipitation')
vocabulary_term_SAMPLE_LIBRARY_SELECTION_5METHYLCYTIDINE_ANTIBODY.setLabel('5-methylcytidine antibody')
vocabulary_term_SAMPLE_LIBRARY_SELECTION_5METHYLCYTIDINE_ANTIBODY.setOrdinal(4)
vocabulary_SAMPLE_LIBRARY_SELECTION.addTerm(vocabulary_term_SAMPLE_LIBRARY_SELECTION_5METHYLCYTIDINE_ANTIBODY)

vocabulary_term_SAMPLE_LIBRARY_SELECTION_RTPCR = tr.createNewVocabularyTerm('RT-PCR')
vocabulary_term_SAMPLE_LIBRARY_SELECTION_RTPCR.setDescription('Source material was selected by reverse transcription PCR')
vocabulary_term_SAMPLE_LIBRARY_SELECTION_RTPCR.setLabel('RT-PCR')
vocabulary_term_SAMPLE_LIBRARY_SELECTION_RTPCR.setOrdinal(12)
vocabulary_SAMPLE_LIBRARY_SELECTION.addTerm(vocabulary_term_SAMPLE_LIBRARY_SELECTION_RTPCR)

vocabulary_term_SAMPLE_LIBRARY_SELECTION_CFM = tr.createNewVocabularyTerm('CF-M')
vocabulary_term_SAMPLE_LIBRARY_SELECTION_CFM.setDescription('Cot-filtered moderately repetitive genomic DNA')
vocabulary_term_SAMPLE_LIBRARY_SELECTION_CFM.setLabel('CF-M')
vocabulary_term_SAMPLE_LIBRARY_SELECTION_CFM.setOrdinal(14)
vocabulary_SAMPLE_LIBRARY_SELECTION.addTerm(vocabulary_term_SAMPLE_LIBRARY_SELECTION_CFM)

vocabulary_term_SAMPLE_LIBRARY_SELECTION_CDNA = tr.createNewVocabularyTerm('CDNA')
vocabulary_term_SAMPLE_LIBRARY_SELECTION_CDNA.setDescription('complementary DNA')
vocabulary_term_SAMPLE_LIBRARY_SELECTION_CDNA.setLabel('cDNA')
vocabulary_term_SAMPLE_LIBRARY_SELECTION_CDNA.setOrdinal(9)
vocabulary_SAMPLE_LIBRARY_SELECTION.addTerm(vocabulary_term_SAMPLE_LIBRARY_SELECTION_CDNA)

vocabulary_term_SAMPLE_LIBRARY_SELECTION_MF = tr.createNewVocabularyTerm('MF')
vocabulary_term_SAMPLE_LIBRARY_SELECTION_MF.setDescription('Methyl Filtrated')
vocabulary_term_SAMPLE_LIBRARY_SELECTION_MF.setLabel('MF')
vocabulary_term_SAMPLE_LIBRARY_SELECTION_MF.setOrdinal(6)
vocabulary_SAMPLE_LIBRARY_SELECTION.addTerm(vocabulary_term_SAMPLE_LIBRARY_SELECTION_MF)

vocabulary_term_SAMPLE_LIBRARY_SELECTION_OTHER = tr.createNewVocabularyTerm('OTHER')
vocabulary_term_SAMPLE_LIBRARY_SELECTION_OTHER.setDescription(None)
vocabulary_term_SAMPLE_LIBRARY_SELECTION_OTHER.setLabel('other')
vocabulary_term_SAMPLE_LIBRARY_SELECTION_OTHER.setOrdinal(17)
vocabulary_SAMPLE_LIBRARY_SELECTION.addTerm(vocabulary_term_SAMPLE_LIBRARY_SELECTION_OTHER)

vocabulary_term_SAMPLE_LIBRARY_SELECTION_PCR = tr.createNewVocabularyTerm('PCR')
vocabulary_term_SAMPLE_LIBRARY_SELECTION_PCR.setDescription('Source material was selected by designed primers')
vocabulary_term_SAMPLE_LIBRARY_SELECTION_PCR.setLabel('PCR')
vocabulary_term_SAMPLE_LIBRARY_SELECTION_PCR.setOrdinal(10)
vocabulary_SAMPLE_LIBRARY_SELECTION.addTerm(vocabulary_term_SAMPLE_LIBRARY_SELECTION_PCR)

vocabulary_term_SAMPLE_LIBRARY_SELECTION_MSLL = tr.createNewVocabularyTerm('MSLL')
vocabulary_term_SAMPLE_LIBRARY_SELECTION_MSLL.setDescription('Methylation Spanning Linking Library')
vocabulary_term_SAMPLE_LIBRARY_SELECTION_MSLL.setLabel('MSLL')
vocabulary_term_SAMPLE_LIBRARY_SELECTION_MSLL.setOrdinal(7)
vocabulary_SAMPLE_LIBRARY_SELECTION.addTerm(vocabulary_term_SAMPLE_LIBRARY_SELECTION_MSLL)

vocabulary_term_SAMPLE_LIBRARY_SELECTION_CFT = tr.createNewVocabularyTerm('CF-T')
vocabulary_term_SAMPLE_LIBRARY_SELECTION_CFT.setDescription('Cot-filtered theoretical single-copy genomic DNA')
vocabulary_term_SAMPLE_LIBRARY_SELECTION_CFT.setLabel('CF-T')
vocabulary_term_SAMPLE_LIBRARY_SELECTION_CFT.setOrdinal(16)
vocabulary_SAMPLE_LIBRARY_SELECTION.addTerm(vocabulary_term_SAMPLE_LIBRARY_SELECTION_CFT)

vocabulary_SAMPLE_LIBRARY_SOURCE_VOC = tr.createNewVocabulary('SAMPLE_LIBRARY_SOURCE_VOC')
vocabulary_SAMPLE_LIBRARY_SOURCE_VOC.setDescription(None)
vocabulary_SAMPLE_LIBRARY_SOURCE_VOC.setUrlTemplate(None)
vocabulary_SAMPLE_LIBRARY_SOURCE_VOC.setManagedInternally(False)
vocabulary_SAMPLE_LIBRARY_SOURCE_VOC.setInternalNamespace(False)
vocabulary_SAMPLE_LIBRARY_SOURCE_VOC.setChosenFromList(True)

vocabulary_term_SAMPLE_LIBRARY_SOURCE_VOC_OTHER = tr.createNewVocabularyTerm('OTHER')
vocabulary_term_SAMPLE_LIBRARY_SOURCE_VOC_OTHER.setDescription(None)
vocabulary_term_SAMPLE_LIBRARY_SOURCE_VOC_OTHER.setLabel(None)
vocabulary_term_SAMPLE_LIBRARY_SOURCE_VOC_OTHER.setOrdinal(6)
vocabulary_SAMPLE_LIBRARY_SOURCE_VOC.addTerm(vocabulary_term_SAMPLE_LIBRARY_SOURCE_VOC_OTHER)

vocabulary_term_SAMPLE_LIBRARY_SOURCE_VOC_NONGENOMIC = tr.createNewVocabularyTerm('NON-GENOMIC')
vocabulary_term_SAMPLE_LIBRARY_SOURCE_VOC_NONGENOMIC.setDescription(None)
vocabulary_term_SAMPLE_LIBRARY_SOURCE_VOC_NONGENOMIC.setLabel(None)
vocabulary_term_SAMPLE_LIBRARY_SOURCE_VOC_NONGENOMIC.setOrdinal(2)
vocabulary_SAMPLE_LIBRARY_SOURCE_VOC.addTerm(vocabulary_term_SAMPLE_LIBRARY_SOURCE_VOC_NONGENOMIC)

vocabulary_term_SAMPLE_LIBRARY_SOURCE_VOC_GENOMIC = tr.createNewVocabularyTerm('GENOMIC')
vocabulary_term_SAMPLE_LIBRARY_SOURCE_VOC_GENOMIC.setDescription(None)
vocabulary_term_SAMPLE_LIBRARY_SOURCE_VOC_GENOMIC.setLabel(None)
vocabulary_term_SAMPLE_LIBRARY_SOURCE_VOC_GENOMIC.setOrdinal(1)
vocabulary_SAMPLE_LIBRARY_SOURCE_VOC.addTerm(vocabulary_term_SAMPLE_LIBRARY_SOURCE_VOC_GENOMIC)

vocabulary_term_SAMPLE_LIBRARY_SOURCE_VOC_VIRAL = tr.createNewVocabularyTerm('VIRAL')
vocabulary_term_SAMPLE_LIBRARY_SOURCE_VOC_VIRAL.setDescription(None)
vocabulary_term_SAMPLE_LIBRARY_SOURCE_VOC_VIRAL.setLabel(None)
vocabulary_term_SAMPLE_LIBRARY_SOURCE_VOC_VIRAL.setOrdinal(4)
vocabulary_SAMPLE_LIBRARY_SOURCE_VOC.addTerm(vocabulary_term_SAMPLE_LIBRARY_SOURCE_VOC_VIRAL)

vocabulary_term_SAMPLE_LIBRARY_SOURCE_VOC_SYNTHETIC = tr.createNewVocabularyTerm('SYNTHETIC')
vocabulary_term_SAMPLE_LIBRARY_SOURCE_VOC_SYNTHETIC.setDescription(None)
vocabulary_term_SAMPLE_LIBRARY_SOURCE_VOC_SYNTHETIC.setLabel(None)
vocabulary_term_SAMPLE_LIBRARY_SOURCE_VOC_SYNTHETIC.setOrdinal(3)
vocabulary_SAMPLE_LIBRARY_SOURCE_VOC.addTerm(vocabulary_term_SAMPLE_LIBRARY_SOURCE_VOC_SYNTHETIC)

vocabulary_term_SAMPLE_LIBRARY_SOURCE_VOC_RNA = tr.createNewVocabularyTerm('RNA')
vocabulary_term_SAMPLE_LIBRARY_SOURCE_VOC_RNA.setDescription(None)
vocabulary_term_SAMPLE_LIBRARY_SOURCE_VOC_RNA.setLabel(None)
vocabulary_term_SAMPLE_LIBRARY_SOURCE_VOC_RNA.setOrdinal(5)
vocabulary_SAMPLE_LIBRARY_SOURCE_VOC.addTerm(vocabulary_term_SAMPLE_LIBRARY_SOURCE_VOC_RNA)

vocabulary_SAMPLE_LIBRARY_STRATEGY = tr.createNewVocabulary('SAMPLE_LIBRARY_STRATEGY')
vocabulary_SAMPLE_LIBRARY_STRATEGY.setDescription('Sequencing technique for this library.')
vocabulary_SAMPLE_LIBRARY_STRATEGY.setUrlTemplate(None)
vocabulary_SAMPLE_LIBRARY_STRATEGY.setManagedInternally(False)
vocabulary_SAMPLE_LIBRARY_STRATEGY.setInternalNamespace(False)
vocabulary_SAMPLE_LIBRARY_STRATEGY.setChosenFromList(True)

vocabulary_term_SAMPLE_LIBRARY_STRATEGY_AMPLICON = tr.createNewVocabularyTerm('AMPLICON')
vocabulary_term_SAMPLE_LIBRARY_STRATEGY_AMPLICON.setDescription('Sequencing of overlapping or distinct PCR or RT-PCR products')
vocabulary_term_SAMPLE_LIBRARY_STRATEGY_AMPLICON.setLabel('AMPLICON')
vocabulary_term_SAMPLE_LIBRARY_STRATEGY_AMPLICON.setOrdinal(15)
vocabulary_SAMPLE_LIBRARY_STRATEGY.addTerm(vocabulary_term_SAMPLE_LIBRARY_STRATEGY_AMPLICON)

vocabulary_term_SAMPLE_LIBRARY_STRATEGY_CHIPSEQ = tr.createNewVocabularyTerm('CHIP-SEQ')
vocabulary_term_SAMPLE_LIBRARY_STRATEGY_CHIPSEQ.setDescription('Direct sequencing of chromatin immunoprecipitates')
vocabulary_term_SAMPLE_LIBRARY_STRATEGY_CHIPSEQ.setLabel('ChIP-Seq')
vocabulary_term_SAMPLE_LIBRARY_STRATEGY_CHIPSEQ.setOrdinal(1)
vocabulary_SAMPLE_LIBRARY_STRATEGY.addTerm(vocabulary_term_SAMPLE_LIBRARY_STRATEGY_CHIPSEQ)

vocabulary_term_SAMPLE_LIBRARY_STRATEGY_RNASEQ = tr.createNewVocabularyTerm('RNA-SEQ')
vocabulary_term_SAMPLE_LIBRARY_STRATEGY_RNASEQ.setDescription('RNA sequencing')
vocabulary_term_SAMPLE_LIBRARY_STRATEGY_RNASEQ.setLabel('RNA-Seq')
vocabulary_term_SAMPLE_LIBRARY_STRATEGY_RNASEQ.setOrdinal(2)
vocabulary_SAMPLE_LIBRARY_STRATEGY.addTerm(vocabulary_term_SAMPLE_LIBRARY_STRATEGY_RNASEQ)

vocabulary_term_SAMPLE_LIBRARY_STRATEGY_OTHER = tr.createNewVocabularyTerm('OTHER')
vocabulary_term_SAMPLE_LIBRARY_STRATEGY_OTHER.setDescription('Library strategy not listed')
vocabulary_term_SAMPLE_LIBRARY_STRATEGY_OTHER.setLabel('OTHER')
vocabulary_term_SAMPLE_LIBRARY_STRATEGY_OTHER.setOrdinal(17)
vocabulary_SAMPLE_LIBRARY_STRATEGY.addTerm(vocabulary_term_SAMPLE_LIBRARY_STRATEGY_OTHER)

vocabulary_term_SAMPLE_LIBRARY_STRATEGY_CTS = tr.createNewVocabularyTerm('CTS')
vocabulary_term_SAMPLE_LIBRARY_STRATEGY_CTS.setDescription('Concatenated Tag Sequencing')
vocabulary_term_SAMPLE_LIBRARY_STRATEGY_CTS.setLabel('CTS')
vocabulary_term_SAMPLE_LIBRARY_STRATEGY_CTS.setOrdinal(8)
vocabulary_SAMPLE_LIBRARY_STRATEGY.addTerm(vocabulary_term_SAMPLE_LIBRARY_STRATEGY_CTS)

vocabulary_term_SAMPLE_LIBRARY_STRATEGY_MNASESEQ = tr.createNewVocabularyTerm('MNASE-SEQ')
vocabulary_term_SAMPLE_LIBRARY_STRATEGY_MNASESEQ.setDescription('Direct sequencing following MNase digestion')
vocabulary_term_SAMPLE_LIBRARY_STRATEGY_MNASESEQ.setLabel('MNase-Seq')
vocabulary_term_SAMPLE_LIBRARY_STRATEGY_MNASESEQ.setOrdinal(4)
vocabulary_SAMPLE_LIBRARY_STRATEGY.addTerm(vocabulary_term_SAMPLE_LIBRARY_STRATEGY_MNASESEQ)

vocabulary_term_SAMPLE_LIBRARY_STRATEGY_EST = tr.createNewVocabularyTerm('EST')
vocabulary_term_SAMPLE_LIBRARY_STRATEGY_EST.setDescription('Single pass sequencing of cDNA templates')
vocabulary_term_SAMPLE_LIBRARY_STRATEGY_EST.setLabel('EST')
vocabulary_term_SAMPLE_LIBRARY_STRATEGY_EST.setOrdinal(6)
vocabulary_SAMPLE_LIBRARY_STRATEGY.addTerm(vocabulary_term_SAMPLE_LIBRARY_STRATEGY_EST)

vocabulary_term_SAMPLE_LIBRARY_STRATEGY_FLCDNA = tr.createNewVocabularyTerm('FL-CDNA')
vocabulary_term_SAMPLE_LIBRARY_STRATEGY_FLCDNA.setDescription('Full-length sequencing of cDNA templates')
vocabulary_term_SAMPLE_LIBRARY_STRATEGY_FLCDNA.setLabel('FL-cDNA')
vocabulary_term_SAMPLE_LIBRARY_STRATEGY_FLCDNA.setOrdinal(7)
vocabulary_SAMPLE_LIBRARY_STRATEGY.addTerm(vocabulary_term_SAMPLE_LIBRARY_STRATEGY_FLCDNA)

vocabulary_term_SAMPLE_LIBRARY_STRATEGY_CLONEEND = tr.createNewVocabularyTerm('CLONEEND')
vocabulary_term_SAMPLE_LIBRARY_STRATEGY_CLONEEND.setDescription("Clone end (5', 3', or both) sequencing")
vocabulary_term_SAMPLE_LIBRARY_STRATEGY_CLONEEND.setLabel('CLONEEND')
vocabulary_term_SAMPLE_LIBRARY_STRATEGY_CLONEEND.setOrdinal(10)
vocabulary_SAMPLE_LIBRARY_STRATEGY.addTerm(vocabulary_term_SAMPLE_LIBRARY_STRATEGY_CLONEEND)

vocabulary_term_SAMPLE_LIBRARY_STRATEGY_MEDIPSEQ = tr.createNewVocabularyTerm('MEDIP-SEQ')
vocabulary_term_SAMPLE_LIBRARY_STRATEGY_MEDIPSEQ.setDescription('Methylated DNA immunoprecipitation sequencing')
vocabulary_term_SAMPLE_LIBRARY_STRATEGY_MEDIPSEQ.setLabel('MeDIP-Seq')
vocabulary_term_SAMPLE_LIBRARY_STRATEGY_MEDIPSEQ.setOrdinal(3)
vocabulary_SAMPLE_LIBRARY_STRATEGY.addTerm(vocabulary_term_SAMPLE_LIBRARY_STRATEGY_MEDIPSEQ)

vocabulary_term_SAMPLE_LIBRARY_STRATEGY_BARCODE = tr.createNewVocabularyTerm('BARCODE')
vocabulary_term_SAMPLE_LIBRARY_STRATEGY_BARCODE.setDescription('Sequencing of products that have been tagged with a short identifying sequence (barcode)')
vocabulary_term_SAMPLE_LIBRARY_STRATEGY_BARCODE.setLabel('BARCODE')
vocabulary_term_SAMPLE_LIBRARY_STRATEGY_BARCODE.setOrdinal(9)
vocabulary_SAMPLE_LIBRARY_STRATEGY.addTerm(vocabulary_term_SAMPLE_LIBRARY_STRATEGY_BARCODE)

vocabulary_term_SAMPLE_LIBRARY_STRATEGY_MRESEQ = tr.createNewVocabularyTerm('MRE-SEQ')
vocabulary_term_SAMPLE_LIBRARY_STRATEGY_MRESEQ.setDescription('Methylation-sensitive restriction enzyme sequencing')
vocabulary_term_SAMPLE_LIBRARY_STRATEGY_MRESEQ.setLabel('MRE-Seq')
vocabulary_term_SAMPLE_LIBRARY_STRATEGY_MRESEQ.setOrdinal(5)
vocabulary_SAMPLE_LIBRARY_STRATEGY.addTerm(vocabulary_term_SAMPLE_LIBRARY_STRATEGY_MRESEQ)

vocabulary_term_SAMPLE_LIBRARY_STRATEGY_FINISHING = tr.createNewVocabularyTerm('FINISHING')
vocabulary_term_SAMPLE_LIBRARY_STRATEGY_FINISHING.setDescription('Sequencing intended to finish (close) gaps in existing coverage')
vocabulary_term_SAMPLE_LIBRARY_STRATEGY_FINISHING.setLabel('FINISHING')
vocabulary_term_SAMPLE_LIBRARY_STRATEGY_FINISHING.setOrdinal(16)
vocabulary_SAMPLE_LIBRARY_STRATEGY.addTerm(vocabulary_term_SAMPLE_LIBRARY_STRATEGY_FINISHING)

vocabulary_term_SAMPLE_LIBRARY_STRATEGY_WCS = tr.createNewVocabularyTerm('WCS')
vocabulary_term_SAMPLE_LIBRARY_STRATEGY_WCS.setDescription('Whole chromosome (or other replicon) shotgun')
vocabulary_term_SAMPLE_LIBRARY_STRATEGY_WCS.setLabel('WCS')
vocabulary_term_SAMPLE_LIBRARY_STRATEGY_WCS.setOrdinal(12)
vocabulary_SAMPLE_LIBRARY_STRATEGY.addTerm(vocabulary_term_SAMPLE_LIBRARY_STRATEGY_WCS)

vocabulary_term_SAMPLE_LIBRARY_STRATEGY_POOLCLONE = tr.createNewVocabularyTerm('POOLCLONE')
vocabulary_term_SAMPLE_LIBRARY_STRATEGY_POOLCLONE.setDescription('Shotgun of pooled clones (usually BACs and Fosmids)')
vocabulary_term_SAMPLE_LIBRARY_STRATEGY_POOLCLONE.setLabel('POOLCLONE')
vocabulary_term_SAMPLE_LIBRARY_STRATEGY_POOLCLONE.setOrdinal(14)
vocabulary_SAMPLE_LIBRARY_STRATEGY.addTerm(vocabulary_term_SAMPLE_LIBRARY_STRATEGY_POOLCLONE)

vocabulary_term_SAMPLE_LIBRARY_STRATEGY_CLONE = tr.createNewVocabularyTerm('CLONE')
vocabulary_term_SAMPLE_LIBRARY_STRATEGY_CLONE.setDescription('Genomic clone based (hierarchical) sequencing')
vocabulary_term_SAMPLE_LIBRARY_STRATEGY_CLONE.setLabel('CLONE')
vocabulary_term_SAMPLE_LIBRARY_STRATEGY_CLONE.setOrdinal(13)
vocabulary_SAMPLE_LIBRARY_STRATEGY.addTerm(vocabulary_term_SAMPLE_LIBRARY_STRATEGY_CLONE)

vocabulary_term_SAMPLE_LIBRARY_STRATEGY_WGS = tr.createNewVocabularyTerm('WGS')
vocabulary_term_SAMPLE_LIBRARY_STRATEGY_WGS.setDescription('Whole genome shotgun')
vocabulary_term_SAMPLE_LIBRARY_STRATEGY_WGS.setLabel('WGS')
vocabulary_term_SAMPLE_LIBRARY_STRATEGY_WGS.setOrdinal(11)
vocabulary_SAMPLE_LIBRARY_STRATEGY.addTerm(vocabulary_term_SAMPLE_LIBRARY_STRATEGY_WGS)

vocabulary_SAMPLE_MOLECULE = tr.createNewVocabulary('SAMPLE_MOLECULE')
vocabulary_SAMPLE_MOLECULE.setDescription(None)
vocabulary_SAMPLE_MOLECULE.setUrlTemplate(None)
vocabulary_SAMPLE_MOLECULE.setManagedInternally(False)
vocabulary_SAMPLE_MOLECULE.setInternalNamespace(False)
vocabulary_SAMPLE_MOLECULE.setChosenFromList(True)

vocabulary_term_SAMPLE_MOLECULE_PROTEIN = tr.createNewVocabularyTerm('PROTEIN')
vocabulary_term_SAMPLE_MOLECULE_PROTEIN.setDescription(None)
vocabulary_term_SAMPLE_MOLECULE_PROTEIN.setLabel(None)
vocabulary_term_SAMPLE_MOLECULE_PROTEIN.setOrdinal(6)
vocabulary_SAMPLE_MOLECULE.addTerm(vocabulary_term_SAMPLE_MOLECULE_PROTEIN)

vocabulary_term_SAMPLE_MOLECULE_TOTAL_RNA = tr.createNewVocabularyTerm('TOTAL_RNA')
vocabulary_term_SAMPLE_MOLECULE_TOTAL_RNA.setDescription(None)
vocabulary_term_SAMPLE_MOLECULE_TOTAL_RNA.setLabel(None)
vocabulary_term_SAMPLE_MOLECULE_TOTAL_RNA.setOrdinal(1)
vocabulary_SAMPLE_MOLECULE.addTerm(vocabulary_term_SAMPLE_MOLECULE_TOTAL_RNA)

vocabulary_term_SAMPLE_MOLECULE_CYTOPLASMIC_RNA = tr.createNewVocabularyTerm('CYTOPLASMIC_RNA')
vocabulary_term_SAMPLE_MOLECULE_CYTOPLASMIC_RNA.setDescription(None)
vocabulary_term_SAMPLE_MOLECULE_CYTOPLASMIC_RNA.setLabel(None)
vocabulary_term_SAMPLE_MOLECULE_CYTOPLASMIC_RNA.setOrdinal(3)
vocabulary_SAMPLE_MOLECULE.addTerm(vocabulary_term_SAMPLE_MOLECULE_CYTOPLASMIC_RNA)

vocabulary_term_SAMPLE_MOLECULE_NUCLEAR_RNA = tr.createNewVocabularyTerm('NUCLEAR_RNA')
vocabulary_term_SAMPLE_MOLECULE_NUCLEAR_RNA.setDescription(None)
vocabulary_term_SAMPLE_MOLECULE_NUCLEAR_RNA.setLabel(None)
vocabulary_term_SAMPLE_MOLECULE_NUCLEAR_RNA.setOrdinal(4)
vocabulary_SAMPLE_MOLECULE.addTerm(vocabulary_term_SAMPLE_MOLECULE_NUCLEAR_RNA)

vocabulary_term_SAMPLE_MOLECULE_GENOMIC_DNA = tr.createNewVocabularyTerm('GENOMIC_DNA')
vocabulary_term_SAMPLE_MOLECULE_GENOMIC_DNA.setDescription(None)
vocabulary_term_SAMPLE_MOLECULE_GENOMIC_DNA.setLabel(None)
vocabulary_term_SAMPLE_MOLECULE_GENOMIC_DNA.setOrdinal(5)
vocabulary_SAMPLE_MOLECULE.addTerm(vocabulary_term_SAMPLE_MOLECULE_GENOMIC_DNA)

vocabulary_term_SAMPLE_MOLECULE_POLYA_RNA = tr.createNewVocabularyTerm('POLYA_RNA')
vocabulary_term_SAMPLE_MOLECULE_POLYA_RNA.setDescription(None)
vocabulary_term_SAMPLE_MOLECULE_POLYA_RNA.setLabel(None)
vocabulary_term_SAMPLE_MOLECULE_POLYA_RNA.setOrdinal(2)
vocabulary_SAMPLE_MOLECULE.addTerm(vocabulary_term_SAMPLE_MOLECULE_POLYA_RNA)

vocabulary_term_SAMPLE_MOLECULE_OTHER = tr.createNewVocabularyTerm('OTHER')
vocabulary_term_SAMPLE_MOLECULE_OTHER.setDescription(None)
vocabulary_term_SAMPLE_MOLECULE_OTHER.setLabel(None)
vocabulary_term_SAMPLE_MOLECULE_OTHER.setOrdinal(18)
vocabulary_SAMPLE_MOLECULE.addTerm(vocabulary_term_SAMPLE_MOLECULE_OTHER)

vocabulary_SAMPLE_TYPE = tr.createNewVocabulary('SAMPLE_TYPE')
vocabulary_SAMPLE_TYPE.setDescription('Type of sample delivered by the customer')
vocabulary_SAMPLE_TYPE.setUrlTemplate(None)
vocabulary_SAMPLE_TYPE.setManagedInternally(False)
vocabulary_SAMPLE_TYPE.setInternalNamespace(False)
vocabulary_SAMPLE_TYPE.setChosenFromList(True)

vocabulary_term_SAMPLE_TYPE_MRNA = tr.createNewVocabularyTerm('MRNA')
vocabulary_term_SAMPLE_TYPE_MRNA.setDescription(None)
vocabulary_term_SAMPLE_TYPE_MRNA.setLabel(None)
vocabulary_term_SAMPLE_TYPE_MRNA.setOrdinal(8)
vocabulary_SAMPLE_TYPE.addTerm(vocabulary_term_SAMPLE_TYPE_MRNA)

vocabulary_term_SAMPLE_TYPE_OTHER = tr.createNewVocabularyTerm('OTHER')
vocabulary_term_SAMPLE_TYPE_OTHER.setDescription(None)
vocabulary_term_SAMPLE_TYPE_OTHER.setLabel(None)
vocabulary_term_SAMPLE_TYPE_OTHER.setOrdinal(9)
vocabulary_SAMPLE_TYPE.addTerm(vocabulary_term_SAMPLE_TYPE_OTHER)

vocabulary_term_SAMPLE_TYPE_PROCESSED_DNA_LIBRARY = tr.createNewVocabularyTerm('PROCESSED_DNA_LIBRARY')
vocabulary_term_SAMPLE_TYPE_PROCESSED_DNA_LIBRARY.setDescription(None)
vocabulary_term_SAMPLE_TYPE_PROCESSED_DNA_LIBRARY.setLabel(None)
vocabulary_term_SAMPLE_TYPE_PROCESSED_DNA_LIBRARY.setOrdinal(10)
vocabulary_SAMPLE_TYPE.addTerm(vocabulary_term_SAMPLE_TYPE_PROCESSED_DNA_LIBRARY)

vocabulary_term_SAMPLE_TYPE_GENOMIC_DNA = tr.createNewVocabularyTerm('GENOMIC_DNA')
vocabulary_term_SAMPLE_TYPE_GENOMIC_DNA.setDescription(None)
vocabulary_term_SAMPLE_TYPE_GENOMIC_DNA.setLabel(None)
vocabulary_term_SAMPLE_TYPE_GENOMIC_DNA.setOrdinal(1)
vocabulary_SAMPLE_TYPE.addTerm(vocabulary_term_SAMPLE_TYPE_GENOMIC_DNA)

vocabulary_term_SAMPLE_TYPE_SMALL_RNA = tr.createNewVocabularyTerm('SMALL_RNA')
vocabulary_term_SAMPLE_TYPE_SMALL_RNA.setDescription(None)
vocabulary_term_SAMPLE_TYPE_SMALL_RNA.setLabel(None)
vocabulary_term_SAMPLE_TYPE_SMALL_RNA.setOrdinal(2)
vocabulary_SAMPLE_TYPE.addTerm(vocabulary_term_SAMPLE_TYPE_SMALL_RNA)

vocabulary_term_SAMPLE_TYPE_CHIP = tr.createNewVocabularyTerm('CHIP')
vocabulary_term_SAMPLE_TYPE_CHIP.setDescription(None)
vocabulary_term_SAMPLE_TYPE_CHIP.setLabel(None)
vocabulary_term_SAMPLE_TYPE_CHIP.setOrdinal(4)
vocabulary_SAMPLE_TYPE.addTerm(vocabulary_term_SAMPLE_TYPE_CHIP)

vocabulary_term_SAMPLE_TYPE_SYNTHETIC = tr.createNewVocabularyTerm('SYNTHETIC')
vocabulary_term_SAMPLE_TYPE_SYNTHETIC.setDescription(None)
vocabulary_term_SAMPLE_TYPE_SYNTHETIC.setLabel(None)
vocabulary_term_SAMPLE_TYPE_SYNTHETIC.setOrdinal(6)
vocabulary_SAMPLE_TYPE.addTerm(vocabulary_term_SAMPLE_TYPE_SYNTHETIC)

vocabulary_term_SAMPLE_TYPE_TOTAL_RNA = tr.createNewVocabularyTerm('TOTAL_RNA')
vocabulary_term_SAMPLE_TYPE_TOTAL_RNA.setDescription(None)
vocabulary_term_SAMPLE_TYPE_TOTAL_RNA.setLabel(None)
vocabulary_term_SAMPLE_TYPE_TOTAL_RNA.setOrdinal(3)
vocabulary_SAMPLE_TYPE.addTerm(vocabulary_term_SAMPLE_TYPE_TOTAL_RNA)

vocabulary_term_SAMPLE_TYPE_FRAGMENTED_GENOMIC_DNA = tr.createNewVocabularyTerm('FRAGMENTED_GENOMIC_DNA')
vocabulary_term_SAMPLE_TYPE_FRAGMENTED_GENOMIC_DNA.setDescription(None)
vocabulary_term_SAMPLE_TYPE_FRAGMENTED_GENOMIC_DNA.setLabel(None)
vocabulary_term_SAMPLE_TYPE_FRAGMENTED_GENOMIC_DNA.setOrdinal(7)
vocabulary_SAMPLE_TYPE.addTerm(vocabulary_term_SAMPLE_TYPE_FRAGMENTED_GENOMIC_DNA)

vocabulary_term_SAMPLE_TYPE_NONGENOMIC = tr.createNewVocabularyTerm('NON-GENOMIC')
vocabulary_term_SAMPLE_TYPE_NONGENOMIC.setDescription(None)
vocabulary_term_SAMPLE_TYPE_NONGENOMIC.setLabel(None)
vocabulary_term_SAMPLE_TYPE_NONGENOMIC.setOrdinal(12)
vocabulary_SAMPLE_TYPE.addTerm(vocabulary_term_SAMPLE_TYPE_NONGENOMIC)

vocabulary_term_SAMPLE_TYPE_VIRAL = tr.createNewVocabularyTerm('VIRAL')
vocabulary_term_SAMPLE_TYPE_VIRAL.setDescription(None)
vocabulary_term_SAMPLE_TYPE_VIRAL.setLabel(None)
vocabulary_term_SAMPLE_TYPE_VIRAL.setOrdinal(5)
vocabulary_SAMPLE_TYPE.addTerm(vocabulary_term_SAMPLE_TYPE_VIRAL)

vocabulary_term_SAMPLE_TYPE_BISULFITE_CHIP = tr.createNewVocabularyTerm('BISULFITE_CHIP')
vocabulary_term_SAMPLE_TYPE_BISULFITE_CHIP.setDescription(None)
vocabulary_term_SAMPLE_TYPE_BISULFITE_CHIP.setLabel(None)
vocabulary_term_SAMPLE_TYPE_BISULFITE_CHIP.setOrdinal(11)
vocabulary_SAMPLE_TYPE.addTerm(vocabulary_term_SAMPLE_TYPE_BISULFITE_CHIP)

vocabulary_SBS_SEQUENCING_KIT_VERSION = tr.createNewVocabulary('SBS_SEQUENCING_KIT_VERSION')
vocabulary_SBS_SEQUENCING_KIT_VERSION.setDescription('Version of the Sequencing by Synthesis (SBS) Kit')
vocabulary_SBS_SEQUENCING_KIT_VERSION.setUrlTemplate(None)
vocabulary_SBS_SEQUENCING_KIT_VERSION.setManagedInternally(False)
vocabulary_SBS_SEQUENCING_KIT_VERSION.setInternalNamespace(False)
vocabulary_SBS_SEQUENCING_KIT_VERSION.setChosenFromList(True)

vocabulary_term_SBS_SEQUENCING_KIT_VERSION_SBS_HS_V3 = tr.createNewVocabularyTerm('SBS_HS_V3')
vocabulary_term_SBS_SEQUENCING_KIT_VERSION_SBS_HS_V3.setDescription(None)
vocabulary_term_SBS_SEQUENCING_KIT_VERSION_SBS_HS_V3.setLabel('TruSeq SBS HS v3')
vocabulary_term_SBS_SEQUENCING_KIT_VERSION_SBS_HS_V3.setOrdinal(6)
vocabulary_SBS_SEQUENCING_KIT_VERSION.addTerm(vocabulary_term_SBS_SEQUENCING_KIT_VERSION_SBS_HS_V3)

vocabulary_term_SBS_SEQUENCING_KIT_VERSION_V3 = tr.createNewVocabularyTerm('V3')
vocabulary_term_SBS_SEQUENCING_KIT_VERSION_V3.setDescription(None)
vocabulary_term_SBS_SEQUENCING_KIT_VERSION_V3.setLabel(None)
vocabulary_term_SBS_SEQUENCING_KIT_VERSION_V3.setOrdinal(5)
vocabulary_SBS_SEQUENCING_KIT_VERSION.addTerm(vocabulary_term_SBS_SEQUENCING_KIT_VERSION_V3)

vocabulary_term_SBS_SEQUENCING_KIT_VERSION_V4 = tr.createNewVocabularyTerm('V4')
vocabulary_term_SBS_SEQUENCING_KIT_VERSION_V4.setDescription(None)
vocabulary_term_SBS_SEQUENCING_KIT_VERSION_V4.setLabel(None)
vocabulary_term_SBS_SEQUENCING_KIT_VERSION_V4.setOrdinal(4)
vocabulary_SBS_SEQUENCING_KIT_VERSION.addTerm(vocabulary_term_SBS_SEQUENCING_KIT_VERSION_V4)

vocabulary_term_SBS_SEQUENCING_KIT_VERSION_TRUSEQ_V5 = tr.createNewVocabularyTerm('TRUSEQ_V5')
vocabulary_term_SBS_SEQUENCING_KIT_VERSION_TRUSEQ_V5.setDescription(None)
vocabulary_term_SBS_SEQUENCING_KIT_VERSION_TRUSEQ_V5.setLabel('TruSeq v5')
vocabulary_term_SBS_SEQUENCING_KIT_VERSION_TRUSEQ_V5.setOrdinal(1)
vocabulary_SBS_SEQUENCING_KIT_VERSION.addTerm(vocabulary_term_SBS_SEQUENCING_KIT_VERSION_TRUSEQ_V5)

vocabulary_term_SBS_SEQUENCING_KIT_VERSION_V5 = tr.createNewVocabularyTerm('V5')
vocabulary_term_SBS_SEQUENCING_KIT_VERSION_V5.setDescription(None)
vocabulary_term_SBS_SEQUENCING_KIT_VERSION_V5.setLabel(None)
vocabulary_term_SBS_SEQUENCING_KIT_VERSION_V5.setOrdinal(3)
vocabulary_SBS_SEQUENCING_KIT_VERSION.addTerm(vocabulary_term_SBS_SEQUENCING_KIT_VERSION_V5)

vocabulary_SCS_PROTOCOL_VERSION = tr.createNewVocabulary('SCS_PROTOCOL_VERSION')
vocabulary_SCS_PROTOCOL_VERSION.setDescription('THE Illumina protocol')
vocabulary_SCS_PROTOCOL_VERSION.setUrlTemplate(None)
vocabulary_SCS_PROTOCOL_VERSION.setManagedInternally(False)
vocabulary_SCS_PROTOCOL_VERSION.setInternalNamespace(False)
vocabulary_SCS_PROTOCOL_VERSION.setChosenFromList(True)

vocabulary_term_SCS_PROTOCOL_VERSION_V7 = tr.createNewVocabularyTerm('V7')
vocabulary_term_SCS_PROTOCOL_VERSION_V7.setDescription(None)
vocabulary_term_SCS_PROTOCOL_VERSION_V7.setLabel(None)
vocabulary_term_SCS_PROTOCOL_VERSION_V7.setOrdinal(3)
vocabulary_SCS_PROTOCOL_VERSION.addTerm(vocabulary_term_SCS_PROTOCOL_VERSION_V7)

vocabulary_term_SCS_PROTOCOL_VERSION_V8 = tr.createNewVocabularyTerm('V8')
vocabulary_term_SCS_PROTOCOL_VERSION_V8.setDescription(None)
vocabulary_term_SCS_PROTOCOL_VERSION_V8.setLabel(None)
vocabulary_term_SCS_PROTOCOL_VERSION_V8.setOrdinal(1)
vocabulary_SCS_PROTOCOL_VERSION.addTerm(vocabulary_term_SCS_PROTOCOL_VERSION_V8)

vocabulary_term_SCS_PROTOCOL_VERSION_V6 = tr.createNewVocabularyTerm('V6')
vocabulary_term_SCS_PROTOCOL_VERSION_V6.setDescription(None)
vocabulary_term_SCS_PROTOCOL_VERSION_V6.setLabel(None)
vocabulary_term_SCS_PROTOCOL_VERSION_V6.setOrdinal(2)
vocabulary_SCS_PROTOCOL_VERSION.addTerm(vocabulary_term_SCS_PROTOCOL_VERSION_V6)

vocabulary_SCS_SOFTWARE_VERSION = tr.createNewVocabulary('SCS_SOFTWARE_VERSION')
vocabulary_SCS_SOFTWARE_VERSION.setDescription('Software version of the Sequencing PC attached to the GA')
vocabulary_SCS_SOFTWARE_VERSION.setUrlTemplate(None)
vocabulary_SCS_SOFTWARE_VERSION.setManagedInternally(False)
vocabulary_SCS_SOFTWARE_VERSION.setInternalNamespace(False)
vocabulary_SCS_SOFTWARE_VERSION.setChosenFromList(True)

vocabulary_term_SCS_SOFTWARE_VERSION_HCS_145 = tr.createNewVocabularyTerm('HCS_1.4.5')
vocabulary_term_SCS_SOFTWARE_VERSION_HCS_145.setDescription(None)
vocabulary_term_SCS_SOFTWARE_VERSION_HCS_145.setLabel('HCS 1.4.5/RTA 1.12.4')
vocabulary_term_SCS_SOFTWARE_VERSION_HCS_145.setOrdinal(2)
vocabulary_SCS_SOFTWARE_VERSION.addTerm(vocabulary_term_SCS_SOFTWARE_VERSION_HCS_145)

vocabulary_term_SCS_SOFTWARE_VERSION_210 = tr.createNewVocabularyTerm('2.10')
vocabulary_term_SCS_SOFTWARE_VERSION_210.setDescription(None)
vocabulary_term_SCS_SOFTWARE_VERSION_210.setLabel('2.10/RTA 1.13.48')
vocabulary_term_SCS_SOFTWARE_VERSION_210.setOrdinal(4)
vocabulary_SCS_SOFTWARE_VERSION.addTerm(vocabulary_term_SCS_SOFTWARE_VERSION_210)

vocabulary_term_SCS_SOFTWARE_VERSION_25 = tr.createNewVocabularyTerm('2.5')
vocabulary_term_SCS_SOFTWARE_VERSION_25.setDescription(None)
vocabulary_term_SCS_SOFTWARE_VERSION_25.setLabel('2.5 (no longer in use)')
vocabulary_term_SCS_SOFTWARE_VERSION_25.setOrdinal(8)
vocabulary_SCS_SOFTWARE_VERSION.addTerm(vocabulary_term_SCS_SOFTWARE_VERSION_25)

vocabulary_term_SCS_SOFTWARE_VERSION_HCS_1515 = tr.createNewVocabularyTerm('HCS_1.5.15')
vocabulary_term_SCS_SOFTWARE_VERSION_HCS_1515.setDescription(None)
vocabulary_term_SCS_SOFTWARE_VERSION_HCS_1515.setLabel('HCS 1.5.15/1.13.48')
vocabulary_term_SCS_SOFTWARE_VERSION_HCS_1515.setOrdinal(1)
vocabulary_SCS_SOFTWARE_VERSION.addTerm(vocabulary_term_SCS_SOFTWARE_VERSION_HCS_1515)

vocabulary_term_SCS_SOFTWARE_VERSION_28 = tr.createNewVocabularyTerm('2.8')
vocabulary_term_SCS_SOFTWARE_VERSION_28.setDescription(None)
vocabulary_term_SCS_SOFTWARE_VERSION_28.setLabel('2.8/RTA 1.8')
vocabulary_term_SCS_SOFTWARE_VERSION_28.setOrdinal(6)
vocabulary_SCS_SOFTWARE_VERSION.addTerm(vocabulary_term_SCS_SOFTWARE_VERSION_28)

vocabulary_term_SCS_SOFTWARE_VERSION_26 = tr.createNewVocabularyTerm('2.6')
vocabulary_term_SCS_SOFTWARE_VERSION_26.setDescription(None)
vocabulary_term_SCS_SOFTWARE_VERSION_26.setLabel('2.6/RTA 1.6 (no longer in use)')
vocabulary_term_SCS_SOFTWARE_VERSION_26.setOrdinal(7)
vocabulary_SCS_SOFTWARE_VERSION.addTerm(vocabulary_term_SCS_SOFTWARE_VERSION_26)

vocabulary_term_SCS_SOFTWARE_VERSION_HCS_14 = tr.createNewVocabularyTerm('HCS_1.4')
vocabulary_term_SCS_SOFTWARE_VERSION_HCS_14.setDescription(None)
vocabulary_term_SCS_SOFTWARE_VERSION_HCS_14.setLabel('HCS 1.4/RTA 1.12')
vocabulary_term_SCS_SOFTWARE_VERSION_HCS_14.setOrdinal(3)
vocabulary_SCS_SOFTWARE_VERSION.addTerm(vocabulary_term_SCS_SOFTWARE_VERSION_HCS_14)

vocabulary_term_SCS_SOFTWARE_VERSION_24 = tr.createNewVocabularyTerm('2.4')
vocabulary_term_SCS_SOFTWARE_VERSION_24.setDescription(None)
vocabulary_term_SCS_SOFTWARE_VERSION_24.setLabel('2.4 (no longer in use)')
vocabulary_term_SCS_SOFTWARE_VERSION_24.setOrdinal(9)
vocabulary_SCS_SOFTWARE_VERSION.addTerm(vocabulary_term_SCS_SOFTWARE_VERSION_24)

vocabulary_term_SCS_SOFTWARE_VERSION_29 = tr.createNewVocabularyTerm('2.9')
vocabulary_term_SCS_SOFTWARE_VERSION_29.setDescription(None)
vocabulary_term_SCS_SOFTWARE_VERSION_29.setLabel('2.9/RTA2.9')
vocabulary_term_SCS_SOFTWARE_VERSION_29.setOrdinal(5)
vocabulary_SCS_SOFTWARE_VERSION.addTerm(vocabulary_term_SCS_SOFTWARE_VERSION_29)

vocabulary_SEQUENCER = tr.createNewVocabulary('SEQUENCER')
vocabulary_SEQUENCER.setDescription('Which Sequencer was used?')
vocabulary_SEQUENCER.setUrlTemplate(None)
vocabulary_SEQUENCER.setManagedInternally(False)
vocabulary_SEQUENCER.setInternalNamespace(False)
vocabulary_SEQUENCER.setChosenFromList(True)

vocabulary_term_SEQUENCER_ATTILA = tr.createNewVocabularyTerm('ATTILA')
vocabulary_term_SEQUENCER_ATTILA.setDescription('old GA II, do not use any more (deprecated)')
vocabulary_term_SEQUENCER_ATTILA.setLabel('Attila GA II')
vocabulary_term_SEQUENCER_ATTILA.setOrdinal(6)
vocabulary_SEQUENCER.addTerm(vocabulary_term_SEQUENCER_ATTILA)

vocabulary_term_SEQUENCER_RUA = tr.createNewVocabularyTerm('RUA')
vocabulary_term_SEQUENCER_RUA.setDescription('back of the room')
vocabulary_term_SEQUENCER_RUA.setLabel('Rua HiSeq2000')
vocabulary_term_SEQUENCER_RUA.setOrdinal(1)
vocabulary_SEQUENCER.addTerm(vocabulary_term_SEQUENCER_RUA)

vocabulary_term_SEQUENCER_ELLAC = tr.createNewVocabularyTerm('ELLAC')
vocabulary_term_SEQUENCER_ELLAC.setDescription('GA IIx at the front of the room')
vocabulary_term_SEQUENCER_ELLAC.setLabel('Ellac GA IIx')
vocabulary_term_SEQUENCER_ELLAC.setOrdinal(4)
vocabulary_SEQUENCER.addTerm(vocabulary_term_SEQUENCER_ELLAC)

vocabulary_term_SEQUENCER_ATTILAX = tr.createNewVocabularyTerm('ATTILAX')
vocabulary_term_SEQUENCER_ATTILAX.setDescription('Upgraded Attila, back of the room (deprecated)')
vocabulary_term_SEQUENCER_ATTILAX.setLabel('Attila GA IIx')
vocabulary_term_SEQUENCER_ATTILAX.setOrdinal(5)
vocabulary_SEQUENCER.addTerm(vocabulary_term_SEQUENCER_ATTILAX)

vocabulary_term_SEQUENCER_KITT = tr.createNewVocabularyTerm('KITT')
vocabulary_term_SEQUENCER_KITT.setDescription(None)
vocabulary_term_SEQUENCER_KITT.setLabel('Kitt HiSeq2500')
vocabulary_term_SEQUENCER_KITT.setOrdinal(2)
vocabulary_SEQUENCER.addTerm(vocabulary_term_SEQUENCER_KITT)

vocabulary_SEQUENCING_APPLICATION = tr.createNewVocabulary('SEQUENCING_APPLICATION')
vocabulary_SEQUENCING_APPLICATION.setDescription('Type of experiment of the High Throughput Sequencing applications')
vocabulary_SEQUENCING_APPLICATION.setUrlTemplate(None)
vocabulary_SEQUENCING_APPLICATION.setManagedInternally(False)
vocabulary_SEQUENCING_APPLICATION.setInternalNamespace(False)
vocabulary_SEQUENCING_APPLICATION.setChosenFromList(True)

vocabulary_term_SEQUENCING_APPLICATION_GENOMIC_DNA_SEQ = tr.createNewVocabularyTerm('GENOMIC_DNA_SEQ')
vocabulary_term_SEQUENCING_APPLICATION_GENOMIC_DNA_SEQ.setDescription(None)
vocabulary_term_SEQUENCING_APPLICATION_GENOMIC_DNA_SEQ.setLabel(None)
vocabulary_term_SEQUENCING_APPLICATION_GENOMIC_DNA_SEQ.setOrdinal(3)
vocabulary_SEQUENCING_APPLICATION.addTerm(vocabulary_term_SEQUENCING_APPLICATION_GENOMIC_DNA_SEQ)

vocabulary_term_SEQUENCING_APPLICATION_RNA_SEQ = tr.createNewVocabularyTerm('RNA_SEQ')
vocabulary_term_SEQUENCING_APPLICATION_RNA_SEQ.setDescription(None)
vocabulary_term_SEQUENCING_APPLICATION_RNA_SEQ.setLabel(None)
vocabulary_term_SEQUENCING_APPLICATION_RNA_SEQ.setOrdinal(2)
vocabulary_SEQUENCING_APPLICATION.addTerm(vocabulary_term_SEQUENCING_APPLICATION_RNA_SEQ)

vocabulary_term_SEQUENCING_APPLICATION_SMALL_RNA_SEQ = tr.createNewVocabularyTerm('SMALL_RNA_SEQ')
vocabulary_term_SEQUENCING_APPLICATION_SMALL_RNA_SEQ.setDescription(None)
vocabulary_term_SEQUENCING_APPLICATION_SMALL_RNA_SEQ.setLabel(None)
vocabulary_term_SEQUENCING_APPLICATION_SMALL_RNA_SEQ.setOrdinal(1)
vocabulary_SEQUENCING_APPLICATION.addTerm(vocabulary_term_SEQUENCING_APPLICATION_SMALL_RNA_SEQ)

vocabulary_term_SEQUENCING_APPLICATION_CHIP_SEQ = tr.createNewVocabularyTerm('CHIP_SEQ')
vocabulary_term_SEQUENCING_APPLICATION_CHIP_SEQ.setDescription(None)
vocabulary_term_SEQUENCING_APPLICATION_CHIP_SEQ.setLabel(None)
vocabulary_term_SEQUENCING_APPLICATION_CHIP_SEQ.setOrdinal(4)
vocabulary_SEQUENCING_APPLICATION.addTerm(vocabulary_term_SEQUENCING_APPLICATION_CHIP_SEQ)

vocabulary_TRANSFER_METHOD = tr.createNewVocabulary('TRANSFER_METHOD')
vocabulary_TRANSFER_METHOD.setDescription('How does the customer get the data')
vocabulary_TRANSFER_METHOD.setUrlTemplate(None)
vocabulary_TRANSFER_METHOD.setManagedInternally(False)
vocabulary_TRANSFER_METHOD.setInternalNamespace(False)
vocabulary_TRANSFER_METHOD.setChosenFromList(True)

vocabulary_term_TRANSFER_METHOD_RSYNC = tr.createNewVocabularyTerm('RSYNC')
vocabulary_term_TRANSFER_METHOD_RSYNC.setDescription(None)
vocabulary_term_TRANSFER_METHOD_RSYNC.setLabel(None)
vocabulary_term_TRANSFER_METHOD_RSYNC.setOrdinal(1)
vocabulary_TRANSFER_METHOD.addTerm(vocabulary_term_TRANSFER_METHOD_RSYNC)

exp_type_HT_SEQUENCING = tr.createNewExperimentType('HT_SEQUENCING')
exp_type_HT_SEQUENCING.setDescription('High Throughput Sequencing (e.g. Illumina GA)')

samp_type_ILLUMINA_FLOW_CELL = tr.createNewSampleType('ILLUMINA_FLOW_CELL')
samp_type_ILLUMINA_FLOW_CELL.setDescription('Flow Cell containing eight Flow Lanes')
samp_type_ILLUMINA_FLOW_CELL.setListable(True)
samp_type_ILLUMINA_FLOW_CELL.setSubcodeUnique(False)
samp_type_ILLUMINA_FLOW_CELL.setAutoGeneratedCode(False)
samp_type_ILLUMINA_FLOW_CELL.setGeneratedCodePrefix('BSSE-QGF-FC-')

samp_type_ILLUMINA_FLOW_LANE = tr.createNewSampleType('ILLUMINA_FLOW_LANE')
samp_type_ILLUMINA_FLOW_LANE.setDescription('Flow Lane containing one Biological Sample')
samp_type_ILLUMINA_FLOW_LANE.setListable(True)
samp_type_ILLUMINA_FLOW_LANE.setSubcodeUnique(False)
samp_type_ILLUMINA_FLOW_LANE.setAutoGeneratedCode(False)
samp_type_ILLUMINA_FLOW_LANE.setGeneratedCodePrefix('BSSE-QGF-FL-')

samp_type_ILLUMINA_SEQUENCING = tr.createNewSampleType('ILLUMINA_SEQUENCING')
samp_type_ILLUMINA_SEQUENCING.setDescription('Biological Sample')
samp_type_ILLUMINA_SEQUENCING.setListable(True)
samp_type_ILLUMINA_SEQUENCING.setSubcodeUnique(False)
samp_type_ILLUMINA_SEQUENCING.setAutoGeneratedCode(True)
samp_type_ILLUMINA_SEQUENCING.setGeneratedCodePrefix('BSSE-QGF-')

data_set_type_ALIGNMENT = tr.createNewDataSetType('ALIGNMENT')
data_set_type_ALIGNMENT.setDescription('Aligner ouput, ideally bam/sam')

data_set_type_BASECALL_STATS = tr.createNewDataSetType('BASECALL_STATS')
data_set_type_BASECALL_STATS.setDescription('Base Call Statistics')

data_set_type_BIGWIGGLE = tr.createNewDataSetType('BIGWIGGLE')
data_set_type_BIGWIGGLE.setDescription('visualization')

data_set_type_ELAND_ALIGNMENT = tr.createNewDataSetType('ELAND_ALIGNMENT')
data_set_type_ELAND_ALIGNMENT.setDescription('Illumina Eland Alignment Output')

data_set_type_FASTQ_GZ = tr.createNewDataSetType('FASTQ_GZ')
data_set_type_FASTQ_GZ.setDescription('Gzipped Fastq file produced by Casava 1.8+')

data_set_type_ILLUMINA_GA_OUTPUT = tr.createNewDataSetType('ILLUMINA_GA_OUTPUT')
data_set_type_ILLUMINA_GA_OUTPUT.setDescription('The directory structure as produced by the Illumina GA Pipeline (whatever Illumina spits out)')

data_set_type_ILLUMINA_HISEQ_OUTPUT = tr.createNewDataSetType('ILLUMINA_HISEQ_OUTPUT')
data_set_type_ILLUMINA_HISEQ_OUTPUT.setDescription('HiSeq2000 Output')

data_set_type_MACS_OUTPUT = tr.createNewDataSetType('MACS_OUTPUT')
data_set_type_MACS_OUTPUT.setDescription('MACS Peak Caller output')

data_set_type_QUALITY_JPGS = tr.createNewDataSetType('QUALITY_JPGS')
data_set_type_QUALITY_JPGS.setDescription('R generated Quality plots')

data_set_type_QUALITY_PDFS = tr.createNewDataSetType('QUALITY_PDFS')
data_set_type_QUALITY_PDFS.setDescription('R generated pdfs showing quality data')

data_set_type_QUALITY_SVG = tr.createNewDataSetType('QUALITY_SVG')
data_set_type_QUALITY_SVG.setDescription('R generated Quality plots')

data_set_type_RUNINFO = tr.createNewDataSetType('RUNINFO')
data_set_type_RUNINFO.setDescription('Run statstics: Status.html and Status_Files folder')

data_set_type_SRF_PER_LANE = tr.createNewDataSetType('SRF_PER_LANE')
data_set_type_SRF_PER_LANE.setDescription('Short Read Format file (SRF) plus meta data file (TSV)')

data_set_type_THUMBNAILS = tr.createNewDataSetType('THUMBNAILS')
data_set_type_THUMBNAILS.setDescription('Thumbnails provided by the Illumina software')

data_set_type_TSV = tr.createNewDataSetType('TSV')
data_set_type_TSV.setDescription('Tab separated files')

data_set_type_UNKNOWN = tr.createNewDataSetType('UNKNOWN')
data_set_type_UNKNOWN.setDescription('Unknown')

prop_type_AFFILIATION = tr.createNewPropertyType('AFFILIATION', DataType.CONTROLLEDVOCABULARY)
prop_type_AFFILIATION.setLabel('Affiliation')
prop_type_AFFILIATION.setManagedInternally(False)
prop_type_AFFILIATION.setInternalNamespace(False)

prop_type_AFFILIATION.setVocabulary(vocabulary_AFFILIATION)

prop_type_AGILENT_KIT = tr.createNewPropertyType('AGILENT_KIT', DataType.CONTROLLEDVOCABULARY)
prop_type_AGILENT_KIT.setLabel('Agilent Kit')
prop_type_AGILENT_KIT.setManagedInternally(False)
prop_type_AGILENT_KIT.setInternalNamespace(False)

prop_type_AGILENT_KIT.setVocabulary(vocabulary_AGILENT_KIT)

prop_type_ALIGNMENT_SOFTWARE = tr.createNewPropertyType('ALIGNMENT_SOFTWARE', DataType.CONTROLLEDVOCABULARY)
prop_type_ALIGNMENT_SOFTWARE.setLabel('Alignment software')
prop_type_ALIGNMENT_SOFTWARE.setManagedInternally(False)
prop_type_ALIGNMENT_SOFTWARE.setInternalNamespace(False)

prop_type_ALIGNMENT_SOFTWARE.setVocabulary(vocabulary_ALIGNMENT_SOFTWARE)

prop_type_ANALYSIS_FINISHED = tr.createNewPropertyType('ANALYSIS_FINISHED', DataType.TIMESTAMP)
prop_type_ANALYSIS_FINISHED.setLabel('Analysis finished')
prop_type_ANALYSIS_FINISHED.setManagedInternally(False)
prop_type_ANALYSIS_FINISHED.setInternalNamespace(False)


prop_type_BARCODE = tr.createNewPropertyType('BARCODE', DataType.CONTROLLEDVOCABULARY)
prop_type_BARCODE.setLabel('Index 1')
prop_type_BARCODE.setManagedInternally(False)
prop_type_BARCODE.setInternalNamespace(False)

prop_type_BARCODE.setVocabulary(vocabulary_BARCODES)

prop_type_BARCODES = tr.createNewPropertyType('BARCODES', DataType.MULTILINE_VARCHAR)
prop_type_BARCODES.setLabel('Adaptor and Barcodes')
prop_type_BARCODES.setManagedInternally(False)
prop_type_BARCODES.setInternalNamespace(False)


prop_type_BARCODE_COMPLEXITY_CHECKER = tr.createNewPropertyType('BARCODE_COMPLEXITY_CHECKER', DataType.MULTILINE_VARCHAR)
prop_type_BARCODE_COMPLEXITY_CHECKER.setLabel('Barcode_Complexity_Checker')
prop_type_BARCODE_COMPLEXITY_CHECKER.setManagedInternally(False)
prop_type_BARCODE_COMPLEXITY_CHECKER.setInternalNamespace(False)


prop_type_BARCODE_LENGTH = tr.createNewPropertyType('BARCODE_LENGTH', DataType.INTEGER)
prop_type_BARCODE_LENGTH.setLabel('Barcode Length (+ recognition site)')
prop_type_BARCODE_LENGTH.setManagedInternally(False)
prop_type_BARCODE_LENGTH.setInternalNamespace(False)


prop_type_BAREBACKED = tr.createNewPropertyType('BAREBACKED', DataType.BOOLEAN)
prop_type_BAREBACKED.setLabel('Barebacked?')
prop_type_BAREBACKED.setManagedInternally(False)
prop_type_BAREBACKED.setInternalNamespace(False)


prop_type_BASESCOVERED = tr.createNewPropertyType('BASESCOVERED', DataType.INTEGER)
prop_type_BASESCOVERED.setLabel('bases Covered')
prop_type_BASESCOVERED.setManagedInternally(False)
prop_type_BASESCOVERED.setInternalNamespace(False)


prop_type_BIOLOGICAL_SAMPLE_ARRIVED = tr.createNewPropertyType('BIOLOGICAL_SAMPLE_ARRIVED', DataType.TIMESTAMP)
prop_type_BIOLOGICAL_SAMPLE_ARRIVED.setLabel('Arrival Date of Biological Sample')
prop_type_BIOLOGICAL_SAMPLE_ARRIVED.setManagedInternally(False)
prop_type_BIOLOGICAL_SAMPLE_ARRIVED.setInternalNamespace(False)


prop_type_CALCLUATED_EXAMPLE = tr.createNewPropertyType('CALCLUATED_EXAMPLE', DataType.REAL)
prop_type_CALCLUATED_EXAMPLE.setLabel('Calcluated_example')
prop_type_CALCLUATED_EXAMPLE.setManagedInternally(False)
prop_type_CALCLUATED_EXAMPLE.setInternalNamespace(False)


prop_type_CASAVA_VERSION = tr.createNewPropertyType('CASAVA_VERSION', DataType.CONTROLLEDVOCABULARY)
prop_type_CASAVA_VERSION.setLabel('Casava Version')
prop_type_CASAVA_VERSION.setManagedInternally(False)
prop_type_CASAVA_VERSION.setInternalNamespace(False)

prop_type_CASAVA_VERSION.setVocabulary(vocabulary_CASAVA_VERSION)

prop_type_CELL_PLASTICITY_SYSTEMSX = tr.createNewPropertyType('CELL_PLASTICITY_SYSTEMSX', DataType.BOOLEAN)
prop_type_CELL_PLASTICITY_SYSTEMSX.setLabel('Cell Plasticity (SystemsX)')
prop_type_CELL_PLASTICITY_SYSTEMSX.setManagedInternally(False)
prop_type_CELL_PLASTICITY_SYSTEMSX.setInternalNamespace(False)


prop_type_CHROMCOUNT = tr.createNewPropertyType('CHROMCOUNT', DataType.INTEGER)
prop_type_CHROMCOUNT.setLabel('chrom Count')
prop_type_CHROMCOUNT.setManagedInternally(False)
prop_type_CHROMCOUNT.setInternalNamespace(False)


prop_type_CLUSTER_GENERATION_KIT_VERSION = tr.createNewPropertyType('CLUSTER_GENERATION_KIT_VERSION', DataType.CONTROLLEDVOCABULARY)
prop_type_CLUSTER_GENERATION_KIT_VERSION.setLabel('CS Generation Kit Version')
prop_type_CLUSTER_GENERATION_KIT_VERSION.setManagedInternally(False)
prop_type_CLUSTER_GENERATION_KIT_VERSION.setInternalNamespace(False)

prop_type_CLUSTER_GENERATION_KIT_VERSION.setVocabulary(vocabulary_CLUSTER_GENERATION_KIT_VERSION)

prop_type_CLUSTER_STATION = tr.createNewPropertyType('CLUSTER_STATION', DataType.CONTROLLEDVOCABULARY)
prop_type_CLUSTER_STATION.setLabel('Cluster Station')
prop_type_CLUSTER_STATION.setManagedInternally(False)
prop_type_CLUSTER_STATION.setInternalNamespace(False)

prop_type_CLUSTER_STATION.setVocabulary(vocabulary_CLUSTER_STATION)

prop_type_CLUSTER_STATION_SOFTWARE_VERSION = tr.createNewPropertyType('CLUSTER_STATION_SOFTWARE_VERSION', DataType.CONTROLLEDVOCABULARY)
prop_type_CLUSTER_STATION_SOFTWARE_VERSION.setLabel('CS Software version')
prop_type_CLUSTER_STATION_SOFTWARE_VERSION.setManagedInternally(False)
prop_type_CLUSTER_STATION_SOFTWARE_VERSION.setInternalNamespace(False)

prop_type_CLUSTER_STATION_SOFTWARE_VERSION.setVocabulary(vocabulary_CLUSTER_STATION_SOFTWARE_VERSION)

prop_type_CONCENTRATION_FLOWLANE = tr.createNewPropertyType('CONCENTRATION_FLOWLANE', DataType.REAL)
prop_type_CONCENTRATION_FLOWLANE.setLabel('Concentration in flow lane [pM]')
prop_type_CONCENTRATION_FLOWLANE.setManagedInternally(False)
prop_type_CONCENTRATION_FLOWLANE.setInternalNamespace(False)


prop_type_CONCENTRATION_ORIGINAL_ILLUMINA = tr.createNewPropertyType('CONCENTRATION_ORIGINAL_ILLUMINA', DataType.REAL)
prop_type_CONCENTRATION_ORIGINAL_ILLUMINA.setLabel(u'Concentration (original) [ng/ul]')
prop_type_CONCENTRATION_ORIGINAL_ILLUMINA.setManagedInternally(False)
prop_type_CONCENTRATION_ORIGINAL_ILLUMINA.setInternalNamespace(False)


prop_type_CONCENTRATION_PREPARED_ILLUMINA = tr.createNewPropertyType('CONCENTRATION_PREPARED_ILLUMINA', DataType.REAL)
prop_type_CONCENTRATION_PREPARED_ILLUMINA.setLabel('Concentration (prepared) [ng/ul]')
prop_type_CONCENTRATION_PREPARED_ILLUMINA.setManagedInternally(False)
prop_type_CONCENTRATION_PREPARED_ILLUMINA.setInternalNamespace(False)


prop_type_CONCENTRATION_TOTAL = tr.createNewPropertyType('CONCENTRATION_TOTAL', DataType.INTEGER)
prop_type_CONCENTRATION_TOTAL.setLabel('DNA concentration of library [ng/ul])')
prop_type_CONCENTRATION_TOTAL.setManagedInternally(False)
prop_type_CONCENTRATION_TOTAL.setInternalNamespace(False)


prop_type_CONC_IF_SAMPLE_PROCESSED_DNA_LIBRARY = tr.createNewPropertyType('CONC_IF_SAMPLE_PROCESSED_DNA_LIBRARY', DataType.INTEGER)
prop_type_CONC_IF_SAMPLE_PROCESSED_DNA_LIBRARY.setLabel('Conc. if sample="proc. DNA library" (nM)')
prop_type_CONC_IF_SAMPLE_PROCESSED_DNA_LIBRARY.setManagedInternally(False)
prop_type_CONC_IF_SAMPLE_PROCESSED_DNA_LIBRARY.setInternalNamespace(False)


prop_type_CONTACT_PERSON_EMAIL = tr.createNewPropertyType('CONTACT_PERSON_EMAIL', DataType.VARCHAR)
prop_type_CONTACT_PERSON_EMAIL.setLabel('Email of Contact Person')
prop_type_CONTACT_PERSON_EMAIL.setManagedInternally(False)
prop_type_CONTACT_PERSON_EMAIL.setInternalNamespace(False)


prop_type_CONTACT_PERSON_NAME = tr.createNewPropertyType('CONTACT_PERSON_NAME', DataType.VARCHAR)
prop_type_CONTACT_PERSON_NAME.setLabel('Name of Contact Person')
prop_type_CONTACT_PERSON_NAME.setManagedInternally(False)
prop_type_CONTACT_PERSON_NAME.setInternalNamespace(False)


prop_type_CONTROL_LANE = tr.createNewPropertyType('CONTROL_LANE', DataType.CONTROLLEDVOCABULARY)
prop_type_CONTROL_LANE.setLabel('Control Lane')
prop_type_CONTROL_LANE.setManagedInternally(False)
prop_type_CONTROL_LANE.setInternalNamespace(False)

prop_type_CONTROL_LANE.setVocabulary(vocabulary_CONTROL_LANE)

prop_type_CREATED_ON_CS = tr.createNewPropertyType('CREATED_ON_CS', DataType.TIMESTAMP)
prop_type_CREATED_ON_CS.setLabel('Clustering date')
prop_type_CREATED_ON_CS.setManagedInternally(False)
prop_type_CREATED_ON_CS.setInternalNamespace(False)


prop_type_CS_PROTOCOL_VERSION = tr.createNewPropertyType('CS_PROTOCOL_VERSION', DataType.VARCHAR)
prop_type_CS_PROTOCOL_VERSION.setLabel('CS Protocol Version')
prop_type_CS_PROTOCOL_VERSION.setManagedInternally(False)
prop_type_CS_PROTOCOL_VERSION.setInternalNamespace(False)


prop_type_CYCLES = tr.createNewPropertyType('CYCLES', DataType.INTEGER)
prop_type_CYCLES.setLabel('Number of Cycles')
prop_type_CYCLES.setManagedInternally(False)
prop_type_CYCLES.setInternalNamespace(False)


prop_type_CYCLES_REQUESTED_BY_CUSTOMER = tr.createNewPropertyType('CYCLES_REQUESTED_BY_CUSTOMER', DataType.CONTROLLEDVOCABULARY)
prop_type_CYCLES_REQUESTED_BY_CUSTOMER.setLabel('Cycles requested')
prop_type_CYCLES_REQUESTED_BY_CUSTOMER.setManagedInternally(False)
prop_type_CYCLES_REQUESTED_BY_CUSTOMER.setInternalNamespace(False)

prop_type_CYCLES_REQUESTED_BY_CUSTOMER.setVocabulary(vocabulary_CYCLES)

prop_type_DATA_TRANSFERRED = tr.createNewPropertyType('DATA_TRANSFERRED', DataType.TIMESTAMP)
prop_type_DATA_TRANSFERRED.setLabel('Data transferred')
prop_type_DATA_TRANSFERRED.setManagedInternally(False)
prop_type_DATA_TRANSFERRED.setInternalNamespace(False)


prop_type_DNA_CONCENTRATION_OF_LIBRARY = tr.createNewPropertyType('DNA_CONCENTRATION_OF_LIBRARY', DataType.INTEGER)
prop_type_DNA_CONCENTRATION_OF_LIBRARY.setLabel('DNA concentration of library (nM)')
prop_type_DNA_CONCENTRATION_OF_LIBRARY.setManagedInternally(False)
prop_type_DNA_CONCENTRATION_OF_LIBRARY.setInternalNamespace(False)


prop_type_END_TYPE = tr.createNewPropertyType('END_TYPE', DataType.CONTROLLEDVOCABULARY)
prop_type_END_TYPE.setLabel('Paired End / Single Read')
prop_type_END_TYPE.setManagedInternally(False)
prop_type_END_TYPE.setInternalNamespace(False)

prop_type_END_TYPE.setVocabulary(vocabulary_END_TYPE)

prop_type_EXPERIMENT_DESIGN = tr.createNewPropertyType('EXPERIMENT_DESIGN', DataType.CONTROLLEDVOCABULARY)
prop_type_EXPERIMENT_DESIGN.setLabel('Experiment Design')
prop_type_EXPERIMENT_DESIGN.setManagedInternally(False)
prop_type_EXPERIMENT_DESIGN.setInternalNamespace(False)

prop_type_EXPERIMENT_DESIGN.setVocabulary(vocabulary_EXPERIMENT_DESIGN)

prop_type_EXTERNAL_SAMPLE_NAME = tr.createNewPropertyType('EXTERNAL_SAMPLE_NAME', DataType.VARCHAR)
prop_type_EXTERNAL_SAMPLE_NAME.setLabel('External Sample Name')
prop_type_EXTERNAL_SAMPLE_NAME.setManagedInternally(False)
prop_type_EXTERNAL_SAMPLE_NAME.setInternalNamespace(False)


prop_type_FLOWCELLTYPE = tr.createNewPropertyType('FLOWCELLTYPE', DataType.VARCHAR)
prop_type_FLOWCELLTYPE.setLabel('Flow Cell Type')
prop_type_FLOWCELLTYPE.setManagedInternally(False)
prop_type_FLOWCELLTYPE.setInternalNamespace(False)


prop_type_FLOW_CELL_SEQUENCED_ON = tr.createNewPropertyType('FLOW_CELL_SEQUENCED_ON', DataType.TIMESTAMP)
prop_type_FLOW_CELL_SEQUENCED_ON.setLabel('Sequencing date')
prop_type_FLOW_CELL_SEQUENCED_ON.setManagedInternally(False)
prop_type_FLOW_CELL_SEQUENCED_ON.setInternalNamespace(False)


prop_type_FRAGMENT_SIZE_PREPARED_ILLUMINA = tr.createNewPropertyType('FRAGMENT_SIZE_PREPARED_ILLUMINA', DataType.INTEGER)
prop_type_FRAGMENT_SIZE_PREPARED_ILLUMINA.setLabel('Fragment Size (prepared) [base (pairs)]')
prop_type_FRAGMENT_SIZE_PREPARED_ILLUMINA.setManagedInternally(False)
prop_type_FRAGMENT_SIZE_PREPARED_ILLUMINA.setInternalNamespace(False)


prop_type_ILLUMINA_PIPELINE_VERSION = tr.createNewPropertyType('ILLUMINA_PIPELINE_VERSION', DataType.CONTROLLEDVOCABULARY)
prop_type_ILLUMINA_PIPELINE_VERSION.setLabel('Pipeline Version')
prop_type_ILLUMINA_PIPELINE_VERSION.setManagedInternally(False)
prop_type_ILLUMINA_PIPELINE_VERSION.setInternalNamespace(False)

prop_type_ILLUMINA_PIPELINE_VERSION.setVocabulary(vocabulary_PIPELINE_VERSION)

prop_type_INDEX2 = tr.createNewPropertyType('INDEX2', DataType.CONTROLLEDVOCABULARY)
prop_type_INDEX2.setLabel('Index 2')
prop_type_INDEX2.setManagedInternally(False)
prop_type_INDEX2.setInternalNamespace(False)

prop_type_INDEX2.setVocabulary(vocabulary_INDEX2)

prop_type_INDEXREAD = tr.createNewPropertyType('INDEXREAD', DataType.INTEGER)
prop_type_INDEXREAD.setLabel('Index Read')
prop_type_INDEXREAD.setManagedInternally(False)
prop_type_INDEXREAD.setInternalNamespace(False)


prop_type_INVOICE = tr.createNewPropertyType('INVOICE', DataType.BOOLEAN)
prop_type_INVOICE.setLabel('Invoice sent?')
prop_type_INVOICE.setManagedInternally(False)
prop_type_INVOICE.setInternalNamespace(False)


prop_type_ISCOMPRESSED = tr.createNewPropertyType('ISCOMPRESSED', DataType.BOOLEAN)
prop_type_ISCOMPRESSED.setLabel('Is Compressed')
prop_type_ISCOMPRESSED.setManagedInternally(False)
prop_type_ISCOMPRESSED.setInternalNamespace(False)


prop_type_ISSUED_COMMAND = tr.createNewPropertyType('ISSUED_COMMAND', DataType.MULTILINE_VARCHAR)
prop_type_ISSUED_COMMAND.setLabel('Issued Command')
prop_type_ISSUED_COMMAND.setManagedInternally(False)
prop_type_ISSUED_COMMAND.setInternalNamespace(False)


prop_type_ISSWAPPED = tr.createNewPropertyType('ISSWAPPED', DataType.INTEGER)
prop_type_ISSWAPPED.setLabel('Is byte swapped')
prop_type_ISSWAPPED.setManagedInternally(False)
prop_type_ISSWAPPED.setInternalNamespace(False)


prop_type_KIT = tr.createNewPropertyType('KIT', DataType.CONTROLLEDVOCABULARY)
prop_type_KIT.setLabel('Library preparation kit')
prop_type_KIT.setManagedInternally(False)
prop_type_KIT.setInternalNamespace(False)

prop_type_KIT.setVocabulary(vocabulary_KIT)

prop_type_KIT_ARRIVED = tr.createNewPropertyType('KIT_ARRIVED', DataType.TIMESTAMP)
prop_type_KIT_ARRIVED.setLabel('Kit arrived')
prop_type_KIT_ARRIVED.setManagedInternally(False)
prop_type_KIT_ARRIVED.setInternalNamespace(False)


prop_type_KIT_PREPARED = tr.createNewPropertyType('KIT_PREPARED', DataType.TIMESTAMP)
prop_type_KIT_PREPARED.setLabel('Sample processed')
prop_type_KIT_PREPARED.setManagedInternally(False)
prop_type_KIT_PREPARED.setInternalNamespace(False)


prop_type_LANECOUNT = tr.createNewPropertyType('LANECOUNT', DataType.INTEGER)
prop_type_LANECOUNT.setLabel('Lane Count')
prop_type_LANECOUNT.setManagedInternally(False)
prop_type_LANECOUNT.setInternalNamespace(False)


prop_type_LIBRARY_PROCESSING_FAILED = tr.createNewPropertyType('LIBRARY_PROCESSING_FAILED', DataType.BOOLEAN)
prop_type_LIBRARY_PROCESSING_FAILED.setLabel('Library processing failed')
prop_type_LIBRARY_PROCESSING_FAILED.setManagedInternally(False)
prop_type_LIBRARY_PROCESSING_FAILED.setInternalNamespace(False)


prop_type_LIBRARY_PROCESSING_POSSIBLE = tr.createNewPropertyType('LIBRARY_PROCESSING_POSSIBLE', DataType.BOOLEAN)
prop_type_LIBRARY_PROCESSING_POSSIBLE.setLabel('Library Processing possible')
prop_type_LIBRARY_PROCESSING_POSSIBLE.setManagedInternally(False)
prop_type_LIBRARY_PROCESSING_POSSIBLE.setInternalNamespace(False)


prop_type_LIBRARY_PROCESSING_SUCCESSFUL = tr.createNewPropertyType('LIBRARY_PROCESSING_SUCCESSFUL', DataType.BOOLEAN)
prop_type_LIBRARY_PROCESSING_SUCCESSFUL.setLabel('Library processing successful')
prop_type_LIBRARY_PROCESSING_SUCCESSFUL.setManagedInternally(False)
prop_type_LIBRARY_PROCESSING_SUCCESSFUL.setInternalNamespace(False)


prop_type_LOT = tr.createNewPropertyType('LOT', DataType.INTEGER)
prop_type_LOT.setLabel('Kit Lot #')
prop_type_LOT.setManagedInternally(False)
prop_type_LOT.setInternalNamespace(False)


prop_type_MACS_VERSION = tr.createNewPropertyType('MACS_VERSION', DataType.CONTROLLEDVOCABULARY)
prop_type_MACS_VERSION.setLabel('MACS VERSION')
prop_type_MACS_VERSION.setManagedInternally(False)
prop_type_MACS_VERSION.setInternalNamespace(False)

prop_type_MACS_VERSION.setVocabulary(vocabulary_MACS_VERSION)

prop_type_MAPPED_READS = tr.createNewPropertyType('MAPPED_READS', DataType.INTEGER)
prop_type_MAPPED_READS.setLabel('Mapped reads')
prop_type_MAPPED_READS.setManagedInternally(False)
prop_type_MAPPED_READS.setInternalNamespace(False)


prop_type_MAX = tr.createNewPropertyType('MAX', DataType.REAL)
prop_type_MAX.setLabel('Maximum')
prop_type_MAX.setManagedInternally(False)
prop_type_MAX.setInternalNamespace(False)


prop_type_MEAN = tr.createNewPropertyType('MEAN', DataType.REAL)
prop_type_MEAN.setLabel('Mean')
prop_type_MEAN.setManagedInternally(False)
prop_type_MEAN.setInternalNamespace(False)


prop_type_MIN = tr.createNewPropertyType('MIN', DataType.REAL)
prop_type_MIN.setLabel('Minimum')
prop_type_MIN.setManagedInternally(False)
prop_type_MIN.setInternalNamespace(False)


prop_type_MISMATCH_IN_INDEX = tr.createNewPropertyType('MISMATCH_IN_INDEX', DataType.CONTROLLEDVOCABULARY)
prop_type_MISMATCH_IN_INDEX.setLabel('Mismatch in Index')
prop_type_MISMATCH_IN_INDEX.setManagedInternally(False)
prop_type_MISMATCH_IN_INDEX.setInternalNamespace(False)

prop_type_MISMATCH_IN_INDEX.setVocabulary(vocabulary_MISMATCH_IN_INDEX)

prop_type_NANO_DROP = tr.createNewPropertyType('NANO_DROP', DataType.CONTROLLEDVOCABULARY)
prop_type_NANO_DROP.setLabel('Nano Drop')
prop_type_NANO_DROP.setManagedInternally(False)
prop_type_NANO_DROP.setInternalNamespace(False)

prop_type_NANO_DROP.setVocabulary(vocabulary_NANO_DROP)

prop_type_NCBI_ORGANISM_TAXONOMY = tr.createNewPropertyType('NCBI_ORGANISM_TAXONOMY', DataType.CONTROLLEDVOCABULARY)
prop_type_NCBI_ORGANISM_TAXONOMY.setLabel('Organism (NCBI Taxonomy)')
prop_type_NCBI_ORGANISM_TAXONOMY.setManagedInternally(False)
prop_type_NCBI_ORGANISM_TAXONOMY.setInternalNamespace(False)

prop_type_NCBI_ORGANISM_TAXONOMY.setVocabulary(vocabulary_NCBI_TAXONOMY)

prop_type_NM_DNA = tr.createNewPropertyType('NM_DNA', DataType.REAL)
prop_type_NM_DNA.setLabel('Calculated DNA concentration of library (nM)')
prop_type_NM_DNA.setManagedInternally(False)
prop_type_NM_DNA.setInternalNamespace(False)


prop_type_NOTES = tr.createNewPropertyType('NOTES', DataType.MULTILINE_VARCHAR)
prop_type_NOTES.setLabel('Notes')
prop_type_NOTES.setManagedInternally(False)
prop_type_NOTES.setInternalNamespace(False)


prop_type_NOTES_CUSTOMER = tr.createNewPropertyType('NOTES_CUSTOMER', DataType.MULTILINE_VARCHAR)
prop_type_NOTES_CUSTOMER.setLabel('Notes Customer')
prop_type_NOTES_CUSTOMER.setManagedInternally(False)
prop_type_NOTES_CUSTOMER.setInternalNamespace(False)


prop_type_NUMBER_OF_ATTACHMENTS = tr.createNewPropertyType('NUMBER_OF_ATTACHMENTS', DataType.INTEGER)
prop_type_NUMBER_OF_ATTACHMENTS.setLabel('Number of Attachments')
prop_type_NUMBER_OF_ATTACHMENTS.setManagedInternally(False)
prop_type_NUMBER_OF_ATTACHMENTS.setInternalNamespace(False)


prop_type_ORGANISM_FREE = tr.createNewPropertyType('ORGANISM_FREE', DataType.VARCHAR)
prop_type_ORGANISM_FREE.setLabel('Organism (if OTHER)')
prop_type_ORGANISM_FREE.setManagedInternally(False)
prop_type_ORGANISM_FREE.setInternalNamespace(False)


prop_type_OTRS = tr.createNewPropertyType('OTRS', DataType.INTEGER)
prop_type_OTRS.setLabel('OTRS ID #')
prop_type_OTRS.setManagedInternally(False)
prop_type_OTRS.setInternalNamespace(False)


prop_type_PAIRED_END_KIT = tr.createNewPropertyType('PAIRED_END_KIT', DataType.VARCHAR)
prop_type_PAIRED_END_KIT.setLabel('Paired End Kit')
prop_type_PAIRED_END_KIT.setManagedInternally(False)
prop_type_PAIRED_END_KIT.setInternalNamespace(False)


prop_type_PERCENTAGE_ONE_MISMATCH_READS_INDEX = tr.createNewPropertyType('PERCENTAGE_ONE_MISMATCH_READS_INDEX', DataType.REAL)
prop_type_PERCENTAGE_ONE_MISMATCH_READS_INDEX.setLabel('% One Mismatch Reads (Index)')
prop_type_PERCENTAGE_ONE_MISMATCH_READS_INDEX.setManagedInternally(False)
prop_type_PERCENTAGE_ONE_MISMATCH_READS_INDEX.setInternalNamespace(False)


prop_type_PERCENTAGE_PASSED_FILTERING = tr.createNewPropertyType('PERCENTAGE_PASSED_FILTERING', DataType.REAL)
prop_type_PERCENTAGE_PASSED_FILTERING.setLabel('% Passes Illumina Filtering (PF)')
prop_type_PERCENTAGE_PASSED_FILTERING.setManagedInternally(False)
prop_type_PERCENTAGE_PASSED_FILTERING.setInternalNamespace(False)


prop_type_PERCENTAGE_PERFECT_INDEX_READS = tr.createNewPropertyType('PERCENTAGE_PERFECT_INDEX_READS', DataType.REAL)
prop_type_PERCENTAGE_PERFECT_INDEX_READS.setLabel('% Perfect Index Reads')
prop_type_PERCENTAGE_PERFECT_INDEX_READS.setManagedInternally(False)
prop_type_PERCENTAGE_PERFECT_INDEX_READS.setInternalNamespace(False)


prop_type_PERCENTAGE_RAW_CLUSTERS_PER_LANE = tr.createNewPropertyType('PERCENTAGE_RAW_CLUSTERS_PER_LANE', DataType.REAL)
prop_type_PERCENTAGE_RAW_CLUSTERS_PER_LANE.setLabel('% of raw clusters per lane')
prop_type_PERCENTAGE_RAW_CLUSTERS_PER_LANE.setManagedInternally(False)
prop_type_PERCENTAGE_RAW_CLUSTERS_PER_LANE.setInternalNamespace(False)


prop_type_PREPARED_BY = tr.createNewPropertyType('PREPARED_BY', DataType.VARCHAR)
prop_type_PREPARED_BY.setLabel('Prepared by')
prop_type_PREPARED_BY.setManagedInternally(False)
prop_type_PREPARED_BY.setInternalNamespace(False)


prop_type_PRIMARYDATASIZE = tr.createNewPropertyType('PRIMARYDATASIZE', DataType.INTEGER)
prop_type_PRIMARYDATASIZE.setLabel('primary Data Size')
prop_type_PRIMARYDATASIZE.setManagedInternally(False)
prop_type_PRIMARYDATASIZE.setInternalNamespace(False)


prop_type_PRIMARYINDEXSIZE = tr.createNewPropertyType('PRIMARYINDEXSIZE', DataType.INTEGER)
prop_type_PRIMARYINDEXSIZE.setLabel('primary Index Size')
prop_type_PRIMARYINDEXSIZE.setManagedInternally(False)
prop_type_PRIMARYINDEXSIZE.setInternalNamespace(False)


prop_type_PRINCIPAL_INVESTIGATOR_EMAIL = tr.createNewPropertyType('PRINCIPAL_INVESTIGATOR_EMAIL', DataType.VARCHAR)
prop_type_PRINCIPAL_INVESTIGATOR_EMAIL.setLabel('Email of Principal Investigator')
prop_type_PRINCIPAL_INVESTIGATOR_EMAIL.setManagedInternally(False)
prop_type_PRINCIPAL_INVESTIGATOR_EMAIL.setInternalNamespace(False)


prop_type_PRINCIPAL_INVESTIGATOR_NAME = tr.createNewPropertyType('PRINCIPAL_INVESTIGATOR_NAME', DataType.VARCHAR)
prop_type_PRINCIPAL_INVESTIGATOR_NAME.setLabel('Name of Principal Investigator')
prop_type_PRINCIPAL_INVESTIGATOR_NAME.setManagedInternally(False)
prop_type_PRINCIPAL_INVESTIGATOR_NAME.setInternalNamespace(False)


prop_type_REQUIRED_LANES = tr.createNewPropertyType('REQUIRED_LANES', DataType.CONTROLLEDVOCABULARY)
prop_type_REQUIRED_LANES.setLabel('Number of Required Lanes')
prop_type_REQUIRED_LANES.setManagedInternally(False)
prop_type_REQUIRED_LANES.setInternalNamespace(False)

prop_type_REQUIRED_LANES.setVocabulary(vocabulary_REQUIRED_LANES)

prop_type_SAMPLE_CHARACTERISTICS = tr.createNewPropertyType('SAMPLE_CHARACTERISTICS', DataType.MULTILINE_VARCHAR)
prop_type_SAMPLE_CHARACTERISTICS.setLabel('Sample characteristics')
prop_type_SAMPLE_CHARACTERISTICS.setManagedInternally(False)
prop_type_SAMPLE_CHARACTERISTICS.setInternalNamespace(False)


prop_type_SAMPLE_DATA_PROCESSING = tr.createNewPropertyType('SAMPLE_DATA_PROCESSING', DataType.MULTILINE_VARCHAR)
prop_type_SAMPLE_DATA_PROCESSING.setLabel('Sample Data Processing')
prop_type_SAMPLE_DATA_PROCESSING.setManagedInternally(False)
prop_type_SAMPLE_DATA_PROCESSING.setInternalNamespace(False)


prop_type_SAMPLE_EXTRACT_PROTOCOL = tr.createNewPropertyType('SAMPLE_EXTRACT_PROTOCOL', DataType.MULTILINE_VARCHAR)
prop_type_SAMPLE_EXTRACT_PROTOCOL.setLabel('Sample Extract Protocol')
prop_type_SAMPLE_EXTRACT_PROTOCOL.setManagedInternally(False)
prop_type_SAMPLE_EXTRACT_PROTOCOL.setInternalNamespace(False)


prop_type_SAMPLE_ID = tr.createNewPropertyType('SAMPLE_ID', DataType.VARCHAR)
prop_type_SAMPLE_ID.setLabel('Sample ID')
prop_type_SAMPLE_ID.setManagedInternally(False)
prop_type_SAMPLE_ID.setInternalNamespace(False)


prop_type_SAMPLE_KIND = tr.createNewPropertyType('SAMPLE_KIND', DataType.CONTROLLEDVOCABULARY)
prop_type_SAMPLE_KIND.setLabel('Sample Kind')
prop_type_SAMPLE_KIND.setManagedInternally(False)
prop_type_SAMPLE_KIND.setInternalNamespace(False)

prop_type_SAMPLE_KIND.setVocabulary(vocabulary_SAMPLE_TYPE)

prop_type_SAMPLE_LIBRARY_SELECTION = tr.createNewPropertyType('SAMPLE_LIBRARY_SELECTION', DataType.CONTROLLEDVOCABULARY)
prop_type_SAMPLE_LIBRARY_SELECTION.setLabel('Sample Library Selection')
prop_type_SAMPLE_LIBRARY_SELECTION.setManagedInternally(False)
prop_type_SAMPLE_LIBRARY_SELECTION.setInternalNamespace(False)

prop_type_SAMPLE_LIBRARY_SELECTION.setVocabulary(vocabulary_SAMPLE_LIBRARY_SELECTION)

prop_type_SAMPLE_LIBRARY_SOURCE = tr.createNewPropertyType('SAMPLE_LIBRARY_SOURCE', DataType.CONTROLLEDVOCABULARY)
prop_type_SAMPLE_LIBRARY_SOURCE.setLabel('Sample Library Source')
prop_type_SAMPLE_LIBRARY_SOURCE.setManagedInternally(False)
prop_type_SAMPLE_LIBRARY_SOURCE.setInternalNamespace(False)

prop_type_SAMPLE_LIBRARY_SOURCE.setVocabulary(vocabulary_SAMPLE_LIBRARY_SOURCE_VOC)

prop_type_SAMPLE_LIBRARY_STRATEGY = tr.createNewPropertyType('SAMPLE_LIBRARY_STRATEGY', DataType.CONTROLLEDVOCABULARY)
prop_type_SAMPLE_LIBRARY_STRATEGY.setLabel('Sample Library Strategy')
prop_type_SAMPLE_LIBRARY_STRATEGY.setManagedInternally(False)
prop_type_SAMPLE_LIBRARY_STRATEGY.setInternalNamespace(False)

prop_type_SAMPLE_LIBRARY_STRATEGY.setVocabulary(vocabulary_SAMPLE_LIBRARY_STRATEGY)

prop_type_SAMPLE_MOLECULE = tr.createNewPropertyType('SAMPLE_MOLECULE', DataType.CONTROLLEDVOCABULARY)
prop_type_SAMPLE_MOLECULE.setLabel('Sample molecule')
prop_type_SAMPLE_MOLECULE.setManagedInternally(False)
prop_type_SAMPLE_MOLECULE.setInternalNamespace(False)

prop_type_SAMPLE_MOLECULE.setVocabulary(vocabulary_SAMPLE_MOLECULE)

prop_type_SAMPLE_SOURCE_NAME = tr.createNewPropertyType('SAMPLE_SOURCE_NAME', DataType.VARCHAR)
prop_type_SAMPLE_SOURCE_NAME.setLabel('Sample Source Name')
prop_type_SAMPLE_SOURCE_NAME.setManagedInternally(False)
prop_type_SAMPLE_SOURCE_NAME.setInternalNamespace(False)


prop_type_SAMTOOLS_FLAGSTAT = tr.createNewPropertyType('SAMTOOLS_FLAGSTAT', DataType.MULTILINE_VARCHAR)
prop_type_SAMTOOLS_FLAGSTAT.setLabel('Samtools Flagstat Output')
prop_type_SAMTOOLS_FLAGSTAT.setManagedInternally(False)
prop_type_SAMTOOLS_FLAGSTAT.setInternalNamespace(False)


prop_type_SBS_KIT = tr.createNewPropertyType('SBS_KIT', DataType.VARCHAR)
prop_type_SBS_KIT.setLabel('SBS Kit')
prop_type_SBS_KIT.setManagedInternally(False)
prop_type_SBS_KIT.setInternalNamespace(False)


prop_type_SBS_SEQUENCING_KIT_VERSION = tr.createNewPropertyType('SBS_SEQUENCING_KIT_VERSION', DataType.CONTROLLEDVOCABULARY)
prop_type_SBS_SEQUENCING_KIT_VERSION.setLabel('SBS Sequencing Kit Version')
prop_type_SBS_SEQUENCING_KIT_VERSION.setManagedInternally(False)
prop_type_SBS_SEQUENCING_KIT_VERSION.setInternalNamespace(False)

prop_type_SBS_SEQUENCING_KIT_VERSION.setVocabulary(vocabulary_SBS_SEQUENCING_KIT_VERSION)

prop_type_SCRIPT_TYPE = tr.createNewPropertyType('SCRIPT_TYPE', DataType.REAL)
prop_type_SCRIPT_TYPE.setLabel('Script Type')
prop_type_SCRIPT_TYPE.setManagedInternally(False)
prop_type_SCRIPT_TYPE.setInternalNamespace(False)


prop_type_SCS_PROTOCOL_VERSION = tr.createNewPropertyType('SCS_PROTOCOL_VERSION', DataType.CONTROLLEDVOCABULARY)
prop_type_SCS_PROTOCOL_VERSION.setLabel('SCS Protocol Version')
prop_type_SCS_PROTOCOL_VERSION.setManagedInternally(False)
prop_type_SCS_PROTOCOL_VERSION.setInternalNamespace(False)

prop_type_SCS_PROTOCOL_VERSION.setVocabulary(vocabulary_SCS_PROTOCOL_VERSION)

prop_type_SCS_SOFTWARE_VERSION = tr.createNewPropertyType('SCS_SOFTWARE_VERSION', DataType.CONTROLLEDVOCABULARY)
prop_type_SCS_SOFTWARE_VERSION.setLabel('SCS Software Version')
prop_type_SCS_SOFTWARE_VERSION.setManagedInternally(False)
prop_type_SCS_SOFTWARE_VERSION.setInternalNamespace(False)

prop_type_SCS_SOFTWARE_VERSION.setVocabulary(vocabulary_SCS_SOFTWARE_VERSION)

prop_type_SEQUENCER = tr.createNewPropertyType('SEQUENCER', DataType.CONTROLLEDVOCABULARY)
prop_type_SEQUENCER.setLabel('Sequencer')
prop_type_SEQUENCER.setManagedInternally(False)
prop_type_SEQUENCER.setInternalNamespace(False)

prop_type_SEQUENCER.setVocabulary(vocabulary_SEQUENCER)

prop_type_SEQUENCER_FINISHED = tr.createNewPropertyType('SEQUENCER_FINISHED', DataType.TIMESTAMP)
prop_type_SEQUENCER_FINISHED.setLabel('Sequencer finished')
prop_type_SEQUENCER_FINISHED.setManagedInternally(False)
prop_type_SEQUENCER_FINISHED.setInternalNamespace(False)


prop_type_SEQUENCING_APPLICATION = tr.createNewPropertyType('SEQUENCING_APPLICATION', DataType.CONTROLLEDVOCABULARY)
prop_type_SEQUENCING_APPLICATION.setLabel('Sequencing Application')
prop_type_SEQUENCING_APPLICATION.setManagedInternally(False)
prop_type_SEQUENCING_APPLICATION.setInternalNamespace(False)

prop_type_SEQUENCING_APPLICATION.setVocabulary(vocabulary_SEQUENCING_APPLICATION)

prop_type_STARTING_AMOUNT_OF_SAMPLE_IN_NG = tr.createNewPropertyType('STARTING_AMOUNT_OF_SAMPLE_IN_NG', DataType.REAL)
prop_type_STARTING_AMOUNT_OF_SAMPLE_IN_NG.setLabel('Starting amount of sample (ng)')
prop_type_STARTING_AMOUNT_OF_SAMPLE_IN_NG.setManagedInternally(False)
prop_type_STARTING_AMOUNT_OF_SAMPLE_IN_NG.setInternalNamespace(False)


prop_type_STD = tr.createNewPropertyType('STD', DataType.REAL)
prop_type_STD.setLabel('Standard deviation')
prop_type_STD.setManagedInternally(False)
prop_type_STD.setInternalNamespace(False)


prop_type_SURFACECOUNT = tr.createNewPropertyType('SURFACECOUNT', DataType.INTEGER)
prop_type_SURFACECOUNT.setLabel('Surface Count')
prop_type_SURFACECOUNT.setManagedInternally(False)
prop_type_SURFACECOUNT.setInternalNamespace(False)


prop_type_SWATHCOUNT = tr.createNewPropertyType('SWATHCOUNT', DataType.INTEGER)
prop_type_SWATHCOUNT.setLabel('Swath Count')
prop_type_SWATHCOUNT.setManagedInternally(False)
prop_type_SWATHCOUNT.setInternalNamespace(False)


prop_type_TILECOUNT = tr.createNewPropertyType('TILECOUNT', DataType.INTEGER)
prop_type_TILECOUNT.setLabel('Tile Count')
prop_type_TILECOUNT.setManagedInternally(False)
prop_type_TILECOUNT.setInternalNamespace(False)


prop_type_TOTAL_READS = tr.createNewPropertyType('TOTAL_READS', DataType.INTEGER)
prop_type_TOTAL_READS.setLabel('Total reads')
prop_type_TOTAL_READS.setManagedInternally(False)
prop_type_TOTAL_READS.setInternalNamespace(False)


prop_type_TRANSFER_METHOD = tr.createNewPropertyType('TRANSFER_METHOD', DataType.CONTROLLEDVOCABULARY)
prop_type_TRANSFER_METHOD.setLabel('Data transfer method')
prop_type_TRANSFER_METHOD.setManagedInternally(False)
prop_type_TRANSFER_METHOD.setInternalNamespace(False)

prop_type_TRANSFER_METHOD.setVocabulary(vocabulary_TRANSFER_METHOD)

prop_type_UL_DNA = tr.createNewPropertyType('UL_DNA', DataType.REAL)
prop_type_UL_DNA.setLabel('Calculated ul DNA for 2nM stock')
prop_type_UL_DNA.setManagedInternally(False)
prop_type_UL_DNA.setInternalNamespace(False)


prop_type_UL_EB = tr.createNewPropertyType('UL_EB', DataType.REAL)
prop_type_UL_EB.setLabel('Calculated ul EB for 2nM stock ')
prop_type_UL_EB.setManagedInternally(False)
prop_type_UL_EB.setInternalNamespace(False)


prop_type_UL_STOCK = tr.createNewPropertyType('UL_STOCK', DataType.INTEGER)
prop_type_UL_STOCK.setLabel('ul of 2nM stock')
prop_type_UL_STOCK.setManagedInternally(False)
prop_type_UL_STOCK.setInternalNamespace(False)


prop_type_VERSION = tr.createNewPropertyType('VERSION', DataType.VARCHAR)
prop_type_VERSION.setLabel('Version')
prop_type_VERSION.setManagedInternally(False)
prop_type_VERSION.setInternalNamespace(False)


prop_type_YIELD_MBASES = tr.createNewPropertyType('YIELD_MBASES', DataType.INTEGER)
prop_type_YIELD_MBASES.setLabel('Yield(Mbases)')
prop_type_YIELD_MBASES.setManagedInternally(False)
prop_type_YIELD_MBASES.setInternalNamespace(False)


prop_type_ZOOMLEVELS = tr.createNewPropertyType('ZOOMLEVELS', DataType.INTEGER)
prop_type_ZOOMLEVELS.setLabel('zoom Levels')
prop_type_ZOOMLEVELS.setManagedInternally(False)
prop_type_ZOOMLEVELS.setInternalNamespace(False)


assignment_DATA_SET_ALIGNMENT_ALIGNMENT_SOFTWARE = tr.assignPropertyType(data_set_type_ALIGNMENT, prop_type_ALIGNMENT_SOFTWARE)
assignment_DATA_SET_ALIGNMENT_ALIGNMENT_SOFTWARE.setMandatory(False)
assignment_DATA_SET_ALIGNMENT_ALIGNMENT_SOFTWARE.setSection(None)
assignment_DATA_SET_ALIGNMENT_ALIGNMENT_SOFTWARE.setPositionInForms(1)

assignment_DATA_SET_ALIGNMENT_VERSION = tr.assignPropertyType(data_set_type_ALIGNMENT, prop_type_VERSION)
assignment_DATA_SET_ALIGNMENT_VERSION.setMandatory(False)
assignment_DATA_SET_ALIGNMENT_VERSION.setSection(None)
assignment_DATA_SET_ALIGNMENT_VERSION.setPositionInForms(2)

assignment_DATA_SET_ALIGNMENT_NOTES = tr.assignPropertyType(data_set_type_ALIGNMENT, prop_type_NOTES)
assignment_DATA_SET_ALIGNMENT_NOTES.setMandatory(False)
assignment_DATA_SET_ALIGNMENT_NOTES.setSection(None)
assignment_DATA_SET_ALIGNMENT_NOTES.setPositionInForms(3)

assignment_DATA_SET_ALIGNMENT_SAMTOOLS_FLAGSTAT = tr.assignPropertyType(data_set_type_ALIGNMENT, prop_type_SAMTOOLS_FLAGSTAT)
assignment_DATA_SET_ALIGNMENT_SAMTOOLS_FLAGSTAT.setMandatory(False)
assignment_DATA_SET_ALIGNMENT_SAMTOOLS_FLAGSTAT.setSection(None)
assignment_DATA_SET_ALIGNMENT_SAMTOOLS_FLAGSTAT.setPositionInForms(4)

assignment_DATA_SET_ALIGNMENT_MAPPED_READS = tr.assignPropertyType(data_set_type_ALIGNMENT, prop_type_MAPPED_READS)
assignment_DATA_SET_ALIGNMENT_MAPPED_READS.setMandatory(False)
assignment_DATA_SET_ALIGNMENT_MAPPED_READS.setSection(None)
assignment_DATA_SET_ALIGNMENT_MAPPED_READS.setPositionInForms(5)

assignment_DATA_SET_ALIGNMENT_TOTAL_READS = tr.assignPropertyType(data_set_type_ALIGNMENT, prop_type_TOTAL_READS)
assignment_DATA_SET_ALIGNMENT_TOTAL_READS.setMandatory(False)
assignment_DATA_SET_ALIGNMENT_TOTAL_READS.setSection(None)
assignment_DATA_SET_ALIGNMENT_TOTAL_READS.setPositionInForms(6)

assignment_DATA_SET_ALIGNMENT_ISSUED_COMMAND = tr.assignPropertyType(data_set_type_ALIGNMENT, prop_type_ISSUED_COMMAND)
assignment_DATA_SET_ALIGNMENT_ISSUED_COMMAND.setMandatory(False)
assignment_DATA_SET_ALIGNMENT_ISSUED_COMMAND.setSection(None)
assignment_DATA_SET_ALIGNMENT_ISSUED_COMMAND.setPositionInForms(7)

assignment_DATA_SET_BASECALL_STATS_MISMATCH_IN_INDEX = tr.assignPropertyType(data_set_type_BASECALL_STATS, prop_type_MISMATCH_IN_INDEX)
assignment_DATA_SET_BASECALL_STATS_MISMATCH_IN_INDEX.setMandatory(False)
assignment_DATA_SET_BASECALL_STATS_MISMATCH_IN_INDEX.setSection(None)
assignment_DATA_SET_BASECALL_STATS_MISMATCH_IN_INDEX.setPositionInForms(1)

assignment_DATA_SET_BIGWIGGLE_NOTES = tr.assignPropertyType(data_set_type_BIGWIGGLE, prop_type_NOTES)
assignment_DATA_SET_BIGWIGGLE_NOTES.setMandatory(False)
assignment_DATA_SET_BIGWIGGLE_NOTES.setSection(None)
assignment_DATA_SET_BIGWIGGLE_NOTES.setPositionInForms(1)

assignment_DATA_SET_BIGWIGGLE_VERSION = tr.assignPropertyType(data_set_type_BIGWIGGLE, prop_type_VERSION)
assignment_DATA_SET_BIGWIGGLE_VERSION.setMandatory(False)
assignment_DATA_SET_BIGWIGGLE_VERSION.setSection(None)
assignment_DATA_SET_BIGWIGGLE_VERSION.setPositionInForms(2)

assignment_DATA_SET_BIGWIGGLE_ISCOMPRESSED = tr.assignPropertyType(data_set_type_BIGWIGGLE, prop_type_ISCOMPRESSED)
assignment_DATA_SET_BIGWIGGLE_ISCOMPRESSED.setMandatory(False)
assignment_DATA_SET_BIGWIGGLE_ISCOMPRESSED.setSection(None)
assignment_DATA_SET_BIGWIGGLE_ISCOMPRESSED.setPositionInForms(3)

assignment_DATA_SET_BIGWIGGLE_ISSWAPPED = tr.assignPropertyType(data_set_type_BIGWIGGLE, prop_type_ISSWAPPED)
assignment_DATA_SET_BIGWIGGLE_ISSWAPPED.setMandatory(False)
assignment_DATA_SET_BIGWIGGLE_ISSWAPPED.setSection(None)
assignment_DATA_SET_BIGWIGGLE_ISSWAPPED.setPositionInForms(4)

assignment_DATA_SET_BIGWIGGLE_PRIMARYDATASIZE = tr.assignPropertyType(data_set_type_BIGWIGGLE, prop_type_PRIMARYDATASIZE)
assignment_DATA_SET_BIGWIGGLE_PRIMARYDATASIZE.setMandatory(False)
assignment_DATA_SET_BIGWIGGLE_PRIMARYDATASIZE.setSection(None)
assignment_DATA_SET_BIGWIGGLE_PRIMARYDATASIZE.setPositionInForms(5)

assignment_DATA_SET_BIGWIGGLE_PRIMARYINDEXSIZE = tr.assignPropertyType(data_set_type_BIGWIGGLE, prop_type_PRIMARYINDEXSIZE)
assignment_DATA_SET_BIGWIGGLE_PRIMARYINDEXSIZE.setMandatory(False)
assignment_DATA_SET_BIGWIGGLE_PRIMARYINDEXSIZE.setSection(None)
assignment_DATA_SET_BIGWIGGLE_PRIMARYINDEXSIZE.setPositionInForms(6)

assignment_DATA_SET_BIGWIGGLE_ZOOMLEVELS = tr.assignPropertyType(data_set_type_BIGWIGGLE, prop_type_ZOOMLEVELS)
assignment_DATA_SET_BIGWIGGLE_ZOOMLEVELS.setMandatory(False)
assignment_DATA_SET_BIGWIGGLE_ZOOMLEVELS.setSection(None)
assignment_DATA_SET_BIGWIGGLE_ZOOMLEVELS.setPositionInForms(7)

assignment_DATA_SET_BIGWIGGLE_CHROMCOUNT = tr.assignPropertyType(data_set_type_BIGWIGGLE, prop_type_CHROMCOUNT)
assignment_DATA_SET_BIGWIGGLE_CHROMCOUNT.setMandatory(False)
assignment_DATA_SET_BIGWIGGLE_CHROMCOUNT.setSection(None)
assignment_DATA_SET_BIGWIGGLE_CHROMCOUNT.setPositionInForms(8)

assignment_DATA_SET_BIGWIGGLE_BASESCOVERED = tr.assignPropertyType(data_set_type_BIGWIGGLE, prop_type_BASESCOVERED)
assignment_DATA_SET_BIGWIGGLE_BASESCOVERED.setMandatory(False)
assignment_DATA_SET_BIGWIGGLE_BASESCOVERED.setSection(None)
assignment_DATA_SET_BIGWIGGLE_BASESCOVERED.setPositionInForms(9)

assignment_DATA_SET_BIGWIGGLE_MEAN = tr.assignPropertyType(data_set_type_BIGWIGGLE, prop_type_MEAN)
assignment_DATA_SET_BIGWIGGLE_MEAN.setMandatory(False)
assignment_DATA_SET_BIGWIGGLE_MEAN.setSection(None)
assignment_DATA_SET_BIGWIGGLE_MEAN.setPositionInForms(10)

assignment_DATA_SET_BIGWIGGLE_MIN = tr.assignPropertyType(data_set_type_BIGWIGGLE, prop_type_MIN)
assignment_DATA_SET_BIGWIGGLE_MIN.setMandatory(False)
assignment_DATA_SET_BIGWIGGLE_MIN.setSection(None)
assignment_DATA_SET_BIGWIGGLE_MIN.setPositionInForms(11)

assignment_DATA_SET_BIGWIGGLE_MAX = tr.assignPropertyType(data_set_type_BIGWIGGLE, prop_type_MAX)
assignment_DATA_SET_BIGWIGGLE_MAX.setMandatory(False)
assignment_DATA_SET_BIGWIGGLE_MAX.setSection(None)
assignment_DATA_SET_BIGWIGGLE_MAX.setPositionInForms(12)

assignment_DATA_SET_BIGWIGGLE_STD = tr.assignPropertyType(data_set_type_BIGWIGGLE, prop_type_STD)
assignment_DATA_SET_BIGWIGGLE_STD.setMandatory(False)
assignment_DATA_SET_BIGWIGGLE_STD.setSection(None)
assignment_DATA_SET_BIGWIGGLE_STD.setPositionInForms(13)

assignment_DATA_SET_FASTQ_GZ_NOTES = tr.assignPropertyType(data_set_type_FASTQ_GZ, prop_type_NOTES)
assignment_DATA_SET_FASTQ_GZ_NOTES.setMandatory(False)
assignment_DATA_SET_FASTQ_GZ_NOTES.setSection(None)
assignment_DATA_SET_FASTQ_GZ_NOTES.setPositionInForms(1)

assignment_DATA_SET_FASTQ_GZ_YIELD_MBASES = tr.assignPropertyType(data_set_type_FASTQ_GZ, prop_type_YIELD_MBASES)
assignment_DATA_SET_FASTQ_GZ_YIELD_MBASES.setMandatory(False)
assignment_DATA_SET_FASTQ_GZ_YIELD_MBASES.setSection(None)
assignment_DATA_SET_FASTQ_GZ_YIELD_MBASES.setPositionInForms(2)

assignment_DATA_SET_FASTQ_GZ_PERCENTAGE_PASSED_FILTERING = tr.assignPropertyType(data_set_type_FASTQ_GZ, prop_type_PERCENTAGE_PASSED_FILTERING)
assignment_DATA_SET_FASTQ_GZ_PERCENTAGE_PASSED_FILTERING.setMandatory(False)
assignment_DATA_SET_FASTQ_GZ_PERCENTAGE_PASSED_FILTERING.setSection(None)
assignment_DATA_SET_FASTQ_GZ_PERCENTAGE_PASSED_FILTERING.setPositionInForms(3)

assignment_DATA_SET_FASTQ_GZ_BARCODE = tr.assignPropertyType(data_set_type_FASTQ_GZ, prop_type_BARCODE)
assignment_DATA_SET_FASTQ_GZ_BARCODE.setMandatory(False)
assignment_DATA_SET_FASTQ_GZ_BARCODE.setSection(None)
assignment_DATA_SET_FASTQ_GZ_BARCODE.setPositionInForms(4)

assignment_DATA_SET_FASTQ_GZ_PERCENTAGE_RAW_CLUSTERS_PER_LANE = tr.assignPropertyType(data_set_type_FASTQ_GZ, prop_type_PERCENTAGE_RAW_CLUSTERS_PER_LANE)
assignment_DATA_SET_FASTQ_GZ_PERCENTAGE_RAW_CLUSTERS_PER_LANE.setMandatory(False)
assignment_DATA_SET_FASTQ_GZ_PERCENTAGE_RAW_CLUSTERS_PER_LANE.setSection(None)
assignment_DATA_SET_FASTQ_GZ_PERCENTAGE_RAW_CLUSTERS_PER_LANE.setPositionInForms(5)

assignment_DATA_SET_FASTQ_GZ_PERCENTAGE_PERFECT_INDEX_READS = tr.assignPropertyType(data_set_type_FASTQ_GZ, prop_type_PERCENTAGE_PERFECT_INDEX_READS)
assignment_DATA_SET_FASTQ_GZ_PERCENTAGE_PERFECT_INDEX_READS.setMandatory(False)
assignment_DATA_SET_FASTQ_GZ_PERCENTAGE_PERFECT_INDEX_READS.setSection(None)
assignment_DATA_SET_FASTQ_GZ_PERCENTAGE_PERFECT_INDEX_READS.setPositionInForms(6)

assignment_DATA_SET_FASTQ_GZ_PERCENTAGE_ONE_MISMATCH_READS_INDEX = tr.assignPropertyType(data_set_type_FASTQ_GZ, prop_type_PERCENTAGE_ONE_MISMATCH_READS_INDEX)
assignment_DATA_SET_FASTQ_GZ_PERCENTAGE_ONE_MISMATCH_READS_INDEX.setMandatory(False)
assignment_DATA_SET_FASTQ_GZ_PERCENTAGE_ONE_MISMATCH_READS_INDEX.setSection(None)
assignment_DATA_SET_FASTQ_GZ_PERCENTAGE_ONE_MISMATCH_READS_INDEX.setPositionInForms(7)

assignment_EXPERIMENT_HT_SEQUENCING_EXPERIMENT_DESIGN = tr.assignPropertyType(exp_type_HT_SEQUENCING, prop_type_EXPERIMENT_DESIGN)
assignment_EXPERIMENT_HT_SEQUENCING_EXPERIMENT_DESIGN.setMandatory(False)
assignment_EXPERIMENT_HT_SEQUENCING_EXPERIMENT_DESIGN.setSection(None)
assignment_EXPERIMENT_HT_SEQUENCING_EXPERIMENT_DESIGN.setPositionInForms(1)

assignment_SAMPLE_ILLUMINA_FLOW_CELL_SEQUENCER = tr.assignPropertyType(samp_type_ILLUMINA_FLOW_CELL, prop_type_SEQUENCER)
assignment_SAMPLE_ILLUMINA_FLOW_CELL_SEQUENCER.setMandatory(False)
assignment_SAMPLE_ILLUMINA_FLOW_CELL_SEQUENCER.setSection(None)
assignment_SAMPLE_ILLUMINA_FLOW_CELL_SEQUENCER.setPositionInForms(2)

assignment_SAMPLE_ILLUMINA_FLOW_CELL_END_TYPE = tr.assignPropertyType(samp_type_ILLUMINA_FLOW_CELL, prop_type_END_TYPE)
assignment_SAMPLE_ILLUMINA_FLOW_CELL_END_TYPE.setMandatory(False)
assignment_SAMPLE_ILLUMINA_FLOW_CELL_END_TYPE.setSection(None)
assignment_SAMPLE_ILLUMINA_FLOW_CELL_END_TYPE.setPositionInForms(4)

assignment_SAMPLE_ILLUMINA_FLOW_CELL_FLOW_CELL_SEQUENCED_ON = tr.assignPropertyType(samp_type_ILLUMINA_FLOW_CELL, prop_type_FLOW_CELL_SEQUENCED_ON)
assignment_SAMPLE_ILLUMINA_FLOW_CELL_FLOW_CELL_SEQUENCED_ON.setMandatory(False)
assignment_SAMPLE_ILLUMINA_FLOW_CELL_FLOW_CELL_SEQUENCED_ON.setSection(None)
assignment_SAMPLE_ILLUMINA_FLOW_CELL_FLOW_CELL_SEQUENCED_ON.setPositionInForms(5)

assignment_SAMPLE_ILLUMINA_FLOW_CELL_SEQUENCER_FINISHED = tr.assignPropertyType(samp_type_ILLUMINA_FLOW_CELL, prop_type_SEQUENCER_FINISHED)
assignment_SAMPLE_ILLUMINA_FLOW_CELL_SEQUENCER_FINISHED.setMandatory(False)
assignment_SAMPLE_ILLUMINA_FLOW_CELL_SEQUENCER_FINISHED.setSection(None)
assignment_SAMPLE_ILLUMINA_FLOW_CELL_SEQUENCER_FINISHED.setPositionInForms(6)

assignment_SAMPLE_ILLUMINA_FLOW_CELL_ILLUMINA_PIPELINE_VERSION = tr.assignPropertyType(samp_type_ILLUMINA_FLOW_CELL, prop_type_ILLUMINA_PIPELINE_VERSION)
assignment_SAMPLE_ILLUMINA_FLOW_CELL_ILLUMINA_PIPELINE_VERSION.setMandatory(False)
assignment_SAMPLE_ILLUMINA_FLOW_CELL_ILLUMINA_PIPELINE_VERSION.setSection(None)
assignment_SAMPLE_ILLUMINA_FLOW_CELL_ILLUMINA_PIPELINE_VERSION.setPositionInForms(7)

assignment_SAMPLE_ILLUMINA_FLOW_CELL_CYCLES_REQUESTED_BY_CUSTOMER = tr.assignPropertyType(samp_type_ILLUMINA_FLOW_CELL, prop_type_CYCLES_REQUESTED_BY_CUSTOMER)
assignment_SAMPLE_ILLUMINA_FLOW_CELL_CYCLES_REQUESTED_BY_CUSTOMER.setMandatory(False)
assignment_SAMPLE_ILLUMINA_FLOW_CELL_CYCLES_REQUESTED_BY_CUSTOMER.setSection(None)
assignment_SAMPLE_ILLUMINA_FLOW_CELL_CYCLES_REQUESTED_BY_CUSTOMER.setPositionInForms(8)

assignment_SAMPLE_ILLUMINA_FLOW_CELL_INDEXREAD = tr.assignPropertyType(samp_type_ILLUMINA_FLOW_CELL, prop_type_INDEXREAD)
assignment_SAMPLE_ILLUMINA_FLOW_CELL_INDEXREAD.setMandatory(False)
assignment_SAMPLE_ILLUMINA_FLOW_CELL_INDEXREAD.setSection(None)
assignment_SAMPLE_ILLUMINA_FLOW_CELL_INDEXREAD.setPositionInForms(9)

assignment_SAMPLE_ILLUMINA_FLOW_CELL_CONTROL_LANE = tr.assignPropertyType(samp_type_ILLUMINA_FLOW_CELL, prop_type_CONTROL_LANE)
assignment_SAMPLE_ILLUMINA_FLOW_CELL_CONTROL_LANE.setMandatory(False)
assignment_SAMPLE_ILLUMINA_FLOW_CELL_CONTROL_LANE.setSection(None)
assignment_SAMPLE_ILLUMINA_FLOW_CELL_CONTROL_LANE.setPositionInForms(10)

assignment_SAMPLE_ILLUMINA_FLOW_CELL_FLOWCELLTYPE = tr.assignPropertyType(samp_type_ILLUMINA_FLOW_CELL, prop_type_FLOWCELLTYPE)
assignment_SAMPLE_ILLUMINA_FLOW_CELL_FLOWCELLTYPE.setMandatory(False)
assignment_SAMPLE_ILLUMINA_FLOW_CELL_FLOWCELLTYPE.setSection(None)
assignment_SAMPLE_ILLUMINA_FLOW_CELL_FLOWCELLTYPE.setPositionInForms(11)

assignment_SAMPLE_ILLUMINA_FLOW_CELL_LANECOUNT = tr.assignPropertyType(samp_type_ILLUMINA_FLOW_CELL, prop_type_LANECOUNT)
assignment_SAMPLE_ILLUMINA_FLOW_CELL_LANECOUNT.setMandatory(False)
assignment_SAMPLE_ILLUMINA_FLOW_CELL_LANECOUNT.setSection(None)
assignment_SAMPLE_ILLUMINA_FLOW_CELL_LANECOUNT.setPositionInForms(12)

assignment_SAMPLE_ILLUMINA_FLOW_CELL_SURFACECOUNT = tr.assignPropertyType(samp_type_ILLUMINA_FLOW_CELL, prop_type_SURFACECOUNT)
assignment_SAMPLE_ILLUMINA_FLOW_CELL_SURFACECOUNT.setMandatory(False)
assignment_SAMPLE_ILLUMINA_FLOW_CELL_SURFACECOUNT.setSection(None)
assignment_SAMPLE_ILLUMINA_FLOW_CELL_SURFACECOUNT.setPositionInForms(13)

assignment_SAMPLE_ILLUMINA_FLOW_CELL_SWATHCOUNT = tr.assignPropertyType(samp_type_ILLUMINA_FLOW_CELL, prop_type_SWATHCOUNT)
assignment_SAMPLE_ILLUMINA_FLOW_CELL_SWATHCOUNT.setMandatory(False)
assignment_SAMPLE_ILLUMINA_FLOW_CELL_SWATHCOUNT.setSection(None)
assignment_SAMPLE_ILLUMINA_FLOW_CELL_SWATHCOUNT.setPositionInForms(14)

assignment_SAMPLE_ILLUMINA_FLOW_CELL_TILECOUNT = tr.assignPropertyType(samp_type_ILLUMINA_FLOW_CELL, prop_type_TILECOUNT)
assignment_SAMPLE_ILLUMINA_FLOW_CELL_TILECOUNT.setMandatory(False)
assignment_SAMPLE_ILLUMINA_FLOW_CELL_TILECOUNT.setSection(None)
assignment_SAMPLE_ILLUMINA_FLOW_CELL_TILECOUNT.setPositionInForms(15)

assignment_SAMPLE_ILLUMINA_FLOW_CELL_SBS_KIT = tr.assignPropertyType(samp_type_ILLUMINA_FLOW_CELL, prop_type_SBS_KIT)
assignment_SAMPLE_ILLUMINA_FLOW_CELL_SBS_KIT.setMandatory(False)
assignment_SAMPLE_ILLUMINA_FLOW_CELL_SBS_KIT.setSection(None)
assignment_SAMPLE_ILLUMINA_FLOW_CELL_SBS_KIT.setPositionInForms(16)

assignment_SAMPLE_ILLUMINA_FLOW_CELL_PAIRED_END_KIT = tr.assignPropertyType(samp_type_ILLUMINA_FLOW_CELL, prop_type_PAIRED_END_KIT)
assignment_SAMPLE_ILLUMINA_FLOW_CELL_PAIRED_END_KIT.setMandatory(False)
assignment_SAMPLE_ILLUMINA_FLOW_CELL_PAIRED_END_KIT.setSection(None)
assignment_SAMPLE_ILLUMINA_FLOW_CELL_PAIRED_END_KIT.setPositionInForms(17)

assignment_SAMPLE_ILLUMINA_FLOW_CELL_ANALYSIS_FINISHED = tr.assignPropertyType(samp_type_ILLUMINA_FLOW_CELL, prop_type_ANALYSIS_FINISHED)
assignment_SAMPLE_ILLUMINA_FLOW_CELL_ANALYSIS_FINISHED.setMandatory(False)
assignment_SAMPLE_ILLUMINA_FLOW_CELL_ANALYSIS_FINISHED.setSection(None)
assignment_SAMPLE_ILLUMINA_FLOW_CELL_ANALYSIS_FINISHED.setPositionInForms(19)

assignment_SAMPLE_ILLUMINA_FLOW_CELL_NOTES = tr.assignPropertyType(samp_type_ILLUMINA_FLOW_CELL, prop_type_NOTES)
assignment_SAMPLE_ILLUMINA_FLOW_CELL_NOTES.setMandatory(False)
assignment_SAMPLE_ILLUMINA_FLOW_CELL_NOTES.setSection(None)
assignment_SAMPLE_ILLUMINA_FLOW_CELL_NOTES.setPositionInForms(20)

assignment_SAMPLE_ILLUMINA_FLOW_LANE_DATA_TRANSFERRED = tr.assignPropertyType(samp_type_ILLUMINA_FLOW_LANE, prop_type_DATA_TRANSFERRED)
assignment_SAMPLE_ILLUMINA_FLOW_LANE_DATA_TRANSFERRED.setMandatory(False)
assignment_SAMPLE_ILLUMINA_FLOW_LANE_DATA_TRANSFERRED.setSection(None)
assignment_SAMPLE_ILLUMINA_FLOW_LANE_DATA_TRANSFERRED.setPositionInForms(1)

assignment_SAMPLE_ILLUMINA_FLOW_LANE_NOTES = tr.assignPropertyType(samp_type_ILLUMINA_FLOW_LANE, prop_type_NOTES)
assignment_SAMPLE_ILLUMINA_FLOW_LANE_NOTES.setMandatory(False)
assignment_SAMPLE_ILLUMINA_FLOW_LANE_NOTES.setSection(None)
assignment_SAMPLE_ILLUMINA_FLOW_LANE_NOTES.setPositionInForms(4)

assignment_DATA_SET_ILLUMINA_GA_OUTPUT_SAMPLE_KIND = tr.assignPropertyType(data_set_type_ILLUMINA_GA_OUTPUT, prop_type_SAMPLE_KIND)
assignment_DATA_SET_ILLUMINA_GA_OUTPUT_SAMPLE_KIND.setMandatory(False)
assignment_DATA_SET_ILLUMINA_GA_OUTPUT_SAMPLE_KIND.setSection(None)
assignment_DATA_SET_ILLUMINA_GA_OUTPUT_SAMPLE_KIND.setPositionInForms(1)

assignment_DATA_SET_ILLUMINA_HISEQ_OUTPUT_CASAVA_VERSION = tr.assignPropertyType(data_set_type_ILLUMINA_HISEQ_OUTPUT, prop_type_CASAVA_VERSION)
assignment_DATA_SET_ILLUMINA_HISEQ_OUTPUT_CASAVA_VERSION.setMandatory(False)
assignment_DATA_SET_ILLUMINA_HISEQ_OUTPUT_CASAVA_VERSION.setSection(None)
assignment_DATA_SET_ILLUMINA_HISEQ_OUTPUT_CASAVA_VERSION.setPositionInForms(1)

assignment_SAMPLE_ILLUMINA_SEQUENCING_AFFILIATION = tr.assignPropertyType(samp_type_ILLUMINA_SEQUENCING, prop_type_AFFILIATION)
assignment_SAMPLE_ILLUMINA_SEQUENCING_AFFILIATION.setMandatory(True)
assignment_SAMPLE_ILLUMINA_SEQUENCING_AFFILIATION.setSection('Contact')
assignment_SAMPLE_ILLUMINA_SEQUENCING_AFFILIATION.setPositionInForms(9)

assignment_SAMPLE_ILLUMINA_SEQUENCING_CONTACT_PERSON_NAME = tr.assignPropertyType(samp_type_ILLUMINA_SEQUENCING, prop_type_CONTACT_PERSON_NAME)
assignment_SAMPLE_ILLUMINA_SEQUENCING_CONTACT_PERSON_NAME.setMandatory(True)
assignment_SAMPLE_ILLUMINA_SEQUENCING_CONTACT_PERSON_NAME.setSection('Contact')
assignment_SAMPLE_ILLUMINA_SEQUENCING_CONTACT_PERSON_NAME.setPositionInForms(10)

assignment_SAMPLE_ILLUMINA_SEQUENCING_CONTACT_PERSON_EMAIL = tr.assignPropertyType(samp_type_ILLUMINA_SEQUENCING, prop_type_CONTACT_PERSON_EMAIL)
assignment_SAMPLE_ILLUMINA_SEQUENCING_CONTACT_PERSON_EMAIL.setMandatory(True)
assignment_SAMPLE_ILLUMINA_SEQUENCING_CONTACT_PERSON_EMAIL.setSection('Contact')
assignment_SAMPLE_ILLUMINA_SEQUENCING_CONTACT_PERSON_EMAIL.setPositionInForms(11)

assignment_SAMPLE_ILLUMINA_SEQUENCING_PRINCIPAL_INVESTIGATOR_NAME = tr.assignPropertyType(samp_type_ILLUMINA_SEQUENCING, prop_type_PRINCIPAL_INVESTIGATOR_NAME)
assignment_SAMPLE_ILLUMINA_SEQUENCING_PRINCIPAL_INVESTIGATOR_NAME.setMandatory(True)
assignment_SAMPLE_ILLUMINA_SEQUENCING_PRINCIPAL_INVESTIGATOR_NAME.setSection('Contact')
assignment_SAMPLE_ILLUMINA_SEQUENCING_PRINCIPAL_INVESTIGATOR_NAME.setPositionInForms(12)

assignment_SAMPLE_ILLUMINA_SEQUENCING_PRINCIPAL_INVESTIGATOR_EMAIL = tr.assignPropertyType(samp_type_ILLUMINA_SEQUENCING, prop_type_PRINCIPAL_INVESTIGATOR_EMAIL)
assignment_SAMPLE_ILLUMINA_SEQUENCING_PRINCIPAL_INVESTIGATOR_EMAIL.setMandatory(True)
assignment_SAMPLE_ILLUMINA_SEQUENCING_PRINCIPAL_INVESTIGATOR_EMAIL.setSection('Contact')
assignment_SAMPLE_ILLUMINA_SEQUENCING_PRINCIPAL_INVESTIGATOR_EMAIL.setPositionInForms(13)

assignment_SAMPLE_ILLUMINA_SEQUENCING_EXTERNAL_SAMPLE_NAME = tr.assignPropertyType(samp_type_ILLUMINA_SEQUENCING, prop_type_EXTERNAL_SAMPLE_NAME)
assignment_SAMPLE_ILLUMINA_SEQUENCING_EXTERNAL_SAMPLE_NAME.setMandatory(True)
assignment_SAMPLE_ILLUMINA_SEQUENCING_EXTERNAL_SAMPLE_NAME.setSection('Sample Data')
assignment_SAMPLE_ILLUMINA_SEQUENCING_EXTERNAL_SAMPLE_NAME.setPositionInForms(25)

assignment_SAMPLE_ILLUMINA_SEQUENCING_SAMPLE_KIND = tr.assignPropertyType(samp_type_ILLUMINA_SEQUENCING, prop_type_SAMPLE_KIND)
assignment_SAMPLE_ILLUMINA_SEQUENCING_SAMPLE_KIND.setMandatory(True)
assignment_SAMPLE_ILLUMINA_SEQUENCING_SAMPLE_KIND.setSection('Sample Data')
assignment_SAMPLE_ILLUMINA_SEQUENCING_SAMPLE_KIND.setPositionInForms(27)

assignment_SAMPLE_ILLUMINA_SEQUENCING_SEQUENCING_APPLICATION = tr.assignPropertyType(samp_type_ILLUMINA_SEQUENCING, prop_type_SEQUENCING_APPLICATION)
assignment_SAMPLE_ILLUMINA_SEQUENCING_SEQUENCING_APPLICATION.setMandatory(True)
assignment_SAMPLE_ILLUMINA_SEQUENCING_SEQUENCING_APPLICATION.setSection('Sample Data')
assignment_SAMPLE_ILLUMINA_SEQUENCING_SEQUENCING_APPLICATION.setPositionInForms(28)

assignment_SAMPLE_ILLUMINA_SEQUENCING_CONCENTRATION_ORIGINAL_ILLUMINA = tr.assignPropertyType(samp_type_ILLUMINA_SEQUENCING, prop_type_CONCENTRATION_ORIGINAL_ILLUMINA)
assignment_SAMPLE_ILLUMINA_SEQUENCING_CONCENTRATION_ORIGINAL_ILLUMINA.setMandatory(False)
assignment_SAMPLE_ILLUMINA_SEQUENCING_CONCENTRATION_ORIGINAL_ILLUMINA.setSection('Sample Data')
assignment_SAMPLE_ILLUMINA_SEQUENCING_CONCENTRATION_ORIGINAL_ILLUMINA.setPositionInForms(32)

assignment_SAMPLE_ILLUMINA_SEQUENCING_CONC_IF_SAMPLE_PROCESSED_DNA_LIBRARY = tr.assignPropertyType(samp_type_ILLUMINA_SEQUENCING, prop_type_CONC_IF_SAMPLE_PROCESSED_DNA_LIBRARY)
assignment_SAMPLE_ILLUMINA_SEQUENCING_CONC_IF_SAMPLE_PROCESSED_DNA_LIBRARY.setMandatory(False)
assignment_SAMPLE_ILLUMINA_SEQUENCING_CONC_IF_SAMPLE_PROCESSED_DNA_LIBRARY.setSection('Sample Data')
assignment_SAMPLE_ILLUMINA_SEQUENCING_CONC_IF_SAMPLE_PROCESSED_DNA_LIBRARY.setPositionInForms(35)

assignment_SAMPLE_ILLUMINA_SEQUENCING_NCBI_ORGANISM_TAXONOMY = tr.assignPropertyType(samp_type_ILLUMINA_SEQUENCING, prop_type_NCBI_ORGANISM_TAXONOMY)
assignment_SAMPLE_ILLUMINA_SEQUENCING_NCBI_ORGANISM_TAXONOMY.setMandatory(True)
assignment_SAMPLE_ILLUMINA_SEQUENCING_NCBI_ORGANISM_TAXONOMY.setSection('Sample Data')
assignment_SAMPLE_ILLUMINA_SEQUENCING_NCBI_ORGANISM_TAXONOMY.setPositionInForms(36)

assignment_SAMPLE_ILLUMINA_SEQUENCING_ORGANISM_FREE = tr.assignPropertyType(samp_type_ILLUMINA_SEQUENCING, prop_type_ORGANISM_FREE)
assignment_SAMPLE_ILLUMINA_SEQUENCING_ORGANISM_FREE.setMandatory(False)
assignment_SAMPLE_ILLUMINA_SEQUENCING_ORGANISM_FREE.setSection('Sample Data')
assignment_SAMPLE_ILLUMINA_SEQUENCING_ORGANISM_FREE.setPositionInForms(40)

assignment_SAMPLE_ILLUMINA_SEQUENCING_CELL_PLASTICITY_SYSTEMSX = tr.assignPropertyType(samp_type_ILLUMINA_SEQUENCING, prop_type_CELL_PLASTICITY_SYSTEMSX)
assignment_SAMPLE_ILLUMINA_SEQUENCING_CELL_PLASTICITY_SYSTEMSX.setMandatory(False)
assignment_SAMPLE_ILLUMINA_SEQUENCING_CELL_PLASTICITY_SYSTEMSX.setSection('Sample Data')
assignment_SAMPLE_ILLUMINA_SEQUENCING_CELL_PLASTICITY_SYSTEMSX.setPositionInForms(41)

assignment_SAMPLE_ILLUMINA_SEQUENCING_SAMPLE_LIBRARY_STRATEGY = tr.assignPropertyType(samp_type_ILLUMINA_SEQUENCING, prop_type_SAMPLE_LIBRARY_STRATEGY)
assignment_SAMPLE_ILLUMINA_SEQUENCING_SAMPLE_LIBRARY_STRATEGY.setMandatory(False)
assignment_SAMPLE_ILLUMINA_SEQUENCING_SAMPLE_LIBRARY_STRATEGY.setSection('GEO compliant annotation')
assignment_SAMPLE_ILLUMINA_SEQUENCING_SAMPLE_LIBRARY_STRATEGY.setPositionInForms(48)

assignment_SAMPLE_ILLUMINA_SEQUENCING_SAMPLE_SOURCE_NAME = tr.assignPropertyType(samp_type_ILLUMINA_SEQUENCING, prop_type_SAMPLE_SOURCE_NAME)
assignment_SAMPLE_ILLUMINA_SEQUENCING_SAMPLE_SOURCE_NAME.setMandatory(False)
assignment_SAMPLE_ILLUMINA_SEQUENCING_SAMPLE_SOURCE_NAME.setSection('GEO compliant annotation')
assignment_SAMPLE_ILLUMINA_SEQUENCING_SAMPLE_SOURCE_NAME.setPositionInForms(49)

assignment_SAMPLE_ILLUMINA_SEQUENCING_SAMPLE_LIBRARY_SELECTION = tr.assignPropertyType(samp_type_ILLUMINA_SEQUENCING, prop_type_SAMPLE_LIBRARY_SELECTION)
assignment_SAMPLE_ILLUMINA_SEQUENCING_SAMPLE_LIBRARY_SELECTION.setMandatory(False)
assignment_SAMPLE_ILLUMINA_SEQUENCING_SAMPLE_LIBRARY_SELECTION.setSection('GEO compliant annotation')
assignment_SAMPLE_ILLUMINA_SEQUENCING_SAMPLE_LIBRARY_SELECTION.setPositionInForms(50)

assignment_SAMPLE_ILLUMINA_SEQUENCING_SAMPLE_CHARACTERISTICS = tr.assignPropertyType(samp_type_ILLUMINA_SEQUENCING, prop_type_SAMPLE_CHARACTERISTICS)
assignment_SAMPLE_ILLUMINA_SEQUENCING_SAMPLE_CHARACTERISTICS.setMandatory(False)
assignment_SAMPLE_ILLUMINA_SEQUENCING_SAMPLE_CHARACTERISTICS.setSection('GEO compliant annotation')
assignment_SAMPLE_ILLUMINA_SEQUENCING_SAMPLE_CHARACTERISTICS.setPositionInForms(51)

assignment_SAMPLE_ILLUMINA_SEQUENCING_SAMPLE_MOLECULE = tr.assignPropertyType(samp_type_ILLUMINA_SEQUENCING, prop_type_SAMPLE_MOLECULE)
assignment_SAMPLE_ILLUMINA_SEQUENCING_SAMPLE_MOLECULE.setMandatory(False)
assignment_SAMPLE_ILLUMINA_SEQUENCING_SAMPLE_MOLECULE.setSection('GEO compliant annotation')
assignment_SAMPLE_ILLUMINA_SEQUENCING_SAMPLE_MOLECULE.setPositionInForms(52)

assignment_SAMPLE_ILLUMINA_SEQUENCING_SAMPLE_EXTRACT_PROTOCOL = tr.assignPropertyType(samp_type_ILLUMINA_SEQUENCING, prop_type_SAMPLE_EXTRACT_PROTOCOL)
assignment_SAMPLE_ILLUMINA_SEQUENCING_SAMPLE_EXTRACT_PROTOCOL.setMandatory(False)
assignment_SAMPLE_ILLUMINA_SEQUENCING_SAMPLE_EXTRACT_PROTOCOL.setSection('GEO compliant annotation')
assignment_SAMPLE_ILLUMINA_SEQUENCING_SAMPLE_EXTRACT_PROTOCOL.setPositionInForms(53)

assignment_SAMPLE_ILLUMINA_SEQUENCING_SAMPLE_DATA_PROCESSING = tr.assignPropertyType(samp_type_ILLUMINA_SEQUENCING, prop_type_SAMPLE_DATA_PROCESSING)
assignment_SAMPLE_ILLUMINA_SEQUENCING_SAMPLE_DATA_PROCESSING.setMandatory(False)
assignment_SAMPLE_ILLUMINA_SEQUENCING_SAMPLE_DATA_PROCESSING.setSection('GEO compliant annotation')
assignment_SAMPLE_ILLUMINA_SEQUENCING_SAMPLE_DATA_PROCESSING.setPositionInForms(55)

assignment_SAMPLE_ILLUMINA_SEQUENCING_CYCLES_REQUESTED_BY_CUSTOMER = tr.assignPropertyType(samp_type_ILLUMINA_SEQUENCING, prop_type_CYCLES_REQUESTED_BY_CUSTOMER)
assignment_SAMPLE_ILLUMINA_SEQUENCING_CYCLES_REQUESTED_BY_CUSTOMER.setMandatory(False)
assignment_SAMPLE_ILLUMINA_SEQUENCING_CYCLES_REQUESTED_BY_CUSTOMER.setSection('Sequencing')
assignment_SAMPLE_ILLUMINA_SEQUENCING_CYCLES_REQUESTED_BY_CUSTOMER.setPositionInForms(57)

assignment_SAMPLE_ILLUMINA_SEQUENCING_END_TYPE = tr.assignPropertyType(samp_type_ILLUMINA_SEQUENCING, prop_type_END_TYPE)
assignment_SAMPLE_ILLUMINA_SEQUENCING_END_TYPE.setMandatory(False)
assignment_SAMPLE_ILLUMINA_SEQUENCING_END_TYPE.setSection('Sequencing')
assignment_SAMPLE_ILLUMINA_SEQUENCING_END_TYPE.setPositionInForms(58)

assignment_SAMPLE_ILLUMINA_SEQUENCING_REQUIRED_LANES = tr.assignPropertyType(samp_type_ILLUMINA_SEQUENCING, prop_type_REQUIRED_LANES)
assignment_SAMPLE_ILLUMINA_SEQUENCING_REQUIRED_LANES.setMandatory(False)
assignment_SAMPLE_ILLUMINA_SEQUENCING_REQUIRED_LANES.setSection('Sequencing')
assignment_SAMPLE_ILLUMINA_SEQUENCING_REQUIRED_LANES.setPositionInForms(59)

assignment_SAMPLE_ILLUMINA_SEQUENCING_BARCODE_LENGTH = tr.assignPropertyType(samp_type_ILLUMINA_SEQUENCING, prop_type_BARCODE_LENGTH)
assignment_SAMPLE_ILLUMINA_SEQUENCING_BARCODE_LENGTH.setMandatory(False)
assignment_SAMPLE_ILLUMINA_SEQUENCING_BARCODE_LENGTH.setSection('Sequencing')
assignment_SAMPLE_ILLUMINA_SEQUENCING_BARCODE_LENGTH.setPositionInForms(60)

assignment_SAMPLE_ILLUMINA_SEQUENCING_BARCODES = tr.assignPropertyType(samp_type_ILLUMINA_SEQUENCING, prop_type_BARCODES)
assignment_SAMPLE_ILLUMINA_SEQUENCING_BARCODES.setMandatory(False)
assignment_SAMPLE_ILLUMINA_SEQUENCING_BARCODES.setSection('Sequencing')
assignment_SAMPLE_ILLUMINA_SEQUENCING_BARCODES.setPositionInForms(62)

assignment_SAMPLE_ILLUMINA_SEQUENCING_BARCODE = tr.assignPropertyType(samp_type_ILLUMINA_SEQUENCING, prop_type_BARCODE)
assignment_SAMPLE_ILLUMINA_SEQUENCING_BARCODE.setMandatory(False)
assignment_SAMPLE_ILLUMINA_SEQUENCING_BARCODE.setSection('Sequencing')
assignment_SAMPLE_ILLUMINA_SEQUENCING_BARCODE.setPositionInForms(63)

assignment_SAMPLE_ILLUMINA_SEQUENCING_INDEX2 = tr.assignPropertyType(samp_type_ILLUMINA_SEQUENCING, prop_type_INDEX2)
assignment_SAMPLE_ILLUMINA_SEQUENCING_INDEX2.setMandatory(False)
assignment_SAMPLE_ILLUMINA_SEQUENCING_INDEX2.setSection('Sequencing')
assignment_SAMPLE_ILLUMINA_SEQUENCING_INDEX2.setPositionInForms(64)

assignment_SAMPLE_ILLUMINA_SEQUENCING_NOTES_CUSTOMER = tr.assignPropertyType(samp_type_ILLUMINA_SEQUENCING, prop_type_NOTES_CUSTOMER)
assignment_SAMPLE_ILLUMINA_SEQUENCING_NOTES_CUSTOMER.setMandatory(False)
assignment_SAMPLE_ILLUMINA_SEQUENCING_NOTES_CUSTOMER.setSection('Notes')
assignment_SAMPLE_ILLUMINA_SEQUENCING_NOTES_CUSTOMER.setPositionInForms(73)

assignment_SAMPLE_ILLUMINA_SEQUENCING_OTRS = tr.assignPropertyType(samp_type_ILLUMINA_SEQUENCING, prop_type_OTRS)
assignment_SAMPLE_ILLUMINA_SEQUENCING_OTRS.setMandatory(False)
assignment_SAMPLE_ILLUMINA_SEQUENCING_OTRS.setSection('Laboratory of Quantitative Genomics (LQG)')
assignment_SAMPLE_ILLUMINA_SEQUENCING_OTRS.setPositionInForms(74)

assignment_SAMPLE_ILLUMINA_SEQUENCING_SAMPLE_ID = tr.assignPropertyType(samp_type_ILLUMINA_SEQUENCING, prop_type_SAMPLE_ID)
assignment_SAMPLE_ILLUMINA_SEQUENCING_SAMPLE_ID.setMandatory(False)
assignment_SAMPLE_ILLUMINA_SEQUENCING_SAMPLE_ID.setSection('Laboratory of Quantitative Genomics (LQG)')
assignment_SAMPLE_ILLUMINA_SEQUENCING_SAMPLE_ID.setPositionInForms(75)

assignment_SAMPLE_ILLUMINA_SEQUENCING_BIOLOGICAL_SAMPLE_ARRIVED = tr.assignPropertyType(samp_type_ILLUMINA_SEQUENCING, prop_type_BIOLOGICAL_SAMPLE_ARRIVED)
assignment_SAMPLE_ILLUMINA_SEQUENCING_BIOLOGICAL_SAMPLE_ARRIVED.setMandatory(False)
assignment_SAMPLE_ILLUMINA_SEQUENCING_BIOLOGICAL_SAMPLE_ARRIVED.setSection('Laboratory of Quantitative Genomics (LQG)')
assignment_SAMPLE_ILLUMINA_SEQUENCING_BIOLOGICAL_SAMPLE_ARRIVED.setPositionInForms(76)

assignment_SAMPLE_ILLUMINA_SEQUENCING_LIBRARY_PROCESSING_POSSIBLE = tr.assignPropertyType(samp_type_ILLUMINA_SEQUENCING, prop_type_LIBRARY_PROCESSING_POSSIBLE)
assignment_SAMPLE_ILLUMINA_SEQUENCING_LIBRARY_PROCESSING_POSSIBLE.setMandatory(False)
assignment_SAMPLE_ILLUMINA_SEQUENCING_LIBRARY_PROCESSING_POSSIBLE.setSection('LQG: Generation of DNA library')
assignment_SAMPLE_ILLUMINA_SEQUENCING_LIBRARY_PROCESSING_POSSIBLE.setPositionInForms(77)

assignment_SAMPLE_ILLUMINA_SEQUENCING_FRAGMENT_SIZE_PREPARED_ILLUMINA = tr.assignPropertyType(samp_type_ILLUMINA_SEQUENCING, prop_type_FRAGMENT_SIZE_PREPARED_ILLUMINA)
assignment_SAMPLE_ILLUMINA_SEQUENCING_FRAGMENT_SIZE_PREPARED_ILLUMINA.setMandatory(False)
assignment_SAMPLE_ILLUMINA_SEQUENCING_FRAGMENT_SIZE_PREPARED_ILLUMINA.setSection('LQG: Generation of DNA library')
assignment_SAMPLE_ILLUMINA_SEQUENCING_FRAGMENT_SIZE_PREPARED_ILLUMINA.setPositionInForms(78)

assignment_SAMPLE_ILLUMINA_SEQUENCING_CONCENTRATION_PREPARED_ILLUMINA = tr.assignPropertyType(samp_type_ILLUMINA_SEQUENCING, prop_type_CONCENTRATION_PREPARED_ILLUMINA)
assignment_SAMPLE_ILLUMINA_SEQUENCING_CONCENTRATION_PREPARED_ILLUMINA.setMandatory(False)
assignment_SAMPLE_ILLUMINA_SEQUENCING_CONCENTRATION_PREPARED_ILLUMINA.setSection('LQG: Generation of DNA library')
assignment_SAMPLE_ILLUMINA_SEQUENCING_CONCENTRATION_PREPARED_ILLUMINA.setPositionInForms(80)

assignment_SAMPLE_ILLUMINA_SEQUENCING_NM_DNA = tr.assignPropertyType(samp_type_ILLUMINA_SEQUENCING, prop_type_NM_DNA)
assignment_SAMPLE_ILLUMINA_SEQUENCING_NM_DNA.setMandatory(False)
assignment_SAMPLE_ILLUMINA_SEQUENCING_NM_DNA.setSection('LQG: Generation of DNA library')
assignment_SAMPLE_ILLUMINA_SEQUENCING_NM_DNA.setPositionInForms(81)

assignment_SAMPLE_ILLUMINA_SEQUENCING_UL_STOCK = tr.assignPropertyType(samp_type_ILLUMINA_SEQUENCING, prop_type_UL_STOCK)
assignment_SAMPLE_ILLUMINA_SEQUENCING_UL_STOCK.setMandatory(False)
assignment_SAMPLE_ILLUMINA_SEQUENCING_UL_STOCK.setSection('LQG: Generation of DNA library')
assignment_SAMPLE_ILLUMINA_SEQUENCING_UL_STOCK.setPositionInForms(82)

assignment_SAMPLE_ILLUMINA_SEQUENCING_UL_DNA = tr.assignPropertyType(samp_type_ILLUMINA_SEQUENCING, prop_type_UL_DNA)
assignment_SAMPLE_ILLUMINA_SEQUENCING_UL_DNA.setMandatory(False)
assignment_SAMPLE_ILLUMINA_SEQUENCING_UL_DNA.setSection('LQG: Generation of DNA library')
assignment_SAMPLE_ILLUMINA_SEQUENCING_UL_DNA.setPositionInForms(83)

assignment_SAMPLE_ILLUMINA_SEQUENCING_UL_EB = tr.assignPropertyType(samp_type_ILLUMINA_SEQUENCING, prop_type_UL_EB)
assignment_SAMPLE_ILLUMINA_SEQUENCING_UL_EB.setMandatory(False)
assignment_SAMPLE_ILLUMINA_SEQUENCING_UL_EB.setSection('LQG: Generation of DNA library')
assignment_SAMPLE_ILLUMINA_SEQUENCING_UL_EB.setPositionInForms(84)

assignment_SAMPLE_ILLUMINA_SEQUENCING_KIT_PREPARED = tr.assignPropertyType(samp_type_ILLUMINA_SEQUENCING, prop_type_KIT_PREPARED)
assignment_SAMPLE_ILLUMINA_SEQUENCING_KIT_PREPARED.setMandatory(False)
assignment_SAMPLE_ILLUMINA_SEQUENCING_KIT_PREPARED.setSection('LQG: Generation of DNA library')
assignment_SAMPLE_ILLUMINA_SEQUENCING_KIT_PREPARED.setPositionInForms(85)

assignment_SAMPLE_ILLUMINA_SEQUENCING_PREPARED_BY = tr.assignPropertyType(samp_type_ILLUMINA_SEQUENCING, prop_type_PREPARED_BY)
assignment_SAMPLE_ILLUMINA_SEQUENCING_PREPARED_BY.setMandatory(False)
assignment_SAMPLE_ILLUMINA_SEQUENCING_PREPARED_BY.setSection('LQG: Generation of DNA library')
assignment_SAMPLE_ILLUMINA_SEQUENCING_PREPARED_BY.setPositionInForms(86)

assignment_SAMPLE_ILLUMINA_SEQUENCING_KIT = tr.assignPropertyType(samp_type_ILLUMINA_SEQUENCING, prop_type_KIT)
assignment_SAMPLE_ILLUMINA_SEQUENCING_KIT.setMandatory(False)
assignment_SAMPLE_ILLUMINA_SEQUENCING_KIT.setSection('LQG: Generation of DNA library')
assignment_SAMPLE_ILLUMINA_SEQUENCING_KIT.setPositionInForms(87)

assignment_SAMPLE_ILLUMINA_SEQUENCING_LOT = tr.assignPropertyType(samp_type_ILLUMINA_SEQUENCING, prop_type_LOT)
assignment_SAMPLE_ILLUMINA_SEQUENCING_LOT.setMandatory(False)
assignment_SAMPLE_ILLUMINA_SEQUENCING_LOT.setSection('LQG: Generation of DNA library')
assignment_SAMPLE_ILLUMINA_SEQUENCING_LOT.setPositionInForms(89)

assignment_SAMPLE_ILLUMINA_SEQUENCING_STARTING_AMOUNT_OF_SAMPLE_IN_NG = tr.assignPropertyType(samp_type_ILLUMINA_SEQUENCING, prop_type_STARTING_AMOUNT_OF_SAMPLE_IN_NG)
assignment_SAMPLE_ILLUMINA_SEQUENCING_STARTING_AMOUNT_OF_SAMPLE_IN_NG.setMandatory(False)
assignment_SAMPLE_ILLUMINA_SEQUENCING_STARTING_AMOUNT_OF_SAMPLE_IN_NG.setSection('LQG: Generation of DNA library')
assignment_SAMPLE_ILLUMINA_SEQUENCING_STARTING_AMOUNT_OF_SAMPLE_IN_NG.setPositionInForms(90)

assignment_SAMPLE_ILLUMINA_SEQUENCING_DNA_CONCENTRATION_OF_LIBRARY = tr.assignPropertyType(samp_type_ILLUMINA_SEQUENCING, prop_type_DNA_CONCENTRATION_OF_LIBRARY)
assignment_SAMPLE_ILLUMINA_SEQUENCING_DNA_CONCENTRATION_OF_LIBRARY.setMandatory(False)
assignment_SAMPLE_ILLUMINA_SEQUENCING_DNA_CONCENTRATION_OF_LIBRARY.setSection('LQG: Generation of DNA library')
assignment_SAMPLE_ILLUMINA_SEQUENCING_DNA_CONCENTRATION_OF_LIBRARY.setPositionInForms(92)

assignment_SAMPLE_ILLUMINA_SEQUENCING_NANO_DROP = tr.assignPropertyType(samp_type_ILLUMINA_SEQUENCING, prop_type_NANO_DROP)
assignment_SAMPLE_ILLUMINA_SEQUENCING_NANO_DROP.setMandatory(False)
assignment_SAMPLE_ILLUMINA_SEQUENCING_NANO_DROP.setSection('LQG: Generation of DNA library')
assignment_SAMPLE_ILLUMINA_SEQUENCING_NANO_DROP.setPositionInForms(93)

assignment_SAMPLE_ILLUMINA_SEQUENCING_AGILENT_KIT = tr.assignPropertyType(samp_type_ILLUMINA_SEQUENCING, prop_type_AGILENT_KIT)
assignment_SAMPLE_ILLUMINA_SEQUENCING_AGILENT_KIT.setMandatory(False)
assignment_SAMPLE_ILLUMINA_SEQUENCING_AGILENT_KIT.setSection('LQG: Generation of DNA library')
assignment_SAMPLE_ILLUMINA_SEQUENCING_AGILENT_KIT.setPositionInForms(94)

assignment_SAMPLE_ILLUMINA_SEQUENCING_LIBRARY_PROCESSING_SUCCESSFUL = tr.assignPropertyType(samp_type_ILLUMINA_SEQUENCING, prop_type_LIBRARY_PROCESSING_SUCCESSFUL)
assignment_SAMPLE_ILLUMINA_SEQUENCING_LIBRARY_PROCESSING_SUCCESSFUL.setMandatory(False)
assignment_SAMPLE_ILLUMINA_SEQUENCING_LIBRARY_PROCESSING_SUCCESSFUL.setSection('LQG: Generation of DNA library')
assignment_SAMPLE_ILLUMINA_SEQUENCING_LIBRARY_PROCESSING_SUCCESSFUL.setPositionInForms(95)

assignment_SAMPLE_ILLUMINA_SEQUENCING_LIBRARY_PROCESSING_FAILED = tr.assignPropertyType(samp_type_ILLUMINA_SEQUENCING, prop_type_LIBRARY_PROCESSING_FAILED)
assignment_SAMPLE_ILLUMINA_SEQUENCING_LIBRARY_PROCESSING_FAILED.setMandatory(False)
assignment_SAMPLE_ILLUMINA_SEQUENCING_LIBRARY_PROCESSING_FAILED.setSection('LQG: Generation of DNA library')
assignment_SAMPLE_ILLUMINA_SEQUENCING_LIBRARY_PROCESSING_FAILED.setPositionInForms(96)

assignment_SAMPLE_ILLUMINA_SEQUENCING_CLUSTER_STATION = tr.assignPropertyType(samp_type_ILLUMINA_SEQUENCING, prop_type_CLUSTER_STATION)
assignment_SAMPLE_ILLUMINA_SEQUENCING_CLUSTER_STATION.setMandatory(False)
assignment_SAMPLE_ILLUMINA_SEQUENCING_CLUSTER_STATION.setSection('LQG: Clustering Process')
assignment_SAMPLE_ILLUMINA_SEQUENCING_CLUSTER_STATION.setPositionInForms(102)

assignment_SAMPLE_ILLUMINA_SEQUENCING_CREATED_ON_CS = tr.assignPropertyType(samp_type_ILLUMINA_SEQUENCING, prop_type_CREATED_ON_CS)
assignment_SAMPLE_ILLUMINA_SEQUENCING_CREATED_ON_CS.setMandatory(False)
assignment_SAMPLE_ILLUMINA_SEQUENCING_CREATED_ON_CS.setSection('LQG: Clustering Process')
assignment_SAMPLE_ILLUMINA_SEQUENCING_CREATED_ON_CS.setPositionInForms(103)

assignment_SAMPLE_ILLUMINA_SEQUENCING_CS_PROTOCOL_VERSION = tr.assignPropertyType(samp_type_ILLUMINA_SEQUENCING, prop_type_CS_PROTOCOL_VERSION)
assignment_SAMPLE_ILLUMINA_SEQUENCING_CS_PROTOCOL_VERSION.setMandatory(False)
assignment_SAMPLE_ILLUMINA_SEQUENCING_CS_PROTOCOL_VERSION.setSection('LQG: Clustering Process')
assignment_SAMPLE_ILLUMINA_SEQUENCING_CS_PROTOCOL_VERSION.setPositionInForms(104)

assignment_SAMPLE_ILLUMINA_SEQUENCING_CLUSTER_GENERATION_KIT_VERSION = tr.assignPropertyType(samp_type_ILLUMINA_SEQUENCING, prop_type_CLUSTER_GENERATION_KIT_VERSION)
assignment_SAMPLE_ILLUMINA_SEQUENCING_CLUSTER_GENERATION_KIT_VERSION.setMandatory(False)
assignment_SAMPLE_ILLUMINA_SEQUENCING_CLUSTER_GENERATION_KIT_VERSION.setSection('LQG: Clustering Process')
assignment_SAMPLE_ILLUMINA_SEQUENCING_CLUSTER_GENERATION_KIT_VERSION.setPositionInForms(105)

assignment_SAMPLE_ILLUMINA_SEQUENCING_CLUSTER_STATION_SOFTWARE_VERSION = tr.assignPropertyType(samp_type_ILLUMINA_SEQUENCING, prop_type_CLUSTER_STATION_SOFTWARE_VERSION)
assignment_SAMPLE_ILLUMINA_SEQUENCING_CLUSTER_STATION_SOFTWARE_VERSION.setMandatory(False)
assignment_SAMPLE_ILLUMINA_SEQUENCING_CLUSTER_STATION_SOFTWARE_VERSION.setSection('LQG: Clustering Process')
assignment_SAMPLE_ILLUMINA_SEQUENCING_CLUSTER_STATION_SOFTWARE_VERSION.setPositionInForms(106)

assignment_SAMPLE_ILLUMINA_SEQUENCING_CONCENTRATION_FLOWLANE = tr.assignPropertyType(samp_type_ILLUMINA_SEQUENCING, prop_type_CONCENTRATION_FLOWLANE)
assignment_SAMPLE_ILLUMINA_SEQUENCING_CONCENTRATION_FLOWLANE.setMandatory(False)
assignment_SAMPLE_ILLUMINA_SEQUENCING_CONCENTRATION_FLOWLANE.setSection('LQG: Clustering Process')
assignment_SAMPLE_ILLUMINA_SEQUENCING_CONCENTRATION_FLOWLANE.setPositionInForms(107)

assignment_SAMPLE_ILLUMINA_SEQUENCING_CYCLES = tr.assignPropertyType(samp_type_ILLUMINA_SEQUENCING, prop_type_CYCLES)
assignment_SAMPLE_ILLUMINA_SEQUENCING_CYCLES.setMandatory(False)
assignment_SAMPLE_ILLUMINA_SEQUENCING_CYCLES.setSection('LQG: Sequencing')
assignment_SAMPLE_ILLUMINA_SEQUENCING_CYCLES.setPositionInForms(108)

assignment_SAMPLE_ILLUMINA_SEQUENCING_SCS_PROTOCOL_VERSION = tr.assignPropertyType(samp_type_ILLUMINA_SEQUENCING, prop_type_SCS_PROTOCOL_VERSION)
assignment_SAMPLE_ILLUMINA_SEQUENCING_SCS_PROTOCOL_VERSION.setMandatory(False)
assignment_SAMPLE_ILLUMINA_SEQUENCING_SCS_PROTOCOL_VERSION.setSection('LQG: Sequencing')
assignment_SAMPLE_ILLUMINA_SEQUENCING_SCS_PROTOCOL_VERSION.setPositionInForms(110)

assignment_SAMPLE_ILLUMINA_SEQUENCING_SBS_SEQUENCING_KIT_VERSION = tr.assignPropertyType(samp_type_ILLUMINA_SEQUENCING, prop_type_SBS_SEQUENCING_KIT_VERSION)
assignment_SAMPLE_ILLUMINA_SEQUENCING_SBS_SEQUENCING_KIT_VERSION.setMandatory(False)
assignment_SAMPLE_ILLUMINA_SEQUENCING_SBS_SEQUENCING_KIT_VERSION.setSection('LQG: Sequencing')
assignment_SAMPLE_ILLUMINA_SEQUENCING_SBS_SEQUENCING_KIT_VERSION.setPositionInForms(111)

assignment_SAMPLE_ILLUMINA_SEQUENCING_SCS_SOFTWARE_VERSION = tr.assignPropertyType(samp_type_ILLUMINA_SEQUENCING, prop_type_SCS_SOFTWARE_VERSION)
assignment_SAMPLE_ILLUMINA_SEQUENCING_SCS_SOFTWARE_VERSION.setMandatory(False)
assignment_SAMPLE_ILLUMINA_SEQUENCING_SCS_SOFTWARE_VERSION.setSection('LQG: Sequencing')
assignment_SAMPLE_ILLUMINA_SEQUENCING_SCS_SOFTWARE_VERSION.setPositionInForms(112)

assignment_SAMPLE_ILLUMINA_SEQUENCING_BAREBACKED = tr.assignPropertyType(samp_type_ILLUMINA_SEQUENCING, prop_type_BAREBACKED)
assignment_SAMPLE_ILLUMINA_SEQUENCING_BAREBACKED.setMandatory(False)
assignment_SAMPLE_ILLUMINA_SEQUENCING_BAREBACKED.setSection('LQG: Sequencing')
assignment_SAMPLE_ILLUMINA_SEQUENCING_BAREBACKED.setPositionInForms(113)

assignment_SAMPLE_ILLUMINA_SEQUENCING_INVOICE = tr.assignPropertyType(samp_type_ILLUMINA_SEQUENCING, prop_type_INVOICE)
assignment_SAMPLE_ILLUMINA_SEQUENCING_INVOICE.setMandatory(False)
assignment_SAMPLE_ILLUMINA_SEQUENCING_INVOICE.setSection('LQG: Administrative')
assignment_SAMPLE_ILLUMINA_SEQUENCING_INVOICE.setPositionInForms(117)

assignment_SAMPLE_ILLUMINA_SEQUENCING_NOTES = tr.assignPropertyType(samp_type_ILLUMINA_SEQUENCING, prop_type_NOTES)
assignment_SAMPLE_ILLUMINA_SEQUENCING_NOTES.setMandatory(False)
assignment_SAMPLE_ILLUMINA_SEQUENCING_NOTES.setSection('LQG: Administrative')
assignment_SAMPLE_ILLUMINA_SEQUENCING_NOTES.setPositionInForms(118)

assignment_SAMPLE_ILLUMINA_SEQUENCING_NUMBER_OF_ATTACHMENTS = tr.assignPropertyType(samp_type_ILLUMINA_SEQUENCING, prop_type_NUMBER_OF_ATTACHMENTS)
assignment_SAMPLE_ILLUMINA_SEQUENCING_NUMBER_OF_ATTACHMENTS.setMandatory(False)
assignment_SAMPLE_ILLUMINA_SEQUENCING_NUMBER_OF_ATTACHMENTS.setSection(None)
assignment_SAMPLE_ILLUMINA_SEQUENCING_NUMBER_OF_ATTACHMENTS.setPositionInForms(119)

assignment_DATA_SET_MACS_OUTPUT_MACS_VERSION = tr.assignPropertyType(data_set_type_MACS_OUTPUT, prop_type_MACS_VERSION)
assignment_DATA_SET_MACS_OUTPUT_MACS_VERSION.setMandatory(False)
assignment_DATA_SET_MACS_OUTPUT_MACS_VERSION.setSection(None)
assignment_DATA_SET_MACS_OUTPUT_MACS_VERSION.setPositionInForms(1)

assignment_DATA_SET_MACS_OUTPUT_NOTES = tr.assignPropertyType(data_set_type_MACS_OUTPUT, prop_type_NOTES)
assignment_DATA_SET_MACS_OUTPUT_NOTES.setMandatory(False)
assignment_DATA_SET_MACS_OUTPUT_NOTES.setSection(None)
assignment_DATA_SET_MACS_OUTPUT_NOTES.setPositionInForms(2)
