/*
 * Copyright 2010 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.knime;

import java.io.File;
import java.io.IOException;

import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataType;
import org.knime.core.data.def.DefaultRow;
import org.knime.core.data.def.StringCell;
import org.knime.core.node.BufferedDataContainer;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public class TestNodeModel extends NodeModel
{

    protected TestNodeModel()
    {
        super(0, 1);
    }

    @Override
    protected void loadInternals(File arg0, ExecutionMonitor arg1) throws IOException,
            CanceledExecutionException
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    protected void loadValidatedSettingsFrom(NodeSettingsRO arg0) throws InvalidSettingsException
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    protected void reset()
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    protected void saveInternals(File arg0, ExecutionMonitor arg1) throws IOException,
            CanceledExecutionException
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    protected void saveSettingsTo(NodeSettingsWO arg0)
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    protected void validateSettings(NodeSettingsRO arg0) throws InvalidSettingsException
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    protected BufferedDataTable[] execute(BufferedDataTable[] inData, ExecutionContext exec)
            throws Exception
    {
        BufferedDataContainer container = exec.createDataContainer(new DataTableSpec(new String[] {"col1", "col2"}, 
                new DataType[] {StringCell.TYPE, StringCell.TYPE}));
        for (int i = 0; i < 5; i++)
        {
            container.addRowToTable(new DefaultRow("row-"+i, "a-"+i, "b-"+i));
        }
        container.close();
        return new BufferedDataTable[] {container.getTable()};
    }
    
    @Override
    protected DataTableSpec[] configure(final DataTableSpec[] inSpecs)
            throws InvalidSettingsException
    {
        return new DataTableSpec[0];
    }
}
