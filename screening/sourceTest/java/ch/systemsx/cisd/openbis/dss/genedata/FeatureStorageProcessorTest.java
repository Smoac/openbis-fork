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

package ch.systemsx.cisd.openbis.dss.genedata;

import java.io.File;
import java.util.List;
import java.util.Properties;

import org.testng.annotations.Test;

import ch.systemsx.cisd.base.tests.AbstractFileSystemTestCase;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.etlserver.IStorageProcessor;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public class FeatureStorageProcessorTest extends AbstractFileSystemTestCase
{
    private static final String EXAMPLE1 = "barcode = Plate_042" 
    	+ "\n\n<Layer=alpha>\n"
        + "\t1\t2\n"
        + "A\t4.5\t4.6\n"
        + "B\t3.5\t5.6\n"
        + "C\t3.3\t5.7\n"
        + "\n\n<Layer=beta>\n"
        + "\t1\t2\n"
        + "A\t14.5\t14.6\n"
        + "B\t13.5\t15.6\n"
        + "C\t13.3\t15.7\n"
        ;

    @Test
    public void test()
    {
        File incomingDir = new File(workingDirectory, "incoming");
        incomingDir.mkdirs();
        File dataSetFile = new File(incomingDir, "Plate042.stat");
        FileUtilities.writeToFile(dataSetFile, EXAMPLE1);
        File rootDir = new File(workingDirectory, "rootDir");
        rootDir.mkdirs();
        IStorageProcessor storageProcessor = new FeatureStorageProcessor(new Properties());
        
        storageProcessor.storeData(new DataSetInformation(), null,
                null, dataSetFile, rootDir);
        
        assertEquals(0, incomingDir.listFiles().length);
        assertEquals(1, rootDir.listFiles().length);
        File original = new File(rootDir, "original");
        assertEquals(true, original.isDirectory());
        assertEquals(2, original.listFiles().length);
        
        storageProcessor.commit();
        
        assertEquals(1, original.listFiles().length);
        File transformedDataSetFile = original.listFiles()[0];
        assertEquals("Plate042.stat.txt", transformedDataSetFile.getName());
        List<String> lines = FileUtilities.loadToStringList(transformedDataSetFile);
        assertEquals("barcode,row,col,alpha,beta", lines.get(0));
        assertEquals("Plate_042,A,1,4.5,14.5", lines.get(1));
        assertEquals("Plate_042,A,2,4.6,14.6", lines.get(2));
        assertEquals("Plate_042,B,1,3.5,13.5", lines.get(3));
        assertEquals("Plate_042,B,2,5.6,15.6", lines.get(4));
        assertEquals("Plate_042,C,1,3.3,13.3", lines.get(5));
        assertEquals("Plate_042,C,2,5.7,15.7", lines.get(6));
        assertEquals(7, lines.size());
   }
}
