package com.prostoradio.app;

/**
 * Created by timonsk on 16.05.14.
 */
public class CONSTANTS {
    public static final String ProstoFM_Kiev    = "http://62.80.190.246:8000/ProstoRadiO128";
    public static final String ProstoFM_Odessa    = "http://62.80.190.246:8000/ProstoRadiO128";
    public static final String ProstoFM_Rock       = "http://62.80.190.246:8000/PRock128";


    public static final String ProstoFM_Kiev_lable  = "PROSTO FM Kiev (128kb/s)";
    public static final String ProstoFM_Odessa_lable  = "PROSTO FM Odessa (128kb/s)";
    public static final String ProstoFM_Rock_lable      = "PROSTO FM Rock (128kb/s)";
    public static final StreamStation[] STATIONS =
            {
                    new StreamStation(ProstoFM_Kiev_lable, ProstoFM_Kiev),
                    new StreamStation(ProstoFM_Odessa_lable, ProstoFM_Odessa),
                    new StreamStation(ProstoFM_Rock_lable, ProstoFM_Rock)
            };
    public static final StreamStation DEFAULT_STREAM_STATION = STATIONS[0];
}
