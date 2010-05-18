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

package ch.systemsx.cisd.openbis.plugin.query.server;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.annotation.Resource;

import org.apache.commons.lang.StringEscapeUtils;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;

import ch.systemsx.cisd.authentication.ISessionManager;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.spring.ExposablePropertyPaceholderConfigurer;
import ch.systemsx.cisd.common.spring.IInvocationLoggerContext;
import ch.systemsx.cisd.common.utilities.PropertyParametersUtil;
import ch.systemsx.cisd.common.utilities.PropertyUtils;
import ch.systemsx.cisd.common.utilities.PropertyParametersUtil.SectionProperties;
import ch.systemsx.cisd.openbis.generic.server.AbstractServer;
import ch.systemsx.cisd.openbis.generic.server.business.bo.DataAccessExceptionTranslator;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IQueryDAO;
import ch.systemsx.cisd.openbis.generic.server.plugin.IDataSetTypeSlaveServerPlugin;
import ch.systemsx.cisd.openbis.generic.server.plugin.ISampleTypeSlaveServerPlugin;
import ch.systemsx.cisd.openbis.generic.shared.authorization.annotation.RoleSet;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.QueryType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModel;
import ch.systemsx.cisd.openbis.generic.shared.dto.GroupPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.QueryPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;
import ch.systemsx.cisd.openbis.plugin.query.shared.DatabaseDefinition;
import ch.systemsx.cisd.openbis.plugin.query.shared.IQueryServer;
import ch.systemsx.cisd.openbis.plugin.query.shared.ResourceNames;
import ch.systemsx.cisd.openbis.plugin.query.shared.SimpleDatabaseConfigurationContext;
import ch.systemsx.cisd.openbis.plugin.query.shared.authorization.QueryAccessController;
import ch.systemsx.cisd.openbis.plugin.query.shared.basic.dto.IQueryUpdates;
import ch.systemsx.cisd.openbis.plugin.query.shared.basic.dto.NewQuery;
import ch.systemsx.cisd.openbis.plugin.query.shared.basic.dto.QueryDatabase;
import ch.systemsx.cisd.openbis.plugin.query.shared.basic.dto.QueryExpression;
import ch.systemsx.cisd.openbis.plugin.query.shared.basic.dto.QueryParameterBindings;
import ch.systemsx.cisd.openbis.plugin.query.shared.translator.QueryTranslator;

/**
 * @author Franz-Josef Elmer
 */
@Component(ResourceNames.QUERY_PLUGIN_SERVER)
public class QueryServer extends AbstractServer<IQueryServer> implements IQueryServer
{
    /** property with database keys/names separated by delimiter */
    private static final String DATABASE_KEYS = "query-databases";

    private static final String LABEL_PROPERTY_KEY = "label";

    private static final String CREATOR_MINIMAL_ROLE_KEY = "creator-minimal-role";

    private static final String DEFAULT_CREATOR_MINIMAL_ROLE = RoleSet.POWER_USER.name();

    private static final String DATA_SPACE_KEY = "data-space";

    @Resource(name = "propertyConfigurer")
    private ExposablePropertyPaceholderConfigurer configurer;

    /**
     * map from dbKey to IDAO
     * 
     * @deprecated don't use it directly - use getter instead
     */
    @Deprecated
    private final Map<String, IDAO> daos = new HashMap<String, IDAO>();

    /**
     * map from dbKey to DatabaseDefinition
     */
    private Map<String, DatabaseDefinition> definitions;

    public QueryServer()
    {
    }

    QueryServer(final ISessionManager<Session> sessionManager, final IDAOFactory daoFactory,
            final ISampleTypeSlaveServerPlugin sampleTypeSlaveServerPlugin,
            final IDataSetTypeSlaveServerPlugin dataSetTypeSlaveServerPlugin, IDAO dao)
    {
        super(sessionManager, daoFactory, sampleTypeSlaveServerPlugin, dataSetTypeSlaveServerPlugin);
    }

    public IQueryServer createLogger(IInvocationLoggerContext context)
    {
        return new QueryServerLogger(getSessionManager(), context);
    }

