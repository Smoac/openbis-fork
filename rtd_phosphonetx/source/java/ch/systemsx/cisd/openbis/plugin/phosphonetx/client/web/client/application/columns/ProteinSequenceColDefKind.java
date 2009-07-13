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
import ch.systemsx.cisd.openbis.plugin.phosphonetx.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.basic.dto.ProteinSequence;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public enum ProteinSequenceColDefKind implements IColumnDefinitionKind<ProteinSequence>
{
    SHORT_NAME(new AbstractColumnDefinitionKind<ProteinSequence>(Dict.SEQUENCE_SHORT_NAME, 20)
            {
                @Override
                public String tryGetValue(ProteinSequence entity)
                {
                    return entity.getShortName();
                }
            }), 
    DATABASE(new AbstractColumnDefinitionKind<ProteinSequence>(Dict.DATABASE_NAME_AND_VERSION)
            {
                @Override
                public String tryGetValue(ProteinSequence entity)
                {
                    return entity.getDatabaseNameAndVersion();
                }
            }), 
    SEQUENCE(new AbstractColumnDefinitionKind<ProteinSequence>(Dict.SEQUENCE, 400)
            {
                @Override
                public String tryGetValue(ProteinSequence entity)
                {
                    return entity.getSequence();
                }
            }), 
    ;
    
    private final AbstractColumnDefinitionKind<ProteinSequence> columnDefinitionKind;

    private ProteinSequenceColDefKind(AbstractColumnDefinitionKind<ProteinSequence> columnDefinitionKind)
    {
        this.columnDefinitionKind = columnDefinitionKind;
    }

    public String id()
    {
        return name();
    }

    public AbstractColumnDefinitionKind<ProteinSequence> getDescriptor()
    {
        return columnDefinitionKind;
    }

}
