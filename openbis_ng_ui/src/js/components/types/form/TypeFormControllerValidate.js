import PageControllerValidate from '@src/js/components/common/page/PageConrollerValidate.js'
import TypeFormControllerStrategies from '@src/js/components/types/form/TypeFormControllerStrategies.js'
import TypeFormSelectionType from '@src/js/components/types/form/TypeFormSelectionType.js'
import messages from '@src/js/common/messages.js'

export default class TypeFormControllerValidate extends PageControllerValidate {
  validate(validator) {
    const { type, properties } = this.context.getState()

    const newType = this._validateType(validator, type)
    const newProperties = this._validateProperties(validator, properties)

    return {
      type: newType,
      properties: newProperties
    }
  }

  async select(firstError) {
    const { type, properties } = this.context.getState()

    if (firstError.object === type) {
      await this.setSelection({
        type: TypeFormSelectionType.TYPE,
        params: {
          part: firstError.name
        }
      })
    } else if (properties.includes(firstError.object)) {
      await this.setSelection({
        type: TypeFormSelectionType.PROPERTY,
        params: {
          id: firstError.object.id,
          part: firstError.name
        }
      })
    }
  }

  _validateType(validator, type) {
    const strategy = this._getStrategy()
    validator.validateNotEmpty(type, 'code', messages.get(messages.CODE))
    validator.validateCode(type, 'code', messages.get(messages.CODE))
    strategy.validateTypeAttributes(validator, type)
    return validator.withErrors(type)
  }

  _validateProperties(validator, properties) {
    properties.forEach(property => {
      this._validateProperty(validator, property)
    })
    return validator.withErrors(properties)
  }

  _validateProperty(validator, property) {
    validator.validateNotEmpty(property, 'code', messages.get(messages.CODE))

    if (property.internal.value) {
      validator.validateInternalCode(
        property,
        'code',
        messages.get(messages.CODE)
      )
    } else {
      validator.validateCode(property, 'code', messages.get(messages.CODE))
    }

    validator.validateNotEmpty(property, 'label', messages.get(messages.LABEL))
    validator.validateNotEmpty(
      property,
      'description',
      messages.get(messages.DESCRIPTION)
    )
    validator.validateNotEmpty(
      property,
      'dataType',
      messages.get(messages.DATA_TYPE)
    )

    if (property.vocabulary.visible) {
      validator.validateNotEmpty(
        property,
        'vocabulary',
        messages.get(messages.VOCABULARY_TYPE)
      )
    }
    if (property.initialValueForExistingEntities.visible) {
      validator.validateNotEmpty(
        property,
        'initialValueForExistingEntities',
        messages.get(messages.INITIAL_VALUE)
      )
    }
  }

  _getStrategy() {
    const strategies = new TypeFormControllerStrategies()
    strategies.extendObjectTypeStrategy(new ObjectTypeStrategy())
    strategies.extendCollectionTypeStrategy(new CollectionTypeStrategy())
    strategies.extendDataSetTypeStrategy(new DataSetTypeStrategy())
    strategies.extendMaterialTypeStrategy(new MaterialTypeStrategy())
    return strategies.getStrategy(this.object.type)
  }
}

class ObjectTypeStrategy {
  validateTypeAttributes(validator, type) {
    validator.validateNotEmpty(
      type,
      'generatedCodePrefix',
      messages.get(messages.GENERATED_CODE_PREFIX)
    )
  }
}

class CollectionTypeStrategy {
  validateTypeAttributes() {}
}

class DataSetTypeStrategy {
  validateTypeAttributes() {}
}

class MaterialTypeStrategy {
  validateTypeAttributes() {}
}
