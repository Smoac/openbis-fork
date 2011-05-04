/*
 * Copyright 2011 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.installer.izpack;

import java.io.File;

import com.izforge.izpack.installer.AutomatedInstallData;

/**
 * @author Kaloyan Enimanev
 */
public class InstallationContext
{
    /**
     * set to true if the installation process is trying to update an existing openBIS installation.
     */
    public static boolean isUpdateInstallation = false;

    /**
     * set to true if this is the first openBIS installation on the machine.
     */
    public static boolean isFirstTimeInstallation = true;

    public static File installDir;

    public static void initialize(AutomatedInstallData data)
    {
        String installPath = data.getInstallPath();
        System.out.println("Initializing -- " + installPath);
        installDir = new File(installPath);
        isFirstTimeInstallation = (installDir.exists() == false);
        isUpdateInstallation = installDir.exists();
    }

}
