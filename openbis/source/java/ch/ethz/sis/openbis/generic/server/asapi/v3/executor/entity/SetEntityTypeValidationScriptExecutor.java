/*
 * Copyright 2017 ETH Zuerich, SIS
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

package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.entity;

import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.plugin.id.IPluginId;
import ch.ethz.sis.openbis.generic.asapi.v3.exceptions.ObjectNotFoundException;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.plugin.IMapPluginByIdExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.common.batch.MapBatch;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ScriptType;
import ch.systemsx.cisd.openbis.generic.shared.dto.EntityTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ScriptPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityKind;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
@Component
public class SetEntityTypeValidationScriptExecutor 
{
    @Autowired
    private IMapPluginByIdExecutor mapPluginByIdExecutor;

    public <T, PE extends EntityTypePE> void setValidationPlugin(IOperationContext context, 
            MapBatch<T, PE> batch, IPluginIdProvider<T> pluginIdProvider, EntityKind entityKind)
    {
        Set<IPluginId> pluginIds = new HashSet<>();
        Set<Entry<T, PE>> entrySet = batch.getObjects().entrySet();
        for (Entry<T, PE> entry : entrySet)
        {
            IPluginId validationPluginId = pluginIdProvider.getPluginId(entry.getKey());
            if (validationPluginId != null)
            {
                pluginIds.add(validationPluginId);
            }
        }
        Map<IPluginId, ScriptPE> map = mapPluginByIdExecutor.map(context, pluginIds);
        for (Entry<T, PE> entry : entrySet)
        {
            T typeCreation = entry.getKey();
            IPluginId validationPluginId = pluginIdProvider.getPluginId(typeCreation);
            if (validationPluginId != null)
            {
                ScriptPE pluginPE = map.get(validationPluginId);
                if (pluginPE == null)
                {
                    throw new ObjectNotFoundException(validationPluginId);
                }
                if (false == ScriptType.ENTITY_VALIDATION.equals(pluginPE.getScriptType()))
                {
                    throw new UserFailureException("Entity type validation plugin has to be of type '" + ScriptType.ENTITY_VALIDATION
                            + "'. The specified plugin with id '" + validationPluginId + "' is of type '" + pluginPE.getScriptType()
                            + "'.");
                }
                
                if (pluginPE.getEntityKind() != null
                        && false == pluginPE.getEntityKind().equals(entityKind))
                {
                    throw new UserFailureException("Entity type validation plugin has entity kind set to '" + pluginPE.getEntityKind()
                    + "'. Expected a plugin where entity kind is either '" + entityKind + "' or null.");
                }
                entry.getValue().setValidationScript(pluginPE);
            }
        }
    }

}
