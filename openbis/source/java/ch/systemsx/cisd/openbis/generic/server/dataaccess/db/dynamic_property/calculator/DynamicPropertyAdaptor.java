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

package ch.systemsx.cisd.openbis.generic.server.dataaccess.db.dynamic_property.calculator;

import ch.systemsx.cisd.openbis.generic.server.dataaccess.IPropertyValueValidator;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.PropertyValidator;
import ch.systemsx.cisd.openbis.generic.shared.basic.BasicConstant;
import ch.systemsx.cisd.openbis.generic.shared.dto.EntityPropertyPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.EntityTypePropertyTypePE;

/**
 * {@link IEntityPropertyAdaptor} for dynamic property with lazy evaluation and cyclic dependencies
 * detection.
 * 
 * @author Piotr Buczek
 */
// TODO 2010-11-05, Piotr Buczek: refactor to use DynamicPropertyEvaluator
class DynamicPropertyAdaptor implements IEntityPropertyAdaptor
{
    private enum State
    {
        EMPTY, EVALUATING, EVALUATED
    }

    private static final IPropertyValueValidator validator = new PropertyValidator();

    private static final String ERROR_PREFIX = "ERROR: ";

    private State state = State.EMPTY;

    private String value = null;

    private final String code;

    private final EntityPropertyPE propertyPE;

    private final IEntityAdaptor entityAdaptor;

    public DynamicPropertyAdaptor(String code, IEntityAdaptor entityAdaptor,
            EntityPropertyPE propertyPE)
    {
        this.code = code;
        this.entityAdaptor = entityAdaptor;
        this.propertyPE = propertyPE;
    }

    public String propertyTypeCode()
    {
        return code;
    }

    private String doGetValue()
    {
        switch (state)
        {
            case EMPTY:
                // start evaluation
                state = State.EVALUATING;
                value = doEvaluate();
                break;
            case EVALUATING:
                // cycle found - return an error
                state = State.EVALUATED;
                String errorMsg =
                        ERROR_PREFIX + "cycle found in dependencies of property "
                                + propertyTypeCode();
                value = BasicConstant.ERROR_PROPERTY_PREFIX + errorMsg;
                break;
            case EVALUATED:
                // value was already computed
                break;
        }
        return value;
    }

    private String doEvaluate()
    {
        EntityTypePropertyTypePE etpt = propertyPE.getEntityTypePropertyType();
        assert etpt != null;
        try
        {
            // TODO 2010-11-05, Piotr Buczek: use cache
            final DynamicPropertyCalculator calculator =
                    DynamicPropertyCalculator.create(etpt.getScript().getScript());
            calculator.setEntity(entityAdaptor);
            final String dynamicValue = calculator.evalAsString();
            final String validatedValue =
                    validator.validatePropertyValue(etpt.getPropertyType(), dynamicValue);
            return validatedValue;
        } catch (Exception e)
        {
            String errorMsg = ERROR_PREFIX + e.getMessage();
            // operationLog.info(errorMsg);
            return BasicConstant.ERROR_PROPERTY_PREFIX + errorMsg;
        }
    }

    public String valueAsString()
    {
        return doGetValue();
    }

    public String renderedValue()
    {
        return valueAsString();
    }

    public EntityPropertyPE getPropertyPE()
    {
        return propertyPE;
    }

    //
    // Object
    //

    @Override
    public String toString()
    {
        return propertyTypeCode() + " " + valueAsString();
    }

}
