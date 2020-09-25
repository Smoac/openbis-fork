import VocabularyFormComponentTest from '@srcTest/js/components/types/form/VocabularyFormComponentTest.js'
import fixture from '@srcTest/js/common/fixture.js'

let common = null

beforeEach(() => {
  common = new VocabularyFormComponentTest()
  common.beforeEach()
})

describe(VocabularyFormComponentTest.SUITE, () => {
  test('change term', testChangeTerm)
})

async function testChangeTerm() {
  const form = await common.mountExisting(fixture.TEST_VOCABULARY_DTO)

  form.getButtons().getEdit().click()
  await form.update()

  form.getGrid().getRows()[1].click()
  await form.update()

  form.expectJSON({
    grid: {
      rows: [
        { values: { 'label.value': fixture.TEST_TERM_1_DTO.getLabel() } },
        { values: { 'label.value': fixture.TEST_TERM_2_DTO.getLabel() } },
        { values: { 'label.value': fixture.TEST_TERM_3_DTO.getLabel() } },
        { values: { 'label.value': fixture.TEST_TERM_4_DTO.getLabel() } },
        { values: { 'label.value': fixture.TEST_TERM_5_DTO.getLabel() } },
        { values: { 'label.value': fixture.TEST_TERM_6_DTO.getLabel() } }
      ]
    },
    parameters: {
      term: {
        title: 'Term',
        label: {
          label: 'Label',
          value: fixture.TEST_TERM_2_DTO.getLabel(),
          enabled: true,
          mode: 'edit'
        }
      }
    },
    buttons: {
      message: null
    }
  })

  form.getParameters().getTerm().getLabel().change('New Label')
  await form.update()

  form.expectJSON({
    grid: {
      rows: [
        { values: { 'label.value': fixture.TEST_TERM_1_DTO.getLabel() } },
        { values: { 'label.value': 'New Label' } },
        { values: { 'label.value': fixture.TEST_TERM_3_DTO.getLabel() } },
        { values: { 'label.value': fixture.TEST_TERM_4_DTO.getLabel() } },
        { values: { 'label.value': fixture.TEST_TERM_5_DTO.getLabel() } },
        { values: { 'label.value': fixture.TEST_TERM_6_DTO.getLabel() } }
      ]
    },
    parameters: {
      term: {
        title: 'Term',
        label: {
          label: 'Label',
          value: 'New Label',
          enabled: true,
          mode: 'edit'
        }
      }
    },
    buttons: {
      message: {
        text: 'You have unsaved changes.',
        type: 'warning'
      }
    }
  })
}
