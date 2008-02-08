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

package ch.systemsx.cisd.cifex.client.application;

import com.google.gwt.i18n.client.DateTimeFormat;

/**
 * Some constants used through the whole web application.
 * 
 * @author Christian Ribeaud
 */
public final class Constants
{
    private Constants()
    {
        // Can not be instantiated.
    }

    /** Default date/time format. */
    public static final String DEFAULT_DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss zzz";

    /** Default <code>DateTimeFormat</code> used here. */
    public static final DateTimeFormat defaultDateTimeFormat = DateTimeFormat.getFormat(DEFAULT_DATE_TIME_FORMAT);

    /** Name of the <code>RemoteServiceServlet</code> extension (The <i>GWT</i> server side). */
    public static final String CIFEX_SERVLET_NAME = "/cifex/cifex";

    /** The HTTP URL parameter used to specify the file id. */
    public static final String FILE_ID_PARAMETER = "fileId";

    /** The HTTP URL parameter used to specify the email. */
    public static final String USERCODE_PARAMETER = "user";

    /** Name of the <code>HttpServlet</code> extension to upload a file. */
    public static final String FILE_UPLOAD_SERVLET_NAME = getPrepend() + "file-upload";

    /** Name of the <code>HttpServlet</code> extension to download a file. */
    public static final String FILE_DOWNLOAD_SERVLET_NAME = getPrepend() + "file-download";

    /** The table <code>null</code> value representation. */
    public static final String TABLE_NULL_VALUE = "-";

    /** The table <i>empty</i> value representation. */
    public static final String TABLE_EMPTY_VALUE = "";

    private final static String getPrepend()
    {
        return "/cifex/";
    }
}
