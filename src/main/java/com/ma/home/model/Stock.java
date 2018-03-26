package com.ma.home.model;


import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;


public class Stock {
    private DateTime date;
    private float open;
    private float high;
    private float low;
    private float close;
    private double volume;
    private List<Stock> stockElementList = null;

    public Stock()
    {
        stockElementList = new ArrayList<>();
    }

    public Stock(DateTime date, float open, float high, float low, float close, double volume) {
        this.date = date;
        this.open = open;
        this.high = high;
        this.low = low;
        this.close = close;
        this.volume = volume;
    }

    public List<Stock> getStockElementList() {
        return stockElementList;
    }

    public void setStockElementList(List<Stock> stockElementList) {
        this.stockElementList = stockElementList;
    }


    public DateTime getDate() {
        return date;
    }

    public void setDate(DateTime date) {
        this.date = date;
    }

    public float getOpen() {
        return open;
    }

    public void setOpen(float open) {
        this.open = open;
    }

    public float getHigh() {
        return high;
    }

    public void setHigh(float high) {
        this.high = high;
    }

    public float getLow() {
        return low;
    }

    public void setLow(float low) {
        this.low = low;
    }

    public float getClose() {
        return close;
    }

    public void setClose(float close) {
        this.close = close;
    }

    public double getVolume() {
        return volume;
    }

    public void setVolume(double volume) {
        this.volume = volume;
    }

    @Override
    public String toString() {
        return "StockElement{" +
                "Date='" + date + '\'' +
                ", Open=" + open +
                ", High=" + high +
                ", Low=" + low +
                ", Close=" + close +
                '}';
    }
}