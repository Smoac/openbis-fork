/*
 *  Copyright ETH 2023 Zürich, Scientific IT Services
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

import React from 'react'
import { withStyles } from '@material-ui/core/styles'
import messages from '@src/js/common/messages.js'
import Button from '@src/js/components/common/form/Button.jsx'
import ViewComfyIcon from '@material-ui/icons/ViewComfy'
import ViewListIcon from '@material-ui/icons/ViewList'
import PublishIcon from '@material-ui/icons/Publish'
import SettingsIcon from '@material-ui/icons/Settings'
import SearchIcon from '@material-ui/icons/Search'
import InfoIcon from '@material-ui/icons/InfoOutlined'
import CreateNewFolderIcon from '@material-ui/icons/CreateNewFolderOutlined'
import autoBind from 'auto-bind'
import { ToggleButton } from '@material-ui/lab'

const styles = () => ({})

class Toolbar extends React.Component {

  constructor(props, context) {
    super(props, context)
    autoBind(this)
  }

  render() {
    const {
      viewType,
      onViewTypeChange,
      classes,
      showInfo,
      onShowInfoChange
    } = this.props
    return (
      <>
        <ToggleButton styles={{ root: classes.button }} selected={showInfo} onChange={onShowInfoChange} value='Info' aria-label='Info' size='small'>
          <InfoIcon />
        </ToggleButton>
        <Button styles={{ root: classes.button }} label='New folder' startIcon={<CreateNewFolderIcon />} />
        <Button styles={{ root: classes.button }} label='Search' startIcon={<SearchIcon />} />
        {viewType === 'list' && <Button styles={{ root: classes.button }} label={<ViewComfyIcon />} onClick={() => onViewTypeChange('grid')} />}
        {viewType === 'grid' && <Button styles={{ root: classes.button }} label={<ViewListIcon />} onClick={() => onViewTypeChange('list')} />}
        <Button styles={{ root: classes.button }} label={<SettingsIcon />} />
        <Button styles={{ root: classes.button }} label={messages.get(messages.UPLOAD)} startIcon={<PublishIcon />} />
      </>
    )
  }
}

export default withStyles(styles)(Toolbar)
