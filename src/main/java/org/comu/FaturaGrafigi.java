package org.comu;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import org.bson.types.ObjectId;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.labels.PieSectionLabelGenerator;
import org.jfree.chart.labels.StandardPieSectionLabelGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

/**
 * Fatura ve ödemelerle ilgili grafikleri görüntüleyen sınıf.
 * Bu sınıf üç farklı grafik gösterir:
 * 1. Pasta grafiği: Ödenen ve kalan faturaların oranı
 * 2. Çubuk grafiği: Müşterilere göre toplam fatura tutarları
 * 3. Çizgi grafiği: Aylara göre fatura dağılımı
 */
public class FaturaGrafigi extends JPanel {
    private FaturaDao faturaDao;
    private MusteriDao musteriDao;
    private OdemeDao odemeDao;
    
    // Grafik panelleri
    private ChartPanel odenenKalanPieChartPanel;
    private ChartPanel musteriTutarBarChartPanel;
    private ChartPanel aylikFaturaLineChartPanel;
    private JPanel descriptionPanel;
    
    // Tarih aralığı seçimi için bileşenler
    private JTextField txtBaslangicTarihi, txtBitisTarihi;
    private JButton btnFiltrele, btnTemizle;
    private JButton btnSonBirHafta, btnSonBirAy, btnSonBirYil;
    
    // Tarih formatı
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    
    // Filtreleme için tarih aralığı
    private Date baslangicTarihi = null;
    private Date bitisTarihi = null;
    
    /**
     * FaturaGrafigi sınıfının constructor'ı.
     * MongoDB bağlantısını kurar ve grafikleri oluşturur.
     */
    public FaturaGrafigi() {
        // MongoDB bağlantısı
        MongoClient client = MongoClients.create("mongodb://localhost:27017");
        MongoDatabase database = client.getDatabase("java_proje");
        faturaDao = new FaturaDao(database);
        musteriDao = new MusteriDao(database);
        odemeDao = new OdemeDao(database);
        
        // Panel ayarları
        setLayout(new BorderLayout(10, 10));
        
        // Tarih filtreleme panelini ekle
        add(createDateFilterPanel(), BorderLayout.NORTH);
        
        // Grafik paneli
        JPanel graphPanel = new JPanel(new GridLayout(2, 2, 10, 10));
        graphPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
        
        // Grafikleri oluştur
        odenenKalanPieChartPanel = createOdenenKalanPieChart();
        musteriTutarBarChartPanel = createMusteriTutarBarChart();
        aylikFaturaLineChartPanel = createAylikFaturaLineChart();
        descriptionPanel = createDescriptionPanel();
        
        // Grafik panellerine ekle
        graphPanel.add(odenenKalanPieChartPanel);
        graphPanel.add(musteriTutarBarChartPanel);
        graphPanel.add(aylikFaturaLineChartPanel);
        graphPanel.add(descriptionPanel);
        
        add(graphPanel, BorderLayout.CENTER);
    }
    
