const copyanddisable = async (context, args, uiField) => {
  console.log("copyanddisable called");
  if (args.length > 0) {
    let checkboxVal = context.userForm.controls[`${uiField.id}`].value;
    for (const arg of args) {
      let controlsArr = arg.split("=");
      if (controlsArr.length > 1) {
        let control1 = controlsArr[0],
          control2 = controlsArr[1];
        let filteredList1 = context.uiFields.filter(
          (uiField) => uiField.id == control1
        );
        let filteredList2 = context.uiFields.filter(
          (uiField) => uiField.id == control2
        );
        if (filteredList1.length > 0 && filteredList2.length > 0) {
          let uiField1 = filteredList1[0];
          let uiField2 = filteredList2[0];
          if (
            context.isControlInMultiLang(uiField1) &&
            context.isControlInMultiLang(uiField2)
          ) {
            context.dataCaptureLanguages.forEach((language, i) => {
              const fromId = uiField1.id + "_" + language;
              const fromFieldValue = context.userForm.controls[fromId].value;
              const toId = uiField2.id + "_" + language;
              if (checkboxVal == true) {
                context.userForm.controls[toId].setValue(fromFieldValue);
                context.userForm.controls[toId].disable();
              } else {
                context.userForm.controls[toId].enable();
              }
            });
          } else {
            if (
              !context.isControlInMultiLang(uiField1) &&
              !context.isControlInMultiLang(uiField2)
            ) {
              const fromFieldValue =
                context.userForm.controls[uiField1.id].value;
              if (checkboxVal == true) {
                context.userForm.controls[uiField2.id].setValue(fromFieldValue);
                context.userForm.controls[uiField2.id].disable();
                if (
                  uiField2.controlType == "dropdown" ||
                  uiField2.controlType == "button"
                ) {
                  context.selectOptionsDataArray[`${uiField2.id}`] =
                    context.selectOptionsDataArray[`${uiField1.id}`];
                  context.searchInDropdown(uiField2.id);
                  if (context.isThisFieldInLocationHeirarchies(uiField2.id)) {
                    context.resetLocationFields(uiField2.id);
                  }
                }
              } else {
                context.userForm.controls[uiField2.id].enable();
              }
            }
          }
        }
      }
    }
  } else {
    console.log(
      "Invalid number of argumments sent to 'copy&disable' changeAction."
    );
  }
};
export default copyanddisable;
