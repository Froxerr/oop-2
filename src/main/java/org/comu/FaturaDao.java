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

public class FaturaDao {
    private MongoCollection<Document> faturaCollection;
    public FaturaDao(MongoDatabase database) {
        this.faturaCollection = database.getCollection("faturalar");
    }

    public void faturaEkle(Fatura fatura, ObjectId musteri_id)
    {
        Document doc = new Document("fatura_barkod", fatura.getFatura_barkod())
                .append("fatura_tarihi",fatura.getFatura_tarihi())
                .append("musteri_id", musteri_id)
                .append("fatura_durumu", fatura.getFatura_durumu())
                .append("fatura_urun_adi", fatura.getFatura_urun_adi())
                .append("fatura_urun_markasi", fatura.getFatura_urun_markasi())
                .append("fatura_son_odeme",fatura.getFatura_son_odeme())
                .append("kalan_tutar", fatura.getKalan_tutar())
                .append("satin_alma_yeri", fatura.getSatin_alma_yeri());
        faturaCollection.insertOne(doc);
    }

    public List<Fatura> faturaListele()
    {
        List<Fatura> faturaListele = new ArrayList<Fatura>();
        for(Document doc : faturaCollection.find())
        {
            Fatura fatura = new Fatura();
            fatura.setId(doc.getObjectId("_id"));
            fatura.setFatura_barkod(doc.getString("fatura_barkod"));
            fatura.setFatura_tarihi(doc.getString("fatura_tarihi"));
            fatura.setMusteri_id(doc.getObjectId("musteri_id"));
            fatura.setFatura_durumu(doc.getString("fatura_durumu"));
            fatura.setFatura_urun_adi(doc.getString("fatura_urun_adi"));
            fatura.setFatura_urun_markasi(doc.getString("fatura_urun_markasi"));
            fatura.setFatura_son_odeme(doc.getString("fatura_son_odeme"));
            fatura.setKalan_tutar(doc.getInteger("kalan_tutar"));
            fatura.setSatin_alma_yeri(doc.getString("satin_alma_yeri"));
            faturaListele.add(fatura);
        }
        return faturaListele;
    }

    public boolean faturaSil(String id)
    {
        try{
            ObjectId Objectid = new ObjectId(id);
            DeleteResult result = faturaCollection.deleteOne(Filters.eq("_id",Objectid));
            return result.getDeletedCount() > 0;
        }catch (Exception e){
            return false;
        }
    }

    public boolean faturaGuncelle(String id, Fatura fatura)
    {
        try {
            ObjectId Objectid = new ObjectId(id);
            UpdateResult result = faturaCollection.updateMany(Filters.eq("_id", Objectid),
                    Updates.combine(
                            Updates.set("fatura_barkod", fatura.getFatura_barkod()),
                            Updates.set("fatura_tarihi", fatura.getFatura_tarihi()),
                            Updates.set("musteri_id", fatura.getMusteri_id()),
                            Updates.set("fatura_durumu", fatura.getFatura_durumu()),
                            Updates.set("fatura_urun_adi", fatura.getFatura_urun_adi()),
                            Updates.set("fatura_urun_markasi", fatura.getFatura_urun_markasi()),
                            Updates.set("fatura_son_odeme", fatura.getFatura_son_odeme()),
                            Updates.set("kalan_tutar", fatura.getKalan_tutar()),
                            Updates.set("satin_alma_yeri", fatura.getSatin_alma_yeri())
                    ));
            return result.getModifiedCount() > 0;
        }catch (Exception e){
            return false;
        }
    }

}
