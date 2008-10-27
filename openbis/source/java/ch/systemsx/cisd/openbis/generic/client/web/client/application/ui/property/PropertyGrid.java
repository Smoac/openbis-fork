/*
 * Copyright 2007 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.property;

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.SourcesTableEvents;
import com.google.gwt.user.client.ui.TableListener;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IMessageProvider;

/**
 * A <code>HTMLTable</code> that displays a couple of properties.
 * 
 * @author Christian Ribeaud
 */
public final class PropertyGrid extends Grid
{
    private final IPropertyValueRenderer<Object> defaultPropertyValueRenderer;

    private final Map<Class<?>, IPropertyValueRenderer<?>> propertyValueRenderers =
            new HashMap<Class<?>, IPropertyValueRenderer<?>>();

    private final Map<String, GridCellListener> listeners = new HashMap<String, GridCellListener>();

    private Map<String, ?> properties;

    private final IMessageProvider messageProvider;

    public PropertyGrid(final IMessageProvider messageProvider, final int rows)
    {
        super(rows, 2);
        this.messageProvider = messageProvider;
        setStyleName("property-grid");
        getColumnFormatter().addStyleName(0, "header");
        defaultPropertyValueRenderer = new ObjectPropertyValueRenderer(messageProvider);
        addTableListener(new PropertyGridListener());
        registerDefaultPropertyValueRenderers();
    }

    /** Registers default <code>PropertyValueRenderer</code>. */
    private final void registerDefaultPropertyValueRenderers()
    {
        propertyValueRenderers.put(Date.class, new DatePropertyValueRenderer(messageProvider));
    }

    /**
     * For given <var>className</var> returns the corresponding <code>PropertyValueRenderer</code>
     * or {@link #defaultPropertyValueRenderer} if:
     * <ul>
     * <li>none is found</li>
     * <li>given <var>clazz</var> is <code>null</code></li>
     * </ul>
     */
    @SuppressWarnings("unchecked")
    private final <T> IPropertyValueRenderer<? super T> getPropertyValueRenderer(final T value)
    {
        if (value == null)
        {
            return defaultPropertyValueRenderer;
        }
        final Class<T> clazz = (Class<T>) value.getClass();
        final IPropertyValueRenderer<T> renderer =
                (IPropertyValueRenderer<T>) propertyValueRenderers.get(clazz);
        if (renderer == null)
        {
            if (clazz.isArray())
            {
                final T[] array = (T[]) value;
                if (array.length > 0)
                {
                    return new ObjectArrayPropertyValueRenderer(messageProvider,
                            getPropertyValueRenderer(array[0]));
                } else
                {
                    return new ObjectArrayPropertyValueRenderer(messageProvider,
                            defaultPropertyValueRenderer);
                }
            } else
            {
                return defaultPropertyValueRenderer;
            }
        }
        return renderer;
    }

    /** For given <var>clazz</var> registers given <var>propertyValueRenderer</var>. */
    public final <T> void registerPropertyValueRenderer(final Class<T> clazz,
            final IPropertyValueRenderer<T> propertyValueRenderer)
    {
        propertyValueRenderers.put(clazz, propertyValueRenderer);
    }

    /** Unregisters <code>PropertyValueRenderer</code> for given <var>clazz</var>. */
    public final <T> void unregisterPropertyValueRenderer(final Class<T> clazz)
    {
        propertyValueRenderers.remove(clazz);
    }

    /**
     * Adds a <code>GridCellListener</code> for given <var>row</var>.
     * <p>
     * If there is already a <code>GridCellListener</code> registered for given <var>row</var>,
     * the new one will replace it.
     * </p>
     */
    public final void addGridCellListener(final String key, final GridCellListener listener)
    {
        assert key != null : "Unspecified key.";
        assert listener != null : "Undefined GridCellListener.";
        listeners.put(key, listener);
    }

    /**
     * Sets the properties that are going to be displayed here.
     */
    public final <T> void setProperties(final Map<String, ? super T> properties)
    {
        this.properties = properties;
        fillTable();
    }

    private final <T> String renderValue(final T value)
    {
        final IPropertyValueRenderer<? super T> propertyValueRenderer =
                getPropertyValueRenderer(value);
        return propertyValueRenderer.render(value);
    }

    private final void fillTable()
    {
        assert properties != null : "Unspecified properties.";
        int row = 0;
        for (final Iterator<String> iterator = properties.keySet().iterator(); iterator.hasNext(); row++)
        {
            final String key = iterator.next();
            setHTML(row, 0, key);
            setHTML(row, 1, renderValue(properties.get(key)));
        }
    }

    //
    // Helper classes
    //

    private final class PropertyGridListener implements TableListener
    {

        //
        // TableListener
        //

        public final void onCellClicked(final SourcesTableEvents sender, final int row,
                final int column)
        {
            // Only for property value (index 1).
            if (column == 1)
            {
                final String cellValue = getText(row, column);
                final GridCellListener listener = listeners.get(cellValue);
                if (listener != null)
                {
                    listener.onCellClicked();
                }
            }
        }

    }

    /** Event listener interface for property table events. */
    public static interface GridCellListener
    {

        /** Fired when a property value cell is clicked. */
        public void onCellClicked();
    }

}