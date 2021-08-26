package person.shark.stock.worker.stock;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import org.mozilla.universalchardet.UniversalDetector;
import person.shark.stock.pojo.StockDO;
import person.shark.stock.util.StringUtil;
import person.shark.stock.worker.http.HttpRequestWorker;

import java.io.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.Charset;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class StockWorker {

//    public List<StockDO> findStockList() throws IOException, NoSuchAlgorithmException, KeyManagementException {
//        HttpRequestWorker httpRequestWorker = new HttpRequestWorker();
//        String url = "https://www.tpex.org.tw/openapi/v1/tpex_mainboard_quotes";
//        String response = httpRequestWorker.sendHttpsGetRequest(url);
//        Gson gson = new Gson();
//        List<StockDO> stockList = gson.fromJson(response, new TypeToken<List<StockDO>>(){}.getType());
//        return stockList;
//    }


    public List<StockDO> findStockList() throws IOException, CsvValidationException {
        List<StockDO> stockList = new ArrayList<>();
        File file = new File("file");
        File[] stockCsvFileArray = file.listFiles();
        for(File stockCsvFile: stockCsvFileArray) {
            System.out.println("fileName: " + stockCsvFile.getName());
            String encode = detectFileEncode(stockCsvFile);
            FileReader fileReader = new FileReader(stockCsvFile.getAbsoluteFile(), Charset.forName(encode));
            CSVReader csvReader = new CSVReader(fileReader);
            String[] record;
            int index = 0;
            while ((record = csvReader.readNext()) != null) {
                if(index != 0) {
                    StockDO stockDO = new StockDO();
                    stockDO.setName(record[3]);
                    stockDO.setRecentOneYearPayBack(Double.parseDouble(record[5]));
                    stockDO.setRecentTwoYearPayBack(Double.parseDouble(record[6]));
                    stockDO.setRecentThreeYearPayBack(Double.parseDouble(record[7]));
                    stockDO.setRecentFourYearPayBack(Double.parseDouble(record[8]));
                    stockDO.setPrice(Double.parseDouble(record[20]));
                    stockList.add(stockDO);
                }
                index ++;
            }
        }

        return stockList;
    }

    private String detectFileEncode(File file) {
        byte[] buff = new byte[4096];
        String encode = null;
        try(FileInputStream fileInputStream = new FileInputStream(file)) {
            UniversalDetector detector = new UniversalDetector(null);
            int read;
            while ((read = fileInputStream.read(buff)) > 0 && !detector.isDone()) {
                detector.handleData(buff, 0, read);
            }
            detector.dataEnd();
            encode = detector.getDetectedCharset();
            if (StringUtil.isEmpty(encode)) {
                encode = "UTF-8";
            }
            detector.reset();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return encode;
    }

    public List<StockDO> filterByPayBackRate(List<StockDO> stockList, double payBackRate) {
        List<StockDO> filterStockList = new ArrayList<>();
        for(StockDO stock: stockList) {
            if(new BigDecimal(stock.getPrice()).compareTo(BigDecimal.ZERO) == 0) {
                continue;
            }
            BigDecimal fourYearAveragePayBackRate = new BigDecimal(stock.getRecentOneYearPayBack())
                    .add(new BigDecimal(stock.getRecentTwoYearPayBack()))
                    .add(new BigDecimal(stock.getRecentThreeYearPayBack()))
                    .add(new BigDecimal(stock.getRecentFourYearPayBack()))
                    .divide(new BigDecimal(4), 4, RoundingMode.FLOOR)
                    .divide(new BigDecimal(stock.getPrice()), 4, RoundingMode.FLOOR);

            if(new BigDecimal(payBackRate).compareTo(fourYearAveragePayBackRate) == -1) {
                stock.setPayBackRate(fourYearAveragePayBackRate.doubleValue());
                filterStockList.add(stock);
            }
        }
        return filterStockList;
    }

    public List<StockDO> filterByPayBackStandardDeviationRate(List<StockDO> stockList, double payBackStandardDeviationRateCondition) {
        List<StockDO> filterStockList = new ArrayList<>();
        for(StockDO stock: stockList) {
            BigDecimal packBackAverage = new BigDecimal(stock.getRecentOneYearPayBack())
                    .add(new BigDecimal(stock.getRecentTwoYearPayBack()))
                    .add(new BigDecimal(stock.getRecentThreeYearPayBack()))
                    .add(new BigDecimal(stock.getRecentFourYearPayBack()))
                    .divide(new BigDecimal(4), 4, RoundingMode.FLOOR);
            BigDecimal recentOneYearPayBackDeviation = new BigDecimal(stock.getRecentOneYearPayBack()).subtract(packBackAverage);
            BigDecimal recentTwoYearPayBackDeviation = new BigDecimal(stock.getRecentTwoYearPayBack()).subtract(packBackAverage);
            BigDecimal recentThreeYearPayBackDeviation = new BigDecimal(stock.getRecentThreeYearPayBack()).subtract(packBackAverage);
            BigDecimal recentFourYearPayBackDeviation = new BigDecimal(stock.getRecentFourYearPayBack()).subtract(packBackAverage);

            BigDecimal payBackStandardDeviation = new BigDecimal(Math.sqrt(BigDecimal.ZERO
                    .add(recentOneYearPayBackDeviation.multiply(recentOneYearPayBackDeviation))
                    .add(recentTwoYearPayBackDeviation.multiply(recentTwoYearPayBackDeviation))
                    .add(recentThreeYearPayBackDeviation.multiply(recentThreeYearPayBackDeviation))
                    .add(recentFourYearPayBackDeviation.multiply(recentFourYearPayBackDeviation))
                    .divide(new BigDecimal(4), 4, RoundingMode.FLOOR).doubleValue()
            ));
            BigDecimal payBackStandardDeviationRate = payBackStandardDeviation.divide(packBackAverage, 4, RoundingMode.FLOOR);
            if(payBackStandardDeviationRate.compareTo(new BigDecimal(payBackStandardDeviationRateCondition)) == 1) {
                continue;
            }
            stock.setPayBackStandardDeviationRate(payBackStandardDeviationRate.doubleValue());
            filterStockList.add(stock);

        }
        return filterStockList;
    }

    public List<StockDO> sortByPayBackRate(List<StockDO> stockList) {
        stockList.sort(Comparator.comparing(stock -> new BigDecimal(stock.getPayBackRate() * -1)));
        return stockList;
    }

    public void print(List<StockDO> stockList) {
        Gson gson = new Gson();
        for(StockDO stock: stockList) {
            System.out.println(gson.toJson(stock));
        }
    }

}
