/*
 * Copyright 2007 ETH Zuerich, CISD
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ch.systemsx.cisd.openbis.generic.shared.dto;

import ch.systemsx.cisd.openbis.generic.shared.GenericSharedConstants;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ProjectIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;

/**
 * The full description of a new experiment. Used when registering an experiment to the LIMS.
 * 
 * @author Bernd Rinn
 */
public final class NewExperiment extends AbstractEntity<NewExperiment>
{

    public final static NewExperiment[] EMPTY_ARRAY = new NewExperiment[0];

    private static final long serialVersionUID = GenericSharedConstants.VERSION;

    private ProjectIdentifier projectIdentifier;

    private String studyObjectCode;

    private String studyObjectTypeCode;

    private String experimentTypeCode;

    private String description;

    /**
     * Processing instructions related to this experiment.
     * <p>
     * Processing instructions are procedure type specific.
     * </p>
     */
    private NewProcessingInstruction[] processingInstructions = new NewProcessingInstruction[0];

    /** List of cell plates that are related to this experiment. */
    private SampleIdentifier[] cellPlates = SampleIdentifier.EMPTY_ARRAY;

    public final String getDescription()
    {
        return description;
    }

    public final void setDescription(final String description)
    {
        this.description = description;
    }

    public EntityType getEntityType()
    {
        return new EntityType(experimentTypeCode, "Experiment of type " + experimentTypeCode);
    }

    public final String getExperimentTypeCode()
    {
        return experimentTypeCode;
    }

    public final void setExperimentTypeCode(final String experimentTypeCode)
    {
        this.experimentTypeCode = experimentTypeCode;
    }

    public final String getStudyObjectCode()
    {
        return studyObjectCode;
    }

    public final void setStudyObjectCode(final String studyObjectCode)
    {
        this.studyObjectCode = studyObjectCode;
    }

    public ProjectIdentifier getProjectIdentifier()
    {
        return projectIdentifier;
    }

    public final void setProjectIdentifier(final ProjectIdentifier projectIdentifier)
    {
        this.projectIdentifier = projectIdentifier;
    }

    /** Never returns <code>null</code> but could return an empty array. */
    public final SampleIdentifier[] getCellPlates()
    {
        return cellPlates == null ? SampleIdentifier.EMPTY_ARRAY : cellPlates;
    }

    public final void setCellPlates(final SampleIdentifier[] cellPlateCodes)
    {
        this.cellPlates = cellPlateCodes;
    }

    public final void setProcessingInstructions(
            final NewProcessingInstruction[] processingInstructions)
    {
        this.processingInstructions = processingInstructions;
    }

    /** Never returns <code>null</code> but could return an empty array. */
    public final NewProcessingInstruction[] getProcessingInstructions()
    {
        return processingInstructions == null ? new NewProcessingInstruction[0]
                : processingInstructions;
    }

    public final String getStudyObjectTypeCode()
    {
        return studyObjectTypeCode;
    }

    public final void setStudyObjectTypeCode(final String studyObjectTypeCode)
    {
        this.studyObjectTypeCode = studyObjectTypeCode;
    }

}