    public int initDatabases(String sessionToken)
    {
        checkSession(sessionToken);
        initDatabaseDefinitions();
        return getDatabaseDefinitions().size();
    }

    public List<QueryDatabase> listQueryDatabases(String sessionToken)
    {
        checkSession(sessionToken);

        final List<QueryDatabase> results = new ArrayList<QueryDatabase>();
        for (DatabaseDefinition definition : getDatabaseDefinitions().values())
        {
            results.add(new QueryDatabase(definition.getKey(), definition.getLabel()));
        }
        Collections.sort(results);
        return results;
    }

    public List<QueryExpression> listQueries(String sessionToken, QueryType queryType)
    {
        checkSession(sessionToken);

        try
        {
            List<QueryPE> queries = getDAOFactory().getQueryDAO().listQueries(queryType);
            return QueryTranslator.translate(queries, getDatabaseDefinitions());
        } catch (DataAccessException ex)
        {
            throw new UserFailureException(ex.getMostSpecificCause().getMessage(), ex);
        }
    }

    public void registerQuery(String sessionToken, NewQuery expression)
    {
        Session session = getSession(sessionToken);
        QueryAccessController.checkWriteAccess(session, expression.getQueryDatabase().getKey(),
                "create");

        QueryPE query = new QueryPE();
        query.setName(expression.getName());
        query.setDescription(expression.getDescription());
        query.setExpression(expression.getExpression());
        query.setPublic(expression.isPublic());
        query.setRegistrator(session.tryGetPerson());
        query.setQueryType(expression.getQueryType());
        query.setQueryDatabaseKey(expression.getQueryDatabase().getKey());
        try
        {
            getDAOFactory().getQueryDAO().createQuery(query);
        } catch (DataAccessException ex)
        {
            DataAccessExceptionTranslator.throwException(ex, "Query definition '"
                    + expression.getName() + "'", null);
        }
    }

    public void deleteQueries(String sessionToken, List<TechId> filterIds)
    {
        Session session = getSession(sessionToken);

        IQueryDAO queryDAO = getDAOFactory().getQueryDAO();
        try
        {
            for (TechId techId : filterIds)
            {
                QueryPE query = queryDAO.getByTechId(techId);
                QueryAccessController.checkWriteAccess(session, query.getQueryDatabaseKey(),
                        "delete");
                queryDAO.delete(query);
            }
        } catch (DataAccessException ex)
        {
            DataAccessExceptionTranslator.throwException(ex, "Query definition", null);
        }
    }

    public void updateQuery(String sessionToken, IQueryUpdates updates)
    {
        Session session = getSession(sessionToken);
        QueryAccessController.checkWriteAccess(session, updates.getQueryDatabase().getKey(),
                "update");

        try
        {
            IQueryDAO queryDAO = getDAOFactory().getQueryDAO();
            QueryPE query = queryDAO.getByTechId(TechId.create(updates));

            query.setName(updates.getName());
            query.setDescription(updates.getDescription());
            query.setExpression(updates.getExpression());
            query.setPublic(updates.isPublic());
            query.setQueryType(updates.getQueryType());
            query.setQueryDatabaseKey(updates.getQueryDatabase().getKey());

            queryDAO.validateAndSaveUpdatedEntity(query);
        } catch (DataAccessException ex)
        {
            DataAccessExceptionTranslator.throwException(ex, "Query definition '"
                    + updates.getName() + "'", null);
        }
    }

    public TableModel queryDatabase(String sessionToken, QueryDatabase database, String sqlQuery,
            QueryParameterBindings bindings)
    {
        Session session = getSession(sessionToken);
        try
        {
            String dbKey = database.getKey();
            QueryAccessController.checkWriteAccess(session, dbKey, "create and perform");
            return QueryAccessController.filterResults(session.tryGetPerson(), dbKey,
                    getDAOFactory(), queryDatabaseWithKey(dbKey, sqlQuery, bindings));
        } catch (DataAccessException ex)
        {
            throw new UserFailureException(ex.getMostSpecificCause().getMessage(), ex);
        }
    }

