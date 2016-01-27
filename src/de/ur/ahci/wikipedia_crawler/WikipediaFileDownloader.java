package de.ur.ahci.wikipedia_crawler;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class WikipediaFileDownloader {

    public static void main(String... args) {
        try {
            new WikipediaFileDownloader().run();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public WikipediaFileDownloader() throws IOException {
    }

    public void run() {
        List<String> pages = readPagesList("excellent_articles.txt");
        new Thread(new Runnable() {
            @Override
            public void run() {
                for(int i = 0; i < pages.size(); i++) {
                    String url = "https://de.wikipedia.org" + pages.get(i);

                    new Thread(new FileDownloadThread(url, doc -> {
                        try {
                            onPageDownloaded(doc);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    })).start();

                    try {
                        Thread.sleep(5000);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    private List<String> readPagesList(String s) {
        List<String> URLs = new ArrayList<>();
        try {
            BufferedReader reader = new BufferedReader(new FileReader(s));
            String line;
            while((line = reader.readLine()) != null) {
                URLs.add(line);
            }

            reader.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return URLs;
    }

    private void onPageDownloaded(Document doc) throws IOException {
        if(doc == null) {
            System.out.println("error");
        } else {
            String title = doc.head().getElementsByTag("title").first().ownText();
            title = title.substring(0, title.indexOf(" – Wikipedia")) + ".txt";

            title = title.replaceAll("\\\\", "");
            title = title.replaceAll("/", "");

            FileWriter writer = new FileWriter("wiki_texts/" + title);

            Element body = doc.getElementsByClass("mw-body-content").first();
            Elements textParts = body.getElementsByTag("p");
            for(Element textElement : textParts) {
                writer.write(textElement.text());
                writer.write("\n");
            }

            writer.flush();
            writer.close();
        }
    }

    private class FileDownloadThread implements Runnable {

        private String url;
        private Consumer<Document> callback;

        public FileDownloadThread(String URL, Consumer<Document> callback) {
            this.url = URL;
            this.callback = callback;
        }

        @Override
        public void run() {
            System.out.println("Trying to download " + url);
            try {
                URL url = new URL(this.url);
                Document doc = Jsoup.parse(url, 5000);
                callback.accept(doc);

            } catch (MalformedURLException e) {
                e.printStackTrace();
                callback.accept(null);
            } catch (IOException e) {
                e.printStackTrace();
                callback.accept(null);
            }
        }
    }

}
