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
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

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

    public void FrecuenciaEtiquetas(String etiqueta, Integer frecuencia)
    {
        MongoCollection<Document> collection = database.getCollection("etiquetas");

        collection.updateOne(new Document("etiqueta", etiqueta),
                new Document("$set", new Document("frecuencia", frecuencia)));

    }
    public Integer Frecuencia(String palabra){
        ListCollectionsIterable<Document> collections = database.listCollections();
        for (Document var : collections)
        {
            System.out.println(var.getString("name"));
            MongoCollection<Document> collection = database.getCollection(var.getString("name"));

        }
        return 0;
    }
}
