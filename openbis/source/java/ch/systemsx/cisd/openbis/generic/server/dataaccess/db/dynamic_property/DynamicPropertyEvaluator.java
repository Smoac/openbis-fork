/*
 * Copyright 2010 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.server.dataaccess.db.dynamic_property;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.openbis.generic.server.business.bo.EntityPropertiesConverter;
import ch.systemsx.cisd.openbis.generic.server.business.bo.EntityPropertiesConverter.ComplexPropertyValueHelper;
import ch.systemsx.cisd.openbis.generic.server.business.bo.IEntityPropertiesConverter;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.db.dynamic_property.calculator.DynamicPropertyCalculator;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.db.dynamic_property.calculator.EntityAdaptorFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.db.dynamic_property.calculator.IEntityAdaptor;
import ch.systemsx.cisd.openbis.generic.shared.basic.BasicConstant;
import ch.systemsx.cisd.openbis.generic.shared.dto.EntityPropertyPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.EntityTypePropertyTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.IEntityInformationWithPropertiesHolder;
import ch.systemsx.cisd.openbis.generic.shared.dto.MaterialPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PropertyTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ScriptPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.VocabularyTermPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityKind;

/**
 * Default implementation of {@link IDynamicPropertyEvaluator}. For efficient evaluation of
 * properties a cache of compiled script is used internally.
 * 
 * @author Piotr Buczek
 */
