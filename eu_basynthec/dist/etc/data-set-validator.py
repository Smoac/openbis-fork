import os
import re
 
def validate_data_set_file(file):
    errors = []
    if False:
        errors.append(createFileValidationError(file.getName() + " is not a valid data set. The file name cannot contain 'invalid'."))
 
    return errors
