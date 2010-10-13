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

package ch.systemsx.cisd.openbis.generic.server.dataaccess.db.search;

import java.io.File;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.InitializingBean;

import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.logging.Log4jSimpleLogger;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.utilities.ModifiedShortPrefixToStringStyle;

/**
 * A bean class that contains properties related to <i>Hibernate Search</i>.
 * <p>
 * This bean must be initialized before <code>hibernate-session-factory</code> bean as it will
 * remove the <code>indexBase</code> directory.
 * </p>
 * 
 * @author Christian Ribeaud
 */
public final class HibernateSearchContext implements InitializingBean
{
    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
            HibernateSearchContext.class);

    private IndexMode indexMode = IndexMode.SKIP_IF_MARKER_FOUND;

    private String indexBase = System.getProperty("java.io.tmpdir");

    /**
     * 0 means no limit.
     */
    private int batchSize = 1000;

    private int maxResults = 100000;

    public int getMaxResults()
    {
        return maxResults;
    }

    public void setMaxResults(int maxResults)
    {
        if (maxResults > 0)
        {
            this.maxResults = maxResults;
        }
    }

    public final String getIndexBase()
    {
        return indexBase;
    }

    public final void setIndexBase(final String indexBase)
    {
        if (StringUtils.isNotBlank(indexBase))
        {
            this.indexBase = indexBase;
        }
    }

    public final int getBatchSize()
    {
        return batchSize;
    }

    public final void setBatchSize(final int batchSize)
    {
        this.batchSize = Math.max(0, batchSize);
    }

    public final IndexMode getIndexMode()
    {
        return indexMode;
    }

    public final void setIndexMode(final IndexMode indexMode)
    {
        assert indexMode != null : "Unspecified index mode.";
        this.indexMode = indexMode;
    }

    //
    // Object
    //

    @Override
    public final String toString()
    {
        return ToStringBuilder.reflectionToString(this,
                ModifiedShortPrefixToStringStyle.MODIFIED_SHORT_PREFIX_STYLE);
    }

    //
    // InitializingBean
    //

    public final void afterPropertiesSet() throws Exception
    {
        if (getIndexMode() == IndexMode.INDEX_FROM_SCRATCH)
        {
            final File searchIndexBase = new File(getIndexBase());
            if (searchIndexBase.exists())
            {
                final boolean deleted =
                        FileUtilities.deleteRecursively(searchIndexBase, new Log4jSimpleLogger(
                                operationLog, Level.DEBUG));
                operationLog.info(String.format("Index base '%s' %s.", searchIndexBase
                        .getAbsolutePath(), deleted ? "has been successfully deleted"
                        : "has NOT been deleted"));
            }
        }
    }
}
