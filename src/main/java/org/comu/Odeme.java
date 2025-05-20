package org.comu;

import org.bson.types.ObjectId;

public class Odeme {
    private ObjectId id;
    private ObjectId fatura_id;
    private String odeme_tarihi;
    private int odenen_tutar;
    private String odeme_yontemi;

    public Odeme(){}
    public Odeme(ObjectId fatura_id, String odeme_tarihi, int odenen_tutar, String odeme_yontemi) {
        this.fatura_id = fatura_id;
        this.odeme_tarihi = odeme_tarihi;
        this.odenen_tutar = odenen_tutar;
        this.odeme_yontemi = odeme_yontemi;
    }

    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    public ObjectId getFatura_id() {
        return fatura_id;
    }

    public void setFatura_id(ObjectId fatura_id) {
        this.fatura_id = fatura_id;
    }

    public String getOdeme_tarihi() {
        return odeme_tarihi;
    }

    public void setOdeme_tarihi(String odeme_tarihi) {
        this.odeme_tarihi = odeme_tarihi;
    }

    public int getOdenen_tutar() {
        return odenen_tutar;
    }

    public void setOdenen_tutar(int odenen_tutar) {
        this.odenen_tutar = odenen_tutar;
    }

    public String getOdeme_yontemi() {
        return odeme_yontemi;
    }

    public void setOdeme_yontemi(String odeme_yontemi) {
        this.odeme_yontemi = odeme_yontemi;
    }
}
