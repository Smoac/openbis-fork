import React from "react";

const DefaultMetadataField = ({keyProp, valueProp}) => {
    return (
        <p>
            <strong>{keyProp}:</strong>{JSON.stringify(valueProp)}
        </p>
    )
}

export default DefaultMetadataField;