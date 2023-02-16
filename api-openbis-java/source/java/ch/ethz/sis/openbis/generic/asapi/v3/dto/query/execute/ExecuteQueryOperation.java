/*
 * Copyright ETH 2012 - 2023 Zürich, Scientific IT Services
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
package ch.ethz.sis.openbis.generic.asapi.v3.dto.query.execute;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.operation.IOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.query.id.IQueryId;
import ch.systemsx.cisd.base.annotation.JsonObject;

/**
 * @author pkupczyk
 */
@JsonObject("as.dto.query.execute.ExecuteQueryOperation")
public class ExecuteQueryOperation implements IOperation
{
    private static final long serialVersionUID = 1L;

    private IQueryId queryId;

    private QueryExecutionOptions options;

    @SuppressWarnings("unused")
    private ExecuteQueryOperation()
    {
    }

    public ExecuteQueryOperation(IQueryId queryId, QueryExecutionOptions options)
    {
        this.queryId = queryId;
        this.options = options;
    }

    public IQueryId getQueryId()
    {
        return queryId;
    }

    public QueryExecutionOptions getOptions()
    {
        return options;
    }

    @Override
    public String getMessage()
    {
        return toString();
    }

    @Override
    public String toString()
    {
        return getClass().getSimpleName() + (queryId != null ? " " + queryId : "");
    }

}
