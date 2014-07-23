/*
 * Copyright 2014 ETH Zuerich, CISD
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

package ch.ethz.sis.openbis.generic.server.api.v3.context;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import ch.systemsx.cisd.openbis.generic.shared.dto.Session;

/**
 * @author pkupczyk
 */
public class Context implements IContext
{

    private final Stack<String> descriptions = new Stack<String>();

    private final Map<String, Object> attributes = new HashMap<String, Object>();

    private final Session session;

    public Context(Session session)
    {
        this.session = session;
    }

    @Override
    public void pushContextDescription(String description)
    {
        descriptions.push(description);
    }

    @Override
    public String popContextDescription()
    {
        return descriptions.pop();
    }

    @Override
    public Collection<String> getContextDescriptions()
    {
        return descriptions;
    }

    @Override
    public Session getSession()
    {
        return session;
    }

    @Override
    public Object getAttribute(String attributeName)
    {
        return attributes.get(attributeName);
    }

    @Override
    public void setAttribute(String attributeName, Object attributeValue)
    {
        attributes.put(attributeName, attributeValue);
    }

}
