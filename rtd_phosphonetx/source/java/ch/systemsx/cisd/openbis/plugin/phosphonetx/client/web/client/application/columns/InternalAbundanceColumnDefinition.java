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

import java.util.Map;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.framework.AbstractColumnDefinition;
import ch.systemsx.cisd.openbis.generic.shared.basic.GridRowModel;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.basic.dto.ProteinInfo;

public final class InternalAbundanceColumnDefinition extends AbstractColumnDefinition<ProteinInfo>
{
    private static final Double ZERO = new Double(1e-40);

    private long sampleID;

    private Map<String, String> properties;

    // GWT only
    public InternalAbundanceColumnDefinition()
    {
        super();
    }

    public InternalAbundanceColumnDefinition(String headerTextOrNull,
            Map<String, String> propertiesOrNull, int width, boolean isHidden, long sampleID)
    {
        super(headerTextOrNull, width, isHidden);
        this.properties = propertiesOrNull;
        this.sampleID = sampleID;
    }

    public String getIdentifier()
    {
        return "abundance-" + Long.toString(sampleID);
    }

    @Override
    protected String tryGetValue(ProteinInfo entity)
    {
        Double abundance = tryToGetAbundance(entity);
        return abundance == null ? null : Double.toString(abundance);
    }

    @Override
    public Comparable<?> getComparableValue(GridRowModel<ProteinInfo> rowModel)
    {
        Double abundance = tryToGetAbundance(rowModel.getOriginalObject());
        return abundance == null ? ZERO : abundance;
    }

    @Override
    public boolean isNumeric()
    {
        return true;
    }

    @Override
    public String tryToGetProperty(String key)
    {
        return properties == null ? null : properties.get(key);
    }

    private Double tryToGetAbundance(ProteinInfo entity)
    {
        Map<Long, Double> abundances = entity.getAbundances();
        if (abundances == null)
        {
            return null;
        }
        return abundances.get(sampleID);
    }

}