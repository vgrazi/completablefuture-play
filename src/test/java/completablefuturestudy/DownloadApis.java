package completablefuturestudy;

import kong.unirest.HttpResponse;
import kong.unirest.Unirest;

public class DownloadApis {
    public static void main(String[] args) {

        String result =
//        downloadSummary();
//        getQuotes();
        getMovers();
        int debug = 0;
    }

    private static String getQuotes() {
        HttpResponse<String> response = Unirest.get("https://apidojo-yahoo-finance-v1.p.rapidapi.com/market/get-quotes?region=US&lang=en&symbols=BAC%252CKC%253DF%252C002210.KS%252CIWM%252CAMECX")
            .header("x-rapidapi-host", "apidojo-yahoo-finance-v1.p.rapidapi.com")
            .header("x-rapidapi-key", "cfe64b388dmshd83351535f13124p178e70jsnba0add7d7c37")
            .asString();
        return response.getBody();
    }

    private static String downloadSummary() {
        HttpResponse<String> response = Unirest.get("https://apidojo-yahoo-finance-v1.p.rapidapi.com/market/get-summary?region=US&lang=en")
            .header("x-rapidapi-host", "apidojo-yahoo-finance-v1.p.rapidapi.com")
            .header("x-rapidapi-key", "cfe64b388dmshd83351535f13124p178e70jsnba0add7d7c37")
            .asString();
        return response.getBody();

    }
    private static String getMovers() {
        HttpResponse<String> response = Unirest.get("https://apidojo-yahoo-finance-v1.p.rapidapi.com/market/get-movers?region=US&lang=en")
            .header("x-rapidapi-host", "apidojo-yahoo-finance-v1.p.rapidapi.com")
            .header("x-rapidapi-key", "cfe64b388dmshd83351535f13124p178e70jsnba0add7d7c37")
            .asString();
        return response.getBody();

    }
}
