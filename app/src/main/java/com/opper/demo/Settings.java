package com.opper.demo;

import com.omofresh.oppersdk.misc.SysUnits;

public class Settings {

    /**
     * {@link SysUnits#G} 克
     * {@link SysUnits#KG} 千克
     * {@link SysUnits#JIN} 市斤
     * {@link SysUnits#JIN_HK} 斤(香港)
     */
    public static int unit = SysUnits.JIN;

    /**
     * 小数位，0 取整
     */
    public static int decimals = 2;

    /**
     * 去皮重量
     */
    public static double tare = 0;

    /**
     * 置0偏移量
     */
    public static double offset = 0;

    /**
     * 秤台的振动幅度（取决于秤的精度）
     */
    public static int vibrateGrams = 0;

    /**
     * 秤的精度，100克
     * 取决于秤的精度
     */
    public static int e = 100;

}
