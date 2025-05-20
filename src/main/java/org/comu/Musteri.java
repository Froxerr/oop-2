package org.comu;

import org.bson.types.ObjectId;

public class Musteri {
    private ObjectId id;
    private String ad;
    private String sehir;
    private String telefon;
    private String dogumTarihi;


    public Musteri() {}
    public Musteri(String ad, String sehir, String telefon, String dogumTarihi) {
        this.ad = ad;
        this.sehir = sehir;
        this.telefon = telefon;
        this.dogumTarihi = dogumTarihi;
    }

    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    public String getAd() {
        return ad;
    }

    public void setAd(String ad) {
        this.ad = ad;
    }

    public String getSehir() {
        return sehir;
    }

    public void setSehir(String sehir) {
        this.sehir = sehir;
    }

    public String getTelefon() {
        return telefon;
    }

    public void setTelefon(String telefon) {
        this.telefon = telefon;
    }

    public String getDogumTarihi() {
        return dogumTarihi;
    }

    public void setDogumTarihi(String dogumTarihi) {
        this.dogumTarihi = dogumTarihi;
    }
}