    /**
     * Tarih filtreleme panelini oluşturur.
     * @return Tarih filtreleme paneli
     */
    private JPanel createDateFilterPanel() {
        JPanel filterPanel = new JPanel(new BorderLayout(5, 5));
        filterPanel.setBorder(BorderFactory.createTitledBorder("Tarih Aralığı Filtresi"));
        
        // Tarih alanları için panel
        JPanel datePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        
        // Başlangıç tarihi
        datePanel.add(new JLabel("Başlangıç Tarihi:"));
        txtBaslangicTarihi = new JTextField(10);
        txtBaslangicTarihi.setText(dateFormat.format(new Date())); // Bugünün tarihi
        datePanel.add(txtBaslangicTarihi);
        
        // Bitiş tarihi
        datePanel.add(new JLabel("Bitiş Tarihi:"));
        txtBitisTarihi = new JTextField(10);
        txtBitisTarihi.setText(dateFormat.format(new Date())); // Bugünün tarihi
        datePanel.add(txtBitisTarihi);
        
        // Ana filtreleme butonları
        btnFiltrele = new JButton("Filtrele");
        btnTemizle = new JButton("Tümünü Göster");
        datePanel.add(btnFiltrele);
        datePanel.add(btnTemizle);
        
        // Kısa yol butonları için panel
        JPanel shortcutPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        shortcutPanel.add(new JLabel("Kısa Yollar:"));
        btnSonBirHafta = new JButton("Son 1 Hafta");
        btnSonBirAy = new JButton("Son 1 Ay");
        btnSonBirYil = new JButton("Son 1 Yıl");
        
        shortcutPanel.add(btnSonBirHafta);
        shortcutPanel.add(btnSonBirAy);
        shortcutPanel.add(btnSonBirYil);
        
        // Buton olayları
        btnFiltrele.addActionListener(e -> {
            try {
                baslangicTarihi = dateFormat.parse(txtBaslangicTarihi.getText());
                bitisTarihi = dateFormat.parse(txtBitisTarihi.getText());
                guncelleGrafikler();
            } catch (ParseException ex) {
                JOptionPane.showMessageDialog(this, 
                        "Lütfen geçerli bir tarih formatı girin (yyyy-MM-dd)", 
                        "Hatalı Tarih Formatı", 
                        JOptionPane.ERROR_MESSAGE);
            }
        });
        
        btnTemizle.addActionListener(e -> {
            baslangicTarihi = null;
            bitisTarihi = null;
            txtBaslangicTarihi.setText(dateFormat.format(new Date()));
            txtBitisTarihi.setText(dateFormat.format(new Date()));
            guncelleGrafikler();
        });
        
        btnSonBirHafta.addActionListener(e -> {
            Calendar cal = Calendar.getInstance();
            bitisTarihi = cal.getTime(); // Bugün
            
            cal.add(Calendar.DAY_OF_MONTH, -7); // 7 gün öncesi
            baslangicTarihi = cal.getTime();
            
            txtBaslangicTarihi.setText(dateFormat.format(baslangicTarihi));
            txtBitisTarihi.setText(dateFormat.format(bitisTarihi));
            
            guncelleGrafikler();
        });
        
        btnSonBirAy.addActionListener(e -> {
            Calendar cal = Calendar.getInstance();
            bitisTarihi = cal.getTime(); // Bugün
            
            cal.add(Calendar.MONTH, -1); // 1 ay öncesi
            baslangicTarihi = cal.getTime();
            
            txtBaslangicTarihi.setText(dateFormat.format(baslangicTarihi));
            txtBitisTarihi.setText(dateFormat.format(bitisTarihi));
            
            guncelleGrafikler();
        });
        
        btnSonBirYil.addActionListener(e -> {
            Calendar cal = Calendar.getInstance();
            bitisTarihi = cal.getTime(); // Bugün
            
            cal.add(Calendar.YEAR, -1); // 1 yıl öncesi
            baslangicTarihi = cal.getTime();
            
            txtBaslangicTarihi.setText(dateFormat.format(baslangicTarihi));
            txtBitisTarihi.setText(dateFormat.format(bitisTarihi));
            
            guncelleGrafikler();
        });
        
        // Panel birleştirme
        filterPanel.add(datePanel, BorderLayout.NORTH);
        filterPanel.add(shortcutPanel, BorderLayout.CENTER);
        
        return filterPanel;
    }
    
    /**
     * Tüm grafikleri günceller.
     */
    public void guncelleGrafikler() {
        // Grafik panellerini temizle
        Container parent = odenenKalanPieChartPanel.getParent();
        parent.remove(odenenKalanPieChartPanel);
        parent.remove(musteriTutarBarChartPanel);
        parent.remove(aylikFaturaLineChartPanel);
        
        // Yeni grafikleri oluştur
        odenenKalanPieChartPanel = createOdenenKalanPieChart();
        musteriTutarBarChartPanel = createMusteriTutarBarChart();
        aylikFaturaLineChartPanel = createAylikFaturaLineChart();
        
        // Grafikleri yeniden ekle
        parent.add(odenenKalanPieChartPanel, 0);
        parent.add(musteriTutarBarChartPanel, 1);
        parent.add(aylikFaturaLineChartPanel, 2);
        
        // Panel'i yeniden çiz
        parent.revalidate();
        parent.repaint();
    }
    
