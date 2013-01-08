/*
 * Copyright 2013 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.server.dataaccess.dynamic_property.calculator;

import java.util.HashMap;
import java.util.Map;

import ch.systemsx.cisd.openbis.generic.server.dataaccess.dynamic_property.IDynamicPropertyCalculatorFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.EntityTypePropertyTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ScriptPE;

/**
 * @author Pawel Glyzewski
 */
public class DynamicPropertyCalculatorFactory implements IDynamicPropertyCalculatorFactory
{
    private final Map<ScriptPE, IDynamicPropertyCalculator> calculatorsByScript =
            new HashMap<ScriptPE, IDynamicPropertyCalculator>();

    @Override
    /** Returns a calculator for given script (creates a new one if nothing is found in cache). */
    public IDynamicPropertyCalculator getCalculator(EntityTypePropertyTypePE etpt)
    {
        ScriptPE scriptPE = etpt.getScript();

        // Creation of a calculator takes some time because of compilation of the script.
        // That is why a cache is used.
        IDynamicPropertyCalculator result = calculatorsByScript.get(scriptPE);
        if (result == null)
        {
            result = JythonDynamicPropertyCalculator.create(scriptPE.getScript());
            calculatorsByScript.put(scriptPE, result);
        }
        return result;
    }
}
