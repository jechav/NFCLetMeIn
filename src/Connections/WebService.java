/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Connections;

/**
 *
 * @author andres
 */
import com.json.parsers.JsonParserFactory;
import com.json.parsers.JSONParser;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Map;

public class WebService {

    private String buffer;
    private String nombre;
    private String apellidos;
    private String tipoempleados;
    private Map jsonData;

    public WebService(int id) {
        try {
            // Se abre la conexión
            URL url = new URL("http://letmein-ws.esy.es/usuarios.php?id="+id);
            URLConnection conexion = url.openConnection();
            conexion.connect();

            // Lectura
            InputStream is = conexion.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            final char[] buffer = new char[1000];
            String json = new String(buffer, 0, br.read(buffer));

            //Quita los corchetes que vienen incluidos para hacer la conversion correctramente
            char[] otro = new char[json.length() - 4];
            json.getChars(3, json.length() - 1, otro, 0);
            String res = new String(otro);

            //Comvierte jason a un Map manejable
            JsonParserFactory factory = JsonParserFactory.getInstance();
            JSONParser parser = factory.newJsonParser();
            jsonData = parser.parseJson(res);

        } catch (MalformedURLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public String getNombre() {

        String value = (String) jsonData.get("nombres");
        System.out.println(value);
        return value;
    }
    public String getApellidos(){
        String value = (String) jsonData.get("apellidos");
        System.out.println(value);
        return value;
    }
    public String getEmpleado(){
        String value = (String) jsonData.get("tipoEmpleado");
        System.out.println(value);
        return value;
    }
}
