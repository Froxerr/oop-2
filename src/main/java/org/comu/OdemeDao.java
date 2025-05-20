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
import java.util.Collections;
import java.util.List;

public class OdemeDao {
    private MongoCollection<Document> odemeCollection;
    private FaturaDao faturaDao;

    public OdemeDao(MongoDatabase database) {
        this.odemeCollection = database.getCollection("odemeler");
        this.faturaDao = new FaturaDao(database);
    }

    public void OdemeEkleme(Odeme odeme, ObjectId faturaId) {
        // Faturaları taze olarak getir (en güncel verileri almak için)
        List<Fatura> faturalar = faturaDao.faturaListele();
        Fatura ilgiliFatura = null;
        
        for (Fatura fatura : faturalar) {
            if (fatura.getId().equals(faturaId)) {
                ilgiliFatura = fatura;
                break;
            }
        }
        
        if (ilgiliFatura == null) {
            throw new IllegalArgumentException("Belirtilen ID'ye sahip fatura bulunamadı: " + faturaId);
        }
        
        int kalanTutar = ilgiliFatura.getKalan_tutar();
        int odenenTutar = odeme.getOdenen_tutar();
        
        if (odenenTutar <= 0) {
            throw new IllegalArgumentException("Ödeme tutarı sıfırdan büyük olmalıdır");
        }
        
        if (odenenTutar > kalanTutar) {
            throw new IllegalArgumentException("Ödeme tutarı, kalan tutardan büyük olamaz. Kalan tutar: " + kalanTutar);
        }
        
        // Ödemeyi veritabanına ekle
        Document doc = new Document("fatura_id", faturaId)
                .append("odeme_tarihi", odeme.getOdeme_tarihi())
                .append("odenen_tutar", odeme.getOdenen_tutar())
                .append("odeme_yontemi", odeme.getOdeme_yontemi());
        odemeCollection.insertOne(doc);
        
        // Faturanın kalan tutarını hesapla ve güncelle
        int yeniKalanTutar = kalanTutar - odenenTutar;
        ilgiliFatura.setKalan_tutar(yeniKalanTutar);
        
        // Durumunu güncelle
        if (yeniKalanTutar == 0) {
            ilgiliFatura.setFatura_durumu("Ödendi");
        } else {
            ilgiliFatura.setFatura_durumu("Kısmi Ödeme");
        }
        
        // Faturayı veritabanında güncelle
        boolean guncellemeBasarili = faturaDao.faturaGuncelle(ilgiliFatura.getId().toString(), ilgiliFatura);
        if (!guncellemeBasarili) {
            throw new RuntimeException("Fatura güncellenirken hata oluştu. Kalan tutar güncellenemedi.");
        }
    }

    public List<Odeme> odemeListe()
    {
        List<Odeme> odemeListe = new ArrayList<Odeme>();
        for(Document doc : odemeCollection.find())
        {
            Odeme odeme = new Odeme();
            odeme.setId(doc.getObjectId("_id"));
            odeme.setFatura_id(doc.getObjectId("fatura_id"));
            odeme.setOdeme_tarihi(doc.getString("odeme_tarihi"));
            odeme.setOdeme_yontemi(doc.getString("odeme_yontemi"));
            odeme.setOdenen_tutar(doc.getInteger(("odenen_tutar")));
            odemeListe.add(odeme);
        }
        return odemeListe;
    }

