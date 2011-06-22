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

package ch.systemsx.cisd.openbis.dss.client.api.v1;

import java.io.File;
import java.io.InputStream;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;

import ch.systemsx.cisd.common.exceptions.InvalidSessionException;
import ch.systemsx.cisd.openbis.dss.client.api.v1.impl.OpenbisServiceFacade;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.FileInfoDssDTO;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataSet.Connections;

/**
 * A class that provides uniform access to data set metadata (from the openBIS AS) and data (from
 * the openBIS DSS).
 * 
 * @author Chandrasekhar Ramakrishnan
 */
public class DataSet
{
    private final OpenbisServiceFacade facade;

    private ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataSet metadata;

    private IDataSetDss dataSetDss;

    /**
     * Constructor.
     * 
     * @param facade The facade used to get access to the server.
     * @param metadata The metadata. May be null if not available at construction time.
     * @param dataSetDss The data. May be null if not available at construction time.
     */
    public DataSet(OpenbisServiceFacade facade,
            ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataSet metadata,
            IDataSetDss dataSetDss)
    {
        this.facade = facade;
        this.metadata = metadata;
        this.dataSetDss = dataSetDss;

        // One of the ivars must be non-null;
        assert metadata != null || dataSetDss != null;
    }

    /**
     * @see ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataSet#getCode()
     */
    public String getCode()
    {
        return (metadata == null) ? dataSetDss.getCode() : metadata.getCode();
    }

    /**
     * @see ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataSet#getExperimentIdentifier()
     */
    public String getExperimentIdentifier()
    {
        return getMetadata().getExperimentIdentifier();
    }

    /**
     * @see ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataSet#getSampleIdentifierOrNull()
     */
    public String getSampleIdentifierOrNull()
    {
        return getMetadata().getSampleIdentifierOrNull();
    }

    /**
     * @see ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataSet#getDataSetTypeCode()
     */
    public String getDataSetTypeCode()
    {
        return getMetadata().getDataSetTypeCode();
    }

    /**
     * @see ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataSet#getRegistrationDate()
     */
    public Date getRegistrationDate()
    {
        return getMetadata().getRegistrationDate();
    }

    /**
     * @see ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataSet#getProperties()
     */
    public HashMap<String, String> getProperties()
    {
        return getMetadata().getProperties();
    }

    /**
     * @see ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataSet#getRetrievedConnections()
     */
    public EnumSet<Connections> getRetrievedConnections()
    {
        return getMetadata().getRetrievedConnections();
    }

    /**
     * @see ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataSet#getParentCodes()
     */
    public List<String> getParentCodes()
    {
        return getMetadata().getParentCodes();
    }

    /**
     * @see ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataSet#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj)
    {
        return getMetadata().equals(obj);
    }

    /**
     * @see ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataSet#hashCode()
     */
    @Override
    public int hashCode()
    {
        return getMetadata().hashCode();
    }

    /**
     * @see ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataSet#toString()
     */
    @Override
    public String toString()
    {
        return getMetadata().toString();
    }

    /**
     * @param startPath
     * @param isRecursive
     * @throws IllegalArgumentException
     * @throws InvalidSessionException
     * @see ch.systemsx.cisd.openbis.dss.client.api.v1.IDataSetDss#listFiles(java.lang.String,
     *      boolean)
     */
    public FileInfoDssDTO[] listFiles(String startPath, boolean isRecursive)
            throws IllegalArgumentException, InvalidSessionException
    {
        return getDataSetDss().listFiles(startPath, isRecursive);
    }

    /**
     * @param path
     * @throws IllegalArgumentException
     * @throws InvalidSessionException
     * @see ch.systemsx.cisd.openbis.dss.client.api.v1.IDataSetDss#getFile(java.lang.String)
     */
    public InputStream getFile(String path) throws IllegalArgumentException,
            InvalidSessionException
    {
        return getDataSetDss().getFile(path);
    }

    /**
     * @param overrideStoreRootPathOrNull
     * @throws IllegalArgumentException
     * @throws InvalidSessionException
     * @see ch.systemsx.cisd.openbis.dss.client.api.v1.IDataSetDss#tryLinkToContents(java.lang.String)
     */
    public File tryLinkToContents(String overrideStoreRootPathOrNull)
            throws IllegalArgumentException, InvalidSessionException
    {
        return getDataSetDss().tryLinkToContents(overrideStoreRootPathOrNull);
    }

    /**
     * @param overrideStoreRootPathOrNull
     * @param downloadDir
     * @throws IllegalArgumentException
     * @throws InvalidSessionException
     * @see ch.systemsx.cisd.openbis.dss.client.api.v1.IDataSetDss#getLinkOrCopyOfContents(java.lang.String,
     *      java.io.File)
     */
    public File getLinkOrCopyOfContents(String overrideStoreRootPathOrNull, File downloadDir)
            throws IllegalArgumentException, InvalidSessionException
    {
        return getDataSetDss().getLinkOrCopyOfContents(overrideStoreRootPathOrNull, downloadDir);
    }

    /**
     * @param overrideStoreRootPathOrNull
     * @param downloadDir
     * @param pathInDataSet
     * @throws IllegalArgumentException
     * @throws InvalidSessionException
     * @see ch.systemsx.cisd.openbis.dss.client.api.v1.IDataSetDss#getLinkOrCopyOfContent(java.lang.String,
     *      java.io.File, java.lang.String)
     */
    public File getLinkOrCopyOfContent(String overrideStoreRootPathOrNull, File downloadDir,
            String pathInDataSet) throws IllegalArgumentException, InvalidSessionException
    {
        return getDataSetDss().getLinkOrCopyOfContent(overrideStoreRootPathOrNull, downloadDir,
                pathInDataSet);
    }

    /**
     * Internal accessor, made public for testing, but clients should not need it.
     */
    public IDataSetDss getDataSetDss()
    {
        // lazily initialize the ivar.
        if (null == dataSetDss)
        {
            dataSetDss = facade.getDataSetDss(getMetadata().getCode());
        }
        return dataSetDss;
    }

    /**
     * Internal accessor
     */
    private ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataSet getMetadata()
    {
        // lazily initialize the ivar.
        if (null == metadata)
        {
            metadata = facade.tryRawDataSet(dataSetDss.getCode());
        }
        return metadata;
    }
}
