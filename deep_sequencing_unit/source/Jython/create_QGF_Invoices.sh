LIB=lib/
CLASSPATH=lib/
cd lib
java -cp dss_client.jar:jython.jar:stream-supporting-httpinvoker.jar:spring.jar:commons-logging.jar:commons-httpclient.jar:commons-codec.jar:commons-lang.jar:log5j.jar:poi-3.7-20101029.jar:poi-ooxml-3.7-20101029.jar:poi-ooxml-schemas-3.7-20101029.jar org.python.util.jython ../create_QGF_Invoices.py
