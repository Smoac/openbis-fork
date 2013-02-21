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

package ch.systemsx.cisd.openbis.dss.client.admin;



/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public class MoveDataSetCommand extends AbstractCommand
{   
    static final class MoveDataSetCommandArguments extends CommonArguments
    {
        String getDataSetCode()
        {
            return arguments.isEmpty() ? "" : arguments.get(0);
        }
        
        String getShareId()
        {
            return arguments.size() < 2 ? "" : arguments.get(1);
        }

        @Override
        protected boolean allAdditionalMandatoryArgumentsPresent()
        {
            return arguments.size() == 2;
        }
    }

    private MoveDataSetCommandArguments arguments;
    
    MoveDataSetCommand()
    {
        super("move");
        arguments = new MoveDataSetCommandArguments();
    }

    @Override
    protected MoveDataSetCommandArguments getArguments()
    {
        return arguments;
    }

    @Override
    protected String getRequiredArgumentsString()
    {
        return "<data set code> <share id>";
    }

    @Override
    void execute()
    {
        String dataSetCode = arguments.getDataSetCode();
        String shareId = arguments.getShareId();
        service.shuffleDataSet(sessionToken, dataSetCode, shareId);
        System.out.println("Data set " + dataSetCode + " successfully moved to share " + shareId + ".");
    }
}
