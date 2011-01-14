package ch.systemsx.cisd.openbis.plugin.screening.server;

import java.util.Date;
import java.util.List;

import ch.systemsx.cisd.common.collections.TableMap;
import ch.systemsx.cisd.common.mail.IMailClient;
import ch.systemsx.cisd.common.shared.basic.utils.StringUtils;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.server.util.KeyExtractorFactory;
import ch.systemsx.cisd.openbis.generic.shared.ICommonServer;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ListMaterialCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Material;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewMaterial;
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
class LibraryRegistrationTask implements Runnable
{

    private static final String SUCCESSFUL_LIBRARY_REGISTARION_STATUS =
            "Library successfully registered";

    private static final String UNSUCCESSFUL_LIBRARY_REGISTARION_STATUS =
            "Library registration failed";

    private static final String DELIM = " ";


    private final String sessionToken;

    private final String email;

    private final List<NewMaterial> newGenesOrNull;

    private final List<NewMaterial> newOligosOrNull;

    private final List<NewSamplesWithTypes> newSamplesWithType;

    private final IGenericServer genericServer;

    private final ICommonServer commonServer;

    private final IDAOFactory daoFactory;

    private final IMailClient mailClient;


    public LibraryRegistrationTask(String sessionToken, String email,
            List<NewMaterial> newGenesOrNull, List<NewMaterial> newOligosOrNull,
            List<NewSamplesWithTypes> newSamplesWithType, ICommonServer commonServer,
            IGenericServer server, IDAOFactory daoFactory, IMailClient mailClient)
    {
        this.sessionToken = sessionToken;
        this.email = email;
        this.newGenesOrNull = newGenesOrNull;
        this.newOligosOrNull = newOligosOrNull;
        this.newSamplesWithType = newSamplesWithType;
        this.commonServer = commonServer;
        this.genericServer = server;
        this.daoFactory = daoFactory;
        this.mailClient = mailClient;
    }

    public void run()
    {
        boolean success = true;
        Date startDate = new Date();
        StringBuilder message = new StringBuilder();

        try
        {
            // when one of these methods fails it will throw an unchecked exception
            registerOrUpdateGenes(message);
            registerOrUpdateOligos(message);
            registerOrUpdateSamples(message);
        } catch (RuntimeException rex)
        {
            success = false;
        }

        sendEmail(message.toString(), startDate, email, success);
    }

    private void registerOrUpdateSamples(StringBuilder message)
    {
        try
        {
            if (newSamplesWithType != null)
            {
                genericServer.registerOrUpdateSamples(sessionToken, newSamplesWithType);
                for (NewSamplesWithTypes s : newSamplesWithType)
                {
                    message.append("Successfuly saved " + s.getNewSamples().size()
                            + " samples of type " + s.getSampleType() + ".\n");
                }
            }
        } catch (RuntimeException ex)
        {
            message.append("ERROR: Plates and wells could not be saved!\n");
            message.append(ex.getMessage());
            throw ex;
        }
    }

    private void registerOrUpdateOligos(StringBuilder message)
    {
        try
        {
            if (newOligosOrNull != null)
            {
                genericServer.registerOrUpdateMaterials(sessionToken,
                        ScreeningConstants.SIRNA_PLUGIN_TYPE_NAME, newOligosOrNull);
                message.append("Successfuly saved " + newOligosOrNull.size() + " siRNAs.\n");
            }
        } catch (RuntimeException ex)
        {
            message.append("ERROR: siRNAs could not be saved!\n");
            message.append(ex.getMessage());
            throw ex;
        }
    }

    private void registerOrUpdateGenes(StringBuilder message)
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
                
                genericServer.registerOrUpdateMaterials(sessionToken,
                        ScreeningConstants.GENE_PLUGIN_TYPE_CODE, newGenesOrNull);
                message.append("Successfuly saved properties of " + newGenesOrNull.size()
                        + " genes.\n");
            }
        } catch (RuntimeException ex)
        {
            message.append("ERROR: Genes could not be saved!\n");
            message.append(ex.getMessage());
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


    private void sendEmail(String content, Date startDate, String recipient,
            boolean successful)
    {
        String status =
                successful ? SUCCESSFUL_LIBRARY_REGISTARION_STATUS
                        : UNSUCCESSFUL_LIBRARY_REGISTARION_STATUS;

        String subject = addDate(status, startDate);
        mailClient.sendMessage(subject, content, null, null, recipient);
    }

    private static String addDate(String subject, Date startDate)
    {
        return subject + " (initiated at " + startDate + ")";
    }

}