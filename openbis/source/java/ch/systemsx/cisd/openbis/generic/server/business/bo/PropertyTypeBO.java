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
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PropertyType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Vocabulary;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.EntityTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.MaterialTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PropertyTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;
import ch.systemsx.cisd.openbis.generic.shared.dto.VocabularyPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityDataType;
import ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityKind;

/**
 * The only productive implementation of {@link IPropertyTypeBO}.
 * 
 * @author Christian Ribeaud
 */
public final class PropertyTypeBO extends VocabularyBO implements IPropertyTypeBO
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
        assert propertyType != null : "Unspecified property type.";
        propertyTypePE = new PropertyTypePE();
        propertyTypePE.setDatabaseInstance(getHomeDatabaseInstance());
        propertyTypePE.setCode(propertyType.getCode());
        propertyTypePE.setLabel(propertyType.getLabel());
        propertyTypePE.setDescription(propertyType.getDescription());
        DataTypePE dataTypePE = getDataTypeCode(propertyType.getDataType());
        propertyTypePE.setType(dataTypePE);
        MaterialTypePE materialType = tryGetMaterialType(propertyType.getMaterialType());
        propertyTypePE.setMaterialType(materialType);
        propertyTypePE.setRegistrator(findRegistrator());

        if (EntityDataType.CONTROLLEDVOCABULARY.equals(dataTypePE.getCode()))
        {
            Vocabulary vocabulary = propertyType.getVocabulary();
            if (vocabulary.getId() == null)
            {
                throw new UserFailureException("Vocabulary not selected");
            } else
            {
                // loading existing vocabulary
                loadDataByTechId(TechId.create(vocabulary));
            }
            VocabularyPE vocabularyPE = getVocabulary();
            propertyTypePE.setVocabulary(vocabularyPE);
        }
    }

    private MaterialTypePE tryGetMaterialType(MaterialType materialType)
    {
        if (materialType != null)
        {
            EntityTypePE entityType =
                    getEntityTypeDAO(EntityKind.MATERIAL).tryToFindEntityTypeByCode(
                            materialType.getCode());
            return (MaterialTypePE) entityType;
        } else
        {
            return null;
        }
    }

    private DataTypePE getDataTypeCode(final DataType dataType)
    {
        DataTypePE dataTypePE = null;
        final String dataTypeCode = dataType.getCode().name();
        try
        {
            dataTypePE =
                    getPropertyTypeDAO().getDataTypeByCode(EntityDataType.valueOf(dataTypeCode));
        } catch (final IllegalArgumentException e)
        {
            throw UserFailureException.fromTemplate("Unknow data type code '%s'.", dataType);
        }
        assert dataTypePE != null : "Can not be null reaching this point.";
        return dataTypePE;
    }

    @Override
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

    public final PropertyTypePE getPropertyType()
    {
        assert propertyTypePE != null : "Property type not defined.";
        return propertyTypePE;
    }
}
