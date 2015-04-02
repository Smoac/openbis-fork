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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import ch.ethz.sis.openbis.generic.server.api.v3.executor.OperationContext;
import ch.ethz.sis.openbis.generic.server.api.v3.executor.common.ISearchObjectExecutor;
import ch.ethz.sis.openbis.generic.server.api.v3.translator.ITranslator;
import ch.ethz.sis.openbis.generic.server.api.v3.translator.TranslationContext;
import ch.ethz.sis.openbis.generic.server.api.v3.utils.ExceptionUtils;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.search.AbstractObjectSearchCriterion;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;

/**
 * @author pkupczyk
 */
public abstract class AbstractSearchMethodExecutor<OBJECT, OBJECT_PE, CRITERION extends AbstractObjectSearchCriterion<?>, FETCH_OPTIONS> extends
        AbstractMethodExecutor implements ISearchMethodExecutor<OBJECT, CRITERION, FETCH_OPTIONS>
{

    @Override
    public List<OBJECT> search(String sessionToken, CRITERION criterion, FETCH_OPTIONS fetchOptions)
    {
        Session session = getSession(sessionToken);
        return translate(session, search(session, criterion), fetchOptions);
    }

    private List<OBJECT_PE> search(Session session, CRITERION criterion)
    {
        OperationContext context = new OperationContext(session);
        try
        {
            return getSearchExecutor().search(context, criterion);
        } catch (Throwable t)
        {
            throw ExceptionUtils.create(context, t);
        }
    }

    private List<OBJECT> translate(Session session, List<OBJECT_PE> peList, FETCH_OPTIONS fetchOptions)
    {
        if (peList == null || peList.isEmpty())
        {
            return Collections.emptyList();
        }

        TranslationContext context = new TranslationContext(session);
        Map<OBJECT_PE, OBJECT> peToObjectMap = getTranslator().translate(context, peList, fetchOptions);
        return new ArrayList<OBJECT>(peToObjectMap.values());
    }

    protected abstract ISearchObjectExecutor<CRITERION, OBJECT_PE> getSearchExecutor();

    protected abstract ITranslator<OBJECT_PE, OBJECT, FETCH_OPTIONS> getTranslator();

}
