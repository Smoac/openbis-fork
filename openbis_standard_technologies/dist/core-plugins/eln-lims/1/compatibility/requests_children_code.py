def retrieve_childrenCode(): 

    sample= entity.samplePE()
    children_code = sample.children[0].getCode()

    return children_code
     
def calculate():
    return retrieve_childrenCode()
