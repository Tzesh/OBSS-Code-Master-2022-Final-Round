package com.example.springboot;

import com.example.springboot.utils.MainSite;
import com.example.springboot.utils.OtherSite;
import com.example.springboot.utils.RedirectChecker;
import com.example.springboot.utils.URLChecker;
import org.jsoup.Connection;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.LinkedList;
import java.util.List;

@RestController
public class Controller {

    @GetMapping("/")
    @RequestMapping(value = "/", produces = MediaType.APPLICATION_JSON_VALUE)
    public String process(@RequestParam(name = "url", required = false, defaultValue = "") String url) throws IOException {
        if (!URLChecker.isURL(url)) return "Given URL is not a valid url";
        MainSite mainSite = new MainSite();
        mainSite.url = url;
        Connection.Response res = null;
        Document doc = null;
        long analysisDuration = System.currentTimeMillis();
        try {
            res = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/535.21 (KHTML, like Gecko) Chrome/19.0.1042.0 Safari/535.21")
                    .timeout(3000)
                    .followRedirects(true)
                    .execute();
            doc = res.parse();
        } catch (SocketTimeoutException e) {
            return "unreachable"; // this is where I had screwed up
        } catch (IOException e) {
            e.printStackTrace();
        }
        mainSite.responseCode = res.statusCode();
        mainSite.responseMessage = res.statusMessage();
        if (doc == null) return "";
        Elements elems = doc.select("a[href]");
        List<String> internalLinksList = new LinkedList<>();
        List<String> externalLinksList = new LinkedList<>();

        for (Element elem : elems) {
            String link = elem.attr("href");
            if (URLChecker.isURL(link)) {
                if (link.replace("https://", "http://").contains(url.replace("https://", "http://"))) {
                    internalLinksList.add(link);
                } else {
                    externalLinksList.add(link);
                }
            }
        }
        List<OtherSite> internalLinks = new LinkedList<>();
        for (int i = 0; i < internalLinksList.size(); i++) {
            String link = internalLinksList.get(i);
            internalLinks.add(accessToSubSite(link));
        }
        mainSite.internalLinks = internalLinks;
        List<OtherSite> externalLinks = new LinkedList<>();
        for (int i = 0; i < externalLinksList.size(); i++) {
            String link = externalLinksList.get(i);
            externalLinks.add(accessToSubSite(link));
        }
        mainSite.externalLinks = externalLinks;
        ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
        analysisDuration = System.currentTimeMillis() - analysisDuration;
        mainSite.analysisDuration = analysisDuration;
        String json = ow.writeValueAsString(mainSite);
        return json;
    }

    private OtherSite accessToSubSite(String link) throws IOException {
        OtherSite site = new OtherSite();
        site.parsedUrl = link;
        site.secured = link.contains("https://");
        site.finalUrl = link;
        if (!crawl(link, site)) {
            site.responseCode = 310;
            site.responseMessage = "Too Many Redirects";
        }
        Connection.Response res = null;
        Document doc = null;
        long startingTime = System.currentTimeMillis();
        try {
            res = Jsoup.connect(link)
                    .userAgent("Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/535.21 (KHTML, like Gecko) Chrome/19.0.1042.0 Safari/535.21")
                    .timeout(3000)
                    .followRedirects(true)
                    .execute();
            doc = res.parse();
        } catch (SocketTimeoutException e) {
            if (res != null) {
                site.responseCode = res.statusCode();
                site.responseMessage = res.statusMessage();
            }
            site.reachable = false;
            site.contentLength = 0;
            site.secured = false;
            site.totalAccessDuration = (System.currentTimeMillis() - startingTime);
            return site;
        } catch (HttpStatusException e) {
            site.responseCode = 404;
            site.responseMessage = "Not Found";
            site.reachable = false;
            site.secured = false;
            site.contentLength = 0;
            site.totalAccessDuration = (System.currentTimeMillis() - startingTime);
            return site;
        } catch (UnknownHostException e) {
            site.responseCode = 503;
            site.responseMessage = "Service Unavailable";
            site.reachable = false;
            site.secured = false;
            site.contentLength = 0;
            site.totalAccessDuration = (System.currentTimeMillis() - startingTime);
            return site;
        } catch (MalformedURLException e) {
            site.responseCode = 400;
            site.responseMessage = "Bad Request";
            site.finalUrl = "";
            site.reachable = false;
            site.secured = false;
            site.contentLength = 0;
            site.totalAccessDuration = (System.currentTimeMillis() - startingTime);
            return site;
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (res != null) {
            site.reachable = true;
            site.secured = false;
            site.responseCode = res.statusCode();
            site.responseMessage = res.statusMessage();
            site.finalUrl = res.url().toString();
            String length = res.header("Content-Length");
            site.contentLength = length == null ? 0 : Integer.valueOf(length);
            try {
                if (RedirectChecker.getRedirectsNumber(res) > 3) {
                    site.responseCode = 310;
                    site.responseMessage = "Too Many Redirects";
                }
            } catch (Exception e) {
                site.totalAccessDuration = (System.currentTimeMillis() - startingTime);
                return site;
            }
        }
        site.totalAccessDuration = (System.currentTimeMillis() - startingTime);
        return site;
    }

    private boolean crawl(String url, OtherSite site) {

        Connection.Response response = null;
        try {
            response = Jsoup.connect(url).followRedirects(false).execute();
        } catch (Exception e) {
            if (site.redirectedURLs.size() < 3) return true;
            return false;
        }

        System.out.println(response.statusCode() + " : " + url);

        if (response.hasHeader("location")) {
            String redirectUrl = response.header("location");
            site.redirectedURLs.add(redirectUrl);
            if (site.redirectedURLs.size() > 3) {
                return false;
            }
            return crawl(redirectUrl, site);
        }
        return true;
    }
}
