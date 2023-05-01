package gt.edu.url;

import com.mongodb.*;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import picocli.CommandLine;


import javax.print.Doc;
import java.io.BufferedReader;
import java.io.File;
import java.io.StringReader;
import java.nio.file.Files;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.Callable;

@CommandLine.Command(name = "IA abogado", mixinStandardHelpOptions = true, version = "0.0.1",
        description = "Un motor de clasificación")

public class App implements Callable<Integer>
{
    @CommandLine.Option(names = {"-f", "--file"}, description = "File to read", required = false)
    private File file;

    @Override
    public Integer call() throws Exception{

        int total = 0;
        CConexion conexion = new CConexion();
        conexion.crearConexionC();

        if(file != null){
            BufferedReader bfr = Files.newBufferedReader(file.toPath());
            String line;
            while ((line = bfr.readLine()) != null) {
                //QUITAR SIGNOS DE PUNTUACION Y ESPACIOS DOBLES - CONVERTIR A MINUSCULAS
                total++;
                System.out.println(total);
                line = line.toLowerCase();
                line = line.replaceAll("[!\\\"#$%&'()*+,-./:;<=>?@\\\\[\\\\]^_`{}~]", " ");
                line = line.replaceAll("\\s+", " ");


                //SEPARAR EN ETIQUETAS
                String[] partes = line.split("\\|");

                //PARTES DE LA PRIMERA POSICION DE PARTES
                String[] N = (partes[0].toString()).split(" ");

                //LEEMOS PALABRA POR PALABRA
                for(int i = 0; i<N.length; i++){
                    String palabra = N[i].toString();
                    //LEEMOS POR ETIQUETA
                    for(int j = 1; j<= partes.length - 1; j++ ){
                        if(partes[j] != ""){
                            String etiqueta = partes[j];
                            conexion.IngresarDato(etiqueta, palabra);
                        }
                    }
                }
            }

            //CALCULAR PROBABILIDAD

            return 0;
        }else {
            return 1;
        }
    }


    public static void main( String[] args )
    {
        int exitCode = new CommandLine(new App()).execute(args);
        System.exit(exitCode);
    }
}
