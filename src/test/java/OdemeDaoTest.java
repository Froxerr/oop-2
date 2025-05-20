import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.comu.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class OdemeDaoTest {
    // Test için kullanılacak değişkenler
    private ObjectId fatura_id;
    private ObjectId musteri_id;
    private String odeme_tarihi = "2025-2-05";
    private int odenen_tutar = 41;
    private String unique_odeme_yontemi = "Paypal_"+ UUID.randomUUID();
    private Odeme odeme;
    private Fatura testFatura;

    // Veritabanı bağlantıları
    private MongoClient client = MongoClients.create("mongodb://localhost:27017");
    private MongoDatabase db = client.getDatabase("java_proje"); // Doğru veritabanı adı
    private OdemeDao dao = new OdemeDao(db);
    private FaturaDao faturaDao = new FaturaDao(db);

    @BeforeEach
    public void setUp() {
        // Her test öncesinde yeni bir test faturası oluştur
        testFatura = new Fatura();
        testFatura.setFatura_barkod("TEST_" + UUID.randomUUID().toString().substring(0, 8));
        testFatura.setFatura_tarihi("2025-01-01");
        testFatura.setFatura_durumu("Ödenmedi");
        testFatura.setFatura_urun_adi("Test Ürün");
        testFatura.setFatura_urun_markasi("Test Marka");
        testFatura.setFatura_son_odeme("2025-12-31");
        testFatura.setKalan_tutar(100); // Test için 100 birim tutar
        testFatura.setSatin_alma_yeri("Test Yer");
        
        // Veritabanına faturayı ekle ve ID'sini al
        musteri_id = new ObjectId(); // Test için geçici bir müşteri ID'si
        faturaDao.faturaEkle(testFatura, musteri_id);
        
        // Eklenen faturayı bul
        List<Fatura> faturalar = faturaDao.faturaListele();
        for (Fatura fatura : faturalar) {
            if (fatura.getFatura_barkod().equals(testFatura.getFatura_barkod())) {
                fatura_id = fatura.getId();
                testFatura = fatura;
                break;
            }
        }
        
        // Test için ödeme nesnesini oluştur
        odeme = new Odeme(fatura_id, odeme_tarihi, odenen_tutar, unique_odeme_yontemi);
    }

    @AfterEach
    public void tearDown() {
        // Test verilerini temizle
        db.getCollection("odemeler").deleteMany(new Document("odeme_yontemi", unique_odeme_yontemi));
        
        // Oluşturulan test faturasını sil
        if (fatura_id != null) {
            faturaDao.faturaSil(fatura_id.toString());
        }
    }

    @Test
    public void OdemeDaoConnectionTest() {
        assertNotNull(dao, "Ödemeler Veritabanı kontrolünde veritabanı bağlantı sorunu var.");
        MongoCollection<Document> collection = db.getCollection("odemeler");
        assertNotNull(collection,"Koleksiyon kısmı boş veritabanı adının doğru girildiğine emin ol.");
    }

    @Test
    public void OdemeEkleTest() {
        // Test öncesi faturanın kalan tutarını al
        int initialKalanTutar = testFatura.getKalan_tutar();
        
        // Ödeme ekle
        dao.OdemeEkleme(odeme, fatura_id);

        // Kontrol: Eklenen ödeme gerçekten var mı?
        Document found = db.getCollection("odemeler")
                .find(new Document("odeme_yontemi", unique_odeme_yontemi))
                .first();
        assertNotNull(found, "Ödeme MongoDB'ye eklenemedi.");
        
        // Fatura kalan tutarının güncellenmesini kontrol et
        Fatura updatedFatura = findFaturaById(fatura_id);
        assertNotNull(updatedFatura, "Güncellenmiş fatura bulunamadı");
        
        assertEquals(initialKalanTutar - odenen_tutar, updatedFatura.getKalan_tutar(), 
                "Fatura kalan tutarı doğru güncellenmedi");
        
        // Kalan tutar 0 değilse "Kısmi Ödeme" durumunu kontrol et
        if (updatedFatura.getKalan_tutar() > 0) {
            assertEquals("Kısmi Ödeme", updatedFatura.getFatura_durumu(), 
                    "Fatura kısmi ödendiğinde durum 'Kısmi Ödeme' olmalı");
        } else {
            assertEquals("Ödendi", updatedFatura.getFatura_durumu(),
                    "Fatura tam ödendiğinde durum 'Ödendi' olmalı");
        }
    }

    @Test
    public void OdemeListeleTest() {
        // Test verisi oluştur
        dao.OdemeEkleme(odeme, fatura_id);
        
        List<Odeme> odemeListele = dao.odemeListe();
        assertNotNull(odemeListele, "Veri listeleme de sorun var");
        assertFalse(odemeListele.isEmpty(), "Veri listeleme de sorun var");
        
        // Test için eklenen ödemeyi bul
        boolean found = false;
        for (Odeme ode : odemeListele) {
            if (ode.getOdeme_yontemi().equals(unique_odeme_yontemi)) {
                found = true;
                break;
            }
        }
        
        assertTrue(found, "Eklenen ödeme listede bulunamadı");
    }

    @Test
    public void OdemeSilmeTest() {
        // Veri ekleme
        dao.OdemeEkleme(odeme, odeme.getFatura_id());

        // Eklenen veriyi bulma
        Document inserted = db.getCollection("odemeler")
                .find(Filters.eq("odeme_yontemi", unique_odeme_yontemi))
                .first();
        assertNotNull(inserted);

        String insertedId = inserted.getObjectId("_id").toHexString();
        
        // Faturanın güncellenmiş kalan tutarını al
        Fatura updatedFatura = findFaturaById(fatura_id);
        int updatedKalanTutar = updatedFatura.getKalan_tutar();
        
        // Silme işlemini kontrol etme
        Boolean silindi = dao.odemeSil(insertedId);
        assertTrue(silindi, "Ödeme silinemedi");
        
        // Fatura kalan tutarının geri döndüğünü kontrol et
        Fatura restoredFatura = findFaturaById(fatura_id);
        assertEquals(updatedKalanTutar + odenen_tutar, restoredFatura.getKalan_tutar(), 
                "Ödeme silindiğinde fatura kalan tutarı doğru güncellenemedi");
    }

    @Test
    public void OdemeGuncellemeTest() {
        // Odeme oluşturma
        dao.OdemeEkleme(odeme, odeme.getFatura_id());

        // Eklenen veriyi bulma
        Document inserted = db.getCollection("odemeler")
                .find(Filters.eq("odeme_yontemi", unique_odeme_yontemi))
                .first();
        assertNotNull(inserted);

        String insertedId = inserted.getObjectId("_id").toHexString();
        
        // Faturanın güncellenmiş kalan tutarını al
        Fatura fatura = findFaturaById(fatura_id);
        int updatedKalanTutar = fatura.getKalan_tutar();
        
        // Ödeme tutarını değiştir (kalanın yarısı kadar)
        int yeniOdenenTutar = 30; // Kalanın yarısını öde
        
        // Veri güncelleme
        Odeme guncelOdeme = new Odeme(fatura_id, odeme_tarihi, yeniOdenenTutar, "Guncel_"+UUID.randomUUID());

        boolean sonuc = dao.odemeGuncelleme(guncelOdeme, insertedId);
        assertTrue(sonuc, "Ödeme güncellenemedi");
        
        // Fatura kalan tutarının doğru güncellendiğini kontrol et
        Fatura updatedFaturaAfterChange = findFaturaById(fatura_id);
        assertEquals(100 - yeniOdenenTutar, updatedFaturaAfterChange.getKalan_tutar(), 
                "Ödeme güncellendiğinde fatura kalan tutarı doğru güncellenmedi");
    }
    
    // Yardımcı metod: Fatura ID'sine göre faturayı bul
    private Fatura findFaturaById(ObjectId id) {
        List<Fatura> faturalar = faturaDao.faturaListele();
        for (Fatura fatura : faturalar) {
            if (fatura.getId().equals(id)) {
                return fatura;
            }
        }
        return null;
    }
}
