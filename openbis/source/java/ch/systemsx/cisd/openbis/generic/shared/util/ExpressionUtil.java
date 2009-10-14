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

package ch.systemsx.cisd.openbis.generic.shared.util;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Useful static methods for expressions.
 *
 * @author Franz-Josef Elmer
 */
public class ExpressionUtil
{
    public static final String PARAMETER_PATTERN = "\\$\\{.*?\\}";
    private static final Pattern PATTERN = Pattern.compile(ExpressionUtil.PARAMETER_PATTERN);

    /**
     * Extracts all parameters in the specified expression
     */
    public static Set<String> extractParameters(String expression)
    {
        Set<String> list = new LinkedHashSet<String>();
        Matcher matcher = PATTERN.matcher(expression);
        while (matcher.find())
        {
            String group = matcher.group();
            list.add(group.substring(2, group.length() - 1));
        }
        return list;
    }
    
    private ExpressionUtil()
    {
    }

}
