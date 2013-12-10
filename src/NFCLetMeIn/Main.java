/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package NFCLetMeIn;

import Connections.ThreadConeccion;
import java.io.IOException;
import javax.swing.UnsupportedLookAndFeelException;
 
public class Main {

    
    public static void main(String[] args) throws ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException, IOException {
        Ventana jframe = new Ventana();
        ThreadConeccion hiloconeccion = new ThreadConeccion(jframe);
        if (hiloconeccion.encontrado) {

            jframe.setVisible(true);
            hiloconeccion.run();
           
        }

    }

}
