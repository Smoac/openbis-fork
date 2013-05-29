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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import org.apache.commons.lang.StringUtils;
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
import org.knime.core.node.port.flowvariable.FlowVariablePortObject;
import org.knime.core.node.workflow.FlowVariable;

import ch.systemsx.cisd.openbis.dss.client.api.v1.DataSet;
import ch.systemsx.cisd.openbis.dss.client.api.v1.IOpenbisServiceFacade;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.NewDataSetDTO;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.NewDataSetDTO.DataSetOwnerType;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.NewDataSetDTOBuilder;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.NewDataSetMetadataDTO;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataSetType;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Sample;
import ch.systemsx.cisd.openbis.knime.common.AbstractOpenBisNodeModel;
import ch.systemsx.cisd.openbis.knime.common.IOpenbisServiceFacadeFactory;
import ch.systemsx.cisd.openbis.knime.common.Util;

/**
 * {@link NodeModel} for registration of a file as a new data set. The file path is expected as an {@link URIPortObject}.
 *
 * @author Franz-Josef Elmer
 */
public class DataSetRegistrationNodeModel extends AbstractOpenBisNodeModel
{
    static final String DATA_SET_TYPE_KEY = "data-set-type";
    static final String OWNER_TYPE_KEY = "owner-type";
    static final String OWNER_KEY = "owner";
    static final String FILE_VARIABLE_KEY = "file-variable";
    static final String PROPERTY_TYPE_CODES_KEY = "property-type-codes";
    static final String PROPERTY_VALUES_KEY = "property-values";
    
    private final IOpenbisServiceFacadeFactory serviceFacadeFactory;
    private DataSetOwnerType ownerType;
    private String owner;
    private String fileVariable;
    private Map<String, String> properties;
    private DataSetType dataSetType;

    public DataSetRegistrationNodeModel(IOpenbisServiceFacadeFactory serviceFacadeFactory)
    {
        this(URIPortObject.class, serviceFacadeFactory);
    }
    
    protected DataSetRegistrationNodeModel(Class<? extends PortObject> portObjectClass, 
            IOpenbisServiceFacadeFactory serviceFacadeFactory)
    {
        super(new PortType[] {new PortType(portObjectClass) },  new PortType[] {});
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
        byte[] bytes = settings.getByteArray(DATA_SET_TYPE_KEY, null);
        dataSetType = (DataSetType) Util.deserializeDescription(bytes);
        ownerType = DataSetOwnerType.valueOf(settings.getString(OWNER_TYPE_KEY));
        owner = settings.getString(OWNER_KEY);
        fileVariable = settings.getString(FILE_VARIABLE_KEY, null);
        properties = getProperties(settings);
    }

    static Map<String, String> getProperties(NodeSettingsRO settings)
            throws InvalidSettingsException
    {
        String[] propertyTypeCodes = settings.getStringArray(PROPERTY_TYPE_CODES_KEY, new String[0]);
        String[] propertyValues = settings.getStringArray(PROPERTY_VALUES_KEY, new String[0]);
        if (propertyTypeCodes.length != propertyValues.length)
        {
            throw new InvalidSettingsException("Corrupted properties: " + propertyTypeCodes.length
                    + " property type codes but " + propertyValues.length + " values.");
        }
        Map<String, String> props = new HashMap<String, String>(); 
        for (int i = 0; i < propertyTypeCodes.length; i++)
        {
            String propertyValue = propertyValues[i];
            if (StringUtils.isNotBlank(propertyValue))
            {
                props.put(propertyTypeCodes[i], propertyValue);
            }
        }
        return props;
    }

