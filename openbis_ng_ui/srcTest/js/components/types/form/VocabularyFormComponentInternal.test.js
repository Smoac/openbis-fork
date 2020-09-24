import VocabularyFormComponentTest from '@srcTest/js/components/types/form/VocabularyFormComponentTest.js'
import objectTypes from '@src/js/common/consts/objectType.js'
import openbis from '@srcTest/js/services/openbis.js'
import fixture from '@srcTest/js/common/fixture.js'

let common = null

beforeEach(() => {
  common = new VocabularyFormComponentTest()
  common.beforeEach()
})

describe('VocabularyFormComponentInternal', () => {
  test('internal', testInternal)
})

async function testInternal() {
  await doTestInternal(false, fixture.TEST_USER_DTO, fixture.TEST_USER_DTO)
  await doTestInternal(true, fixture.TEST_USER_DTO, fixture.TEST_USER_DTO)
  await doTestInternal(false, fixture.TEST_USER_DTO, fixture.SYSTEM_USER_DTO)
  await doTestInternal(true, fixture.TEST_USER_DTO, fixture.SYSTEM_USER_DTO)
  await doTestInternal(false, fixture.SYSTEM_USER_DTO, fixture.TEST_USER_DTO)
  await doTestInternal(true, fixture.SYSTEM_USER_DTO, fixture.TEST_USER_DTO)
  await doTestInternal(false, fixture.SYSTEM_USER_DTO, fixture.SYSTEM_USER_DTO)
  await doTestInternal(true, fixture.SYSTEM_USER_DTO, fixture.SYSTEM_USER_DTO)
}

async function doTestInternal(
  vocabularyInternal,
  vocabularyRegistrator,
  termRegistrator
) {
  const isSystemInternalVocabulary =
    vocabularyInternal &&
    vocabularyRegistrator.userId === fixture.SYSTEM_USER_DTO.userId

  const isSystemInternalTerm =
    vocabularyInternal &&
    termRegistrator.userId === fixture.SYSTEM_USER_DTO.userId

  const term = new openbis.VocabularyTerm()
  term.setCode('TEST_TERM')
  term.setDescription('Test Term Description')
  term.setLabel('Test Term Label')
  term.setRegistrator(termRegistrator)
  term.setOfficial(true)

  const vocabulary = new openbis.Vocabulary()
  vocabulary.setCode('TEST_VOCABULARY')
  vocabulary.setDescription('Test Vocabulary Description')
  vocabulary.setUrlTemplate('Test Vocabulary Url Template')
  vocabulary.setTerms([term])
  vocabulary.setManagedInternally(vocabularyInternal)
  vocabulary.setRegistrator(vocabularyRegistrator)

  common.facade.loadVocabulary.mockReturnValue(Promise.resolve(vocabulary))

  const form = await common.mount({
    id: vocabulary.getCode(),
    type: objectTypes.VOCABULARY_TYPE
  })

  form.getButtons().getEdit().click()
  await form.update()

  form.expectJSON({
    parameters: {
      vocabulary: {
        title: 'Vocabulary',
        messages: isSystemInternalVocabulary
          ? [
              {
                type: 'lock',
                text:
                  'This is a system internal vocabulary. The vocabulary parameters cannot be changed.'
              }
            ]
          : [],
        code: {
          value: vocabulary.getCode(),
          enabled: false
        },
        description: {
          value: vocabulary.getDescription(),
          enabled: !isSystemInternalVocabulary
        },
        urlTemplate: {
          value: vocabulary.getUrlTemplate(),
          enabled: !isSystemInternalVocabulary
        }
      }
    }
  })

  form.getGrid().getRows()[0].click()
  await form.update()

  form.expectJSON({
    parameters: {
      term: {
        title: 'Term',
        messages: isSystemInternalTerm
          ? [
              {
                type: 'lock',
                text:
                  'This is a system internal term. The term parameters cannot be changed. The term cannot be removed.'
              }
            ]
          : [],
        code: {
          value: term.getCode(),
          enabled: false
        },
        description: {
          value: term.getDescription(),
          enabled: !isSystemInternalTerm
        },
        label: {
          value: term.getLabel(),
          enabled: !isSystemInternalTerm
        }
      }
    },
    buttons: {
      removeTerm: {
        enabled: !isSystemInternalTerm
      }
    }
  })
}
