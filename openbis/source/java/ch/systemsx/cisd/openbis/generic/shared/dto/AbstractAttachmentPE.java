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

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;
import javax.persistence.SequenceGenerator;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.hibernate.validator.Length;
import org.hibernate.validator.NotNull;

import ch.systemsx.cisd.common.utilities.ModifiedShortPrefixToStringStyle;
import ch.systemsx.cisd.openbis.generic.shared.GenericSharedConstants;
import ch.systemsx.cisd.openbis.generic.shared.util.EqualsHashUtils;

/**
 * Superclass to store attachment. Does not contain attachment content.
 * 
 * @author Christian Ribeaud
 * @author Tomasz Pylak
 */
@MappedSuperclass
public class AbstractAttachmentPE extends HibernateAbstractRegistrationHolder implements
        Serializable, Comparable<AbstractAttachmentPE>
{
    private static final long serialVersionUID = GenericSharedConstants.VERSION;

    private String fileName;

    private int version;

    transient private Long id;

    /**
     * Parent (e.g. an experiment) to which this attachment belongs.
     */
    private ExperimentPE parent;

    /**
     * Returns the file name of the property or <code>null</code>.
     */
    @Column(name = ColumnNames.FILE_NAME_COLUMN)
    @NotNull(message = ValidationMessages.FILE_NAME_NOT_NULL_MESSAGE)
    @Length(max = 100, message = ValidationMessages.FILE_NAME_LENGTH_MESSAGE)
    public String getFileName()
    {
        return fileName;
    }

    public void setFileName(final String fileName)
    {
        this.fileName = fileName;
    }

    @Column(name = ColumnNames.VERSION_COLUMN)
    @NotNull(message = ValidationMessages.VERSION_NOT_NULL_MESSAGE)
    public int getVersion()
    {
        return version;
    }

    public void setVersion(final int version)
    {
        this.version = version;
    }

    @SequenceGenerator(name = SequenceNames.EXPERIMENT_ATTACHMENT_SEQUENCE, sequenceName = SequenceNames.EXPERIMENT_ATTACHMENT_SEQUENCE, allocationSize = 1)
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = SequenceNames.EXPERIMENT_ATTACHMENT_SEQUENCE)
    public final Long getId()
    {
        return id;
    }

    public final void setId(final Long id)
    {
        this.id = id;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @NotNull(message = ValidationMessages.EXPERIMENT_NOT_NULL_MESSAGE)
    @JoinColumn(name = ColumnNames.EXPERIMENT_COLUMN, updatable = false)
    public ExperimentPE getParent()
    {
        return parent;
    }

    public void setParent(final ExperimentPE parent)
    {
        this.parent = parent;
    }

    //
    // Object
    //

    @Override
    public boolean equals(final Object obj)
    {
        EqualsHashUtils.assertDefined(getFileName(), "name");
        EqualsHashUtils.assertDefined(getVersion(), "version");
        if (obj == this)
        {
            return true;
        }
        if (obj instanceof AbstractAttachmentPE == false)
        {
            return false;
        }
        final AbstractAttachmentPE that = (AbstractAttachmentPE) obj;
        final EqualsBuilder builder = new EqualsBuilder();
        builder.append(getFileName(), that.getFileName());
        builder.append(getVersion(), that.getVersion());
        builder.append(getParent(), that.getParent());
        return builder.isEquals();
    }

    @Override
    public int hashCode()
    {
        final HashCodeBuilder builder = new HashCodeBuilder();
        builder.append(getFileName());
        builder.append(getVersion());
        builder.append(getParent());
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

    public final int compareTo(final AbstractAttachmentPE o)
    {
        final int byFile = getFileName().compareTo(o.getFileName());
        return byFile == 0 ? Integer.valueOf(getVersion()).compareTo(o.getVersion()) : byFile;
    }
}