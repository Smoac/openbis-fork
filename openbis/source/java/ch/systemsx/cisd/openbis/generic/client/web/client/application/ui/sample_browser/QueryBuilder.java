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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.sample_browser;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for creating GET queries.
 * <p>
 * <strong>Usage:</strong><br>
 * <code>QueryBuilder qb = new QueryBuilder("server");</code><br>
 * <code>qb.set("parStr","myString");</code><br>
 * <code>qb.set("parBool",false);</code><br>
 * <code>qb.getQuery();</code>
 * </p>
 * <p>
 * <strong>Output:</strong> <code>server?parStr=myString&parBool=0</code>.
 * </p>
 * 
 * @author Izabela Adamczyk
 */
public class QueryBuilder
{
    private List<String> parameters;

    private String server;

    public QueryBuilder(String server)
    {
        this.server = server;
        parameters = new ArrayList<String>();
    }

    public void setParameter(String name, String value)
    {
        parameters.add(name + "=" + value);
    }

    public void setParameter(String name, int value)
    {
        setParameter(name, value + "");
    }

    public void setParameter(String name, boolean value)
    {
        setParameter(name, value ? "1" : "0");
    }

    public String getQuery()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(server);
        if (parameters.size() > 0)
        {
            boolean first = true;
            for (String s : parameters)
            {
                if (first)
                {
                    sb.append("?");
                    first = false;
                } else
                {
                    sb.append("&");
                }
                sb.append(s);
            }
        }
        return sb.toString();
    }
}