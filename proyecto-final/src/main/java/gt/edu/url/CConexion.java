package gt.edu.url;

import com.mongodb.*;
import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

import javax.print.Doc;
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


}