    /**
     * Belirli bir tarih aralığındaki faturaları filtreleyerek döndürür.
     * @return Filtrelenmiş fatura listesi
     */
    private List<Fatura> getFilteredFaturalar() {
        List<Fatura> tumFaturalar = faturaDao.faturaListele();
        
        // Filtreleme yoksa tüm faturaları döndür
        if (baslangicTarihi == null || bitisTarihi == null) {
            return tumFaturalar;
        }
        
        List<Fatura> filteredFaturalar = new ArrayList<>();
        SimpleDateFormat dbFormat = new SimpleDateFormat("yyyy-MM-dd");
        
        // Tarihe göre faturaları filtrele
        for (Fatura fatura : tumFaturalar) {
            try {
                Date faturaTarihi = dbFormat.parse(fatura.getFatura_tarihi());
                if (!faturaTarihi.before(baslangicTarihi) && !faturaTarihi.after(bitisTarihi)) {
                    filteredFaturalar.add(fatura);
                }
            } catch (ParseException e) {
                // Tarih ayrıştırma hatası - faturaları atla
                System.out.println("Hatalı tarih formatı: " + fatura.getFatura_tarihi());
            }
        }
        
        return filteredFaturalar;
    }
    
    /**
     * Belirli bir tarih aralığındaki ödemeleri filtreleyerek döndürür.
     * @return Filtrelenmiş ödeme listesi
     */
    private List<Odeme> getFilteredOdemeler() {
        List<Odeme> tumOdemeler = odemeDao.odemeListe();
        
        // Filtreleme yoksa tüm ödemeleri döndür
        if (baslangicTarihi == null || bitisTarihi == null) {
            return tumOdemeler;
        }
        
        List<Odeme> filteredOdemeler = new ArrayList<>();
        SimpleDateFormat dbFormat = new SimpleDateFormat("yyyy-MM-dd");
        
        // Tarihe göre ödemeleri filtrele
        for (Odeme odeme : tumOdemeler) {
            try {
                Date odemeTarihi = dbFormat.parse(odeme.getOdeme_tarihi());
                if (!odemeTarihi.before(baslangicTarihi) && !odemeTarihi.after(bitisTarihi)) {
                    filteredOdemeler.add(odeme);
                }
            } catch (ParseException e) {
                // Tarih ayrıştırma hatası - ödemeleri atla
                System.out.println("Hatalı tarih formatı: " + odeme.getOdeme_tarihi());
            }
        }
        
        return filteredOdemeler;
    }
    
