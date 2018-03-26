package com.ma.home.service;


import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.ma.home.model.Stock;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;


public class StockService {
    private static final String strUrl = "https://www.quandl.com/api/v3/datasets/EOD/AAPL.json?api_key=s2zrK5rk1-_bu5cP6Q2d";
    private static Future<List<Stock>> stockDownloaderFuture = null;
    private Stock stock;

    public StockService() {
        stock = new Stock();
    }

    public void downloadStockContent() {
        stockDownloaderFuture = downloadStock();
    }

    public synchronized Future<List<Stock>> downloadStock() {
        ExecutorService executorService = Executors.newFixedThreadPool(2);
        return executorService.submit(() -> {
            URL url = new URL(strUrl);
            URLConnection urlConnection = url.openConnection();
            InputStreamReader inputStreamReader = new InputStreamReader(urlConnection.getInputStream());
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            String stockContent = bufferedReader.readLine();
            parseStockContents(stockContent);
            executorService.shutdownNow();
            return stock.getStockElementList();
        });
    }

    public void parseStockContents(String stockContent) {
        JsonArray data_array = new JsonParser()
                .parse(stockContent)
                .getAsJsonObject()
                .getAsJsonObject("dataset")
                .getAsJsonArray("data");

        for (JsonElement element : data_array) {
            try {
                String[] commaSplit = element.toString().split(",");
                List<String> elementList = new ArrayList<>(Arrays.asList(commaSplit));
                for (int i = elementList.size() - 1; i >= 0; i--) {
                    // There are 13 elements. we only need fist 6 element. Remove the rest.
                    if (i > 5) {
                        elementList.remove(i);
                    }
                    // first element is the date
                    if (i == 0) {
                        // First element is date as ["2018-03-23". We have to remove extra characters
                        String replaced = elementList.get(i).replace("[\"", " ");
                        String[] splitted = replaced.split("\"");
                        replaced = splitted[0];
                        replaced = replaced.trim();
                        elementList.set(i, replaced);
                    }
                }
                DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern("yyyy-MM-dd");
                DateTime dateTime = dateTimeFormatter.parseDateTime(elementList.get(0));
                Stock stockElement = new Stock(dateTime,
                                                Float.parseFloat(elementList.get(1)),
                                                Float.parseFloat(elementList.get(2)),
                                                Float.parseFloat(elementList.get(3)),
                                                Float.parseFloat(elementList.get(4)),
                                                Double.parseDouble(elementList.get(5)));
                stock.getStockElementList().add(stockElement);
            } catch (Exception ex) {
                System.out.println("Exception: " + ex.getMessage() + " At: " + Thread.currentThread().getStackTrace()[1]);
            }
        }
    }

    public List<Stock> getDownloadedStock() {
        if (stockDownloaderFuture != null && stockDownloaderFuture.isDone()) {
            try {
                List<Stock> stockElementList = stockDownloaderFuture.get();
                return stockElementList;
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public Stock getStock() {
        return stock;
    }

    public void setStock(Stock stock) {
        this.stock = stock;
    }
}