public class DynamicPropertyEvaluator implements IDynamicPropertyEvaluator
{
    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
            DynamicPropertyEvaluator.class);

    public static final String ERROR_PREFIX = "ERROR: ";

    /** cache of calculators with precompiled expressions */
    private final Map<ScriptPE, DynamicPropertyCalculator> calculatorsByScript =
            new HashMap<ScriptPE, DynamicPropertyCalculator>();

    /** path of evaluation - used to generate meaningful error message for cyclic dependencies */
    private final List<EntityTypePropertyTypePE> evaluationPath =
            new ArrayList<EntityTypePropertyTypePE>();

    private EntityPropertiesConverterDelegatorFacade entityPropertiesConverter;

    public DynamicPropertyEvaluator(IDAOFactory daoFactory)
    {
        assert daoFactory != null;
        this.entityPropertiesConverter = new EntityPropertiesConverterDelegatorFacade(daoFactory);
    }

    /** Returns a calculator for given script (creates a new one if nothing is found in cache). */
    private DynamicPropertyCalculator getCalculator(ScriptPE scriptPE)
    {
        // Creation of a calculator takes some time because of compilation of the script.
        // That is why a cache is used.
        DynamicPropertyCalculator result = calculatorsByScript.get(scriptPE);
        if (result == null)
        {
            result = DynamicPropertyCalculator.create(scriptPE.getScript());
            calculatorsByScript.put(scriptPE, result);
        }
        return result;
    }

    public <T extends IEntityInformationWithPropertiesHolder> void evaluateProperties(T entity)
    {
        if (operationLog.isDebugEnabled())
        {
            operationLog.debug(String.format("Evaluating dynamic properties of entity '%s'.",
                    entity));
        }
        final IEntityAdaptor entityAdaptor = EntityAdaptorFactory.create(entity, this);

        Set<EntityPropertyPE> propertiesToRemove = new HashSet<EntityPropertyPE>();
        for (EntityPropertyPE property : entity.getProperties())
        {
            EntityTypePropertyTypePE etpt = property.getEntityTypePropertyType();
            if (etpt.isDynamic())
            {
                final String dynamicValue = evaluateProperty(entityAdaptor, etpt, true);

                String valueOrNull = null;
                MaterialPE materialOrNull = null;
                VocabularyTermPE termOrNull = null;
                if (dynamicValue == null)
                {
                    propertiesToRemove.add(property);
                } else if (dynamicValue.startsWith(BasicConstant.ERROR_PROPERTY_PREFIX))
                {
                    property.setUntypedValue(dynamicValue, null, null);
                } else
                {
                    try
                    {
                        switch (etpt.getPropertyType().getType().getCode())
                        {
                            case CONTROLLEDVOCABULARY:
                                termOrNull =
                                        entityPropertiesConverter.tryGetVocabularyTerm(
                                                dynamicValue, etpt.getPropertyType());
                                break;
                            case MATERIAL:
                                materialOrNull =
                                        entityPropertiesConverter.tryGetMaterial(dynamicValue,
                                                etpt.getPropertyType());
                                break;
                            default:
                                valueOrNull = dynamicValue;
                        }
                    } catch (Throwable t)
                    {
                        valueOrNull = errorPropertyValue(t.getMessage());
                    }
                    property.setUntypedValue(valueOrNull, termOrNull, materialOrNull);
                }
            }
        }
        // remove properties in a separate loop not to cause ConcurrentModificationException
        for (EntityPropertyPE property : propertiesToRemove)
        {
            property.getEntity().removeProperty(property);
        }
    }

    public List<EntityTypePropertyTypePE> getEvaluationPath()
    {
        return evaluationPath;
    }

    public String evaluateProperty(IEntityAdaptor entityAdaptor, EntityTypePropertyTypePE etpt)
    {
        // TODO 2010-11-22, Piotr Buczek: will dependency with Vocabulary/Material fail?
        // are values computed by dependent properties thrown away?
        return evaluateProperty(entityAdaptor, etpt, false);
    }

    private String evaluateProperty(IEntityAdaptor entityAdaptor, EntityTypePropertyTypePE etpt,
            boolean startPath)
    {
        assert etpt.isDynamic() == true : "expected dynamic property";
        try
        {
            if (startPath)
            {
                evaluationPath.clear();
            } else
            {
                evaluationPath.add(etpt);
            }
            final DynamicPropertyCalculator calculator = getCalculator(etpt.getScript());
            calculator.setEntity(entityAdaptor);
            etpt.getEntityType().getEntityKind();
            final String dynamicValue = calculator.evalAsString();
            final String validatedValue =
                    entityPropertiesConverter.tryCreateValidatedPropertyValue(etpt.getEntityType()
                            .getEntityKind(), etpt.getPropertyType(), etpt, dynamicValue);
            return validatedValue;
        } catch (Throwable t)
        {
            final String errorValue = errorPropertyValue(t.getMessage());
            return errorValue;
        }
    }

    /** @return value for property storing specified error message */
    public static String errorPropertyValue(String error)
    {
        String errorMsg = ERROR_PREFIX + error;
        operationLog.info(errorMsg);
        return BasicConstant.ERROR_PROPERTY_PREFIX + errorMsg;
    }

    private static class EntityPropertiesConverterDelegatorFacade
    {
        final Map<EntityKind, IEntityPropertiesConverter> convertersByEntityKind =
                new HashMap<EntityKind, IEntityPropertiesConverter>();

        private final ComplexPropertyValueHelper complexPropertyValueHelper;

        public EntityPropertiesConverterDelegatorFacade(IDAOFactory daoFactory)
        {
            this.complexPropertyValueHelper =
                    new EntityPropertiesConverter.ComplexPropertyValueHelper(daoFactory);
            for (EntityKind entityKind : EntityKind.values())
            {
                convertersByEntityKind.put(entityKind, new EntityPropertiesConverter(entityKind,
                        daoFactory));
            }
        }

        public String tryCreateValidatedPropertyValue(final EntityKind entityKind,
                PropertyTypePE propertyType, EntityTypePropertyTypePE entityTypePropertyType,
                String value)
        {
            return convertersByEntityKind.get(entityKind).tryCreateValidatedPropertyValue(
                    propertyType, entityTypePropertyType, value);
        }

        public MaterialPE tryGetMaterial(String value, PropertyTypePE propertyType)
        {
            return complexPropertyValueHelper.tryGetMaterial(value, propertyType);
        }

        public VocabularyTermPE tryGetVocabularyTerm(String value, PropertyTypePE propertyType)
        {
            return complexPropertyValueHelper.tryGetVocabularyTerm(value, propertyType);
        }
    }
}
