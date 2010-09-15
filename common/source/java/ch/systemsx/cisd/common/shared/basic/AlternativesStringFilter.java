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

package ch.systemsx.cisd.common.shared.basic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ch.systemsx.cisd.common.shared.basic.utils.StringUtils;

/**
 * A filter for <code>String</code> values. It supports:
 * <ul>
 * <li>Alternatives (by ' ')</li>
 * <li>Negation (by '!')</li>
 * <li>Binding to start of value (by '^')</li>
 * <li>Binding to end of value (by '$')</li>
 * <li>Quoting by single ("'") and double ('"') quotes</li>
 * <li>Escaping of special characters (by '\')</li>
 * </ul>
 * 
 * @author Bernd Rinn
 */
public class AlternativesStringFilter
{
    private static final String PREFIX_NOT = "!";

    private static final String PREFIX_START_ANCHOR = "^";

    private static final String SUFFIX_END_ANCHOR = "$";

    private static final String ESCAPE = "\\";

    private static final String SUFFIX_ESCAPED_END_ANCHOR = ESCAPE + SUFFIX_END_ANCHOR;

    private List<Matcher> alternatives = new ArrayList<Matcher>();

    /**
     * A role that can check whether a <var>value</var> matches.
     */
    interface Matcher
    {
        boolean matches(String value);
    }

    static abstract class AbstractTextMatcher implements Matcher
    {
        protected final String filterText;

        AbstractTextMatcher(String filterText)
        {
            this.filterText = filterText.toLowerCase().replace(ESCAPE, StringUtils.EMPTY_STRING);
        }

        abstract boolean doMatch(String value);

        public boolean matches(String value)
        {
            return doMatch(value);
        }
    }

    static class ContainsMatcher extends AbstractTextMatcher
    {
        ContainsMatcher(String filterText)
        {
            super(filterText);
        }

        @Override
        protected boolean doMatch(String value)
        {
            return value.contains(filterText);
        }

    }

    static class StartAnchorMatcher extends AbstractTextMatcher
    {
        StartAnchorMatcher(String filterText)
        {
            super(filterText);
        }

        @Override
        protected boolean doMatch(String value)
        {
            return value.startsWith(filterText);
        }

    }

    static class EndAnchorMatcher extends AbstractTextMatcher
    {
        EndAnchorMatcher(String filterText)
        {
            super(filterText);
        }

        @Override
        protected boolean doMatch(String value)
        {
            return value.endsWith(filterText);
        }

    }

    static class EqualsMatcher extends AbstractTextMatcher
    {
        EqualsMatcher(String filterText)
        {
            super(filterText);
        }

        @Override
        protected boolean doMatch(String value)
        {
            return value.equals(filterText);
        }

    }

    interface NumericalComparison
    {
        boolean matches(double value, double filterValue);
    }

    enum ComparisonKind implements NumericalComparison
    {
        LT("<")
        {
            public boolean matches(double value, double filterValue)
            {
                return value < filterValue;
            }
        },
        GT(">")
        {
            public boolean matches(double value, double filterValue)
            {
                return value > filterValue;
            }
        },
        LE("<=")
        {
            public boolean matches(double value, double filterValue)
            {
                return value <= filterValue;
            }
        },
        GE(">=")
        {
            public boolean matches(double value, double filterValue)
            {
                return value >= filterValue;
            }
        },
        EQ("=")
        {
            public boolean matches(double value, double filterValue)
            {
                return value == filterValue;
            }
        };

        private final String operator;

        ComparisonKind(String operator)
        {
            this.operator = operator;
        }

        public String getOperator()
        {
            return operator;
        }

    }

    private static Map<String, ComparisonKind> comparisonKindByOperator =
            new HashMap<String, ComparisonKind>();

    static
    {
        for (ComparisonKind comparisonKind : ComparisonKind.values())
        {
            comparisonKindByOperator.put(comparisonKind.operator, comparisonKind);
        }
    }

    static class NumericMatcher implements Matcher
    {
        protected final double filterValue;

        private NumericalComparison comparison;

        NumericMatcher(NumericalComparison comparison, double filterValue)
        {
            this.filterValue = filterValue;
            this.comparison = comparison;
        }

        private boolean doMatch(double value)
        {
            return comparison.matches(value, filterValue);
        }

        public boolean matches(String value)
        {
            try
            {
                Double d = Double.parseDouble(value);
                return doMatch(d);
            } catch (NumberFormatException ex)
            {
                return false;
            }
        }
    }

    static class NegationMatcher implements Matcher
    {
        private Matcher delegate;

        NegationMatcher(Matcher delegate)
        {
            this.delegate = delegate;
        }

        public boolean matches(String value)
        {
            return delegate.matches(value) == false;
        }
    }

    /**
     * Sets a new filter <var>value</var>.
     */
    public void setFilterValue(String value)
    {
        alternatives.clear();
        for (String s : StringUtils.tokenize(value))
        {
            final boolean negateValue = s.startsWith(PREFIX_NOT);
            if (negateValue)
            {
                s = s.substring(1);
            }
            Matcher matcher = tryGetNumericMatcher(s);
            if (matcher == null)
            {
                matcher = getStringMatcher(negateValue ? s.substring(1) : s);
            }
            if (negateValue)
            {
                matcher = new NegationMatcher(matcher);
            }
            alternatives.add(matcher);
        }
    }

    private Matcher tryGetNumericMatcher(String s)
    {
        if (s.length() < 2)
        {
            return null;
        }
        if ("<>=".indexOf(s.charAt(0)) > -1)
        {
            int operatorLength = (s.charAt(1) == '=') ? 2 : 1;
            String operator = s.substring(0, operatorLength);
            String filterValue = s.substring(operatorLength);
            ComparisonKind comparisonKindOrNull = tryGetComparisonKind(operator);
            if (comparisonKindOrNull == null)
            {
                return null;
            }
            try
            {
                double dobuleValue = Double.parseDouble(filterValue);
                return new NumericMatcher(comparisonKindOrNull, dobuleValue);
            } catch (NumberFormatException e)
            {
                return null;
            }
        }
        return null;
    }

    private ComparisonKind tryGetComparisonKind(String operator)
    {
        return comparisonKindByOperator.get(operator);
    }

    private Matcher getStringMatcher(String s)
    {
        if (isStartAnchored(s))
        {
            if (isEndAnchored(s))
            {
                return new EqualsMatcher(s.substring(1, s.length() - 1));
            } else
            {
                return new StartAnchorMatcher(s.substring(1));
            }
        } else if (isEndAnchored(s))
        {
            return new EndAnchorMatcher(s.substring(0, s.length() - 1));
        } else
        {
            return new ContainsMatcher(s);
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
     * Returns <code>true</code>, if the given <var>value</var> passes this filter.
     */
    public boolean passes(String value)
    {
        if (alternatives.isEmpty())
        {
            return true;
        }
        for (final Matcher matcher : alternatives)
        {
            if (matcher.matches(value))
            {
                return true;
            }
        }
        return false;
    }

}
