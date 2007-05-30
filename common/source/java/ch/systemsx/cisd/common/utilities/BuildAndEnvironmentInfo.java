/*
 * Copyright 2007 ETH Zuerich, CISD
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

package ch.systemsx.cisd.common.utilities;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * Bean with build and environment information.
 * <p>
 * Does <em>not</em> depend on any library jar files.
 * 
 * @author Franz-Josef Elmer
 */
public final class BuildAndEnvironmentInfo
{
    /**
     * The one-and-only instance.
     */
    public static final BuildAndEnvironmentInfo INSTANCE = new BuildAndEnvironmentInfo();

    private final String version;

    private final String revision;

    private final boolean cleanSources;

    private BuildAndEnvironmentInfo()
    {
        String extractedVersion = "unknown";
        String extractedRevision = "unknown";
        boolean extractedCleanFlag = false;
        InputStream stream = BuildAndEnvironmentInfo.class.getResourceAsStream("/BUILD.INFO");
        if (stream != null)
        {
            try
            {
                BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
                StringTokenizer tokenizer = new StringTokenizer(reader.readLine(), ":");
                extractedVersion = tokenizer.nextToken();
                extractedRevision = tokenizer.nextToken();
                extractedCleanFlag = "clean".equals(tokenizer.nextToken());
            } catch (IOException ex)
            {
                // ignored
            } finally
            {
                try
                {
                    stream.close();
                } catch (IOException ex)
                {
                    // ignored
                }
            }
        }
        this.version = extractedVersion;
        this.revision = extractedRevision;
        this.cleanSources = extractedCleanFlag;
    }

    /**
     * @return Name and version of the operating system.
     */
    public String getOS()
    {
        return System.getProperty("os.name") + " " + System.getProperty("os.version");
    }

    /**
     * @return Name and version of the Java Virtual Machine.
     */
    public String getJavaVM()
    {
        return System.getProperty("java.vm.name") + " " + System.getProperty("java.vm.version");
    }

    /**
     * @return The version of the software.
     */
    public String getVersion()
    {
        return version;
    }

    /**
     * @return <code>true</code> if the versioned entities of the working copy have been clean when this build has
     *         been made, in other words, whether the revision given by {@link #getRevision()} does really identify 
     *         the source that is build has been produced from.
     */
    public boolean isCleanSources()
    {
        return cleanSources;
    }

    /**
     * @return The revision number.
     */
    public String getRevision()
    {
        return revision;
    }

    /**
     * Returns the build number of the software,
     */
    public String getBuildNumber()
    {
        if (isCleanSources())
        {
            return version + " (r" + revision + ")";
        } else
        {
            return version + " (r" + revision + "*)";
        }
    }

    /**
     * Returns version, build number, Java VM, and OS as a {@link List} with four entries.
     */
    public List<String> getEnvironmentInfo()
    {
        final List<String> environmentInfo = new ArrayList<String>();
        environmentInfo.add("Version:\t" + getVersion());
        environmentInfo.add("Build number:\t" + getBuildNumber());
        environmentInfo.add("Java VM:\t" + getJavaVM());
        environmentInfo.add("OS:\t\t" + getOS());
        return environmentInfo;
    }

    /**
     * Returns version, build number, Java VM, and OS in a four-liner as one {@link String}.
     */
    @Override
    public String toString()
    {
        return StringUtilities.concatenateWithNewLine(getEnvironmentInfo());
    }

    /**
     * Shows build and environment information on the console.
     */
    public static void main(String[] args)
    {
        System.out.println(BuildAndEnvironmentInfo.INSTANCE);
    }

}
