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

import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.hibernate.validator.NotNull;

import ch.systemsx.cisd.common.utilities.ClassUtils;
import ch.systemsx.cisd.openbis.generic.shared.GenericSharedConstants;
import ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityKind;

/**
 * Persistence Entity representing entity type.
 * <p>
 * Entity is one of: material, sample, experiment
 * </p>
 * 
 * @author Franz-Josef Elmer
 * @author Izabela Adamczyk
 */
@MappedSuperclass
public abstract class EntityTypePE extends AbstractTypePE
{
    private static final long serialVersionUID = GenericSharedConstants.VERSION;

    private DatabaseInstancePE databaseInstance;

    @ManyToOne(fetch = FetchType.EAGER)
    @NotNull(message = ValidationMessages.DATABASE_INSTANCE_NOT_NULL_MESSAGE)
    @JoinColumn(name = ColumnNames.DATABASE_INSTANCE_COLUMN, updatable = false)
    public final DatabaseInstancePE getDatabaseInstance()
    {
        return databaseInstance;
    }

    public final void setDatabaseInstance(final DatabaseInstancePE databaseInstance)
    {
        this.databaseInstance = databaseInstance;
    }

    /**
     * Creates an {@link EntityPropertyPE} from given <var>entityKind</var>.
     */
    public final static <T extends EntityTypePE> T createEntityTypePE(final EntityKind entityKind)
    {
        assert entityKind != null : "Unspecified entity kind.";
        return ClassUtils.createInstance(entityKind.<T> getTypeClass());
    }

    //
    // AbstractTypePE
    //

    @Override
    ToStringBuilder createStringBuilder()
    {
        final ToStringBuilder builder = super.createStringBuilder();
        builder.append("databaseInstance", getDatabaseInstance());
        return builder;
    }

}
