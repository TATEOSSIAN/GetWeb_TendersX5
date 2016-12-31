package getweb;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.jsoup.Connection.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;


/**
 *
 * @author AZAZELLO
 */
public class TendersX5 {
    
    public static final String                      INDEX_URL = "https://tender.x5.ru";
    public static final String LOGIN_URL            = INDEX_URL.concat("/user/login/login/");
    public static final String HALLS_URL            = INDEX_URL.concat("/auction/guiding/halls");
    public static final String TENDERS_START_URL    = INDEX_URL.concat("/auction/guiding/list_auction/2-over");
    public static final String START_TENDERS_URL    = INDEX_URL.concat("/auction/guiding/list_auction/2-start");
    
    public static final String USER_AGENT = "Mozilla/5.0 (Windows NT 6.1; Win64; x64)";

    public static void main(String[] args) throws IOException, URISyntaxException {

        Response resp = Jsoup.connect(INDEX_URL)
                .userAgent(USER_AGENT)
                .execute();

        Response loginPageResponse = Jsoup.connect(LOGIN_URL)
                .userAgent(USER_AGENT)
                .timeout(10*1000)
                .cookies(resp.cookies())
                .headers(resp.headers())
                .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8")
                .header("Content-Type", "application/x-www-form-urlencoded;charset=windows-1251")
                .data("Username", "kaskad-ltd")
                .data("Password", "master2019")
                .data("RedirectURL", "/auction/guiding/list_auction/1-start")
                .method(Method.POST)
                .followRedirects(true)
                .execute();
        
        Map<String, String> mapLoginPageCookies = loginPageResponse.cookies();

        resp = Jsoup.connect(HALLS_URL)
                .userAgent(USER_AGENT)
                .timeout(10*1000)
                .cookies(resp.cookies())
                .cookies(mapLoginPageCookies)
                .method(Method.GET)
                .execute();
        
        ArrayList<Document> documents = new ArrayList<>();

        resp = Jsoup.connect(TENDERS_START_URL)
                .userAgent(USER_AGENT)
                .timeout(10*1000)
                .cookies(resp.cookies())
                .cookies(mapLoginPageCookies)
                .method(Method.GET)
                .execute();
         
        Document doc = resp.parse();
        documents.add(doc);
        
        Elements links = doc.getElementsByClass("path");
        
        ArrayList<String> hrefs = new ArrayList<>();
        
        for (Element link : links) {
            
            String linkHref = link.attr("href");
            hrefs.add(linkHref);            
        }
        
        List<String> deduped = hrefs.stream().distinct().collect(Collectors.toList());
        
        for (String url : deduped) {
            
            resp = Jsoup.connect(INDEX_URL.concat(url))
                    .userAgent(USER_AGENT)
                    .timeout(10*1000)
                    .cookies(resp.cookies())
                    .cookies(mapLoginPageCookies)
                    .method(Method.GET)
                    .execute();
            
            doc = resp.parse();
            documents.add(doc); 
        }

        for (Document lDoc : documents) {
            Elements tables = lDoc.getElementsByClass("list");
            Element table = tables.get(0);
            Element tbody = table.child(0);
                       
            Elements rows = tbody.getElementsByTag("tr");
            for (Element row : rows) {
                String[] masParams = null;
                Elements urls = null;
                if (row.elementSiblingIndex() != 0)
                    urls = row.getElementsByTag("a");
                Element url = null;
                if (urls != null)
                    url = urls.get(0);
                String rwtxt = "";
                if (url != null)
                    rwtxt = url.attr("href");
                if (!rwtxt.isEmpty()) {                
                    URI q = new URI(rwtxt);
                    masParams = q.getRawQuery().split("&");
                }
                
                if (masParams != null)                
                toReachTender(masParams);                
            }            
        }        
    }
    
    private static void toReachTender(String[] params) {
        
    }
    
}
