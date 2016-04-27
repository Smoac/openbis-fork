/*
 * Copyright 2011 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.dss.generic.server.ftp;

import java.util.Properties;

/**
 * A builder for {@link FtpServerConfig} objects.
 * 
 * @author Kaloyan Enimanev
 */
public class FtpServerConfigBuilder
{
    private Properties props = new Properties();

    public FtpServerConfig getConfig()
    {
        return new FtpServerConfig(props);
    }

    public FtpServerConfigBuilder()
    {
        this(true, false);
    }

    public FtpServerConfigBuilder(boolean enable, boolean useSSL)
    {
        props.put(FtpServerConfig.ENABLE_KEY, String.valueOf(enable));
        props.put(FtpServerConfig.USE_SSL_KEY, String.valueOf(useSSL));
    }

    public FtpServerConfigBuilder showParentsAndChildren()
    {
        props.setProperty(FtpServerConfig.SHOW_PARENTS_AND_CHILDREN_KEY, Boolean.TRUE.toString());
        return this;

    }

    public FtpServerConfigBuilder withTemplate(String template)
    {
        props.setProperty(FtpServerConfig.DATASET_DISPLAY_TEMPLATE_KEY, template);
        return this;

    }

    public FtpServerConfigBuilder withFileListFilter(String dataSetType, String filterPattern)
    {
        String key = FtpServerConfig.DATASET_FILELIST_FILTER_KEY + dataSetType;
        props.setProperty(key, filterPattern);
        return this;
    }

    public FtpServerConfigBuilder withFileListSubPath(String dataSetType, String subPathPattern)
    {
        String key = FtpServerConfig.DATASET_FILELIST_SUBPATH_KEY + dataSetType;
        props.setProperty(key, subPathPattern);
        return this;
    }
}
