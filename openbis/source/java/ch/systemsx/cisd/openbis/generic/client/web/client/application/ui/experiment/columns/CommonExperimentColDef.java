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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.experiment.columns;

import java.util.Date;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.AbstractColumnDefinition;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.sample.renderer.SimpleDateRenderer;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.sample.renderer.SimplePersonRenderer;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.sample.renderer.SimpleYesNoRenderer;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.Person;

/**
 * Definition of experiment table columns together with the instructions to render each column
 * value.
 * 
 * @author Tomasz Pylak
 */
public class CommonExperimentColDef extends AbstractColumnDefinition<Experiment>
{
    protected CommonExperimentColDefKind columnKind;

    // GWT
    public CommonExperimentColDef()
    {
        this.columnKind = null;
    }

    public CommonExperimentColDef(final CommonExperimentColDefKind columnKind,
            final String headerText)
    {
        super(headerText, columnKind.getWidth(), columnKind.isHidden());
        this.columnKind = columnKind;
    }

    public CommonExperimentColDefKind getColumnKind()
    {
        return columnKind;
    }

    public String getIdentifier()
    {
        return columnKind.id();
    }

    @Override
    protected String tryGetValue(final Experiment entity)
    {
        switch (columnKind)
        {
            case CODE:
                return entity.getCode();
            case EXPERIMENT_TYPE:
                return entity.getExperimentType().getCode();
            case EXPERIMENT_IDENTIFIER:
                return entity.getIdentifier();
            case GROUP:
                return entity.getProject().getGroup().getCode();
            case PROJECT:
                return entity.getProject().getCode();
            case REGISTRATOR:
                return renderPerson(entity.getRegistrator());
            case REGISTRATION_DATE:
                return renderDate(entity.getRegistrationDate());
            case IS_INVALID:
                return fromBoolean(entity.getInvalidation() != null);
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
}