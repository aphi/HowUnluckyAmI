package nz.aphi.howunlucky;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.RadioGroup;

public class SetupActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);
    }


    public void buttonStart(View view) {
        Intent intent = new Intent(this, RollerActivity.class);
//        EditText editText = (EditText) findViewById(R.id.editText);
//        String message = editText.getText().toString();

        // Get dice configuration from radio button
        RadioGroup rdc = (RadioGroup) findViewById(R.id.radio_dice_configuration);
        int selectedId = rdc.getCheckedRadioButtonId();

        int num_dice = 1;
        int dice_max = 6;
        switch(selectedId) {
            case R.id.select_1d6:
                num_dice = 1;
                dice_max = 6;
                break;
            case R.id.select_2d6:
                num_dice = 2;
                dice_max = 6;
                break;
        }
        intent.putExtra("num_dice", num_dice);
        intent.putExtra("dice_max", dice_max);

        // Get rolling method from radio button
        RadioGroup rrm = (RadioGroup) findViewById(R.id.radio_rolling_method);
        selectedId = rrm.getCheckedRadioButtonId();

        boolean approller = true;
        switch(selectedId) {
            case R.id.select_approller:
                approller = true;
                break;
            case R.id.select_manualroller:
                approller = false;
                break;
        }
        intent.putExtra("approller", approller);

        startActivity(intent);
    }
}
