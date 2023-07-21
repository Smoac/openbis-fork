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

import React, { useRef } from "react";
import { withStyles } from "@material-ui/core/styles";
import Button from "@material-ui/core/Button";

const styles = () => ({
  invisible: {
    display: 'none'
  }
})

class UploadButton extends React.Component {

  render () {
    const { children, classes, size, variant, color, onClick } = this.props;
    const fileInputRef = React.createRef();

    return (
      <div>
        {/* Hidden file input */}
        <input
          type="file"
          ref={fileInputRef}
          className={classes.invisible}
          onChange={onClick}
        />

        {/* Button to trigger the file input */}
        <Button
          classes={{ root: classes.button }}
          color={color}
          size={size}
          variant={variant}
          onClick={() => fileInputRef.current.click()}
        >
          {children}
        </Button>
      </div>
    );
  }
}

export default withStyles(styles)(UploadButton)