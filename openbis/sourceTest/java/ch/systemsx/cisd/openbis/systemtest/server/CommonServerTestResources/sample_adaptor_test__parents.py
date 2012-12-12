def validate(sample, isNew):
    try:
        assertEntities(sample.parents(), ['A02','3V-125'], 'parents')
    except Exception, ex:
        return ex.args[0]
