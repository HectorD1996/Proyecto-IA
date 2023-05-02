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

    public Integer Total_universo(String etiqueta){
        Document query = new Document("etiqueta", etiqueta);
        Document result = database.getCollection("etiquetas").find(query).first();
        Integer total = 0;
        if(result != null){
            total = result.getInteger("total");
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
                        .append("etiqueta",etiqueta)
                        .append("total", cantidadTotal);
                collection.insertOne(ingreso);
            }
        }
    }

    public Map<String, Double> Frecuencia(String oracion){
        ListCollectionsIterable<Document> collections = database.listCollections();
        MongoCollection<Document> base = database.getCollection("etiquetas");
        Integer valorM = 0;
        //VER CANTIDAD TOTAL DEL UNIVERSO
        for (Document etiqueta_uni : collections){
            String universo = etiqueta_uni.getString("name");
            valorM += Total_universo(universo);
        }

        Map<String, Double> totales = new HashMap<String, Double>();

        String[] palabras = (oracion.split(" "));

        for (Document etiqueta_base : collections)
        {
            //VER LA PRINCIPAL EN ESE MOMENTO
            double denominador = 0.0;
            double numerador = 0.0;
            double multiplicacion = 1.0;
            double multiplicacion_proba = 1.0;
            String Base_Actual = etiqueta_base.getString("name");
            //VER LA CANTIDAD DE LA ETIQUETA BASE
            Document pregunta_base = new Document("etiqueta", Base_Actual);
            Document resultado_etiqueta_base = base.find(pregunta_base).first();
            if(resultado_etiqueta_base != null){
                Integer total_etiqueta_base = resultado_etiqueta_base.getInteger("total");
                double marginalizado_base =  (double)(total_etiqueta_base + 1) / (valorM + 1);
                MongoCollection<Document> coleccion_Base = database.getCollection(Base_Actual);
                for(int i = 0; i < palabras.length; i++){
                    Document query_3 = new Document("palabra", palabras[i].toString());
                    Document result3 = coleccion_Base.find(query_3).first();
                    Integer total_base_palabra = 0;
                    if(result3 != null){
                        total_base_palabra = result3.getInteger("cantidad");
                    }

                    double prob_base = (double)(total_base_palabra + 1)/ (total_etiqueta_base + 1);
                    multiplicacion = prob_base * multiplicacion;
                }
                numerador = (multiplicacion * marginalizado_base);
                //denominador = numerador;

                for(Document sub_etiqueta : collections){
                    String Sub_Actual = sub_etiqueta.getString("name");
                    if(!(Sub_Actual.equals("etiquetas")) && !(Sub_Actual.equals("default"))){
                        // CANTIDAD DE PALABRAS POR ETIQUETA
                        Document query = new Document("etiqueta", Sub_Actual);
                        Document result = base.find(query).first();
                        multiplicacion_proba = 1.0;
                        if(result != null){
                            Integer total_sub_etiqueta = result.getInteger("total");
                            //CANTIDAD DE VECES QUE ESTA LA PALABRA EN ESA ETIQUETA
                            for(int j = 0; j< palabras.length; j++){
                                MongoCollection<Document> collection = database.getCollection(Sub_Actual);
                                Document query2 = new Document("palabra", palabras[j]);
                                Document result2 = collection.find(query2).first();
                                Integer total_sub_palabra = 0;
                                if(result2 != null){
                                    total_sub_palabra = result2.getInteger("cantidad");
                                    //CALCULAR FRECUENCIAS PARA ESA MARGINALIZACION

                                }
                                double probabilidad = (double)(total_sub_palabra + 1)/ (total_sub_etiqueta + 1);
                                multiplicacion_proba = multiplicacion_proba * probabilidad;

                            }
                            double marginalizado = (double)(total_sub_etiqueta + 1)/ (valorM +1);

                            //CALCULAR ESA SUMA
                            double resultado_probabilidad = multiplicacion_proba * marginalizado;
                            denominador += resultado_probabilidad;
                        }
                    }
                }

                //CALCULAR TOTAL
                double total = numerador / denominador;
                totales.put(Base_Actual, total);
                System.out.println(Base_Actual + " " + total);
            }

        }
        return totales;
    }

}
