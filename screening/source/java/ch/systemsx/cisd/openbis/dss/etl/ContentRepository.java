/*
 * Copyright 2010 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.dss.etl;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import ch.systemsx.cisd.common.io.IContent;

/**
 * @author Franz-Josef Elmer
 */
public class ContentRepository implements IContentRepository
{
    // separates the archive name from the path to the file inside the archive
    public static final String ARCHIVE_DELIMITER = ":";

    private final File rootDirectory;

    private final IContentRepository defaultRepository;

    private final Map<String, IContentRepositoryFactory> factories;

    private final Map<String, IContentRepository> repositories =
            new HashMap<String, IContentRepository>();

    public ContentRepository(File rootDirectory,
            Map<String, IContentRepositoryFactory> repositoryFactories)
    {
        this.rootDirectory = rootDirectory;
        factories = repositoryFactories;
        defaultRepository = new FilesystemBasedContentRepository(rootDirectory);
    }

    public void open()
    {
        defaultRepository.open();
    }

    public IContent getContent(String path)
    {
        int indexOfArchiveDelimiter = path.indexOf(ARCHIVE_DELIMITER);
        if (indexOfArchiveDelimiter < 0)
        {
            return defaultRepository.getContent(path);
        }
        String archivePath = path.substring(0, indexOfArchiveDelimiter);
        String pathInArchive = path.substring(indexOfArchiveDelimiter + ARCHIVE_DELIMITER.length());
        IContentRepository repository = repositories.get(archivePath);
        if (repository == null)
        {
            int lastIndexOfDot = archivePath.lastIndexOf('.');
            if (lastIndexOfDot < 0)
            {
                throw new IllegalArgumentException(
                        "Invalid path because archive file type missing: " + path);
            }
            String archiveType = archivePath.substring(lastIndexOfDot + 1).toLowerCase();
            IContentRepositoryFactory factory = factories.get(archiveType);
            if (factory == null)
            {
                throw new IllegalArgumentException(
                        "Invalid path because archive file type unknown: " + path);
            }
            repository = factory.createRepository(rootDirectory, archivePath);
            repository.open();
            repositories.put(archivePath, repository);
        }
        return repository.getContent(pathInArchive);
    }

    public void close()
    {
        defaultRepository.close();
        for (IContentRepository repository : repositories.values())
        {
            repository.close();
        }
    }

}
