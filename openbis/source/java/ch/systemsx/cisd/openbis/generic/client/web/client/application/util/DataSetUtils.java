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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.util;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.GenericViewModel;
import ch.systemsx.cisd.openbis.generic.shared.basic.URLMethodWithParameters;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExternalData;

/**
 * @author Franz-Josef Elmer
 */
public class DataSetUtils
{
    private static final String MAIN_DATA_SET_PATH = "mdsPath";

    private static final String MAIN_DATA_SET_PATTERN = "mdsPattern";

    private static final String AUTO_RESOLVE = "autoResolve";

    private static final String SESSION_ID = "sessionID";

    private static final String MODE = "mode";

    public static void showDataSet(ExternalData dataSet, GenericViewModel model)
    {
        String url = createDataViewUrl(dataSet, model, null, false);
        WindowUtils.openWindow(url);
    }

    public static String createDataViewUrl(ExternalData dataSet, GenericViewModel model,
            String modeOrNull, boolean autoResolve)
    {
        URLMethodWithParameters methodWithParameters =
                new URLMethodWithParameters(dataSet.getDataStore().getDownloadUrl() + "/"
                        + dataSet.getCode());
        methodWithParameters.addParameter(SESSION_ID, model.getSessionContext().getSessionID());
        if (modeOrNull != null)
        {
            methodWithParameters.addParameter(MODE, modeOrNull);
        }
        if (autoResolve)
        {
            methodWithParameters.addParameter(AUTO_RESOLVE, autoResolve);
            if (StringUtils.isBlank(dataSet.getDataSetType().getMainDataSetPattern()) == false)
            {
                final String regexpPattern =
                        translateToRegexp(dataSet.getDataSetType().getMainDataSetPattern());
                methodWithParameters.addParameter(MAIN_DATA_SET_PATTERN, regexpPattern);
            }
            if (StringUtils.isBlank(dataSet.getDataSetType().getMainDataSetPath()) == false)
            {
                methodWithParameters.addParameter(MAIN_DATA_SET_PATH, dataSet.getDataSetType()
                        .getMainDataSetPath());
            }
        }
        String url = methodWithParameters.toString();
        return url;
    }

    public static final String REGEXP_PREFIX = "regexp:";

    // all regexp metacharacters except '*', '?', '[' and ']' that need to be escaped in translation
    private static final char[] REGEXP_METACHARACTERS_TO_ESCAPE =
        { '\\', '|', '(', ')', '{', '}', '^', '$', '+', '.', '<', '>' };

    /**
     * @return Given wildcard pattern e.g. '*.tsv' translated to regexp pattern. No translation is
     *         done if given pattern starts with {@link #REGEXP_PREFIX}.
     */
    public static String translateToRegexp(final String wildcardPattern)
    {
        assert wildcardPattern != null;

        String result = wildcardPattern;
        if (false == wildcardPattern.startsWith(REGEXP_PREFIX))
        {
            // escape meta characters and replace the wildcard meta-characters:
            // - "*" with ".*"
            // - "?" with "."
            result = StringUtils.escape(result, REGEXP_METACHARACTERS_TO_ESCAPE);
            result = result.replace("*", ".*");
            result = result.replace("?", ".");
        } else
        {
            result = result.substring(REGEXP_PREFIX.length());
        }
        return result;
    }

}
