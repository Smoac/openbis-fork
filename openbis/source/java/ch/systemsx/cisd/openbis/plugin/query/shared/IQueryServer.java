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

package ch.systemsx.cisd.openbis.plugin.query.shared;

import java.util.List;

import org.springframework.transaction.annotation.Transactional;

import ch.systemsx.cisd.openbis.generic.shared.IServer;
import ch.systemsx.cisd.openbis.generic.shared.authorization.annotation.AuthorizationGuard;
import ch.systemsx.cisd.openbis.generic.shared.authorization.annotation.ReturnValueFilter;
import ch.systemsx.cisd.openbis.generic.shared.authorization.annotation.RoleSet;
import ch.systemsx.cisd.openbis.generic.shared.authorization.annotation.RolesAllowed;
import ch.systemsx.cisd.openbis.generic.shared.authorization.validator.ExpressionValidator;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IFilterOrColumnUpdates;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewExpression;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModel;
import ch.systemsx.cisd.openbis.plugin.query.shared.authorization.predicate.DeleteQueryPredicate;
import ch.systemsx.cisd.openbis.plugin.query.shared.authorization.predicate.UpdateQueryPredicate;
import ch.systemsx.cisd.openbis.plugin.query.shared.basic.dto.QueryExpression;
import ch.systemsx.cisd.openbis.plugin.query.shared.basic.dto.QueryParameterBindings;

/**
 * @author Franz-Josef Elmer
 */
public interface IQueryServer extends IServer
{

    @Transactional(readOnly = true)
    @RolesAllowed(RoleSet.OBSERVER)
    public String tryToGetQueryDatabaseLabel(String sessionToken);

    @Transactional(readOnly = true)
    @RolesAllowed(RoleSet.POWER_USER)
    public TableModel queryDatabase(String sessionToken, String sqlQuery,
            QueryParameterBindings bindings);

    @Transactional(readOnly = true)
    @RolesAllowed(RoleSet.OBSERVER)
    public TableModel queryDatabase(String sessionToken, TechId queryId,
            QueryParameterBindings bindings);

    @Transactional
    @RolesAllowed(RoleSet.OBSERVER)
    @ReturnValueFilter(validatorClass = ExpressionValidator.class)
    public List<QueryExpression> listQueries(String sessionToken);

    @Transactional
    @RolesAllowed(RoleSet.POWER_USER)
    public void registerQuery(String sessionToken, NewExpression expression);

    @Transactional
    @RolesAllowed(RoleSet.POWER_USER)
    public void deleteQueries(String sessionToken,
            @AuthorizationGuard(guardClass = DeleteQueryPredicate.class) List<TechId> queryIds);

    @Transactional
    @RolesAllowed(RoleSet.POWER_USER)
    public void updateQuery(
            String sessionToken,
            @AuthorizationGuard(guardClass = UpdateQueryPredicate.class) IFilterOrColumnUpdates updates);
}