    /**
     * Ödenen ve kalan fatura tutarlarının pasta grafiğini oluşturur.
     * @return ChartPanel - Pasta grafiği paneli
     */
    private ChartPanel createOdenenKalanPieChart() {
        // Fatura ve ödeme verilerini al
        List<Fatura> faturalar = getFilteredFaturalar();
        List<Odeme> odemeler = getFilteredOdemeler();
        
        // Her bir durum için fatura sayısı ve toplam tutarları hesapla
        int toplamOdenmemis = 0;     // Hiç ödeme yapılmamış faturalar
        int toplamKismenOdenmis = 0; // Kısmen ödenmiş faturalar
        int toplamOdenmis = 0;       // Tamamen ödenmiş faturalar
        
        int odenmemisFaturaSayisi = 0;
        int kismenOdenmişFaturaSayisi = 0;
        int odenmisFaturaSayisi = 0;
        
        // Ödenmiş faturaların orijinal tutarlarını hesaplamak için
        Map<ObjectId, Integer> orijinalFaturaTutarlari = new HashMap<>();
        Map<ObjectId, String> faturaDurumlari = new HashMap<>();
        
        // Önce tüm faturaları durumlarına göre sınıflandır ve verileri hazırla
        for (Fatura fatura : faturalar) {
            ObjectId faturaId = fatura.getId();
            int kalanTutar = fatura.getKalan_tutar();
            String durum = fatura.getFatura_durumu();
            
            if (durum == null) {
                durum = "Ödenmemiş"; // Varsayılan durum
            }
            
            // Fatura durumunu kaydet
            faturaDurumlari.put(faturaId, durum);
            
            // Faturanın orijinal kalan tutarını kaydet
            orijinalFaturaTutarlari.put(faturaId, kalanTutar);
        }
        
        // Ödeme tutarlarını hesapla ve fatura tutarlarına ekle
        for (Odeme odeme : odemeler) {
            ObjectId faturaId = odeme.getFatura_id();
            int odenenTutar = odeme.getOdenen_tutar();
            
            // Eğer orijinal fatura tutarı kaydedilmişse, bu ödemeyi ekle
            if (orijinalFaturaTutarlari.containsKey(faturaId)) {
                int yeniTutar = orijinalFaturaTutarlari.get(faturaId) + odenenTutar;
                orijinalFaturaTutarlari.put(faturaId, yeniTutar);
            }
        }
        
        // Şimdi duruma göre fatura tutarlarını hesapla
        for (Fatura fatura : faturalar) {
            ObjectId faturaId = fatura.getId();
            int kalanTutar = fatura.getKalan_tutar();
            String durum = faturaDurumlari.get(faturaId);
            int orijinalTutar = orijinalFaturaTutarlari.getOrDefault(faturaId, kalanTutar);
            
            // Fatura durumuna göre sınıflandırma
            if (durum.equals("Ödenmedi") || durum.equals("Ödenmemiş")) {
                toplamOdenmemis += orijinalTutar;
                odenmemisFaturaSayisi++;
            } 
            else if (durum.equals("Kısmi Ödeme") || durum.contains("Kısmi") || durum.contains("Kısmen")) {
                toplamKismenOdenmis += orijinalTutar;
                kismenOdenmişFaturaSayisi++;
            } 
            else if (durum.equals("Ödendi") || durum.equals("Ödenmiş")) {
                toplamOdenmis += orijinalTutar;
                odenmisFaturaSayisi++;
            }
        }
        
        // Kontrol çıktıları
        System.out.println("Fatura Durum İstatistikleri:");
        System.out.println("Ödenmemiş Fatura: " + odenmemisFaturaSayisi + " adet, Toplam: " + toplamOdenmemis + " TL");
        System.out.println("Kısmen Ödenmiş Fatura: " + kismenOdenmişFaturaSayisi + " adet, Toplam: " + toplamKismenOdenmis + " TL");
        System.out.println("Ödenmiş Fatura: " + odenmisFaturaSayisi + " adet, Toplam: " + toplamOdenmis + " TL");
        
        // Toplam ödeme miktarını da görüntüle
        int toplamOdemeMiktari = 0;
        for (Odeme odeme : odemeler) {
            toplamOdemeMiktari += odeme.getOdenen_tutar();
        }
        System.out.println("Toplam Ödeme Miktarı: " + toplamOdemeMiktari + " TL");
        
        // Pasta grafik verisini oluştur
        DefaultPieDataset<String> dataset = new DefaultPieDataset<>();
        
        // Tüm kategorileri her zaman ekle (değeri 0 olsa bile göster)
        dataset.setValue("Ödenmemiş", toplamOdenmemis);
        dataset.setValue("Kısmen Ödenmiş", toplamKismenOdenmis);
        dataset.setValue("Ödenmiş", toplamOdenmis);
        
        // Başlık ayarla
        String title = "Fatura Durumu Dağılımı";
        if (baslangicTarihi != null && bitisTarihi != null) {
            title += " (" + dateFormat.format(baslangicTarihi) + " - " + dateFormat.format(bitisTarihi) + ")";
        }
        
        // Pasta grafiğini oluştur
        JFreeChart chart = ChartFactory.createPieChart(
                title, 
                dataset,
                true,  // legend
                true,  // tooltips
                false  // URLs
        );
        
        // Pasta grafiği görünüm ayarları
        PiePlot<String> plot = (PiePlot<String>) chart.getPlot();
        plot.setSectionPaint("Ödenmemiş", new Color(204, 0, 0));      // Kırmızı
        plot.setSectionPaint("Kısmen Ödenmiş", new Color(255, 153, 0)); // Turuncu
        plot.setSectionPaint("Ödenmiş", new Color(0, 153, 51));       // Yeşil
        plot.setLabelFont(new Font("SansSerif", Font.PLAIN, 12));
        plot.setNoDataMessage("Veri bulunamadı");
        plot.setExplodePercent("Ödenmiş", 0.10);
        plot.setBackgroundPaint(new Color(240, 240, 240));
        
        // Etiketleri özelleştir - tutar ve yüzde göster
        PieSectionLabelGenerator labelGenerator = new StandardPieSectionLabelGenerator(
                "{0}: {1} TL ({2})", new DecimalFormat("0"), new DecimalFormat("0.0%"));
        plot.setLabelGenerator(labelGenerator);
        
        // ChartPanel oluştur
        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setBorder(BorderFactory.createTitledBorder("Fatura Durumları"));
        chartPanel.setPreferredSize(new Dimension(400, 300));
        
        return chartPanel;
    }
    
