import java.net.*;
import java.io.*;

public class TestHubspot {
    public static void main(String[] args) throws Exception {
        URL url = new URL("https://20098866.fs1.hubspotusercontent-na1.net/hubfs/20098866/Inmuebles%20Privados%20Vitrina/Casa%20Prueba%20Railway-a649bf6b/metadatos.json");
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");
        
        try {
            System.out.println("Response Code: " + con.getResponseCode());
            System.out.println("Response Message: " + con.getResponseMessage());
            
            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                System.out.println(inputLine);
            }
            in.close();
        } catch(Exception e) {
            e.printStackTrace();
            if (con.getErrorStream() != null) {
                BufferedReader in = new BufferedReader(new InputStreamReader(con.getErrorStream()));
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    System.out.println(inputLine);
                }
                in.close();
            }
        }
    }
}
