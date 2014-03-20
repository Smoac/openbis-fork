/*
 * Copyright 2014 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.datastoreserver.systemtests;

import java.io.File;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.testng.annotations.Test;

import ch.systemsx.cisd.base.unix.Unix;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.ServiceProvider;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AbstractExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PhysicalDataSet;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifierFactory;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public class MoveCopyTest extends SystemTestCase
{

    @Override
    protected File getIncomingDirectory()
    {
        return new File(rootDir, "incoming-move-copy-test");
    }

    @Override
    protected int dataSetImportWaitDurationInSeconds()
    {
        return 40;
    }
    
    @Test
    public void test() throws Exception
    {
        File dataSet = new File(workingDirectory, "my-data");
        dataSet.mkdirs();
        FileUtilities.writeToFile(new File(dataSet, "greetings.txt"), "hello test");
        File folderToMoveSubfolder = new File(dataSet, "folder_to_move/subfolder");
        folderToMoveSubfolder.mkdirs();
        FileUtilities.writeToFile(new File(folderToMoveSubfolder, "greetings1.txt"), "hello world");
        File folderToCopySubfolder = new File(dataSet, "folder_to_copy/subfolder");
        folderToCopySubfolder.mkdirs();
        FileUtilities.writeToFile(new File(folderToCopySubfolder, "greetings2.txt"), "hello universe");
        
        moveFileToIncoming(dataSet);
        waitUntilDataSetImported();
        
        IEncapsulatedOpenBISService service = ServiceProvider.getOpenBISService();
        Experiment experiment = service.tryGetExperiment(ExperimentIdentifierFactory.parse("/MOVE_COPY_TEST/P1/E1"));
        List<AbstractExternalData> dataSets = service.listDataSetsByExperimentID(experiment.getId());
        PhysicalDataSet dataSet1 = dataSets.get(0).tryGetAsDataSet();
        File ds1 = new File(store, "1/" + dataSet1.getLocation() + "/original");
        StringBuilder builder = new StringBuilder();
        render(builder, ds1, ds1, new HashMap<Long, Long>());
        assertEquals("inode:0 path:somewhere/file_copy/greetings.txt size:10 content:hello test\n"  
                + "inode:1 path:somewhere/file_copy_hard_link/greetings1.txt size:10 content:hello test\n"  
                + "inode:1 path:somewhere/file_copy_hard_link/greetings2.txt size:10 content:hello test\n"  
                + "inode:1 path:somewhere/file_move/greetings.txt size:10 content:hello test\n" 
                + "inode:2 path:somewhere/folder_copy/subfolder/greetings2.txt size:14 content:hello universe\n" 
                + "inode:3 path:somewhere/folder_copy_hard_link1/subfolder/greetings2.txt size:14 content:hello universe\n"
                + "inode:3 path:somewhere/folder_copy_hard_link2/subfolder/greetings2.txt size:14 content:hello universe\n" 
                + "inode:4 path:somewhere/folder_move/subfolder/greetings1.txt size:11 content:hello world\n", 
                builder.toString());
        assertEquals(1, dataSets.size());
    }
    
    private void render(StringBuilder builder, File dataSet, File file, Map<Long, Long> inodeMap)
    {
        if (file.isFile())
        {
            long inode = Unix.getInode(file.getAbsolutePath());
            Long mappedInode = inodeMap.get(inode);
            if (mappedInode == null)
            {
                mappedInode = new Long(inodeMap.size());
                inodeMap.put(inode, mappedInode);
            }
            builder.append("inode:").append(mappedInode);
            builder.append(" path:").append(FileUtilities.getRelativeFilePath(dataSet, file));
            builder.append(" size:").append(file.length());
            builder.append(" content:").append(FileUtilities.loadToString(file).trim()).append('\n');
        }
        if (file.isDirectory())
        {
            File[] files = file.listFiles();
            Arrays.sort(files, new Comparator<File>()
                {

                    @Override
                    public int compare(File f1, File f2)
                    {
                        return f1.getPath().compareTo(f2.getPath());
                    }
                });
            for (File child : files)
            {
                render(builder, dataSet, child, inodeMap);
            }
        }
    }

}
