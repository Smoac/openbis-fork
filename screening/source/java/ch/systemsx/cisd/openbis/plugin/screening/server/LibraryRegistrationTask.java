package ch.systemsx.cisd.openbis.plugin.screening.server;

import java.io.IOException;
import java.io.Writer;
import java.util.Arrays;
import java.util.List;

import ch.rinn.restrictions.Private;
import ch.systemsx.cisd.base.exceptions.IOExceptionUnchecked;
import ch.systemsx.cisd.common.collections.TableMap;
import ch.systemsx.cisd.common.shared.basic.utils.StringUtils;
import ch.systemsx.cisd.openbis.generic.server.IASyncAction;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.server.util.KeyExtractorFactory;
import ch.systemsx.cisd.openbis.generic.shared.ICommonServer;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ListMaterialCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Material;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewMaterial;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewMaterialsWithTypes;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewSamplesWithTypes;
import ch.systemsx.cisd.openbis.generic.shared.dto.EntityTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.translator.MaterialTypeTranslator;
import ch.systemsx.cisd.openbis.generic.shared.util.EntityHelper;
import ch.systemsx.cisd.openbis.plugin.generic.shared.IGenericServer;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.ScreeningConstants;

/**
 * Saves genes, oligos and plates. Sends an email to specified address upon error or completion.
 * 
 * @author Izabela Adamczyk
 */
class LibraryRegistrationTask implements IASyncAction
{

    private static final String DELIM = " ";

    private final String sessionToken;

    private final List<NewMaterial> newGenesOrNull;

    private final List<NewMaterial> newOligosOrNull;

    private final List<NewSamplesWithTypes> newSamplesWithType;

    private final IGenericServer genericServer;

    private final ICommonServer commonServer;

    private final IDAOFactory daoFactory;

    public LibraryRegistrationTask(String sessionToken, List<NewMaterial> newGenesOrNull,
            List<NewMaterial> newOligosOrNull, List<NewSamplesWithTypes> newSamplesWithType,
            ICommonServer commonServer, IGenericServer server, IDAOFactory daoFactory)
    {
        this.sessionToken = sessionToken;
        this.newGenesOrNull = newGenesOrNull;
        this.newOligosOrNull = newOligosOrNull;
        this.newSamplesWithType = newSamplesWithType;
        this.commonServer = commonServer;
        this.genericServer = server;
        this.daoFactory = daoFactory;
    }

    private void registerOrUpdateSamples(Writer message) throws IOException
    {
        try
        {
            if (newSamplesWithType != null)
            {
                genericServer.registerOrUpdateSamples(sessionToken, newSamplesWithType);
                for (NewSamplesWithTypes s : newSamplesWithType)
                {
                    message.write("Successfuly saved " + s.getNewEntities().size()
                            + " samples of type " + s.getEntityType() + ".\n");
                }
            }
        } catch (RuntimeException ex)
        {
            message.write("ERROR: Plates and wells could not be saved!\n");
            message.write(ex.getMessage());
            throw ex;
        }
    }

    private void registerOrUpdateMaterials(String materialTypeCode, List<NewMaterial> newMaterials)
    {
        List<NewMaterialsWithTypes> materialsWithTypes =
                createMaterialsWithTypes(materialTypeCode, newMaterials);
        genericServer.registerOrUpdateMaterials(sessionToken, materialsWithTypes);
    }

    @Private
    static List<NewMaterialsWithTypes> createMaterialsWithTypes(String materialTypeCode,
            List<NewMaterial> newMaterials)
    {
        MaterialType materialType = new MaterialType();
        materialType.setCode(materialTypeCode);
        NewMaterialsWithTypes materialsWithType =
                new NewMaterialsWithTypes(materialType, newMaterials);
        materialsWithType.setAllowUpdateIfExist(true);
        List<NewMaterialsWithTypes> materialsWithTypes = Arrays.asList(materialsWithType);
        return materialsWithTypes;
    }

