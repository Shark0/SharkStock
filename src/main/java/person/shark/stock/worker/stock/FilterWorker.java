package person.shark.stock.worker.stock;

import com.google.gson.Gson;
import person.shark.stock.pojo.*;

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

    public List<StockDo> filterByRevenueRegressionNm(
            List<StockDo> stockDoList, int nMonth, int mMonth,
            BigDecimal nSlopeCondition, BigDecimal mSlopeCondition) {
        Gson gson = new Gson();
        for(StockDo stockDo: stockDoList) {
            System.out.println("stockId = " + stockDo.getId());
            List<RevenueDo> revenueDoList = stockDo.getRevenueList();
            RevenueRegressionNmDo revenueRegressionNmDo =
                    calculateRevenueRegressionNm(revenueDoList, nMonth, mMonth);
            stockDo.setNSlope(revenueRegressionNmDo.getNSlope());
            stockDo.setMSlope(revenueRegressionNmDo.getMSlope());
        }

        stockDoList = stockDoList.stream().filter(
                stockDo ->
                        (stockDo.getNSlope().compareTo(nSlopeCondition) > 0) &&
                        (stockDo.getMSlope().compareTo(mSlopeCondition) <= 0))
                .collect(Collectors.toList());

        return stockDoList;
    }

    public RevenueRegressionNmDo calculateRevenueRegressionNm(
            List<RevenueDo> revenueList, int nMonth, int mMonth) {
        RevenueRegressionNmDo revenueRegressionNmDo = new RevenueRegressionNmDo();
        int totalMonth = nMonth + mMonth;
        if (revenueList.size() < (totalMonth + 12)) {
            revenueRegressionNmDo.setNSlope(new BigDecimal(0));
            revenueRegressionNmDo.setNSlope(new BigDecimal(0));
            return revenueRegressionNmDo;
        }

        Map<String, RevenueDo> revenueDoMap = new HashMap<>();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMM");
        for (int i = 0; i < (totalMonth + 12); i++) {
            RevenueDo revenueDo = revenueList.get(i);
            String dateString = simpleDateFormat.format(revenueDo.getDate());
            revenueDoMap.put(dateString, revenueDo);

        }

        List<BigDecimal> nxList = new ArrayList<>();
        List<BigDecimal> originalNyList = new ArrayList<>();
        List<BigDecimal> mxList = new ArrayList<>();
        List<BigDecimal> originalMyList = new ArrayList<>();
        BigDecimal maxDeviationRevenue = new BigDecimal(0);
        BigDecimal totalNDeviationRevenue = new BigDecimal(0);
        BigDecimal totalMDeviationRevenue = new BigDecimal(0);
        for (int i = totalMonth - 1; i >= 0; i--) {
            int y = totalMonth - i;
//            System.out.println("i = " + i + ", y = " + y);
            RevenueDo revenueDo = revenueList.get(i);
            Date date = revenueDo.getDate();
            BigDecimal revenue = revenueDo.getRevenue();
//            System.out.println("date = " + simpleDateFormat.format(date) + ", revenue = " + revenue.doubleValue());

            Calendar previewYearCalendar = Calendar.getInstance();
            previewYearCalendar.setTime(date);
            previewYearCalendar.add(Calendar.YEAR, -1);
            Date previewYearDate = previewYearCalendar.getTime();
            String previewYear = simpleDateFormat.format(previewYearDate);
            RevenueDo previewYearRevenueDo = revenueDoMap.get(previewYear);
//            System.out.println("previewYear = " + previewYear + ", revenue = " + previewYearRevenueDo.getRevenue().doubleValue());

            BigDecimal deviationRevenue = revenue.subtract(previewYearRevenueDo.getRevenue());
            if (deviationRevenue.abs().compareTo(maxDeviationRevenue) > 0) {
                maxDeviationRevenue = deviationRevenue.abs();
            }

            if (i > nMonth - 1) {
                mxList.add(new BigDecimal(y));
                originalMyList.add(deviationRevenue);
                totalMDeviationRevenue.add(deviationRevenue);
            } else if (i == (nMonth - 1)) {
                mxList.add(new BigDecimal(y));
                originalMyList.add(deviationRevenue);
                totalMDeviationRevenue.add(deviationRevenue);
                nxList.add(new BigDecimal(y));
                originalNyList.add(deviationRevenue);
                totalNDeviationRevenue.add(deviationRevenue);
            } else {
                nxList.add(new BigDecimal(y));
                originalNyList.add(deviationRevenue);
                totalNDeviationRevenue.add(deviationRevenue);
            }
        }
        System.out.println("maxDeviationRevenue = " + maxDeviationRevenue);
        BigDecimal onePercentMaxRevenue = maxDeviationRevenue.divide(new BigDecimal(100), 2, RoundingMode.HALF_DOWN);
        System.out.println("onePercentMaxRevenue = " + onePercentMaxRevenue);

        List<BigDecimal> nyList = new ArrayList<>();
        for (BigDecimal y : originalNyList) {
            nyList.add(y.divide(onePercentMaxRevenue, 2, RoundingMode.HALF_DOWN));
        }


        List<BigDecimal> myList = new ArrayList<>();
        for (BigDecimal y : originalMyList) {
            myList.add(y.divide(onePercentMaxRevenue, 2, RoundingMode.HALF_DOWN));
        }


        RegressionWorker regressionWorker = new RegressionWorker();
        BigDecimal nSlope = regressionWorker.calculateRegressionSlope(nxList, nyList);
        BigDecimal mSlope = regressionWorker.calculateRegressionSlope(mxList, myList);
        System.out.println("nxList = " + new Gson().toJson(nxList));
        System.out.println("originalNyList = " + new Gson().toJson(originalNyList));
        System.out.println("nyList = " + new Gson().toJson(nyList));
        System.out.println("mxList = " + new Gson().toJson(mxList));
        System.out.println("originalMyList = " + new Gson().toJson(originalMyList));
        System.out.println("myList = " + new Gson().toJson(myList));
        System.out.println("nSlope = " + nSlope + ", mSlope = " + mSlope);
        revenueRegressionNmDo.setNSlope(nSlope);
        revenueRegressionNmDo.setTotalNDeviationRevenue(totalNDeviationRevenue);
        revenueRegressionNmDo.setMSlope(mSlope);
        revenueRegressionNmDo.setTotalMDeviationRevenue(totalMDeviationRevenue);
        return revenueRegressionNmDo;
    }

    public List<StockDo> filterByRevenueRegressionN(
            List<StockDo> stockDoList, int calculateMonthCount, BigDecimal nSlopeCondition) {
        Gson gson = new Gson();
        for (StockDo stockDo : stockDoList) {
            System.out.println("stockId = " + stockDo.getId());
            List<RevenueDo> revenueDoList = stockDo.getRevenueList();
            RevenueRegressionNDo revenueRegressionNDo = calculateNmRevenueRegressionN(revenueDoList, calculateMonthCount);
            stockDo.setNSlope(revenueRegressionNDo.getNSlop());
            stockDo.setTotalNDeviationRevenue(revenueRegressionNDo.getTotalNDeviationRevenue());
        }
        stockDoList = stockDoList.stream().filter((stockDo) -> (stockDo.getNSlope().compareTo(nSlopeCondition) > 0)).collect(Collectors.toList());
        return stockDoList;
    }

    public RevenueRegressionNDo calculateNmRevenueRegressionN(
            List<RevenueDo> revenueList, int nMonth) {
        RevenueRegressionNDo revenueRegressionNDo = new RevenueRegressionNDo();
        if (revenueList.size() < (nMonth + 12)) {
            revenueRegressionNDo.setNSlop(new BigDecimal(0));
            return revenueRegressionNDo;
        }

        Map<String, RevenueDo> revenueDoMap = new HashMap<>();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMM");
        for (int i = 0; i < (nMonth + 12); i++) {
            RevenueDo revenueDo = revenueList.get(i);
            String dateString = simpleDateFormat.format(revenueDo.getDate());
            revenueDoMap.put(dateString, revenueDo);
        }
        List<BigDecimal> xList = new ArrayList<>();
        List<BigDecimal> originalYList = new ArrayList<>();
        BigDecimal totalDeviationRevenue = new BigDecimal(0);
        BigDecimal maxDeviationRevenue = new BigDecimal(0);
        for (int i = nMonth - 1; i >= 0; i--) {
            int y = nMonth - i;
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
            if (deviationRevenue.abs().compareTo(maxDeviationRevenue) > 0) {
                maxDeviationRevenue = deviationRevenue.abs();
            }
            totalDeviationRevenue = totalDeviationRevenue.add(deviationRevenue);
            xList.add(new BigDecimal(y));
            originalYList.add(deviationRevenue);
        }
        BigDecimal onePercentMaxRevenue = maxDeviationRevenue.divide(new BigDecimal(100), 2, RoundingMode.HALF_DOWN);

        List<BigDecimal> yList = new ArrayList<>();
        for (BigDecimal y : originalYList) {
            yList.add(y.divide(onePercentMaxRevenue, 2, RoundingMode.HALF_DOWN));
        }

        RegressionWorker regressionWorker = new RegressionWorker();
        BigDecimal slope = regressionWorker.calculateRegressionSlope(xList, yList);

        System.out.println("slope = " + slope +
                ", totalDeviationRevenue = " + totalDeviationRevenue.doubleValue() +
                ", xList = " + new Gson().toJson(xList) + ", onePercentMaxRevenue = " + onePercentMaxRevenue.doubleValue() +
                ", originalYList = " + new Gson().toJson(originalYList) +
                ", yList = " + new Gson().toJson(yList));

        revenueRegressionNDo.setNSlop(slope);
        revenueRegressionNDo.setTotalNDeviationRevenue(totalDeviationRevenue);
        return revenueRegressionNDo;
    }

}
