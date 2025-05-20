package org.comu.api;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

/**
 * Yapay Zeka API'si ile iletişim kurmak için kullanılan servis sınıfı.
 * Bu sınıf, müşteri, fatura ve ödeme bilgilerinin otomatik olarak 
 * yapay zeka üzerinden oluşturulmasını sağlar.
 */
public class AIApiService {
    
    // API seçimi - Fireworks API veya HuggingFace API
    private static final boolean USE_FIREWORKS_API = true; // true: Fireworks API, false: HuggingFace API
    
    // Fireworks AI API (Yeni)
    private static final String FIREWORKS_API_URL = "https://router.huggingface.co/fireworks-ai/inference/v1/chat/completions";
    private static final String FIREWORKS_MODEL = "accounts/fireworks/models/deepseek-r1"; // Fireworks için model
    
    // HuggingFace API URL formatı (Eski)
    private static final String HUGGINGFACE_API_URL_FORMAT = "https://api-inference.huggingface.co/models/%s";
    
    // Birincil ve yedek modeller - birisi çalışmazsa diğerini dener
    private static final String[] MODELS = {
        "deepseek-ai/DeepSeek-R1",
        "distilgpt2", 
        "facebook/blenderbot-400M-distill",
        "google/flan-t5-small",
        "Helsinki-NLP/opus-mt-en-tr"
    };
    
    private static final String API_KEY = ""; // API anahtarı
    private static final boolean DEBUG_MODE = true; // Debug modu açık
    private static final boolean OFFLINE_MODE = false; // Çevrimdışı mod - true olursa API hiç çağrılmaz
    
    private final Gson gson = new Gson();
    private final Random random = new Random();
    private int currentModelIndex = 0;
    
    /**
     * Yapay zeka API'sine istek gönderir ve yanıtı alır.
     * 
     * @param endpoint İstek yapılacak endpoint (örn: "musteri", "fatura", "odeme")
     * @param prompt API'ye gönderilecek açıklama metni
     * @return API'den dönen metin, JSON formatına dönüştürülmüş olarak
     */
    private String sendRequest(String endpoint, String prompt) {
        if (OFFLINE_MODE) {
            if (DEBUG_MODE) System.out.println("Çevrimdışı mod aktif, örnek veri döndürülüyor");
            return handleFailedResponse(endpoint);
        }
        
        // Önce Fireworks API denenir (USE_FIREWORKS_API true ise)
        if (USE_FIREWORKS_API) {
            String result = sendFireworksRequest(endpoint, prompt);
            if (result != null) {
                return result;
            }
            // Fireworks başarısız olursa HuggingFace'e geri dönüş yap
            if (DEBUG_MODE) System.out.println("Fireworks API başarısız oldu, HuggingFace API'ye geçiliyor");
        }
        
        // HuggingFace API (eski yöntem) ile dene
        return sendHuggingFaceRequest(endpoint, prompt);
    }
    
