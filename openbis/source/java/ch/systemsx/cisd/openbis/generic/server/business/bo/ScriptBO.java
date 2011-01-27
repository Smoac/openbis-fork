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

package ch.systemsx.cisd.openbis.generic.server.business.bo;

import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataRetrievalFailureException;

import ch.rinn.restrictions.Private;
import ch.systemsx.cisd.common.evaluator.Evaluator;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IScriptUpdates;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Script;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ScriptType;
import ch.systemsx.cisd.openbis.generic.shared.dto.EntityTypePropertyTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ScriptPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;
import ch.systemsx.cisd.openbis.generic.shared.managed_property.ManagedPropertyEvaluator;

/**
 * The only productive implementation of {@link IScriptBO}. We are using an interface here to keep
 * the system testable.
 * 
 * @author Izabela Adamczyk
 */
public final class ScriptBO extends AbstractBusinessObject implements IScriptBO
{

    private ScriptPE script;

    private final IScriptFactory scriptFactory;

    public ScriptBO(final IDAOFactory daoFactory, final Session session)
    {
        this(daoFactory, session, new ScriptFactory());
    }

    @Private
    // for testing
    ScriptBO(final IDAOFactory daoFactory, final Session session, IScriptFactory scriptFactory)
    {
        super(daoFactory, session);
        this.scriptFactory = scriptFactory;
    }

    @Private
    interface IScriptFactory
    {
        ScriptPE create();
    }

    private static class ScriptFactory implements IScriptFactory
    {
        public ScriptPE create()
        {
            return new ScriptPE();
        }
    }

    public void deleteByTechId(TechId groupId) throws UserFailureException
    {
        loadDataByTechId(groupId);
        try
        {
            getScriptDAO().delete(script);
        } catch (final DataAccessException ex)
        {
            throwException(ex, String.format("Script '%s'", script.getName()));
        }
    }

    public void loadDataByTechId(TechId id)
    {
        try
        {
            script = getScriptDAO().getByTechId(id);
        } catch (DataRetrievalFailureException exception)
        {
            throw new UserFailureException(exception.getMessage());
        }
    }

    public void save() throws UserFailureException
    {
        assert script != null : "Script not defined";
        try
        {
            checkScriptCompilation(script.getScriptType(), script.getScript());
            getScriptDAO().createOrUpdate(script);
        } catch (final DataAccessException e)
        {
            throwException(e, "Script '" + script.getName() + "'");
        }
    }

    public void define(Script newScript) throws UserFailureException
    {
        assert newScript != null : "Unspecified script.";
        script = scriptFactory.create();
        script.setDatabaseInstance(getHomeDatabaseInstance());
        script.setName(newScript.getName());
        script.setDescription(newScript.getDescription());
        script.setRegistrator(findRegistrator());
        script.setScript(newScript.getScript());
        script.setScriptType(newScript.getScriptType());
        script.setEntityKind(newScript.getEntityKind());
    }

    public void update(IScriptUpdates updates)
    {
        loadDataByTechId(TechId.create(updates));
        script.setName(updates.getName());
        script.setDescription(updates.getDescription());
        boolean scriptChanged = false;
        if (script.getScript().equals(updates.getScript()) == false)
        {
            scriptChanged = true;
            script.setScript(updates.getScript());
            checkScriptCompilation(script.getScriptType(), updates.getScript());
        }
        getScriptDAO().createOrUpdate(script);

        script.getScript();
        if (scriptChanged && script.isDynamic())
        {
            for (EntityTypePropertyTypePE assignment : script.getPropertyAssignments())
            {
                getEntityPropertyTypeDAO(assignment.getEntityType().getEntityKind())
                        .scheduleDynamicPropertiesEvaluation(assignment);
            }
        }
    }

    private void checkScriptCompilation(ScriptType scriptType, String scriptExpression)
    {
        Evaluator.checkScriptCompilation(scriptExpression);
        if (scriptType == ScriptType.MANAGED_PROPERTY)
        {
            // constructor checks script
            new ManagedPropertyEvaluator(scriptExpression);
        }
    }

}
