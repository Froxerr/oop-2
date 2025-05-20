package org.comu;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import org.bson.types.ObjectId;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class OdemeGoruntule extends JPanel {
    private OdemeDao odemeDao;
    private FaturaDao faturaDao;
    private JTable table;
    private DefaultTableModel model;
    
    // Input alanları
    private JTextField txtOdemeTarihi, txtOdenenTutar, txtOdemeYontemi;
    private JComboBox<String> cmbFaturalar;
    
    // Butonlar
    private JButton btnEkle, btnGuncelle, btnSil;
    
    // Seçili ödeme ID
    private String seciliOdemeId = "";

    public OdemeGoruntule()
    {
        // Mongo bağlantısı
        MongoClient client = MongoClients.create("mongodb://localhost:27017");
        MongoDatabase database = client.getDatabase("java_proje");
        odemeDao = new OdemeDao(database);
        faturaDao = new FaturaDao(database);

        setLayout(new BorderLayout());

        String[] kolonlar = {"ID", "Fatura Barkod", "Ödeme Tarihi", "Ödenen Tutar", "Ödeme Yöntemi"};

        model = new DefaultTableModel(kolonlar, 0);
        table = new JTable(model);

        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane, BorderLayout.CENTER);
        
        // Input alanlarını oluştur
        JPanel inputPanel = new JPanel(new GridLayout(2, 4, 5, 5));
        inputPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Fatura seçimi
        inputPanel.add(new JLabel("Fatura:"));
        cmbFaturalar = new JComboBox<>();
        inputPanel.add(cmbFaturalar);
        
        // Ödeme Tarihi
        inputPanel.add(new JLabel("Ödeme Tarihi:"));
        txtOdemeTarihi = new JTextField(10);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        txtOdemeTarihi.setText(dateFormat.format(new Date())); // Bugünün tarihi
        inputPanel.add(txtOdemeTarihi);
        
        // Ödenen Tutar
        inputPanel.add(new JLabel("Ödenen Tutar:"));
        txtOdenenTutar = new JTextField(10);
        inputPanel.add(txtOdenenTutar);
        
        // Ödeme Yöntemi
        inputPanel.add(new JLabel("Ödeme Yöntemi:"));
        txtOdemeYontemi = new JTextField(10);
        inputPanel.add(txtOdemeYontemi);
        
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
                seciliOdemeId = table.getValueAt(row, 0).toString();
                String faturaBarkod = table.getValueAt(row, 1).toString();
                cmbFaturalar.setSelectedItem(faturaBarkod);
                
                txtOdemeTarihi.setText(table.getValueAt(row, 2) != null ? table.getValueAt(row, 2).toString() : "");
                txtOdenenTutar.setText(table.getValueAt(row, 3) != null ? table.getValueAt(row, 3).toString() : "");
                txtOdemeYontemi.setText(table.getValueAt(row, 4) != null ? table.getValueAt(row, 4).toString() : "");
            }
        });
        
        // Buton işlevleri
        btnEkle.addActionListener(e -> odemeEkle());
        btnGuncelle.addActionListener(e -> odemeGuncelle());
        btnSil.addActionListener(e -> odemeSil());

        // Verileri yükle
        verileriYukle();
        // Fatura combobox'ı doldur
        faturalariYukle();
    }
    
    private void seciliFaturaAyarla(String faturaId) {
        List<Fatura> faturalar = faturaDao.faturaListele();
        for (Fatura fatura : faturalar) {
            if (fatura.getId().toString().equals(faturaId)) {
                // Faturanın tam gösterimini oluştur
                String musteriAdi = getMusteriAdiFromId(fatura.getMusteri_id());
                String faturaGosterim = String.format("%s - %s - %s", 
                    fatura.getFatura_barkod(),
                    fatura.getFatura_urun_adi(),
                    musteriAdi);
                
                // Combobox'ta tam gösterimi ara ve seç
                for (int i = 0; i < cmbFaturalar.getItemCount(); i++) {
                    String item = cmbFaturalar.getItemAt(i);
                    if (item.startsWith(fatura.getFatura_barkod())) {
                        cmbFaturalar.setSelectedIndex(i);
                        return;
                    }
                }
                return;
            }
        }
    }
    
    private void odemeEkle() {
        try {
            if (cmbFaturalar.getSelectedIndex() == -1) {
                JOptionPane.showMessageDialog(this, "Lütfen bir fatura seçin", "Hata", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            if (txtOdenenTutar.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Lütfen ödenen tutarı giriniz", "Hata", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            String seciliFaturaGosterim = cmbFaturalar.getSelectedItem().toString();
            ObjectId faturaId = getFaturaIdFromBarkod(seciliFaturaGosterim);
            
            if (faturaId == null) {
                JOptionPane.showMessageDialog(this, 
                    "Seçilen barkoda ait fatura bulunamadı! Fatura listesini güncelleyiniz.", 
                    "Hata", 
                    JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // Faturaları taze olarak yükle ve seçili faturayla eşleşeni bul
            List<Fatura> faturalar = faturaDao.faturaListele();
            Fatura seciliFatura = null;
            for (Fatura fatura : faturalar) {
                if (fatura.getId().equals(faturaId)) {
                    seciliFatura = fatura;
                    break;
                }
            }
            
            // Fatura bulunamadıysa uyarı ver
            if (seciliFatura == null) {
                JOptionPane.showMessageDialog(this, 
                    "Seçili fatura sistemde bulunamadı! Fatura listesini güncelleyiniz.", 
                    "Hata", 
                    JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // Ödeme tutarını kontrol et
            int odenenTutar = Integer.parseInt(txtOdenenTutar.getText());
            if (odenenTutar <= 0) {
                JOptionPane.showMessageDialog(this, "Ödeme tutarı sıfırdan büyük olmalıdır", "Hata", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            if (odenenTutar > seciliFatura.getKalan_tutar()) {
                JOptionPane.showMessageDialog(this, 
                    "Ödeme tutarı faturanın kalan tutarından (" + seciliFatura.getKalan_tutar() + ") büyük olamaz", 
                    "Hata", 
                    JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            Odeme odeme = new Odeme(
                faturaId,
                txtOdemeTarihi.getText(),
                odenenTutar,
                txtOdemeYontemi.getText()
            );
            
            odemeDao.OdemeEkleme(odeme, faturaId);
            
            // Verileri yeniden yükle
            verileriYukle();
            
            // Fatura listesini de güncelle
            faturalariYukle();
            
            formuTemizle();
            JOptionPane.showMessageDialog(this, 
                "Ödeme başarıyla eklendi ve fatura kalan tutarı " + (seciliFatura.getKalan_tutar() - odenenTutar) + 
                " olarak güncellendi", 
                "Başarılı", 
                JOptionPane.INFORMATION_MESSAGE);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Ödenen tutar sayısal bir değer olmalıdır", "Hata", JOptionPane.ERROR_MESSAGE);
        } catch (IllegalArgumentException ex) {
            // OdemeDao sınıfında fırlatılan özel hata mesajlarını yakala
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Hata", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Hata: " + ex.getMessage(), "Hata", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void odemeGuncelle() {
        if (seciliOdemeId.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Lütfen güncellenecek ödemeyi seçin", "Uyarı", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        try {
            if (cmbFaturalar.getSelectedIndex() == -1) {
                JOptionPane.showMessageDialog(this, "Lütfen bir fatura seçin", "Hata", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            if (txtOdenenTutar.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Lütfen ödenen tutarı giriniz", "Hata", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            String seciliFaturaGosterim = cmbFaturalar.getSelectedItem().toString();
            ObjectId faturaId = getFaturaIdFromBarkod(seciliFaturaGosterim);
            
            Odeme odeme = new Odeme(
                faturaId,
                txtOdemeTarihi.getText(),
                Integer.parseInt(txtOdenenTutar.getText()),
                txtOdemeYontemi.getText()
            );
            
            boolean success = odemeDao.odemeGuncelleme(odeme, seciliOdemeId);
            if (success) {
                verileriYukle();
                formuTemizle();
                JOptionPane.showMessageDialog(this, "Ödeme başarıyla güncellendi ve fatura kalan tutarı güncellendi", "Başarılı", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Ödeme güncellenemedi", "Hata", JOptionPane.ERROR_MESSAGE);
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Ödenen tutar sayısal bir değer olmalıdır", "Hata", JOptionPane.ERROR_MESSAGE);
        } catch (IllegalArgumentException ex) {
            // OdemeDao sınıfında fırlatılan özel hata mesajlarını yakala
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Hata", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Hata: " + ex.getMessage(), "Hata", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void odemeSil() {
        if (seciliOdemeId.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Lütfen silinecek ödemeyi seçin", "Uyarı", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        try {
            int cevap = JOptionPane.showConfirmDialog(this, 
                "Ödeme silinecek ve ilgili fatura tutarı güncellenecektir. Emin misiniz?", 
                "Onay", 
                JOptionPane.YES_NO_OPTION);
            
            if (cevap == JOptionPane.YES_OPTION) {
                boolean success = odemeDao.odemeSil(seciliOdemeId);
                if (success) {
                    verileriYukle();
                    formuTemizle();
                    JOptionPane.showMessageDialog(this, "Ödeme başarıyla silindi ve fatura tutarı güncellendi", "Başarılı", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this, "Ödeme silinemedi", "Hata", JOptionPane.ERROR_MESSAGE);
                }
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Hata: " + ex.getMessage(), "Hata", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void formuTemizle() {
        txtOdemeTarihi.setText(new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
        txtOdenenTutar.setText("");
        txtOdemeYontemi.setText("");
        cmbFaturalar.setSelectedIndex(-1);
        seciliOdemeId = "";
        table.clearSelection();
    }
    
    public void faturalariYukle() {
        cmbFaturalar.removeAllItems();
        try {
            List<Fatura> faturalar = faturaDao.faturaListele();
            for (Fatura fatura : faturalar) {
                // Sadece ödenmemiş (kalan tutarı > 0) faturaları göster
                if (fatura.getKalan_tutar() > 0) {
                    // Müşteri adını bul
                    String musteriAdi = getMusteriAdiFromId(fatura.getMusteri_id());
                    // Barkod ve diğer bilgileri birleştir: "BARKOD - Ürün Adı - Müşteri Adı"
                    String faturaGosterim = String.format("%s - %s - %s", 
                        fatura.getFatura_barkod(),
                        fatura.getFatura_urun_adi(),
                        musteriAdi);
                    cmbFaturalar.addItem(faturaGosterim);
                }
            }
        } catch (Exception e) {
            System.out.println("Fatura listesi yüklenirken hata: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Müşteri ID'sinden müşteri adını bulan metod
    private String getMusteriAdiFromId(ObjectId musteriId) {
        try {
            MongoClient client = MongoClients.create("mongodb://localhost:27017");
            MongoDatabase database = client.getDatabase("java_proje");
            MusteriDao musteriDao = new MusteriDao(database);
            
            List<Musteri> musteriler = musteriDao.musteriListe();
            for (Musteri musteri : musteriler) {
                if (musteri.getId().equals(musteriId)) {
                    client.close();
                    return musteri.getAd();
                }
            }
            client.close();
        } catch (Exception e) {
            System.out.println("Müşteri adı bulunurken hata: " + e.getMessage());
        }
        return "Bilinmeyen Müşteri";
    }

    private ObjectId getFaturaIdFromBarkod(String faturaGosterim) {
        // Barkodu çıkar (ilk tire işaretine kadar olan kısım)
        String barkod = faturaGosterim.split(" - ")[0];
        
        List<Fatura> faturalar = faturaDao.faturaListele();
        for (Fatura fatura : faturalar) {
            if (fatura.getFatura_barkod().equals(barkod)) {
                return fatura.getId();
            }
        }
        return null;
    }
    
    private String getFaturaBarkodFromId(ObjectId faturaId) {
        List<Fatura> faturalar = faturaDao.faturaListele();
        for (Fatura fatura : faturalar) {
            if (fatura.getId().equals(faturaId)) {
                String musteriAdi = getMusteriAdiFromId(fatura.getMusteri_id());
                return String.format("%s - %s - %s", 
                    fatura.getFatura_barkod(),
                    fatura.getFatura_urun_adi(),
                    musteriAdi);
            }
        }
        return "";
    }

    public void verileriYukle() {
        List<Odeme> odemeler = odemeDao.odemeListe();
        
        // Tabloyu temizle
        model.setRowCount(0);
        
        // Her ödemeyi tabloya ekle
        for (Odeme odeme : odemeler) {
            ObjectId odeme_id = odeme.getId();
            ObjectId fatura_id = odeme.getFatura_id();
            String faturaBarkod = getFaturaBarkodFromId(fatura_id);
            String odeme_tarihi = odeme.getOdeme_tarihi();
            int odenen_tutar = odeme.getOdenen_tutar();
            String odeme_yontemi = odeme.getOdeme_yontemi();
            
            model.addRow(new Object[]{odeme_id, faturaBarkod, odeme_tarihi, odenen_tutar, odeme_yontemi});
        }
    }
}
