package com.example.tpo;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.commons.codec.digest.DigestUtils;

import java.awt.*;
import java.io.IOException;
import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;

import org.json.JSONObject;


public class LoginController {
    @FXML
    private TextField idField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private Button loginButton;
    @FXML
    private ProgressIndicator progressIndicator;
    @FXML
    private Label errorMessageLabel;
    @FXML
    private Hyperlink websiteHyperLink;
    @FXML
    private void initialize() {
        idField.setOnKeyPressed(this::handleIdFieldKeyPress);
        passwordField.setOnKeyPressed(this::handlePasswordFieldKeyPress);
    }

    @FXML
    private void login() {

        String id = idField.getText().trim();
        String password = passwordField.getText().trim();

        if(id.isEmpty() || password.isEmpty())
            return;

        disableControls();

        try {
            String url = "";

            HttpClient httpClient = HttpClients.createDefault();
            HttpPost httpPost = new HttpPost(url);

            String hashedPwd = DigestUtils.sha256Hex(password);

            JSONObject json = new JSONObject();
            json.put("id", id);
            json.put("hashedPwd", hashedPwd);

            StringEntity data = new StringEntity(json.toString(), ContentType.APPLICATION_JSON);
            httpPost.setEntity(data);

            Task<Void> task = new Task<Void>() {
                @Override
                protected Void call() throws Exception {
                    HttpResponse response = httpClient.execute(httpPost);

                    if (response.getStatusLine().getStatusCode() == 302) {
                        Header locationHeader = response.getFirstHeader("Location");

                        if (locationHeader != null) {

                            String redirectUrl = locationHeader.getValue();
                            HttpGet httpGet = new HttpGet(redirectUrl);
                            HttpResponse redirectedResponse = httpClient.execute(httpGet);
                            HttpEntity entity = redirectedResponse.getEntity();

                            if (entity!=null) {
                                String responseContent = EntityUtils.toString(entity);
                                JSONObject jsonResponse = new JSONObject(responseContent);
                                boolean success = jsonResponse.getBoolean("success");

                                if(success){
                                    String accessToken = jsonResponse.getString("accessToken");
                                    String searchByRollNoAPILink = jsonResponse.getString("searchByRollNoAPILink");
                                    Platform.runLater(() -> {
                                            Stage stage = (Stage) loginButton.getScene().getWindow();
                                        try {
                                            DataSingleton data = DataSingleton.getInstance();
                                            data.setAccessToken(accessToken);
                                            data.setSearchByRollNoAPILink(searchByRollNoAPILink);

                                            navigateToHomeScreen(stage);
                                        } catch (IOException e) {
                                            enableControls();
                                            throw new RuntimeException(e);
                                        }
                                    });

                                }else{
                                    String error = jsonResponse.getString("error");
                                    Platform.runLater(() -> errorMessageLabel.setText(error));
                                }
                            }
                        }
                    }

                    enableControls();
                    return null;
                }
            };

            Thread thread = new Thread(task);
            thread.start();
        } catch (Exception e) {
            e.printStackTrace();
            enableControls();
        }
    }

    private void handleIdFieldKeyPress(KeyEvent keyEvent) {
        if (keyEvent.getCode()==KeyCode.ENTER)
            passwordField.requestFocus();
    }

    private void handlePasswordFieldKeyPress(KeyEvent keyEvent) {
        if (keyEvent.getCode()==KeyCode.ENTER)
            loginButton.fire();
    }

    @FXML
    private void openWebsite() {
        String url = "https://www.navedansari.in";
        try {
            Desktop.getDesktop().browse(new URI(url));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void disableControls(){
        loginButton.setDisable(true);
        progressIndicator.setVisible(true);
        errorMessageLabel.setText("");
        idField.setFocusTraversable(false);
        passwordField.setFocusTraversable(false);
        loginButton.setFocusTraversable(false);
        websiteHyperLink.setFocusTraversable(false);
    }

    private void enableControls(){
        loginButton.setDisable(false);
        progressIndicator.setVisible(false);
    }

    private void navigateToHomeScreen(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("homeScreen.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 600, 400);
        stage.setTitle("Home");
        stage.setScene(scene);
    }

}
