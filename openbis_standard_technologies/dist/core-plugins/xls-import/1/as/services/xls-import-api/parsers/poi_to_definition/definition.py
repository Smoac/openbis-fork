class Definition(object):
    '''
        Used to hold values for object(Vocabulary, SampleType etc.) creation.
    '''

    def __init__(self):
        self.type = None
        self.attributes = {}
        self.properties = []

    def __str__(self):
        return "\n".join([
            "Definition type:",
            str(self.type),
            "Attributes:",
            str(self.attributes),
            "Properties:",
            str(self.properties),
            "==================" * 3, ''])

    __repr__ = __str__