/*
 * Copyright 2014 ETH Zuerich, SIS
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

package ch.systemsx.cisd.openbis.dss.generic.server.api.v2.sequencedatabases;

import java.io.File;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.fasta.FastaUtilities;
import ch.systemsx.cisd.common.fasta.SequenceType;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.process.ProcessExecutionHelper;
import ch.systemsx.cisd.common.process.ProcessResult;
import ch.systemsx.cisd.openbis.dss.generic.shared.utils.BlastUtils;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SequenceSearchResult;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public class BlastDatabase extends AbstractSequenceDatabase
{
    public static final Logger operationLog =
            LogFactory.getLogger(LogCategory.OPERATION, BlastDatabase.class);
    private static final Logger machineLog =
            LogFactory.getLogger(LogCategory.MACHINE, BlastDatabase.class);
    private static final String QUERY_FILE_NAME_TEMPLATE = "query-{0,date,yyyyMMDDHHmmssSSS}-{1}.fasta";
    private static final Pattern STITLE_PATTERN = Pattern.compile("(.*) \\[Data set: (.*), File: (.*)\\]$"); 

    private final File databaseFolder;
    private final String blastn;
    private final String blastp;
    private final boolean available;
    private final File queriesFolder;
    
    private AtomicInteger counter = new AtomicInteger();

    public BlastDatabase(Properties properties, File storeRoot)
    {
        super(properties, storeRoot);
        String blastToolDirectory = BlastUtils.getBLASTToolDirectory(properties);
        blastn = blastToolDirectory + "blastn";
        blastp = blastToolDirectory + "blastp";
        available = process(blastn, "-version");
        if (available == false)
        {
            BlastUtils.logMissingTools(operationLog);
        }
        databaseFolder = BlastUtils.getBlastDatabaseFolder(properties, storeRoot);
        queriesFolder = new File(databaseFolder, "queries-folder");
        queriesFolder.mkdirs();
    }

    @Override
    public boolean isAvailable()
    {
        return available;
    }
    
    @Override
    public List<SequenceSearchResult> search(String sequenceSnippet, Map<String, String> optionalParameters)
    {
        List<SequenceSearchResult> result = new ArrayList<SequenceSearchResult>();
        SequenceType sequenceType = FastaUtilities.determineSequenceType(sequenceSnippet);
        String queryFileName = new MessageFormat(QUERY_FILE_NAME_TEMPLATE).format(
                new Object[] {new Date(), counter.getAndIncrement()});
        File queryFile = new File(queriesFolder, queryFileName);
        try
        {
            FileUtilities.writeToFile(queryFile, ">query\n" + sequenceSnippet + "\n");
            List<String> command = createCommand(sequenceType, queryFile);
            List<String> output = processAndDeliverOutput(command);
            for (String line : output)
            {
                String[] row = line.split("\t");
                Matcher matcher = STITLE_PATTERN.matcher(row[0]);
                if (matcher.matches())
                {
                    SequenceSearchResult sequenceSearchResult = new SequenceSearchResult();
                    sequenceSearchResult.setSequenceIdentifier(matcher.group(1));
                    sequenceSearchResult.setDataSetCode(matcher.group(2));
                    sequenceSearchResult.setPathInDataSet(matcher.group(3));
                    sequenceSearchResult.setPositionInSequence(parse(row[1]));
                    result.add(sequenceSearchResult);
                }
            }
        } finally
        {
            FileUtilities.delete(queryFile);
        }
        return result;
    }
    
    private int parse(String number)
    {
        try
        {
            return Integer.parseInt(number);
        } catch (NumberFormatException ex)
        {
            return -1;
        }
    }

    private List<String> createCommand(SequenceType sequenceType, File queryFile)
    {
        List<String> command = new ArrayList<String>();
        if (sequenceType == SequenceType.NUCL)
        {
            command.add(blastn);
            command.add("-task");
            command.add("blastn");
        } else
        {
            command.add(blastp);
            command.add("-task");
            command.add("blastp");
        }
        command.add("-db");
        command.add(databaseFolder.getAbsolutePath() + "/all-" + sequenceType.toString().toLowerCase());
        command.add("-query");
        command.add(queryFile.getAbsolutePath());
        command.add("-outfmt");
        command.add("6 stitle sstart");
        return command;
    }
    
    private boolean process(String... command)
    {
        ProcessResult processResult = ProcessExecutionHelper.run(Arrays.asList(command), operationLog, machineLog);
        if (processResult.isOK())
        {
            processResult.logAsInfo();
        } else
        {
            processResult.log();
        }
        return processResult.isOK();
    }

    private List<String> processAndDeliverOutput(List<String> command)
    {
        ProcessResult processResult = ProcessExecutionHelper.run(command, operationLog, machineLog);
        List<String> output = processResult.getOutput();
        if (processResult.isOK())
        {
            return output;
        }
        StringBuilder builder = new StringBuilder("Execution failed: ").append(command);
        List<String> lines = processResult.isBinaryOutput() ? processResult.getErrorOutput() : output;
        for (String line : lines)
        {
            builder.append("\n").append(line);
        }
        processResult.log();
        throw new EnvironmentFailureException(builder.toString()); 
    }
}
