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

package ch.systemsx.cisd.openbis.generic.shared.translator;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringEscapeUtils;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Filter;
import ch.systemsx.cisd.openbis.generic.shared.dto.FilterPE;
import ch.systemsx.cisd.openbis.generic.shared.util.HibernateUtils;

/**
 * A {@link Filter} &lt;---&gt; {@link FilterPE} translator.
 * 
 * @author Izabela Adamczyk
 */
public final class FilterTranslator
{
    private static final String PARAMETER_PATTERN = "\\$\\{.*?\\}";

    private FilterTranslator()
    {
        // Can not be instantiated.
    }

    public final static List<Filter> translate(final List<FilterPE> filters)
    {
        final List<Filter> result = new ArrayList<Filter>();
        for (final FilterPE filter : filters)
        {
            result.add(FilterTranslator.translate(filter));
        }
        return result;
    }

    public final static Filter translate(final FilterPE filter)
    {
        if (filter == null)
        {
            return null;
        }
        final Filter result = new Filter();
        result.setId(HibernateUtils.getId(filter));
        result.setModificationDate(filter.getModificationDate());
        result.setExpression(StringEscapeUtils.escapeHtml(filter.getExpression()));
        result.setName(StringEscapeUtils.escapeHtml(filter.getName()));
        result.setDescription(StringEscapeUtils.escapeHtml(filter.getDescription()));
        result.setRegistrator(PersonTranslator.translate(filter.getRegistrator()));
        result.setRegistrationDate(filter.getRegistrationDate());
        result.setDatabaseInstance(DatabaseInstanceTranslator.translate(filter
                .getDatabaseInstance()));
        result.setPublic(filter.isPublic());
        result.setParameters(extractParameters(filter.getExpression()));
        return result;
    }

    static Set<String> extractParameters(String expression)
    {
        Pattern parameterPattern = Pattern.compile(PARAMETER_PATTERN);
        Set<String> list = new HashSet<String>();
        Matcher matcher = parameterPattern.matcher(expression);
        while (matcher.find())
        {
            String group = matcher.group();
            list.add(group.substring(2, group.length() - 1));
        }
        return list;
    }

}
