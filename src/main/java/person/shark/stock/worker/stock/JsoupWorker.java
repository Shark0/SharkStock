package person.shark.stock.worker.stock;

import com.google.gson.Gson;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import person.shark.stock.pojo.DividendDo;
import person.shark.stock.pojo.RevenueDo;

import java.math.BigDecimal;
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
            if (dividendContentElements.size() != 0) {

                Elements dividendElements = dividendContentElements.get(0).getElementsByClass("List(n)");
                System.out.println("dividendElements.size() = " + dividendElements.size());

                for (Element dividendRowElement : dividendElements) {
                    Elements yearElements = dividendRowElement.getElementsByClass("D(f) Start(0) H(100%) Ai(c) Bgc(#fff) table-row:h_Bgc(#e7f3ff) Pstart(12px) Pend(12px) Bdrststart(4px) Bdrsbstart(4px) Pos(r) Bxz(bb) Z(2)");
                    if (yearElements.size() == 0) {
                        //no dividend info
                        continue;
                    }
                    String yearText = yearElements.get(0).text();
                    System.out.println("yearText = " + yearText);
                    if("-".equalsIgnoreCase(yearText)) {
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
                    if(!"-".equals(cashDividendText)) {
                        dividend = dividend.add(new BigDecimal(cashDividendText));
                    }
                    if(!"-".equals(stockDividendText)) {
                        dividend = dividend.add(new BigDecimal(stockDividendText));
                    }

                    BigDecimal yearDividend = yearDividendMap.get(year);
                    if(yearDividend == null) {
                        yearDividend = dividend;
                    } else {
                        yearDividend = yearDividend.add(dividend);
                    }
                    yearDividendMap.put(year, yearDividend);

                    String dividendRateText = dividendInfoElements.get(2).getElementsByTag("span").get(0).text().replace("%", "");

                    System.out.println("stockCode = " + stockCode + ", yearText = " + yearText + ", cashDividendText = " + cashDividendText +
                            ", stockDividendText = " + stockDividendText + ", dividendRateText = " + dividendRateText);
                    BigDecimal dividendRate = new BigDecimal(0);
                    if(!"-".equals(dividendRateText)) {
                        dividendRate = dividendRate.add(new BigDecimal(dividendRateText));
                    }
                    BigDecimal yearDividendRate = yearDividendRateMap.get(year);
                    if(yearDividendRate == null) {
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
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);

        for(Integer key: yearDividendMap.keySet()) {
            DividendDo dividendDo = new DividendDo();
            BigDecimal yearDividend = yearDividendMap.get(key);
            BigDecimal yearDividendRate = yearDividendRateMap.get(key);
            dividendDo.setYear(key);
            dividendDo.setDividend(yearDividend);
            dividendDo.setDividendRate(yearDividendRate);
            list.add(dividendDo);
        }

        list.sort(new Comparator<DividendDo>() {
            @Override
            public int compare(DividendDo o1, DividendDo o2) {
                return o2.getYear() - o1.getYear();
            }
        });
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
            if (revenueContentElements.size() != 0) {
                Element ulElement = revenueContentElements.get(0);
                Elements liElements = ulElement.getElementsByClass("List(n)");
                for (Element liElement : liElements) {
                    Elements dateElements = liElement.getElementsByClass("W(65px) Ta(start)");
                    if(dateElements.size() == 0) {
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
        if (revenueList.size() > 0) {
            revenueList.remove(0);
        }
        return revenueList;
    }
}
