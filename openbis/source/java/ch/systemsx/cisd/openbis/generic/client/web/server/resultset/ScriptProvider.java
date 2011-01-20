/*
 * Copyright 2011 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.client.web.server.resultset;

import static ch.systemsx.cisd.openbis.generic.client.web.client.dto.ScriptGridColumnIDs.DESCRIPTION;
import static ch.systemsx.cisd.openbis.generic.client.web.client.dto.ScriptGridColumnIDs.ENTITY_KIND;
import static ch.systemsx.cisd.openbis.generic.client.web.client.dto.ScriptGridColumnIDs.NAME;
import static ch.systemsx.cisd.openbis.generic.client.web.client.dto.ScriptGridColumnIDs.REGISTRATION_DATE;
import static ch.systemsx.cisd.openbis.generic.client.web.client.dto.ScriptGridColumnIDs.REGISTRATOR;
import static ch.systemsx.cisd.openbis.generic.client.web.client.dto.ScriptGridColumnIDs.SCRIPT;
import static ch.systemsx.cisd.openbis.generic.client.web.client.dto.ScriptGridColumnIDs.SCRIPT_TYPE;

import java.util.List;

import ch.systemsx.cisd.openbis.generic.shared.ICommonServer;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Script;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ScriptType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TypedTableModel;
import ch.systemsx.cisd.openbis.generic.shared.util.TypedTableModelBuilder;

/**
 * Provider of {@link Script} instances.
 *
 * @author Franz-Josef Elmer
 */
public class ScriptProvider extends AbstractCommonTableModelProvider<Script>
{
    private final ScriptType scriptTypeOrNull;
    private final EntityKind entityKindOrNull;

    public ScriptProvider(ICommonServer commonServer, String sessionToken, ScriptType scriptTypeOrNull,
            EntityKind entityKindOrNull)
    {
        super(commonServer, sessionToken);
        this.scriptTypeOrNull = scriptTypeOrNull;
        this.entityKindOrNull = entityKindOrNull;
    }

    @Override
    protected TypedTableModel<Script> createTableModel(int maxSize)
    {
        List<Script> scripts = commonServer.listScripts(sessionToken, scriptTypeOrNull, entityKindOrNull);
        TypedTableModelBuilder<Script> builder = new TypedTableModelBuilder<Script>();
        builder.addColumn(NAME);
        builder.addColumn(DESCRIPTION);
        builder.addColumn(SCRIPT);
        builder.addColumn(ENTITY_KIND);
        builder.addColumn(SCRIPT_TYPE);
        builder.addColumn(REGISTRATOR);
        builder.addColumn(REGISTRATION_DATE).withDefaultWidth(300);
        for (Script script : scripts)
        {
            builder.addRow(script);
            builder.column(NAME).addString(script.getName());
            builder.column(DESCRIPTION).addString(script.getDescription());
            builder.column(SCRIPT).addString(script.getScript());
            EntityKind kind = script.getEntityKind();
            builder.column(ENTITY_KIND).addString(kind == null ? "All" : kind.getDescription());
            builder.column(SCRIPT_TYPE).addString(script.getScriptType().getDescription());
            builder.column(REGISTRATOR).addPerson(script.getRegistrator());
            builder.column(REGISTRATION_DATE).addDate(script.getRegistrationDate());
        }
        return builder.getModel();
    }

}
