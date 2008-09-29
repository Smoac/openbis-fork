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

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.hibernate.validator.NotNull;

import ch.systemsx.cisd.openbis.generic.shared.GenericSharedConstants;

/**
 * Contains information about an attachment together with its content.
 * 
 * @author Bernd Rinn
 */
@Entity
@Table(name = TableNames.EXPERIMENT_ATTACHMENTS_TABLE, uniqueConstraints =
    { @UniqueConstraint(columnNames =
        { ColumnNames.EXPERIMENT_COLUMN, ColumnNames.FILE_NAME_COLUMN, ColumnNames.VERSION_COLUMN }) })
public class FileAttachmentPE extends AbstractAttachmentPE
{
    private static final long serialVersionUID = GenericSharedConstants.VERSION;

    private byte[] value;

    /**
     * Returns bytes blob stored in the attachment.
     */
    @NotNull(message = ValidationMessages.ATTACHMENT_CONTENT_NOT_NULL_MESSAGE)
    public byte[] getValue()
    {
        return value;
    }

    public void setValue(final byte[] value)
    {
        this.value = value;
    }

    @Override
    public final String toString()
    {
        return getClass().getSimpleName() + "{len(value)="
                + (value != null ? value.length : "<null>") + ",attachment=" + super.toString()
                + "}";
    }
}
