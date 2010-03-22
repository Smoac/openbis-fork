package ch.systemsx.cisd.openbis.plugin.generic.client.web.server;

import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import ch.systemsx.cisd.common.io.DelegatedReader;
import ch.systemsx.cisd.common.parser.IParserObjectFactory;
import ch.systemsx.cisd.common.parser.IParserObjectFactoryFactory;
import ch.systemsx.cisd.common.parser.IPropertyMapper;
import ch.systemsx.cisd.common.parser.ParserException;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.BatchRegistrationResult;
import ch.systemsx.cisd.openbis.generic.client.web.server.BisTabFileLoader;
import ch.systemsx.cisd.openbis.generic.client.web.server.NamedInputStream;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewMaterial;
import ch.systemsx.cisd.openbis.plugin.generic.client.web.server.parser.NewMaterialParserObjectFactory;

public class MaterialLoader
{

    private List<BatchRegistrationResult> results;

    private List<NewMaterial> newMaterials;

    private BisTabFileLoader<NewMaterial> tabFileLoader;

    public void load(Collection<NamedInputStream> files)
    {
        tabFileLoader =
                new BisTabFileLoader<NewMaterial>(new IParserObjectFactoryFactory<NewMaterial>()
                    {
                        public final IParserObjectFactory<NewMaterial> createFactory(
                                final IPropertyMapper propertyMapper) throws ParserException
                        {
                            return new NewMaterialParserObjectFactory(propertyMapper);
                        }
                    }, false);
        newMaterials = new ArrayList<NewMaterial>();
        results = new ArrayList<BatchRegistrationResult>(files.size());
        for (final NamedInputStream file : files)
        {
            final Reader reader = new InputStreamReader(file.getInputStream());
            final List<NewMaterial> loadedMaterials =
                    tabFileLoader.load(new DelegatedReader(reader, file.getOriginalFilename()));
            newMaterials.addAll(loadedMaterials);
            results.add(new BatchRegistrationResult(file.getOriginalFilename(), String.format(
                    "%d material(s) found and registered.", loadedMaterials.size())));
        }
    }

    public List<BatchRegistrationResult> getResults()
    {
        return new ArrayList<BatchRegistrationResult>(results);
    }

    public List<NewMaterial> getNewMaterials()
    {
        return new ArrayList<NewMaterial>(newMaterials);
    }

}