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

package ch.systemsx.cisd.openbis.dss.generic.server.graph;

import java.io.File;
import java.io.IOException;

import org.testng.annotations.Test;

/**
 * @author Chandrasekhar Ramakrishnan
 */
public class TabularDataHeatmapTest extends AbstractTabularDataGraphTest
{
    @Test
    public void testHeatmap() throws IOException
    {
        File outputFile = getImageOutputFile();

        TabularDataHeatmapConfiguration config =
                new TabularDataHeatmapConfiguration("Test", "WellName", "InfectionIndex", 300, 200);
        AbstractTabularDataGraph<TabularDataHeatmapConfiguration> graph =
                new TabularDataHeatmap(config, getDatasetFileLines(), getOutputStream(outputFile));
        assertSame(graph.tryXColumnNumber(), graph.tryYColumnNumber());
        graph.generateImage();

        assertTrue(outputFile.exists());
        assertTrue(outputFile.length() > 0);
    }

    @Test
    public void testBigNumberHeatmap() throws IOException
    {
        File outputFile = getImageOutputFile();

        TabularDataHeatmapConfiguration config =
                new TabularDataHeatmapConfiguration("Test", "WellName", "BigNumber", 300, 200);
        AbstractTabularDataGraph<TabularDataHeatmapConfiguration> graph =
                new TabularDataHeatmap(config, getBigNumberDatasetFileLines(),
                        getOutputStream(outputFile));
        assertSame(graph.tryXColumnNumber(), graph.tryYColumnNumber());
        graph.generateImage();

        assertTrue(outputFile.exists());
        assertTrue(outputFile.length() > 0);
    }

    @Test
    public void testIncorrectlyConfiguredHeatmap() throws IOException
    {
        File outputFile = getImageOutputFile();

        TabularDataHeatmapConfiguration config =
                new TabularDataHeatmapConfiguration("Test", "WellName", "Non-Existant", 300, 200);
        AbstractTabularDataGraph<TabularDataHeatmapConfiguration> graph =
                new TabularDataHeatmap(config, getDatasetFileLines(), getOutputStream(outputFile));

        assertTrue(graph.tryColumnNumberForHeader(config.getZAxisColumn()) < 0);

        graph.generateImage();

        assertTrue(outputFile.exists());
        assertTrue(outputFile.length() > 0);
    }
}
