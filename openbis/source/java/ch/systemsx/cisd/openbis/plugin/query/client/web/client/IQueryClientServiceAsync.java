/*
 * Copyright 2010 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.plugin.query.client.web.client;

import java.util.List;

import com.google.gwt.user.client.rpc.AsyncCallback;

import ch.systemsx.cisd.openbis.generic.client.web.client.IClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.IResultSetConfig;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ResultSet;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.TableExportCriteria;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.TableModelReference;
import ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IFilterOrColumnUpdates;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewExpression;
import ch.systemsx.cisd.openbis.plugin.query.shared.basic.dto.QueryExpression;

/**
 * @author Piotr Buczek
 */
public interface IQueryClientServiceAsync extends IClientServiceAsync
{

    /** @see IQueryClientService#tryToGetQueryDatabaseLabel() */
    public void tryToGetQueryDatabaseLabel(AsyncCallback<String> callback);

    /** @see IQueryClientService#createQueryResultsReport(String) */
    public void createQueryResultsReport(String sqlQuery,
            AsyncCallback<TableModelReference> callback);

    /** @see IQueryClientService#listQueries(IResultSetConfig) */
    public void listQueries(IResultSetConfig<String, QueryExpression> resultSetConfig,
            AsyncCallback<ResultSet<QueryExpression>> callback);

    /** @see IQueryClientService#listQueries() */
    public void listQueries(AsyncCallback<List<QueryExpression>> callback)
            throws UserFailureException;

    /** @see IQueryClientService#prepareExportQueries(TableExportCriteria) */
    public void prepareExportQueries(TableExportCriteria<QueryExpression> criteria,
            AsyncCallback<String> callback);

    /** @see IQueryClientService#registerQuery(NewExpression) */
    public void registerQuery(NewExpression query, AsyncCallback<Void> callback);

    /** @see IQueryClientService#deleteQueries(List) */
    public void deleteQueries(List<TechId> filterIds, AsyncCallback<Void> callback);

    /** @see IQueryClientService#updateQuery(IFilterOrColumnUpdates) */
    public void updateQuery(final IFilterOrColumnUpdates queryUpdate, AsyncCallback<Void> callback);
}
