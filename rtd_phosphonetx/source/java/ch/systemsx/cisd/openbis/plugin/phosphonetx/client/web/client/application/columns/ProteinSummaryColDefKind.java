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
import ch.systemsx.cisd.openbis.generic.shared.basic.GridRowModel;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.basic.dto.ProteinSummary;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public enum ProteinSummaryColDefKind implements IColumnDefinitionKind<ProteinSummary>
{
    FDR(new AbstractColumnDefinitionKind<ProteinSummary>(Dict.FDR)
        {
            @Override
            public String tryGetValue(ProteinSummary entity)
            {
                return Double.toString(entity.getFDR());
            }

            @Override
            public Comparable<?> getComparableValue(GridRowModel<ProteinSummary> entity)
            {
                return entity.getOriginalObject().getFDR();
            }
        }), PROTEIN_COUNT(new AbstractColumnDefinitionKind<ProteinSummary>(Dict.PROTEIN_COUNT)
        {
            @Override
            public String tryGetValue(ProteinSummary entity)
            {
                return Integer.toString(entity.getProteinCount());
            }

            @Override
            public Comparable<?> getComparableValue(GridRowModel<ProteinSummary> entity)
            {
                return entity.getOriginalObject().getProteinCount();
            }
        }), PEPTIDE_COUNT(new AbstractColumnDefinitionKind<ProteinSummary>(Dict.PEPTIDE_COUNT)
        {
            @Override
            public String tryGetValue(ProteinSummary entity)
            {
                return Integer.toString(entity.getPeptideCount());
            }

            @Override
            public Comparable<?> getComparableValue(GridRowModel<ProteinSummary> entity)
            {
                return entity.getOriginalObject().getPeptideCount();
            }
        }),            ;
    
    private final AbstractColumnDefinitionKind<ProteinSummary> columnDefinitionKind;

    private ProteinSummaryColDefKind(AbstractColumnDefinitionKind<ProteinSummary> columnDefinitionKind)
    {
        this.columnDefinitionKind = columnDefinitionKind;
    }

    public String id()
    {
        return name();
    }

    public AbstractColumnDefinitionKind<ProteinSummary> getDescriptor()
    {
        return columnDefinitionKind;
    }


}
