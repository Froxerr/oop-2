import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.comu.Fatura;
import org.comu.FaturaDao;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class FaturaDoaTest {
    //Fatura sınıfı için gerekli olan bir örnek
    private String barkod = "123456";
    private String tarih = "2024-01-01";
    private ObjectId musteriId = new ObjectId();
    private String durum = "Ödenmedi";
    private String urunAdi = "Laptop";
    private String marka = "Asus";
    private String satinAlmaYeri = "Teknosa";
    private String sonOdeme = "2024-02-01";
    private int kalanTutar = 5000;

    //fatura nesnesini oluşturma işlemi
    Fatura fatura = new Fatura(barkod,tarih,musteriId,durum,urunAdi,marka,satinAlmaYeri,sonOdeme,kalanTutar);

    //Veritabanı bağlantıları
    MongoClient client = MongoClients.create("mongodb://localhost:27017");
    MongoDatabase db = client.getDatabase("java_proje");
    FaturaDao dao = new FaturaDao(db);
    
    @AfterEach
    public void tearDown() {
        // Test verilerini temizle
        db.getCollection("faturalar").deleteOne(new Document("fatura_barkod", "123456"));
        db.getCollection("faturalar").deleteOne(new Document("fatura_barkod", "999999"));
    }

    @Test
    public void FaturaDaoConnectionTest() {
        assertNotNull(dao, "Fatura Veritabanı kontrolünde veritabanı bağlantı sorunu var.");
        MongoCollection<Document> collection = db.getCollection("faturalar");
        assertNotNull(collection,"Koleksiyon kısmı boş veritabanı adının doğru girildiğine emin ol.");
    }

    @Test
    public void FaturaEkleTest()
    {
        dao.faturaEkle(fatura,musteriId);

        // Kontrol: Eklenen fatura gerçekten var mı?
        Document found = db.getCollection("faturalar")
                .find(new Document("fatura_barkod", "123456"))
                .first();

        assertNotNull(found, "Fatura MongoDB'ye eklenemedi.");

        // Not: tearDown metodu test sonrası temizleme işlemini yapacak
    }

    @Test
    public void FaturaListeleTest()
    {
        List<Fatura> faturaListele = dao.faturaListele();

        assertNotNull(faturaListele,"Veri listeleme de sorun var");
        assertFalse(faturaListele.isEmpty(), "Liste boş, veri gelmedi.");
    }

    @Test
    public void FaturaSilmeTest()
    {
        // Veri ekleme
        Fatura fatura = new Fatura("999999", "2024-01-01", new ObjectId(), "Ödenmedi", "Tablet", "Apple", "MediaMarkt", "2024-02-01", 7000);
        dao.faturaEkle(fatura, fatura.getMusteri_id());

        // Eklenen veriyi bulma
        Document inserted = db.getCollection("faturalar")
                .find(Filters.eq("fatura_barkod", "999999"))
                .first();
        assertNotNull(inserted);

        String insertedId = inserted.getObjectId("_id").toHexString();

        // Silme işlemini kontrol etme
        Boolean silindi = dao.faturaSil(insertedId);
        assertTrue(silindi, "Fatura silinemedi");
    }

    @Test
    public void FaturaGuncellemeTest()
    {
        // Fatura oluşturma
        ObjectId musteriId = new ObjectId();
        Fatura orijinalFatura = new Fatura("123456", "2024-01-01", musteriId, "Ödenmedi", "Laptop", "Asus", "Teknosa", "2024-02-01", 5000);
        dao.faturaEkle(orijinalFatura, musteriId);

        // Fatura id alma
        Document inserted = db.getCollection("faturalar")
                .find(Filters.eq("fatura_barkod", "123456"))
                .first();
        assertNotNull(inserted);
        String insertedId = inserted.getObjectId("_id").toHexString();

        // Veri güncelleme
        Fatura guncelFatura = new Fatura("123456", "2024-01-01", musteriId, "Ödendi", "Laptop", "HP", "Vatan", "2024-02-10", 0);

        boolean sonuc = dao.faturaGuncelle(insertedId, guncelFatura);
        assertTrue(sonuc, "Fatura güncellenemedi");
    }
}
