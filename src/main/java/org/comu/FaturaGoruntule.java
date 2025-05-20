package org.comu;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import org.bson.types.ObjectId;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import javax.swing.RowFilter;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class FaturaGoruntule extends JPanel {
    private FaturaDao faturaDao;
    private MusteriDao musteriDao;
    private JTable table;
    private DefaultTableModel model;
    
    // Input alanları
    private JTextField txtBarkod, txtFaturaTarihi, txtDurumu, txtUrunAdi;
    private JTextField txtUrunMarkasi, txtSatinAlmaYeri, txtSonOdeme, txtKalanTutar;
    private JComboBox<String> cmbMusteriler;
    
    // Filtreleme için input alanları
    private JTextField txtAramaUrunAdi;
    private JTextField txtAramaUrunMarkasi;
    private JTextField txtAramaMinTutar;
    private JTextField txtAramaMaxTutar;
    
    // Butonlar
    private JButton btnEkle, btnGuncelle, btnSil, btnFiltrele, btnTemizle;
    
    // Seçili fatura ID
    private String seciliFaturaId = "";
    
    // Tablo sorter
    private TableRowSorter<DefaultTableModel> sorter;

    public FaturaGoruntule() {
        // Mongo bağlantısı
        MongoClient client = MongoClients.create("mongodb://localhost:27017");
        MongoDatabase database = client.getDatabase("java_proje");
        faturaDao = new FaturaDao(database);
        musteriDao = new MusteriDao(database);

        setLayout(new BorderLayout());

        // Üst panel oluştur - Arama/Filtreleme alanı için
        JPanel searchPanel = new JPanel(new GridLayout(4, 2, 10, 5));
        searchPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Arama Kriterleri"));
        
        // Ürün Adı
        searchPanel.add(new JLabel("Ürün Adı:"));
        txtAramaUrunAdi = new JTextField(20);
        searchPanel.add(txtAramaUrunAdi);
        
        // Ürün Markası
        searchPanel.add(new JLabel("Ürün Markası:"));
        txtAramaUrunMarkasi = new JTextField(20);
        searchPanel.add(txtAramaUrunMarkasi);
        
        // Minimum Fiyat
        searchPanel.add(new JLabel("Min. Tutar:"));
        txtAramaMinTutar = new JTextField(10);
        searchPanel.add(txtAramaMinTutar);
        
        // Maksimum Fiyat
        searchPanel.add(new JLabel("Max. Tutar:"));
        txtAramaMaxTutar = new JTextField(10);
        searchPanel.add(txtAramaMaxTutar);
        
        // Butonları içeren panel
        JPanel buttonSearchPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        btnFiltrele = new JButton("Filtrele");
        btnTemizle = new JButton("Temizle");
        buttonSearchPanel.add(btnFiltrele);
        buttonSearchPanel.add(btnTemizle);
        
        // Ana arama paneli
        JPanel mainSearchPanel = new JPanel(new BorderLayout());
        mainSearchPanel.add(searchPanel, BorderLayout.CENTER);
        mainSearchPanel.add(buttonSearchPanel, BorderLayout.SOUTH);
        mainSearchPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        add(mainSearchPanel, BorderLayout.NORTH);

        String[] kolonlar = {"ID", "Barkod", "Fatura Tarihi", "Müşteri ID", "Durumu", "Ürün Adı", "Ürün Markası", "Satın Alma Yeri", "Son Ödeme", "Kalan Tutar"};

        model = new DefaultTableModel(kolonlar, 0);
        table = new JTable(model);
        
        // TableRowSorter ekle
        sorter = new TableRowSorter<>(model);
        table.setRowSorter(sorter);

        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane, BorderLayout.CENTER);
        
        // Form panellerini oluştur
        JPanel formPanel = new JPanel(new GridLayout(5, 4, 5, 5));
        formPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Barkod
        formPanel.add(new JLabel("Barkod:"));
        txtBarkod = new JTextField(10);
        formPanel.add(txtBarkod);
        
        // Fatura Tarihi
        formPanel.add(new JLabel("Fatura Tarihi:"));
        txtFaturaTarihi = new JTextField(10);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        txtFaturaTarihi.setText(dateFormat.format(new Date())); // Bugünün tarihi
        formPanel.add(txtFaturaTarihi);
        
        // Müşteri Seçimi
        formPanel.add(new JLabel("Müşteri:"));
        cmbMusteriler = new JComboBox<>();
        formPanel.add(cmbMusteriler);
        
        // Durumu
        formPanel.add(new JLabel("Durumu:"));
        txtDurumu = new JTextField(10);
        formPanel.add(txtDurumu);
        
        // Ürün Adı
        formPanel.add(new JLabel("Ürün Adı:"));
        txtUrunAdi = new JTextField(10);
        formPanel.add(txtUrunAdi);
        
        // Ürün Markası
        formPanel.add(new JLabel("Ürün Markası:"));
        txtUrunMarkasi = new JTextField(10);
        formPanel.add(txtUrunMarkasi);
        
        // Satın Alma Yeri
        formPanel.add(new JLabel("Satın Alma Yeri:"));
        txtSatinAlmaYeri = new JTextField(10);
        formPanel.add(txtSatinAlmaYeri);
        
        // Son Ödeme Tarihi
        formPanel.add(new JLabel("Son Ödeme:"));
        txtSonOdeme = new JTextField(10);
        txtSonOdeme.setText(dateFormat.format(new Date())); // Bugünün tarihi
        formPanel.add(txtSonOdeme);
        
        // Kalan Tutar
        formPanel.add(new JLabel("Kalan Tutar:"));
        txtKalanTutar = new JTextField(10);
        formPanel.add(txtKalanTutar);
        
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
        southPanel.add(formPanel, BorderLayout.CENTER);
        southPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        // Panel'i ana panele ekle
        add(southPanel, BorderLayout.SOUTH);
        
        // Tablodan seçim yapıldığında inputları doldur
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && table.getSelectedRow() != -1) {
                int row = table.getSelectedRow();
                seciliFaturaId = table.getValueAt(row, 0).toString();
                txtBarkod.setText(table.getValueAt(row, 1) != null ? table.getValueAt(row, 1).toString() : "");
                txtFaturaTarihi.setText(table.getValueAt(row, 2) != null ? table.getValueAt(row, 2).toString() : "");
                String musteriAdi = table.getValueAt(row, 3).toString();
                cmbMusteriler.setSelectedItem(musteriAdi);
                txtDurumu.setText(table.getValueAt(row, 4) != null ? table.getValueAt(row, 4).toString() : "");
                txtUrunAdi.setText(table.getValueAt(row, 5) != null ? table.getValueAt(row, 5).toString() : "");
                txtUrunMarkasi.setText(table.getValueAt(row, 6) != null ? table.getValueAt(row, 6).toString() : "");
                txtSatinAlmaYeri.setText(table.getValueAt(row, 7) != null ? table.getValueAt(row, 7).toString() : "");
                txtSonOdeme.setText(table.getValueAt(row, 8) != null ? table.getValueAt(row, 8).toString() : "");
                txtKalanTutar.setText(table.getValueAt(row, 9) != null ? table.getValueAt(row, 9).toString() : "");
            }
        });
        
        // Buton işlevleri
        btnEkle.addActionListener(e -> faturaEkle());
        btnGuncelle.addActionListener(e -> faturaGuncelle());
        btnSil.addActionListener(e -> faturaSil());
        btnFiltrele.addActionListener(e -> faturaFiltrele());
        btnTemizle.addActionListener(e -> filtreyiTemizle());
        
        // Enter tuşuna basıldığında filtreleme yap
        txtAramaUrunAdi.addActionListener(e -> faturaFiltrele());
        txtAramaUrunMarkasi.addActionListener(e -> faturaFiltrele());
        txtAramaMinTutar.addActionListener(e -> faturaFiltrele());
        txtAramaMaxTutar.addActionListener(e -> faturaFiltrele());
        
        // Verileri yükle
        verileriYukle();
        // Müşteri combobox'ı doldur
        musterileriYukle();
    }
    
    private void seciliMusteriAyarla(String musteriId) {
        List<Musteri> musteriler = musteriDao.musteriListe();
        for (Musteri musteri : musteriler) {
            if (musteri.getId().toString().equals(musteriId)) {
                cmbMusteriler.setSelectedItem(musteri.getAd());
                return;
            }
        }
    }
    
    public void musterileriYukle() {
        cmbMusteriler.removeAllItems();
        try {
            List<Musteri> musteriler = musteriDao.musteriListe();
            for (Musteri musteri : musteriler) {
                cmbMusteriler.addItem(musteri.getAd());
            }
        } catch (Exception e) {
            System.out.println("Müşteri listesi yüklenirken hata: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    // Müşteri adından ID'sini bulan yardımcı metod
    private ObjectId getMusteriIdFromAd(String musteriAd) {
        List<Musteri> musteriler = musteriDao.musteriListe();
        for (Musteri musteri : musteriler) {
            if (musteri.getAd().equals(musteriAd)) {
                return musteri.getId();
            }
        }
        return null;
    }
    
    // Müşteri ID'sinden adını bulan yardımcı metod
    private String getMusteriAdFromId(ObjectId musteriId) {
        List<Musteri> musteriler = musteriDao.musteriListe();
        for (Musteri musteri : musteriler) {
            if (musteri.getId().equals(musteriId)) {
                return musteri.getAd();
            }
        }
        return "";
    }
    
    private void faturaEkle() {
        try {
            if (txtBarkod.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Lütfen barkod giriniz", "Hata", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            if (cmbMusteriler.getSelectedIndex() == -1) {
                JOptionPane.showMessageDialog(this, "Lütfen bir müşteri seçin", "Hata", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            if (txtKalanTutar.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Lütfen kalan tutarı giriniz", "Hata", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            String seciliMusteriAdi = cmbMusteriler.getSelectedItem().toString();
            ObjectId musteriId = getMusteriIdFromAd(seciliMusteriAdi);
            
            Fatura fatura = new Fatura(
                txtBarkod.getText(),
                txtFaturaTarihi.getText(),
                musteriId,
                txtDurumu.getText(),
                txtUrunAdi.getText(),
                txtUrunMarkasi.getText(),
                txtSatinAlmaYeri.getText(),
                txtSonOdeme.getText(),
                Integer.parseInt(txtKalanTutar.getText())
            );
            
            faturaDao.faturaEkle(fatura, musteriId);
            verileriYukle();
            formuTemizle();
            JOptionPane.showMessageDialog(this, "Fatura başarıyla eklendi");
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Kalan tutar sayısal bir değer olmalıdır", "Hata", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Hata: " + ex.getMessage(), "Hata", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void faturaGuncelle() {
        if (seciliFaturaId.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Lütfen güncellenecek faturayı seçin", "Uyarı", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        try {
            if (txtBarkod.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Lütfen barkod giriniz", "Hata", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            if (cmbMusteriler.getSelectedIndex() == -1) {
                JOptionPane.showMessageDialog(this, "Lütfen bir müşteri seçin", "Hata", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            if (txtKalanTutar.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Lütfen kalan tutarı giriniz", "Hata", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            String seciliMusteriAdi = cmbMusteriler.getSelectedItem().toString();
            ObjectId musteriId = getMusteriIdFromAd(seciliMusteriAdi);
            
            Fatura fatura = new Fatura(
                txtBarkod.getText(),
                txtFaturaTarihi.getText(),
                musteriId,
                txtDurumu.getText(),
                txtUrunAdi.getText(),
                txtUrunMarkasi.getText(),
                txtSatinAlmaYeri.getText(),
                txtSonOdeme.getText(),
                Integer.parseInt(txtKalanTutar.getText())
            );
            
            faturaDao.faturaGuncelle(seciliFaturaId, fatura);
            verileriYukle();
            formuTemizle();
            JOptionPane.showMessageDialog(this, "Fatura başarıyla güncellendi");
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Kalan tutar sayısal bir değer olmalıdır", "Hata", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Hata: " + ex.getMessage(), "Hata", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void faturaSil() {
        if (seciliFaturaId.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Lütfen silinecek faturayı seçin", "Uyarı", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        try {
            int cevap = JOptionPane.showConfirmDialog(this, "Fatura silinecek. Emin misiniz?", "Onay", JOptionPane.YES_NO_OPTION);
            if (cevap == JOptionPane.YES_OPTION) {
                faturaDao.faturaSil(seciliFaturaId);
                verileriYukle();
                formuTemizle();
                JOptionPane.showMessageDialog(this, "Fatura başarıyla silindi");
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Hata: " + ex.getMessage(), "Hata", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void formuTemizle() {
        txtBarkod.setText("");
        txtFaturaTarihi.setText(new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
        cmbMusteriler.setSelectedIndex(-1);
        txtDurumu.setText("");
        txtUrunAdi.setText("");
        txtUrunMarkasi.setText("");
        txtSatinAlmaYeri.setText("");
        txtSonOdeme.setText(new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
        txtKalanTutar.setText("");
        seciliFaturaId = "";
        table.clearSelection();
    }
    
    public void verileriYukle() {
        List<Fatura> faturalar = faturaDao.faturaListele();
        
        // Tabloyu temizle
        model.setRowCount(0);
        
        // Her faturayı tabloya ekle
        for (Fatura fatura : faturalar) {
            ObjectId faturaId = fatura.getId();
            String barkod = fatura.getFatura_barkod();
            String faturaTarihi = fatura.getFatura_tarihi();
            ObjectId musteriId = fatura.getMusteri_id();
            String musteriAdi = getMusteriAdFromId(musteriId);
            String durumu = fatura.getFatura_durumu();
            String urunAdi = fatura.getFatura_urun_adi();
            String urunMarkasi = fatura.getFatura_urun_markasi();
            String satinAlmaYeri = fatura.getSatin_alma_yeri();
            String sonOdeme = fatura.getFatura_son_odeme();
            int kalanTutar = fatura.getKalan_tutar();
            
            model.addRow(new Object[]{faturaId, barkod, faturaTarihi, musteriAdi, durumu, urunAdi, urunMarkasi, satinAlmaYeri, sonOdeme, kalanTutar});
        }
    }
    
    // Filtreleme işlemini gerçekleştiren metod
    private void faturaFiltrele() {
        // Tüm kriterleri al
        String aramaUrunAdi = txtAramaUrunAdi.getText().trim();
        String aramaUrunMarkasi = txtAramaUrunMarkasi.getText().trim();
        String aramaMinTutar = txtAramaMinTutar.getText().trim();
        String aramaMaxTutar = txtAramaMaxTutar.getText().trim();
        
        // Tüm alanlar boşsa, filtreyi temizle
        if (aramaUrunAdi.isEmpty() && aramaUrunMarkasi.isEmpty() && 
            aramaMinTutar.isEmpty() && aramaMaxTutar.isEmpty()) {
            sorter.setRowFilter(null);
            return;
        }
        
        // Her bir kriter için ayrı RowFilter oluştur
        List<RowFilter<Object,Object>> filters = new ArrayList<>();
        
        // Ürün adı filtresi
        if (!aramaUrunAdi.isEmpty()) {
            filters.add(RowFilter.regexFilter("(?i)" + aramaUrunAdi, 5));
        }
        
        // Ürün markası filtresi
        if (!aramaUrunMarkasi.isEmpty()) {
            filters.add(RowFilter.regexFilter("(?i)" + aramaUrunMarkasi, 6));
        }
        
        // Fiyat aralığı filtreleri
        try {
            if (!aramaMinTutar.isEmpty()) {
                int minTutar = Integer.parseInt(aramaMinTutar);
                filters.add(new RowFilter<Object,Object>() { //Yeni bir Filter sınıfı oluşturuyoruz
                    @Override //Üzerine yazıyoruz
                    public boolean include(Entry<? extends Object, ? extends Object> entry) {
                        //include eğer değer varsa yani true ise değeri gösteriyor
                        //eğer false ise değeri göstermiyor şeklinde işlem yapıyor
                        int kalan = Integer.parseInt(entry.getStringValue(9));
                        return kalan >= minTutar; //burada kalan bizim min tutardan büyükse değerleri true yani gösteriyor
                    }
                });
            }
            
            if (!aramaMaxTutar.isEmpty()) {
                int maxTutar = Integer.parseInt(aramaMaxTutar);
                filters.add(new RowFilter<Object,Object>() {
                    @Override
                    public boolean include(Entry<? extends Object, ? extends Object> entry) {
                        int kalan = Integer.parseInt(entry.getStringValue(9));
                        return kalan <= maxTutar;
                    }
                });
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Tutar alanları sayısal değer olmalıdır", "Hata", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // Tüm filtreleri birleştir (AND ile)
        sorter.setRowFilter(RowFilter.andFilter(filters));
    }
    
    // Filtreyi temizle
    private void filtreyiTemizle() {
        txtAramaUrunAdi.setText("");
        txtAramaUrunMarkasi.setText("");
        txtAramaMinTutar.setText("");
        txtAramaMaxTutar.setText("");
        sorter.setRowFilter(null);
    }
}
