package org.comu;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.bson.types.ObjectId;
import org.comu.api.AIApiService;
import org.comu.api.AIResponseDialog;
import com.mongodb.client.MongoClients;

import javax.swing.*;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.ArrayList;

public class EkleGoruntule extends JPanel {
    
    // Form alanları - Müşteri
    private JTextField txtMusteriAd;
    private JTextField txtSehir;
    private JTextField txtTelefon;
    private JTextField txtDogumTarihi;
    
    // Form alanları - Fatura
    private JTextField txtBarkod;
    private JTextField txtFaturaTarih;
    private JComboBox<String> cmbMusteri;
    private JTextField txtDurum;
    private JTextField txtUrunAdi;
    private JTextField txtUrunMarkasi;
    private JTextField txtSatinAlmaYeri;
    private JTextField txtSonOdeme;
    private JTextField txtKalanTutar;
    
    // Form alanları - Ödeme
    private JComboBox<String> cmbFatura;
    private JTextField txtOdemeTarih;
    private JTextField txtOdenenTutar;
    private JTextField txtOdemeYontemi;
    
    // Random veri üretmek için
    private Random random = new Random();
    
    // DAO nesneleri
    private MusteriDao musteriDao;
    private FaturaDao faturaDao;
    private OdemeDao odemeDao;
    
    // API Servisi
    private AIApiService apiService;
    
    // Gson
    private Gson gson = new Gson();
    
    public EkleGoruntule() {
        // Panel düzeni için BoxLayout
        setLayout(new BorderLayout()); // BoxLayout yerine BorderLayout kullanarak panel genişliğini tam olarak ayarlayacağız
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Veritabanı bağlantısını al
        try {
            // MongoDB bağlantısı
            com.mongodb.client.MongoClient mongoClient = MongoClients.create("mongodb://localhost:27017");
            com.mongodb.client.MongoDatabase db = mongoClient.getDatabase("java_proje");
            
            // DAO nesnelerini oluştur
            musteriDao = new MusteriDao(db);
            faturaDao = new FaturaDao(db);
            odemeDao = new OdemeDao(db);
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, 
                "Veritabanı bağlantısı kurulamadı: " + e.getMessage(), 
                "Hata", 
                JOptionPane.ERROR_MESSAGE);
        }
        
        // API servisini başlat
        apiService = new AIApiService();
        
