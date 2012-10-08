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

package ch.systemsx.cisd.openbis.uitest.gui;

import ch.systemsx.cisd.openbis.uitest.functionality.AbstractExecution;
import ch.systemsx.cisd.openbis.uitest.functionality.UpdateSampleType;
import ch.systemsx.cisd.openbis.uitest.layout.SampleTypeBrowserLocation;
import ch.systemsx.cisd.openbis.uitest.page.Browsable;
import ch.systemsx.cisd.openbis.uitest.page.EditSampleTypeDialog;
import ch.systemsx.cisd.openbis.uitest.page.SampleTypeBrowser;
import ch.systemsx.cisd.openbis.uitest.type.BrowsableWrapper;
import ch.systemsx.cisd.openbis.uitest.type.SampleType;

/**
 * @author anttil
 */
public class UpdateSampleTypeGui extends AbstractExecution<UpdateSampleType, Void>
{

    @Override
    public Void run(UpdateSampleType request)
    {
        SampleType sampleType = request.getType();
        Browsable b = new BrowsableWrapper(sampleType);
        SampleTypeBrowser browser = browseTo(new SampleTypeBrowserLocation());
        browser.select(b);
        browser.edit();
        EditSampleTypeDialog dialog = load(EditSampleTypeDialog.class);
        dialog.fillWith(sampleType);
        dialog.save();
        browser.resetFilters();
        return null;
    }

}
