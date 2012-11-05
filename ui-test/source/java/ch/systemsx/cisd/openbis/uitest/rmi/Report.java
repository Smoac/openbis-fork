/*
 * Copyright 2012 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.uitest.rmi;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import ch.systemsx.cisd.openbis.plugin.query.shared.api.v1.IQueryApiServer;
import ch.systemsx.cisd.openbis.plugin.query.shared.api.v1.dto.QueryTableModel;
import ch.systemsx.cisd.openbis.uitest.dsl.Command;
import ch.systemsx.cisd.openbis.uitest.dsl.Inject;

/**
 * @author anttil
 */
public class Report implements Command<Void>
{

    @Inject
    private String session;

    @Inject
    private IQueryApiServer query;

    private List<String> dataSetCodes;

    public Report(String dataSetCode, String... restCodes)
    {
        this.dataSetCodes = new ArrayList<String>();
        this.dataSetCodes.add(dataSetCode);
        this.dataSetCodes.addAll(Arrays.asList(restCodes));
    }

    @Override
    public Void execute()
    {
        QueryTableModel report =
                query.createReportFromDataSets(session, "INTERNAL", "read-all-files", dataSetCodes);

        System.out.println("REPORT: " + report);
        // TODO Auto-generated method stub
        return null;
    }
}
