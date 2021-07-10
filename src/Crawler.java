
import java.io.*;
import java.net.*;
import java.util.*;

public class Crawler {

    public static final String LINK_PREFIX = "<a href=" + '"';  //  создание константы ~~~~~~~~~унифицированный указатель ресурса. Это адрес вебстраницы.
                                                                //
    private static LinkedList<URLDepthPair> oldURL = new LinkedList<URLDepthPair>();
    private static LinkedList<URLDepthPair> newURL = new LinkedList<URLDepthPair>();

    public static void showResult(){
        for (URLDepthPair udp : oldURL){
            System.out.println(udp.toString());
        }
    }

    public static void request(PrintWriter out, URLDepthPair udp) throws MalformedURLException {
        out.println("GET " + udp.getPath() + " HTTP/1.1");
        out.println("Host: " + udp.getHost());
        out.println("Connection: close");
        out.println();
        out.flush();
    }

    public static void getSites(URLDepthPair urlDepthPair) throws IOException {
        newURL.add(new URLDepthPair(urlDepthPair.getUrl(), 0));
        oldURL.add(new URLDepthPair(urlDepthPair.getUrl(), 0));
        while (!newURL.isEmpty()){
            URLDepthPair udp = newURL.removeFirst();
            if (udp.getDepth() < urlDepthPair.getDepth()){
                try{
                    Socket socket = new Socket(udp.getHost(), 80); //подключение к сайту на порт 80
                    socket.setSoTimeout(500);
                    BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                    request(out, udp);
                    String line;
                    try {
                        while ((line = in.readLine()) != null) {
                            while (line.contains(LINK_PREFIX + URLDepthPair.URL_PREFIX)) {
                                StringBuilder currentLink = new StringBuilder();
                                int i = line.indexOf(URLDepthPair.URL_PREFIX);
                                while (line.charAt(i) != '"') {
                                    currentLink.append(line.charAt(i));
                                    i++;
                                }
                                if (!currentLink.toString().isEmpty()){
                                    line = line.substring(i);
                                }
                                URLDepthPair newUDP = new URLDepthPair(currentLink.toString(), udp.getDepth() + 1);
                                if (!(udp.containsURL(newUDP, oldURL))) {
                                    oldURL.add(newUDP);
                                    if (newUDP.getDepth() < urlDepthPair.getDepth()){
                                        newURL.add(newUDP);
                                    }
                                }
                            }
                        }
                    }
                    catch (IOException | StringIndexOutOfBoundsException e){
                        continue;
                    }
                    socket.close();
                }
                catch (Exception e){
                    continue;
                }
            }
        }
        showResult();
    }


    public static void main(String[] args) {
        try{
            args = new String[]{"https://natribu.org/ru","4"};
            URLDepthPair firstURLDepthPair = new URLDepthPair(URLDepthPair.URL_PREFIX + args[0] + "/", Integer.parseInt(args[1]));
            try{
                getSites(firstURLDepthPair);
            }
            catch (IOException | NullPointerException | NumberFormatException exception){
                System.out.println("usage: java crawler " + firstURLDepthPair.getUrl() + " " + firstURLDepthPair.getDepth());
            }
        }
        catch (ArrayIndexOutOfBoundsException e){
            System.out.println("usage: java crawler <URL><depth> ");
        }

    }

}