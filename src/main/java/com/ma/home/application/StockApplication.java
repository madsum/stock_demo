package com.ma.home.application;

import javax.servlet.annotation.WebServlet;

import com.ma.home.model.Stock;
import com.ma.home.service.StockService;
import com.vaadin.addon.charts.Chart;
import com.vaadin.addon.charts.model.*;
import com.vaadin.addon.charts.model.style.SolidColor;
import com.vaadin.annotations.Push;
import com.vaadin.annotations.Theme;
import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinServlet;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Push
@Theme("valo")
public class StockApplication extends UI {
    private final VerticalLayout layout = new VerticalLayout();
    private final ScheduledExecutorService timer = Executors.newSingleThreadScheduledExecutor();
    private StockService stockService;

    @Override
    protected void init(VaadinRequest vaadinRequest) {
        setupLayout();
        setStockDownloadUI();
        stockService = new StockService();
    }

    @WebServlet(urlPatterns = "/*", name = "MyUIServlet", asyncSupported = true)
    @VaadinServletConfiguration(ui = StockApplication.class, productionMode = false)
    public static class MyUIServlet extends VaadinServlet {
    }


    private void setupLayout() {
        layout.setDefaultComponentAlignment(Alignment.MIDDLE_CENTER);
        setContent(layout);
    }

    private void setStockDownloadUI() {
        ProgressBar bar = new ProgressBar(0.0f);
        bar.setHeight("100%");
        bar.setWidth("100%");
        bar.setIndeterminate(true);
        bar.setVisible(false);
        layout.addComponent(bar);
        Button downLoadButton = new Button("Start download", clickEvent -> {
            bar.setVisible(true);
            stockService.downloadStockContent();
            startTimer();
            Notification.show("Downloading",
                    "Stock data downloading. Please wait. ",
                    Notification.Type.TRAY_NOTIFICATION)
                    .setDelayMsec(2000);
        });
        downLoadButton.addStyleName("huge");
        downLoadButton.addStyleName(ValoTheme.BUTTON_PRIMARY);
        downLoadButton.setDisableOnClick(true);
        layout.addComponent(downLoadButton);
    }

    private void setupChart() {
        HorizontalLayout horizontalLayout = new HorizontalLayout();
        horizontalLayout.addComponent(getChart());
        layout.addComponent(horizontalLayout);
    }

    private void startTimer() {
        UI ui = UI.getCurrent();
        timer.scheduleWithFixedDelay(new Runnable() {
            public void run() {
                ui.access((() -> {
                    checkDownloadStatus();}));
            }
        }, 1, 1, TimeUnit.SECONDS);
    }

    private void checkDownloadStatus() {
        List<Stock> stockElements = stockService.getDownloadedStock();
        if (stockElements != null && stockElements.size() != 0) {
            layout.removeAllComponents();
            timer.shutdownNow();
            setupChart();
        } else {
            Notification.show("Still Downloading",
                    "Still downloading Stock data . Kindly please wait. ",
                    Notification.Type.TRAY_NOTIFICATION)
                    .setDelayMsec(1000);
        }
    }

    public Chart getChart() {
        Chart chart = new Chart(ChartType.COLUMN);
        chart.setWidth("1200px");
        chart.setHeight("500px");
        Configuration conf = chart.getConfiguration();
        conf.setTitle("Apple Inc. (AAPL) Stock Prices from NASDAQ");
        conf.setSubTitle("Demo Vaadin Application for FA Solution");


        List<String> listDate = new ArrayList<>();
        List<Number> listOpen = new ArrayList<>();
        List<Number> listHigh = new ArrayList<>();
        List<Number> listLow = new ArrayList<>();
        DateTimeFormatter fmt = DateTimeFormat.forPattern("dd-MM-yyyy");
        for (int i = 0; i < 15; /*stockService.getStock().getStockElementList().size();*/ i++) {
            String onlyDate = fmt.print(stockService.getStock().getStockElementList().get(i).getDate());
            listDate.add(onlyDate);
            listOpen.add(stockService.getStock().getStockElementList().get(i).getOpen());
            listHigh.add(stockService.getStock().getStockElementList().get(i).getHigh());
            listLow.add(stockService.getStock().getStockElementList().get(i).getLow());
        }
        String[] arrayDate = new String[listDate.size()];
        arrayDate = listDate.toArray(arrayDate);
        XAxis x = new XAxis();
        x.setCategories(arrayDate);
        x.setTitle("DATES");
        x.setMin(0);
        conf.addxAxis(x);

        YAxis y = new YAxis();
        y.setMin(0);
        y.setTitle("PRICES");
        conf.addyAxis(y);

        Legend legend = new Legend();
        legend.setLayout(LayoutDirection.VERTICAL);
        legend.setBackgroundColor(new SolidColor("#FFFFFF"));
        legend.setAlign(HorizontalAlign.LEFT);
        legend.setVerticalAlign(VerticalAlign.TOP);
        legend.setX(100);
        legend.setY(70);
        legend.setFloating(true);
        legend.setShadow(true);
        conf.setLegend(legend);

        Tooltip tooltip = new Tooltip();
        tooltip.setFormatter("this.x +': '+ this.y ");
        conf.setTooltip(tooltip);

        PlotOptionsColumn plot = new PlotOptionsColumn();
        plot.setPointPadding(0.2);
        plot.setBorderWidth(0);

        ListSeries openSeries = new ListSeries("Stock Open Price", listOpen);
        ListSeries highSeries = new ListSeries("Stock High Price", listHigh);
        ListSeries lowSeries = new ListSeries("Stock Low Price", listLow);

        conf.addSeries(openSeries);
        conf.addSeries(highSeries);
        conf.addSeries(lowSeries);

        chart.drawChart(conf);
        return chart;
    }
}
