/*
 * Copyright 2011 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.shared.managed_property;

import ch.systemsx.cisd.openbis.generic.server.JythonEvaluatorPool;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityTypePropertyType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ScriptType;
import ch.systemsx.cisd.openbis.generic.shared.dto.EntityTypePropertyTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ScriptPE;

/**
 * Factory for creating managed property evaluators. (Could do some caching or other cleverness.)
 * 
 * @author Chandrasekhar Ramakrishnan
 * @author Jakub Straszewski
 * @author Pawel Glyzewski
 */
public class ManagedPropertyEvaluatorFactory
{

    public static IManagedPropertyEvaluator createManagedPropertyEvaluator(
            EntityTypePropertyTypePE entityTypePropertyTypePE)
    {
        final ScriptPE scriptPE = entityTypePropertyTypePE.getScript();
        assert scriptPE != null && scriptPE.getScriptType() == ScriptType.MANAGED_PROPERTY;

        String script = scriptPE.getScript();

        return createJythonManagedPropertyEvaluator(script);
    }

    private static JythonManagedPropertyEvaluator createJythonManagedPropertyEvaluator(String script)
    {
        if (JythonEvaluatorPool.INSTANCE != null)
        {
            return new JythonManagedPropertyEvaluator(
                    JythonEvaluatorPool.INSTANCE.getManagedPropertiesRunner(script));
        } else
        {
            return new JythonManagedPropertyEvaluator(script);
        }
    }

    public static IManagedPropertyEvaluator createManagedPropertyEvaluator(
            EntityTypePropertyType<?> entityTypePropertyType)
    {
        return createJythonManagedPropertyEvaluator(entityTypePropertyType.getScript().getScript());
    }
}
