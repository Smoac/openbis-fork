package ch.systemsx.cisd.etlserver.plugins;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.apache.log4j.Logger;
import org.springframework.transaction.annotation.Transactional;

import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.mail.EMailAddress;
import ch.systemsx.cisd.common.mail.IMailClient;
import ch.systemsx.cisd.common.mail.MailClient;
import ch.systemsx.cisd.common.maintenance.IMaintenanceTask;
import ch.systemsx.cisd.common.properties.PropertyUtils;
import ch.systemsx.cisd.openbis.dss.generic.server.DataStoreServer;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard.archiver.dataaccess.IMultiDataSetArchiverReadonlyQueryDAO;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard.archiver.dataaccess.MultiDataSetArchiverContainerDTO;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard.archiver.dataaccess.MultiDataSetArchiverDataSourceUtil;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.ServiceProvider;
import ch.systemsx.cisd.openbis.generic.shared.dto.SimpleDataSetInformationDTO;

public class MultiDataSetArchiverOrphanFinderTask implements IMaintenanceTask
{

    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION, MultiDataSetArchiverOrphanFinderTask.class);

    public static final String EMAIL_ADDRESSES_KEY = "email-addresses";

    private static final String SEPARATOR = ",";

    private List<EMailAddress> emailAddresses;

    private IMailClient mailClient;

    @Override
    public void setUp(String pluginName, Properties properties)
    {
        operationLog.info("Task " + pluginName + " initialized.");
        emailAddresses = getEMailAddresses(properties);
        mailClient = new MailClient(DataStoreServer.getConfigParameters().getProperties());
    }

    @Transactional
    @Override
    public void execute()
    {
        operationLog.info(MultiDataSetArchiverOrphanFinderTask.class.getSimpleName() + " Started");

        // 1.Directories
        operationLog.info("1.Directories, obtain archiver directory.");
        String destination = DataStoreServer.getConfigParameters().getProperties().getProperty("archiver.final-destination", null);
        if (destination == null)
        {
            destination = DataStoreServer.getConfigParameters().getProperties().getProperty("archiver.destination", null);
        }

        if (destination == null)
        {
            operationLog.info("No destination directory found, this task can't execute.");
            return;
        }

        // 2.Database
        operationLog.info("2.1 Database, obtain a list of the multi dataset containers on the database.");
        IMultiDataSetArchiverReadonlyQueryDAO readonlyQuery = MultiDataSetArchiverDataSourceUtil.getReadonlyQueryDAO();
        List<MultiDataSetArchiverContainerDTO> containerDTOs = readonlyQuery.listContainers();
        Set<String> multiDatasetsContainersOnDB = new HashSet<String>();
        for (MultiDataSetArchiverContainerDTO containerDTO : containerDTOs)
        {
            multiDatasetsContainersOnDB.add(containerDTO.getPath());
        }

        operationLog.info("2.2 Database, obtain a list of the archived datasets on the database.");
        IEncapsulatedOpenBISService service = ServiceProvider.getOpenBISService();
        List<SimpleDataSetInformationDTO> presentDTOs = service.listPhysicalDataSetsByArchivingStatus(null, Boolean.TRUE);
        Set<String> presentInArchiveOnDB = new HashSet<String>();
        for (SimpleDataSetInformationDTO presentDTO : presentDTOs)
        {
            presentInArchiveOnDB.add(presentDTO.getDataSetCode());
        }

        // 3.Verify if the files on destination are on multi dataset archiver containers or a normal archived dataset
        operationLog.info("3.Verify if the files on destination are on multi dataset archiver containers or a normal archived dataset.");
        File[] filesOnDisk = new File(destination).listFiles();
        List<File> notFounds = new ArrayList<File>();
        for (File file : filesOnDisk)
        {
            if (multiDatasetsContainersOnDB.contains(file.getName()))
            {
                operationLog.info("Found multi dataset archiver container: " + file.getName());
            } else if ((file.getName().toLowerCase().endsWith(".tar") ||
                    file.getName().toLowerCase().endsWith(".zip"))
                    && presentInArchiveOnDB.contains(file.getName().substring(0, file.getName().length() - 4)))
            {
                operationLog.info("Found archived dataset: " + file.getName());
            } else
            {
                notFounds.add(file);
                operationLog.info("Not Found file: " + file.getName());
            }
        }

        // 4.Send email with not found files.
        operationLog.info("4.Send email with not found files.");
        if (notFounds.size() > 0)
        {
            String subject = "openBIS MultiDataSetArchiverOrphanFinderTask found files";
            String content = "Found " + notFounds.size() + " files by MultiDataSetArchiverOrphanFinderTask:\n";
            for (File notFound : notFounds)
            {
                content += notFound.getName() + "\t" + notFound.length() + "\n";
            }
            for (EMailAddress recipient : emailAddresses)
            {
                mailClient.sendEmailMessage(subject, content, null, null, recipient);
                operationLog.info("Mail sent to " + recipient.tryGetEmailAddress());
            }
        }

        operationLog.info(MultiDataSetArchiverOrphanFinderTask.class.getSimpleName() + " Finished");
    }

    private List<EMailAddress> getEMailAddresses(Properties properties)
    {
        String[] tokens =
                PropertyUtils.getMandatoryProperty(properties, EMAIL_ADDRESSES_KEY)
                        .split(SEPARATOR);
        List<EMailAddress> addresses = new ArrayList<EMailAddress>();
        for (String token : tokens)
        {
            addresses.add(new EMailAddress(token.trim()));
        }
        return addresses;
    }

}
