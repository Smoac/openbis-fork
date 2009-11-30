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
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.Generated;
import org.hibernate.annotations.GenerationTime;
import org.hibernate.validator.Length;
import org.hibernate.validator.NotNull;
import org.hibernate.validator.Pattern;

import ch.systemsx.cisd.openbis.generic.shared.IServer;

/**
 * A <i>Persistence Entity</i> which represents a database instance.
 * 
 * @author Christian Ribeaud
 */
@Entity
@Table(name = TableNames.DATABASE_INSTANCES_TABLE)
public final class DatabaseInstancePE extends AbstractIdAndCodeHolder<DatabaseInstancePE>
{
    private static final long serialVersionUID = IServer.VERSION;

    public final static String SYSTEM_DEFAULT = "SYSTEM_DEFAULT";

    public final static DatabaseInstancePE[] EMPTY_ARRAY = new DatabaseInstancePE[0];

    private transient Long id;

    /** Registration date of the database instance. */
    private Date registrationDate;

    /**
     * The local code of this database instance.
     * <p>
     * If <code>SYSTEM_DEFAULT</code>, then {@link #isSystemDefault()} will return <code>true</code>
     * .
     * </p>
     */
    private String code;

    /**
     * The global unique code of this database instance (UUID).
     */
    private String uuid;

    private boolean isHomeDatabase;

    /** Whether this database instance should get a <i>brand</i>. */
    private boolean systemDefault;

    private final void setSystemDefault(final boolean systemDefault)
    {
        this.systemDefault = systemDefault;
    }

    /**
     * Whether given <var>code</var> is <code>SYSTEM_DEFAULT</code> code.
     */
    public final static boolean isSystemDefault(final String code)
    {
        return SYSTEM_DEFAULT.equals(code);
    }

    @Column(name = ColumnNames.REGISTRATION_TIMESTAMP_COLUMN, nullable = false, insertable = false, updatable = false)
    @Generated(GenerationTime.INSERT)
    public final Date getRegistrationDate()
    {
        return HibernateAbstractRegistrationHolder.getDate(registrationDate);
    }

    public void setRegistrationDate(final Date registrationDate)
    {
        this.registrationDate = registrationDate;
    }

    public final void setCode(final String code)
    {
        this.code = code;
        setSystemDefault(isSystemDefault(code));
    }

    @Column(name = ColumnNames.UUID_COLUMN)
    @NotNull(message = ValidationMessages.UUID_NOT_NULL_MESSAGE)
    @Length(min = 1, max = Code.CODE_LENGTH_MAX, message = ValidationMessages.CODE_LENGTH_MESSAGE)
    @Pattern(regex = AbstractIdAndCodeHolder.CODE_PATTERN, flags = java.util.regex.Pattern.CASE_INSENSITIVE, message = ValidationMessages.CODE_PATTERN_MESSAGE)
    // Do not call this method 'getUUID' or Hibernate will complain!
    public String getUuid()
    {
        return uuid;
    }

    // Do not call this method 'setUUID' or Hibernate will complain!
    public void setUuid(final String uuid)
    {
        this.uuid = uuid;
    }

    @Transient
    public final boolean isSystemDefault()
    {
        return systemDefault;
    }

    @Column(name = ColumnNames.IS_ORIGINAL_SOURCE_COLUMN, nullable = false)
    public final boolean isOriginalSource()
    {
        return isHomeDatabase;
    }

    public final void setOriginalSource(final boolean isHomeDatabase)
    {
        this.isHomeDatabase = isHomeDatabase;
    }

    public final void setId(final Long id)
    {
        this.id = id;
    }

    //
    // AbstractIdAndCodeHolder
    //

    @Id
    @SequenceGenerator(name = SequenceNames.DATABASE_INSTANCE_SEQUENCE, sequenceName = SequenceNames.DATABASE_INSTANCE_SEQUENCE, allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = SequenceNames.DATABASE_INSTANCE_SEQUENCE)
    public final Long getId()
    {
        return id;
    }

    @Column(unique = true)
    @NotNull(message = ValidationMessages.CODE_NOT_NULL_MESSAGE)
    @Length(min = 1, max = Code.CODE_LENGTH_MAX, message = ValidationMessages.CODE_LENGTH_MESSAGE)
    @Pattern(regex = AbstractIdAndCodeHolder.CODE_PATTERN, flags = java.util.regex.Pattern.CASE_INSENSITIVE, message = ValidationMessages.CODE_PATTERN_MESSAGE)
    public final String getCode()
    {
        return code;
    }
}