    private void registerOrUpdateOligos(Writer message) throws IOException
    {
        try
        {
            if (newOligosOrNull != null)
            {
                registerOrUpdateMaterials(ScreeningConstants.SIRNA_PLUGIN_TYPE_NAME,
                        newOligosOrNull);
                message.write("Successfuly saved " + newOligosOrNull.size() + " siRNAs.\n");
            }
        } catch (RuntimeException ex)
        {
            message.write("ERROR: siRNAs could not be saved!\n");
            message.write(ex.getMessage());
            throw ex;
        }
    }

    private void registerOrUpdateGenes(Writer message) throws IOException
    {
        try
        {
            if (newGenesOrNull != null)
            {
                TableMap<String, Material> existingGenes = listExistingGenes();
                for (NewMaterial newGene : newGenesOrNull)
                {
                    Material existingGene = existingGenes.tryGet(newGene.getCode());
                    if (existingGene != null)
                    {
                        mergeGeneTypeCode(existingGene, newGene);
                    }
                }

                registerOrUpdateMaterials(ScreeningConstants.GENE_PLUGIN_TYPE_CODE, newGenesOrNull);
                message.write("Successfuly saved properties of " + newGenesOrNull.size()
                        + " genes.\n");
            }
        } catch (RuntimeException ex)
        {
            message.write("ERROR: Genes could not be saved!\n");
            message.write(ex.getMessage());
            throw ex;
        }
    }

    private TableMap<String, Material> listExistingGenes()
    {
        EntityTypePE entityTypePE =
                daoFactory.getEntityTypeDAO(EntityKind.MATERIAL).tryToFindEntityTypeByCode(
                        ScreeningConstants.GENE_PLUGIN_TYPE_CODE);
        MaterialType materialType = MaterialTypeTranslator.translateSimple(entityTypePE);
        List<Material> materials =
                commonServer.listMaterials(sessionToken, new ListMaterialCriteria(materialType),
                        true);

        return new TableMap<String, Material>(materials,
                KeyExtractorFactory.<Material> createCodeKeyExtractor());
    }

    /**
     * when an existing gene is being updated, we merge the existing gene symbols with the newly
     * specified into a space-separated new field. If the new gene does not contain new information
     * i.e. its gene symbol is already present in the DB, the existing gene symbols are not altered.
     * <p>
     * For further information see LMS-1929.
     */
    private void mergeGeneTypeCode(Material existingMaterial, NewMaterial newMaterial)
    {
        IEntityProperty existingGeneProp =
                EntityHelper.tryFindProperty(existingMaterial.getProperties(),
                        ScreeningConstants.GENE_SYMBOLS);
        if (existingGeneProp == null)
        {
            return;
        }

        String existingGene = existingGeneProp.getValue();

        IEntityProperty newGeneProp =
                EntityHelper.tryFindProperty(newMaterial.getProperties(),
                        ScreeningConstants.GENE_SYMBOLS);

        if (newGeneProp == null)
        {
            return;
        }
        String mergedGeneType = mergeGeneTypes(existingGene, newGeneProp.getValue());
        newGeneProp.setValue(mergedGeneType);
    }

    private String mergeGeneTypes(String existingType, String newType)
    {
        if (StringUtils.isBlank(newType) || newType.equals(existingType))
        {
            return existingType;
        }

        boolean ignoreNewType =
                existingType.startsWith(newType + DELIM) || existingType.endsWith(DELIM + newType)
                        || (existingType.indexOf(DELIM + newType + DELIM) > 0);

        if (ignoreNewType)
        {
            return existingType;
        } else
        {
            return existingType + DELIM + newType;
        }
    }

    public boolean doAction(Writer messageWriter)
    {
        try
        {
            registerOrUpdateGenes(messageWriter);
            registerOrUpdateOligos(messageWriter);
            registerOrUpdateSamples(messageWriter);

            return true;
        } catch (IOException ex)
        {
            throw new IOExceptionUnchecked(ex);
        }
    }

    public String getName()
    {
        return "Library registration";
    }

}