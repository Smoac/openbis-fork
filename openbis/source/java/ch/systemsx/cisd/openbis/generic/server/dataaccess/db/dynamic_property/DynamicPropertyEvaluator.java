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
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IPropertyValueValidator;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.PropertyValidator;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.db.dynamic_property.calculator.DynamicPropertyCalculator;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.db.dynamic_property.calculator.EntityAdaptorFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.db.dynamic_property.calculator.IEntityAdaptor;
import ch.systemsx.cisd.openbis.generic.shared.basic.BasicConstant;
import ch.systemsx.cisd.openbis.generic.shared.dto.EntityPropertyPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.EntityTypePropertyTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.IEntityInformationWithPropertiesHolder;
import ch.systemsx.cisd.openbis.generic.shared.dto.ScriptPE;

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

    private static final IPropertyValueValidator validator = new PropertyValidator();

    public static final String ERROR_PREFIX = "ERROR: ";

    /** cache of calculators with precompiled expressions */
    private final Map<ScriptPE, DynamicPropertyCalculator> calculatorsByScript =
            new HashMap<ScriptPE, DynamicPropertyCalculator>();

    /** path of evaluation - used to generate meaningful error message for cyclic dependencies */
    private final List<EntityTypePropertyTypePE> evaluationPath =
            new ArrayList<EntityTypePropertyTypePE>();

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
        for (EntityPropertyPE property : entity.getProperties())
        {
            EntityTypePropertyTypePE etpt = property.getEntityTypePropertyType();
            if (etpt.isDynamic())
            {
                final String dynamicValue = evaluateProperty(entityAdaptor, etpt, true);
                property.setValue(dynamicValue);
            }
        }
    }

    public List<EntityTypePropertyTypePE> getEvaluationPath()
    {
        return evaluationPath;
    }

    public String evaluateProperty(IEntityAdaptor entityAdaptor, EntityTypePropertyTypePE etpt)
    {
        return evaluateProperty(entityAdaptor, etpt, false);
    }

    public String evaluateProperty(IEntityAdaptor entityAdaptor, EntityTypePropertyTypePE etpt,
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
            final String dynamicValue = calculator.evalAsString();
            final String validatedValue =
                    validator.validatePropertyValue(etpt.getPropertyType(), dynamicValue);
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

}
