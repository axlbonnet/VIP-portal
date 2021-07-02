package fr.insalyon.creatis.vip.application.client.view.launch.applicationLayout;

import com.smartgwt.client.widgets.form.fields.CheckboxItem;
import com.smartgwt.client.widgets.form.fields.FormItem;
import fr.insalyon.creatis.vip.application.client.view.launch.boutiquesParsing.BoutiquesInputFlag;

/**
 * Representation of a checkbox input (Flag input type)
 *
 * @author Guillaume Vanel
 * @version %I%, %G%
 */
public class FlagInputLayout extends InputLayout{
    /**
     * Initialises graphical labels and checkbox
     *
     * @param input        BoutiquesInputFlag to be represented
     * @param parentLayout LaunchFormLayout containing this
     */
    public FlagInputLayout(BoutiquesInputFlag input, LaunchFormLayout parentLayout) {
        super(input, parentLayout);
    }

    /**
     * @return FormItem representing a checkbox for this input
     * @see InputLayout#getFormItem()
     */
    @Override
    protected FormItem getFormItem() {
        CheckboxItem inputItem = new CheckboxItem();
        inputItem.setWidth(400);
        inputItem.setShowTitle(false);
        inputItem.setValue(this.getDefaultValue());
        return inputItem;
    }

    /**
     * @return boolean: true if main input box is unchecked
     * @see InputLayout#isMasterEmpty()
     */
    @Override
    public boolean isMasterEmpty(){
        Object masterValue = ValueList.formValue(this.masterForm);
        assert masterValue instanceof Boolean;
        return  !((boolean) masterValue);
    }
}