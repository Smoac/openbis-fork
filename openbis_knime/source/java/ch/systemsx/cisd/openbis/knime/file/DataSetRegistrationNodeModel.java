/*
 * Copyright 2013 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.knime.file;

import java.io.File;
import java.util.List;

import org.knime.core.data.uri.URIContent;
import org.knime.core.data.uri.URIPortObject;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortType;

import ch.systemsx.cisd.openbis.dss.client.api.v1.IOpenbisServiceFacade;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.NewDataSetDTO.DataSetOwnerType;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.NewDataSetDTOBuilder;
import ch.systemsx.cisd.openbis.knime.common.AbstractOpenBisNodeModel;
import ch.systemsx.cisd.openbis.knime.common.Util;

/**
 * {@link NodeModel} for registration of a file as a new data set.
 *
 * @author Franz-Josef Elmer
 */
public class DataSetRegistrationNodeModel extends AbstractOpenBisNodeModel
{
    static final String DATA_SET_TYPE_KEY = "data-set-type";
    static final String OWNER_TYPE_KEY = "owner-type";
    static final String OWNER_KEY = "owner";
    
    private final IOpenbisServiceFacadeFactory serviceFacadeFactory;
    private String dataSetType;
    private DataSetOwnerType ownerType;
    private String owner;

    public DataSetRegistrationNodeModel(IOpenbisServiceFacadeFactory serviceFacadeFactory)
    {
        super(new PortType[] {new PortType(URIPortObject.class) },  new PortType[] {});
        this.serviceFacadeFactory = serviceFacadeFactory;
    }
    
    @Override
    protected PortObjectSpec[] configure(PortObjectSpec[] inSpecs) throws InvalidSettingsException
    {
        return new PortObjectSpec[] {};
    }

    @Override
    protected void loadAdditionalValidatedSettingsFrom(NodeSettingsRO settings)
            throws InvalidSettingsException
    {
        dataSetType = settings.getString(DATA_SET_TYPE_KEY);
        ownerType = DataSetOwnerType.valueOf(settings.getString(OWNER_TYPE_KEY));
        owner = settings.getString(OWNER_KEY);
    }

    @Override
    protected void saveAdditionalSettingsTo(NodeSettingsWO settings)
    {
        settings.addString(DATA_SET_TYPE_KEY, dataSetType);
        settings.addString(OWNER_TYPE_KEY, (ownerType == null ? DataSetOwnerType.EXPERIMENT
                : ownerType).name());
        settings.addString(OWNER_KEY, owner);
    }

    @Override
    protected PortObject[] execute(PortObject[] inObjects, ExecutionContext exec) throws Exception
    {
        File file = getFirstFile(inObjects);
        logger.info("data set file: " + file);
        NewDataSetDTOBuilder builder = new NewDataSetDTOBuilder();
        builder.setDataSetOwnerType(ownerType);
        if (owner != null && owner.trim().length() > 0)
        {
            builder.setDataSetOwnerIdentifier(owner.trim());
        } else
        {
            builder.setDataSetOwnerIdentifier(peekFlowVariableString(Util.VARIABLE_PREFIX + ownerType.name()));
        }
        builder.setFile(file);
        builder.getDataSetMetadata().setDataSetTypeOrNull(dataSetType);
        IOpenbisServiceFacade facade = serviceFacadeFactory.createFacade(url, userID, password);
        facade.putDataSet(builder.asNewDataSetDTO(), file);
        return new PortObject[] {};
    }
    
    private File getFirstFile(PortObject[] inObjects)
    {
        if (inObjects.length != 1)
        {
            throw new IllegalArgumentException("Expecting exactly one port instead of " + inObjects.length + ".");
        }
        PortObject portObject = inObjects[0];
        if (portObject instanceof URIPortObject == false)
        {
            throw new IllegalArgumentException("Expecting an URI port instead of " + portObject.getClass().getName() + ".");
        }
        List<URIContent> uriContents = ((URIPortObject) portObject).getURIContents();
        if (uriContents.isEmpty())
        {
            throw new IllegalArgumentException("Expecting at least on URI in input port.");
        }
        if (uriContents.size() > 1)
        {
            logger.warn(uriContents.size() + " URIs instead of only one: " + uriContents);
        }
        return new File(uriContents.get(0).getURI());
    }

}
