package ch.systemsx.cisd.openbis.plugin.screening.client.web.server;

import java.util.List;

import ch.systemsx.cisd.common.mail.IMailClient;
import ch.systemsx.cisd.common.mail.MailClient;
import ch.systemsx.cisd.openbis.dss.generic.server.MailClientParameters;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewMaterial;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewSample;
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

    private static final String LIBRARY_REGISTARION_STATUS = "Library registration status";

    private final MailClientParameters mailClientParameters;

    private final String sessionToken;

    private final String email;

    private final List<NewMaterial> newGenesOrNull;

    private final List<NewMaterial> newOligosOrNull;

    private final List<NewSamplesWithTypes> newSamplesWithType;

    private final IGenericServer server;

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
        this.server = server;
        this.mailClientParameters = mailClientParameters;
    }

    public void run()
    {
        StringBuilder message = new StringBuilder();
        try
        {
            if (newGenesOrNull != null)
            {
                server.registerMaterials(sessionToken, ScreeningConstants.GENE_PLUGIN_TYPE_CODE,
                        newGenesOrNull);
                message.append("GENES: OK\n");
                for (NewMaterial m : newGenesOrNull)
                {
                    message.append("\t" + m.getCode() + "\n");
                }
            }
        } catch (Exception ex)
        {
            message.append("GENES: ERROR (NOT REGISTERED)\n");
            message.append(ex.getMessage());
            sendEmail(LIBRARY_REGISTARION_STATUS, message.toString(), email);
            return;
        }
        try
        {
            if (newOligosOrNull != null)
            {
                server.registerMaterials(sessionToken, ScreeningConstants.OLIGO_PLUGIN_TYPE_NAME,
                        newOligosOrNull);
                message.append("OLIGOS: OK\n");
                for (NewMaterial m : newOligosOrNull)
                {
                    message.append("\t" + m.getCode() + "\n");
                }
            }
        } catch (Exception ex)
        {
            message.append("OLIGOS: ERROR (NOT REGISTERED)\n");
            message.append(ex.getMessage());
            sendEmail(LIBRARY_REGISTARION_STATUS, message.toString(), email);
            return;
        }
        try
        {
            server.registerSamples(sessionToken, newSamplesWithType);
            message.append("PLATES: OK\n");
            for (NewSamplesWithTypes s : newSamplesWithType)
            {
                message.append("\t" + s.getSampleType() + "\n");
                for (NewSample ns : s.getNewSamples())
                {
                    message.append("\t\t" + ns.getIdentifier() + "\n");
                }
            }
        } catch (Exception ex)
        {
            message.append("PLATES: ERROR (NOT REGISTERED)\n");
            message.append(ex.getMessage());
            sendEmail(LIBRARY_REGISTARION_STATUS, message.toString(), email);
            return;
        }
        sendEmail(LIBRARY_REGISTARION_STATUS, message.toString(), email);

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