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

    public void TotalEtiquetas(String etiqueta, Integer cantidadTotal){
        MongoCollection<Document> collection = database.getCollection("etiquetas");

        //VERIFICAR SI EXISTE LA COLECCION
        Integer cantidad = Math.toIntExact(collection.count());
        if(cantidad == 0){
            database.createCollection("etiquetas");
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

    public Double Frecuencia(String palabra){

        String Palabras[] = palabra.split(" ");
        ListCollectionsIterable<Document> collections = database.listCollections();
        List<Double> probPalabra = new ArrayList<>();
        List<Double> probTotal = new ArrayList<>();
        List<Double> probTotal2 = new ArrayList<>();
        List<String> NombreEtiquetas = new ArrayList<>();
        Double TotalUniverso = 0.0;
        MongoCollection<Document> collectionEtiq = database.getCollection("etiquetas");
        for (Document var : collections)
        {
            String NameEtiqueta = var.getString("name");
            System.out.println(NameEtiqueta);
            MongoCollection<Document> collection = database.getCollection(NameEtiqueta);


            Document query[] = new  Document[Palabras.length];
            Document result[] = new  Document[Palabras.length];
            Document query2 = new Document("etiquta", NameEtiqueta);
            //Document result[] = collection.find(query).first();
            for (int i = 0; i <= Palabras.length-1; i++){
                query[i] = new Document("palabra", Palabras[i]);
                result[i] = collection.find(query[i]).first();

            }
            if(result.length != 0){
                //SI EXISTE - MODIFICA LA CANTIDAD
                if(!NameEtiqueta.equals("etiquetas") && !NameEtiqueta.equals("default")) {

                    Document query3 = new Document("etiqueta", NameEtiqueta);
                    Document resultEtiq = collectionEtiq.find(query3).first();
                    Double TotalEtiqueta = (double)resultEtiq.getInteger("total");
                    Double probabilidad =1.0;
                    for (int i = 0; i <= result.length -1; i++){
                        Double valor = 0.0;
                        if (result[i] == null){
                            valor=0.0;}
                        else {
                             valor = (double) result[i].getInteger("cantidad");
                        }
                        //p(m|e) = frecuencia de mensaje/total de palbras
                        probabilidad = probabilidad * (valor +1/ TotalEtiqueta + 1);
                    }
                    //Double valor = (double)result.getInteger("cantidad");


                    probPalabra.add(probabilidad);
                    probTotal.add(TotalEtiqueta);
                    NombreEtiquetas.add(NameEtiqueta);
                    TotalUniverso +=TotalEtiqueta;
                    System.out.println(probabilidad);
                }
            }
            else{
                if(!NameEtiqueta.equals("etiquetas") && !NameEtiqueta.equals("default")) {
                    Double valor = 0.0;
                    Document query3 = new Document("etiqueta", NameEtiqueta);
                    Document resultEtiq = collectionEtiq.find(query3).first();
                    Double TotalEtiqueta = (double)resultEtiq.getInteger("total");
                    probTotal.add(TotalEtiqueta);
                    NombreEtiquetas.add(NameEtiqueta);
                    TotalUniverso +=TotalEtiqueta;
                    Double probabilidad = (valor / TotalEtiqueta);
                    probPalabra.add(probabilidad);
                    System.out.println(valor);
                }

            }

        }

        for (Double v:probTotal
        ) {
            //p(e) = total equtiqueta/total universo
            probTotal2.add(v + 1/TotalUniverso +1);
            System.out.println(v +1 /TotalUniverso +1);
        }

        //Normalizador
        Double Normalizador = 0.0;
        for (int i = 0; i < probTotal2.size(); i++)
        {
            Normalizador += probPalabra.get(i) * probTotal2.get(i);
        }
        System.out.println("Normlizador");
        System.out.println(Normalizador);

        List<Double> probabilidadFinal = new ArrayList<>();
        Map<Double, String> propFin = new HashMap<>();
        for (int i = 0; i <= probPalabra.size() -1; i++)
        {
            double temp = (probPalabra.get(i)*probTotal2.get(i))/Normalizador;
            System.out.println("valor"+ i);
            System.out.println(temp);
            propFin.put(temp, NombreEtiquetas.get(i));
            probabilidadFinal.add(temp);
        }

        Collections.sort(probabilidadFinal);
        Double key = probabilidadFinal.get(probabilidadFinal.size()-1);

        System.out.println(propFin.get(key));


        return probabilidadFinal.get(probabilidadFinal.size()-1);
    }



}
