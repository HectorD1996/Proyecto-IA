package gt.edu.url;

public class Etiquetas {
    public String etiqueta;
    public String palabra;
    public Integer cantidad;

    public Etiquetas(String Netiqueta, String Npalabra, Integer Ncant){
        etiqueta = Netiqueta;
        palabra = Npalabra;
        cantidad = Ncant;
    }

    public String getNombre(){
        return palabra;
    }

    public String getID(){
        return etiqueta;
    }

    public Integer getCantidad(){
        return cantidad;
    }


}
