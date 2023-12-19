import React from "react";
import constants from "@src/js/components/database/imaging/constants.js";
const BlankImage = ({className}) => {
    return(
        <img className={className}
             src={constants.BLANK_IMG_SRC}
             alt={'Blank New Image'}/>
    )
}

export default BlankImage;