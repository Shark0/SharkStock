package person.shark.stock.worker.stock;

import com.google.gson.Gson;
import person.shark.stock.pojo.DividendDo;
import person.shark.stock.pojo.RevenueDo;
import person.shark.stock.pojo.StockDo;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

public class FilterWorker {
    public List<StockDo> filterByDividendRate(List<StockDo> stockDoList, BigDecimal dividendRate) {
        return stockDoList.stream().filter((stockDo) -> {
            List<DividendDo> dividendDoList = stockDo.getDividendList();
            if (dividendDoList == null || dividendDoList.size() == 0) {
                return false;
            }
            BigDecimal currentDividendRate = dividendDoList.get(0).getDividend().divide(stockDo.getPrice(), 3, RoundingMode.HALF_DOWN);
            System.out.println("stock Id: " + stockDo.getId() + ", currentDividendRate: " + currentDividendRate.doubleValue());
            return currentDividendRate.compareTo(dividendRate) > 0;
        }).collect(Collectors.toList());
    }

    public List<StockDo> filterByRevenueRegressionNm(List<StockDo> stockDoList, int nMonth, int mMonth, int nSlopeCondition, int mSlopeCondition) {
        return stockDoList.stream().filter((stockDo) -> {
            List<RevenueDo> revenueDoList = stockDo.getRevenueList();
            boolean isMeetRevenueRegressionCondition = isMeetRevenueRegressionConditionNm(revenueDoList, nMonth, mMonth, nSlopeCondition, mSlopeCondition);
            System.out.println("stockId = " + stockDo.getId() + ", isMeetRevenueRegressionCondition = " + isMeetRevenueRegressionCondition);
            return isMeetRevenueRegressionCondition;
        }).collect(Collectors.toList());
    }

    public boolean isMeetRevenueRegressionConditionNm(List<RevenueDo> revenueList, int nMonth, int mMonth, int nSlopeCondition, int mSlopeCondition ) {
        int totalMonth = nMonth + mMonth;
        if(revenueList.size() < (totalMonth + 12)) {
            return false;
        }

        Map<String, RevenueDo> revenueDoMap = new HashMap<>();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMM");
        for(int i = 0; i < (totalMonth + 12); i ++) {
            RevenueDo revenueDo = revenueList.get(i);
            String dateString = simpleDateFormat.format(revenueDo.getDate());
            revenueDoMap.put(dateString, revenueDo);
        }
        List<BigDecimal> nxList = new ArrayList<>();
        List<BigDecimal> nyList = new ArrayList<>();
        List<BigDecimal> mxList = new ArrayList<>();
        List<BigDecimal> myList = new ArrayList<>();

        for(int i = totalMonth - 1; i >= 0; i --) {
            int y = totalMonth - i;
            System.out.println("i = " + i + ", y = " + y);
            RevenueDo revenueDo = revenueList.get(i);
            Date date = revenueDo.getDate();
            BigDecimal revenue = revenueDo.getRevenue();
            System.out.println("date = " + simpleDateFormat.format(date) + ", revenue = " + revenue.doubleValue());

            Calendar previewYearCalendar = Calendar.getInstance();
            previewYearCalendar.setTime(date);
            previewYearCalendar.add(Calendar.YEAR, -1);
            Date previewYearDate = previewYearCalendar.getTime();
            String previewYear = simpleDateFormat.format(previewYearDate);
            RevenueDo previewYearRevenueDo = revenueDoMap.get(previewYear);
            System.out.println("previewYear = " + previewYear + ", revenue = " + previewYearRevenueDo.getRevenue().doubleValue());

            BigDecimal deviationRevenue = revenue.subtract(previewYearRevenueDo.getRevenue());

            if(i > nMonth - 1) {
                mxList.add(new BigDecimal(y));
                myList.add(deviationRevenue);
            } else if (i == (nMonth - 1)) {
                mxList.add(new BigDecimal(y));
                myList.add(deviationRevenue);
                nxList.add(new BigDecimal(y));
                nyList.add(deviationRevenue);
            } else {
                nxList.add(new BigDecimal(y));
                nyList.add(deviationRevenue);
            }
        }
        RegressionWorker regressionWorker = new RegressionWorker();
        BigDecimal nSlope = regressionWorker.calculateRegressionSlope(nxList, nyList);
        BigDecimal mSlope = regressionWorker.calculateRegressionSlope(mxList, myList);
        System.out.println("nxList = " + new Gson().toJson(nxList));
        System.out.println("nyList = " + new Gson().toJson(nyList));
        System.out.println("mxList = " + new Gson().toJson(mxList));
        System.out.println("myList = " + new Gson().toJson(myList));
        System.out.println("nSlope = " + nSlope + ", mSlope = " + mSlope);
        return (nSlope.compareTo(new BigDecimal(nSlopeCondition)) > 0) && (mSlope.compareTo(new BigDecimal(mSlopeCondition)) <= 0);
    }

