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

package ch.systemsx.cisd.cifex.client.application;

import com.google.gwt.core.client.GWT;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public class ServletPathConstants
{
    private static final String CONTEXT_PATH = isDeployed() ? "" : "/cifex";
    
    /**
     * Name of the <code>RemoteServiceServlet</code> extension (The <i>GWT</i> server side).
     */
    // Do not use 'getPrepend()' here as this URL does not change, nor in Web/Hosted Mode neither in
    // Deployed Mode.
    public static final String CIFEX_SERVLET_NAME = CONTEXT_PATH + "/cifex";

    /** Name of the <code>HttpServlet</code> extension to upload a file. */
    public static final String FILE_UPLOAD_SERVLET_NAME = getPrepend() + "file-upload";

    /** Name of the <code>HttpServlet</code> extension to upload files > 2GB. */
    public static final String FILE2GB_UPLOAD_SERVLET_NAME = getPrepend() + "file2GB-upload";
    
    /** Name of the <code>HttpServlet</code> extension to download a file. */
    public static final String FILE_DOWNLOAD_SERVLET_NAME = getPrepend() + "file-download";

    private final static String getPrepend()
    {
        return CONTEXT_PATH + "/" + (isDeployed() ? "cifex/" : "");
    }

    /**
     * Whether this application is deployed.
     * <p>
     * Deployed means that module name (<code>ch.systemsx.cisd.cifex.Cifex</code>) not present
     * in module base URL (<code>http://localhost:8080/cifex/</code>).
     * </p>
     */
    private final static boolean isDeployed()
    {
        return GWT.getModuleBaseURL().indexOf(GWT.getModuleName()) < 0;
    }

}
