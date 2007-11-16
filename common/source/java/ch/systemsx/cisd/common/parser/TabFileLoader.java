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

package ch.systemsx.cisd.common.parser;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.exceptions.UserFailureException;

/**
 * Convenient class to load a tab file and deliver a list of beans of type <code>T</code>.
 * 
 * @author Franz-Josef Elmer
 */
public class TabFileLoader<T>
{

    private final IParserObjectFactoryFactory<T> factory;

    /**
     * Creates a new instance based on the specified factory.
     */
    public TabFileLoader(final IParserObjectFactoryFactory<T> factory)
    {
        assert factory != null : "Undefined factory";
        this.factory = factory;
    }

    /**
     * Loads from the specified tab file a list of objects of type <code>T</code>.
     * 
     * @throws UserFailureException if the file does not exists, the header line with correct column bean attribute
     *             names is missing, or a parsing error occurs.
     * @throws EnvironmentFailureException if a IOException occured.
     */
    public final List<T> load(final File file)
    {
        // Just check whether the file exists. Lets <code>FileUtils</code> do the rest.
        if (file.exists() == false)
        {
            throw new UserFailureException("Given file '" + file.getAbsolutePath() + "' does not exist.");
        }
        DefaultReaderParser<T> parser = new DefaultReaderParser<T>();
        final ParserUtilities.Line headerLine =
                ParserUtilities.getFirstAcceptedLine(file, ExcludeEmptyAndCommentLineFilter.INSTANCE);
        if (headerLine == null)
        {
            throw new UserFailureException("No header line found in file '" + file.getAbsolutePath() + "'.");
        }
        final HeaderLineFilter lineFilter = new HeaderLineFilter(headerLine.number);
        final IAliasPropertyMapper propertyMapper = new HeaderFilePropertyMapper(StringUtils.split(headerLine.text));
        parser.setObjectFactory(factory.createFactory(propertyMapper));
        FileReader reader = null;
        try
        {
            reader = new FileReader(file);
            return parser.parse(reader, lineFilter);
        } catch (IOException ex)
        {
            throw new EnvironmentFailureException(ex.getMessage());
        } catch (ParseException ex)
        {
            throw UserFailureException.fromTemplate("A problem has occurred while parsing line %d of file '%s' [%s].",
                    ex.getLineNumber(), file, ex.getCause().getMessage());
        } finally
        {
            IOUtils.closeQuietly(reader);
        }

    }
}
