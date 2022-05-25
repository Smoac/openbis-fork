package ch.systemsx.cisd.openbis.generic.server.security;

public class EmbeddedKeycloakServerProperties
{
    private String contextPath = "/auth";

    private String realmImportFile = "baeldung-realm.json";

    private AdminUser adminUser = new AdminUser();

    public String getContextPath()
    {
        return contextPath;
    }

    public void setContextPath(final String contextPath)
    {
        this.contextPath = contextPath;
    }

    public String getRealmImportFile()
    {
        return realmImportFile;
    }

    public void setRealmImportFile(final String realmImportFile)
    {
        this.realmImportFile = realmImportFile;
    }

    public AdminUser getAdminUser()
    {
        return adminUser;
    }

    public void setAdminUser(final AdminUser adminUser)
    {
        this.adminUser = adminUser;
    }

    public static class AdminUser
    {
        private String username = "admin";

        private String password = "admin";

        public String getUsername()
        {
            return username;
        }

        public void setUsername(final String username)
        {
            this.username = username;
        }

        public String getPassword()
        {
            return password;
        }

        public void setPassword(final String password)
        {
            this.password = password;
        }
    }
}