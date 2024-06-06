package person.shark.stock.worker.stock;

import com.google.gson.Gson;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import person.shark.stock.pojo.DividendDo;
import person.shark.stock.pojo.EpsDo;
import person.shark.stock.pojo.RevenueDo;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.*;

public class JsoupWorker {

    public List<DividendDo> dividend(String stockCode, int yearCount) {
        String url = "https://tw.stock.yahoo.com/quote/" + stockCode + ".TW/dividend";
        System.out.println("url = " + url);
        HashMap<Integer, BigDecimal> yearDividendMap = new HashMap<>();
        HashMap<Integer, BigDecimal> yearDividendRateMap = new HashMap<>();

        int lastYear = Calendar.getInstance().get(Calendar.YEAR) - yearCount;
        try {
            Document document = Jsoup.connect(url).get();
            Elements dividendContentElements = document.getElementsByClass("M(0) P(0) List(n)");
            System.out.println("dividendContentElements.size() = " + dividendContentElements.size());
            if (!dividendContentElements.isEmpty()) {

                Elements dividendElements = dividendContentElements.get(0).getElementsByClass("List(n)");
                System.out.println("dividendElements.size() = " + dividendElements.size());

                for (Element dividendRowElement : dividendElements) {
                    Elements yearElements = dividendRowElement.getElementsByClass("D(f) Start(0) H(100%) Ai(c) Bgc(#fff) table-row:h_Bgc(#e7f3ff) Pstart(12px) Pend(12px) Bdrststart(4px) Bdrsbstart(4px) Pos(r) Bxz(bb) Z(2)");
                    if (yearElements.isEmpty()) {
                        //no dividend info
                        continue;
                    }
                    String yearText = yearElements.get(0).text();
                    System.out.println("yearText = " + yearText);
                    if ("-".equalsIgnoreCase(yearText)) {
                        continue;
                    }
                    int year;
                    if (yearText.contains("Q") || yearText.contains("H")) {
                        year = Integer.parseInt(yearText.substring(0, 4));
                    } else {
                        year = Integer.parseInt(yearText);
                    }
                    if (year < lastYear) {
                        break;
                    }
                    Elements dividendInfoElements = dividendRowElement.getElementsByClass("Fxg(1) Fxs(1) Fxb(0%) Ta(end) Mend(0):lc Mend(12px) W(72px) Miw(72px)");
                    String cashDividendText = dividendInfoElements.get(0).getElementsByTag("span").get(0).text();
                    String stockDividendText = dividendInfoElements.get(1).getElementsByTag("span").get(0).text();

                    BigDecimal dividend = new BigDecimal(0);
                    if (!"-".equals(cashDividendText)) {
                        dividend = dividend.add(new BigDecimal(cashDividendText));
                    }
                    if (!"-".equals(stockDividendText)) {
                        dividend = dividend.add(new BigDecimal(stockDividendText));
                    }

                    BigDecimal yearDividend = yearDividendMap.get(year);
                    if (yearDividend == null) {
                        yearDividend = dividend;
                    } else {
                        yearDividend = yearDividend.add(dividend);
                    }
                    yearDividendMap.put(year, yearDividend);

                    String dividendRateText = dividendInfoElements.get(2).getElementsByTag("span").get(0).text().replace("%", "");

                    System.out.println("stockCode = " + stockCode + ", yearText = " + yearText + ", cashDividendText = " + cashDividendText +
                            ", stockDividendText = " + stockDividendText + ", dividendRateText = " + dividendRateText);
                    BigDecimal dividendRate = new BigDecimal(0);
                    if (!"-".equals(dividendRateText)) {
                        dividendRate = dividendRate.add(new BigDecimal(dividendRateText));
                    }
                    BigDecimal yearDividendRate = yearDividendRateMap.get(year);
                    if (yearDividendRate == null) {
                        yearDividendRate = dividendRate;
                    } else {
                        yearDividendRate = yearDividendRate.add(yearDividendRate);
                    }
                    yearDividendRateMap.put(year, yearDividendRate);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        List<DividendDo> list = new ArrayList<>();
        for (Integer key : yearDividendMap.keySet()) {
            DividendDo dividendDo = new DividendDo();
            BigDecimal yearDividend = yearDividendMap.get(key);
            BigDecimal yearDividendRate = yearDividendRateMap.get(key);
            dividendDo.setYear(key);
            dividendDo.setDividend(yearDividend);
            dividendDo.setDividendRate(yearDividendRate);
            list.add(dividendDo);
        }

        list.sort((o1, o2) -> o2.getYear() - o1.getYear());
        return list;
    }

    public List<EpsDo> eps(String stockCode, int yearCount) {
        String url = "https://tw.stock.yahoo.com/quote/" + stockCode + ".TW/eps";
        System.out.println("url = " + url);
        HashMap<Integer, BigDecimal> yearEpsMap = new HashMap<>();
        HashMap<Integer, Integer> yearEpsCountMap = new HashMap<>();
        int lastYear = Calendar.getInstance().get(Calendar.YEAR) - yearCount;
        List<EpsDo> list = new ArrayList<>();
        try {
            Document document = Jsoup.connect(url).get();
            Elements epsElements = document.getElementsByClass("M(0) P(0) List(n)");
            System.out.println("dividendContentElements.size() = " + epsElements.size());
            if (epsElements.isEmpty()) {
                return list;
            }


            Elements dividendElements = epsElements.get(0).getElementsByClass("List(n)");
            System.out.println("dividendElements.size() = " + dividendElements.size());

            for (Element dividendRowElement : dividendElements) {
                String yearClassName = "D(f) Start(0) H(100%) Ai(c) Bgc(#fff) table-row:h_Bgc(#e7f3ff) Pstart(12px) Pend(12px) Bdrststart(4px) Bdrsbstart(4px) Pos(r) Bxz(bb) Z(2)";
                Elements yearElements = dividendRowElement.getElementsByClass(yearClassName);
                if (yearElements.isEmpty()) {
                    //no dividend info
                    continue;
                }
                String yearText = yearElements.get(0).text();
                System.out.println("yearText = " + yearText);
                if ("-".equalsIgnoreCase(yearText)) {
                    continue;
                }
                int year;
                if (yearText.contains("Q") || yearText.contains("H")) {
                    year = Integer.parseInt(yearText.substring(0, 4));
                } else {
                    year = Integer.parseInt(yearText);
                }
                if (year < lastYear) {
                    break;
                }
                String epxClassName = "Fxg(1) Fxs(1) Fxb(0%) Miw($w-table-cell-min-width) Ta(end) Mend($m-table-cell-space) Mend(0):lc";
                Elements epsElement = dividendRowElement.getElementsByClass(epxClassName);
                String epsText = epsElement.get(0).getElementsByTag("span").get(0).text();

                BigDecimal eps = new BigDecimal(0);
                if (!"-".equals(epsText)) {
                    eps = eps.add(new BigDecimal(epsText));
                }

                BigDecimal yearEps = yearEpsMap.get(year);
                if (yearEps == null) {
                    yearEps = eps;
                } else {
                    yearEps = yearEps.add(eps);
                }
                yearEpsMap.put(year, yearEps);
                Integer yearEpsCount = yearEpsCountMap.get(year);
                if(yearEpsCount == null) {
                    yearEpsCount = 1;
                } else {
                    yearEpsCount = yearEpsCount + 1;
                }
                yearEpsCountMap.put(year, yearEpsCount);
                System.out.println("stockCode = " + stockCode + ", yearText = " + yearText + ", epsText = " + epsText);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        for (Integer key : yearEpsMap.keySet()) {
            EpsDo yearEpsDo = new EpsDo();
            BigDecimal yearEps = yearEpsMap.get(key);
            Integer yearEpsCount = yearEpsCountMap.get(key);
            if (yearEpsCount != 4) {
                yearEps = yearEps.multiply(new BigDecimal(4)).divide(new BigDecimal(yearEpsCount), 2, RoundingMode.HALF_UP);
            }
            yearEpsDo.setYear(key);
            yearEpsDo.setEps(yearEps);
            list.add(yearEpsDo);
        }

        list.sort((o1, o2) -> o2.getYear() - o1.getYear());
        return list;
    }

    public List<RevenueDo> revenue(String stockCode) {
        List<RevenueDo> revenueList = new ArrayList<>();
        String url = "https://tw.stock.yahoo.com/quote/" + stockCode + "/revenue";
        System.out.println("url = " + url);
        try {
            Document document = Jsoup.connect(url).get();
            Elements revenueContentElements = document.getElementsByClass("M(0) P(0) List(n)");
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy/MM");
            if (!revenueContentElements.isEmpty()) {
                Element ulElement = revenueContentElements.get(0);
                Elements liElements = ulElement.getElementsByClass("List(n)");
                for (Element liElement : liElements) {
                    Elements dateElements = liElement.getElementsByClass("W(65px) Ta(start)");
                    if (dateElements.isEmpty()) {
                        continue;
                    }
                    RevenueDo revenueDo = new RevenueDo();
                    String dateString = dateElements.get(0).text();
                    revenueDo.setDate(simpleDateFormat.parse(dateString));
                    Element singleMonthElement = liElement.getElementsByTag("ul").get(0);
                    String revenue = singleMonthElement.getElementsByTag("li").get(0).
                            getElementsByTag("span").get(0).text().
                            replace(",", "");
                    revenueDo.setRevenue(new BigDecimal(revenue));
                    revenueList.add(revenueDo);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (!revenueList.isEmpty()) {
            revenueList.remove(0);
        }
        return revenueList;
    }

    public static void main(String[] args) {
        String stockCode = "1515";
        List<EpsDo> epsDoList = new JsoupWorker().eps(stockCode, 3);
        System.out.println("epsDoList = " + new Gson().toJson(epsDoList));
    }
}
