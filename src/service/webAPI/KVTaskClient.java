package service.webAPI;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class KVTaskClient {
    private HttpClient client;
    private final String apiToken;
    private String url;

    public KVTaskClient(String url) {
        client = HttpClient.newHttpClient();
        this.url = url;
        apiToken = "?API_TOKEN=" + register();
    }
    private String register() {
        try {
            URI uri = URI.create(url + "register");
            HttpRequest request = HttpRequest.newBuilder().uri(uri).GET().build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            int status = response.statusCode();
            if (status >= 200 && status <= 299) {
                System.out.println("Клиент успешно зарегистрирован. Код состояния: " + status);
            } else {
                System.out.println("Произошла ошибка. Код ошибки: " + status);
            }
            return response.body();
        } catch (IOException | InterruptedException ex) {
            System.out.println("Во время выполнения запроса ресурса по url-адресу: '" + url
                    + "register"
                    + "' возникла ошибка.\n"
                    + "Проверьте, пожалуйста, адрес и повторите попытку.");
        } catch (IllegalArgumentException ex) {
            System.out.println("Введённый вами адрес не соответствует формату URL. Попробуйте, пожалуйста, снова.");
        }
        return null;
    }

    public void put(String key, String value) {
        try {
            URI uri = URI.create(url + "save/" + key + apiToken);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(uri)
                    .POST(HttpRequest.BodyPublishers.ofString(value))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            int status = response.statusCode();
            if (status >= 200 && status <= 299) {
                System.out.println("Сервер успешно обработал запрос. Код состояния: " + status);
            } else {
                System.out.println("Произошла ошибка. Код ошибки: " + status);
            }
        } catch (IOException | InterruptedException ex) {
            System.out.println("Во время выполнения запроса ресурса по url-адресу: '"
                    + url + "save/" + key + apiToken + "' возникла ошибка.\n"
                    + "Проверьте, пожалуйста, адрес и повторите попытку.");
        } catch (IllegalArgumentException ex) {
            System.out.println("Введённый вами адрес не соответствует формату URL. Попробуйте, пожалуйста, снова.");
        }
    }

    public String load(String key) {
        try {
            URI uri = URI.create(url + "load/" + key + apiToken);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(uri)
                    .GET()
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return response.body();
        } catch (IOException | InterruptedException ex) {
            System.out.println("Во время выполнения запроса ресурса по url-адресу: '"
                    + url + "load/" + key + apiToken + "' возникла ошибка.\n"
                    + "Проверьте, пожалуйста, адрес и повторите попытку.");
        } catch (IllegalArgumentException ex) {
            System.out.println("Введённый вами адрес не соответствует формату URL. Попробуйте, пожалуйста, снова.");
        }
        return null;
    }
}
