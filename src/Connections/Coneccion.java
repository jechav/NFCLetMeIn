/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Connections;

import java.util.List;
import java.util.ListIterator;
import java.util.Scanner;
import javax.smartcardio.ATR;
import javax.smartcardio.Card;
import javax.smartcardio.CardChannel;
import javax.smartcardio.CardException;
import javax.smartcardio.CardTerminal;
import javax.smartcardio.CommandAPDU;
import javax.smartcardio.ResponseAPDU;
import javax.smartcardio.TerminalFactory;
import javax.swing.JOptionPane;


/**
 *
 * @author jochechavez
 */
public class Coneccion extends Thread{

    public static byte KEY_LOCATION = 0x01;
    public int estado = 0;
    private byte[] atr = null;
    private String protocol = null;
    private byte[] historical = null;
    
    public CardTerminal selectCardTerminal() {

        try {
            // show the list of available terminals
            TerminalFactory factory = TerminalFactory.getDefault();
            List<CardTerminal> terminals = factory.terminals().list();
            ListIterator<CardTerminal> terminalsIterator = terminals.listIterator();
            CardTerminal terminal = null;
            CardTerminal defaultTerminal = null;

            if (terminals.size() > 0) {
                if (terminals.size() == 1) {
                    terminal = terminals.get(0);
                    System.err.println("Se encontró lector: " + terminal.getName());
                    estado=1;
                  
                    return terminal;       
                } else {
                    
                    System.out.println("Please choose one of these card terminals (1-" + terminals.size() + "):");
                    int i = 1;
                    while (terminalsIterator.hasNext()) {
                        terminal = terminalsIterator.next();
                        System.out.print("[" + i + "] - " + terminal + ", card present: " + terminal.isCardPresent());
                        if (i == 1) {
                            defaultTerminal = terminal;
                            System.out.println(" [default terminal]");
                             
                        } else {
                            
                            System.out.println();
                        }
                        i++;
                    }
                    Scanner in = new Scanner(System.in);
                    try {
                        int option = in.nextInt();
                        terminal = terminals.get(option - 1);
                    } catch (Exception e2) {
                        //System.err.println("Wrong value, selecting default terminal!");
                        terminal = defaultTerminal;

                    }
                    System.out.println("Selected: " + terminal.getName());
                    //Console console = System.console(); 
                    
                    return terminal;
                    
                }
            }

        } catch (Exception e) {
            System.err.println("No se encontró tarjetas activas: ");
            JOptionPane.showMessageDialog(null, "No se encontró tarjetas activas: ");
            estado=0;
            System.exit(-1);
            //e.printStackTrace();
           
        }

        return null;
    }
      
