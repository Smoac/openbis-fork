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

package ch.systemsx.cisd.openbis.uitest.page.tab;

import ch.systemsx.cisd.openbis.uitest.infra.Locate;
import ch.systemsx.cisd.openbis.uitest.infra.NotAlwaysPresent;
import ch.systemsx.cisd.openbis.uitest.page.NavigationPage;
import ch.systemsx.cisd.openbis.uitest.type.Experiment;
import ch.systemsx.cisd.openbis.uitest.type.ExperimentType;
import ch.systemsx.cisd.openbis.uitest.type.Sample;
import ch.systemsx.cisd.openbis.uitest.widget.Button;
import ch.systemsx.cisd.openbis.uitest.widget.DropDown;
import ch.systemsx.cisd.openbis.uitest.widget.Text;
import ch.systemsx.cisd.openbis.uitest.widget.TextArea;

public class RegisterExperiment extends NavigationPage
{

    @Locate("openbis_select_experiment-typeopenbis_experiment-registration")
    private DropDown experimentTypeList;

    @NotAlwaysPresent
    @Locate("openbis_generic-experiment-register_formcode")
    private Text code;

    @NotAlwaysPresent
    @Locate("openbis_select_projectgeneric-experiment-register_form")
    private DropDown projectList;

    @NotAlwaysPresent
    @Locate("generic-experiment-register_form_samples")
    private TextArea samples;

    @NotAlwaysPresent
    @Locate("openbis_generic-experiment-register_formsave-button")
    private Button saveButton;

    public void fillWith(Experiment experiment)
    {
        code.write(experiment.getCode());
        projectList.select(experiment.getProject().getCode() + " ("
                + experiment.getProject().getSpace().getCode() + ")");

        samples.clear();
        for (Sample sample : experiment.getSamples())
        {
            samples.append(sample.getCode() + ", ");
        }
    }

    public RegisterExperiment selectExperimentType(ExperimentType experimentType)
    {
        experimentTypeList.select(experimentType.getCode());
        return get(RegisterExperiment.class);
    }

    public RegisterExperiment save()
    {
        this.saveButton.click();
        return get(RegisterExperiment.class);
    }

    @Override
    public String toString()
    {
        return "Register Experiment tab";
    }
}
