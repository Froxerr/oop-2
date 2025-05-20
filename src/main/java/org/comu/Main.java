package org.comu;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;

public class Main extends JFrame {
    private JTabbedPane jTabbedPane;
    private FaturaGoruntule faturaGoruntule;
    private OdemeGoruntule odemeGoruntule;
    private MusteriGoruntule musteriGoruntule;
    private FaturaGrafigi faturaGrafigi;
    private EkleGoruntule ekleGoruntule;

    public static void main(String[] args)
    {
        SwingUtilities.invokeLater(() -> new Main());
    }
    public Main() {
        setTitle("Fatura Takip Sistemi"); //burada JFrame kullanmıyoruz çünkü direkt bunu extendledik
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); //BU direkt kapama muhabbetleri
        setSize(1200,800);
        setLocationRelativeTo(null); //Herhangi bir şeye bağlı değil
        jTabbedPane = new JTabbedPane();

        // Görüntüleme sınıflarının örneklerini oluştur
        faturaGoruntule = new FaturaGoruntule();
        odemeGoruntule = new OdemeGoruntule();
        musteriGoruntule = new MusteriGoruntule();
        faturaGrafigi = new FaturaGrafigi();
        ekleGoruntule = new EkleGoruntule();

        // Oluşturulan örnekleri tab'lara ekle
        jTabbedPane.addTab("Faturaları Görüntüle", faturaGoruntule);
        jTabbedPane.addTab("Odemeleri Görüntüle", odemeGoruntule);
        jTabbedPane.addTab("Müşterileri Görüntüle", musteriGoruntule);
        jTabbedPane.addTab("Fatura Grafikleri", faturaGrafigi);
        jTabbedPane.addTab("Ekleme", ekleGoruntule);
        
        // Sekme değişimini dinle ve verileri güncelle
        jTabbedPane.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                int selectedIndex = jTabbedPane.getSelectedIndex();
                
                // Seçilen sekmeye göre verileri güncelle
                switch (selectedIndex) {
                    case 0: // Faturaları Görüntüle
                        SwingUtilities.invokeLater(() -> {
                            faturaGoruntule.verileriYukle();
                            faturaGoruntule.musterileriYukle();
                        });
                        break;
                    case 1: // Odemeleri Görüntüle
                        SwingUtilities.invokeLater(() -> {
                            odemeGoruntule.verileriYukle();
                            odemeGoruntule.faturalariYukle();
                        });
                        break;
                    case 2: // Müşterileri Görüntüle
                        SwingUtilities.invokeLater(() -> {
                            musteriGoruntule.verileriYukle();
                        });
                        break;
                    case 3: // Fatura Grafikleri
                        // Grafikleri güncelle
                        SwingUtilities.invokeLater(() -> {
                            faturaGrafigi.guncelleGrafikler();
                        });
                        break;
                    case 4: // Ekleme
                        // Buraya herhangi bir işlev eklenmeyecek
                        break;
                }
                
                System.out.println("Sekme değişti: " + jTabbedPane.getTitleAt(selectedIndex) + " sekmesi yüklendi ve veriler güncellendi.");
            }
        });
        
        add(jTabbedPane);
        setVisible(true);

        //IDLER GÖZÜKMESİN VE FATURA GRAFİĞİ KISMINA DA FİLTRELEME ÖZELLİĞİ GELSİN SON OLARAK DA TEST VE MYSQL BAĞLANTISI EKLENSİN.
    }
}