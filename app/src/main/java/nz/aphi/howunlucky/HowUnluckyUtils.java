package nz.aphi.howunlucky;

import android.graphics.Color;

import java.util.LinkedList;
import java.util.TreeMap;

public final class HowUnluckyUtils {
    public static int ll_count(LinkedList<Integer> ll, Integer count_ele) {
        int count = 0;
        for (int ele: ll) {
            if (ele == count_ele){
                count++;
            };
        }
        return count;
    }

    public static int luckiness(int qty, double expectation, double p_of_less, double p_of_this, double p_of_more) {
        double p;
        int luckiness;
        if (qty < expectation) {
            p = p_of_more;
            luckiness = -1;
        } else if (qty > expectation) {
            p = p_of_less;
            luckiness = 1;
        } else {
            return 0;
        }

        if (p >= 0.9)
            luckiness *= 3;
        else if (p >= 0.8)
            luckiness *= 2;
        else if (p >= 0.65)
            luckiness *= 1;
        else {
            luckiness *= 0;
        }

        return luckiness;
    }

    public static String luckiness_string(int luckiness){
        switch (luckiness) {
            case -3:
                return "Very Unlucky";
            case -2:
                return "Unlucky";
            case -1:
                return "Slightly Unlucky";
            case 1:
                return "Slightly Lucky";
            case 2:
                return "Lucky";
            case 3:
                return "Very Lucky";
            default:
                return "Fair";
        }
    }

    public static int luckiness_color(int luckiness){
        switch (luckiness) {
            case -3:
                return Color.rgb(255,179,179);
            case -2:
                return Color.rgb(255,128,128);
            case -1:
                return Color.rgb(255,0,0);
            case 1:
                return Color.rgb(179, 179, 255);
            case 2:
                return Color.rgb(128, 128, 255);
            case 3:
                return Color.rgb(0,0,255);
            default:
                return Color.rgb(0,0,0);
        }
    }


    public static TreeMap<Integer, Double> get_pmf(int num_dice, int dice_max){

        TreeMap<Integer, Double> pmf = new TreeMap<Integer, Double>();
        switch(num_dice) {
            case 1:
                for (int o = 1; o <= dice_max; o++) {
                    pmf.put(o, 1.0 / dice_max);
                }
                break;
            case 2:
                for (int o = 2; o <= 2 * dice_max; o++) {
                    pmf.put(o, (dice_max - Math.abs(o - (dice_max + 1))) / Math.pow(dice_max, 2));
                }
                break;
            default:
                throw new UnsupportedOperationException("More dice not yet supported");
        }

        return pmf;
    }
}
