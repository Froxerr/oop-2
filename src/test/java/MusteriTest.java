import org.bson.types.ObjectId;
import org.comu.Fatura;
import org.comu.Musteri;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MusteriTest {
    @Test
    public void GetterandSetterTest()
    {
        String ad = "İbrahim";
        String sehir = "Denizli";
        String telefon = "5346544545";
        String dogumTarihi = "12-09-2003";

        Musteri musteri = new Musteri(ad,sehir,telefon,dogumTarihi);
        assertEquals(ad,musteri.getAd(), "Ad da bir uyuşmazlık var");
        assertEquals(sehir,musteri.getSehir(), "Sehir de bir uyuşmazlık var");
        assertEquals(telefon,musteri.getTelefon(), "Telefon da bir uyuşmazlık var");
        assertEquals(dogumTarihi,musteri.getDogumTarihi(), "Doğum tarihinde bir uyuşmazlık var");
    }
}
