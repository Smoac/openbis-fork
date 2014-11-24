import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;


//  STEP By Step:
//  1. Open the file maker file with file maker pro
//  2. Go to "File -> Sharing -> ODBC/JDBC" and turn sharing on
//  3. You can go to see the names of the tables and field to "File -> Manage -> Database"
//  Without closing file maker pro you can run this script, modify it as needed
//  More info at http://www.filemaker.com/support/product/docs/fmp/fm11_odbc_jdbc_guide_en.pdf

public class FileMakerMain
{
    // NOTE: FileMaker is so dump that you can't get the fileName and the fileData on the same query, the file data will return null, so we make a second call for this
    public static final String GET_DOCUMENTS = "SELECT CAST(file AS VARCHAR(1000)) AS fileName, info, serial FROM documents";
    public static final String GET_DOCUMENT_DATA = "SELECT GetAs(file, 'FILE') AS fileData FROM documents WHERE serial = ?";
    
    static class Document {
        String fileName;
        String info;
        String serial;
        
        public String toString()
        {
            return fileName + " " + info;
        }
    }
    
    public static List<Document> getDocuments(Connection connection) throws SQLException {
        List<Document> documents = new ArrayList<Document>();
        PreparedStatement preparedStatement = connection.prepareStatement(GET_DOCUMENTS);
        ResultSet result = preparedStatement.executeQuery();
        
        while(result.next()) {
            Document document = new Document();
            document.fileName = result.getString("fileName");
            document.info = result.getString("info");
            document.serial = result.getString("serial");
            documents.add(document);
        }
        result.close();
        preparedStatement.close();
        return documents;
    }
    
    public static byte[] getFile(Connection connection, Document document) throws SQLException {
        byte[] bytes = {};
        PreparedStatement preparedStatement = connection.prepareStatement(GET_DOCUMENT_DATA);
        preparedStatement.setString(1, document.serial);
        ResultSet result = preparedStatement.executeQuery();
        if(result.next()) {
            byte[] value = result.getBytes("fileData");
            if(value != null) {
                bytes = value;
            }
        }
        result.close();
        preparedStatement.close();
        return bytes;
    }
    
    public static void main(String[] args) throws Exception {
        Class.forName("com.filemaker.jdbc.Driver").newInstance();
        Connection connection = DriverManager.getConnection("jdbc:filemaker://127.0.0.1/BOXIT_documents_Peter.fmp12","designer", "seattle");
        
        for(Document document: getDocuments(connection)) {
            byte[] bytes = getFile(connection, document);
            System.out.println(document + " " + bytes.length);
        }
        connection.close();
    }
}
