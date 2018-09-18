package nz.aphi.howunlucky;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.ConstraintSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.lang.reflect.Array;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Random;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.Vector;

import org.apache.commons.math3.distribution.BinomialDistribution;

public class RollerActivity extends Activity {
    private Random rand;
    private LinkedList<Integer> hist;
    private TreeMap<Integer, Double> pmf;
    private int num_dice;
    private int dice_max;
    private boolean approller;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_roller);

        // Get the Intent that started this activity and extract the information
        Intent intent = getIntent();

        this.num_dice = intent.getIntExtra("num_dice", 1);
        this.dice_max = intent.getIntExtra("dice_max", 6);
        this.approller = intent.getBooleanExtra("approller", true);

        ConstraintLayout mainLayout = findViewById(R.id.activity_roller_constraint_layout);
        LayoutInflater layoutInflater = getLayoutInflater();
        if (this.approller) {
            layoutInflater.inflate(R.layout.roller, mainLayout, true);
        } else {
            layoutInflater.inflate(R.layout.input, mainLayout, true);


            // Generate UI Entry Buttons
            GridLayout gridLayout = (GridLayout)findViewById(R.id.input_cl);
            gridLayout.setColumnCount(3);
            gridLayout.setRowCount((int) Math.ceil(this.num_dice * this.dice_max / 3));
            int place = 0;

            GridLayout.LayoutParams params;
            Button b;

            for (int dice_value = this.num_dice; dice_value <= this.num_dice * this.dice_max; dice_value++) {
                final int dice_value_copy = dice_value;

                params = new GridLayout.LayoutParams(
                        GridLayout.spec(place / 3, GridLayout.FILL, 1), //rowspec
                        GridLayout.spec(place % 3, GridLayout.FILL, 1) // colspec
                );
                params.setMargins(0, 0, 0, 0);

                b = new Button(this);
                b.setText(String.valueOf(dice_value));
                b.setTextSize(24);
                b.setLayoutParams(params);

                b.setOnClickListener(new View.OnClickListener(){
                    @Override
                    public void onClick(View v){
                        processNumber(dice_value_copy);
                    }
                });

                gridLayout.addView(b);

                place++;
            }
        }

        ScrollView sv = findViewById(R.id.probabilities_scrollview);
        ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) sv.getLayoutParams();
        if (this.approller) {
            params.topToBottom = R.id.roller_cl;
        } else {
            params.topToBottom = R.id.input_cl;
        }
        sv.setLayoutParams(params);

        // Set dice roll button on click listener
        if (this.approller){
            final MediaPlayer mp = MediaPlayer.create(this, R.raw.dice_faster);

            Button roll_button = findViewById(R.id.button_roller);
            roll_button.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v){
                    if (mp.isPlaying()) {
                        mp.seekTo(0);
                    } else {
                        mp.start();
                    }
                    buttonRoll(v);
                }
            });
        }

        reset();
        }

    private void reset() {
        // Initialise
        this.rand = new Random(System.currentTimeMillis());
        this.hist = new LinkedList<Integer>();
        this.pmf = HowUnluckyUtils.get_pmf(this.num_dice, this.dice_max);
    }

    private void processNumber(int rollResult){
        // Update records
        this.hist.add(rollResult);

        // Update UI: Probabilities
        LinearLayout probabilitiesLayout = findViewById(R.id.probabilities_layout);
        probabilitiesLayout.removeAllViews();

        LinearLayout.LayoutParams lp_p = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);

        // Compute Probability scores for each outcome
        for(int outcome: pmf.keySet()) {
            int qty = HowUnluckyUtils.ll_count(this.hist, outcome);
            int n = this.hist.size();
            double p = this.pmf.get(outcome);

            double expectation = n * p;
            double std_dev = Math.pow(n * p * (1 - p), 0.5);
            double z_score = (qty - expectation) / std_dev;

            BinomialDistribution bd = new BinomialDistribution(n, p);
            double p_of_less = bd.cumulativeProbability(qty - 1);
            double p_of_this = bd.probability(qty);
            double p_of_more = 1.0 - (p_of_less + p_of_this);

            int luckiness_score = HowUnluckyUtils.luckiness(qty, expectation, p_of_less, p_of_this, p_of_more);
            String res = HowUnluckyUtils.luckiness_string(luckiness_score);
            int color = HowUnluckyUtils.luckiness_color(luckiness_score);

            // Update UI: Probability Entry
//            String outcomeText = String.format("%2d: %3d (%.1f) : (%.2f %.2f %.2f) : %s", outcome, qty, expectation, p_of_less, p_of_this, p_of_more, res);
            String outcomeText = String.format("%2d: %s %3d (exp %.1f)", outcome, res, qty, expectation);

            TextView probabilityElement = new TextView(this);
            probabilityElement.setText(outcomeText);
            probabilityElement.setLayoutParams(lp_p);
            probabilityElement.setTextSize(16);
            probabilityElement.setTextColor(color);
            probabilitiesLayout.addView(probabilityElement);
        }


        // Update UI: Roll Counter
        TextView roll_counter = findViewById(R.id.roll_counter);
        roll_counter.setText(String.format("%3d Rolls:", this.hist.size()));

        // Update History Bar
        LinearLayout historyLayout = findViewById(R.id.history_layout);
        TextView historyElement = new TextView(this);
        historyElement.setText(String.valueOf(rollResult));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);

        lp.setMarginEnd(20);
        historyElement.setLayoutParams(lp);
        historyElement.setTextSize(25);
        historyLayout.addView(historyElement);

        // Update history - Scroll across
        final HorizontalScrollView historyScrollView = (HorizontalScrollView) findViewById(R.id.history_scrollview);
        historyScrollView.post(new Runnable() {
            @Override
            public void run() {
                historyScrollView.setHorizontalScrollBarEnabled(false);
                historyScrollView.scrollTo(1000000,0);
                historyScrollView.setHorizontalScrollBarEnabled(true);
            }
        });
    }


//    public void numberEntry(View view){
//        this.processNumber(rollResult);
//    }

    public void buttonRoll(View view) {
        // Generate Roll
        int rollResult;
        switch (this.num_dice) {
            case 1:
                rollResult = this.rand.nextInt(this.dice_max) + 1;
                break;
            case 2:
                int d1 = this.rand.nextInt(this.dice_max) + 1;
                int d2 = this.rand.nextInt(this.dice_max) + 1;
                rollResult = d1 + d2;
                break;
            default:
                throw new UnsupportedOperationException("More dice not yet supported");
        }

        // Display Roll Result
        TextView rollResultTextView = findViewById(R.id.rollResult);
        rollResultTextView.setText(String.valueOf(rollResult));

        this.processNumber(rollResult);
    }
}
