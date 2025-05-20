package org.comu.api;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import javax.swing.*;
import java.awt.*;

/**
 * API'den dönen JSON yanıtını formatlı bir şekilde göstermek ve
 * kullanıcıdan onay almak için kullanılan dialog sınıfı.
 */
public class AIResponseDialog extends JDialog {

    private boolean approved = false;

    /**
     * Dialog penceresini oluşturur ve gösterir.
     *
     * @param parent Ana pencere
     * @param title Dialog başlığı
     * @param jsonResponse Görüntülenecek JSON yanıtı
     */
    public AIResponseDialog(JFrame parent, String title, String jsonResponse) {
        super(parent, title, true);
        
        // Dialog boyutları ve konumu
        setSize(500, 400);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout(10, 10));
        
        // JSON'ı formatlı bir şekilde göster
        String prettyJson = formatJson(jsonResponse);
        JTextArea textArea = new JTextArea(prettyJson);
        textArea.setEditable(false);
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane scrollPane = new JScrollPane(textArea);
        
        // Açıklama etiketi
        JLabel lblInfo = new JLabel("Yapay Zeka tarafından oluşturulan veriyi inceleyip onaylayabilirsiniz:");
        lblInfo.setBorder(BorderFactory.createEmptyBorder(10, 10, 5, 10));
        
        // Buton paneli
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnApprove = new JButton("Ekle");
        JButton btnCancel = new JButton("İptal");
        
        // Buton işlevleri
        btnApprove.addActionListener(e -> {
            approved = true;
            dispose();
        });
        
        btnCancel.addActionListener(e -> {
            approved = false;
            dispose();
        });
        
        // Butonları panele ekle
        buttonPanel.add(btnApprove);
        buttonPanel.add(btnCancel);
        
        // Bileşenleri pencereye ekle
        add(lblInfo, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    /**
     * JSON formatındaki veriyi okunabilir hale getirir.
     *
     * @param jsonString Format edilecek JSON string
     * @return Formatlı JSON string
     */
    private String formatJson(String jsonString) {
        try {
            JsonObject jsonObject = JsonParser.parseString(jsonString).getAsJsonObject();
            return new GsonBuilder().setPrettyPrinting().create().toJson(jsonObject);
        } catch (Exception e) {
            return jsonString; // Format edilemezse olduğu gibi döndür
        }
    }
    
    /**
     * Kullanıcının "Ekle" butonuna tıklayıp tıklamadığını kontrol eder.
     *
     * @return Eğer kullanıcı "Ekle" butonuna tıkladıysa true, aksi halde false
     */
    public boolean isApproved() {
        return approved;
    }
    
    /**
     * Dialog penceresini gösterir ve kullanıcının kararını bekler.
     *
     * @return Eğer kullanıcı "Ekle" butonuna tıkladıysa true, aksi halde false
     */
    public boolean showDialog() {
        setVisible(true); // Bu çağrı, dialog kapatılana kadar bloke eder
        return approved;
    }
} 