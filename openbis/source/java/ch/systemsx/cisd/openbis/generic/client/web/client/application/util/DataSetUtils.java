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
                methodWithParameters.addParameter(MAIN_DATA_SET_PATTERN, dataSet.getDataSetType()
                        .getMainDataSetPattern());
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
}
