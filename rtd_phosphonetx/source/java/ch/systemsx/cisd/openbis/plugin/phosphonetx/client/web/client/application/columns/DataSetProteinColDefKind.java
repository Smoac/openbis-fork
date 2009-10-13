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

package ch.systemsx.cisd.openbis.plugin.phosphonetx.client.web.client.application.columns;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.framework.AbstractColumnDefinitionKind;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.framework.IColumnDefinitionKind;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.GridRowModel;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.basic.dto.DataSetProtein;

/**
 * @author Franz-Josef Elmer
 */
public enum DataSetProteinColDefKind implements IColumnDefinitionKind<DataSetProtein>
{
    DATA_SET_PERM_ID(new AbstractColumnDefinitionKind<DataSetProtein>(Dict.DATA_SET_PERM_ID, 200)
        {
            @Override
            public String tryGetValue(DataSetProtein entity)
            {
                return entity.getDataSetPermID();
            }
        }), SEQUENCE_NAME(new AbstractColumnDefinitionKind<DataSetProtein>(Dict.SEQUENCE_NAME, 80)
        {
            @Override
            public String tryGetValue(DataSetProtein entity)
            {
                return entity.getSequenceName();
            }
        }),

    PEPTIDE_COUNT(new AbstractColumnDefinitionKind<DataSetProtein>(Dict.PEPTIDE_COUNT, 80)
        {
            @Override
            public String tryGetValue(DataSetProtein entity)
            {
                return Integer.toString(entity.getPeptideCount());
            }

            @Override
            public Comparable<?> getComparableValue(GridRowModel<DataSetProtein> entity)
            {
                return entity.getOriginalObject().getPeptideCount();
            }
        }),

    FDR(new AbstractColumnDefinitionKind<DataSetProtein>(Dict.FDR, 80)
        {
            @Override
            public String tryGetValue(DataSetProtein entity)
            {
                int perMille = (int) (1000 * entity.getFalseDiscoveryRate() + 0.5);
                return (perMille / 10) + "." + (perMille % 10) + " %";
            }

            @Override
            public Comparable<?> getComparableValue(GridRowModel<DataSetProtein> entity)
            {
                return entity.getOriginalObject().getFalseDiscoveryRate();
            }
        }),

    ;

    private final AbstractColumnDefinitionKind<DataSetProtein> columnDefinitionKind;

    private DataSetProteinColDefKind(
            AbstractColumnDefinitionKind<DataSetProtein> columnDefinitionKind)
    {
        this.columnDefinitionKind = columnDefinitionKind;
    }

    public String id()
    {
        return name();
    }

    public AbstractColumnDefinitionKind<DataSetProtein> getDescriptor()
    {
        return columnDefinitionKind;
    }

}
