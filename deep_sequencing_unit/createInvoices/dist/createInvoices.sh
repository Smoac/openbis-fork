LIB=/home/sbsuser/openbis/createInvoices/lib/
CLASSPATH=/home/sbsuser/openbis/createInvoices/lib/
cd $LIB/..
java -cp $LIB/dom4j-1.6.1.jar:$LIB/geronimo-stax-api_1.0_spec-1.0.jar:$LIB/openbis-query-api.jar:$LIB/dss_client.jar:jython.jar:$LIB/stream-supporting-httpinvoker.jar:$LIB/spring.jar:commons-logging.jar:$LIB/commons-httpclient.jar:$LIB/commons-codec.jar:$LIB/commons-lang.jar:$LIB/log5j.jar:$LIB/poi.jar:$LIB/poi-ooxml.jar:$LIB/poi-ooxml-schemas.jar org.python.util.jython create_QGF_Invoices_simplified.py "$@"