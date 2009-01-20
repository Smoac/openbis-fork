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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.sample.renderer.SimpleDateRenderer;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.sample.renderer.SimplePersonRenderer;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.sample.renderer.SimpleYesNoRenderer;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.AbstractRegistrationHolder;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.IInvalidationProvider;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.Person;

/**
 * Abstract superclass which holds meta data. Subclasses must implement
 * {@link IColumnDefinitionKind#tryGetValue(Object)}.
 * 
 * @author Franz-Josef Elmer
 */
public abstract class AbstractColumnDefinitionKind<T> implements IColumnDefinitionKind<T>
{
    // TODO 2008-12-08, Tomasz Pylak: refactor the code to remove this field. It has to have the
    // same name as the Sample field because grid sorting was implemented in that ugly way.
    private String sortField;

    private String headerMsgKey;

    private int width;

    private boolean isHidden;

    public AbstractColumnDefinitionKind(final String sortField, final String headerMsgKey,
            final int width, final boolean isHidden)
    {
        this.sortField = sortField;
        this.headerMsgKey = headerMsgKey;
        this.width = width;
        this.isHidden = isHidden;
    }

    public AbstractColumnDefinitionKind(final String sortField, final String headerMsgKey,
            final boolean isHidden)
    {
        this(sortField, headerMsgKey, AbstractColumnDefinition.DEFAULT_COLUMN_WIDTH, isHidden);
    }

    public AbstractColumnDefinitionKind(final String sortField, final String headerMsgKey)
    {
        this(sortField, headerMsgKey, false);
    }

    public int getWidth()
    {
        return width;
    }

    public boolean isHidden()
    {
        return isHidden;
    }

    // key in the translations dictionary
    public String getHeaderMsgKey()
    {
        return headerMsgKey;
    }

    public String id()
    {
        return sortField; // NOTE: it should be possible to use name() when sorting will be fixed
    }

    protected String renderRegistrationDate(final AbstractRegistrationHolder entity)
    {
        return SimpleDateRenderer.renderDate(entity.getRegistrationDate());
    }

    protected String renderRegistrator(final AbstractRegistrationHolder entity)
    {
        return renderRegistrator(entity.getRegistrator());
    }

    protected String renderRegistrator(final Person person)
    {
        return SimplePersonRenderer.createPersonName(person).toString();
    }

    protected String renderInvalidationFlag(final IInvalidationProvider invalidationProvider)
    {
        return SimpleYesNoRenderer.render(invalidationProvider.getInvalidation() != null);
    }

}
