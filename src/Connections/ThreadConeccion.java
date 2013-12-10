/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Connections;

import Connections.Coneccion.CardType;
import Imagen.Imagen;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.smartcardio.ATR;
import javax.smartcardio.Card;
import javax.smartcardio.CardChannel;
import javax.smartcardio.CardException;
import javax.smartcardio.CardTerminal;
import NFCLetMeIn.Ventana;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 *
 * @author jochechavez
 */
public class ThreadConeccion extends Thread {

    public String response;
    CardTerminal ct;
    Coneccion coneccion;
    Card c = null;
    public boolean encontrado = false;
    public Ventana jframe;
    WebService2 web;
    Imagen imagenHandler;
    
    public ThreadConeccion(Ventana jframe) {
        this.jframe = jframe;
        coneccion = new Coneccion();
        web = new WebService2();
        imagenHandler = new Imagen();
        //System.out.println("antes de conectar");
        conectar();
        if(encontrado){
            jframe.getLabelEstado().setText(ct.getName());
        }
        //System.out.println("despues de conectar");

    }

    @Override
    public void run() {
        while (true) {
            try {
                
                if (ct.isCardPresent()) {
                    c = coneccion.establishConnection(ct);
                    ATR atr = c.getATR();

                    if (coneccion.getICardType(atr.getHistoricalBytes()) == CardType.MIFARE_1K) {
                        //Lectura y coneccion con las tarjetas Mirafare de 1 k
                        unk();
                    } else {
                        if (coneccion.getICardType(atr.getHistoricalBytes()) == CardType.MIFARE_ULTRALIGHT) {
                            //Lectura y coneccion con las tarjetas Mirafare Ultralight  
                            ultralight();
                        }
                    }

                }
            } catch (CardException ex) {
                Logger.getLogger(ThreadConeccion.class.getName()).log(Level.SEVERE, null, ex);
            } catch (SQLException ex) {
                Logger.getLogger(ThreadConeccion.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(ThreadConeccion.class.getName()).log(Level.SEVERE, null, ex);
            }

        }

    }

    private void conectar() {

        ct = coneccion.selectCardTerminal();
        if (ct != null) {
            encontrado = true;
        }
    }

    private void pausa(int mlSeg) {
        try {
            
            ct.waitForCardAbsent(mlSeg);
                
        } catch (Exception e) {
        }
    }

    private void ultralight() throws CardException, SQLException, IOException {
        System.out.println("Mifire Ultralight - Espere lectura...");
        CardChannel channel = c.getBasicChannel();
        byte block = (byte) 0x04;
        response = coneccion.read(channel, block, 0x10);
        
        muestraResultados();

        //c.disconnect(true);
        pausa(Integer.MAX_VALUE);
    }

    private void unk() throws SQLException, IOException {
        System.out.println("Mifire 1k - Esperando lectura");

        CardChannel channel = c.getBasicChannel();

        byte block = (byte) 0x04;

        //System.out.println("Set Key begins ...");
        byte[] key = {(byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF};
        boolean success = coneccion.setKey(channel);
        if (!success) {
            //System.out.println("Could not set Key ...");
            return;
        }
        //System.out.println("Set Key ends ...");
        
        //System.out.println("Authentication begins ...");
        boolean authenticated = coneccion.authenticate(channel, block);
        //System.out.println("Authentication Ends ...");
        if (authenticated) {
            response = coneccion.read(channel, block, 0x10);
            muestraResultados();
        }else{
            muestraResultados();
        }

        pausa(Integer.MAX_VALUE);
    }

    private void muestraResultados() throws SQLException, IOException {
        if (!"error".equals(response) && (response != null)) {
            
            WebService ws = new WebService(Integer.parseInt(response));
           
            if(imagenHandler.saveImage(response)>1){
                
                jframe.getLabelImage().setIcon(imagenHandler.getImagen(response));
            }else{
                jframe.getLabelImage().setIcon(imagenHandler.getImagen("defecto"));
            }          
            
            jframe.getFieldcedula().setText(response);
            jframe.getFieldNombre().setText(ws.getNombre());
            jframe.getFieldApellidos().setText(ws.getApellidos());
            jframe.getFieldTe().setText(ws.getEmpleado());
            
             Calendar cal = new GregorianCalendar(); 

                // Get the components of the time 
                int hour12 = cal.get(Calendar.HOUR); // 0..11 
                int hour24 = cal.get(Calendar.HOUR_OF_DAY); // 0..23 
                int min = cal.get(Calendar.MINUTE); // 0..59 
                int sec = cal.get(Calendar.SECOND); // 0..59 
                int ms = cal.get(Calendar.MILLISECOND); // 0..999 
                int ampm = cal.get(Calendar.AM_PM); 
                
            if(web.getStateUser(Integer.parseInt(response))==1){
                web.registerExit(Integer.parseInt(response));
                jframe.getFieldEstado().setText("Salió");
                jframe.getFieldfout().setText(Integer.toString(cal.get(Calendar.DATE))+"/"+Integer.toString(cal.get(Calendar.MONTH)+1)+"/"+Integer.toString(cal.get(Calendar.YEAR)));
                jframe.getFieldhout().setText(hour12+":"+min+":"+sec+" "+(ampm==Calendar.AM?"am":"pm"));
                
                jframe.getFieldfin().setText(web.getdLastDateAcces(Integer.parseInt(response)));
                jframe.getFieldhin().setText("");
                
                
            }else{
                web.registerAcces(Integer.parseInt(response));
                jframe.getFieldEstado().setText("Ingresó");
                jframe.getFieldfin().setText(Integer.toString(cal.get(Calendar.DATE))+"/"+Integer.toString(cal.get(Calendar.MONTH)+1)+"/"+Integer.toString(cal.get(Calendar.YEAR)));
                jframe.getFieldhin().setText(hour12+":"+min+":"+sec+" "+(ampm==Calendar.AM?"am":"pm"));
                jframe.getFieldfout().setText("");
                jframe.getFieldhout().setText("");
            }
            
            response = null;
            
        } else {
            System.err.println("Lectura Incompleta o erronea");
            jframe.getFieldcedula().setText("Lectura Incompleta o erronea");
            jframe.getFieldNombre().setText("");
            jframe.getFieldApellidos().setText("");
            jframe.getFieldTe().setText("");
            jframe.getFieldEstado().setText("");
            jframe.getLabelImage().setIcon(imagenHandler.getImagen("defecto"));
        }
    }

}
