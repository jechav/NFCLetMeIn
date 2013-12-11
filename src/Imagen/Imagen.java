/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Imagen;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import javax.swing.ImageIcon;

/**
 *
 * @author jochechavez
 */
public class Imagen {

    public int saveImage(String id) throws IOException {
        int estado=0;
        String imageUrl = "http://letmein-webservices.esy.es/Imagenes/"+id+".jpg";
        String destinationFile = System.getProperty("java.io.tmpdir").toString()+id+".jpg";
            
        URL url = new URL(imageUrl);
        InputStream is = url.openStream();
        OutputStream os = new FileOutputStream(destinationFile);

        byte[] b = new byte[2048];
        int length;
        
        while ((length = is.read(b)) != -1) {
            os.write(b, 0, length);
            
            estado++;
        }
        
        
        is.close();
        os.close();
        return estado;
    }
    public ImageIcon getImagen(String id){
        return new ImageIcon(System.getProperty("java.io.tmpdir").toString()+id+".jpg");
    }
}