    /**
     * Fireworks API'sine istek gönderir (yeni chat completion formatı)
     */
    private String sendFireworksRequest(String endpoint, String prompt) {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpPost request = new HttpPost(FIREWORKS_API_URL);
            
            // Header bilgileri ayarla
            request.setHeader("Content-Type", "application/json");
            request.setHeader("Authorization", "Bearer " + API_KEY);
            
            // Endpoint'e göre uygun prompt oluştur
            String formattedPrompt = formatPromptForEndpoint(endpoint, prompt);
            
            // İstek gövdesi oluştur - Fireworks AI formatına göre
            JsonObject requestBody = new JsonObject();
            JsonArray messagesArray = new JsonArray();
            
            // Kullanıcı mesajı ekle
            JsonObject userMessage = new JsonObject();
            userMessage.addProperty("role", "user");
            userMessage.addProperty("content", formattedPrompt);
            messagesArray.add(userMessage);
            
            requestBody.add("messages", messagesArray);
            requestBody.addProperty("model", FIREWORKS_MODEL);
            requestBody.addProperty("stream", false);
            
            // Debug: İstek detaylarını yazdır
            if (DEBUG_MODE) {
                System.out.println("=== FIREWORKS API İSTEK DETAYLARI ===");
                System.out.println("URL: " + FIREWORKS_API_URL);
                System.out.println("Model: " + FIREWORKS_MODEL);
                System.out.println("Endpoint: " + endpoint);
                System.out.println("Headers: Content-Type=application/json, Authorization=Bearer " + API_KEY.substring(0, 5) + "...");
                System.out.println("İstek Gövdesi: " + requestBody);
                System.out.println("========================");
            }
            
            // İsteği gönder
            request.setEntity(new StringEntity(requestBody.toString(), "UTF-8"));
            
            // Yanıtı al
            try (CloseableHttpResponse response = httpClient.execute(request)) {
                // Debug: HTTP durum kodunu yazdır
                if (DEBUG_MODE) {
                    System.out.println("HTTP Durum Kodu: " + response.getStatusLine().getStatusCode());
                    System.out.println("Durum Açıklaması: " + response.getStatusLine().getReasonPhrase());
                }
                
                int statusCode = response.getStatusLine().getStatusCode();
                
                // Başarılı durum kodu
                if (statusCode == 200) {
                    HttpEntity entity = response.getEntity();
                    if (entity != null) {
                        String responseText = EntityUtils.toString(entity);
                        
                        // Debug: API yanıtını yazdır
                        if (DEBUG_MODE) {
                            System.out.println("Fireworks API yanıtı: " + 
                                (responseText.length() > 100 ? responseText.substring(0, 100) + "..." : responseText));
                        }
                        
                        // Boş yanıtları kontrol et
                        if (responseText.trim().isEmpty()) {
                            if (DEBUG_MODE) System.out.println("Fireworks API boş yanıt döndürdü");
                            return null;
                        }
                        
                        // Fireworks yanıt formatını işle
                        try {
                            JsonObject jsonResponse = JsonParser.parseString(responseText).getAsJsonObject();
                            
                            // "choices" dizisini kontrol et
                            if (jsonResponse.has("choices") && jsonResponse.get("choices").isJsonArray()) {
                                JsonArray choices = jsonResponse.get("choices").getAsJsonArray();
                                if (choices.size() > 0) {
                                    JsonObject firstChoice = choices.get(0).getAsJsonObject();
                                    if (firstChoice.has("message") && 
                                        firstChoice.get("message").getAsJsonObject().has("content")) {
                                        
                                        String content = firstChoice.get("message")
                                                                    .getAsJsonObject()
                                                                    .get("content")
                                                                    .getAsString();
                                        
                                        if (DEBUG_MODE) {
                                            System.out.println("Fireworks API içerik: " + content);
                                        }
                                        
                                        // İçerikte JSON formatı olup olmadığını kontrol et
                                        if (content.trim().startsWith("{") && content.trim().endsWith("}")) {
                                            try {
                                                // İçerik zaten JSON formatında, doğrudan kullan
                                                JsonObject jsonContent = JsonParser.parseString(content).getAsJsonObject();
                                                
                                                // Beklenen tüm anahtarların olup olmadığını kontrol et
                                                boolean hasAllKeys = false;
                                                if (endpoint.equals("musteri")) {
                                                    hasAllKeys = jsonContent.has("musteri_adi") && 
                                                                jsonContent.has("sehir") && 
                                                                jsonContent.has("telefon") && 
                                                                jsonContent.has("dogum_tarihi");
                                                } else if (endpoint.equals("fatura")) {
                                                    hasAllKeys = jsonContent.has("barkod") && 
                                                                jsonContent.has("fatura_tarihi") && 
                                                                jsonContent.has("durum") && 
                                                                jsonContent.has("urun_adi");
                                                } else if (endpoint.equals("odeme")) {
                                                    hasAllKeys = jsonContent.has("odeme_tarihi") && 
                                                                jsonContent.has("odenen_tutar") && 
                                                                jsonContent.has("odeme_yontemi");
                                                }
                                                
                                                if (hasAllKeys) {
                                                    if (DEBUG_MODE) {
                                                        System.out.println("API içeriğinde tam JSON bulundu: " + content);
                                                    }
                                                    return content; // JSON formatında içerik doğrudan döndürülüyor
                                                } else {
                                                    if (DEBUG_MODE) {
                                                        System.out.println("API içeriğinde JSON bulundu ama tüm alanlar yok");
                                                    }
                                                }
                                            } catch (Exception e) {
                                                if (DEBUG_MODE) {
                                                    System.out.println("İçerik JSON benzeri ama parse edilemedi: " + e.getMessage());
                                                }
                                            }
                                        }
                                        
                                        // Düz yanıt metni için JSON ayrıştırma denenecek
                                        return extractJsonFromText(content, endpoint);
                                    }
                                }
                            }
                            
                            if (DEBUG_MODE) {
                                System.out.println("Fireworks API yanıt formatı beklendiği gibi değil");
                            }
                        } catch (Exception e) {
                            if (DEBUG_MODE) {
                                System.out.println("Fireworks API yanıt işleme hatası: " + e.getMessage());
                            }
                        }
                    }
                } else {
                    if (DEBUG_MODE) {
                        System.out.println("Fireworks API hatası: " + statusCode);
                    }
                }
            }
        } catch (Exception e) {
            if (DEBUG_MODE) {
                System.err.println("=== FIREWORKS API HATASI ===");
                System.err.println("Hata mesajı: " + e.getMessage());
                e.printStackTrace();
                System.err.println("=================");
            }
        }
        
