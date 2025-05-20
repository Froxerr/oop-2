package org.comu;

import org.bson.types.ObjectId;

public class Fatura {
    private ObjectId id;
    private String fatura_barkod;
    private String fatura_tarihi;
    private ObjectId musteri_id;
    private String fatura_durumu;
    private String fatura_urun_adi;
    private String fatura_urun_markasi;
    private String satin_alma_yeri;
    private String fatura_son_odeme;
    private int kalan_tutar;

    public Fatura() {}
    public Fatura(String fatura_barkod, String fatura_tarihi, ObjectId musteri_id, String fatura_durumu, String fatura_urun_adi, String fatura_urun_markasi,
                  String satin_alma_yeri, String fatura_son_odeme, int kalan_tutar)
    {
        this.fatura_barkod = fatura_barkod;
        this.fatura_tarihi = fatura_tarihi;
        this.musteri_id = musteri_id;
        this.fatura_durumu = fatura_durumu;
        this.fatura_urun_adi = fatura_urun_adi;
        this.fatura_urun_markasi = fatura_urun_markasi;
        this.satin_alma_yeri = satin_alma_yeri;
        this.kalan_tutar = kalan_tutar;
        this.fatura_son_odeme = fatura_son_odeme;
    }

    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    public String getFatura_barkod() {
        return fatura_barkod;
    }

    public void setFatura_barkod(String fatura_barkod) {
        this.fatura_barkod = fatura_barkod;
    }

    public String getFatura_tarihi() {
        return fatura_tarihi;
    }

    public void setFatura_tarihi(String fatura_tarihi) {
        this.fatura_tarihi = fatura_tarihi;
    }

    public ObjectId getMusteri_id() {
        return musteri_id;
    }

    public void setMusteri_id(ObjectId musteri_id) {
        this.musteri_id = musteri_id;
    }

    public String getFatura_durumu() {
        return fatura_durumu;
    }

    public void setFatura_durumu(String fatura_durumu) {
        this.fatura_durumu = fatura_durumu;
    }

    public String getFatura_urun_adi() {
        return fatura_urun_adi;
    }

    public void setFatura_urun_adi(String fatura_urun_adi) {
        this.fatura_urun_adi = fatura_urun_adi;
    }

    public String getFatura_urun_markasi() {
        return fatura_urun_markasi;
    }

    public void setFatura_urun_markasi(String fatura_urun_markasi) {
        this.fatura_urun_markasi = fatura_urun_markasi;
    }

    public String getSatin_alma_yeri() {
        return satin_alma_yeri;
    }

    public void setSatin_alma_yeri(String satin_alma_yeri) {
        this.satin_alma_yeri = satin_alma_yeri;
    }

    public String getFatura_son_odeme() {
        return fatura_son_odeme;
    }

    public void setFatura_son_odeme(String fatura_son_odeme) {
        this.fatura_son_odeme = fatura_son_odeme;
    }

    public int getKalan_tutar() {
        return kalan_tutar;
    }

    public void setKalan_tutar(int kalan_tutar) {
        this.kalan_tutar = kalan_tutar;
    }
}
