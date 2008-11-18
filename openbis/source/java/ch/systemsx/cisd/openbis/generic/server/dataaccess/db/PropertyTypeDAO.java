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

package ch.systemsx.cisd.openbis.generic.server.dataaccess.db;

import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.SessionFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.support.JdbcAccessor;
import org.springframework.orm.hibernate3.HibernateTemplate;

import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IPropertyTypeDAO;
import ch.systemsx.cisd.openbis.generic.shared.dto.CodeConverter;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatabaseInstancePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PropertyTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityDataType;

/**
 * <i>Data Access Object</i> implementation for {@link PropertyTypePE}.
 * 
 * @author Christian Ribeaud
 */
final class PropertyTypeDAO extends AbstractDAO implements IPropertyTypeDAO
{

    /**
     * This logger does not output any SQL statement. If you want to do so, you had better set an
     * appropriate debugging level for class {@link JdbcAccessor}.
     */
    private static final Logger operationLog =
            LogFactory.getLogger(LogCategory.OPERATION, PropertyTypeDAO.class);

    PropertyTypeDAO(final SessionFactory sessionFactory, final DatabaseInstancePE databaseInstance)
    {
        super(sessionFactory, databaseInstance);
    }

    private final static void checkType(final PropertyTypePE propertyType)
    {
        if (propertyType.isInternalNamespace())
        {
            throw new DataIntegrityViolationException(String.format(
                    "Given code '%s' does not contain '%s' prefix.", propertyType.getCode(),
                    CodeConverter.USER_PROPERTY_PREFIX));
        }
        if (propertyType.getVocabulary() != null)
        {
            if (propertyType.getType() == null
                    || EntityDataType.CONTROLLEDVOCABULARY.equals(propertyType.getType().getCode()) == false)

            {
                throw new DataIntegrityViolationException(String
                        .format("Inconsistent property type: vocabulary specified "
                                + "but type is not 'controlled vocabulary'"));
            }
        }
    }

    //
    // IPropertyTypeDAO
    //

    public final List<DataTypePE> listDataTypes() throws DataAccessException
    {
        final List<DataTypePE> list =
                cast(getHibernateTemplate().find(
                        String.format("from %s dt", DataTypePE.class.getSimpleName())));
        if (operationLog.isDebugEnabled())
        {
            operationLog
                    .debug("listDataTypes(): " + list.size() + " data type(s) have been found.");
        }
        return list;
    }

    public final void createPropertyType(final PropertyTypePE propertyType)
            throws DataAccessException
    {
        assert propertyType != null : "Missing property type.";
        propertyType.getRegistrator().setDatabaseInstance(getDatabaseInstance());
        propertyType.setDatabaseInstance(getDatabaseInstance());
        validatePE(propertyType);
        checkType(propertyType);

        final HibernateTemplate template = getHibernateTemplate();
        template.save(propertyType);
        template.flush();
        if (operationLog.isInfoEnabled())
        {
            operationLog.info("ADD: property type '" + propertyType.getCode() + "'.");
        }
    }

    public final List<PropertyTypePE> listPropertyTypes() throws DataAccessException
    {
        final List<PropertyTypePE> list =
                cast(getHibernateTemplate().find(
                        String.format("from %s v where v.managedInternally = false"
                                + " and v.databaseInstance = ?", PropertyTypePE.class
                                .getSimpleName()), toArray(getDatabaseInstance())));
        if (operationLog.isDebugEnabled())
        {
            operationLog.debug("listPropertyTypes(): " + list.size()
                    + " property type(s) have been found.");
        }
        return list;
    }

    public final Long tryFindPropertyTypeIdByCode(final String propertyTypeCode)
    {
        assert propertyTypeCode != null : "Unspecified property type code";
        try
        {
            final List<Long> ids;
            final String convertedCode = CodeConverter.tryToDatabase(propertyTypeCode);
            final boolean internalNamespace = CodeConverter.isInternalNamespace(propertyTypeCode);
            ids =
                    cast(getHibernateTemplate().find(
                            String.format("select pt.id from %s pt where pt.simpleCode = ? "
                                    + "and pt.databaseInstance = ? and pt.internalNamespace = ?",
                                    PropertyTypePE.class.getSimpleName()),
                            toArray(convertedCode, getDatabaseInstance(), internalNamespace)));
            final Long id = getEntity(ids);
            return id;
        } catch (final EmptyResultDataAccessException e)
        {
            return null;
        }
    }

    public final PropertyTypePE tryFindPropertyTypeByCode(final String code)
            throws DataAccessException
    {
        assert code != null : "Unspecified property type code";
        final List<PropertyTypePE> list =
                cast(getHibernateTemplate().find(
                        String.format("select pt from %s pt where pt.simpleCode = ? "
                                + "and pt.databaseInstance = ? and pt.internalNamespace = ?",
                                PropertyTypePE.class.getSimpleName()),
                        toArray(CodeConverter.tryToDatabase(code), getDatabaseInstance(),
                                CodeConverter.isInternalNamespace(code))));
        final PropertyTypePE entity = tryFindEntity(list, "type");
        if (operationLog.isDebugEnabled())
        {
            operationLog.debug("tryFindPropertyTypeByCode(" + code + "): '" + entity + "'");
        }
        return entity;
    }
}
