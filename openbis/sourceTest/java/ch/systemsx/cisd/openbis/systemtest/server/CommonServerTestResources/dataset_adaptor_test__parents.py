def validate(dataset, isNew):
    try:
        assertEntities(dataset.parents(), ["20081105092159111-1", "20081105092159222-2", "20081105092159333-3"], 'parents')
    except Exception, ex:
        return ex.args[0]
