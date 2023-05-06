package gt.edu.url;

import com.mongodb.*;
import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.ListCollectionsIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Aggregates.*;
import org.bson.Document;

import javax.print.Doc;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

import static com.mongodb.client.model.Filters.*;

public class CConexion {
    MongoClient mongo = null;
    MongoDatabase database = null;
    //CREAR CONEXION CON MONGO
    public MongoClient crearConexionC(){

        String servidor = "localhost";
        Integer puerto = 27017;
        try{
            mongo = new MongoClient(servidor, puerto);
            database = mongo.getDatabase("dbmongo");
        }catch(MongoException e) {
            System.out.println("Error en la conexion " + e.toString());
        }
        return mongo;
    }

    public void IngresarDato(String etiqueta, String palabra){
        //LLAMAR A LA COLECCION
        MongoCollection<Document> collection = database.getCollection(etiqueta);

        //VERIFICAR SI EXISTE LA COLECCION
        Integer cantidad = Math.toIntExact(collection.count());

        if(cantidad == 0){
            //SI NO EXISTE - LA CREA
            database.createCollection(etiqueta);
            //INGRESA LA PALABRA
            Document ingreso = new Document()
                    .append("palabra",palabra)
                    .append("cantidad", 1);
            collection.insertOne(ingreso);
        }
        else{
            //BUSCAR LA PALABRA
            Document query = new Document("palabra", palabra);
            Document result = collection.find(query).first();

            //VERIFICA SI EXISTE O NO
            if(result != null){
                //SI EXISTE - MODIFICA LA CANTIDAD
                Integer valor = result.getInteger("cantidad");
                collection.updateOne(new Document("palabra", palabra),
                        new Document("$set", new Document("cantidad", valor + 1)));
            }
            else{
                //LA PALABRA NO EXISTE - LA INGRESA
                Document ingreso = new Document()
                        .append("palabra",palabra)
                        .append("cantidad", 1);
                collection.insertOne(ingreso);
            }
        }

    }

    public Integer Total_Oraciones(String etiqueta){
        Document query = new Document("etiqueta", etiqueta);
        Document result = database.getCollection("etiquetas").find(query).first();
        Integer total = 0;
        if(result != null){
            total = result.getInteger("frecuencia");
        }

        return total;
    }

    public void TotalEtiquetas(String etiqueta, Integer cantidadTotal){
        MongoCollection<Document> collection = database.getCollection("etiquetas");

        //VERIFICAR SI EXISTE LA COLECCION
        Integer cantidad = Math.toIntExact(collection.count());
        if(cantidad == 0){
            //database.createCollection("etiquetas");
            //VERIFICAR SI EXISTE LA ETIQUETA PREVIAMENTE
            Document ingreso = new Document()
                    .append("id", cantidad + 1)
                    .append("etiqueta",etiqueta)
                    .append("total", cantidadTotal);
            collection.insertOne(ingreso);
        }
        else{
            Document query = new Document("etiqueta", etiqueta);
            Document result = collection.find(query).first();
            if(result != null){
                Integer valor = result.getInteger("total");
                collection.updateOne(new Document("etiqueta", etiqueta),
                        new Document("$set", new Document("total", valor + cantidadTotal)));
            }else{
                Document ingreso = new Document()
                        .append("id", cantidad + 1)
                        .append("etiqueta",etiqueta)
                        .append("total", cantidadTotal);
                collection.insertOne(ingreso);
            }
        }
    }

    public void FrecuenciaEtiquetas(String etiqueta, Integer frecuencia)
    {
        MongoCollection<Document> collection = database.getCollection("etiquetas");

        collection.updateOne(new Document("etiqueta", etiqueta),
                new Document("$set", new Document("frecuencia", frecuencia)));

    }

