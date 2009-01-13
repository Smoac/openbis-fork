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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.sample.columns;

import java.util.Date;

import ch.systemsx.cisd.openbis.generic.client.shared.DatabaseInstance;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.sample.renderer.SimpleDateRenderer;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.sample.renderer.SimplePersonRenderer;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.sample.renderer.SimpleYesNoRenderer;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.Group;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.Person;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.Procedure;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.Sample;

public class CommonSampleColDef extends AbstractSampleColDef
{
    protected CommonSampleColDefKind columnKind;

    // GWT
    public CommonSampleColDef()
    {
        this.columnKind = null;
    }

    public CommonSampleColDef(final CommonSampleColDefKind columnKind, final String headerText)
    {
        super(headerText, columnKind.getWidth(), columnKind.isHidden());
        this.columnKind = columnKind;
    }

    public CommonSampleColDefKind getColumnKind()
    {
        return columnKind;
    }

    public String getIdentifier()
    {
        return columnKind.id();
    }

    @Override
    protected String tryGetValue(final Sample sample)
    {
        final Experiment exp = tryToGetExperiment(sample);
        switch (columnKind)
        {
            case DATABASE_INSTANCE:
                return printDatabaseInstance(sample);
            case GROUP:
                return printGroup(sample);
            case CODE:
                return sample.getCode();
            case SAMPLE_IDENTIFIER:
                return sample.getIdentifier();
            case IS_INSTANCE_SAMPLE:
                return fromBoolean(sample.getDatabaseInstance() != null);
            case REGISTRATOR:
                return renderPerson(sample.getRegistrator());
            case REGISTRATION_DATE:
                return renderDate(sample.getRegistrationDate());
            case IS_INVALID:
                return fromBoolean(sample.getInvalidation() != null);
            case PROJECT_FOR_SAMPLE:
                return exp == null ? null : exp.getProject().getCode();
            case EXPERIMENT_FOR_SAMPLE:
                return exp == null ? null : exp.getCode();
            case EXPERIMENT_IDENTIFIER_FOR_SAMPLE:
                return exp == null ? null : exp.getIdentifier();
            default:
                throw new IllegalStateException("unhandled column " + this);
        }
    }

    private static String renderDate(final Date date)
    {
        return SimpleDateRenderer.renderDate(date);
    }

    private static String renderPerson(final Person person)
    {
        return SimplePersonRenderer.createPersonName(person).toString();
    }

    private static String fromBoolean(final boolean b)
    {
        return SimpleYesNoRenderer.render(b);
    }

    private final static Experiment tryToGetExperiment(final Sample sample)
    {
        final Procedure procedure = sample.getValidProcedure();
        if (procedure != null)
        {
            return procedure.getExperiment();
        }
        return null;
    }

    private final static String printGroup(final Sample sample)
    {
        final Group group = sample.getGroup();
        return group == null ? "" : group.getCode();
    }

    private final static String printDatabaseInstance(final Sample sample)
    {
        DatabaseInstance databaseInstance = sample.getDatabaseInstance();
        if (databaseInstance == null)
        {
            databaseInstance = sample.getGroup().getInstance();
        }
        return databaseInstance.getCode();
    }
}