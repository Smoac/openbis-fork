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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.model.renderer;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.framework.EntityPropertyColDef;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.framework.IColumnDefinitionUI;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataTypeCode;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityPropertiesHolder;

/**
 * An abstract decorator for {@link EntityPropertyColDef} to be used in grids for rendering values
 * in a different way in grids than in export. <br>
 * It changes {@link EntityPropertyColDef#getValue(Object)} behavior distinguishing certain property
 * types so in grid there can be e.g. a link displayed for hyperlink property. Other methods are
 * delegated without any change.
 * 
 * @author Piotr Buczek
 */
public abstract class AbstractPropertyColRenderer<T extends IEntityPropertiesHolder> implements
        IColumnDefinitionUI<T>
{

    /**
     * @return property renderer for given column of given entity
     */
    public static <S extends IEntityPropertiesHolder> AbstractPropertyColRenderer<S> getPropertyColRenderer(
            EntityPropertyColDef<S> colDef)
    {
        switch (colDef.getDataTypeCode())
        {
            case HYPERLINK:
                return new HyperlinkPropertyColRenderer<S>(colDef);
            case MULTILINE_VARCHAR:
                return new MultilineVarcharPropertyColRenderer<S>(colDef);
            case CONTROLLEDVOCABULARY:
                return new VocabularyPropertyColRenderer<S>(colDef);
            case TIMESTAMP:
                return new TimestampPropertyColRenderer<S>(colDef);
            default:
                return new DefaultPropertyColRenderer<S>(colDef);
        }
    }

    protected final EntityPropertyColDef<T> colDef;

    public AbstractPropertyColRenderer(EntityPropertyColDef<T> colDef)
    {
        super();
        this.colDef = colDef;
    }

    public String getValue(T entity)
    {
        return renderValue(entity);
    }

    public Comparable<?> getComparableValue(T rowModel)
    {
        return colDef.getComparableValue(rowModel);
    }

    /**
     * @return given <var>value</var> rendered depending on property type
     */
    protected abstract String renderValue(T entity);

    // default delegate methods

    public String getHeader()
    {
        return colDef.getHeader();
    }

    public String getIdentifier()
    {
        return colDef.getIdentifier();
    }

    public int getWidth()
    {
        return colDef.getWidth();
    }

    public boolean isHidden()
    {
        return colDef.isHidden();
    }

    public boolean isNumeric()
    {
        return colDef.isNumeric();
    }

    public DataTypeCode getDataTypeCode()
    {
        return colDef.getDataTypeCode();
    }

}
