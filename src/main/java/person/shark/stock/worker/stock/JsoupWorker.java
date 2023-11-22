package person.shark.stock.worker.stock;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import person.shark.stock.pojo.StockDo;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Calendar;
import java.util.HashMap;

public class JsoupWorker {

    public void findYahooStockInfo(StockDo stockDO) {
        String url = "https://tw.stock.yahoo.com/quote/" + stockDO.getId() + ".TW/dividend";
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        int thePreviousYear = currentYear - 1;
        int thePastTwoYear = currentYear - 2;
        int thePastThreeYear = currentYear - 3;
        BigDecimal thePreviousYearDividend = new BigDecimal("0");
        HashMap<Integer, BigDecimal> yearDividendRateMap = new HashMap<>();
        try {
            Document document = Jsoup.connect(url).get();
            Elements dividendContentElements = document.getElementsByClass("M(0) P(0) List(n)");
            if (dividendContentElements.size() != 0) {
                Element ulElement = dividendContentElements.get(0);
                Elements liElements = ulElement.getElementsByTag("li");
                for (Element liElement : liElements) {
                    Elements yearElements = liElement.getElementsByClass("D(f) W(84px) Ta(start)");
                    if (yearElements.size() == 0) {
                        //no dividend info
                        break;
                    }
                    String yearText = yearElements.get(0).text();
                    int year;
                    if (yearText.contains("Q") || yearText.contains("H")) {
                        year = Integer.parseInt(yearText.substring(0, 4));
                    } else {
                        year = Integer.parseInt(yearText);
                    }
                    if (year < thePastThreeYear) {
                        break;
                    }
                    Elements dividendElements = liElement.getElementsByClass("Fxg(1) Fxs(1) Fxb(0%) Ta(end) Mend($m-table-cell-space) Mend(0):lc Miw(62px)");
                    String cashDividend = dividendElements.get(0).getElementsByTag("span").get(0).text();
                    String stockDividend = dividendElements.get(1).getElementsByTag("span").get(0).text();
                    String dividendRate = liElement.getElementsByClass("Fxg(1) Fxs(1) Fxb(0%) Ta(end) Mend($m-table-cell-space) Mend(0):lc Miw(70px)").get(0).getElementsByTag("span").get(0).text();
                    System.out.println("id = " + stockDO.getId() + ", yearText = " + yearText + ", cashDividend = " + cashDividend + ", stockDividend= " + stockDividend + ", dividendRate = " + dividendRate);
                    BigDecimal yearDividendRate = yearDividendRateMap.get(year);
                    if (yearDividendRate == null) {
                        yearDividendRate = new BigDecimal("0");
                    }
                    if (!dividendRate.equalsIgnoreCase("-")) {
                        dividendRate = dividendRate.replace("%", "");
                        yearDividendRate = yearDividendRate.add(new BigDecimal(dividendRate));
                    }
                    yearDividendRateMap.put(year, yearDividendRate);
                    if (year == thePreviousYear) {
                        if (!cashDividend.equals("-")) {
                            thePreviousYearDividend = thePreviousYearDividend.add(new BigDecimal(cashDividend));
                        }
                        if (!cashDividend.equals("-")) {
                            thePreviousYearDividend = thePreviousYearDividend.add(new BigDecimal(stockDividend));
                        }
                    }
                }
            }

            if (yearDividendRateMap.get(thePreviousYear) != null) {
                stockDO.setThePreviousYearDividendRate(yearDividendRateMap.get(thePreviousYear));
            } else {
                stockDO.setThePreviousYearDividendRate(new BigDecimal("0"));
            }
            if (yearDividendRateMap.get(thePastTwoYear) != null) {
                stockDO.setThePastTwoYearDividendRate(yearDividendRateMap.get(thePastTwoYear));
            } else {
                stockDO.setThePastTwoYearDividendRate(new BigDecimal("0"));
            }
            if (yearDividendRateMap.get(thePastThreeYear) != null) {
                stockDO.setThePastThreeYearDividendRate(yearDividendRateMap.get(thePastThreeYear));
            } else {
                stockDO.setThePastThreeYearDividendRate(new BigDecimal("0"));
            }

            BigDecimal currentYearDividendRate = yearDividendRateMap.get(currentYear);
            if (currentYearDividendRate == null) {
                currentYearDividendRate = thePreviousYearDividend.multiply(BigDecimal.valueOf(100)).divide(stockDO.getPrice(), 2, RoundingMode.CEILING);
            }
            stockDO.setCurrentDividendRate(currentYearDividendRate);
        } catch (Exception e) {
            System.out.println("error stock id = " + stockDO.getId());
            e.printStackTrace();
        }
    }
}
