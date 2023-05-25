import React from 'react'
import { withStyles } from '@material-ui/core/styles'
import Container from '@src/js/components/common/form/Container.jsx'
import Button from '@src/js/components/common/form/Button.jsx'
import ViewComfyIcon from '@material-ui/icons/ViewComfy'
import ViewListIcon from '@material-ui/icons/ViewList'

const styles = theme => ({
  containerDefault: {
    padding: `${theme.spacing(1)}px ${theme.spacing(2)}px`
  },
  containerSquare: {
    padding: `${theme.spacing(2)}px ${theme.spacing(2)}px`
  }
})

class Toolbar extends React.Component {
  render() {
    const { viewType, onViewTypeChange } = this.props
    return (
      <Container>
        {viewType === 'list' && <Button label={<ViewComfyIcon/>} onClick={() => onViewTypeChange('grid')} />}
        {viewType === 'grid' && <Button label={<ViewListIcon/>} onClick={() => onViewTypeChange('list')} />}
      </Container>
    )
  }
}

export default withStyles(styles)(Toolbar)