    public TableModel queryDatabase(String sessionToken, TechId queryId,
            QueryParameterBindings bindings)
    {
        Session session = getSession(sessionToken);
        try
        {
            IQueryDAO queryDAO = getDAOFactory().getQueryDAO();
            QueryPE query = queryDAO.getByTechId(queryId);
            String dbKey = query.getQueryDatabaseKey();
            String expression = StringEscapeUtils.unescapeHtml(query.getExpression());
            QueryAccessController.checkReadAccess(session, dbKey);
            return QueryAccessController.filterResults(session.tryGetPerson(), dbKey,
                    getDAOFactory(), queryDatabaseWithKey(dbKey, expression, bindings));
        } catch (DataAccessException ex)
        {
            throw new UserFailureException(ex.getMostSpecificCause().getMessage(), ex);
        }
    }

    private TableModel queryDatabaseWithKey(String dbKey, String sqlQuery,
            QueryParameterBindings bindings)
    {
        return getDAO(dbKey).query(sqlQuery, bindings);
    }

    private IDAO getDAO(String dbKey)
    {
        IDAO result = daos.get(dbKey);
        if (result == null)
        {
            DatabaseDefinition definition = getDatabaseDefinitions().get(dbKey);
            if (definition == null)
            {
                throw new UnsupportedOperationException("Undefined query database '" + dbKey + "'");
            }
            result = new DAO(definition.getConfigurationContext().getDataSource());
        }
        return result;
    }

    private Map<String, DatabaseDefinition> getDatabaseDefinitions()
    {
        if (definitions == null)
        {
            initDatabaseDefinitions();
        }
        return definitions;
    }

    private void initDatabaseDefinitions()
    {
        if (definitions != null)
        {
            return;
        }

        definitions = new HashMap<String, DatabaseDefinition>();
        Properties resolvedProps = configurer.getResolvedProps();
        SectionProperties[] sectionsProperties =
                PropertyParametersUtil
                        .extractSectionProperties(resolvedProps, DATABASE_KEYS, false);
        Set<String> labels = new HashSet<String>();
        for (int i = 0; i < sectionsProperties.length; i++)
        {
            final String databaseKey = sectionsProperties[i].getKey();
            final Properties databaseProperties = sectionsProperties[i].getProperties();

            final SimpleDatabaseConfigurationContext configurationContext =
                    new SimpleDatabaseConfigurationContext(databaseProperties);
            final String label =
                    PropertyUtils.getMandatoryProperty(databaseProperties, LABEL_PROPERTY_KEY);
            final String creatorMinimalRoleString =
                    PropertyUtils.getProperty(databaseProperties, CREATOR_MINIMAL_ROLE_KEY,
                            DEFAULT_CREATOR_MINIMAL_ROLE);
            final String dataSpaceOrNullString =
                    PropertyUtils.getProperty(databaseProperties, DATA_SPACE_KEY);

            if (labels.contains(label))
            {
                throw new UnsupportedOperationException(
                        "Query databases need to have unique labels but '" + label
                                + "' label is used more than once.");
            }
            labels.add(label);
            GroupPE dataSpaceOrNull = null;
            if (dataSpaceOrNullString != null)
            {
                dataSpaceOrNull =
                        getDAOFactory().getGroupDAO().tryFindGroupByCodeAndDatabaseInstance(
                                dataSpaceOrNullString, getDAOFactory().getHomeDatabaseInstance());
                if (dataSpaceOrNull == null)
                {
                    throw new UnsupportedOperationException("Query database '" + databaseKey
                            + "' is not defined properly. Space '" + dataSpaceOrNullString
                            + "' doesn't exist.");
                }
            }
            try
            {
                final RoleSet creatorMinimalRole = RoleSet.valueOf(creatorMinimalRoleString);
                definitions.put(databaseKey, new DatabaseDefinition(configurationContext,
                        databaseKey, label, creatorMinimalRole, dataSpaceOrNull));
            } catch (IllegalArgumentException ex)
            {
                throw new UnsupportedOperationException("Query database '" + databaseKey
                        + "' is not defined properly. '" + creatorMinimalRoleString
                        + "' is not a valid role.");
            }

        }
        QueryAccessController.initialize(definitions);
    }
}
