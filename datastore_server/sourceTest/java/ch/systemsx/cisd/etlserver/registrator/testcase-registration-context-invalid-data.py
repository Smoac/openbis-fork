execfile("sourceTest/java/ch/systemsx/cisd/etlserver/registrator/simple-testcase.py")

class AClass:
    """A simple example class"""
    i = 12345
    def f(self):
        return 'hello world'

x = AClass()

transaction.getRegistrationContext().put("nonSerializableObject",x)

