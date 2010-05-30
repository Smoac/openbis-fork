/*
 * Copyright 2010 ETH Zuerich, CISD
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

import java.util.ArrayList;
import java.util.List;

import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.widget.grid.GridCellRenderer;

import ch.systemsx.cisd.cifex.client.application.ui.IGridCellRendererNonPlainText;
import ch.systemsx.cisd.cifex.client.application.utils.ObjectUtils;
import ch.systemsx.cisd.cifex.client.application.utils.StringUtils;

/**
 * A filter for {@link ModelData} records. It supports: <li>Alternatives (by ' ') <li>Negation (by
 * '!') <li>Binding to start of value (by '^') <li>Binding to end of value (by '$') <li>Quoting by
 * single ("'") and double ('"') quotes <li>Escaping of special characters (by '\')
 * 
 * @author Bernd Rinn
 */
public class ColumnFilter
{
    private static final String PREFIX_NOT = "!";

    private static final String PREFIX_START_ANCHOR = "^";

    private static final String SUFFIX_END_ANCHOR = "$";

    private final static String ESCAPE = "\\";

    private static final String SUFFIX_ESCAPED_END_ANCHOR = ESCAPE + SUFFIX_END_ANCHOR;

    private final GridCellRenderer<ModelData> rendererOrNull;

    private final String filterPropertyKey;

    private List<Matcher> alternatives = new ArrayList<Matcher>();

    /**
     * A role that can check whether a <var>value</var> matches.
     */
    interface Matcher
    {
        boolean matches(String value);
    }

    static abstract class AbstractMatcher implements Matcher
    {
        protected final String filterText;

        protected final boolean comparisonValue;

        AbstractMatcher(String filterText, boolean comparisonValue)
        {
            this.filterText = filterText.toLowerCase().replace(ESCAPE, StringUtils.EMPTY_STRING);
            this.comparisonValue = comparisonValue;
        }

        abstract boolean doMatch(String value);

        public boolean matches(String value)
        {
            return doMatch(value) == comparisonValue;
        }
    }

    static class ContainsMatcher extends AbstractMatcher
    {
        ContainsMatcher(String filterText, boolean comparisonValue)
        {
            super(filterText, comparisonValue);
        }

        @Override
        protected boolean doMatch(String value)
        {
            return value.contains(filterText);
        }

    }

    static class StartAnchorMatcher extends AbstractMatcher
    {
        StartAnchorMatcher(String filterText, boolean comparisonValue)
        {
            super(filterText, comparisonValue);
        }

        @Override
        protected boolean doMatch(String value)
        {
            return value.startsWith(filterText);
        }

    }

    static class EndAnchorMatcher extends AbstractMatcher
    {
        EndAnchorMatcher(String filterText, boolean comparisonValue)
        {
            super(filterText, comparisonValue);
        }

        @Override
        protected boolean doMatch(String value)
        {
            return value.endsWith(filterText);
        }

    }

    static class EqualsMatcher extends AbstractMatcher
    {
        EqualsMatcher(String filterText, boolean comparisonValue)
        {
            super(filterText, comparisonValue);
        }

        @Override
        protected boolean doMatch(String value)
        {
            return value.equals(filterText);
        }

    }

    ColumnFilter(GridCellRenderer<ModelData> rendererOrNull, String filterPropertyKey)
    {
        if (rendererOrNull instanceof IGridCellRendererNonPlainText<?>)
        {
            this.rendererOrNull =
                    ((IGridCellRendererNonPlainText<ModelData>) rendererOrNull)
                            .getPlainTextRenderer();
        } else
        {
            this.rendererOrNull = rendererOrNull;
        }
        this.filterPropertyKey = filterPropertyKey;
    }

    /**
     * Sets a new filter <var>value</var>.
     */
    public void setFilterValue(String value)
    {
        alternatives.clear();
        for (String s : StringUtils.tokenize(value))
        {
            final boolean comparisonValue = (s.startsWith(PREFIX_NOT) == false);
            if (comparisonValue == false)
            {
                s = s.substring(1);
            }
            if (isStartAnchored(s))
            {
                if (isEndAnchored(s))
                {
                    alternatives.add(new EqualsMatcher(s.substring(1, s.length() - 1),
                            comparisonValue));
                } else
                {
                    alternatives.add(new StartAnchorMatcher(s.substring(1),
                            comparisonValue));
                }
            } else if (isEndAnchored(s))
            {
                alternatives.add(new EndAnchorMatcher(s.substring(0, s.length() - 1),
                        comparisonValue));
            } else
            {
                alternatives.add(new ContainsMatcher(s, comparisonValue));
            }
        }
    }

    private boolean isStartAnchored(String s)
    {
        return s.startsWith(PREFIX_START_ANCHOR);
    }

    private boolean isEndAnchored(String s)
    {
        return s.endsWith(SUFFIX_END_ANCHOR) && s.endsWith(SUFFIX_ESCAPED_END_ANCHOR) == false;
    }

    /**
     * Returns <code>true</code>, if the given <var>filteredPropertyKey</var> of <var>record</var>
     * passes this filter.
     */
    public boolean passes(ModelData record)
    {
        if (alternatives.isEmpty())
        {
            return true;
        }
        final String value = getValue(record);
        for (final Matcher matcher : alternatives)
        {
            if (matcher.matches(value))
            {
                return true;
            }
        }
        return false;
    }

    private String getValue(ModelData record)
    {
        final String renderedText;
        if (rendererOrNull == null)
        {
            renderedText = ObjectUtils.toString(record.get(filterPropertyKey)).toLowerCase();
        } else
        {
            renderedText =
                    ((String) rendererOrNull.render(record, filterPropertyKey, null, 0, 0, null,
                            null)).toLowerCase();
        }
        return renderedText;
    }
}
