import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeTokenRequest;
import com.google.api.client.googleapis.auth.oauth2.GoogleRefreshTokenRequest;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.AccessToken;
import com.google.auth.oauth2.GoogleCredentials;

import java.util.Scanner;

public class SheetsServiceUtil {
    public static final String APPLICATION_NAME = "Google Sheets API Java App";
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();

    public static String getRefreshToken(String clientId, String clientSecret) throws Exception {
        String redirectUri = "urn:ietf:wg:oauth:2.0:oob";
        String authUrl = "https://accounts.google.com/o/oauth2/v2/auth"
                + "?client_id=" + clientId
                + "&redirect_uri=" + redirectUri
                + "&response_type=code"
                + "&scope=https://www.googleapis.com/auth/spreadsheets"
                + "&access_type=offline"
                + "&prompt=consent";

        System.out.println("Go to the following URL and authorize:");
        System.out.println(authUrl);

        System.out.print("Enter the authorization code: ");
        Scanner scanner = new Scanner(System.in);
        String code = scanner.nextLine();

        GoogleTokenResponse tokenResponse = new GoogleAuthorizationCodeTokenRequest(
                GoogleNetHttpTransport.newTrustedTransport(),
                JSON_FACTORY,
                "https://oauth2.googleapis.com/token",
                clientId,
                clientSecret,
                code,
                redirectUri
        ).execute();

        System.out.println("Access Token: " + tokenResponse.getAccessToken());
        System.out.println("Refresh Token: " + tokenResponse.getRefreshToken());

        return tokenResponse.getRefreshToken();
    }

    public static String getAccessTokenFromRefreshToken(String clientId, String clientSecret, String refreshToken) throws Exception {
        GoogleRefreshTokenRequest request = new GoogleRefreshTokenRequest(
                GoogleNetHttpTransport.newTrustedTransport(),
                JSON_FACTORY,
                refreshToken,
                clientId,
                clientSecret
        );

        GoogleTokenResponse response = request.execute();
        return response.getAccessToken();
    }

    public static Sheets getSheetsServiceWithAccessToken(String accessToken) throws Exception {
        GoogleCredentials credentials = GoogleCredentials.create(new AccessToken(accessToken, null));

        return new Sheets.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                JSON_FACTORY,
                new HttpCredentialsAdapter(credentials)
        )
                .setApplicationName(APPLICATION_NAME)
                .build();
    }
}
