package com.sam_chordas.android.stockhawk.exceptions;

/**
 * Created by nikhil.p on 26/04/16.
 */
public class InvalidStockException extends Exception{
    public InvalidStockException() {
        super("Stock doesn't exist");
    }
}
