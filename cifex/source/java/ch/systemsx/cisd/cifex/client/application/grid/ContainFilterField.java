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

package ch.systemsx.cisd.cifex.client.application.grid;

import java.util.Collections;
import java.util.List;

import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.widget.grid.GridCellRenderer;

import ch.systemsx.cisd.cifex.client.application.ui.IGridCellRendererNonPlainText;
import ch.systemsx.cisd.cifex.client.application.utils.ObjectUtils;
import ch.systemsx.cisd.cifex.client.application.utils.StringUtils;

/**
 * A field to filter grid rows by the value of one specified column.
 * 
 * @author Tomasz Pylak
 */
public class ContainFilterField<M extends ModelData> extends AbstractFilterField<M>
{
    private static final String PREFIX_NOT = "!";

    private static final String PREFIX_START = "^";

    private static final String SUFFIX_END = "$";

    private final GridCellRenderer<ModelData> rendererOrNull;
    
    private List<String> alternatives = Collections.emptyList();

    public ContainFilterField(String filteredPropertyKey, String title,
            GridCellRenderer<ModelData> rendererOrNull)
    {
        super(filteredPropertyKey, title);
        if (rendererOrNull instanceof IGridCellRendererNonPlainText<?>)
        {
            this.rendererOrNull =
                    ((IGridCellRendererNonPlainText<ModelData>) rendererOrNull)
                            .getPlainTextRenderer();
        } else
        {
            this.rendererOrNull = rendererOrNull;
        }
    }

    @Override
    protected void onFilter()
    {
        alternatives = StringUtils.tokenize(getRawValue());
        super.onFilter();
    }

    @Override
    public boolean isMatching(M record)
    {
        return doSelect(record, alternatives, getProperty());
    }

    private boolean doSelect(ModelData record, List<String> filterTextAlternatives, String filteredPropertyKey)
    {
        if (filterTextAlternatives.isEmpty())
        {
            return true;
        }
        for (String filterText : filterTextAlternatives)
        {
            if (doSelect(record, filterText, filteredPropertyKey))
            {
                return true;
            }
        }
        return false;
    }
    
    private boolean doSelect(ModelData record, String filterText, String filteredPropertyKey)
    {
        if (StringUtils.isBlank(filterText))
        {
            return true;
        }
        final String renderedText;
        if (rendererOrNull == null)
        {
            renderedText = ObjectUtils.toString(record.get(filteredPropertyKey)).toLowerCase();
        } else
        {
            renderedText =
                    ((String) rendererOrNull.render(record, filteredPropertyKey, null, 0, 0, null,
                            null)).toLowerCase();
        }
        boolean comparisonValue = true;
        String lowerCaseFilterText = filterText.toLowerCase();
        if (filterText.startsWith(PREFIX_NOT))
        {
            comparisonValue = false;
            lowerCaseFilterText = lowerCaseFilterText.substring(1);
        }
        if (lowerCaseFilterText.startsWith(PREFIX_START))
        {
            if (lowerCaseFilterText.endsWith(SUFFIX_END))
            {
                return (renderedText.equals(lowerCaseFilterText.substring(1, lowerCaseFilterText
                        .length() - 1)) == comparisonValue);
            } else
            {
                return (renderedText.startsWith(lowerCaseFilterText.substring(1)) == comparisonValue);
            }
        } else
        {
            if (lowerCaseFilterText.endsWith(SUFFIX_END))
            {
                return (renderedText.endsWith(lowerCaseFilterText.substring(0, lowerCaseFilterText
                        .length() - 1)) == comparisonValue);
            } else
            {
                return (renderedText.contains(lowerCaseFilterText) == comparisonValue);
            }
        }
    }
}
