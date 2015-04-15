/*
 * Copyright 2015 ETH Zuerich, SIS
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

package ch.systemsx.cisd.datamover.transformation;

import java.io.File;
import java.util.List;
import java.util.Properties;

import org.testng.AssertJUnit;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.exceptions.Status;
import ch.systemsx.cisd.common.logging.BufferedAppender;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public class CommandBasedTransformerTest extends AssertJUnit
{
    private BufferedAppender logRecorder;

    @BeforeMethod
    public void setUp()
    {
        logRecorder = new BufferedAppender();
    }
    
    @Test
    public void testCreateCommandWithFilename()
    {
        Properties properties = new Properties();
        properties.setProperty(CommandBasedTransformer.COMMAND_TEMPLATE_PROP, 
                "cmd   -dgH  'alpha beta' ${file-name}");
        CommandBasedTransformer transformer = new CommandBasedTransformer(properties);
        
        List<String> command = transformer.createCommand(new File("abc/def.txt"));
        
        assertEquals("[cmd, -dgH, alpha beta, def.txt]", command.toString());
    }
    
    @Test
    public void testCreateCommandWithAbsoluteFilePath()
    {
        Properties properties = new Properties();
        properties.setProperty(CommandBasedTransformer.COMMAND_TEMPLATE_PROP, 
                "cmd   -dgH  \"alpha 'beta'\" ${absolute-file-path}");
        CommandBasedTransformer transformer = new CommandBasedTransformer(properties);
        File file = new File("abc/def.txt");
        
        List<String> command = transformer.createCommand(file);
        
        assertEquals("[cmd, -dgH, alpha 'beta', " + file.getAbsolutePath() + "]", command.toString());
    }

    @Test
    public void test()
    {
        Properties properties = new Properties();
        properties.setProperty(CommandBasedTransformer.COMMAND_TEMPLATE_PROP, 
                "env MY_FILE=${file-name}");
        properties.setProperty(CommandBasedTransformer.ENV_PROP_PREFIX + "MY_NAME", "Albert");
        CommandBasedTransformer transformer = new CommandBasedTransformer(properties);

        File file = new File("abc/def.txt");
        Status status = transformer.transform(file);

        assertEquals("OK", status.toString());
        String logContent = logRecorder.getLogContent();
        String expectedStart = "Execute: [env, MY_FILE=def.txt]\n"
                + "Running command: env MY_FILE=def.txt (I/O in separate thread)\n";
        assertEquals(expectedStart, logContent.substring(0, Math.min(logContent.length(), expectedStart.length())));
        assertEquals("log content does not contain env variable MY_NAME.", true, logContent.contains("MY_NAME=Albert"));
        
    }
}
