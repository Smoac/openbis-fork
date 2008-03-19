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

package ch.systemsx.cisd.ant.task;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Jar;
import org.apache.tools.ant.types.FileSet;

import ch.systemsx.cisd.ant.common.AbstractEclipseClasspathExecutor;
import ch.systemsx.cisd.ant.common.EclipseClasspathEntry;

/**
 * @author felmer
 */
public class RecursiveJar extends Jar
{

    private static final class SubprojectFileSetAppender extends AbstractEclipseClasspathExecutor
    {
        private final class RelatedSubprojectFileSet extends FileSet
        {
            private final FileSet set;

            private final String subProject;

            RelatedSubprojectFileSet(FileSet set, String subProject)
            {
                super(set);
                this.set = set;
                this.subProject = subProject;
            }

            @Override
            public DirectoryScanner getDirectoryScanner(Project p)
            {
                File dir = set.getDir(p);
                String path = dir.getAbsolutePath();
                File baseDir = p.getBaseDir();
                String basePath = baseDir.getAbsolutePath();
                if (path.startsWith(basePath) == false)
                {
                    throw new BuildException("Base directory of fileset has to be a relative one.");
                }
                String subdir = path.substring(basePath.length());
                File subprojectDir =
                        new File(new File(baseDir.getParentFile(), subProject), subdir);
                if (subprojectDir.exists())
                {
                    log("scan " + subprojectDir, Project.MSG_DEBUG);
                    setDir(subprojectDir);
                    return super.getDirectoryScanner(p);
                }
                return new DirectoryScanner()
                    {
                        @Override
                        public synchronized String[] getIncludedFiles()
                        {
                            return new String[0];
                        }

                        @Override
                        public synchronized String[] getIncludedDirectories()
                        {
                            return new String[0];
                        }
                    };
            }
        }

        private final RecursiveJar jar;

        private final FileSet fileSet;

        private final File subprojectsParent;

        private final Set<String> subprojectsAlreadySeen;

        SubprojectFileSetAppender(RecursiveJar jar, FileSet fileSet, File subprojectsParent)
        {
            this(jar, fileSet, subprojectsParent, new HashSet<String>());
        }

        SubprojectFileSetAppender(RecursiveJar jar, FileSet fileSet, File subprojectsParent,
                Set<String> subprojectsAlreadySeen)
        {
            this.jar = jar;
            this.fileSet = fileSet;
            this.subprojectsParent = subprojectsParent;
            this.subprojectsAlreadySeen = subprojectsAlreadySeen;
        }

        @Override
        protected void executeEntries(List<EclipseClasspathEntry> entries)
        {
            for (EclipseClasspathEntry entry : entries)
            {
                if (entry.isSubprojectEntry())
                {
                    String subproject = entry.getPath().substring(1);
                    if (subprojectsAlreadySeen.contains(subproject))
                    {
                        continue;
                    }
                    subprojectsAlreadySeen.add(subproject);
                    jar.addFilesetPlain(new RelatedSubprojectFileSet(fileSet, subproject));
                    File subprojectDir = new File(subprojectsParent, subproject);
                    new SubprojectFileSetAppender(jar, fileSet, subprojectsParent,
                            subprojectsAlreadySeen).execute(subprojectDir);
                }
            }
        }

        @Override
        protected void handleAbsentsOfClasspathFile(File eclipseClasspathFile)
        {
        }

    }

    @Override
    public void addFileset(final FileSet set)
    {
        addFilesetPlain(set);
        File subprojectsParent = getProject().getBaseDir().getParentFile();
        SubprojectFileSetAppender appender =
                new SubprojectFileSetAppender(this, set, subprojectsParent);
        appender.executeFor(this);
    }

    void addFilesetPlain(FileSet set)
    {
        super.addFileset(set);
    }

}
