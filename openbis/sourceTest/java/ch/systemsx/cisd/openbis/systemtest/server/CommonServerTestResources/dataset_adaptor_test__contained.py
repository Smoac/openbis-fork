def validate(dataset, isNew):
    try:
        assertEntities(dataset.contained(), ["20110509092359990-11", "20110509092359990-12"], 'contained')
        assertEntities(dataset.containedOfType("HCS_IMAGE"), ["20110509092359990-11", "20110509092359990-12"], 'contained of type HCS_IMAGE')
        assertEntities(dataset.containedOfType('NOT_EXISTING_TYPE'), [], 'contained of type NOT_EXISTING_TYPE')
    except Exception, ex:
        return ex.args[0]
