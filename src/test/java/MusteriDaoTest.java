import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.comu.Fatura;
import org.comu.FaturaDao;
import org.comu.Musteri;
import org.comu.MusteriDao;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class MusteriDaoTest {
    String uniqueAd = "İbrahim_" + UUID.randomUUID();
    String sehir = "Denizli";
    String telefon = "5346544545";
    String dogumTarihi = "12-09-2003";

    //Müşteri nesnesi oluşturuyoruz
    Musteri musteri = new Musteri(uniqueAd,sehir,telefon,dogumTarihi);

    //Veritabanı bağlantıları
    MongoClient client = MongoClients.create("mongodb://localhost:27017");
    MongoDatabase db = client.getDatabase("java_proje");
    MusteriDao dao = new MusteriDao(db);

    @Test
    public void MusteriDaoConnectionTest() {
        assertNotNull(dao, "Fatura Veritabanı kontrolünde veritabanı bağlantı sorunu var.");
        MongoCollection<Document> collection = db.getCollection("musteriler");
        assertNotNull(collection,"Koleksiyon kısmı boş veritabanı adının doğru girildiğine emin ol.");
    }

    @Test
    public void MusteriEkleTest()
    {

        dao.musteriEkle(musteri);

        //Ekelenen müşteriyi bulma durumu
        Document found = db.getCollection("musteriler")
                .find(new Document("`musteri_adi`", uniqueAd))
                .first();
        //Eklenen müşteri gerçekten var mı
        assertNotNull(found, "Müşteri MongoDB'ye eklenemedi.");

        // Temizleme (temiz bir test ortamı için)
        db.getCollection("musteriler").deleteOne(new Document("`musteri_adi`",uniqueAd));
    }

    @Test
    public void MusteriListeleTest()
    {
        List<Musteri> musteriListele = dao.musteriListe();

        assertNotNull(musteriListele,"Veri listeleme de sorun var");
        assertFalse(musteriListele.isEmpty(), "Liste boş, veri gelmedi.");
    }

    @Test
    public void MusteriSilmeTest()
    {
        // Veri ekleme
        dao.musteriEkle(musteri);

        //Ekelenen müşteriyi bulma durumu
        Document found = db.getCollection("musteriler")
                .find(new Document("`musteri_adi`", uniqueAd))
                .first();

        assertNotNull(found);

        String insertedId = found.getObjectId("_id").toHexString();

        // Silme işlemini kontrol etme
        Boolean silindi = dao.musteriSil(insertedId);
        assertTrue(silindi, "Fatura silinemedi");
    }

    @Test
    public void MusteriGuncellemeTest()
    {
        // Fatura oluşturma
        dao.musteriEkle(musteri);

        //Ekelenen müşteriyi bulma durumu
        Document found = db.getCollection("musteriler")
                .find(new Document("`musteri_adi`", uniqueAd))
                .first();
        //Eklenen müşteri gerçekten var mı
        assertNotNull(found, "Müşteri MongoDB'ye eklenemedi.");
        String insertedId = found.getObjectId("_id").toHexString();

        // Veri güncelleme
        Musteri musteri1 = new Musteri("İbrahim",sehir,telefon,dogumTarihi);

        boolean sonuc = dao.musteriGuncelleById(insertedId, musteri1);
        assertTrue(sonuc, "Fatura güncellenemedi");
    }
}
