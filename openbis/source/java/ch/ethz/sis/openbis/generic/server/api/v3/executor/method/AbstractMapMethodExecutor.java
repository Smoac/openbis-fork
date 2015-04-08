/*
 * Copyright 2015 ETH Zuerich, CISD
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

package ch.ethz.sis.openbis.generic.server.api.v3.executor.method;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import ch.ethz.sis.openbis.generic.server.api.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.api.v3.executor.common.IMapObjectByIdExecutor;
import ch.ethz.sis.openbis.generic.server.api.v3.translator.ITranslator;
import ch.ethz.sis.openbis.generic.server.api.v3.translator.TranslationContext;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.IObjectId;

/**
 * @author pkupczyk
 */
public abstract class AbstractMapMethodExecutor<OBJECT_ID extends IObjectId, OBJECT_PE, OBJECT, FETCH_OPTIONS> extends AbstractMethodExecutor
{

    public Map<OBJECT_ID, OBJECT> map(final String sessionToken, final List<? extends OBJECT_ID> objectIds, final FETCH_OPTIONS fetchOptions)
    {
        return executeInContext(sessionToken, new IMethodAction<Map<OBJECT_ID, OBJECT>>()
            {
                @Override
                public Map<OBJECT_ID, OBJECT> execute(IOperationContext context)
                {
                    Map<OBJECT_ID, OBJECT_PE> map = getMapExecutor().map(context, objectIds);
                    return translate(context, map, fetchOptions);
                }
            });
    }

    private Map<OBJECT_ID, OBJECT> translate(IOperationContext context, Map<OBJECT_ID, OBJECT_PE> idToPeMap, FETCH_OPTIONS fetchOptions)
    {
        if (idToPeMap == null || idToPeMap.isEmpty())
        {
            return Collections.emptyMap();
        }

        TranslationContext translationContext = new TranslationContext(context.getSession());
        Map<OBJECT_PE, OBJECT> peToObjectMap = getTranslator().translate(translationContext, idToPeMap.values(), fetchOptions);
        Map<OBJECT_ID, OBJECT> idToObjectMap = new LinkedHashMap<OBJECT_ID, OBJECT>();

        for (Map.Entry<OBJECT_ID, OBJECT_PE> entry : idToPeMap.entrySet())
        {
            OBJECT_ID id = entry.getKey();
            OBJECT_PE pe = entry.getValue();
            OBJECT object = peToObjectMap.get(pe);

            if (object != null)
            {
                idToObjectMap.put(id, object);
            }
        }

        return idToObjectMap;
    }

    protected abstract IMapObjectByIdExecutor<OBJECT_ID, OBJECT_PE> getMapExecutor();

    protected abstract ITranslator<OBJECT_PE, OBJECT, FETCH_OPTIONS> getTranslator();

}
