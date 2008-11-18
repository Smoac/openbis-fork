/*
 * Copyright 2008 ETH Zuerich, CISD
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
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.hibernate.annotations.Type;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.FieldBridge;
import org.hibernate.search.annotations.Index;
import org.hibernate.search.annotations.Store;
import org.hibernate.validator.NotNull;

import ch.systemsx.cisd.openbis.generic.shared.GenericSharedConstants;

/**
 * An attachment content <i>Persistent Entity</i>.
 * 
 * @author Christian Ribeaud
 */
@Entity
@Table(name = TableNames.EXPERIMENT_ATTACHMENT_CONTENT_TABLE)
public class AttachmentContentPE implements Serializable
{
    private static final long serialVersionUID = GenericSharedConstants.VERSION;

    private byte[] value;

    transient private Long id;

    @SequenceGenerator(name = SequenceNames.EXPERIMENT_ATTACHMENT_CONTENT_SEQUENCE, sequenceName = SequenceNames.EXPERIMENT_ATTACHMENT_CONTENT_SEQUENCE, allocationSize = 1)
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = SequenceNames.EXPERIMENT_ATTACHMENT_CONTENT_SEQUENCE)
    public final Long getId()
    {
        return id;
    }

    public final void setId(final Long id)
    {
        this.id = id;
    }

    /**
     * Returns bytes blob stored in the attachment.
     */
    @Column(name = ColumnNames.VALUE_COLUMN, updatable = false)
    @NotNull(message = ValidationMessages.VALUE_NOT_NULL_MESSAGE)
    @Type(type = "org.springframework.orm.hibernate3.support.BlobByteArrayType")
    @Field(bridge = @FieldBridge(impl = ByteArrayBridge.class), index = Index.TOKENIZED, store = Store.NO)
    public byte[] getValue()
    {
        return value;
    }

    public void setValue(final byte[] value)
    {
        this.value = value;
    }
}