    @Override
    protected void saveAdditionalSettingsTo(NodeSettingsWO settings)
    {
        if (fileVariable != null)
        {
            settings.addString(FILE_VARIABLE_KEY, fileVariable);
        }
        if (dataSetType != null)
        {
            settings.addByteArray(DATA_SET_TYPE_KEY, Util.serializeDescription(dataSetType));
        }
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
            String variableName = Util.VARIABLE_PREFIX + ownerType.name();
            try
            {
                builder.setDataSetOwnerIdentifier(getStringFlowVariable(variableName));
            } catch (NoSuchElementException ex)
            {
                throw new IllegalArgumentException("Owner " + ownerType.toString().toLowerCase() 
                        + " hasn't been specified. Also flow variable '"
                        + variableName + "' is undefined.");
            }
        }
        builder.setFile(file);
        NewDataSetMetadataDTO dataSetMetadata = builder.getDataSetMetadata();
        dataSetMetadata.setDataSetTypeOrNull(dataSetType.getCode());
        dataSetMetadata.setProperties(properties);
        NewDataSetDTO dataSetDTO = builder.asNewDataSetDTO();

        IOpenbisServiceFacade facade = null;
        try
        {
            facade = serviceFacadeFactory.createFacade(url, userID, password);
            checkOwner(dataSetDTO, facade);
            facade.putDataSet(dataSetDTO, file);
            return new PortObject[] {};
        } finally
        {
            if (facade != null)
            {
                facade.logout();
            }
        }
    }

    private void checkOwner(NewDataSetDTO dataSetDTO, IOpenbisServiceFacade facade)
    {
        String identifier = dataSetDTO.getDataSetOwner().getIdentifier();
        DataSetOwnerType type = dataSetDTO.getDataSetOwner().getType();
        try
        {
            switch (type)
            {
                case EXPERIMENT:
                    List<Experiment> experiments = facade.getExperiments(Collections.singletonList(identifier));
                    if (experiments.isEmpty())
                    {
                        throw new IllegalArgumentException("Unknown experiment.");
                    }
                    break;
                case SAMPLE:
                    List<Sample> samples = facade.getSamples(Collections.singletonList(identifier));
                    if (samples.isEmpty())
                    {
                        throw new IllegalArgumentException("Unknown sample.");
                    } else if (samples.get(0).getExperimentIdentifierOrNull() == null)
                    {
                        throw new IllegalArgumentException("Not directly linked to an experiment.");
                    }
                    break;
                case DATA_SET:
                    DataSet dataSet = facade.getDataSet(identifier);
                    if (dataSet == null)
                    {
                        throw new IllegalArgumentException("Unknown data set.");
                    }
            }
        } catch (Exception ex)
        {
            throw new IllegalArgumentException("Error for data set owner of type " + type.toString().toLowerCase() + " '"  
                    + identifier +"': " + ex.getMessage(), ex);
        }
    }
    
    private File getFirstFile(PortObject[] inObjects)
    {
        if (inObjects.length != 1)
        {
            throw new IllegalArgumentException("Expecting exactly one port instead of " + inObjects.length + ".");
        }
        PortObject portObject = inObjects[0];
        File file;
        if (portObject instanceof URIPortObject)
        {
            List<URIContent> uriContents = ((URIPortObject) portObject).getURIContents();
            if (uriContents.isEmpty())
            {
                throw new IllegalArgumentException("Expecting at least on URI in input port.");
            }
            if (uriContents.size() > 1)
            {
                logger.warn(uriContents.size() + " URIs instead of only one: " + uriContents);
            }
            file = new File(uriContents.get(0).getURI());
        } else if (portObject instanceof FlowVariablePortObject)
        {
            if (StringUtils.isBlank(fileVariable))
            {
                throw new IllegalArgumentException("Unspecified file variable.");
            }
            file = new File(getStringFlowVariable(fileVariable));
        } else
        {
            throw new IllegalArgumentException("Invalid port: " + portObject.getClass().getName() + ".");
        }
        if (file.exists() == false)
        {
            throw new IllegalArgumentException("File does not exist: " + file);
        }
        return file;
    }
    
    protected Map<String, FlowVariable> getFlowVariables()
    {
        return getAvailableFlowVariables();
    }

}
