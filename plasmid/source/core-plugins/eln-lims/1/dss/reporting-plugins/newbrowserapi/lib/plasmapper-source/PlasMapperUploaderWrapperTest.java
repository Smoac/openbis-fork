package ch.ethz.ssdm.eln;
import ch.systemsx.cisd.common.ssl.SslCertificateHelper;

public class PlasMapperUploaderWrapperTest
{
    public static final void main(String[] args) {
        SslCertificateHelper.trustAnyCertificate("https://localhost:8443/PlasMapper");
        
        PlasMapperUploaderWrapper.uploadAndCopyGeneratedFile(
                "https://localhost:8443/PlasMapper",
                "/Users/juanf/Documents/installations/S200/servers/openBIS-server/jetty/webapps",
                "/Users/juanf/Downloads/FRP1819.fasta",
                "/Users/juanf/Downloads/destination/FRP1819.svg"
        );
    }
}
