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

package ch.systemsx.cisd.openbis.dss.etl.dynamix.tools.feature_converter;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.io.FileUtils;

import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.parser.IParserObjectFactory;
import ch.systemsx.cisd.common.parser.IParserObjectFactoryFactory;
import ch.systemsx.cisd.common.parser.IPropertyMapper;
import ch.systemsx.cisd.common.parser.ParserException;
import ch.systemsx.cisd.common.parser.ParsingException;
import ch.systemsx.cisd.openbis.generic.shared.parser.BisTabFileLoader;

/**
 * @author Izabela Adamczyk
 */
public class FeatureVectorConverter
{

    public static void main(String[] args) throws ParserException, ParsingException,
            IllegalArgumentException, IOException
    {
        String in = args[0];
        IParserObjectFactoryFactory<InputRow> parser = new IParserObjectFactoryFactory<InputRow>()
            {
                public IParserObjectFactory<InputRow> createFactory(IPropertyMapper propertyMapper)
                        throws ParserException
                {
                    return new InputRowFactory(InputRow.class, propertyMapper);
                }
            };
        BisTabFileLoader<InputRow> loader = new BisTabFileLoader<InputRow>(parser, false);
        File inFile = new File(in);
        List<InputRow> list = loader.load(FileUtils.openInputStream(inFile));
        List<InputRowsNamedCollection> experiments = InputRowsHelper.extract(list);
        for (InputRowsNamedCollection e : experiments)
        {
            String outName = e.getName() + ".csv";
            File outFile =
                    FileUtilities.createNextNumberedFile(new File(inFile.getParent(), outName),
                            null);
            FileUtils.writeLines(outFile, InputRowsHelper.toTsv(e.getRows()));
            System.out.println(outFile.getPath());
        }
    }

}
