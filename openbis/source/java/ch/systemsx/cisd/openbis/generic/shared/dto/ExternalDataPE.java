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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;

import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.IndexedEmbedded;
import org.hibernate.validator.Length;
import org.hibernate.validator.NotNull;

import ch.systemsx.cisd.common.types.BooleanOrUnknown;
import ch.systemsx.cisd.openbis.generic.shared.IServer;
import ch.systemsx.cisd.openbis.generic.shared.dto.hibernate.Location;
import ch.systemsx.cisd.openbis.generic.shared.dto.hibernate.SearchFieldConstants;

/**
 * Kind of <i>Java Bean</i> or <i>Value Object</i> which contains any information we would like to
 * know about one EXTERNAL DATA.
 * <p>
 * This class is the <i>Java Object</i> representation of the corresponding data in the database.
 * </p>
 * 
 * @author Christian Ribeaud
 */
@Entity
@Table(name = TableNames.EXTERNAL_DATA_TABLE, uniqueConstraints = @UniqueConstraint(columnNames =
    { ColumnNames.LOCATION_COLUMN, ColumnNames.LOCATOR_TYPE_COLUMN }))
@PrimaryKeyJoinColumn(name = ColumnNames.DATA_ID_COLUMN)
@Indexed
public final class ExternalDataPE extends DataPE
{
    private static final long serialVersionUID = IServer.VERSION;

    /** An empty array of <code>ExternalData</code>. */
    @SuppressWarnings("hiding")
    public final static ExternalDataPE[] EMPTY_ARRAY = new ExternalDataPE[0];

    private String location;

    private VocabularyTermPE storageFormatVocabularyTerm;

    private FileFormatTypePE fileFormatType;

    private LocatorTypePE locatorType;

    private BooleanOrUnknown complete = BooleanOrUnknown.U;

    /**
     * Returns the id of the locator type of the location of this external data, or
     * <code>null</code> if not yet set.
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @NotNull(message = ValidationMessages.LOCATOR_TYPE_NOT_NULL_MESSAGE)
    @JoinColumn(name = ColumnNames.LOCATOR_TYPE_COLUMN, updatable = false)
    public final LocatorTypePE getLocatorType()
    {
        return locatorType;
    }

    public void setLocatorType(final LocatorTypePE locatorType)
    {
        this.locatorType = locatorType;
    }

    /** Returns <code>locator</code>. */
    @Column(name = ColumnNames.LOCATION_COLUMN)
    @Length(max = 1024, message = ValidationMessages.LOCATION_LENGTH_MESSAGE)
    @NotNull(message = ValidationMessages.LOCATION_NOT_NULL_MESSAGE)
    @Location(relative = true, message = ValidationMessages.LOCATION_NOT_RELATIVE)
    public String getLocation()
    {
        return location;
    }

    /** Sets <code>locator</code>. */
    public void setLocation(final String location)
    {
        this.location = location;
    }

    @ManyToOne(fetch = FetchType.EAGER)
    @NotNull(message = ValidationMessages.STORAGE_FORMAT_NOT_NULL_MESSAGE)
    @JoinColumn(name = ColumnNames.STORAGE_FORMAT_COLUMN, updatable = false)
    public VocabularyTermPE getStorageFormatVocabularyTerm()
    {
        return storageFormatVocabularyTerm;
    }

    public void setStorageFormatVocabularyTerm(final VocabularyTermPE storageFormatVocabularyTerm)
    {
        this.storageFormatVocabularyTerm = storageFormatVocabularyTerm;
    }

    /**
     * Returns the storage format (proprietary or BDS) of this external data set.
     */
    @Transient
    public StorageFormat getStorageFormat()
    {
        return StorageFormat.tryGetFromCode(storageFormatVocabularyTerm.getCode());
    }

    /** Returns <code>fileFormatType</code>. */
    @ManyToOne(fetch = FetchType.EAGER)
    @NotNull(message = ValidationMessages.FILE_FORMAT_TYPE_NOT_NULL_MESSAGE)
    @JoinColumn(name = ColumnNames.FILE_FORMAT_TYPE, updatable = true)
    @IndexedEmbedded(prefix = SearchFieldConstants.PREFIX_FILE_FORMAT_TYPE)
    public FileFormatTypePE getFileFormatType()
    {
        return fileFormatType;
    }

    /** Sets <code>fileFormatType</code>. */
    public void setFileFormatType(final FileFormatTypePE fileFormatType)
    {
        this.fileFormatType = fileFormatType;
    }

    @NotNull(message = ValidationMessages.IS_COMPLETE_NOT_NULL_MESSAGE)
    @Column(name = ColumnNames.IS_COMPLETE_COLUMN)
    @Enumerated(EnumType.STRING)
    /*
     * * Returns {@link BooleanOrUnknown#T}, if the data set is complete in the data store and
     * {@link BooleanOrUnknown#F}, if some parts of the data are missing. If the completeness is not
     * known (e.g. because the data set is stored in a format that does not allow to assess the
     * completeness, {@link BooleanOrUnknown#U} is returned.
     */
    public BooleanOrUnknown getComplete()
    {
        return complete;
    }

    /**
     * Sets whether this data set is complete in the data store or not. The default is
     * {@link BooleanOrUnknown#U}, which corresponds to the case where the data are stored in a
     * format that does not allow to assess completeness.
     */
    public void setComplete(final BooleanOrUnknown complete)
    {
        this.complete = complete;
    }

}
