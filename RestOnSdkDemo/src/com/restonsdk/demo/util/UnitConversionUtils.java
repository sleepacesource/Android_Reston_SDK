package com.restonsdk.demo.util;

public class UnitConversionUtils {
    //由英尺和英寸组成英寸英寸的字符串
    public static String CombinedToFTIN(int ft, int in) {
        return ft + "\'" + " " + in + "\"";
    }

    public static String cmToFtIn(int cm) {
        //得到英尺的整数
        int fix = (int) (cm / 30.48);
        //得到英尺，包括小数
        double d = cm / 30.48;
        //得到小数部分
        double remainder = d - fix;
        int ft = fix;
        //将小数部分转换为英寸
        int in = (int) Math.round(remainder * 12);
        return ft + "\'" + in + "\"";
    }

    public static int cmToFtInData(int cm) {
        //得到英尺的整数
        int fix = (int) (cm / 30.48);
        //得到英尺，包括小数
        double d = cm / 30.48;
        //得到小数部分
        double remainder = d - fix;
        int ft = fix;
        //将小数部分转换为英寸
        int in = (int) Math.round(remainder * 12);
        return ft * 10 + in;
    }

    public static int FtInToCm(String ftIn) {
        String[] s = ftIn.split(" ");
        int singleQuoteIndex = s[0].indexOf("\'");
        int doubleQuoteIndex = s[1].indexOf("\"");
        int ft = Integer.parseInt(s[0].substring(0, singleQuoteIndex));
        int in = Integer.parseInt(s[1].substring(0, doubleQuoteIndex));
        int result = (int) Math.round((ft * 12 + in) * 2.54);
        return result;
    }

    //单位转换，kg到lb(英镑)
    public static int kgToLb(int kg) {
        int result = (int) Math.round(kg * 2.2);
        return result;
    }

    //单位转换lb(英镑)到kg
    public static int lbToKg(int lb) {
        int result = (int) Math.round(lb * 0.454);
        return result;
    }


    public static double setCm2Ft(int cm) {
        int ft = (int) (cm / 30.48);
        double in = cm % 30.48 / 2.54;
        return 10 * ft + in;
    }


    public static String setInch(int inch) {
        int ft = inch / 10;
        int in = inch % 10;
        return ft + "\'" + in + "\"";
    }

    public static int getCM(String str){

        int singleQuoteIndex = str.indexOf("\'");
        int doubleQuoteIndex = str.indexOf("\"");
        int ft = Integer.parseInt(str.substring(0, singleQuoteIndex));
        int in = Integer.parseInt(str.substring(singleQuoteIndex+1, doubleQuoteIndex));
        int result = (int) Math.round((ft * 12 + in) * 2.54);
        return result;
    }

}
