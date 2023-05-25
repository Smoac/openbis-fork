import React from 'react'
import { withStyles } from '@material-ui/core/styles'
import messages from '@src/js/common/messages.js'
import Container from '@src/js/components/common/form/Container.jsx'
import Button from '@src/js/components/common/form/Button.jsx'
import ViewComfyIcon from '@material-ui/icons/ViewComfy'
import ViewListIcon from '@material-ui/icons/ViewList'
import PublishIcon from '@material-ui/icons/Publish'
import SettingsIcon from '@material-ui/icons/Settings'
import SearchIcon from '@material-ui/icons/Search'
import InfoIcon from '@material-ui/icons/InfoOutlined'
import CreateNewFolderIcon from '@material-ui/icons/CreateNewFolderOutlined'

const styles = theme => ({
})

class Toolbar extends React.Component {
  render() {
    const { viewType, onViewTypeChange, classes } = this.props
    return (
      <Container>
        <Button styles={{ root: classes.button }} label={<CreateNewFolderIcon />} />
        <Button styles={{ root: classes.button }} label={<InfoIcon />} />
        <Button styles={{ root: classes.button }} label={<SearchIcon />} />
        {viewType === 'list' && <Button styles={{ root: classes.button }} label={<ViewComfyIcon />} onClick={() => onViewTypeChange('grid')} />}
        {viewType === 'grid' && <Button styles={{ root: classes.button }} label={<ViewListIcon />} onClick={() => onViewTypeChange('list')} />}
        <Button styles={{ root: classes.button }} label={<SettingsIcon />} />
        <Button styles={{ root: classes.button }} label={<><PublishIcon /> {messages.get(messages.UPLOAD)}</>} />
      </Container>
    )
  }
}

export default withStyles(styles)(Toolbar)
