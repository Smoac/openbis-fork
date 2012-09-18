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

package ch.systemsx.cisd.openbis.uitest.page.dialog;

import ch.systemsx.cisd.openbis.uitest.infra.Locate;
import ch.systemsx.cisd.openbis.uitest.page.Page;
import ch.systemsx.cisd.openbis.uitest.page.tab.SpaceBrowser;
import ch.systemsx.cisd.openbis.uitest.type.Space;
import ch.systemsx.cisd.openbis.uitest.widget.Button;
import ch.systemsx.cisd.openbis.uitest.widget.Text;

public class AddSpaceDialog extends Page
{

    @Locate("openbis_dialog-code-field")
    private Text code;

    @Locate("openbis_dialog-save-button")
    private Button save;

    /* this is not deleted as this is an example of WAIT
    public SpaceBrowser addSpace(String name, String description)
    {
        this.code.sendKeys(name);
        saveButton.click();
        wait(By.xpath("//div[.=\"" + name.toUpperCase() + "\"]"));
        return get(SpaceBrowser.class);
    }
    */
    public void fillWith(Space space)
    {
        code.write(space.getCode());
    }

    public SpaceBrowser save()
    {
        save.click();
        return get(SpaceBrowser.class);
    }
}
