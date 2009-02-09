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

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.hibernate.search.annotations.ClassBridge;
import org.hibernate.search.annotations.Index;
import org.hibernate.search.annotations.Store;
import org.hibernate.search.bridge.FieldBridge;
import org.hibernate.search.bridge.LuceneOptions;
import org.hibernate.validator.Length;
import org.hibernate.validator.NotNull;

import ch.rinn.restrictions.Private;
import ch.systemsx.cisd.common.utilities.ModifiedShortPrefixToStringStyle;
import ch.systemsx.cisd.openbis.generic.shared.GenericSharedConstants;
import ch.systemsx.cisd.openbis.generic.shared.dto.hibernate.SearchFieldConstants;
import ch.systemsx.cisd.openbis.generic.shared.util.EqualsHashUtils;

/**
 * Contains information about an attachment together with its content.
 * 
 * @author Bernd Rinn
 */
@Entity
@Table(name = TableNames.EXPERIMENT_ATTACHMENTS_TABLE, uniqueConstraints =
    { @UniqueConstraint(columnNames =
        { ColumnNames.EXPERIMENT_COLUMN, ColumnNames.FILE_NAME_COLUMN, ColumnNames.VERSION_COLUMN }) })
@ClassBridge(impl = AttachmentPE.AttachmentSearchBridge.class, index = Index.TOKENIZED, store = Store.NO)
public class AttachmentPE extends HibernateAbstractRegistrationHolder implements Serializable,
        Comparable<AttachmentPE>
{
    private static final long serialVersionUID = GenericSharedConstants.VERSION;

    public static final AttachmentPE[] EMPTY_ARRAY = new AttachmentPE[0];

    private String fileName;

    private int version;

    private AttachmentContentPE attachmentContent;

    transient private Long id;

    /**
     * Parent (e.g. an experiment) to which this attachment belongs.
     */
    private ExperimentPE parent;

    /**
     * This bridge allows to save in the search index not only the content of the attachment, but
     * also corresponding file name and version.
     */
    public static class AttachmentSearchBridge implements FieldBridge
    {
        public void set(String name, Object/* AttachmentPE */value,
                Document/* Lucene document */document, LuceneOptions luceneOptions)
        {
            AttachmentPE attachment = (AttachmentPE) value;
            String attachmentName = attachment.getFileName();
            indexFileContent(document, luceneOptions, attachment, attachmentName);

            // index the file name. Make the field code unique, so that we can recognize which field
            // has matched the query later on
            String attachmentNameFieldName =
                    SearchFieldConstants.PREFIX_ATTACHMENT_FILE_NAME + attachmentName;
            Field field =
                    new Field(attachmentNameFieldName, attachmentName, Field.Store.YES,
                            luceneOptions.getIndex());
            document.add(field);
        }

        private void indexFileContent(Document document, LuceneOptions luceneOptions,
                AttachmentPE attachment, String attachmentName)
        {
            String fieldName =
                    SearchFieldConstants.PREFIX_ATTACHMENT + attachmentName + ", ver. "
                            + attachment.getVersion();
            byte[] byteContent = attachment.getAttachmentContent().getValue();
            String fieldValue = new String(byteContent);
            Field field =
                    new Field(fieldName, fieldValue, luceneOptions.getStore(), luceneOptions
                            .getIndex());
            if (luceneOptions.getBoost() != null)
            {
                field.setBoost(luceneOptions.getBoost());
            }
            document.add(field);
        }
    }

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
    @Private
    // for Hibernate and bean conversion only
    public ExperimentPE getParentInternal()
    {
        return parent;
    }

    @Private
    // for Hibernate and bean conversion only
    public void setParentInternal(final ExperimentPE parent)
    {
        this.parent = parent;
    }

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @NotNull(message = ValidationMessages.ATTACHMENT_CONTENT_NOT_NULL_MESSAGE)
    @JoinColumn(name = ColumnNames.EXPERIMENT_ATTACHMENT_CONTENT_COLUMN, updatable = false)
    public AttachmentContentPE getAttachmentContent()
    {
        return attachmentContent;
    }

    public void setAttachmentContent(final AttachmentContentPE attachmentContent)
    {
        this.attachmentContent = attachmentContent;
    }

    @Override
    public final boolean equals(final Object obj)
    {
        EqualsHashUtils.assertDefined(getFileName(), "name");
        EqualsHashUtils.assertDefined(getVersion(), "version");
        if (obj == this)
        {
            return true;
        }
        if (obj instanceof AttachmentPE == false)
        {
            return false;
        }
        final AttachmentPE that = (AttachmentPE) obj;
        final EqualsBuilder builder = new EqualsBuilder();
        builder.append(getFileName(), that.getFileName());
        builder.append(getVersion(), that.getVersion());
        builder.append(getParentInternal(), that.getParentInternal());
        return builder.isEquals();
    }

    @Override
    public final int hashCode()
    {
        final HashCodeBuilder builder = new HashCodeBuilder();
        builder.append(getFileName());
        builder.append(getVersion());
        builder.append(getParentInternal());
        return builder.toHashCode();
    }

    @Override
    public final String toString()
    {
        final ToStringBuilder builder =
                new ToStringBuilder(this,
                        ModifiedShortPrefixToStringStyle.MODIFIED_SHORT_PREFIX_STYLE);
        builder.append("fileName", getFileName());
        builder.append("version", getVersion());
        builder.append("parent", getParentInternal());
        return builder.toString();
    }

    //
    // Comparable
    //

    public final int compareTo(final AttachmentPE o)
    {
        final int byFile = getFileName().compareTo(o.getFileName());
        return byFile == 0 ? Integer.valueOf(getVersion()).compareTo(o.getVersion()) : byFile;
    }

}
