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

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

import ch.systemsx.cisd.common.utilities.ModifiedShortPrefixToStringStyle;
import ch.systemsx.cisd.openbis.generic.shared.GenericSharedConstants;

/**
 * The full description of a experiment.
 * 
 * @author Christian Ribeaud
 */
public final class Experiment extends AbstractEntity<Experiment>
{
    private static final long serialVersionUID = GenericSharedConstants.VERSION;

    private NewMaterial studyObject;

    private PersonPE registrator;

    private Date registrationDate;

    private ExperimentType experimentType;

    private AttachmentPE[] attachments = new AttachmentPE[0];

    private Procedure[] procedures = new Procedure[0];

    private ProjectPE project;

    private ProcessingInstructionDTO[] processingInstructions = ProcessingInstructionDTO.EMPTY_ARRAY;

    private InvalidationPE invalidation;

    public final NewMaterial getStudyObject()
    {
        return studyObject;
    }

    public final void setStudyObject(final NewMaterial studyObject)
    {
        this.studyObject = studyObject;
    }

    public final PersonPE getRegistrator()
    {
        return registrator;
    }

    public final void setRegistrator(final PersonPE person)
    {
        this.registrator = person;
    }

    public final Date getRegistrationDate()
    {
        return registrationDate;
    }

    public final void setRegistrationDate(final Date registrationDate)
    {
        this.registrationDate = registrationDate;
    }

    public final ExperimentType getExperimentType()
    {
        return experimentType;
    }

    public final void setExperimentType(final ExperimentType experimentType)
    {
        this.experimentType = experimentType;
    }

    /**
     * Returns the descriptions of all {@link FileAttachmentPE}s that are stored for this
     * experiment. If there are no such properties, the array will be empty (have length 0).
     */
    public final AttachmentPE[] getAttachments()
    {
        if (attachments == null)
        {
            return new AttachmentPE[0];
        }
        return attachments;
    }

    /**
     * Sets the descriptions of all {@link FileAttachmentPE}s.
     */
    public final void setAttachments(final AttachmentPE[] attachments)
    {
        this.attachments = attachments;
    }

    /**
     * Sets procedures.
     */
    public final void setProcedures(final Procedure[] procedures)
    {
        this.procedures = procedures;
    }

    /**
     * Returns procedures. Never returns <code>null</code> but could return an empty array.
     */
    public final Procedure[] getProcedures()
    {
        if (procedures == null)
        {
            return new Procedure[0];
        }
        return procedures;
    }

    public final ProjectPE getProject()
    {
        return project;
    }

    public final void setProject(final ProjectPE project)
    {
        this.project = project;
    }

    public final ProcessingInstructionDTO[] getProcessingInstructions()
    {
        if (processingInstructions == null)
        {
            return ProcessingInstructionDTO.EMPTY_ARRAY;
        }
        return processingInstructions;
    }

    public final void setProcessingInstructions(final ProcessingInstructionDTO[] processingInstructions)
    {
        this.processingInstructions = processingInstructions;
    }

    public final InvalidationPE getInvalidation()
    {
        return invalidation;
    }

    public final void setInvalidation(final InvalidationPE invalidation)
    {
        this.invalidation = invalidation;
    }

    //
    // Object
    //

    @Override
    public final boolean equals(final Object obj)
    {
        if (obj == this)
        {
            return true;
        }
        if (obj instanceof Experiment == false)
        {
            return false;
        }
        final Experiment that = (Experiment) obj;
        final EqualsBuilder builder = new EqualsBuilder();
        builder.append(that.project, project);
        builder.append(that.getCode(), getCode());
        return builder.isEquals();
    }

    @Override
    public final int hashCode()
    {
        final HashCodeBuilder builder = new HashCodeBuilder();
        builder.append(project);
        builder.append(getCode());
        return builder.toHashCode();
    }

    @Override
    public String toString()
    {
        return ToStringBuilder.reflectionToString(this,
                ModifiedShortPrefixToStringStyle.MODIFIED_SHORT_PREFIX_STYLE);
    }

    //
    // Comparable
    //

    /**
     * If <code>null</code> values are present for <code>code</code>, then they come first.
     */
    @Override
    public final int compareTo(final Experiment o)
    {
        final String thatCode = o.getCode();
        if (getCode() == null)
        {
            return thatCode == null ? 0 : -1;
        }
        if (thatCode == null)
        {
            return 1;
        }
        return getCode().compareTo(thatCode);
    }

    public final EntityType getEntityType()
    {
        return getExperimentType();
    }
}