        return null;  // İstek başarısız olduysa null döndür
    }
    
    /**
     * LLM yanıtını JSON formatına çevirir
     */
    private String extractJsonFromText(String text, String endpoint) {
        if (DEBUG_MODE) {
            System.out.println("=== METİNDEN JSON ÇIKARILIYOR ===");
            System.out.println("Endpoint: " + endpoint);
            System.out.println("Orijinal metin: " + text);
        }
        
        // LLM bazen yanıtında ```json ve ``` blokları kullanabilir
        if (text.contains("```json")) {
            int startIndex = text.indexOf("```json") + 7;
            int endIndex = text.indexOf("```", startIndex);
            if (endIndex > startIndex) {
                String jsonText = text.substring(startIndex, endIndex).trim();
                if (DEBUG_MODE) {
                    System.out.println("JSON bloğu bulundu: " + jsonText);
                }
                try {
                    JsonObject jsonObject = JsonParser.parseString(jsonText).getAsJsonObject();
                    return jsonObject.toString();
                } catch (Exception e) {
                    if (DEBUG_MODE) {
                        System.out.println("JSON bloğu parse edilemedi: " + e.getMessage());
                    }
                }
            }
        }
        
        // Metinde JSON objesi ara (buna benzer: {"key":"value"})
        int curlyBraceOpenIndex = text.indexOf('{');
        int curlyBraceCloseIndex = text.lastIndexOf('}');
        
        if (curlyBraceOpenIndex >= 0 && curlyBraceCloseIndex > curlyBraceOpenIndex) {
            String possibleJson = text.substring(curlyBraceOpenIndex, curlyBraceCloseIndex + 1);
            try {
                JsonObject jsonObject = JsonParser.parseString(possibleJson).getAsJsonObject();
                if (DEBUG_MODE) {
                    System.out.println("Metin içinde JSON bulundu: " + possibleJson);
                }
                return possibleJson;
            } catch (Exception e) {
                if (DEBUG_MODE) {
                    System.out.println("Metnin içindeki JSON parse edilemedi: " + e.getMessage());
                }
            }
        }
        
        // Regex ile anahtar-değer çiftlerini ayıkla
        JsonObject result = new JsonObject();
        
        if (endpoint.equals("musteri")) {
            String musteriAdi = extractSmartValue(text, "musteri_adi|ad soyad|isim|ad|name", generateRandomName());
            String sehir = extractSmartValue(text, "sehir|şehir|city|il", getRandomTurkishCity());
            String telefon = extractSmartValue(text, "telefon|phone|tel", generateRandomPhone());
            String dogumTarihi = extractSmartValue(text, "dogum_tarihi|doğum tarihi|doğum|birth date", generateRandomDate(1960, 2000));
            
            if (DEBUG_MODE) {
                System.out.println("Ayıklanan değerler:");
                System.out.println("Müşteri Adı: " + musteriAdi);
                System.out.println("Şehir: " + sehir);
                System.out.println("Telefon: " + telefon);
                System.out.println("Doğum Tarihi: " + dogumTarihi);
            }
            
            result.addProperty("musteri_adi", musteriAdi);
            result.addProperty("sehir", sehir);
            result.addProperty("telefon", telefon);
            result.addProperty("dogum_tarihi", dogumTarihi);
            
        } else if (endpoint.equals("fatura")) {
            String barkod = extractSmartValue(text, "barkod|barcode", generateRandomBarcode());
            String faturaTarihi = extractSmartValue(text, "fatura_tarihi|fatura tarihi|invoice date", generateRandomDate(2022, 2023));
            String durum = extractSmartValue(text, "durum|status", "Ödenmemiş");
            String urunAdi = extractSmartValue(text, "urun_adi|ürün adı|ürün|product", getRandomProductName());
            String urunMarkasi = extractSmartValue(text, "urun_markasi|marka|brand", getRandomBrand());
            String satinAlmaYeri = extractSmartValue(text, "satin_alma_yeri|satın alma yeri|mağaza|store", getRandomStore());
            String sonOdemeTarihi = extractSmartValue(text, "son_odeme_tarihi|son ödeme tarihi|due date", generateRandomDate(2023, 2024));
            String kalanTutar = extractSmartValue(text, "kalan_tutar|kalan tutar|tutar|amount", String.format("%.2f", 1000 + random.nextDouble() * 9000));
            
            result.addProperty("barkod", barkod);
            result.addProperty("fatura_tarihi", faturaTarihi);
            result.addProperty("durum", durum);
            result.addProperty("urun_adi", urunAdi);
            result.addProperty("urun_markasi", urunMarkasi);
            result.addProperty("satin_alma_yeri", satinAlmaYeri);
            result.addProperty("son_odeme_tarihi", sonOdemeTarihi);
            result.addProperty("kalan_tutar", kalanTutar);
            
        } else if (endpoint.equals("odeme")) {
            String odemeTarihi = extractSmartValue(text, "odeme_tarihi|ödeme tarihi|payment date", generateRandomDate(2023, 2024));
            String odenenTutar = extractSmartValue(text, "odenen_tutar|ödenen tutar|tutar|amount", String.format("%.2f", 100 + random.nextDouble() * 3000));
            String odemeYontemi = extractSmartValue(text, "odeme_yontemi|ödeme yöntemi|ödeme şekli|payment method", getRandomPaymentMethod());
            
            result.addProperty("odeme_tarihi", odemeTarihi);
            result.addProperty("odenen_tutar", odenenTutar);
            result.addProperty("odeme_yontemi", odemeYontemi);
        }
        
        if (DEBUG_MODE) {
            System.out.println("Oluşturulan JSON: " + gson.toJson(result));
        }
        
        return gson.toJson(result);
    }
    
    /**
     * Metinden akıllı eşleştirme ile değer çıkarır
     * Birden fazla anahtar kelime desteği ile daha esnek eşleştirme yapar
     */
    private String extractSmartValue(String text, String keyPattern, String defaultValue) {
        if (DEBUG_MODE) {
            System.out.println("Akıllı arama: " + keyPattern);
        }
        
        // Anahtar kelime alternatifleri
        String[] keys = keyPattern.split("\\|");
        
        // Her alternatif için dene
        for (String key : keys) {
            // 1. Anahtar: değer formatı
            String pattern1 = "\"?" + key + "\"?\\s*[:\\-=\"]\\s*\"?([^\"\\n,\\}]+)\"?";
            java.util.regex.Pattern r1 = java.util.regex.Pattern.compile(pattern1, java.util.regex.Pattern.CASE_INSENSITIVE);
            java.util.regex.Matcher m1 = r1.matcher(text);
            
            if (m1.find()) {
                String found = m1.group(1).trim().replaceAll("[\":]$", "");
                if (DEBUG_MODE) {
                    System.out.println("Tip 1 eşleşme bulundu '" + key + "': " + found);
                }
                return found;
            }
            
            // 2. İki nokta üst üste ile ayrılmış değerler
            String pattern2 = key + "\\s*:\\s*([^\\n,\\}]+)";
            java.util.regex.Pattern r2 = java.util.regex.Pattern.compile(pattern2, java.util.regex.Pattern.CASE_INSENSITIVE);
            java.util.regex.Matcher m2 = r2.matcher(text);
            
            if (m2.find()) {
                String found = m2.group(1).trim().replaceAll("[\":]$", "");
                if (DEBUG_MODE) {
                    System.out.println("Tip 2 eşleşme bulundu '" + key + "': " + found);
                }
                return found;
            }
        }
        
        if (DEBUG_MODE) {
            System.out.println("Hiçbir değer bulunamadı, varsayılan kullanılıyor: " + defaultValue);
        }
        return defaultValue;
    }
    
    /**
     * Endpoint'e göre uygun prompt formatı oluştur
     */
    private String formatPromptForEndpoint(String endpoint, String basePrompt) {
        switch (endpoint) {
            case "musteri":
                return "Lütfen Türkiye'de yaşayan gerçek bir kişi için müşteri bilgileri oluşturun. " +
                       "Sadece belirtilen bilgileri JSON formatında verin: " +
                       "{\"musteri_adi\": \"Ad Soyad\", " +
                       "\"sehir\": \"Şehir adı\", " + 
                       "\"telefon\": \"05XX XXX XX XX\", " +
                       "\"dogum_tarihi\": \"YYYY-MM-DD\"}";
                       
            case "fatura":
                return "Lütfen gerçekçi bir elektronik ürün faturası bilgileri oluşturun. " +
                       "Sadece belirtilen bilgileri JSON formatında verin: " +
                       "{\"barkod\": \"13 rakamlı kod\", " +
                       "\"fatura_tarihi\": \"YYYY-MM-DD\", " +
                       "\"durum\": \"Ödenmemiş\", " +
                       "\"urun_adi\": \"Ürün adı\", " +
                       "\"urun_markasi\": \"Marka\", " +
                       "\"satin_alma_yeri\": \"Mağaza adı\", " +
                       "\"son_odeme_tarihi\": \"YYYY-MM-DD\", " +
                       "\"kalan_tutar\": \"Rakam\"}";
                       
            case "odeme":
                return "Lütfen gerçekçi bir ödeme bilgisi oluşturun. " +
                       "Sadece belirtilen bilgileri JSON formatında verin: " +
                       "{\"odeme_tarihi\": \"YYYY-MM-DD\", " +
                       "\"odenen_tutar\": \"Rakam\", " +
                       "\"odeme_yontemi\": \"Ödeme şekli\"}";
                       
            default:
                return basePrompt;
        }
    }
    
    /**
     * HuggingFace API'sine istek gönderir (eski metod)
     */
    private String sendHuggingFaceRequest(String endpoint, String prompt) {
        // Tüm modelleri sırayla dene
        for (int i = 0; i < MODELS.length; i++) {
            int modelIndex = (currentModelIndex + i) % MODELS.length;
            String model = MODELS[modelIndex];
            String apiUrl = String.format(HUGGINGFACE_API_URL_FORMAT, model);
            
            try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
                HttpPost request = new HttpPost(apiUrl);
                
                // Header bilgileri ayarla
                request.setHeader("Content-Type", "application/json");
                request.setHeader("Authorization", "Bearer " + API_KEY);
                
                // İstek gövdesi oluştur - HuggingFace formatına göre
                JsonObject requestBody = new JsonObject();
                
                // İstek parametrelerini modele göre özelleştir
                if (model.equals("gpt2") || model.equals("distilgpt2")) {
                    // Text generation için tek prompt formatı kullan
                    requestBody.addProperty("inputs", prompt);
                    requestBody.addProperty("max_new_tokens", 100);
                    requestBody.addProperty("temperature", 0.7);
                    requestBody.addProperty("num_return_sequences", 1);
                }
                else if (model.contains("blenderbot")) {
                    // Sohbet modellerinde inputs kısmı farklı
                    requestBody.addProperty("inputs", prompt);
                    requestBody.addProperty("max_length", 100);
                }
                else if (model.contains("flan-t5")) {
                    // T5 modelleri için
                    requestBody.addProperty("inputs", prompt);
                }
                else if (model.contains("opus-mt")) {
                    // Çeviri modelleri için
                    requestBody.addProperty("inputs", prompt);
                }
                else {
                    // Genel durum için
                    requestBody.addProperty("inputs", prompt);
                }
                
                // Debug: İstek detaylarını yazdır
                if (DEBUG_MODE) {
                    System.out.println("=== API İSTEK DETAYLARI ===");
                    System.out.println("URL: " + apiUrl);
                    System.out.println("Model: " + model);
                    System.out.println("Endpoint: " + endpoint);
                    System.out.println("Headers: Content-Type=application/json, Authorization=Bearer " + API_KEY.substring(0, 5) + "...");
                    System.out.println("İstek Gövdesi: " + requestBody);
                    System.out.println("========================");
                }
                
                // İsteği gönder
                request.setEntity(new StringEntity(requestBody.toString(), "UTF-8"));
                
                // Yanıtı al
                try (CloseableHttpResponse response = httpClient.execute(request)) {
                    // Debug: HTTP durum kodunu yazdır
                    if (DEBUG_MODE) {
                        System.out.println("HTTP Durum Kodu: " + response.getStatusLine().getStatusCode());
                        System.out.println("Durum Açıklaması: " + response.getStatusLine().getReasonPhrase());
                    }
                    
                    int statusCode = response.getStatusLine().getStatusCode();
                    
                    // Başarılı durum kodu
                    if (statusCode == 200) {
                        HttpEntity entity = response.getEntity();
                        if (entity != null) {
                            String responseText = EntityUtils.toString(entity);
                            
                            // Debug: API yanıtını yazdır
                            if (DEBUG_MODE) {
                                System.out.println("API yanıtı: " + 
                                    (responseText.length() > 100 ? responseText.substring(0, 50) + "..." : responseText));
                            }
                            
                            // Boş yanıtları kontrol et
                            if (responseText.trim().isEmpty()) {
                                if (DEBUG_MODE) System.out.println("API boş yanıt döndürdü");
                                continue;  // Sonraki modeli dene
                            }
                            
                            // Yanıtın embedding vektörü (sayısal dizi) olup olmadığını kontrol et
                            if (responseText.trim().startsWith("[") && responseText.contains(",") && 
                                responseText.matches(".*[\\[\\d\\.-]+.*") && !responseText.contains("\"generated_text\"")) {
                                
                                // Embedding vektörü yanıtı, bu modeli atla
                                if (DEBUG_MODE) {
                                    System.out.println("Model embedding vektörü döndürdü, başka bir model deneniyor");
                                }
                                continue;  // Sonraki modeli dene
                            }
                            
                            // Metin yanıtından gerekli bilgileri çıkarıp JSON formatına dönüştür
                            currentModelIndex = modelIndex;  // Başarılı olan modeli hatırla
                            return convertTextResponseToJson(responseText, endpoint);
                        }
                    } 
                    // 401 - Unauthorized (API key sorunu)
                    else if (statusCode == 401) {
                        if (DEBUG_MODE) {
                            System.out.println("API anahtarı geçersiz. Lütfen HuggingFace API anahtarınızı kontrol edin.");
                        }
                    }
                    // 404 - Not Found (model veya endpoint bulunamadı)
                    else if (statusCode == 404) {
                        if (DEBUG_MODE) {
                            System.out.println("Model bulunamadı: " + model + ". Başka bir model deneniyor...");
                        }
                    }
                    // 503 - Service Unavailable (model yükleniyor veya meşgul)
                    else if (statusCode == 503) {
                        if (DEBUG_MODE) {
                            System.out.println("Model şu anda meşgul veya yükleniyor: " + model + ". Başka bir model deneniyor...");
                        }
                    }
                    // Diğer hatalar
                    else {
                        if (DEBUG_MODE) {
                            System.out.println("API hatası: " + statusCode + " - " + response.getStatusLine().getReasonPhrase());
                        }
                    }
                }
            } catch (Exception e) {
                if (DEBUG_MODE) {
                    System.err.println("=== API HATASI ===");
                    System.err.println("Model: " + model);
                    System.err.println("Hata mesajı: " + e.getMessage());
                    e.printStackTrace();
                    System.err.println("=================");
                }
            }
        }
        
        // Tüm modeller başarısız olursa örnek veri döndür
        if (DEBUG_MODE) System.out.println("Tüm modeller başarısız oldu, örnek veri döndürülüyor");
        return handleFailedResponse(endpoint);
    }
    
    /**
     * Başarısız API yanıtı için alternatif veri üretir
     */
    private String handleFailedResponse(String endpoint) {
        if (DEBUG_MODE) System.out.println("API başarısız oldu, örnek veri döndürülüyor: " + endpoint);
        
        // Hata durumunda endpoint'e göre örnek veri döndür
        if ("musteri".equals(endpoint)) {
            return createSampleMusteri();
        } else if ("fatura".equals(endpoint)) {
            return createSampleFatura();
        } else if ("odeme".equals(endpoint)) {
            return createSampleOdeme();
        }
        
        return "{}"; // Boş JSON objesi döndür (hata durumunda)
    }
    
    /**
     * API'den gelen metin yanıtını JSON formatına dönüştürür
     * 
     * @param responseText API yanıt metni
     * @param endpoint İstek yapılan endpoint
     * @return JSON formatına dönüştürülmüş yanıt
     */
    private String convertTextResponseToJson(String responseText, String endpoint) {
        // HuggingFace'in yanıt formatı genellikle metin içerebilir
        // Bu metni parse edip istediğimiz JSON formatına çevirelim
        JsonObject result = new JsonObject();
        
        if (DEBUG_MODE) {
            System.out.println("=== JSON DÖNÜŞÜM BAŞLADI ===");
            System.out.println("Endpoint: " + endpoint);
            System.out.println("Yanıt metni ilk 100 karakter: " + 
                    (responseText.length() > 100 ? responseText.substring(0, 100) + "..." : responseText));
        }
        
        try {
            // Yanıtın embedding vektörü (sayısal dizi) olup olmadığını kontrol et
            if (responseText.trim().startsWith("[") && responseText.contains(",") && 
                responseText.matches(".*[\\[\\d\\.-]+.*")) {
                
                // Sayısal değer dizisi yanıtı - embedding vektörü, bunu işlemeyi atla
                if (DEBUG_MODE) {
                    System.out.println("Embedding vektörü tespit edildi, standart yanıt kullanılacak");
                }
                
                // Direkt olarak örnek veri oluştur
                addDefaultValues(result, endpoint);
                return gson.toJson(result);
            }
            
            // API yanıtında "generated_text" alanı arıyoruz - GPT modellerinde standart çıktı
            if (responseText.contains("generated_text")) {
                try {
                    // GPT modeli formatı
                    if (responseText.startsWith("[")) {
                        // Dizi formatında yanıt
                        JsonArray jsonArray = JsonParser.parseString(responseText).getAsJsonArray();
                        if (jsonArray.size() > 0) {
                            String generatedText = jsonArray.get(0).getAsJsonObject()
                                                .get("generated_text").getAsString();
                            extractInfoFromText(result, generatedText, endpoint);
                        }
                    } else {
                        // Tek obje formatında yanıt
                        JsonObject jsonObject = JsonParser.parseString(responseText).getAsJsonObject();
                        if (jsonObject.has("generated_text")) {
                            String generatedText = jsonObject.get("generated_text").getAsString();
                            extractInfoFromText(result, generatedText, endpoint);
                        }
                    }
                } catch (Exception e) {
                    if (DEBUG_MODE) {
                        System.out.println("generated_text alanı okuma hatası: " + e.getMessage());
                    }
                    // Başarısız olursa düz metin olarak dene
                    extractInfoFromText(result, responseText, endpoint);
                }
            } 
            // Diğer format denemeleri - dizi içinde düz metin
            else if (responseText.trim().startsWith("[")) {
                try {
                    JsonArray jsonArray = JsonParser.parseString(responseText).getAsJsonArray();
                    if (jsonArray.size() > 0) {
                        // İlk eleman bir object mi yoksa düz string mi
                        try {
                            // Obje içinde generated_text
                            String generatedText = jsonArray.get(0).getAsJsonObject()
                                                .get("generated_text").getAsString();
                            extractInfoFromText(result, generatedText, endpoint);
                        } catch (Exception e1) {
                            try {
                                // Düz string
                                String generatedText = jsonArray.get(0).getAsString();
                                extractInfoFromText(result, generatedText, endpoint);
                            } catch (Exception e2) {
                                // Her ikisi de değil, düz metine dön
                                extractInfoFromText(result, responseText, endpoint);
                            }
                        }
                    }
                } catch (Exception e) {
                    if (DEBUG_MODE) {
                        System.out.println("Dizi formatını okuma hatası: " + e.getMessage());
                    }
                    // Başarısız olursa düz metin olarak dene
                    extractInfoFromText(result, responseText, endpoint);
                }
            } 
            // Düz metin olabilir
            else {
                extractInfoFromText(result, responseText, endpoint);
            }
        } catch (Exception e) {
            if (DEBUG_MODE) {
                System.err.println("=== JSON DÖNÜŞÜM HATASI ===");
                System.err.println("Hata: " + e.getMessage());
                e.printStackTrace();
                System.err.println("=========================");
            }
            
            // Hata durumunda varsayılan değerleri ekle
            addDefaultValues(result, endpoint);
        }
        
        String jsonResult = gson.toJson(result);
        if (DEBUG_MODE) {
            System.out.println("Oluşturulan JSON: " + jsonResult);
            System.out.println("=== JSON DÖNÜŞÜM TAMAMLANDI ===");
        }
        
        return jsonResult;
    }
    
    /**
     * Metin yanıtından bilgi çıkarma
     */
    private void extractInfoFromText(JsonObject result, String text, String endpoint) {
        if (DEBUG_MODE) {
            System.out.println("Bilgi çıkarma başlıyor: " + endpoint);
        }
        
        if ("musteri".equals(endpoint)) {
            // Müşteri bilgilerini çıkar ve ekle
            String musteriAdi = extractValueOrDefault(text, "Ad", "İsim Soyisim");
            String sehir = extractValueOrDefault(text, "Şehir", "İstanbul");
            String telefon = extractValueOrDefault(text, "Telefon", generateRandomPhone());
            String dogumTarihi = extractValueOrDefault(text, "Doğum Tarihi", generateRandomDate(1960, 2000));
            
            if (DEBUG_MODE) {
                System.out.println("Çıkarılan müşteri bilgileri:");
                System.out.println("Müşteri Adı: " + musteriAdi);
                System.out.println("Şehir: " + sehir);
                System.out.println("Telefon: " + telefon);
                System.out.println("Doğum Tarihi: " + dogumTarihi);
            }
            
            result.addProperty("musteri_adi", musteriAdi);
            result.addProperty("sehir", sehir);
            result.addProperty("telefon", telefon);
            result.addProperty("dogum_tarihi", dogumTarihi);
            
        } else if ("fatura".equals(endpoint)) {
            // Fatura bilgilerini çıkar ve ekle
            String barkod = generateRandomBarcode();
            String faturaTarihi = extractValueOrDefault(text, "Tarih", generateRandomDate(2022, 2023));
            String urunAdi = extractValueOrDefault(text, "Ürün", "Laptop");
            String urunMarkasi = extractValueOrDefault(text, "Marka", "Samsung");
            String satinAlmaYeri = extractValueOrDefault(text, "Mağaza", "MediaMarkt");
            String sonOdemeTarihi = extractValueOrDefault(text, "Son Ödeme", generateRandomDate(2023, 2024));
            String kalanTutar = extractValueOrDefault(text, "Tutar", "5000");
            
            if (DEBUG_MODE) {
                System.out.println("Çıkarılan fatura bilgileri:");
                System.out.println("Barkod: " + barkod);
                System.out.println("Fatura Tarihi: " + faturaTarihi);
                System.out.println("Ürün: " + urunAdi);
                System.out.println("Marka: " + urunMarkasi);
                System.out.println("Mağaza: " + satinAlmaYeri);
                System.out.println("Son Ödeme Tarihi: " + sonOdemeTarihi);
                System.out.println("Kalan Tutar: " + kalanTutar);
            }
            
            result.addProperty("barkod", barkod);
            result.addProperty("fatura_tarihi", faturaTarihi);
            result.addProperty("durum", "Ödenmemiş");
            result.addProperty("urun_adi", urunAdi);
            result.addProperty("urun_markasi", urunMarkasi);
            result.addProperty("satin_alma_yeri", satinAlmaYeri);
            result.addProperty("son_odeme_tarihi", sonOdemeTarihi);
            result.addProperty("kalan_tutar", kalanTutar);
            
        } else if ("odeme".equals(endpoint)) {
            // Ödeme bilgilerini çıkar ve ekle
            String odemeTarihi = extractValueOrDefault(text, "Ödeme Tarihi", generateRandomDate(2023, 2024));
            String odenenTutar = extractValueOrDefault(text, "Tutar", "1000");
            String odemeYontemi = extractValueOrDefault(text, "Ödeme Yöntemi", "Kredi Kartı");
            
            if (DEBUG_MODE) {
                System.out.println("Çıkarılan ödeme bilgileri:");
                System.out.println("Ödeme Tarihi: " + odemeTarihi);
                System.out.println("Ödenen Tutar: " + odenenTutar);
                System.out.println("Ödeme Yöntemi: " + odemeYontemi);
            }
            
            result.addProperty("odeme_tarihi", odemeTarihi);
            result.addProperty("odenen_tutar", odenenTutar);
            result.addProperty("odeme_yontemi", odemeYontemi);
        }
    }
    
    /**
     * Varsayılan değerleri ekle
     */
    private void addDefaultValues(JsonObject result, String endpoint) {
        if (DEBUG_MODE) {
            System.out.println("Varsayılan değerler ekleniyor: " + endpoint);
        }
        
        if ("musteri".equals(endpoint)) {
            result.addProperty("musteri_adi", generateRandomName());
            result.addProperty("sehir", getRandomTurkishCity());
            result.addProperty("telefon", generateRandomPhone());
            result.addProperty("dogum_tarihi", generateRandomDate(1970, 2000));
        } else if ("fatura".equals(endpoint)) {
            result.addProperty("barkod", generateRandomBarcode());
            result.addProperty("fatura_tarihi", generateRandomDate(2022, 2023));
            result.addProperty("durum", "Ödenmemiş");
            result.addProperty("urun_adi", getRandomProductName());
            result.addProperty("urun_markasi", getRandomBrand());
            result.addProperty("satin_alma_yeri", getRandomStore());
            result.addProperty("son_odeme_tarihi", generateRandomDate(2023, 2024));
            result.addProperty("kalan_tutar", String.format("%.2f", 1000 + random.nextDouble() * 9000));
        } else if ("odeme".equals(endpoint)) {
            result.addProperty("odeme_tarihi", generateRandomDate(2023, 2024));
            result.addProperty("odenen_tutar", String.format("%.2f", 100 + random.nextDouble() * 3000));
            result.addProperty("odeme_yontemi", getRandomPaymentMethod());
        }
    }
    
    /**
     * Metin içinden belirli bir değeri çıkarır
     */
    private String extractValueOrDefault(String text, String key, String defaultValue) {
        if (DEBUG_MODE) {
            System.out.println("Aranan anahtar: " + key);
        }
        
        String pattern = key + "\\s*[:\\-=]\\s*([^\\n,]+)";
        java.util.regex.Pattern r = java.util.regex.Pattern.compile(pattern, java.util.regex.Pattern.CASE_INSENSITIVE);
        java.util.regex.Matcher m = r.matcher(text);
        
        if (m.find()) {
            String found = m.group(1).trim();
            if (DEBUG_MODE) {
                System.out.println("Bulunan değer: " + found);
            }
            return found;
        }
        
        if (DEBUG_MODE) {
            System.out.println("Değer bulunamadı, varsayılan kullanılıyor: " + defaultValue);
        }
        return defaultValue;
    }
    
    /**
     * HuggingFace'in mevcut durum API'sinden model durumunu kontrol eder
     */
    public boolean checkApiHealth() {
        try {
            // Çevrimdışı modda her zaman sağlıklı kabul et
            if (OFFLINE_MODE) return true;
            
            for (String model : MODELS) {
                String apiUrl = String.format(HUGGINGFACE_API_URL_FORMAT, model);
                HttpPost request = new HttpPost(apiUrl);
                request.setHeader("Authorization", "Bearer " + API_KEY);
                
                try (CloseableHttpClient httpClient = HttpClients.createDefault();
                     CloseableHttpResponse response = httpClient.execute(request)) {
                    int statusCode = response.getStatusLine().getStatusCode();
                    
                    // 200 OK veya 500 Internal Server (normal olabilir - model yüklenirken)
                    if (statusCode == 200 || statusCode == 500) {
                        if (DEBUG_MODE) System.out.println("API sağlık kontrolü başarılı: " + model);
                        return true;
                    }
                }
            }
            
            if (DEBUG_MODE) System.out.println("Hiçbir model ile bağlantı kurulamadı");
            return false;
        } catch (Exception e) {
            if (DEBUG_MODE) {
                System.err.println("API sağlık kontrolü hatası: " + e.getMessage());
            }
            return false;
        }
    }
    
    /**
     * API çalışmıyorsa çevrimdışı moda geçer
     */
    public void enableOfflineModeIfNeeded() {
        if (!checkApiHealth()) {
            if (DEBUG_MODE) System.out.println("API bağlantısı sağlanamadı. Çevrimdışı moda geçildi.");
            // Burada OFFLINE_MODE statik final olduğu için runtime'da değiştirilemez
            // Gerçek uygulamada bu değer static final olmayıp runtime'da değiştirilebilir olmalı
        }
    }
    
    /**
     * Rastgele barkod oluşturur
     */
    private String generateRandomBarcode() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 13; i++) {
            sb.append(random.nextInt(10));
        }
        return sb.toString();
    }
    
    /**
     * Rastgele telefon numarası oluşturur
     */
    private String generateRandomPhone() {
        return "05" + (random.nextInt(3) + 3) + random.nextInt(10) + " " + 
               random.nextInt(10) + random.nextInt(10) + random.nextInt(10) + " " + 
               random.nextInt(10) + random.nextInt(10) + " " + 
               random.nextInt(10) + random.nextInt(10);
    }
    
    /**
     * Rastgele tarih oluşturur
     */
    private String generateRandomDate(int startYear, int endYear) {
        int year = startYear + random.nextInt(endYear - startYear + 1);
        int month = 1 + random.nextInt(12);
        int day = 1 + random.nextInt(28);
        return String.format("%04d-%02d-%02d", year, month, day);
    }
    
    /**
     * Rastgele Türkçe isim oluşturur
     */
    private String generateRandomName() {
        String[] firstNames = {"Ahmet", "Mehmet", "Ayşe", "Fatma", "Ali", "Mustafa", "Zeynep", "Emine", 
                              "Hüseyin", "İbrahim", "Hatice", "Hacer", "Ömer", "Elif", "Murat", "Zehra"};
        String[] lastNames = {"Yılmaz", "Kaya", "Demir", "Çelik", "Şahin", "Yıldız", "Yıldırım", "Öztürk", 
                             "Aydın", "Özdemir", "Arslan", "Doğan", "Kılıç", "Aslan", "Çetin", "Korkmaz"};
        
        return firstNames[random.nextInt(firstNames.length)] + " " + 
               lastNames[random.nextInt(lastNames.length)];
    }
    
    /**
     * Rastgele Türkiye şehri döndürür
     */
    private String getRandomTurkishCity() {
        String[] cities = {"İstanbul", "Ankara", "İzmir", "Bursa", "Antalya", "Adana", "Konya", "Gaziantep",
                          "Şanlıurfa", "Kocaeli", "Mersin", "Diyarbakır", "Hatay", "Manisa", "Kayseri", "Samsun"};
        return cities[random.nextInt(cities.length)];
    }
    
    /**
     * Rastgele ürün adı döndürür
     */
    private String getRandomProductName() {
        String[] products = {"Laptop", "Televizyon", "Akıllı Telefon", "Tablet", "Buzdolabı", "Çamaşır Makinesi",
                           "Bulaşık Makinesi", "Fırın", "Mikrodalga Fırın", "Elektrikli Süpürge", "Klima"};
        return products[random.nextInt(products.length)];
    }
    
    /**
     * Rastgele marka adı döndürür
     */
    private String getRandomBrand() {
        String[] brands = {"Samsung", "Apple", "LG", "Sony", "Philips", "Beko", "Arçelik", "Vestel",
                          "Bosch", "Siemens", "Asus", "HP", "Dell", "Lenovo", "Huawei", "Xiaomi"};
        return brands[random.nextInt(brands.length)];
    }
    
    /**
     * Rastgele mağaza adı döndürür
     */
    private String getRandomStore() {
        String[] stores = {"MediaMarkt", "Teknosa", "Vatan Bilgisayar", "Amazon", "Hepsiburada", "Trendyol",
                          "n11", "Bimeks", "Electro World", "Arçelik Bayi", "Beko Bayi", "Apple Store"};
        return stores[random.nextInt(stores.length)];
    }
    
    /**
     * Rastgele ödeme yöntemi döndürür
     */
    private String getRandomPaymentMethod() {
        String[] methods = {"Kredi Kartı", "Banka Havalesi", "EFT", "Nakit", "Çek", "Kapıda Ödeme", "Online Ödeme"};
        return methods[random.nextInt(methods.length)];
    }
    
    /**
     * Yapay zeka kullanarak rastgele bir müşteri bilgisi oluşturur.
     * 
     * @return JSON formatında müşteri bilgisi
     */
    public String generateMusteri() {
        try {
            String prompt = "Türkiye'de yaşayan bir kişi için gerçekçi müşteri bilgileri oluştur. " +
                    "Aşağıdaki formatta cevap ver:\n" +
                    "Ad: [isim soyisim]\n" +
                    "Şehir: [şehir adı]\n" +
                    "Telefon: [telefon numarası]\n" +
                    "Doğum Tarihi: [yyyy-MM-dd formatında tarih]";
            
            return sendRequest("musteri", prompt);
        } catch (Exception e) {
            if (DEBUG_MODE) {
                System.err.println("Müşteri üretme hatası: " + e.getMessage());
                e.printStackTrace();
            }
            return createSampleMusteri();
        }
    }
    
    /**
     * Yapay zeka kullanarak rastgele bir fatura bilgisi oluşturur.
     * 
     * @return JSON formatında fatura bilgisi
     */
    public String generateFatura() {
        try {
            String prompt = "Bir elektronik eşya faturası için gerçekçi bilgiler oluştur. " +
                    "Aşağıdaki formatta cevap ver:\n" +
                    "Tarih: [yyyy-MM-dd formatında]\n" +
                    "Ürün: [elektronik ürün adı]\n" +
                    "Marka: [marka adı]\n" +
                    "Mağaza: [mağaza adı]\n" +
                    "Son Ödeme: [yyyy-MM-dd formatında]\n" +
                    "Tutar: [fiyat] TL";
            
            return sendRequest("fatura", prompt);
        } catch (Exception e) {
            if (DEBUG_MODE) {
                System.err.println("Fatura üretme hatası: " + e.getMessage());
                e.printStackTrace();
            }
            return createSampleFatura();
        }
    }
    
    /**
     * Yapay zeka kullanarak rastgele bir ödeme bilgisi oluşturur.
     * 
     * @return JSON formatında ödeme bilgisi
     */
    public String generateOdeme() {
        try {
            String prompt = "Bir ödeme işlemi için gerçekçi bilgiler oluştur. " +
                    "Aşağıdaki formatta cevap ver:\n" +
                    "Ödeme Tarihi: [yyyy-MM-dd formatında]\n" +
                    "Tutar: [ödeme tutarı] TL\n" +
                    "Ödeme Yöntemi: [ödeme yöntemi]";
            
            return sendRequest("odeme", prompt);
        } catch (Exception e) {
            if (DEBUG_MODE) {
                System.err.println("Ödeme üretme hatası: " + e.getMessage());
                e.printStackTrace();
            }
            return createSampleOdeme();
        }
    }
    
    /**
     * Örnek bir müşteri JSON verisi oluşturur.
     * Bu metod API bağlantısı olmadığında veya bir hata oluştuğunda kullanılır.
     * 
     * @return Örnek müşteri verisi içeren JSON formatında string
     */
    private String createSampleMusteri() {
        JsonObject sample = new JsonObject();
        sample.addProperty("musteri_adi", generateRandomName());
        sample.addProperty("sehir", getRandomTurkishCity());
        sample.addProperty("telefon", generateRandomPhone());
        sample.addProperty("dogum_tarihi", generateRandomDate(1970, 2000));
        
        if (DEBUG_MODE) {
            System.out.println("Örnek müşteri verisi oluşturuldu: " + gson.toJson(sample));
        }
        
        return gson.toJson(sample);
    }
    
    /**
     * Örnek bir fatura JSON verisi oluşturur.
     * Bu metod API bağlantısı olmadığında veya bir hata oluştuğunda kullanılır.
     * 
     * @return Örnek fatura verisi içeren JSON formatında string
     */
    private String createSampleFatura() {
        JsonObject sample = new JsonObject();
        sample.addProperty("barkod", generateRandomBarcode());
        sample.addProperty("fatura_tarihi", generateRandomDate(2022, 2023));
        sample.addProperty("durum", "Ödenmemiş");
        sample.addProperty("urun_adi", getRandomProductName());
        sample.addProperty("urun_markasi", getRandomBrand());
        sample.addProperty("satin_alma_yeri", getRandomStore());
        sample.addProperty("son_odeme_tarihi", generateRandomDate(2023, 2024));
        sample.addProperty("kalan_tutar", String.format("%.2f", 1000 + random.nextDouble() * 9000));
        
        if (DEBUG_MODE) {
            System.out.println("Örnek fatura verisi oluşturuldu: " + gson.toJson(sample));
        }
        
        return gson.toJson(sample);
    }
    
    /**
     * Örnek bir ödeme JSON verisi oluşturur.
     * Bu metod API bağlantısı olmadığında veya bir hata oluştuğunda kullanılır.
     * 
     * @return Örnek ödeme verisi içeren JSON formatında string
     */
    private String createSampleOdeme() {
        JsonObject sample = new JsonObject();
        sample.addProperty("odeme_tarihi", generateRandomDate(2023, 2024));
        sample.addProperty("odenen_tutar", String.format("%.2f", 100 + random.nextDouble() * 3000));
        sample.addProperty("odeme_yontemi", getRandomPaymentMethod());
        
        if (DEBUG_MODE) {
            System.out.println("Örnek ödeme verisi oluşturuldu: " + gson.toJson(sample));
        }
        
        return gson.toJson(sample);
    }
} 