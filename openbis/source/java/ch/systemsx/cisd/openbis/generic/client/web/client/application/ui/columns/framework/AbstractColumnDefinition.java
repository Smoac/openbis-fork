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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.framework;

/**
 * @author Tomasz Pylak
 */
public abstract class AbstractColumnDefinition<T> implements IColumnDefinitionUI<T>
{
    protected abstract String tryGetValue(T entity);

    private String headerText;

    private int width;

    private boolean isHidden;

    // GWT only
    public AbstractColumnDefinition()
    {
        this(null, 0, false);
    }

    /**
     * if headerTextOrNull is null, it means that we never want to call {@link #getHeader()} method
     */
    protected AbstractColumnDefinition(String headerTextOrNull, int width, boolean isHidden)
    {
        this.headerText = headerTextOrNull;
        this.width = width;
        this.isHidden = isHidden;
    }

    public int getWidth()
    {
        return width;
    }

    public boolean isHidden()
    {
        return isHidden;
    }

    public String getHeader()
    {
        assert headerText != null : "header not specified but requested";
        return headerText;
    }

    public String getValue(T entity)
    {
        String value = tryGetValue(entity);
        return value != null ? value : "";
    }

    public Comparable<?> getComparableValue(T rowModel)
    {
        return getValue(rowModel);
    }

    
}