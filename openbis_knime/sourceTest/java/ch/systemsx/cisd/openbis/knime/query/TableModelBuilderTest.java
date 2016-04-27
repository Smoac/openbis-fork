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

package ch.systemsx.cisd.openbis.knime.query;

import javax.swing.table.TableModel;

import org.testng.AssertJUnit;
import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataSet;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataSet.DataSetInitializer;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.EntityRegistrationDetails;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.EntityRegistrationDetails.EntityRegistrationDetailsInitializer;
import ch.systemsx.cisd.openbis.knime.common.TableModelBuilder;

/**
 * @author Franz-Josef Elmer
 */
public class TableModelBuilderTest extends AssertJUnit
{

    @Test
    public void testTwoDataSetOfSameTypeNoSamples()
    {
        TableModelBuilder builder = new TableModelBuilder();
        builder.add(new DataSetBuilder("1-2").type("A").experiment("/A/B/C").property("A", "abc")
                .getDataSet());
        builder.add(new DataSetBuilder("1-3").type("A").experiment("/A/B/D").property("B", "def")
                .getDataSet());

        TableModel tableModel = builder.getTableModel();

        assertEquals("Code,Experiment,A,B\n" + "1-2,/A/B/C,abc,\n" + "1-3,/A/B/D,,def\n",
                render(tableModel));
    }

    @Test
    public void testTwoDataSetOfDifferentTypeWithSamples()
    {
        TableModelBuilder builder = new TableModelBuilder();
        builder.add(new DataSetBuilder("1-2").type("A").experiment("/A/B/C").sample("/A/1")
                .getDataSet());
        builder.add(new DataSetBuilder("1-3").type("B").experiment("/A/B/D").getDataSet());

        TableModel tableModel = builder.getTableModel();

        assertEquals("Code,Type,Sample,Experiment\n" + "1-2,A,/A/1,/A/B/C\n" + "1-3,B,,/A/B/D\n",
                render(tableModel));
    }

    private String render(TableModel tableModel)
    {
        StringBuilder builder = new StringBuilder();
        int rowCount = tableModel.getRowCount();
        int columnCount = tableModel.getColumnCount();
        for (int columnIndex = 0; columnIndex < columnCount; columnIndex++)
        {
            if (columnIndex > 0)
            {
                builder.append(",");
            }
            builder.append(tableModel.getColumnName(columnIndex));
        }
        builder.append("\n");
        for (int rowIndex = 0; rowIndex < rowCount; rowIndex++)
        {
            for (int columnIndex = 0; columnIndex < columnCount; columnIndex++)
            {
                if (columnIndex > 0)
                {
                    builder.append(",");
                }
                builder.append(tableModel.getValueAt(rowIndex, columnIndex));
            }
            builder.append("\n");
        }
        return builder.toString();
    }

    private static final class DataSetBuilder
    {
        private DataSetInitializer initializer = new DataSetInitializer();

        DataSetBuilder(String code)
        {
            initializer.setCode(code);
            initializer.setRegistrationDetails(new EntityRegistrationDetails(
                    new EntityRegistrationDetailsInitializer()));
        }

        DataSet getDataSet()
        {
            return new DataSet(initializer);
        }

        DataSetBuilder type(String type)
        {
            initializer.setDataSetTypeCode(type);
            return this;
        }

        DataSetBuilder sample(String sampleIdentifier)
        {
            initializer.setSampleIdentifierOrNull(sampleIdentifier);
            return this;
        }

        DataSetBuilder experiment(String experimentIdentifier)
        {
            initializer.setExperimentIdentifier(experimentIdentifier);
            return this;
        }

        DataSetBuilder property(String key, String value)
        {
            initializer.getProperties().put(key, value);
            return this;
        }

    }

}
