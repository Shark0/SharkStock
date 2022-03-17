package person.shark.stock.worker.stock;

import com.google.gson.Gson;
import lombok.AllArgsConstructor;
import person.shark.stock.pojo.YahooStockDO;
import person.shark.stock.worker.http.HttpRequestWorker;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

@AllArgsConstructor
public class YahooStockWorker implements Runnable{

    private String stockId;

    private YahooStockListener yahooStockListener;

    @Override
    public void run() {
        try {
            YahooStockDO yahooStock = findYahooStockById(stockId);
            if(yahooStock.getQuoteSummary() != null
                    && yahooStock.getQuoteSummary().getResult() != null
                    && yahooStock.getQuoteSummary().getResult().size() > 0
                    && yahooStock.getQuoteSummary().getResult().get(0).getSummaryDetail() != null
                    && yahooStock.getQuoteSummary().getResult().get(0).getSummaryDetail().getDividendYield() != null
                    && yahooStock.getQuoteSummary().getResult().get(0).getSummaryDetail().getDividendYield().getRaw() != null) {
                yahooStockListener.callBack(stockId, yahooStock);
            } else {
                yahooStockListener.callBack(stockId, null);
            }
        } catch (NoSuchAlgorithmException | KeyManagementException | IOException e) {
            e.printStackTrace();
            yahooStockListener.callBack(stockId, null);
        }
    }

    public YahooStockDO findYahooStockById(String stockId) throws NoSuchAlgorithmException, IOException, KeyManagementException {
        //https://query2.finance.yahoo.com/v10/finance/quoteSummary/1101.tw?modules=assetProfile%2CsummaryProfile%2CsummaryDetail%2CesgScores%2Cprice%2CincomeStatementHistory%2CincomeStatementHistoryQuarterly%2CbalanceSheetHistory%2CbalanceSheetHistoryQuarterly%2CcashflowStatementHistory%2CcashflowStatementHistoryQuarterly%2CdefaultKeyStatistics%2CfinancialData%2CcalendarEvents%2CsecFilings%2CrecommendationTrend%2CupgradeDowngradeHistory%2CinstitutionOwnership%2CfundOwnership%2CmajorDirectHolders%2CmajorHoldersBreakdown%2CinsiderTransactions%2CinsiderHolders%2CnetSharePurchaseActivity%2Cearnings%2CearningsHistory%2CearningsTrend%2CindustryTrend%2CindexTrend%2CsectorTrend
        String url = "https://query2.finance.yahoo.com/v10/finance/quoteSummary/" + stockId + ".tw?modules=summaryDetail%2CdefaultKeyStatistics";
        System.out.println("url = " + url);
        HttpRequestWorker httpRequestWorker = new HttpRequestWorker();
        String response = httpRequestWorker.sendHttpsGetRequest(url);
        Gson gson = new Gson();
        YahooStockDO yahooStock = gson.fromJson(response, YahooStockDO.class);
        return yahooStock;
    }

    public interface YahooStockListener {
        void callBack(String stockId, YahooStockDO yahooStock);
    }
}
