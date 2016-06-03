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

        // 1.Database, obtain a list of the containers on the database.
        operationLog.info("1.Database, obtain a list of the containers on the database.");
        IMultiDataSetArchiverReadonlyQueryDAO readonlyQuery = MultiDataSetArchiverDataSourceUtil.getReadonlyQueryDAO();
        List<MultiDataSetArchiverContainerDTO> containerDTOs = readonlyQuery.listContainers();
        Set<String> containersOnDB = new HashSet<String>();
        for (MultiDataSetArchiverContainerDTO containerDTO : containerDTOs)
        {
            containersOnDB.add(containerDTO.getPath());
        }

        // 2.Directories, obtain a list of the files into the directory.
        operationLog.info("2.Directories, obtain a list of the files into the directory.");
        // String tempFolder = DataStoreServer.getConfigParameters().getProperties().getProperty("archiver.temp-folder", null);
        String finalDestination = DataStoreServer.getConfigParameters().getProperties().getProperty("archiver.final-destination", null);
        // String replicatedDestination = DataStoreServer.getConfigParameters().getProperties().getProperty("archiver.replicated-destination", null);
        File[] containersOnDisk = new File(finalDestination).listFiles();

        // 3.Verify if the files are on the database containers
        operationLog.info("3.Verify if the files are on the database containers.");
        List<File> notFound = new ArrayList<File>();
        for (File file : containersOnDisk)
        {
            if (containersOnDB.contains(file.getName()))
            {
                operationLog.info("Found container: " + file.getName());
            } else
            {
                notFound.add(file);
                operationLog.info("Not Found file: " + file.getName());
            }
        }

        // 4.Verify if the non found files follow the format needed to qualify as Multi DataSet Archiver containers.
        operationLog.info("4.Verify if the non found files follow the format needed to qualify as Multi DataSet Archiver containers.");
        List<File> notFoundFilesAndMDAFormat = new ArrayList<File>();
        for (File file : notFound)
        {
            String name = file.getName();
            String[] nameParts = name.split("-");
            if (nameParts.length == 4 && name.endsWith(".tar"))
            {
                notFoundFilesAndMDAFormat.add(file);
                operationLog.info("Not Found file, MDA format: " + file.getName());
            } else
            {
                operationLog.info("Not Found file, not MDA format: " + file.getName());
            }
        }

        // 5.Send email with not found files with MDA format.
        operationLog.info("Send email with not found files with MDA format.");
        if (notFoundFilesAndMDAFormat.size() > 0)
        {
            String subject = "openBIS MultiDataSetArchiverOrphanFinderTask found files";
            String content = "Found " + notFoundFilesAndMDAFormat.size() + " files by MultiDataSetArchiverOrphanFinderTask:\n";
            for (File notFoundFileAndMDAFormat : notFoundFilesAndMDAFormat)
            {
                content += notFoundFileAndMDAFormat.getName() + "\t" + notFoundFileAndMDAFormat.length() + "\n";
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
