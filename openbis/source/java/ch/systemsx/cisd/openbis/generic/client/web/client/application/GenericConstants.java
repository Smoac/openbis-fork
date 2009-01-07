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

package ch.systemsx.cisd.openbis.generic.client.web.client.application;

import com.google.gwt.core.client.GWT;

/**
 * Some generic constants.
 * 
 * @author Franz-Josef Elmer
 */
public final class GenericConstants
{
    public static final String MANDATORY_LABEL_SEPARATOR = ": *";

    public static final String POPUP_BLOCKER_DETECTED =
            "A pop-up blocker has been detected. Please disable all the pop-up blockers for this site.";

    private GenericConstants()
    {
        // Can not be instantiated.
    }

    /**
     * Prefix all widget IDs have to start with.
     */
    public static final String ID_PREFIX = "openbis_";

    private static final String APPLICATION_NAME = "genericopenbis";

    public static final String COMMON_SERVER_NAME = createServicePath("common");

    /**
     * Creates for the specified service name the service path.
     */
    public final static String createServicePath(final String serviceName)
    {
        // Kind of hack. Unclear why an additional APPLICATION_NAME in productive mode is needed.
        return "/" + APPLICATION_NAME + "/" + (GWT.isScript() ? APPLICATION_NAME + "/" : "")
                + serviceName;
    }

    /** Name of the servlet to download an experiment attachment. */
    public static final String EXPERIMENT_ATTACHMENT_DOWNLOAD_SERVLET_NAME =
            createServicePath("experiment-attachment-download");

    /** The HTTP URL parameter used to specify the version. */
    public static final String VERSION_PARAMETER = "version";

    /** The HTTP URL parameter used to specify the file name. */
    public static final String FILE_NAME_PARAMETER = "fileName";

    /** The HTTP URL parameter used to specify the project. */
    public static final String PROJECT_PARAMETER = "project";

    /** The HTTP URL parameter used to specify the experiment. */
    public static final String EXPERIMENT_PARAMETER = "experiment";

    /** The HTTP URL parameter used to specify the group. */
    public static final String GROUP_PARAMETER = "group";

    /** The HTTP URL parameter used to specify the database. */
    public static final String DATABASE_PARAMETER = "database";

    /** Name of the servlet to export and download a file. */
    public static final String FILE_EXPORTER_DOWNLOAD_SERVLET_NAME =
            createServicePath("export-file-downloader");

    /** The HTTP URL parameter used to specify the export criteria. */
    public static final String EXPORT_CRITERIA_KEY_PARAMETER = "exportDataKey";

    public static final String LABEL_SEPARATOR = ":";

}
