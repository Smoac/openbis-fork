/*
 * Copyright 2009 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.plugin.query.shared.basic.dto;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AbstractExpressionWithParameters;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ServiceVersionHolder;

/**
 * Stores information describing a parametrized query.
 * 
 * @author Piotr Buczek
 */
// TODO 2010-02-23, Piotr Buczek: no need to use subclasses
public class QueryExpression extends AbstractExpressionWithParameters
{
    private static final long serialVersionUID = ServiceVersionHolder.VERSION;

    public QueryExpression()
    {
    }

    public QueryExpression(String expression)
    {
        setExpression(expression);
    }

}
