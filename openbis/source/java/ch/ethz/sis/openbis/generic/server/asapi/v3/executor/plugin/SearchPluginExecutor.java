/*
 * Copyright 2018 ETH Zuerich, SIS
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

package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.plugin;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.ISearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.IdsSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.plugin.id.IPluginId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.plugin.id.PluginPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.plugin.search.NameSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.plugin.search.PluginSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.plugin.search.PluginTypeSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.plugin.search.ScriptTypeSearchCriteria;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.search.AbstractSearchObjectManuallyExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.search.Matcher;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.search.SimpleFieldMatcher;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.search.StringFieldMatcher;
import ch.systemsx.cisd.openbis.generic.shared.dto.ScriptPE;

/**
 * @author Franz-Josef Elmer
 */
@Component
public class SearchPluginExecutor
        extends AbstractSearchObjectManuallyExecutor<PluginSearchCriteria, ScriptPE>
        implements ISearchPluginExecutor
{
    @Autowired
    private IPluginAuthorizationExecutor authorizationExcutor;

    @Override
    public List<ScriptPE> search(IOperationContext context, PluginSearchCriteria criteria)
    {
        authorizationExcutor.canSearch(context);
        return super.search(context, criteria);
    }

    @Override
    protected List<ScriptPE> listAll()
    {
        return daoFactory.getScriptDAO().listAllEntities();
    }

    @Override
    protected Matcher<ScriptPE> getMatcher(ISearchCriteria criteria)
    {
        if (criteria instanceof IdsSearchCriteria)
        {
            return new IdsMatcher();
        } else if (criteria instanceof NameSearchCriteria)
        {
            return new NameMatcher();
        } else if (criteria instanceof PluginTypeSearchCriteria)
        {
            return new PluginTypeMatcher();
        } else if (criteria instanceof ScriptTypeSearchCriteria)
        {
            return new ScriptTypeMatcher();
        } else
        {
            throw new IllegalArgumentException("Unknown search criteria: " + criteria.getClass());
        }
    }

    private class IdsMatcher extends Matcher<ScriptPE>
    {
        @SuppressWarnings("unchecked")
        @Override
        public List<ScriptPE> getMatching(IOperationContext context, List<ScriptPE> objects, ISearchCriteria criteria)
        {
            Collection<IPluginId> ids = ((IdsSearchCriteria<IPluginId>) criteria).getFieldValue();

            if (ids != null && false == ids.isEmpty())
            {
                Collection<String> names = new HashSet<String>();

                for (IPluginId id : ids)
                {
                    if (id instanceof PluginPermId)
                    {
                        names.add(((PluginPermId) id).getPermId());
                    } else
                    {
                        throw new IllegalArgumentException("Unknown id: " + id.getClass());
                    }
                }

                List<ScriptPE> matches = new ArrayList<ScriptPE>();

                for (ScriptPE object : objects)
                {
                    if (names.contains(object.getName()))
                    {
                        matches.add(object);
                    }
                }
                
                return matches;
            } else
            {
                return new ArrayList<ScriptPE>();
            }
        }
    }
    
    private static final class NameMatcher extends StringFieldMatcher<ScriptPE>
    {
        @Override
        protected String getFieldValue(ScriptPE plugin)
        {
            return plugin.getName();
        }
        
    }
    
    private static final class PluginTypeMatcher extends SimpleFieldMatcher<ScriptPE>
    {
        @Override
        protected boolean isMatching(IOperationContext context, ScriptPE script, ISearchCriteria criteria)
        {
            return script.getPluginType().name()
                    .equals(((PluginTypeSearchCriteria) criteria).getFieldValue().name());
        }
    }
    
    private static final class ScriptTypeMatcher extends SimpleFieldMatcher<ScriptPE>
    {
        @Override
        protected boolean isMatching(IOperationContext context, ScriptPE script, ISearchCriteria criteria)
        {
            return script.getScriptType().name()
                    .equals(((ScriptTypeSearchCriteria) criteria).getFieldValue().name());
        }
    }
}
