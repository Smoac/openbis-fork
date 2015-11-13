import re
def calculate():
    code =  entity.entityPE().code
    if re.search ('UC', code):
        codeNum= entity.entityPE().code.split('UC')
    elif re.search('CH',code):
        codeNum= entity.entityPE().code.split('CH')    
    
    return codeNum[1]
