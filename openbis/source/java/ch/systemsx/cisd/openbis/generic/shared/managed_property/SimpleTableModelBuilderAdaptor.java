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

package ch.systemsx.cisd.openbis.generic.shared.managed_property;

import java.util.Date;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModel;
import ch.systemsx.cisd.openbis.generic.shared.managed_property.api.IRowBuilderAdaptor;
import ch.systemsx.cisd.openbis.generic.shared.managed_property.api.ISimpleTableModelBuilderAdaptor;
import ch.systemsx.cisd.openbis.generic.shared.util.IRowBuilder;
import ch.systemsx.cisd.openbis.generic.shared.util.SimpleTableModelBuilder;

/**
 * An {@link ISimpleTableModelBuilderAdaptor} implementation adapting
 * {@link SimpleTableModelBuilder}.
 * <p>
 * The intent of the interface and adaptor is not to expose {@link SimpleTableModelBuilder}
 * interface in Managed Property API and optionally add convenience methods for scripts.
 * 
 * @author Piotr Buczek
 */
public class SimpleTableModelBuilderAdaptor implements ISimpleTableModelBuilderAdaptor
{

    private final SimpleTableModelBuilder builder;

    public static SimpleTableModelBuilderAdaptor create()
    {
        return new SimpleTableModelBuilderAdaptor(new SimpleTableModelBuilder(true));
    }

    private SimpleTableModelBuilderAdaptor(SimpleTableModelBuilder builder)
    {
        this.builder = builder;
    }

    //
    // SimpleTableModelBuilder delegated methods
    //

    // NOTE: TableModel is exposed to keep dependencies simple
    public TableModel getTableModel()
    {
        return builder.getTableModel();
    }

    public void addHeader(String title)
    {
        builder.addHeader(title);
    }

    public void addHeader(String title, String code)
    {
        builder.addHeader(title, code);
    }

    public void addHeader(String title, int defaultColumnWidth)
    {
        builder.addHeader(title, defaultColumnWidth);
    }

    public IRowBuilderAdaptor addRow()
    {
        final IRowBuilder row = builder.addRow();
        return new IRowBuilderAdaptor()
            {

                public void setCell(String headerTitle, String value)
                {
                    row.setCell(headerTitle, value);
                }

                public void setCell(String headerTitle, long value)
                {
                    row.setCell(headerTitle, value);
                }

                public void setCell(String headerTitle, double value)
                {
                    row.setCell(headerTitle, value);
                }

                public void setCell(String headerTitle, Date value)
                {
                    row.setCell(headerTitle, value);
                }

            };
    }

}
