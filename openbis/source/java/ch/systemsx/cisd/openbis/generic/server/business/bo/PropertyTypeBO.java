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

package ch.systemsx.cisd.openbis.generic.server.business.bo;

import org.springframework.dao.DataAccessException;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.PropertyType;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PropertyTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;
import ch.systemsx.cisd.openbis.generic.shared.dto.VocabularyPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityDataType;

/**
 * The only productive implementation of {@link IPropertyTypeBO}.
 * 
 * @author Christian Ribeaud
 */
public final class PropertyTypeBO extends AbstractVocabularyBusinessObject implements
        IPropertyTypeBO
{
    private PropertyTypePE propertyTypePE;

    public PropertyTypeBO(final IDAOFactory daoFactory, final Session session)
    {
        super(daoFactory, session);
    }

    //
    // AbstractVocabularyBusinessObject
    //

    public final void define(final PropertyType propertyType) throws UserFailureException
    {
        propertyTypePE = new PropertyTypePE();
        propertyTypePE.setDatabaseInstance(getHomeDatabaseInstance());
        propertyTypePE.setCode(propertyType.getCode());
        propertyTypePE.setLabel(propertyType.getLabel());
        propertyTypePE.setDescription(propertyType.getDescription());
        final String dataTypeCode = propertyType.getDataType().getCode().name();
        DataTypePE dataTypePE = null;
        try
        {
            dataTypePE =
                    getPropertyTypeDAO().getDataTypeByCode(EntityDataType.valueOf(dataTypeCode));
        } catch (final IllegalArgumentException e)
        {
            throw UserFailureException.fromTemplate(String.format("Unknow data type code '%s'."),
                    dataTypeCode);
        }
        assert dataTypePE != null : "Can not be null reaching this point.";
        propertyTypePE.setType(dataTypePE);
        propertyTypePE.setRegistrator(findRegistrator());
        if (EntityDataType.CONTROLLEDVOCABULARY.equals(dataTypePE.getCode()))
        {
            VocabularyPE vocabularyPE =
                    getVocabularyDAO().tryFindVocabularyByCode(
                            propertyType.getVocabulary().getCode());
            if (vocabularyPE == null)
            {
                vocabularyPE = createVocabulary(propertyType.getVocabulary());
            }
            propertyTypePE.setVocabulary(vocabularyPE);
        }
    }

    public final void save() throws UserFailureException
    {
        assert propertyTypePE != null : "Property type not defined.";
        try
        {
            getPropertyTypeDAO().createPropertyType(propertyTypePE);
        } catch (final DataAccessException e)
        {
            throwException(e, String.format("Property type '%s'.", propertyTypePE.getCode()));
        }
    }

}
