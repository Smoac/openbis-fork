import VocabularyFormComponentTest from '@srcTest/js/components/types/form/VocabularyFormComponentTest.js'

let common = null

beforeEach(() => {
  common = new VocabularyFormComponentTest()
  common.beforeEach()
})

describe(VocabularyFormComponentTest.SUITE, () => {
  test('filter', testFilter)
})

async function testFilter() {
  const form = await common.mountNew()

  const labels = [
    'some 1',
    'SOME 2',
    'Some 3',
    'another 1',
    'ANOTHER 2',
    'Another 3'
  ]

  for (let i = 0; i < labels.length; i++) {
    form.getButtons().getAddTerm().click()
    await form.update()
    form.getParameters().getTerm().getLabel().change(labels[i])
    await form.update()
  }

  form.getGrid().getColumns()[1].getFilter().change('some')
  await form.update()

  form.expectJSON({
    grid: {
      columns: [
        { name: 'code', filter: { value: null } },
        { name: 'label', filter: { value: 'some' } },
        { name: 'description', filter: { value: null } },
        { name: 'official', filter: { value: null } }
      ],
      rows: [
        { values: { label: 'some 1' } },
        { values: { label: 'SOME 2' } },
        { values: { label: 'Some 3' } }
      ],
      paging: {
        range: '1-3 of 3'
      }
    }
  })

  form.getGrid().getColumns()[1].getFilter().change('1')
  await form.update()

  form.expectJSON({
    grid: {
      columns: [
        { name: 'code', filter: { value: null } },
        { name: 'label', filter: { value: '1' } },
        { name: 'description', filter: { value: null } },
        { name: 'official', filter: { value: null } }
      ],
      rows: [
        { values: { label: 'some 1' } },
        { values: { label: 'another 1' } }
      ],
      paging: {
        range: '1-2 of 2'
      }
    }
  })
}
