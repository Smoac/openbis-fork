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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.framework;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.framework.renderers.SimpleDateRenderer;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.framework.renderers.SimplePersonRenderer;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.framework.renderers.SimpleYesNoRenderer;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.GridRowModel;
import ch.systemsx.cisd.openbis.generic.shared.basic.IInvalidationProvider;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AbstractRegistrationHolder;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Person;

/**
 * Abstract superclass which holds meta data. Subclasses must implement
 * {@link AbstractColumnDefinitionKind#tryGetValue(Object)}.
 * <p>
 * Usually this class is extended anonymously in {@link IColumnDefinitionKind} implementation and
 * used as a data holder. It is usefull because all {@link IColumnDefinitionKind} implementations
 * are similar, but cannot extend a class directly (they are enums, not classes).
 * </p>
 * 
 * @author Franz-Josef Elmer
 */
public abstract class AbstractColumnDefinitionKind<T>
{
    /** Renders the cell value of this column for the specified entity. */
    abstract public String tryGetValue(T entity);

    public static final int DATE_COLUMN_WIDTH = 300;

    public static final int DEFAULT_COLUMN_WIDTH = 150;

    private String headerMsgKey;

    private int width;

    private boolean isHidden;

    public AbstractColumnDefinitionKind(final String headerMsgKey, final int width,
            final boolean isHidden)
    {
        this.headerMsgKey = headerMsgKey;
        this.width = width;
        this.isHidden = isHidden;
    }

    public AbstractColumnDefinitionKind(final String headerMsgKey, final boolean isHidden)
    {
        this(headerMsgKey, DEFAULT_COLUMN_WIDTH, isHidden);
    }

    public AbstractColumnDefinitionKind(final String headerMsgKey, final int width)
    {
        this(headerMsgKey, width, false);
    }

    public AbstractColumnDefinitionKind(final String headerMsgKey)
    {
        this(headerMsgKey, false);
    }

    /** Returns default column width. */
    public int getWidth()
    {
        return width;
    }

    /** Returns <code>true</code> if this column should initially be hidden. */
    public boolean isHidden()
    {
        return isHidden;
    }

    /** Returns key in the translations dictionary. */
    public String getHeaderMsgKey()
    {
        return headerMsgKey;
    }

    public Comparable<?> getComparableValue(GridRowModel<T> entity)
    {
        String value = tryGetValue(entity.getOriginalObject());
        return value == null ? "" : value;
    }

    protected final String renderRegistrationDate(final AbstractRegistrationHolder entity)
    {
        return SimpleDateRenderer.renderDate(entity.getRegistrationDate());
    }

    protected final String renderRegistrator(final AbstractRegistrationHolder entity)
    {
        return renderRegistratorPerson(entity.getRegistrator());
    }

    protected final String renderRegistratorPerson(final Person person)
    {
        return SimplePersonRenderer.createPersonName(person).toString();
    }

    protected final String renderInvalidationFlag(final IInvalidationProvider invalidationProvider)
    {
        return SimpleYesNoRenderer.render(invalidationProvider.getInvalidation() != null);
    }
}
