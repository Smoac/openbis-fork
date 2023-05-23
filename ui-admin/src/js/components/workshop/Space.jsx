import React from 'react'

class Space extends React.PureComponent {
  render() {
    console.log('Space ' + this.props.space.code)
    return <div>{this.props.space.code}</div>
  }
}

export default Space