    public static String byteArrayToHexString(byte[] b) {
        StringBuffer sb = new StringBuffer(b.length * 2);
        for (int i = 0; i < b.length; i++) {
            int v = b[i] & 0xff;
            if (v < 16) {
                sb.append('0');
            }
            sb.append(Integer.toHexString(v));
        }
        return sb.toString().toUpperCase();
        
    }
    
    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }
    
    public static String getresponse(byte[] data){
        StringBuffer sb = new StringBuffer(data.length);
        int i=0;
        
        while(i < data.length-1){
            
            if((char)data[i] == '-'){
                
                System.out.println(""+sb.toString());
                return sb.toString();
            }
            
            //System.out.println(data[i]);
            
            sb.append((char)data[i]);
            i++;          
        }
        return "error";
    }

    public Card establishConnection(CardTerminal ct) {
        this.atr = null;
        this.historical = null;
        this.protocol = null;
        /*
        System.out.println("To establish connection, please choose one of these protocols (1-4):");
        System.out.println("[1] - T=0");
        System.out.println("[2] - T=1");
        System.out.println("[3] - T=CL");
        System.out.println("[4] - * [default]");

        String p = "*";
        Scanner in = new Scanner(System.in);
        
        String p = "*";
        try {
            int option = in.nextInt();

            if (option == 1) {
                p = "T=0";
            }
            if (option == 2) {
                p = "T=1";
            }
            if (option == 3) {
                p = "T=CL";
            }
            if (option == 4) {
                p = "*";
            }
        } catch (Exception e) {
            //System.err.println("Wrong value, selecting default protocol!");
            p = "*";
        }
                */
        String p = "*";
        //System.out.println("Selected: " + p);
        
        Card card = null;
        try {
            card = ct.connect(p);
        } catch (CardException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;
        }
        ATR atr = card.getATR();
        System.out.println("Connected:");
        //System.out.println(" - ATR:  " + byteArrayToHexString(atr.getBytes()));
        //System.out.println(" - Historical: " + byteArrayToHexString(atr.getHistoricalBytes()));
        //System.out.println(" - Protocol: " + card.getProtocol());
        
        this.atr = atr.getBytes();
        this.historical = atr.getHistoricalBytes();
        this.protocol = card.getProtocol();

        return card;

    }

    public static boolean setKey(CardChannel channel) {
        byte[] setKey = new byte[]{(byte) 0xff, (byte) 0x82, 0x00, KEY_LOCATION, 0x06, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF};
        ResponseAPDU r;
        try {
            r = channel.transmit(new CommandAPDU(setKey));
            //System.out.println("Set Key: " + Integer.toHexString(r.getSW1()));
            if (r.getSW1() == 0x90) {
                return true;
            }

        } catch (CardException e) {
            e.printStackTrace();
        }

        return false;
    }

    public static boolean authenticate(CardChannel channel, byte block) {

        byte[] authKey = new byte[]{(byte) 0xff, (byte) 0x86, 00, 00, 0x05, 0x01, 0x00, block, 0x60, KEY_LOCATION};

        ResponseAPDU r;
        try {
            r = channel.transmit(new CommandAPDU(authKey));
            //System.out.println("Auth Key: " + Integer.toHexString(r.getSW1()));
            if (r.getSW1() == 0x90) {
                return true;
            }
        } catch (CardException e) {
            e.printStackTrace();
        }

        return false;
    }

    public static String read(CardChannel channel, byte block, int length) {
        byte[] payload = {(byte) 0xFF, (byte) 0xB0, (byte) 0x00, block, (byte) length};
        try {
            CommandAPDU apdu = new CommandAPDU(payload);
            ResponseAPDU r = channel.transmit(apdu);
           
            int success = 0x90;
            if (r.getSW1() == success) {
                //System.out.println("Leyó!");
                return getresponse(r.getBytes());
            }else{
                return "error";
            }
            
            //printData(r.getBytes());
            
            //getresponse(r.getBytes());
        } catch (CardException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return "error";
        }
        
    }

    private static void write(CardChannel channel, byte block) {
        try {
            byte[] payload = {(byte) 0xFF, (byte) 0xD6, (byte) 0x00, block, (byte) 0x10,
                (byte) 0x00, (byte) 0x01, (byte) 0x02, (byte) 0x03, (byte) 0x04, (byte) 0x05, (byte) 0x06, (byte) 0x07, (byte) 0x08, (byte) 0x09, (byte) 0x0A, (byte) 0x0B, (byte) 0x0C, (byte) 0x0D, (byte) 0x0E, (byte) 0x01};
            CommandAPDU apdu = new CommandAPDU(payload);

            ResponseAPDU r = channel.transmit(apdu);
            System.out.println("NR: " + r.getNr());
            System.out.println("SW: " + r.getSW());
            int success = 0x90;
            if (r.getSW1() == success) {
                System.out.println("Yay!");
            }
            System.out.println("SW1: " + Integer.toHexString(r.getSW1()));
            System.out.println("SW2: " + r.getSW2());
            System.out.println("NR: " + r.getNr());
            printData(r.getBytes());

        } catch (CardException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private static void printData(byte[] data) {
        for (int i = 0; i < data.length; i++) {
            System.out.print((char)data[i] + " ");
        }
        System.out.println();
    }

    public static String convertBinToASCII(byte[] bin, int offset, int length) {
        StringBuilder sb = new StringBuilder();
        for (int x = offset; x < offset + length; x++) {
            String s = Integer.toHexString(bin[x]);

            if (s.length() == 1) {
                sb.append('0');
            } else {
                s = s.substring(s.length() - 2);
            }
            sb.append(s);
        }
        System.out.println(sb.toString().toUpperCase());
        return sb.toString().toUpperCase();
    }

    public static enum CardType {

        MIFARE_1K,
        MIFARE_4K,
        MIFARE_ULTRALIGHT,
        MIFARE_MINI,
        TOMAZ_JEWEL,
        FELICA_212K,
        FELICA_424K,
        UNKNOWN
    }

    public static CardType getICardType(byte[] historicalBytes) {

        byte historical_10th_byte = historicalBytes[9]; //0-9 so 10th Byte
        byte historical_11th_byte = historicalBytes[10]; //0-9 so 10th Byte

        //System.out.println("Historical Bytes: 10th byte- " + Integer.toHexString(historical_10th_byte) + " 11th byte- " + Integer.toHexString(historical_11th_byte));

        //How I wished, I could pass a tuple to a switch case.
        if (historical_10th_byte == 0x00 && historical_11th_byte == 0x01) {
            return CardType.MIFARE_1K;
        }
        if (historical_10th_byte == 0x00 && historical_11th_byte == 0x02) {
            return CardType.MIFARE_4K;
        }
        if (historical_10th_byte == 0x00 && historical_11th_byte == 0x03) {
            return CardType.MIFARE_ULTRALIGHT;
        }
        if (historical_10th_byte == 0x00 && historical_11th_byte == 0x26) {
            return CardType.MIFARE_MINI;
        }
        if (historical_10th_byte == 0xF0 && historical_11th_byte == 0x04) {
            return CardType.TOMAZ_JEWEL;
        }
        if (historical_10th_byte == 0xF0 && historical_11th_byte == 0x11) {
            return CardType.FELICA_212K;
        }
        if (historical_10th_byte == 0xF0 && historical_11th_byte == 0x12) {
            return CardType.FELICA_424K;
        }
        return CardType.UNKNOWN;
    }

}
