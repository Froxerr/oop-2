import org.bson.types.ObjectId;
import org.comu.Fatura;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class FaturaTest {

    @Test
    public void GetterandSetterTest()
    {
        String barkod = "123456";
        String tarih = "2024-01-01";
        ObjectId musteriId = new ObjectId();
        String durum = "Ödenmedi";
        String urunAdi = "Laptop";
        String marka = "Asus";
        String satinAlmaYeri = "Teknosa";
        String sonOdeme = "2024-02-01";
        int kalanTutar = 5000;

        Fatura fatura = new Fatura(barkod,tarih,musteriId,durum,urunAdi,marka,satinAlmaYeri,sonOdeme,kalanTutar);

        assertEquals(barkod,fatura.getFatura_barkod(),"Barkod da uyuşmazlık var.");
        assertEquals(tarih,fatura.getFatura_tarihi(),"Tarih de uyuşmazlık var");
        assertEquals(musteriId,fatura.getMusteri_id(),"Muşteri Id de uyuşmazlık var");
        assertEquals(durum, fatura.getFatura_durumu(),"Durum da uyuşmazlık var");
        assertEquals(urunAdi, fatura.getFatura_urun_adi(),"Ürün adında uyuşmazlık var");
        assertEquals(marka, fatura.getFatura_urun_markasi(),"Marka da uyuşmazlık var");
        assertEquals(satinAlmaYeri, fatura.getSatin_alma_yeri(),"Satın alma yerinde uyuşmazlık var");
        assertEquals(sonOdeme,fatura.getFatura_son_odeme(),"Son ödeme de uyuşmazlık var");
        assertEquals(kalanTutar, fatura.getKalan_tutar(),"Kalan tutar da uyuşmazlık var");
    }
}