        // Ana panel oluştur ve dikey düzende tüm formları içerecek
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        
        // Müşteri Ekleme Paneli
        JPanel musteriPanel = createMusteriEklemePanel();
        musteriPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "MÜŞTERİ EKLEME", 
                                                           javax.swing.border.TitledBorder.CENTER, 
                                                           javax.swing.border.TitledBorder.DEFAULT_POSITION, 
                                                           new Font("Arial", Font.BOLD, 16)));
        
        // Fatura Ekleme Paneli
        JPanel faturaPanel = createFaturaEklemePanel();
        faturaPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "FATURA EKLEME", 
                                                           javax.swing.border.TitledBorder.CENTER, 
                                                           javax.swing.border.TitledBorder.DEFAULT_POSITION, 
                                                           new Font("Arial", Font.BOLD, 16)));
        
        // Ödeme Ekleme Paneli
        JPanel odemePanel = createOdemeEklemePanel();
        odemePanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "ÖDEME EKLEME", 
                                                           javax.swing.border.TitledBorder.CENTER, 
                                                           javax.swing.border.TitledBorder.DEFAULT_POSITION, 
                                                           new Font("Arial", Font.BOLD, 16)));
        
        // Panelleri tam genişlikte göster
        musteriPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        faturaPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        odemePanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        // Minimum panel boyutları ayarla
        Dimension panelDimension = new Dimension(800, 300);
        musteriPanel.setMinimumSize(panelDimension);
        musteriPanel.setPreferredSize(panelDimension);
        
        faturaPanel.setMinimumSize(new Dimension(800, 550));
        faturaPanel.setPreferredSize(new Dimension(800, 550));
        
        odemePanel.setMinimumSize(panelDimension);
        odemePanel.setPreferredSize(panelDimension);
        
        // Paneller arası boşluk
        mainPanel.add(musteriPanel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 10))); // Boşluk ekle
        mainPanel.add(faturaPanel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 10))); // Boşluk ekle
        mainPanel.add(odemePanel);
        
        // ScrollPane ile ana paneli sar
        JScrollPane mainScrollPane = new JScrollPane(mainPanel);
        mainScrollPane.setBorder(null);
        mainScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        
        add(mainScrollPane, BorderLayout.CENTER);
    }
    
    private JPanel createMusteriEklemePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        // GridLayout yerine FormLayout kullanacağız
        JPanel formPanel = new JPanel(null); // Absolute pozisyonlama için null layout
        formPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Form alanları
        Font büyükFont = new Font("Arial", Font.BOLD, 16);
        
        JLabel lblAd = new JLabel("Müşteri Adı:");
        lblAd.setFont(büyükFont);
        lblAd.setBounds(10, 20, 180, 30);
        txtMusteriAd = new JTextField(30);
        txtMusteriAd.setFont(büyükFont);
        txtMusteriAd.setEditable(false);
        txtMusteriAd.setBounds(200, 20, 350, 40);  // Yüksekliği artırıldı
        
        JLabel lblSehir = new JLabel("Şehir:");
        lblSehir.setFont(büyükFont);
        lblSehir.setBounds(10, 70, 180, 30);
        txtSehir = new JTextField(30);
        txtSehir.setFont(büyükFont);
        txtSehir.setEditable(false);
        txtSehir.setBounds(200, 70, 350, 40);  // Yüksekliği artırıldı
        
        JLabel lblTelefon = new JLabel("Telefon:");
        lblTelefon.setFont(büyükFont);
        lblTelefon.setBounds(10, 120, 180, 30);
        txtTelefon = new JTextField(30);
        txtTelefon.setFont(büyükFont);
        txtTelefon.setEditable(false);
        txtTelefon.setBounds(200, 120, 350, 40);  // Yüksekliği artırıldı
        
        JLabel lblDogumTarihi = new JLabel("Doğum Tarihi:");
        lblDogumTarihi.setFont(büyükFont);
        lblDogumTarihi.setBounds(10, 170, 180, 30);
        txtDogumTarihi = new JTextField(30);
        txtDogumTarihi.setFont(büyükFont);
        txtDogumTarihi.setEditable(false);
        txtDogumTarihi.setBounds(200, 170, 350, 40);  // Yüksekliği artırıldı
        
        // Form alanlarını panele ekle
        formPanel.add(lblAd);
        formPanel.add(txtMusteriAd);
        formPanel.add(lblSehir);
        formPanel.add(txtSehir);
        formPanel.add(lblTelefon);
        formPanel.add(txtTelefon);
        formPanel.add(lblDogumTarihi);
        formPanel.add(txtDogumTarihi);
        
        formPanel.setPreferredSize(new Dimension(600, 230)); // Panel boyutunu artırdım
        
        // Scroll pane ekleyerek formu içine koy
        JScrollPane scrollPane = new JScrollPane(formPanel);
        scrollPane.setBorder(null);
        
        // Buton paneli
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnOtomatikDoldur = new JButton("Yapay Zeka ile Doldur");
        btnOtomatikDoldur.setFont(büyükFont);
        btnOtomatikDoldur.addActionListener(e -> {
            otomatikMusteriDoldur();
        });
        
        JButton btnEkle = new JButton("Müşteri Ekle");
        btnEkle.setFont(büyükFont);
        btnEkle.addActionListener(e -> {
            if (txtMusteriAd.getText().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Lütfen önce 'Yapay Zeka ile Doldur' butonunu kullanın.", "Uyarı", JOptionPane.WARNING_MESSAGE);
                return;
            }
            musteriEkle();
        });
        
        buttonPanel.add(btnOtomatikDoldur);
        buttonPanel.add(btnEkle);
        
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private JPanel createFaturaEklemePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        JPanel formPanel = new JPanel(null); // Absolute pozisyonlama için null layout
        formPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Font boyutunu artır
        Font büyükFont = new Font("Arial", Font.BOLD, 16);
        
        // Form alanları - input boyutlarını artır
        JLabel lblBarkod = new JLabel("Barkod:");
        lblBarkod.setFont(büyükFont);
        lblBarkod.setBounds(10, 20, 180, 30);
        txtBarkod = new JTextField(30);
        txtBarkod.setFont(büyükFont);
        txtBarkod.setEditable(false);
        txtBarkod.setBounds(200, 20, 350, 40);
        
        JLabel lblTarih = new JLabel("Fatura Tarihi:");
        lblTarih.setFont(büyükFont);
        lblTarih.setBounds(10, 70, 180, 30);
        txtFaturaTarih = new JTextField(30);
        txtFaturaTarih.setFont(büyükFont);
        txtFaturaTarih.setEditable(false);
        txtFaturaTarih.setBounds(200, 70, 350, 40);
        
        JLabel lblMusteri = new JLabel("Müşteri:");
        lblMusteri.setFont(büyükFont);
        lblMusteri.setBounds(10, 120, 180, 30);
        cmbMusteri = new JComboBox<>();
        cmbMusteri.setFont(büyükFont);
        cmbMusteri.setEnabled(true);
        cmbMusteri.setBounds(200, 120, 350, 40);
        
        JLabel lblDurum = new JLabel("Durum:");
        lblDurum.setFont(büyükFont);
        lblDurum.setBounds(10, 170, 180, 30);
        txtDurum = new JTextField(30);
        txtDurum.setFont(büyükFont);
        txtDurum.setEditable(false);
        txtDurum.setBounds(200, 170, 350, 40);
        
        JLabel lblUrunAdi = new JLabel("Ürün Adı:");
        lblUrunAdi.setFont(büyükFont);
        lblUrunAdi.setBounds(10, 220, 180, 30);
        txtUrunAdi = new JTextField(30);
        txtUrunAdi.setFont(büyükFont);
        txtUrunAdi.setEditable(false);
        txtUrunAdi.setBounds(200, 220, 350, 40);
        
        JLabel lblUrunMarkasi = new JLabel("Ürün Markası:");
        lblUrunMarkasi.setFont(büyükFont);
        lblUrunMarkasi.setBounds(10, 270, 180, 30);
        txtUrunMarkasi = new JTextField(30);
        txtUrunMarkasi.setFont(büyükFont);
        txtUrunMarkasi.setEditable(false);
        txtUrunMarkasi.setBounds(200, 270, 350, 40);
        
        JLabel lblSatinAlmaYeri = new JLabel("Satın Alma Yeri:");
        lblSatinAlmaYeri.setFont(büyükFont);
        lblSatinAlmaYeri.setBounds(10, 320, 180, 30);
        txtSatinAlmaYeri = new JTextField(30);
        txtSatinAlmaYeri.setFont(büyükFont);
        txtSatinAlmaYeri.setEditable(false);
        txtSatinAlmaYeri.setBounds(200, 320, 350, 40);
        
        JLabel lblSonOdeme = new JLabel("Son Ödeme Tarihi:");
        lblSonOdeme.setFont(büyükFont);
        lblSonOdeme.setBounds(10, 370, 180, 30);
        txtSonOdeme = new JTextField(30);
        txtSonOdeme.setFont(büyükFont);
        txtSonOdeme.setEditable(false);
        txtSonOdeme.setBounds(200, 370, 350, 40);
        
        JLabel lblKalanTutar = new JLabel("Kalan Tutar:");
        lblKalanTutar.setFont(büyükFont);
        lblKalanTutar.setBounds(10, 420, 180, 30);
        txtKalanTutar = new JTextField(30);
        txtKalanTutar.setFont(büyükFont);
        txtKalanTutar.setEditable(false);
        txtKalanTutar.setBounds(200, 420, 350, 40);
        
        // Form alanlarını panele ekle
        formPanel.add(lblBarkod);
        formPanel.add(txtBarkod);
        formPanel.add(lblTarih);
        formPanel.add(txtFaturaTarih);
        formPanel.add(lblMusteri);
        formPanel.add(cmbMusteri);
        formPanel.add(lblDurum);
        formPanel.add(txtDurum);
        formPanel.add(lblUrunAdi);
        formPanel.add(txtUrunAdi);
        formPanel.add(lblUrunMarkasi);
        formPanel.add(txtUrunMarkasi);
        formPanel.add(lblSatinAlmaYeri);
        formPanel.add(txtSatinAlmaYeri);
        formPanel.add(lblSonOdeme);
        formPanel.add(txtSonOdeme);
        formPanel.add(lblKalanTutar);
        formPanel.add(txtKalanTutar);
        
        formPanel.setPreferredSize(new Dimension(600, 470)); // Panel boyutunu ayarla
        
        // JScrollPane ekleyerek gerektiğinde kaydırma çubuğu gösterilmesini sağla
        JScrollPane scrollPane = new JScrollPane(formPanel);
        scrollPane.setBorder(null);
        
        // Buton paneli
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnOtomatikDoldur = new JButton("Yapay Zeka ile Doldur");
        btnOtomatikDoldur.setFont(büyükFont);
        btnOtomatikDoldur.addActionListener(e -> {
            otomatikFaturaDoldur();
        });
        
        JButton btnEkle = new JButton("Fatura Ekle");
        btnEkle.setFont(büyükFont);
        btnEkle.addActionListener(e -> {
            if (txtBarkod.getText().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Lütfen önce 'Yapay Zeka ile Doldur' butonunu kullanın.", "Uyarı", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (cmbMusteri.getSelectedIndex() == -1 || cmbMusteri.getSelectedIndex() == 0) {
                JOptionPane.showMessageDialog(this, "Lütfen bir müşteri seçin.", "Uyarı", JOptionPane.WARNING_MESSAGE);
                return;
            }
            faturaEkle();
        });
        
        buttonPanel.add(btnOtomatikDoldur);
        buttonPanel.add(btnEkle);
        
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        // Müşterileri yükle
        musterileriYukle();
        
        return panel;
    }
    
    private JPanel createOdemeEklemePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        JPanel formPanel = new JPanel(null);
        formPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Font boyutunu artır
        Font büyükFont = new Font("Arial", Font.BOLD, 16);
        
        // Form alanları
        JLabel lblFatura = new JLabel("Fatura:");
        lblFatura.setFont(büyükFont);
        lblFatura.setBounds(10, 20, 180, 30);
        cmbFatura = new JComboBox<>();
        cmbFatura.setFont(büyükFont);
        cmbFatura.setEnabled(true);
        cmbFatura.setBounds(200, 20, 350, 40);
        
        JLabel lblTarih = new JLabel("Ödeme Tarihi:");
        lblTarih.setFont(büyükFont);
        lblTarih.setBounds(10, 70, 180, 30);
        txtOdemeTarih = new JTextField(30);
        txtOdemeTarih.setFont(büyükFont);
        txtOdemeTarih.setEditable(false);
        txtOdemeTarih.setBounds(200, 70, 350, 40);
        
        JLabel lblTutar = new JLabel("Ödenen Tutar:");
        lblTutar.setFont(büyükFont);
        lblTutar.setBounds(10, 120, 180, 30);
        txtOdenenTutar = new JTextField(30);
        txtOdenenTutar.setFont(büyükFont);
        txtOdenenTutar.setEditable(false);
        txtOdenenTutar.setBounds(200, 120, 350, 40);
        
        JLabel lblYontem = new JLabel("Ödeme Yöntemi:");
        lblYontem.setFont(büyükFont);
        lblYontem.setBounds(10, 170, 180, 30);
        txtOdemeYontemi = new JTextField(30);
        txtOdemeYontemi.setFont(büyükFont);
        txtOdemeYontemi.setEditable(false);
        txtOdemeYontemi.setBounds(200, 170, 350, 40);
        
        // Form alanlarını panele ekle
        formPanel.add(lblFatura);
        formPanel.add(cmbFatura);
        formPanel.add(lblTarih);
        formPanel.add(txtOdemeTarih);
        formPanel.add(lblTutar);
        formPanel.add(txtOdenenTutar);
        formPanel.add(lblYontem);
        formPanel.add(txtOdemeYontemi);
        
        formPanel.setPreferredSize(new Dimension(600, 230)); // Panel boyutunu artırdım
        
        // Scroll pane ekleyerek formu içine koy
        JScrollPane scrollPane = new JScrollPane(formPanel);
        scrollPane.setBorder(null);
        
        // Buton paneli
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnOtomatikDoldur = new JButton("Yapay Zeka ile Doldur");
        btnOtomatikDoldur.setFont(büyükFont);
        btnOtomatikDoldur.addActionListener(e -> {
            otomatikOdemeDoldur();
        });
        
        JButton btnEkle = new JButton("Ödeme Ekle");
        btnEkle.setFont(büyükFont);
        btnEkle.addActionListener(e -> {
            if (txtOdenenTutar.getText().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Lütfen önce 'Yapay Zeka ile Doldur' butonunu kullanın.", "Uyarı", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (cmbFatura.getSelectedIndex() == -1 || cmbFatura.getSelectedIndex() == 0) {
                JOptionPane.showMessageDialog(this, "Lütfen bir fatura seçin.", "Uyarı", JOptionPane.WARNING_MESSAGE);
                return;
            }
            odemeEkle();
        });
        
        buttonPanel.add(btnOtomatikDoldur);
        buttonPanel.add(btnEkle);
        
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        // Faturaları yükle
        faturalariYukle();
        
        return panel;
    }
    
    // Müşteri formu için yapay zeka ile doldurma
    private void otomatikMusteriDoldur() {
        try {
            // API'den müşteri verisi al
            String jsonResponse = apiService.generateMusteri();
            
            // Dialog oluştur ve göster
            JFrame parentFrame = (JFrame) SwingUtilities.getWindowAncestor(this);
            AIResponseDialog dialog = new AIResponseDialog(parentFrame, "Yapay Zeka Müşteri Verisi", jsonResponse);
            
            // Eğer kullanıcı onayladıysa, formu doldur
            if (dialog.showDialog()) {
                // JSON'ı parse et
                JsonObject musteri = gson.fromJson(jsonResponse, JsonObject.class);
                
                // Müşteri zaten var mı kontrol et
                String musteriAdi = musteri.get("musteri_adi").getAsString();
                if (musteriVarMi(musteriAdi)) {
                    JOptionPane.showMessageDialog(this, 
                        "Bu isimde bir müşteri zaten mevcut: " + musteriAdi, 
                        "Uyarı", 
                        JOptionPane.WARNING_MESSAGE);
                    return;
                }
                
                // Form alanlarını doldur
                txtMusteriAd.setText(musteriAdi);
                txtSehir.setText(musteri.get("sehir").getAsString());
                txtTelefon.setText(musteri.get("telefon").getAsString());
                txtDogumTarihi.setText(musteri.get("dogum_tarihi").getAsString());
                
                JOptionPane.showMessageDialog(this, "Müşteri bilgileri formda güncellendi.", "Bilgi", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, 
                "Yapay zeka verileri yüklenirken bir hata oluştu: " + e.getMessage(), 
                "Hata", 
                JOptionPane.ERROR_MESSAGE);
            
            // Hata durumunda basit örnek veri kullan
            useExampleDataForMusteri();
        }
    }
    
    // Fatura formu için yapay zeka ile doldurma
    private void otomatikFaturaDoldur() {
        try {
            // API'den fatura verisi al
            String jsonResponse = apiService.generateFatura();
            
            // Dialog oluştur ve göster
            JFrame parentFrame = (JFrame) SwingUtilities.getWindowAncestor(this);
            AIResponseDialog dialog = new AIResponseDialog(parentFrame, "Yapay Zeka Fatura Verisi", jsonResponse);
            
            // Eğer kullanıcı onayladıysa, formu doldur
            if (dialog.showDialog()) {
                // JSON'ı parse et
                JsonObject fatura = gson.fromJson(jsonResponse, JsonObject.class);
                
                // Fatura zaten var mı kontrol et (barkod'a göre)
                String barkod = fatura.get("barkod").getAsString();
                if (faturaVarMi(barkod)) {
                    JOptionPane.showMessageDialog(this, 
                        "Bu barkoda sahip bir fatura zaten mevcut: " + barkod, 
                        "Uyarı", 
                        JOptionPane.WARNING_MESSAGE);
                    return;
                }
                
                // Form alanlarını doldur
                txtBarkod.setText(barkod);
                txtFaturaTarih.setText(fatura.get("fatura_tarihi").getAsString());
                txtDurum.setText(fatura.get("durum").getAsString());
                txtUrunAdi.setText(fatura.get("urun_adi").getAsString());
                txtUrunMarkasi.setText(fatura.get("urun_markasi").getAsString());
                txtSatinAlmaYeri.setText(fatura.get("satin_alma_yeri").getAsString());
                txtSonOdeme.setText(fatura.get("son_odeme_tarihi").getAsString());
                txtKalanTutar.setText(fatura.get("kalan_tutar").getAsString());
                
                JOptionPane.showMessageDialog(this, "Fatura bilgileri formda güncellendi. Lütfen müşteri seçin.", "Bilgi", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, 
                "Yapay zeka verileri yüklenirken bir hata oluştu: " + e.getMessage(), 
                "Hata", 
                JOptionPane.ERROR_MESSAGE);
            
            // Hata durumunda basit örnek veri kullan
            useExampleDataForFatura();
        }
    }
    
    // Ödeme eklemede kullanım için, fatura ID'sini barkod ve ürün adından çıkar
    private ObjectId getFaturaIdFromDisplay(String display) {
        try {
            // Gelen string formati: "barkod - ürün adı - müşteri adı"
            String barkod = display.split(" - ")[0];
            
            // Bu barkoda sahip faturayı bul
            List<Fatura> faturalar = faturaDao.faturaListele();
            for (Fatura fatura : faturalar) {
                if (fatura.getFatura_barkod().equals(barkod) && fatura.getKalan_tutar() > 0) {
                    return fatura.getId();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    
    // Belirli bir müşteri adı veritabanında var mı kontrol et
    private boolean musteriVarMi(String musteriAdi) {
        try {
            List<Musteri> musteriler = musteriDao.musteriListe();
            for (Musteri m : musteriler) {
                if (m.getAd().equalsIgnoreCase(musteriAdi)) {
                    return true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
    
    // Belirli bir barkoda sahip fatura var mı kontrol et
    private boolean faturaVarMi(String barkod) {
        try {
            List<Fatura> faturalar = faturaDao.faturaListele();
            for (Fatura f : faturalar) {
                if (f.getFatura_barkod().equalsIgnoreCase(barkod)) {
                    return true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
    
    // Ödeme formu için yapay zeka ile doldurma
    private void otomatikOdemeDoldur() {
        try {
            // API'den ödeme verisi al
            String jsonResponse = apiService.generateOdeme();
            
            // Dialog oluştur ve göster
            JFrame parentFrame = (JFrame) SwingUtilities.getWindowAncestor(this);
            AIResponseDialog dialog = new AIResponseDialog(parentFrame, "Yapay Zeka Ödeme Verisi", jsonResponse);
            
            // Eğer kullanıcı onayladıysa, formu doldur
            if (dialog.showDialog()) {
                // JSON'ı parse et
                JsonObject odeme = gson.fromJson(jsonResponse, JsonObject.class);
                
                // Form alanlarını doldur
                String odemeTarihi = odeme.get("odeme_tarihi").getAsString();
                String odenenTutar = odeme.get("odenen_tutar").getAsString();
                String odemeYontemi = odeme.get("odeme_yontemi").getAsString();
                
                // Bu bilgilere sahip ödeme zaten var mı kontrol et
                if (cmbFatura.getSelectedIndex() > 0) {
                    String faturaDisplay = (String) cmbFatura.getSelectedItem();
                    ObjectId faturaId = getFaturaIdFromDisplay(faturaDisplay);
                    
                    if (faturaId != null && odemeVarMi(faturaId, odemeTarihi, odenenTutar, odemeYontemi)) {
                        JOptionPane.showMessageDialog(this, 
                            "Bu ödeme bilgileri için çok benzer bir kayıt zaten mevcut.", 
                            "Uyarı", 
                            JOptionPane.WARNING_MESSAGE);
                        return;
                    }
                }
                
                txtOdemeTarih.setText(odemeTarihi);
                txtOdenenTutar.setText(odenenTutar);
                txtOdemeYontemi.setText(odemeYontemi);
                
                JOptionPane.showMessageDialog(this, "Ödeme bilgileri formda güncellendi. Lütfen fatura seçin.", "Bilgi", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, 
                "Yapay zeka verileri yüklenirken bir hata oluştu: " + e.getMessage(), 
                "Hata", 
                JOptionPane.ERROR_MESSAGE);
            
            // Hata durumunda basit örnek veri kullan
            useExampleDataForOdeme();
        }
    }
    
    // Belirli bir ödeme var mı kontrol et
    private boolean odemeVarMi(ObjectId faturaId, String odemeTarihi, String odenenTutar, String odemeYontemi) {
        try {
            // faturaOdemeleriGetir metodu yok, tüm ödemeleri alıp filtreleyelim
            List<Odeme> tumOdemeler = odemeDao.odemeListe();
            List<Odeme> faturaOdemeleri = new ArrayList<>();
            
            // Fatura ID'sine göre filtrele
            for (Odeme odeme : tumOdemeler) {
                if (odeme.getFatura_id().equals(faturaId)) {
                    faturaOdemeleri.add(odeme);
                }
            }
            
            for (Odeme o : faturaOdemeleri) {
                // Tarih, tutar ve ödeme yöntemi aynı mı kontrol et
                if (o.getOdeme_tarihi().equals(odemeTarihi) && 
                    String.valueOf(o.getOdenen_tutar()).equals(odenenTutar) && 
                    o.getOdeme_yontemi().equals(odemeYontemi)) {
                    return true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
    
    // Müşteri ekleme
    private void musteriEkle() {
        try {
            // Form verilerini al
            String ad = txtMusteriAd.getText();
            String sehir = txtSehir.getText();
            String telefon = txtTelefon.getText();
            String dogumTarihi = txtDogumTarihi.getText();
            
            // Müşteri nesnesi oluştur
            Musteri musteri = new Musteri(ad, sehir, telefon, dogumTarihi);
            
            // Müşteriyi ekle
            musteriDao.musteriEkle(musteri);
            
            JOptionPane.showMessageDialog(this, "Müşteri başarıyla eklendi:\n" + ad + " - " + sehir, "Bilgi", JOptionPane.INFORMATION_MESSAGE);
            temizleMusteriForm();
            
            // Müşteri listesini güncelle
            musterileriYukle();
            
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, 
                "Müşteri eklenirken bir hata oluştu: " + e.getMessage(), 
                "Hata", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    // Fatura ekleme
    private void faturaEkle() {
        try {
            // Form verilerini al
            String barkod = txtBarkod.getText();
            String faturaTarihi = txtFaturaTarih.getText();
            String durum = txtDurum.getText();
            String urunAdi = txtUrunAdi.getText();
            String urunMarkasi = txtUrunMarkasi.getText();
            String satinAlmaYeri = txtSatinAlmaYeri.getText();
            String sonOdemeTarihi = txtSonOdeme.getText();
            int kalanTutar = (int) Double.parseDouble(txtKalanTutar.getText());
            
            // Seçili müşteri adını al
            String musteriAdi = (String) cmbMusteri.getSelectedItem();
            
            // Seçilen ada sahip müşteriyi bul
            List<Musteri> musteriler = musteriDao.musteriListe();
            ObjectId musteriId = null;
            
            for (Musteri musteri : musteriler) {
                if (musteri.getAd().equals(musteriAdi)) {
                    musteriId = musteri.getId();
                    break;
                }
            }
            
            if (musteriId == null) {
                JOptionPane.showMessageDialog(this, "Seçilen müşteri bilgisi bulunamadı.", "Hata", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // Fatura nesnesi oluştur
            Fatura fatura = new Fatura();
            fatura.setFatura_barkod(barkod);
            fatura.setFatura_tarihi(faturaTarihi);
            fatura.setFatura_durumu(durum);
            fatura.setFatura_urun_adi(urunAdi);
            fatura.setFatura_urun_markasi(urunMarkasi);
            fatura.setSatin_alma_yeri(satinAlmaYeri);
            fatura.setFatura_son_odeme(sonOdemeTarihi);
            fatura.setKalan_tutar(kalanTutar);
            
            // Faturayı ekle
            faturaDao.faturaEkle(fatura, musteriId);
            
            JOptionPane.showMessageDialog(this, "Fatura başarıyla eklendi:\nBarkod: " + barkod + "\nÜrün: " + urunAdi + "\nTutar: " + kalanTutar + " TL", "Bilgi", JOptionPane.INFORMATION_MESSAGE);
            temizleFaturaForm();
            
            // Fatura listesini güncelle
            faturalariYukle();
            
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, 
                "Fatura eklenirken bir hata oluştu: " + e.getMessage(), 
                "Hata", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    // Ödeme ekleme
    private void odemeEkle() {
        try {
            // Form verilerini al
            String odemeTarihi = txtOdemeTarih.getText();
            int odenenTutar = (int) Double.parseDouble(txtOdenenTutar.getText());
            String odemeYontemi = txtOdemeYontemi.getText();
            
            // Seçili fatura bilgisini al
            String faturaDisplay = (String) cmbFatura.getSelectedItem();
            
            // Fatura ID'sini burada display'den çıkar
            ObjectId faturaId = getFaturaIdFromDisplay(faturaDisplay);
            
            if (faturaId == null) {
                JOptionPane.showMessageDialog(this, "Seçilen fatura bilgisi bulunamadı.", "Hata", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // Ödeme nesnesi oluştur
            Odeme odeme = new Odeme();
            odeme.setOdeme_tarihi(odemeTarihi);
            odeme.setOdenen_tutar(odenenTutar);
            odeme.setOdeme_yontemi(odemeYontemi);
            
            // Ödemeyi ekle
            odemeDao.OdemeEkleme(odeme, faturaId);
            
            JOptionPane.showMessageDialog(this, "Ödeme başarıyla eklendi:\nTutar: " + odenenTutar + " TL\nYöntem: " + odemeYontemi, "Bilgi", JOptionPane.INFORMATION_MESSAGE);
            temizleOdemeForm();
            
            // Fatura listesini güncelle (ödeme durumları değişmiş olabilir)
            faturalariYukle();
            
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, 
                "Ödeme eklenirken bir hata oluştu: " + e.getMessage(), 
                "Hata", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    // Müşterileri ComboBox'a yükle
    private void musterileriYukle() {
        try {
            // ComboBox'ı temizle
            cmbMusteri.removeAllItems();
            cmbMusteri.addItem("Müşteri seçin...");
            
            // Tüm müşterileri al
            List<Musteri> musteriler = musteriDao.musteriListe();
            
            // Müşterileri ComboBox'a ekle
            for (Musteri musteri : musteriler) {
                // ID'leri gösterme, sadece müşteri adını göster
                String display = musteri.getAd();
                cmbMusteri.addItem(display);
            }
            
            cmbMusteri.setSelectedIndex(0);
            
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, 
                "Müşteriler yüklenirken bir hata oluştu: " + e.getMessage(), 
                "Hata", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    // Faturaları ComboBox'a yükle - Sadece kalan_tutar > 0 olanlar
    private void faturalariYukle() {
        try {
            // ComboBox'ı temizle
            cmbFatura.removeAllItems();
            cmbFatura.addItem("Fatura seçin...");
            
            // Tüm faturaları al
            List<Fatura> faturalar = faturaDao.faturaListele();
            
            // Faturaları ComboBox'a ekle (sadece ödenmemiş olanlar)
            for (Fatura fatura : faturalar) {
                if (fatura.getKalan_tutar() > 0) { // Sadece ödenmemiş faturaları göster
                    // Barkod, ürün adı ve müşteri adını göster
                    String barkod = fatura.getFatura_barkod();
                    String urunAdi = fatura.getFatura_urun_adi();
                    
                    // Müşteri adını bul
                    ObjectId musteriId = fatura.getMusteri_id();
                    String musteriAdi = "Bilinmiyor";
                    
                    if (musteriId != null) {
                        // musteriGetir metodu yok, musteriListe'den bulalım
                        List<Musteri> musteriler = musteriDao.musteriListe();
                        for (Musteri m : musteriler) {
                            if (m.getId().equals(musteriId)) {
                                musteriAdi = m.getAd();
                                break;
                            }
                        }
                    }
                    
                    String display = barkod + " - " + urunAdi + " - " + musteriAdi;
                    cmbFatura.addItem(display);
                }
            }
            
            cmbFatura.setSelectedIndex(0);
            
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, 
                "Faturalar yüklenirken bir hata oluştu: " + e.getMessage(), 
                "Hata", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    // Hata durumunda örnek müşteri verisi kullan
    private void useExampleDataForMusteri() {
        String[] adlar = {"Ahmet", "Mehmet", "Ali", "Veli", "Ayşe", "Fatma", "Zeynep", "Hayriye", "Can", "Deniz", "Gökhan", "Hakan", "İbrahim"};
        String[] soyadlar = {"Yılmaz", "Kaya", "Demir", "Çelik", "Şahin", "Yıldırım", "Öztürk", "Aydın", "Arslan", "Doğan", "Kılıç", "Çetin", "Koç"};
        
        // Rastgele müşteri bilgileri oluştur
        String ad = adlar[random.nextInt(adlar.length)] + " " + soyadlar[random.nextInt(soyadlar.length)];
        String sehir = "İstanbul";
        String telefon = "05" + (random.nextInt(3) + 3) + random.nextInt(10) + " " + 
                       random.nextInt(10) + random.nextInt(10) + random.nextInt(10) + " " + 
                       random.nextInt(10) + random.nextInt(10) + " " + 
                       random.nextInt(10) + random.nextInt(10);
        
        // Rastgele doğum tarihi oluştur (1960-2000 arası)
        int yil = 1960 + random.nextInt(40);
        int ay = 1 + random.nextInt(12);
        int gun = 1 + random.nextInt(28);
        String dogumTarihi = String.format("%04d-%02d-%02d", yil, ay, gun);
        
        // Form alanlarını doldur
        txtMusteriAd.setText(ad);
        txtSehir.setText(sehir);
        txtTelefon.setText(telefon);
        txtDogumTarihi.setText(dogumTarihi);
        
        JOptionPane.showMessageDialog(this, "Örnek müşteri bilgileri formda güncellendi.", "Bilgi", JOptionPane.INFORMATION_MESSAGE);
    }
    
    // Hata durumunda örnek fatura verisi kullan
    private void useExampleDataForFatura() {
        // Rastgele barkod oluştur
        String barkod = "";
        for (int i = 0; i < 13; i++) {
            barkod += random.nextInt(10);
        }
        
        // Rastgele tarihler oluştur (son 1 yıl içinde)
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date simdi = new Date();
        Date faturaTarihi = new Date(simdi.getTime() - (long) random.nextInt(365) * 24 * 60 * 60 * 1000);
        Date sonOdemeTarihi = new Date(faturaTarihi.getTime() + (long) (30 + random.nextInt(30)) * 24 * 60 * 60 * 1000);
        
        // Rastgele ürün ve fiyat bilgileri
        String[] urunler = {"Televizyon", "Bulaşık Makinesi", "Buzdolabı", "Çamaşır Makinesi", "Laptop", "Akıllı Telefon", "Tablet"};
        String[] markalar = {"Samsung", "Apple", "LG", "Arçelik", "Beko", "Vestel", "Bosch"};
        String[] magazalar = {"MediaMarkt", "Teknosa", "Vatan Bilgisayar", "Amazon"};
        
        String urunAdi = urunler[random.nextInt(urunler.length)];
        String urunMarkasi = markalar[random.nextInt(markalar.length)];
        String satinAlmaYeri = magazalar[random.nextInt(magazalar.length)];
        double kalanTutar = 500 + random.nextInt(9500);
        
        // Form alanlarını doldur
        txtBarkod.setText(barkod);
        txtFaturaTarih.setText(sdf.format(faturaTarihi));
        txtDurum.setText("Ödenmemiş");
        txtUrunAdi.setText(urunAdi);
        txtUrunMarkasi.setText(urunMarkasi);
        txtSatinAlmaYeri.setText(satinAlmaYeri);
        txtSonOdeme.setText(sdf.format(sonOdemeTarihi));
        txtKalanTutar.setText(String.format("%.2f", kalanTutar));
        
        JOptionPane.showMessageDialog(this, "Örnek fatura bilgileri formda güncellendi. Lütfen müşteri seçin.", "Bilgi", JOptionPane.INFORMATION_MESSAGE);
    }
    
    // Hata durumunda örnek ödeme verisi kullan
    private void useExampleDataForOdeme() {
        // Rastgele tarih oluştur (son 1 ay içinde)
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date simdi = new Date();
        Date odemeTarihi = new Date(simdi.getTime() - (long) random.nextInt(30) * 24 * 60 * 60 * 1000);
        
        // Rastgele ödeme bilgileri
        double odenenTutar = 100 + random.nextInt(900);
        String[] odemeYontemleri = {"Nakit", "Kredi Kartı", "Havale", "EFT", "Çek"};
        String odemeYontemi = odemeYontemleri[random.nextInt(odemeYontemleri.length)];
        
        // Form alanlarını doldur
        txtOdemeTarih.setText(sdf.format(odemeTarihi));
        txtOdenenTutar.setText(String.format("%.2f", odenenTutar));
        txtOdemeYontemi.setText(odemeYontemi);
        
        JOptionPane.showMessageDialog(this, "Örnek ödeme bilgileri formda güncellendi. Lütfen fatura seçin.", "Bilgi", JOptionPane.INFORMATION_MESSAGE);
    }
    
    // Formları temizleme metodları
    private void temizleMusteriForm() {
        txtMusteriAd.setText("");
        txtSehir.setText("");
        txtTelefon.setText("");
        txtDogumTarihi.setText("");
    }
    
    private void temizleFaturaForm() {
        txtBarkod.setText("");
        txtFaturaTarih.setText("");
        cmbMusteri.setSelectedIndex(0);
        txtDurum.setText("");
        txtUrunAdi.setText("");
        txtUrunMarkasi.setText("");
        txtSatinAlmaYeri.setText("");
        txtSonOdeme.setText("");
        txtKalanTutar.setText("");
    }
    
    private void temizleOdemeForm() {
        cmbFatura.setSelectedIndex(0);
        txtOdemeTarih.setText("");
        txtOdenenTutar.setText("");
        txtOdemeYontemi.setText("");
    }
} 