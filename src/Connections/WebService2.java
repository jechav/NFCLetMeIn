package Connections;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import javax.swing.JOptionPane;

public class WebService2 {

    Connection conexion;

    public WebService2() {
        conexion = GetConnection();
    }

    public static Connection GetConnection() {
        Connection conexion = null;

        try {
            Class.forName("com.mysql.jdbc.Driver");
            String servidor = "jdbc:mysql://sql20.hostinger.es:3306/u215513673_letme";
            String usuarioDB = "u215513673_root";
            String passwordDB = "admin123";
            //String servidor = "jdbc:mysql://localhost/juegos_tradicionales";
            //String usuarioDB="root";
            //String passwordDB="";
            conexion = DriverManager.getConnection(servidor, usuarioDB, passwordDB);
        } catch (ClassNotFoundException ex) {
            JOptionPane.showMessageDialog(null, ex, "Error1 en la Conexión con la BD " + ex.getMessage(), JOptionPane.ERROR_MESSAGE);
            conexion = null;
            System.err.println("");
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(null, ex, "Error2 en la Conexión con la BD " + ex.getMessage(), JOptionPane.ERROR_MESSAGE);
            System.err.println(ex.getMessage());
            conexion = null;
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null, ex, "Error3 en la Conexión con la BD " + ex.getMessage(), JOptionPane.ERROR_MESSAGE);
            conexion = null;
        } finally {
            return conexion;
        }
    }

    public int getStateUser(int id) throws SQLException {
        Statement comando = conexion.createStatement();
        ResultSet registro = comando.executeQuery("select estado from Usuario where cedula=" + id);
        if (registro.next() == true) {
            //System.out.println("Estado "+registro.getString("estado"));
            return Integer.parseInt(registro.getString("estado"));
            
        } else {
            return -1;
        }

    }
    
    public void registerAcces(int id) throws SQLException{
        int idEntrada=-1;
        Statement comando = conexion.createStatement();
        //comando.executeUpdate("insert into Entradas(fechaYhora) values (now())");
        comando.executeUpdate("SET time_zone = '-5:00'");
        comando.executeUpdate("insert into Entradas(fechaYhora) values (now())");
        ResultSet registro = comando.executeQuery("select MAX(id) from Entradas");
        if (registro.next() == true) {
            System.out.println();
            idEntrada = Integer.parseInt(registro.getString("MAX(id)"));
        }
        comando.executeUpdate("insert into RegistroLaboral(usuario,entrada) values ('"+id+"','"+idEntrada+"')");
        comando.executeUpdate("UPDATE Usuario SET estado=1 WHERE cedula = '"+id+"'");

    }
    
    public void registerExit(int id) throws SQLException{
        int idEntrada=-1;
        Statement comando = conexion.createStatement();
        ResultSet registro = comando.executeQuery("SELECT MAX(entrada) FROM RegistroLaboral WHERE usuario ='"+id+"'");
        if (registro.next() == true) {
            //System.out.println(Integer.parseInt(registro.getString("MAX(entrada)")));
            idEntrada = Integer.parseInt(registro.getString("MAX(entrada)"));
        }
        comando.executeUpdate("insert into Salidas(entrada,fechaYhora) values ('"+idEntrada+"',now())");
        comando.executeUpdate("UPDATE Usuario SET estado= 0 WHERE cedula = '"+id+"'");

    }

    public String getdLastDateAcces(int id) throws SQLException {
        int idEntrada = -1;
        String retorno;
        Statement comando = conexion.createStatement();
        ResultSet registro = comando.executeQuery("SELECT MAX(entrada) FROM RegistroLaboral WHERE usuario ='" + id + "'");
        if (registro.next() == true) {
            //System.out.println(Integer.parseInt(registro.getString("MAX(entrada)")));
            idEntrada = Integer.parseInt(registro.getString("MAX(entrada)"));
        }
        registro = comando.executeQuery("SELECT fechaYhora FROM Entradas WHERE id ='" + idEntrada + "'");
        if (registro.next() == true) {
            //System.out.println(registro.getString("fechaYhora"));
            retorno = registro.getString("fechaYhora");
        }else{
            retorno = "error";
        }
        return retorno;
    }
    
 /*
    public static void main(String[] args) throws SQLException {
        
        WebService2 web = new WebService2();
        web.registerAcces(1082666);
        //web.registerExit(123456789);
        //System.out.println(web.getdLastDateAcces(1082963607));
        /*
        if (web.conexion != null) {
            JOptionPane.showMessageDialog(null, "Conexión Realizada Correctamente");

            Statement comando = web.conexion.createStatement();
            comando.executeUpdate("insert into RegistroLaboral(usuario,entrada) values ('1082765394','3')");
            //comando.executeUpdate("insert into Usuario(cedula,nombres,apellidos, tipoEmpleado, estado) values ('123456789','pirindolo','Cotoplon', 'partechicorio', '0')");
            web.conexion.close();
        }
          
                
    }
  */
}
