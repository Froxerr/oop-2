package org.comu;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.List;

public class MusteriDao {
    private MongoCollection<Document> musteriCollection;
    public MusteriDao(MongoDatabase database)
    {
        this.musteriCollection = database.getCollection("musteriler");
    }

    public void musteriEkle(Musteri musteri)
    {
        Document doc = new Document("`musteri_adi`",musteri.getAd())
                .append("`musteri_sehir`",musteri.getSehir())
                .append("`musteri_telefon`",musteri.getTelefon())
                .append("`musteri_dogum_tarihi`",musteri.getDogumTarihi());
        musteriCollection.insertOne(doc);
    }

    public List<Musteri> musteriListe()
    {
        List<Musteri> musteriListe = new ArrayList<>();
        for(Document doc : musteriCollection.find())
        {
            Musteri musteri = new Musteri();
            musteri.setId(doc.getObjectId("_id"));
            musteri.setAd(doc.getString("`musteri_adi`"));
            musteri.setSehir(doc.getString("`musteri_sehir`"));
            musteri.setTelefon(doc.getString("`musteri_telefon`"));
            musteri.setDogumTarihi(doc.getString("`musteri_dogum_tarihi`"));
            musteriListe.add(musteri);
        }
        return musteriListe;
    }

    public boolean musteriSil(String id)
    {
        try{
        ObjectId objectId =  new ObjectId(id);
        DeleteResult result = musteriCollection.deleteOne(Filters.eq("_id",objectId));
        return result.getDeletedCount() > 0;
        }catch (Exception e)
        {
            return false;
        }
    }
    
    public boolean musteriGuncelleById(String id, Musteri musteri)
    {
        try {
            ObjectId objectId = new ObjectId(id);
            UpdateResult result = musteriCollection.updateOne(Filters.eq("_id", objectId),
                    Updates.combine(
                            Updates.set("`musteri_adi`", musteri.getAd()),
                            Updates.set("`musteri_sehir`", musteri.getSehir()),
                            Updates.set("`musteri_telefon`", musteri.getTelefon()),
                            Updates.set("`musteri_dogum_tarihi`", musteri.getDogumTarihi())
                    ));
            return result.getModifiedCount() > 0;
        }catch (Exception e)
        {
            return false;
        }
    }
}
