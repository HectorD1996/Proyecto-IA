package gt.edu.url;

import com.mongodb.*;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import picocli.CommandLine;


import javax.print.Doc;
import java.io.*;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.util.*;
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
        Map<String, Etiquetas> GuardarPalabras = new HashMap<String, Etiquetas>();
        if(file != null){
            Boolean primeralinea = true;
            BufferedReader bfr = Files.newBufferedReader(file.toPath());
            String line;
            while ((line = bfr.readLine()) != null) {
                //QUITAR SIGNOS DE PUNTUACION Y ESPACIOS DOBLES - CONVERTIR A MINUSCULAS
                line = line.toLowerCase();
                line = line.replaceAll("[!\\\"#$%&'()*+,-./:;<=>?@\\\\[\\\\]^_`{}~]", " ");
                line = line.replaceAll("\\|\\|+", " ");
                line = line.replaceAll("\\s+", " ");

                if(primeralinea == true){
                    line = line.replace("\uFEFF", "");
                    primeralinea = false;
                }

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
                            //conexion.IngresarDato(etiqueta, palabra);
                            Integer Ncantidad = 1;
                            String concatenacion = palabra + etiqueta;
                            //conexion.IngresarDato(etiqueta, palabra);

                            if(GuardarPalabras.containsKey(concatenacion)){
                                //System.out.print("Hasta aqui todo bien");
                                Ncantidad = GuardarPalabras.get(concatenacion).cantidad + 1;
                                Etiquetas ingreso = new Etiquetas(etiqueta, palabra, Ncantidad);
                                GuardarPalabras.replace(concatenacion, ingreso);
                            }
                            else{
                                Etiquetas ingreso = new Etiquetas(etiqueta, palabra, Ncantidad);
                                GuardarPalabras.put(concatenacion, ingreso);
                            }


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

            //INGRESAR A LA BASE DE DATOS
            for(Map.Entry<String, Etiquetas> entry : GuardarPalabras.entrySet()){
                String etiquetaI = entry.getValue().etiqueta;
                String palabraI = entry.getValue().palabra;
                Integer cantidadI = entry.getValue().cantidad;
                conexion.IngresarDatoP(etiquetaI, palabraI, cantidadI);
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
                System.out.print("Para salir ingrese -> exit\n");
                System.out.print("Para borrar la base de datos, ingrese: -> borrar\n");
                System.out.print("Para hacer una inferencia ingrese la cadena -> \n");
                input = scanner.nextLine();
                if(input.equals("exit")){
                    break;
                }
                if((input.toLowerCase()).equals("borrar")){
                    conexion.Borrar();
                }
                Map<String, BigDecimal> comparacion = new HashMap<String, BigDecimal>();
                comparacion = conexion.Frecuencia(input);

                //VERIFICAR MAYOR
                BigDecimal max = BigDecimal.ZERO;
                String maximo = " ";
                for (Map.Entry<String, BigDecimal> sunoo : comparacion.entrySet()){
                    BigDecimal nuevo = new BigDecimal(sunoo.getValue().toString());
                    if(nuevo.compareTo(max) == 1) //si nuevo es mayor a
                    {
                        max = nuevo;
                        maximo = sunoo.getKey();
                    }
                }

                System.out.println("Mayor probabilidad de pertenecer a: " + maximo + " con la probabilidad: " + max);
            }

            return 0;
        }else {

            System.out.print("Para salir ingrese -> exit\n");
            System.out.print("Para borrar la base de datos, ingrese: -> borrar\n");
            System.out.print("Para hacer una inferencia ingrese la cadena -> \n");
            Scanner scanner = new Scanner(System.in);
            String input = "";
            while(!input.equals("exit")){
                System.out.print("Para salir ingrese -> exit\n");
                System.out.print("Para borrar la base de datos, ingrese: -> borrar\n");
                System.out.print("Para hacer una inferencia ingrese la cadena -> \n");
                input = scanner.nextLine();
                if((input.toLowerCase()).equals("exit")){
                    break;
                }
                if((input.toLowerCase()).equals("borrar")){
                    conexion.Borrar();
                }
                Map<String, BigDecimal> comparacion = new HashMap<String, BigDecimal>();
                comparacion = conexion.Frecuencia(input);

                //VERIFICAR MAYOR
                BigDecimal max = BigDecimal.ZERO;
                String maximo = " ";
                for (Map.Entry<String, BigDecimal> sunoo : comparacion.entrySet()){
                    BigDecimal nuevo = new BigDecimal(sunoo.getValue().toString());
                    if(nuevo.compareTo(max) == 1) //si nuevo es mayor a
                    {
                        max = nuevo;
                        maximo = sunoo.getKey();
                    }
                }

                System.out.println("Mayor probabilidad de pertenecer a: " + maximo + " con la probabilidad: " + max);
            }
            return 0;
        }
    }


    public static void main( String[] args ) throws IOException {
        /*File archivo = new File ("C:\\Users\\HP\\Documents\\PruebasPro\\Proyecto-IA\\proyecto-final\\prueba.txt");
        FileReader fr = new FileReader(archivo);
        BufferedReader bfr = new BufferedReader(fr);
        String line;
        while ((line = bfr.readLine().toString()) != null) {
            line = line.toLowerCase();
            line = line.replaceAll("[!\\\"#$%&'()*+,-./:;<=>?@\\\\[\\\\]^_`{}~]", " ");
            line = line.replaceAll("\\|\\|+", " ");
            line = line.replaceAll("\\s+", " ");

            //System.out.println(total_personas);

            String[] partes = line.split("\\|");

            //PARTES DE LA PRIMERA POSICION DE PARTES
            String[] N = (partes[0].toString()).split(" ");


            //  AQUI EMPIEZA LO NUEVO

            Map<String, Etiquetas> GuardarPalabras = new HashMap<String, Etiquetas>();
            Map<String, Integer> GuardarAyuda = new HashMap<String, Integer>();
            String concatenacion = " ";
            for(int i = 0; i<N.length; i++){
                line = line.replace("\uFEFF", "");
                String palabra = N[i].toString();
                //LEEMOS POR ETIQUETA
                for(int j = 1; j<= partes.length - 1; j++ ){
                    if(partes[j] != ""){
                        String etiqueta = partes[j];
                        Integer id = 0;
                        Integer Ncantidad = 1;

                        String llave1 = concatenacion;
                        concatenacion= palabra + etiqueta;
                        if(llave1.equals(concatenacion)){
                            System.out.print("son iguales");
                        }
                        //conexion.IngresarDato(etiqueta, palabra);

                        if(GuardarPalabras.containsKey(concatenacion)){
                            Ncantidad = GuardarPalabras.get(concatenacion).cantidad + 1;
                            Etiquetas ingreso = new Etiquetas(etiqueta, palabra, Ncantidad);
                            GuardarPalabras.replace(concatenacion, ingreso);
                        }
                        else{
                            Etiquetas ingreso = new Etiquetas(etiqueta, palabra, Ncantidad);
                            GuardarPalabras.put(concatenacion, ingreso);
                        }


                    }
                }
            }
        }*/



    /*    CConexion conexion = new CConexion();
        conexion.crearConexionC();
        Map<String, BigDecimal> comparacion = new HashMap<String, BigDecimal>();
        String ora = "secret is secret";
        ora = ora.toLowerCase();
        ora = ora.replaceAll("[!\\\"#$%&'()*+,-./:;<=>?@\\\\[\\\\]^_`{}~]", " ");
        ora = ora.replaceAll("\\s+", " ");
        comparacion = conexion.Frecuencia(ora);

        //VERIFICAR MAYOR
        BigDecimal max = BigDecimal.ZERO;
        String maximo = " ";
        for (Map.Entry<String, BigDecimal> sunoo : comparacion.entrySet()){
            BigDecimal nuevo = new BigDecimal(sunoo.getValue().toString());
            if(nuevo.compareTo(max) == 1) //si nuevo es mayor a
                {
                max = nuevo;
                maximo = sunoo.getKey();
            }
        }


        System.out.println("Mayor probabilidad de pertenecer a: " + maximo + " con la probabilidad: " + max);*/
        int exitCode = new CommandLine(new App()).execute(args);
        System.exit(exitCode);
    }
}
