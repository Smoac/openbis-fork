/*
 * Copyright 2008 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.plugin.generic.client.web.client.application.sample;

import ch.systemsx.cisd.openbis.generic.client.web.client.testframework.AsStringAssertion;
import ch.systemsx.cisd.openbis.generic.client.web.client.testframework.CodeAssertion;
import ch.systemsx.cisd.openbis.generic.client.web.client.testframework.EntityPropertyAssertion;
import ch.systemsx.cisd.openbis.generic.client.web.client.testframework.EqualsAssertion;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public class Property
{
    private final String key;
    private final CheckSample checker;
    private final String message;

    public Property(String key, CheckSample checker)
    {
        this.key = key;
        this.checker = checker;
        message = "Property '" + key + "':";
    }
    
    public CheckSample asString(Object value)
    {
        checker.property(key, new AsStringAssertion<Object>(message, value));
        return checker;
    }

    public CheckSample asObject(Object value)
    {
        checker.property(key, new EqualsAssertion<Object>(message, value));
        return checker;
    }
    
    public CheckSample asCode(String code)
    {
        checker.property(key, new CodeAssertion(message, code));
        return checker;
    }
    
    @SuppressWarnings("unchecked")
    public CheckSample asProperty(String value)
    {
        checker.property(key, new EntityPropertyAssertion(message, value));
        return checker;
    }
    
}
