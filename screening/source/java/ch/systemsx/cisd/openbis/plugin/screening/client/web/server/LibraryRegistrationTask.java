package ch.systemsx.cisd.openbis.plugin.screening.client.web.server;

import java.util.List;

import ch.systemsx.cisd.common.mail.IMailClient;
import ch.systemsx.cisd.common.mail.MailClient;
import ch.systemsx.cisd.common.mail.MailClientParameters;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewMaterial;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewSamplesWithTypes;
import ch.systemsx.cisd.openbis.plugin.generic.shared.IGenericServer;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.ScreeningConstants;

/**
 * Registers genes, oligos and plates. Sends an email to specified address upon error or completion.
 * 
 * @author Izabela Adamczyk
 */
class LibraryRegistrationTask implements Runnable
{

    private static final String SUCCESSFUL_LIBRARY_REGISTARION_STATUS =
            "Library successfuly registered";

    private static final String UNSUCCESSFUL_LIBRARY_REGISTARION_STATUS =
            "Library registration failed";

    private final MailClientParameters mailClientParameters;

    private final String sessionToken;

    private final String email;

    private final List<NewMaterial> newGenesOrNull;

    private final List<NewMaterial> newOligosOrNull;

    private final List<NewSamplesWithTypes> newSamplesWithType;

    private final IGenericServer genericServer;

    public LibraryRegistrationTask(final String sessionToken, final String email,
            final List<NewMaterial> newGenesOrNull, final List<NewMaterial> newOligosOrNull,
            final List<NewSamplesWithTypes> newSamplesWithType, IGenericServer server,
            MailClientParameters mailClientParameters)
    {
        this.sessionToken = sessionToken;
        this.email = email;
        this.newGenesOrNull = newGenesOrNull;
        this.newOligosOrNull = newOligosOrNull;
        this.newSamplesWithType = newSamplesWithType;
        this.genericServer = server;
        this.mailClientParameters = mailClientParameters;
    }

    public void run()
    {
        StringBuilder message = new StringBuilder();
        try
        {
            if (newGenesOrNull != null)
            {
                genericServer.registerOrUpdateMaterials(sessionToken,
                        ScreeningConstants.GENE_PLUGIN_TYPE_CODE, newGenesOrNull);
                message.append("Successfuly registered or updated properties of "
                        + newGenesOrNull.size() + " genes.\n");
            }
        } catch (Exception ex)
        {
            message.append("ERROR: Genes could not be registered!\n");
            message.append(ex.getMessage());
            sendErrorEmail(message.toString(), email);
            return;
        }
        try
        {
            if (newOligosOrNull != null)
            {
                genericServer.registerOrUpdateMaterials(sessionToken,
                        ScreeningConstants.OLIGO_PLUGIN_TYPE_NAME, newOligosOrNull);
                message.append("Successfuly registered " + newOligosOrNull.size() + " oligos.\n");
            }
        } catch (Exception ex)
        {
            message.append("ERROR: Oligos could not be registered!\n");
            message.append(ex.getMessage());
            sendErrorEmail(message.toString(), email);
            return;
        }
        try
        {
            genericServer.registerSamples(sessionToken, newSamplesWithType);
            for (NewSamplesWithTypes s : newSamplesWithType)
            {
                message.append("Successfuly registered " + s.getNewSamples().size()
                        + " samples of type " + s.getSampleType() + ".\n");
            }
        } catch (Exception ex)
        {
            message.append("ERROR: Plates and wells could not be registered!\n");
            message.append(ex.getMessage());
            sendErrorEmail(message.toString(), email);
            return;
        }
        sendSuccessEmail(message.toString(), email);

    }

    private void sendErrorEmail(String content, String recipient)
    {
        sendEmail(UNSUCCESSFUL_LIBRARY_REGISTARION_STATUS, content, recipient);
    }

    private void sendSuccessEmail(String content, String recipient)
    {
        sendEmail(SUCCESSFUL_LIBRARY_REGISTARION_STATUS, content, recipient);
    }

    private void sendEmail(String subject, String content, String recipient)
    {
        String from = mailClientParameters.getFrom();
        String smtpHost = mailClientParameters.getSmtpHost();
        String smtpUser = mailClientParameters.getSmtpUser();
        String smtpPassword = mailClientParameters.getSmtpPassword();
        IMailClient mailClient = new MailClient(from, smtpHost, smtpUser, smtpPassword);
        mailClient.sendMessage(subject, content, null, null, recipient);
    }

}