    public List<StockDo> filterByRevenueRegressionN(List<StockDo> stockDoList, int calculateMonthCount) {
        return stockDoList.stream().filter((stockDo) -> {
            List<RevenueDo> revenueDoList = stockDo.getRevenueList();
            boolean isMeetRevenueRegressionCondition = isMeetNmRevenueRegressionConditionN(revenueDoList, calculateMonthCount);
            System.out.println("stockId = " + stockDo.getId() + ", isMeetRevenueRegressionCondition = " + isMeetRevenueRegressionCondition);
            return isMeetRevenueRegressionCondition;
        }).collect(Collectors.toList());
    }

    public boolean isMeetNmRevenueRegressionConditionN(List<RevenueDo> revenueList, int calculateMonthCount) {

        if(revenueList.size() < (calculateMonthCount + 12)) {
            return false;
        }

        Map<String, RevenueDo> revenueDoMap = new HashMap<>();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMM");
        for(int i = 0; i < (calculateMonthCount + 12); i ++) {
            RevenueDo revenueDo = revenueList.get(i);
            String dateString = simpleDateFormat.format(revenueDo.getDate());
            revenueDoMap.put(dateString, revenueDo);
        }
        List<BigDecimal> xList = new ArrayList<>();
        List<BigDecimal> yList = new ArrayList<>();

        BigDecimal totalDeviationRevenue = new BigDecimal(0);
        for(int i = calculateMonthCount - 1; i >= 0; i --) {
            int y = calculateMonthCount - i;
            System.out.println("i = " + i + ", y = " + y);
            RevenueDo revenueDo = revenueList.get(i);
            Date date = revenueDo.getDate();
            BigDecimal revenue = revenueDo.getRevenue();
            System.out.println("date = " + simpleDateFormat.format(date) + ", revenue = " + revenue.doubleValue());

            Calendar previewYearCalendar = Calendar.getInstance();
            previewYearCalendar.setTime(date);
            previewYearCalendar.add(Calendar.YEAR, -1);
            Date previewYearDate = previewYearCalendar.getTime();
            String previewYear = simpleDateFormat.format(previewYearDate);
            RevenueDo previewYearRevenueDo = revenueDoMap.get(previewYear);
            System.out.println("previewYear = " + previewYear + ", revenue = " + previewYearRevenueDo.getRevenue().doubleValue());

            BigDecimal deviationRevenue = revenue.subtract(previewYearRevenueDo.getRevenue());
            totalDeviationRevenue = totalDeviationRevenue.add(deviationRevenue);

            xList.add(new BigDecimal(y));
            yList.add(deviationRevenue);
        }
        RegressionWorker regressionWorker = new RegressionWorker();
        BigDecimal slope = regressionWorker.calculateRegressionSlope(xList, yList);

        System.out.println("slope = " + slope + ", totalDeviationRevenue = " + totalDeviationRevenue.doubleValue()
            + ", xList = " + new Gson().toJson(xList) + ", yList = " + new Gson().toJson(yList));
        return (slope.compareTo(new BigDecimal(0)) > 0) && (totalDeviationRevenue.compareTo(new BigDecimal(0)) > 0);
    }

}
