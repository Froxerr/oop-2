import org.bson.types.ObjectId;
import org.comu.Fatura;
import org.comu.Musteri;
import org.comu.Odeme;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class OdemeTest {

    @Test
    public void GetterandSetterTest()
    {
        ObjectId fatura_id = new ObjectId();
        String odeme_tarihi = "2025-2-05";
        int odenen_tutar = 41;
        String odeme_yontemi = "Paypal";

        Odeme odeme = new Odeme(fatura_id,odeme_tarihi,odenen_tutar,odeme_yontemi);
        assertEquals(fatura_id, odeme.getFatura_id(),"Fatura id'sinde bir uyuşmazlık var");
        assertEquals(odeme_tarihi,odeme.getOdeme_tarihi(), "Odeme tarihinde bir uyuşmazlık var");
        assertEquals(odenen_tutar, odeme.getOdenen_tutar(),"Odenen tutar da bir uyuşmazlık var");
        assertEquals(odeme_yontemi,odeme.getOdeme_yontemi(),"Odeme yönteminde bir uyuşmazlık var");

    }
}
