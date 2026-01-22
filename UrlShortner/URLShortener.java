import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;

class URL {
    private int URLID;
    private int userID;
    private String longURL;
    private String shortURL;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime expiresAt;
}
class User{
    private int userID;
    private String username;
    private String email;
    private String passwordHash;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}
interface IURLShortenerService {
    HashMap<String, URL> urlDatabase = new HashMap<>();
    String generateShortURL(String longURL, int userID, LocalDateTime expiresAt);
    String getLongURL(String shortURL);
    void deleteURL(String shortURL, int userID);
    void updateExpiration(String shortURL, LocalDateTime newExpiresAt, int userID);
}
class URLShortenerService implements IURLShortenerService {

    @Override
    public String generateShortURL(String longURL, int userID, LocalDateTime expiresAt) {
        //Base62 Encode logic here
        return "";
    }

    @Override
    public String getLongURL(String shortURL) {
        return "";
    }

    @Override
    public void deleteURL(String shortURL, int userID) {

    }

    @Override
    public void updateExpiration(String shortURL, LocalDateTime newExpiresAt, int userID) {

    }
}
interface IBase62EncoderService {
    String encode(int URLID);
    int decode(String shortURL);
}
class Base62EncoderService implements IBase62EncoderService {

    @Override
    public String encode(int URLID) {
        return "";
    }

    @Override
    public int decode(String shortURL) {
        return 0;
    }
}
public class URLShortener {
    public static void main(String[] args) {
        IURLShortenerService urlShortenerService = new URLShortenerService();
        String shortURL = urlShortenerService.generateShortURL("https://www.example.com/some/very/long/url", 1, LocalDateTime.now().plusDays(30));
        System.out.println("Short URL: " + shortURL);
    }
}
