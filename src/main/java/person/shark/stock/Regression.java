package person.shark.stock;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

public class Regression {
    public static void main(String[] argv) {
        List<BigDecimal> xList = List.of(
                new BigDecimal("1"), new BigDecimal("2"), new BigDecimal("3"));

        List<BigDecimal> yList = List.of(
                new BigDecimal("6"), new BigDecimal("4"), new BigDecimal("2"));

        BigDecimal slope = new Regression().calculateRegressionSlope(xList, yList);
        System.out.println("slope = " + slope.doubleValue());
    }

    public BigDecimal calculateRegressionSlope(List<BigDecimal> xList, List<BigDecimal> yList) {
        BigDecimal totalX = new BigDecimal("0");
        for(BigDecimal value: xList) {
            totalX = totalX.add(value);
        }
        System.out.println("totalX = " + totalX.doubleValue());
        BigDecimal averageX = totalX.divide(new BigDecimal(xList.size()), 2, RoundingMode.HALF_DOWN);
        System.out.println("averageX = " + averageX.doubleValue());

        BigDecimal totalY = new BigDecimal(0);
        for(BigDecimal value: yList) {
            totalY = totalY.add(value);
        }
        BigDecimal averageY = totalY.divide(new BigDecimal(yList.size()), 2, RoundingMode.HALF_DOWN);
        System.out.println("totalY = " + totalY.doubleValue());
        System.out.println("averageY = " + averageY.doubleValue());


        BigDecimal sumXy = new BigDecimal(0);
        BigDecimal sumVariance = new BigDecimal(0);
        for(int i = 0; i < xList.size(); i ++) {
            BigDecimal x = xList.get(i);
            BigDecimal xDeviation = x.subtract(averageX);
            BigDecimal y = yList.get(i);
            BigDecimal yDeviation = y.subtract(averageY);
            sumXy = sumXy.add(xDeviation.multiply(yDeviation));
            sumVariance = sumVariance.add(xDeviation.multiply(xDeviation));
        }

        BigDecimal slope = sumXy.divide(sumVariance, 3, RoundingMode.HALF_UP);
        return slope;
    }
}