    /**
     * Müşterilere göre toplam fatura tutarlarının çubuk grafiğini oluşturur.
     * @return ChartPanel - Çubuk grafiği paneli
     */
    private ChartPanel createMusteriTutarBarChart() {
        // Veri kümesini oluştur
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        
        // Müşterileri ve faturaları al
        List<Musteri> musteriler = musteriDao.musteriListe();
        List<Fatura> faturalar = getFilteredFaturalar();
        List<Odeme> odemeler = getFilteredOdemeler();
        
        // Her müşteri için kalan tutar ve ödenen tutarı hesapla
        Map<ObjectId, Integer> musteriKalanTutarMap = new HashMap<>();
        Map<ObjectId, Integer> musteriOdenenTutarMap = new HashMap<>();
        
        // Kalan tutarları hesapla
        for (Fatura fatura : faturalar) {
            ObjectId musteriId = fatura.getMusteri_id();
            int kalanTutar = fatura.getKalan_tutar();
            
            // Müşteriye ait kalan tutarı ekle
            musteriKalanTutarMap.put(musteriId, 
                    musteriKalanTutarMap.getOrDefault(musteriId, 0) + kalanTutar);
        }
        
        // Ödenen tutarları hesapla - Faturadan müşteri ID'sini almak için
        for (Odeme odeme : odemeler) {
            ObjectId faturaId = odeme.getFatura_id();
            int odenenTutar = odeme.getOdenen_tutar();
            
            // Faturayla ilişkili müşteriyi bul
            for (Fatura fatura : faturalar) {
                if (fatura.getId().equals(faturaId)) {
                    ObjectId musteriId = fatura.getMusteri_id();
                    // Müşteriye ait ödenen tutarı ekle
                    musteriOdenenTutarMap.put(musteriId, 
                            musteriOdenenTutarMap.getOrDefault(musteriId, 0) + odenenTutar);
                    break;
                }
            }
        }
        
        // Müşteri adlarına göre grafik verisi oluştur
        for (Musteri musteri : musteriler) {
            ObjectId musteriId = musteri.getId();
            
            // Kalan tutar ve ödenen tutar değerlerini al
            int kalanTutar = musteriKalanTutarMap.getOrDefault(musteriId, 0);
            int odenenTutar = musteriOdenenTutarMap.getOrDefault(musteriId, 0);
            
            // Veriyi ekle - eğer toplam tutar (kalan + ödenen) > 0 ise
            if (kalanTutar > 0 || odenenTutar > 0) {
                dataset.addValue(kalanTutar, "Kalan Tutar", musteri.getAd());
                dataset.addValue(odenenTutar, "Ödenen Tutar", musteri.getAd());
            }
        }
        
        // Başlık ayarla
        String title = "Müşterilere Göre Fatura Tutarları";
        if (baslangicTarihi != null && bitisTarihi != null) {
            title += " (" + dateFormat.format(baslangicTarihi) + " - " + dateFormat.format(bitisTarihi) + ")";
        }
        
        // Çubuk grafiğini oluştur
        JFreeChart chart = ChartFactory.createBarChart(
                title,
                "Müşteri",
                "Tutar (TL)",
                dataset,
                PlotOrientation.VERTICAL,
                true,
                true,
                false
        );
        
        // Grafik görünüm ayarları
        CategoryPlot plot = chart.getCategoryPlot();
        plot.setBackgroundPaint(new Color(240, 240, 240));
        plot.setRangeGridlinePaint(Color.BLACK);
        
        // Çubuk renderer ayarları
        BarRenderer renderer = (BarRenderer) plot.getRenderer();
        renderer.setSeriesPaint(0, new Color(204, 0, 0));  // Kalan Tutar - Kırmızı
        renderer.setSeriesPaint(1, new Color(0, 153, 51)); // Ödenen Tutar - Yeşil
        renderer.setDrawBarOutline(true);
        renderer.setShadowVisible(false);
        
        // ChartPanel oluştur
        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setBorder(BorderFactory.createTitledBorder("Müşteri Analizi"));
        chartPanel.setPreferredSize(new Dimension(400, 300));
        
        return chartPanel;
    }
    
