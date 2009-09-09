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

package ch.systemsx.cisd.openbis.generic.shared.dto;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import ch.systemsx.cisd.common.annotation.CollectionMapping;
import ch.systemsx.cisd.openbis.generic.shared.IServer;

/**
 * A basic {@link NewExternalData} object.
 * <p>
 * {@link NewExternalData} extends this class and completes it with additional fields.
 * </p>
 * 
 * @author Christian Ribeaud
 */
public class ExtractableData extends Code<ExtractableData>
{
    private static final long serialVersionUID = IServer.VERSION;

    private Date productionDate;

    private String dataProducerCode;
    
    private List<String> parentDataSetCodes = new ArrayList<String>();

    private List<NewProperty> dataSetProperties = new ArrayList<NewProperty>();

    /**
     * Returns the date when the measurement / calculation that produced this external data set has
     * been performed.
     * <p>
     * This may not be known in which case this method will return <code>null</code>.
     */
    public Date getProductionDate()
    {
        return productionDate;
    }

    /**
     * Sets the date when the measurement / calculation that produced this external data set has
     * been performed.
     */
    public void setProductionDate(final Date productionDate)
    {
        this.productionDate = productionDate;
    }

    /**
     * Returns the code identifying the data source (i.e. measurement device or software pipeline)
     * that produced this external data set.
     * <p>
     * This may not be known in which case this method will return <code>null</code>.
     */
    public String getDataProducerCode()
    {
        return dataProducerCode;
    }

    /**
     * Sets the code identifying the data source (i.e. measurement device or software pipeline) that
     * produced this external data set.
     */
    public void setDataProducerCode(final String dataProducerCode)
    {
        this.dataProducerCode = dataProducerCode;
    }

    @Deprecated
    public final String tryToGetParentDataSetCode()
    {
        return parentDataSetCodes.isEmpty() ? null : parentDataSetCodes.iterator().next();
    }
    
    public final List<String> getParentDataSetCodes()
    {
        return parentDataSetCodes;
    }

    @CollectionMapping(collectionClass = ArrayList.class, elementClass = String.class)
    public final void setParentDataSetCodes(List<String> parentDataSetCodes)
    {
        this.parentDataSetCodes = parentDataSetCodes;
    }

    @CollectionMapping(collectionClass = ArrayList.class, elementClass = NewProperty.class)
    public void setDataSetProperties(List<NewProperty> dataSetProperties)
    {
        this.dataSetProperties = dataSetProperties;
    }

    public List<NewProperty> getDataSetProperties()
    {
        return dataSetProperties;
    }
}