    public boolean odemeSil(String id)
    {
        try {
            ObjectId objectId = new ObjectId(id);
            
            // Silinecek ödemeyi bul
            Document odemeDoc = odemeCollection.find(Filters.eq("_id", objectId)).first();
            if (odemeDoc == null) {
                return false;
            }
            
            // Ödeme bilgilerini al
            ObjectId faturaId = odemeDoc.getObjectId("fatura_id");
            int odenenTutar = odemeDoc.getInteger("odenen_tutar");
            
            // Ödemeyi sil
            DeleteResult result = odemeCollection.deleteOne(Filters.eq("_id", objectId));
            
            if (result.getDeletedCount() > 0) {
                // İlgili faturayı bul ve kalan tutarını güncelle
                List<Fatura> faturalar = faturaDao.faturaListele();
                for (Fatura fatura : faturalar) {
                    if (fatura.getId().equals(faturaId)) {
                        // Faturanın kalan tutarını artır
                        int yeniKalanTutar = fatura.getKalan_tutar() + odenenTutar;
                        fatura.setKalan_tutar(yeniKalanTutar);
                        
                        // Durumunu güncelle
                        if (yeniKalanTutar == 0) {
                            fatura.setFatura_durumu("Ödendi");
                        } else {
                            fatura.setFatura_durumu("Kısmi Ödeme");
                        }
                        
                        faturaDao.faturaGuncelle(fatura.getId().toString(), fatura);
                        break;
                    }
                }
            }
            
            return result.getDeletedCount() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean odemeGuncelleme(Odeme odeme, String id)
    {
        ObjectId objectId = new ObjectId(id);
        try {
            // Önce mevcut ödemeyi bul
            Document eskiOdeme = odemeCollection.find(Filters.eq("_id", objectId)).first();
            if (eskiOdeme == null) {
                return false;
            }
            
            // Eski ödeme bilgilerini al
            ObjectId eskiFaturaId = eskiOdeme.getObjectId("fatura_id");
            int eskiOdenenTutar = eskiOdeme.getInteger("odenen_tutar");
            
            // Yeni ödeme bilgileri
            ObjectId yeniFaturaId = odeme.getFatura_id();
            int yeniOdenenTutar = odeme.getOdenen_tutar();
            
            // Faturayı bul
            List<Fatura> faturalar = faturaDao.faturaListele();
            Fatura fatura = null;
            
            for (Fatura f : faturalar) {
                if (f.getId().equals(yeniFaturaId)) {
                    fatura = f;
                    break;
                }
            }
            
            if (fatura == null) {
                throw new IllegalArgumentException("Belirtilen ID'ye sahip fatura bulunamadı");
            }
            
            // Eski fatura ile yeni fatura aynı mı kontrol et
            if (!eskiFaturaId.equals(yeniFaturaId)) {
                // Farklı fatura seçilmişse, eski faturayı güncelle
                for (Fatura eskiFatura : faturalar) {
                    if (eskiFatura.getId().equals(eskiFaturaId)) {
                        // Eski faturaya ödenen tutarı geri ekle
                        int eskiKalanTutar = eskiFatura.getKalan_tutar() + eskiOdenenTutar;
                        eskiFatura.setKalan_tutar(eskiKalanTutar);
                        
                        // Durumunu güncelle
                        if (eskiKalanTutar == 0) {
                            eskiFatura.setFatura_durumu("Ödendi");
                        } else {
                            eskiFatura.setFatura_durumu("Kısmi Ödeme");
                        }
                        
                        faturaDao.faturaGuncelle(eskiFatura.getId().toString(), eskiFatura);
                        break;
                    }
                }
            }
            
            // Yeni faturanın güncel kalan tutarını hesapla
            int kalanTutar;
            if (eskiFaturaId.equals(yeniFaturaId)) {
                // Aynı fatura, sadece ödeme tutarı değişti
                kalanTutar = fatura.getKalan_tutar() + eskiOdenenTutar - yeniOdenenTutar;
            } else {
                // Farklı fatura
                kalanTutar = fatura.getKalan_tutar() - yeniOdenenTutar;
            }
            
            // Kalan tutar negatif olamaz
            if (kalanTutar < 0) {
                throw new IllegalArgumentException("Ödeme tutarı fatura tutarından büyük olamaz");
            }
            
            // Ödemeyi güncelle
            UpdateResult result = odemeCollection.updateOne(Filters.eq("_id", objectId), Updates.combine(
                    Updates.set("fatura_id", odeme.getFatura_id()),
                    Updates.set("odeme_tarihi", odeme.getOdeme_tarihi()),
                    Updates.set("odenen_tutar", odeme.getOdenen_tutar()),
                    Updates.set("odeme_yontemi", odeme.getOdeme_yontemi())
            ));
            
            // Yeni faturanın kalan tutarını ve durumunu güncelle
            fatura.setKalan_tutar(kalanTutar);
            if (kalanTutar == 0) {
                fatura.setFatura_durumu("Ödendi");
            } else {
                fatura.setFatura_durumu("Kısmi Ödeme");
            }
            
            faturaDao.faturaGuncelle(fatura.getId().toString(), fatura);
            
            return result.getModifiedCount() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