    /**
     * Aylara göre fatura dağılımının çizgi grafiğini oluşturur.
     * @return ChartPanel - Çizgi grafiği paneli
     */
    private ChartPanel createAylikFaturaLineChart() {
        // Veri kümesini oluştur
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        
        // Faturaları ve ödemeleri al
        List<Fatura> faturalar = getFilteredFaturalar();
        List<Odeme> odemeler = getFilteredOdemeler();
        
        // Ay bazında fatura kalan tutarları ve ödenen tutarları toplama
        Map<String, Integer> aylikKalanTutarlar = new TreeMap<>();
        Map<String, Integer> aylikOdenenTutarlar = new TreeMap<>();
        
        // Aylık kalan tutarları hesapla
        for (Fatura fatura : faturalar) {
            String faturaTarihi = fatura.getFatura_tarihi();
            int kalanTutar = fatura.getKalan_tutar();
            
            // Tarih formatı YYYY-MM-DD olduğunu varsayıyoruz
            if (faturaTarihi != null && faturaTarihi.length() >= 7) {
                String yilAy = faturaTarihi.substring(0, 7); // YYYY-MM
                aylikKalanTutarlar.put(yilAy, 
                        aylikKalanTutarlar.getOrDefault(yilAy, 0) + kalanTutar);
            }
        }
        
        // Aylık ödenen tutarları hesapla
        for (Odeme odeme : odemeler) {
            String odemeTarihi = odeme.getOdeme_tarihi();
            int odenenTutar = odeme.getOdenen_tutar();
            
            // Tarih formatı YYYY-MM-DD olduğunu varsayıyoruz
            if (odemeTarihi != null && odemeTarihi.length() >= 7) {
                String yilAy = odemeTarihi.substring(0, 7); // YYYY-MM
                aylikOdenenTutarlar.put(yilAy, 
                        aylikOdenenTutarlar.getOrDefault(yilAy, 0) + odenenTutar);
            }
        }
        
        // Tüm ayları birleştir (her iki veri setinden)
        Set<String> tumAylar = new TreeSet<>();
        tumAylar.addAll(aylikKalanTutarlar.keySet());
        tumAylar.addAll(aylikOdenenTutarlar.keySet());
        
        // Veriyi grafik için hazırla
        for (String ay : tumAylar) {
            int kalanTutar = aylikKalanTutarlar.getOrDefault(ay, 0);
            int odenenTutar = aylikOdenenTutarlar.getOrDefault(ay, 0);
            
            dataset.addValue(kalanTutar, "Kalan Tutar", ay);
            dataset.addValue(odenenTutar, "Ödenen Tutar", ay);
        }
        
        // Başlık ayarla
        String title = "Aylara Göre Fatura ve Ödeme Dağılımı";
        if (baslangicTarihi != null && bitisTarihi != null) {
            title += " (" + dateFormat.format(baslangicTarihi) + " - " + dateFormat.format(bitisTarihi) + ")";
        }
        
        // Çizgi grafiğini oluştur
        JFreeChart chart = ChartFactory.createLineChart(
                title,
                "Ay",
                "Tutar (TL)",
                dataset,
                PlotOrientation.VERTICAL,
                true,
                true,
                false
        );
        
        // Grafik görünüm ayarları
        CategoryPlot plot = chart.getCategoryPlot();
        plot.setBackgroundPaint(new Color(240, 240, 240));
        plot.setRangeGridlinePaint(Color.BLACK);
        
        // Çizgi renderer ayarları
        LineAndShapeRenderer renderer = (LineAndShapeRenderer) plot.getRenderer();
        renderer.setSeriesPaint(0, new Color(204, 0, 0));  // Kalan Tutar - Kırmızı
        renderer.setSeriesPaint(1, new Color(0, 153, 51)); // Ödenen Tutar - Yeşil
        renderer.setSeriesStroke(0, new BasicStroke(2.0f));
        renderer.setSeriesStroke(1, new BasicStroke(2.0f));
        renderer.setSeriesShapesVisible(0, true);
        renderer.setSeriesShapesVisible(1, true);
        
        // ChartPanel oluştur
        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setBorder(BorderFactory.createTitledBorder("Zamana Göre Analiz"));
        chartPanel.setPreferredSize(new Dimension(400, 300));
        
        return chartPanel;
    }
    
