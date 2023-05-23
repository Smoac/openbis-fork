import React from 'react'
import autoBind from 'auto-bind'
import { withStyles } from '@material-ui/core/styles'
import Space from '@src/js/components/workshop/Space.jsx'

const styles = theme => ({
  container: {
    padding: theme.spacing(2)
  },
  selected: {
    color: 'red'
  }
})

class Workshop extends React.PureComponent {
  constructor(props) {
    super(props)
    autoBind(this)
    this.state = {
      spaces: [
        { id: 1, code: 'TEST_CODE', description: 'Test description' },
        { id: 2, code: 'TEST_CODE_2', description: 'Test description 2' },
        { id: 3, code: 'TEST_CODE_3', description: 'Test description 3' },
        { id: 4, code: 'TEST_CODE_4', description: 'Test description 4' }
      ],
      selectedId: null
    }
  }

  handleClick(selectedId) {
    this.setState({
      selectedId
    })
  }

  handleChange(event) {
    const { spaces, selectedId } = this.state

    const fieldName = event.target.name
    const fieldValue = event.target.value

    const selectedIndex = spaces.findIndex(s => s.id === selectedId)
    const selectedSpace = spaces[selectedIndex]

    const newSpaces = [...spaces] // const newSpaces = []; newSpace.pushAll(spaces)
    const newSpace = {
      // newSpace = {}; newSpace.setAll(selectedSpace); newSpace[fieldName] = fieldValue
      ...selectedSpace,
      [fieldName]: fieldValue
    }
    newSpaces[selectedIndex] = newSpace

    this.setState({
      spaces: newSpaces
    })
  }

  render() {
    const { classes } = this.props
    const { spaces, selectedId } = this.state

    const selectedSpace = spaces.find(s => s.id === selectedId)

    return (
      <div className={classes.container}>
        <div>
          List:
          {spaces.map(space => (
            <div
              key={space.id}
              className={space.id === selectedId ? classes.selected : ''}
              onClick={() => this.handleClick(space.id)}
            >
              <Space space={space} />
            </div>
          ))}
        </div>
        {selectedId !== null && (
          <div>
            Form:
            <div>
              Code:
              <input
                name='code'
                value={selectedSpace.code}
                onChange={this.handleChange}
              />
            </div>
            <div>
              Description:
              <input
                name='description'
                value={selectedSpace.description}
                onChange={this.handleChange}
              />
            </div>
          </div>
        )}
      </div>
    )
  }
}

export default withStyles(styles)(Workshop)
