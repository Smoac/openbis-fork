/*
 * Copyright 2009 ETH Zuerich, CISD
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ch.systemsx.cisd.phosphonetx.dto;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import ch.systemsx.cisd.phosphonetx.Constants;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
@XmlType
public class ProteinSummaryHeader
{
    private String referenceDatabase;
    private String winReferenceDatabase;
    private String residueSubstitutions;
    private String organism;
    private String sourceFiles;
    private String sourceFilesAlternative;
    private String winSourceFiles;
    private String sourceFileType;
    private double minimumPeptideProbability;
    private double minimumPeptideWeight;
    private double numberOfPredictedCorrectProteins;
    private int numberOfInput1Spectra;
    private int numberOfInput2Spectra;
    private int numberOfInput3Spectra;
    private double minimumInitialPeptideProbability;
    private double totalEstimatedNumberOfCorrectPeptideAssignments;
    private String sampleEnzyme;
    private ProgramDetails programDetails;

    @XmlAttribute(name = "reference_database", required = true)
    public final String getReferenceDatabase()
    {
        return referenceDatabase;
    }

    public final void setReferenceDatabase(String referenceDatabase)
    {
        this.referenceDatabase = referenceDatabase;
    }

    @XmlAttribute(name = "win-cyg_reference_database")
    public final String getWinReferenceDatabase()
    {
        return winReferenceDatabase;
    }

    public final void setWinReferenceDatabase(String winReferenceDatabase)
    {
        this.winReferenceDatabase = winReferenceDatabase;
    }

    @XmlAttribute(name = "residue_substitution_list")
    public final String getResidueSubstitutions()
    {
        return residueSubstitutions;
    }

    public final void setResidueSubstitutions(String residueSubstitutions)
    {
        this.residueSubstitutions = residueSubstitutions;
    }

    @XmlAttribute(name = "organism")
    public final String getOrganism()
    {
        return organism;
    }

    public final void setOrganism(String organism)
    {
        this.organism = organism;
    }

    @XmlAttribute(name = "source_files", required = true)
    public final String getSourceFiles()
    {
        return sourceFiles;
    }

    public final void setSourceFiles(String sourceFiles)
    {
        this.sourceFiles = sourceFiles;
    }

    @XmlAttribute(name = "win-cyg_source_files")
    public final String getWinSourceFiles()
    {
        return winSourceFiles;
    }

    public final void setWinSourceFiles(String winSourceFiles)
    {
        this.winSourceFiles = winSourceFiles;
    }

    @XmlAttribute(name = "source_files_alt", required = true)
    public final String getSourceFilesAlternative()
    {
        return sourceFilesAlternative;
    }

    public final void setSourceFilesAlternative(String sourceFilesAlternative)
    {
        this.sourceFilesAlternative = sourceFilesAlternative;
    }

    @XmlAttribute(name = "source_file_xtn")
    public final String getSourceFileType()
    {
        return sourceFileType;
    }

    public final void setSourceFileType(String sourceFileType)
    {
        this.sourceFileType = sourceFileType;
    }

    @XmlAttribute(name = "min_peptide_probability", required = true)
    public final double getMinimumPeptideProbability()
    {
        return minimumPeptideProbability;
    }

    public final void setMinimumPeptideProbability(double minimumPeptideProbability)
    {
        this.minimumPeptideProbability = minimumPeptideProbability;
    }

    @XmlAttribute(name = "min_peptide_weight", required = true)
    public final double getMinimumPeptideWeight()
    {
        return minimumPeptideWeight;
    }

    public final void setMinimumPeptideWeight(double minimumPeptideWeight)
    {
        this.minimumPeptideWeight = minimumPeptideWeight;
    }

    @XmlAttribute(name = "num_predicted_correct_prots", required = true)
    public final double getNumberOfPredictedCorrectProteins()
    {
        return numberOfPredictedCorrectProteins;
    }

    public final void setNumberOfPredictedCorrectProteins(double numberOfPredictedCorrectProteins)
    {
        this.numberOfPredictedCorrectProteins = numberOfPredictedCorrectProteins;
    }

    @XmlAttribute(name = "num_input_1_spectra", required = true)
    public final int getNumberOfInput1Spectra()
    {
        return numberOfInput1Spectra;
    }

    public final void setNumberOfInput1Spectra(int numberOfInput1Spectra)
    {
        this.numberOfInput1Spectra = numberOfInput1Spectra;
    }

    @XmlAttribute(name = "num_input_2_spectra", required = true)
    public final int getNumberOfInput2Spectra()
    {
        return numberOfInput2Spectra;
    }

    public final void setNumberOfInput2Spectra(int numberOfInput2Spectra)
    {
        this.numberOfInput2Spectra = numberOfInput2Spectra;
    }

    @XmlAttribute(name = "num_input_3_spectra", required = true)
    public final int getNumberOfInput3Spectra()
    {
        return numberOfInput3Spectra;
    }

    public final void setNumberOfInput3Spectra(int numberOfInput3Spectra)
    {
        this.numberOfInput3Spectra = numberOfInput3Spectra;
    }

    @XmlAttribute(name = "initial_min_peptide_prob", required = true)
    public final double getMinimumInitialPeptideProbability()
    {
        return minimumInitialPeptideProbability;
    }

    public final void setMinimumInitialPeptideProbability(double minimumInitialPeptideProbability)
    {
        this.minimumInitialPeptideProbability = minimumInitialPeptideProbability;
    }

    @XmlAttribute(name = "total_no_spectrum_ids")
    public final double getTotalEstimatedNumberOfCorrectPeptideAssignments()
    {
        return totalEstimatedNumberOfCorrectPeptideAssignments;
    }

    public final void setTotalEstimatedNumberOfCorrectPeptideAssignments(
            double totalEstimatedNumberOfCorrectPeptideAssignments)
    {
        this.totalEstimatedNumberOfCorrectPeptideAssignments =
                totalEstimatedNumberOfCorrectPeptideAssignments;
    }

    @XmlAttribute(name = "sample_enzyme", required = true)
    public final String getSampleEnzyme()
    {
        return sampleEnzyme;
    }

    public final void setSampleEnzyme(String sampleEnzyme)
    {
        this.sampleEnzyme = sampleEnzyme;
    }

    @XmlElement(name = "program_details", namespace = Constants.NAMESPACE)
    public final ProgramDetails getProgramDetails()
    {
        return programDetails;
    }

    public final void setProgramDetails(ProgramDetails programDetails)
    {
        this.programDetails = programDetails;
    }
    
    
}
