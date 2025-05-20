package org.comu;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.bson.types.ObjectId;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class MusteriGoruntule extends JPanel {
    private MusteriDao musteriDao;
    private JTable table;
    private DefaultTableModel model;
    
    // Input alanları
    private JTextField txtAd, txtSehir, txtTelefon, txtDogumTarihi;
    
    // Butonlar
    private JButton btnEkle, btnGuncelle, btnSil;
    
    // Seçili müşteri ID
    private String seciliMusteriId = "";

    public MusteriGoruntule()
    {
        // Mongo bağlantısı
        MongoClient client = MongoClients.create("mongodb://localhost:27017");
        MongoDatabase database = client.getDatabase("java_proje");
        musteriDao = new MusteriDao(database);

        setLayout(new BorderLayout());

        String[] kolonlar = {"ID", "Adı", "Şehri", "Telefon", "Doğum Tarihi"};

        model = new DefaultTableModel(kolonlar, 0);
        table = new JTable(model);

        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane, BorderLayout.CENTER);
        
        // Input alanlarını oluştur
        JPanel inputPanel = new JPanel(new GridLayout(2, 4, 5, 5)); //2 satır 4 sütundan oluştur ve 5 boşluk bırak
        inputPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); //Her yerden 10 margin bırakıyormuşsun gibi düşün
        
        // Ad kısmını oluştur ekle
        inputPanel.add(new JLabel("Ad:"));
        txtAd = new JTextField(10);
        inputPanel.add(txtAd);
        
        // Şehir
        inputPanel.add(new JLabel("Şehir:"));
        txtSehir = new JTextField(10);
        inputPanel.add(txtSehir);
        
        // Telefon
        inputPanel.add(new JLabel("Telefon:"));
        txtTelefon = new JTextField(10);
        inputPanel.add(txtTelefon);
        
        // Doğum Tarihi
        inputPanel.add(new JLabel("Doğum Tarihi:"));
        txtDogumTarihi = new JTextField(10);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        txtDogumTarihi.setText(dateFormat.format(new Date())); // Bugünün tarihi
        inputPanel.add(txtDogumTarihi);
        
        // Butonlar için panel oluştur
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
        
        btnEkle = new JButton("Ekle");
        btnGuncelle = new JButton("Güncelle");
        btnSil = new JButton("Sil");
        
        buttonPanel.add(btnEkle);
        buttonPanel.add(btnGuncelle);
        buttonPanel.add(btnSil);
        
        // Panel oluştur ve tüm alt panelleri yerleştir
        JPanel southPanel = new JPanel(new BorderLayout());
        southPanel.add(inputPanel, BorderLayout.CENTER);
        southPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        // Panel'i ana panele ekle
        add(southPanel, BorderLayout.SOUTH);
        
        // Tablodan seçim yapıldığında inputları doldur
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && table.getSelectedRow() != -1) {
                int row = table.getSelectedRow();
                seciliMusteriId = table.getValueAt(row, 0).toString();
                txtAd.setText(table.getValueAt(row, 1) != null ? table.getValueAt(row, 1).toString() : "");
                txtSehir.setText(table.getValueAt(row, 2) != null ? table.getValueAt(row, 2).toString() : "");
                txtTelefon.setText(table.getValueAt(row, 3) != null ? table.getValueAt(row, 3).toString() : "");
                txtDogumTarihi.setText(table.getValueAt(row, 4) != null ? table.getValueAt(row, 4).toString() : "");
            }
        });
        
        // Buton işlevleri
        btnEkle.addActionListener(e -> musteriEkle());
        btnGuncelle.addActionListener(e -> musteriGuncelle());
        btnSil.addActionListener(e -> musteriSil());

        // Verileri yükle
        verileriYukle();
    }
    
    private void musteriEkle() {
        try {
            if (txtAd.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Lütfen müşteri adını giriniz", "Hata", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            Musteri musteri = new Musteri(
                txtAd.getText(),
                txtSehir.getText(),
                txtTelefon.getText(),
                txtDogumTarihi.getText()
            );
            
            musteriDao.musteriEkle(musteri);
            verileriYukle();
            formuTemizle();
            JOptionPane.showMessageDialog(this, "Müşteri başarıyla eklendi");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Hata: " + ex.getMessage(), "Hata", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void musteriGuncelle() {
        if (seciliMusteriId.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Lütfen güncellenecek müşteriyi seçin", "Uyarı", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        try {
            if (txtAd.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Lütfen müşteri adını giriniz", "Hata", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            Musteri musteri = new Musteri(
                txtAd.getText(),
                txtSehir.getText(),
                txtTelefon.getText(),
                txtDogumTarihi.getText()
            );
            
            musteriDao.musteriGuncelleById(seciliMusteriId, musteri);
            verileriYukle();
            formuTemizle();
            JOptionPane.showMessageDialog(this, "Müşteri başarıyla güncellendi");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Hata: " + ex.getMessage(), "Hata", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void musteriSil() {
        if (seciliMusteriId.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Lütfen silinecek müşteriyi seçin", "Uyarı", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        try {
            int cevap = JOptionPane.showConfirmDialog(this, "Müşteri silinecek. Emin misiniz?", "Onay", JOptionPane.YES_NO_OPTION);
            if (cevap == JOptionPane.YES_OPTION) {
                musteriDao.musteriSil(seciliMusteriId);
                verileriYukle();
                formuTemizle();
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Hata: " + ex.getMessage(), "Hata", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void formuTemizle() {
        txtAd.setText("");
        txtSehir.setText("");
        txtTelefon.setText("");
        txtDogumTarihi.setText(new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
        seciliMusteriId = "";
        table.clearSelection();
    }

    public void verileriYukle() {
        List<Musteri> musteriler = musteriDao.musteriListe();

        // Tabloyu temizle (eğer önceki veriler varsa)
        model.setRowCount(0);

        // Her belgeyi tabloya ekle
        for (Musteri doc : musteriler) {
            ObjectId musteri_id = doc.getId();
            String musteri_adi = doc.getAd();
            String musteri_sehir = doc.getSehir();
            String musteri_telefon = doc.getTelefon();
            String musteri_dogum_tarihi = doc.getDogumTarihi();

            model.addRow(new Object[]{musteri_id, musteri_adi, musteri_sehir, musteri_telefon, musteri_dogum_tarihi});
        }
    }
}
