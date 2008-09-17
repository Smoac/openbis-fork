/*
 * Copyright 2007 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.shared.dto;

import java.util.Date;

import org.apache.commons.lang.builder.ToStringBuilder;

import ch.systemsx.cisd.common.utilities.ModifiedShortPrefixToStringStyle;
import ch.systemsx.cisd.openbis.generic.shared.GenericSharedConstants;

/**
 * A basic <code>Experiment</code> object without procedures, samples, and data.
 * 
 * @author Christian Ribeaud
 */
public final class BaseExperiment extends AbstractEntity<BaseExperiment>
{
    private static final long serialVersionUID = GenericSharedConstants.VERSION;

    private ExperimentType experimentType;

    private Date registrationDate;

    private PersonPE registrator;

    private ProjectPE project;

    private NewMaterial studyObject;

    private ProcessingInstructionDTO[] processingInstructions;

    private boolean invalidated;

    private Date lastDataSetDate;

    public final boolean isInvalidated()
    {
        return invalidated;
    }

    public final void setInvalidated(final boolean invalidated)
    {
        this.invalidated = invalidated;
    }

    public final ProcessingInstructionDTO[] getProcessingInstructions()
    {
        return processingInstructions;
    }

    public final void setProcessingInstructions(
            final ProcessingInstructionDTO[] processingInstructions)
    {
        this.processingInstructions = processingInstructions;
    }

    public final NewMaterial getStudyObject()
    {
        return studyObject;
    }

    public final void setStudyObject(final NewMaterial material)
    {
        this.studyObject = material;
    }

    public final Date getLastDataSetDate()
    {
        return lastDataSetDate;
    }

    public final void setLastDataSetDate(final Date lastDataSetDate)
    {
        this.lastDataSetDate = lastDataSetDate;
    }

    public Date getRegistrationDate()
    {
        return registrationDate;
    }

    public void setRegistrationDate(final Date registrationDate)
    {
        this.registrationDate = registrationDate;
    }

    public PersonPE getRegistrator()
    {
        return registrator;
    }

    public void setRegistrator(final PersonPE registrator)
    {
        this.registrator = registrator;
    }

    public final ProjectPE getProject()
    {
        return project;
    }

    public final void setProject(final ProjectPE project)
    {
        this.project = project;
    }

    public final ExperimentType getExperimentType()
    {
        return experimentType;
    }

    public final void setExperimentType(final ExperimentType experimentType)
    {
        this.experimentType = experimentType;
    }

    //
    // AbstractEntity
    //

    @Override
    public final String toString()
    {
        return ToStringBuilder.reflectionToString(this,
                ModifiedShortPrefixToStringStyle.MODIFIED_SHORT_PREFIX_STYLE);
    }

    public final EntityType getEntityType()
    {
        return getExperimentType();
    }

}