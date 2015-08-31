package com.example.notandi.calculatorapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.os.PersistableBundle;
import android.os.Vibrator;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

public class MainActivity extends AppCompatActivity {

    private TextView m_display;
    private Vibrator m_vibrator;
    private Boolean m_use_vibrator = false;
    SharedPreferences m_sp;
    static final String SCREEN_TEXT = "screenText";
    List tokens, operands, numbers;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        m_display = (TextView) findViewById(R.id.display);
        Typeface digitalFont = Typeface.createFromAsset(getAssets(), "Fonts/digital-7 (mono).ttf");
        m_display.setTypeface(digitalFont);
        m_vibrator = (Vibrator) getApplicationContext().getSystemService(Context.VIBRATOR_SERVICE);
        m_sp = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        m_use_vibrator = m_sp.getBoolean("vibrate", false);
        numbers = new ArrayList();
        operands = new ArrayList();
        tokens = new ArrayList();
        m_display.setText("0");

    }

    @Override
    public void onSaveInstanceState(Bundle savedInstance){
        savedInstance.putString(SCREEN_TEXT, m_display.getText().toString());
        super.onSaveInstanceState(savedInstance);
    }


    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if(savedInstanceState != null){
            m_display.setText(savedInstanceState.getString(SCREEN_TEXT));
        }
        else{
            m_display.setText("0");
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        m_use_vibrator = m_sp.getBoolean("vibrate", false);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();

        //Remembers what colors were used last
        getColors();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, CalcPreferenceActivity.class);
            startActivity( intent );
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    //Gets the color that was chosen from shared preferences
    public void getColors(){
        SharedPreferences shareprefs = PreferenceManager.getDefaultSharedPreferences(this);
        String color = shareprefs.getString("colors", "yellow");
        changeColor(color);
    }

    //Changes the font color to the color chosen
    public void changeColor(String color){
        if (color.equals("Green")){
            m_display.setTextColor(getResources().getColor(R.color.green));
        }
        else if (color.equals("Blue")){
            m_display.setTextColor(getResources().getColor(R.color.blue));
        }
        else if (color.equals("Purple")){
            m_display.setTextColor(getResources().getColor(R.color.purple));
        }
        else if (color.equals("Red")){
            m_display.setTextColor(getResources().getColor(R.color.red));
        }
        else if (color.equals("Orange")){
            m_display.setTextColor(getResources().getColor(R.color.orange));
        }
    }

    //What button was pressed
    public void buttonPressed( View view){
        Button buttonView = (Button) view;
        String text =  m_display.getText().toString();

        switch ( view.getId()){
            case R.id.button1:
            case R.id.button2:
            case R.id.button3:
            case R.id.button4:
            case R.id.button5:
            case R.id.button6:
            case R.id.button7:
            case R.id.button8:
            case R.id.button9:
            case R.id.button0:
                if(text.equals("0")){
                    m_display.setText("");
                }
                m_display.append( buttonView.getText() );
                break;
            case R.id.buttonPlus:
                text = m_display.getText().toString();
                Toast.makeText(getApplicationContext(),text,Toast.LENGTH_LONG).show();
                if( !(text.endsWith("+") || text.endsWith("-"))){

                    m_display.append( buttonView.getText());
                }
                break;
            case R.id.buttonMinus:
                text = m_display.getText().toString();
                Toast.makeText(getApplicationContext(),text,Toast.LENGTH_LONG).show();
                if( !(text.endsWith("+") || text.endsWith("-"))){
                    m_display.append( buttonView.getText());
                }
                break;
            case R.id.buttonBack:
                if(!text.isEmpty()) {
                    String temp = m_display.getText().toString();
                    m_display.setText(temp.substring(0, temp.length() - 1));
                }
                break;
            case R.id.buttonClear:
                m_display.setText("");
                numbers.clear();
                operands.clear();
                tokens.clear();
                break;
            case R.id.buttonEquals:
                String result = evaluateExpression(m_display.getText().toString());
                m_display.setText(result);
        }

        if (m_use_vibrator) {
            m_vibrator.vibrate( 500 );
            Toast.makeText( getApplicationContext(), "Vibrating...", Toast.LENGTH_LONG).show();
        }

    }

    public List combineIfNegative(List tokens){
        List result = new ArrayList();
        result.add(tokens.get(0).toString() + tokens.get(1).toString());
        tokens.remove(0);
        tokens.remove(0);
        for(int i = 0; i < tokens.size(); i++){
            result.add(tokens.get(i));
        }
        return result;
    }

    String evaluateExpression( String expr) {

        int zero = 0;
        BigInteger result = BigInteger.ZERO;
        StringTokenizer st = new StringTokenizer( expr, "+//-", true);

        //put all tokens into list
        while (st.hasMoreElements()){

            tokens.add(st.nextToken());
        }

        //If there is no input we return the empty string
        if(tokens.isEmpty()){
            return "";
        }

        //If there is only one token
        if(tokens.size() == 1){
            String temp_token = tokens.get(0).toString();

            //If the user has entered only one minus/plus sign, we display a zero
            if(temp_token.equals("-") || temp_token.equals("+")){
                result = result.add(BigInteger.valueOf(Long.valueOf(zero)));
                return result.toString();
            }
            else{
                //else  we display the one number entered
                return temp_token;
            }
        }

        //If there are two tokens entered
        if (tokens.size() > 2){

            //And the first token entered is a minus
            if(tokens.get(0).toString().equals("-")){

                //We combine those two tokens in to one negative number
                tokens = combineIfNegative(tokens);
            }

            //And display the combined token
            if(tokens.size() == 1){

                return tokens.get(0).toString();
            }
        }

        //We split the tokens into lists of operators and numbers
        for (int k = 0; k < tokens.size(); k++){
            if(tokens.get(k).equals("-") || tokens.get(k).equals("+")){
                operands.add(tokens.get(k));
            }
            else{
                numbers.add(tokens.get(k));
            }
        }

        //Get the first number into the result variable
        result = result.add(BigInteger.valueOf(Long.parseLong(numbers.get(0).toString())));

        //Remove the first number from numbers list
        numbers.remove(0);

        //Calculate accordingly into the result variable
        for (int j = 0; j < numbers.size(); j++){
            if(operands.get(j).toString().equals("-")){
                result = result.subtract(BigInteger.valueOf(Long.parseLong(numbers.get(j).toString())));
            }
            else if(operands.get(j).toString().equals("+")){
                result = result.add(BigInteger.valueOf(Long.parseLong(numbers.get(j).toString())));
            }
        }

        //Finally we clear all lists
        numbers.clear();
        operands.clear();
        tokens.clear();

        return result.toString();
    }
}
