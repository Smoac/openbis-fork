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
package ch.ethz.sis.openbis.generic.asapi.v3.dto.service.execute;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.operation.IOperation;
import ch.systemsx.cisd.base.annotation.JsonObject;

/**
 * @author Franz-Josef Elmer
 *
 */
@JsonObject("as.dto.service.execute.ExecuteSearchDomainServiceOperation")
public class ExecuteSearchDomainServiceOperation implements IOperation
{
    private static final long serialVersionUID = 1L;

    private SearchDomainServiceExecutionOptions options;

    @SuppressWarnings("unused")
    private ExecuteSearchDomainServiceOperation()
    {
    }

    public ExecuteSearchDomainServiceOperation(SearchDomainServiceExecutionOptions options)
    {
        this.options = options;
    }

    public SearchDomainServiceExecutionOptions getOptions()
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
        return getClass().getSimpleName();
    }

}