    /**
     * Grafikler hakkında açıklamalar içeren panel oluşturur.
     * @return JPanel - Açıklama paneli
     */
    private JPanel createDescriptionPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Grafikler Hakkında Açıklamalar"));
        
        // HTML formatında açıklamalar
        JEditorPane editorPane = new JEditorPane("text/html", 
                "<html>" +
                "<h3>Grafik Açıklamaları</h3>" +
                "<p><b>Fatura Durumları:</b> Faturaların ödeme durumlarına göre dağılımı.</p>" +
                "<ul>" +
                "  <li><span style='color:#CC0000'>Kırmızı</span>: Hiç ödeme yapılmamış faturalar (Ödenmemiş).</li>" +
                "  <li><span style='color:#FF9900'>Turuncu</span>: Kısmen ödemesi yapılmış faturalar (Kısmen Ödenmiş).</li>" +
                "  <li><span style='color:#009933'>Yeşil</span>: Tamamen ödenmiş faturalar (Ödenmiş).</li>" +
                "</ul>" +
                "<p><b>Müşteri Analizi:</b> Her müşterinin ödenen ve kalan fatura tutarlarını gösteren çubuk grafik.</p>" +
                "<ul>" +
                "  <li><span style='color:#009933'>Yeşil</span>: Müşterinin ödediği toplam tutar.</li>" +
                "  <li><span style='color:#CC0000'>Kırmızı</span>: Müşterinin kalan toplam borcu.</li>" +
                "</ul>" +
                "<p><b>Zamana Göre Analiz:</b> Aylık fatura ve ödeme tutarlarının çizgi grafiği.</p>" +
                "<ul>" +
                "  <li><span style='color:#009933'>Yeşil</span>: Her ayda yapılan toplam ödeme tutarı.</li>" +
                "  <li><span style='color:#CC0000'>Kırmızı</span>: Her ayın sonundaki kalan toplam borç.</li>" +
                "</ul>" +
                "<p><b>Tarih Filtreleme:</b> Grafikler seçilen tarih aralığına göre filtrelenebilir.</p>" +
                "<ul>" +
                "  <li>Başlangıç ve bitiş tarihi girerek özel aralık belirleyebilirsiniz.</li>" +
                "  <li>Kısa yol butonları ile hızlıca son periyodları görüntüleyebilirsiniz.</li>" +
                "  <li>Tümünü Göster butonu ile filtreleri kaldırabilirsiniz.</li>" +
                "</ul>" +
                "</html>");
        
        editorPane.setEditable(false);
        editorPane.setBackground(new Color(245, 245, 245));
        
        panel.add(new JScrollPane(editorPane), BorderLayout.CENTER);
        return panel;
    }
}