    public Map<String, BigDecimal> Frecuencia(String oracion){
        //BORRAR
        ListCollectionsIterable<Document> collections = database.listCollections();

        //INICIALIZAR VARIABLES
        MongoCollection<Document> Coleccion_etiquetas = database.getCollection("etiquetas");
        Integer TotalOracionesEtiquetas = 0;
        Map<String, BigDecimal> InferenciaTotales = new HashMap<String, BigDecimal>();

        //VER CANTIDAD TOTAL DE ORACIONES - SE GUARDA EN valorM
        for (Document etiqueta_uni : collections){
            String universo = etiqueta_uni.getString("name");
            TotalOracionesEtiquetas += Total_Oraciones(universo);
        }

        //SEPARA LA ORACION
        oracion = oracion.toLowerCase();
        oracion = oracion.replaceAll("[!\\\"#$%&'()*+,-./:;<=>?@\\\\[\\\\]^_`{}~]", " ");
        oracion = oracion.replaceAll("\\s+", " ");
        String[] palabras = (oracion.split(" "));

        Integer cantidad_etiquetas = Math.toIntExact(Coleccion_etiquetas.count());

        //EMPEZAR A CALCULAR LAS INFERENCIAS PARA TODAS LAS ETIQUETAS
        for(int i = 1; i<= cantidad_etiquetas; i++){
            //NUMERADOR PARA CALCULAR INFERENCIA
            //INICIALIZAR VARIABLES
            double denominador = 0.0;
            double numerador = 0.0;
            double multiplicacion = 1.0;
            double multiplicacion_proba = 1.0;

            //VER LA CANTIDAD DE ORACIONES PARA LA ETIQUETA ACTUAL - SE GUARDA EN total_etiqueta_base
            Document pregunta_base = new Document("id", i);
            Document resultado_etiqueta_base = Coleccion_etiquetas.find(pregunta_base).first();

            //OBTENER LA CANTIDAD DE ORACIONES PARA ESA ETIQUETA
            Integer total_oraciones_etiqueta = resultado_etiqueta_base.getInteger("frecuencia");
            //OBTENER LA CANTIDAD DE PALABRAS EN ESA ETIQUETA
            Integer total_etiqueta_base = resultado_etiqueta_base.getInteger("total");
            //OBTENER EL NOMBRE DE LA ETIQUETA
            String nombre_etiqueta = resultado_etiqueta_base.getString("etiqueta");

            //AQUI SE CALCULA P(ETIQUETA)
            double p_numerador_etiqueta =  (double)(total_oraciones_etiqueta) / (TotalOracionesEtiquetas);

            //AQUI EMPIEZA EL CALCULO DE P(PALABRA|ETIQUETA)
            MongoCollection<Document> coleccion_Base = database.getCollection(nombre_etiqueta);

            //AQUI SE CALCULA P(PALABRA|ETIQUETA)
            for(int j = 0; j < palabras.length; j++){
                //SE BUSCA LA PALABRA EN LA BASE DE DATOS
                Document query_3 = new Document("palabra", palabras[j].toString());
                Document result3 = coleccion_Base.find(query_3).first();
                Integer total_palabra_etiqueta = 0;
                //SI EXISTE LA PALABRA EN ESA ETIQUETA - SI NO EXISTE QUEDA EN 0
                if(result3 != null){
                    total_palabra_etiqueta = result3.getInteger("cantidad");
                }

                //AQUI ES EL CALCULO DE P(PALABRA|ETIQUETA)
                double prob_base = (double)(total_palabra_etiqueta)/ (total_etiqueta_base);
                //AQUI VOY MULTIPLICANDO P(PALABRA|ETIQUETA) * P(PALABRA|ETIQUETA) * P(PALABRA|ETIQUETA) ....
                multiplicacion = prob_base * multiplicacion;
            }
            //AQUI SE CALCULA EL NUMERADOR P(ORACION|ETIQUETA)*P(ETIQUETA)
            numerador = (multiplicacion * p_numerador_etiqueta);

            for(int partes_denominador = 1; partes_denominador<=cantidad_etiquetas; partes_denominador++){
                // CANTIDAD DE PALABRAS POR ETIQUETA
                //INICIALIZAMOS
                multiplicacion_proba = 1.0;
                Document query100 = new Document("id", partes_denominador);
                Document result= Coleccion_etiquetas.find(query100).first();

                //TOTAL DE ORACIONES EN ESA ETIQUETA PARA P(ETIQUETA)
                Integer total_oraciones_sub_etiqueta = result.getInteger("frecuencia");
                //TOTAL DE PALABRAS EN ESA ETIQUETA
                Integer total_palabras_sub_etiqueta = result.getInteger("total");
                //NOMBRE DE ESA ETIQUETA
                String nombre_sub_etiqueta = result.getString("etiqueta");

                //AQUI SE CALCULA P(PALABRA|ETIQUETA)
                for(int j = 0; j< palabras.length; j++){
                    //SE BUSCA LA PALABRA EN LA BASE DE DATOS
                    MongoCollection<Document> collection = database.getCollection(nombre_sub_etiqueta);
                    Document query2 = new Document("palabra", palabras[j]);
                    Document result2 = collection.find(query2).first();

                    //TOTAL DE VECES QUE SE REPITE LA PALABRA EN ESA ETIQUETA
                    Integer total_sub_palabra = 0;
                    if(result2 != null){
                        total_sub_palabra = result2.getInteger("cantidad");
                    }

                    //AQUI ES EL CALCULO DE P(PALABRA|ETIQUETA)
                    double probabilidad = (double)(total_sub_palabra)/ (total_palabras_sub_etiqueta);
                    //AQUI VOY MULTIPLICANDO P(PALABRA|ETIQUETA) * P(PALABRA|ETIQUETA) * P(PALABRA|ETIQUETA) ....
                    multiplicacion_proba = multiplicacion_proba * probabilidad;
                }

                //AQUI SE CALCULA P(ETIQUETA)
                double p_denominador_etiqueta = (double)(total_oraciones_sub_etiqueta)/ (TotalOracionesEtiquetas);

                //AQUI SE CALCULA P(PALABRA|ETIQUETA)*P(ETIQUETA)
                double resultado_probabilidad = multiplicacion_proba * p_denominador_etiqueta;
                //AQUI SE CALCULA P(PALABRA|ETIQUETA)*P(ETIQUETA) + P(PALABRA|ETIQUETA)*P(ETIQUETA)...
                denominador += resultado_probabilidad;


            }

            //CALCULAR TOTAL
            BigDecimal totaldecimal;
            totaldecimal = new BigDecimal(numerador).divide(new BigDecimal(denominador), 6, RoundingMode.HALF_UP);//cantidad de decimales de la solucion

            double total = numerador / denominador;
            InferenciaTotales.put(nombre_etiqueta, totaldecimal);
            System.out.println(nombre_etiqueta + " " + totaldecimal);
        }
        return InferenciaTotales;

    }

}
