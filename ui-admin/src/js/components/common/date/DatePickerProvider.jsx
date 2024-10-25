import React from 'react'
import {AdapterDateFns} from '@mui/x-date-pickers/AdapterDateFns'
import {LocalizationProvider} from '@mui/x-date-pickers/LocalizationProvider';

class DatePickerProvider extends React.Component {
  render() {
    return (
      <LocalizationProvider utils={AdapterDateFns}>
        {this.props.children}
      </LocalizationProvider>
    )
  }
}

export default DatePickerProvider
