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

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Version;

import org.hibernate.annotations.Generated;
import org.hibernate.annotations.GenerationTime;
import org.hibernate.validator.Length;
import org.hibernate.validator.NotNull;
import org.hibernate.validator.Pattern;

import ch.systemsx.cisd.openbis.generic.shared.GenericSharedConstants;

/**
 * A <i>Persistence Entity</i> which represents an entry in {@link TableNames#DATA_STORES_TABLE}.
 * 
 * @author Christian Ribeaud
 */
@Entity
@Table(name = TableNames.DATA_STORES_TABLE)
public final class DataStorePE extends AbstractIdAndCodeHolder<DataStorePE>
{
    private static final long serialVersionUID = GenericSharedConstants.VERSION;

    private transient Long id;

    /**
     * The code of this data store.
     */
    private String code;

    /** Registration date of this data store. */
    private Date registrationDate;

    private String downloadUrl;
    
    private String remoteUrl;
    
    private String sessionToken;

    private DatabaseInstancePE databaseInstance;

    private Date modificationDate;

    public final void setId(final Long id)
    {
        this.id = id;
    }

    public final void setCode(final String code)
    {
        this.code = code;
    }

    @Column(name = ColumnNames.DOWNLOAD_URL_COLUMN, updatable = true)
    @NotNull(message = ValidationMessages.DOWNLOAD_URL_NOT_NULL_MESSAGE)
    public final String getDownloadUrl()
    {
        return downloadUrl;
    }

    public final void setDownloadUrl(final String downloadUrl)
    {
        this.downloadUrl = downloadUrl;
    }

    @Column(name = ColumnNames.REMOTE_URL_COLUMN, updatable = true)
    @NotNull(message = ValidationMessages.REMOTE_URL_NOT_NULL_MESSAGE)
    public final String getRemoteUrl()
    {
        return remoteUrl;
    }

    public final void setRemoteUrl(String remoteUrl)
    {
        this.remoteUrl = remoteUrl;
    }

    @Column(name = ColumnNames.SESSION_TOKEN_COLUMN, updatable = true)
    @NotNull(message = ValidationMessages.SESSION_TOKEN_NOT_NULL_MESSAGE)
    public final String getSessionToken()
    {
        return sessionToken;
    }

    public final void setSessionToken(String sessionToken)
    {
        this.sessionToken = sessionToken;
    }

    @Column(name = ColumnNames.REGISTRATION_TIMESTAMP_COLUMN, nullable = false, insertable = false, updatable = false)
    @Generated(GenerationTime.INSERT)
    public final Date getRegistrationDate()
    {
        return HibernateAbstractRegistrationHolder.getDate(registrationDate);
    }

    public final void setRegistrationDate(final Date registrationDate)
    {
        this.registrationDate = registrationDate;
    }

    @Version
    @Column(name = ColumnNames.MODIFICATION_TIMESTAMP_COLUMN, nullable = false)
    public Date getModificationDate()
    {
        return modificationDate;
    }

    public void setModificationDate(Date versionDate)
    {
        this.modificationDate = versionDate;
    }

    @ManyToOne(fetch = FetchType.EAGER)
    @NotNull(message = ValidationMessages.DATABASE_INSTANCE_NOT_NULL_MESSAGE)
    @JoinColumn(name = ColumnNames.DATABASE_INSTANCE_COLUMN, updatable = false)
    public DatabaseInstancePE getDatabaseInstance()
    {
        return databaseInstance;
    }

    public void setDatabaseInstance(final DatabaseInstancePE databaseInstance)
    {
        this.databaseInstance = databaseInstance;
    }

    //
    // AbstractIdAndCodeHolder
    //

    @Column(name = ColumnNames.CODE_COLUMN)
    @NotNull(message = ValidationMessages.CODE_NOT_NULL_MESSAGE)
    @Length(min = 1, max = 40, message = ValidationMessages.CODE_LENGTH_MESSAGE)
    @Pattern(regex = AbstractIdAndCodeHolder.CODE_PATTERN, flags = java.util.regex.Pattern.CASE_INSENSITIVE, message = ValidationMessages.CODE_PATTERN_MESSAGE)
    public final String getCode()
    {
        return code;
    }

    @Id
    @SequenceGenerator(name = SequenceNames.DATA_STORE_SEQUENCE, sequenceName = SequenceNames.DATA_STORE_SEQUENCE, allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = SequenceNames.DATA_STORE_SEQUENCE)
    public final Long getId()
    {
        return id;
    }

}
