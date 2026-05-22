import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;
import java.util.Arrays;

public class TestEncrypt {
    public static void main(String[] args) throws Exception {
        String secret = System.getenv("API_SECRET_KEY");
        if (secret == null) {
            System.out.println("Set API_SECRET_KEY first.");
            return;
        }

        String jsonPayload = "{\"to\":\"amanktor@gmail.com\",\"subject\":\"Welcome!\",\"name\":\"Aman\"}";
        
        byte[] key = secret.getBytes("UTF-8");
        key = Arrays.copyOf(key, 16); 
        SecretKeySpec secretKey = new SecretKeySpec(key, "AES");
        
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        
        String encrypted = Base64.getEncoder().encodeToString(cipher.doFinal(jsonPayload.getBytes("UTF-8")));
        System.out.println("Final Request Body to send:");
        System.out.println(encrypted);
    }
}
