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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

        int total_personas = 0;
        CConexion conexion = new CConexion();
        conexion.crearConexionC();
        Map<String, Integer> map = new HashMap<String, Integer>();
        Map<String, Integer> map_frecuencias = new HashMap<String, Integer>();

        if(file != null){
            BufferedReader bfr = Files.newBufferedReader(file.toPath());
            String line;
            while ((line = bfr.readLine()) != null) {
                //QUITAR SIGNOS DE PUNTUACION Y ESPACIOS DOBLES - CONVERTIR A MINUSCULAS
                line = line.toLowerCase();
                line = line.replaceAll("[!\\\"#$%&'()*+,-./:;<=>?@\\\\[\\\\]^_`{}~]", " ");
                line = line.replaceAll("\\|\\|+", " ");
                line = line.replaceAll("\\s+", " ");

                total_personas++;
                System.out.println(total_personas);


                //SEPARAR EN ETIQUETAS
                String[] partes = line.split("\\|");

                //PARTES DE LA PRIMERA POSICION DE PARTES
                String[] N = (partes[0].toString()).split(" ");

                //Acumular frecuencia de la etiqueta
                if(map_frecuencias.containsKey(partes[1])){
                    Integer nuevo1 = map_frecuencias.get(partes[1]) + 1;
                    map_frecuencias.replace(partes[1], nuevo1);
                }
                else{
                    map_frecuencias.put(partes[1], 1);
                }

                //LEEMOS PALABRA POR PALABRA
                for(int i = 0; i<N.length; i++){
                    String palabra = N[i].toString();
                    //LEEMOS POR ETIQUETA
                    for(int j = 1; j<= partes.length - 1; j++ ){
                        if(partes[j] != ""){
                            String etiqueta = partes[j];
                            conexion.IngresarDato(etiqueta, palabra);
                            if(map.containsKey(etiqueta)){
                                Integer nuevo = map.get(etiqueta) + 1;
                                map.replace(etiqueta, nuevo);
                            }
                            else{
                                map.put(etiqueta, 1);
                            }
                        }
                    }
                }
            }

            //CALCULAR PROBABILIDAD
            for(Map.Entry<String, Integer> entry : map.entrySet()){
                String etiquetase = entry.getKey();
                Integer Totalote = entry.getValue();
                conexion.TotalEtiquetas(etiquetase, Totalote);
            }

            //Calcular frecuencia de etiquetas
            for(Map.Entry<String, Integer> entry : map_frecuencias.entrySet()){
                String etiqueta = entry.getKey();
                Integer frecuancia = entry.getValue();
                conexion.FrecuenciaEtiquetas(etiqueta, frecuancia);
            }

            //TOTALES


            Scanner scanner = new Scanner(System.in);
            String input = "";
            while(!input.equals("exit")){
                System.out.print("Por favor ingrese la cadena > ");
                input = scanner.nextLine();
                if(input.equals("exit")){
                    break;
                }
                Map<String, Double> comparacion = new HashMap<String, Double>();
                comparacion = conexion.Frecuencia(input);

                //VERIFICAR MAYOR
                double max = 0.0;
                String maximo = " ";
                for (Map.Entry<String, Double> sunoo : comparacion.entrySet()){
                    double nuevo = sunoo.getValue();
                    if(nuevo > max){
                        max = nuevo;
                        maximo = sunoo.getKey();
                    }
                }

                System.out.println("Mayor probabilidad de pertenecer a: " + maximo + " con la probabilidad: " + max);
            }

            return 0;
        }else {

            return 1;
        }
    }


    public static void main( String[] args )
    {
        CConexion conexion = new CConexion();
        conexion.crearConexionC();
        Map<String, Double> comparacion = new HashMap<String, Double>();
        String ora = "secret is secret";
        ora = ora.toLowerCase();
        ora = ora.replaceAll("[!\\\"#$%&'()*+,-./:;<=>?@\\\\[\\\\]^_`{}~]", " ");
        ora = ora.replaceAll("\\s+", " ");
        comparacion = conexion.Frecuencia(ora);

        //VERIFICAR MAYOR
        double max = 0.0;
        String maximo = " ";
        for (Map.Entry<String, Double> sunoo : comparacion.entrySet()){
            double nuevo = sunoo.getValue();
            if(nuevo > max){
                max = nuevo;
                maximo = sunoo.getKey();
            }
        }


        System.out.println("Mayor probabilidad de pertenecer a: " + maximo + " con la probabilidad: " + max);
        int exitCode = new CommandLine(new App()).execute(args);
        System.exit(exitCode);
    }
}
