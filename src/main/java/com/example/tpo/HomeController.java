package com.example.tpo;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;

public class HomeController {
    @FXML
    private TextField rollNoField;
    @FXML
    private Button searchButton;
    @FXML
    private ProgressIndicator progressIndicator;
    @FXML
    private Label errorMessageLabel;
    @FXML
    private Label personalDataTitleLabel;
    @FXML
    private Label personalDataKeysLabel;
    @FXML
    private Label personalDataValuesLabel;
    @FXML
    private Label academicRecordTitleLabel;
    @FXML
    private Label academicRecordKeysLabel;
    @FXML
    private Label academicRecordValuesLabel;
    @FXML
    private Label placementStatusTitleLabel;
    @FXML
    private Label placementStatusKeysLabel;
    @FXML
    private Label placementStatusValuesLabel;
    @FXML
    private Button logoutButton;
    private String accessToken;
    private String searchByRollNoAPILink;

    public void initialize() {
        DataSingleton data = DataSingleton.getInstance();
        accessToken = data.getAccessToken();
        searchByRollNoAPILink = data.getSearchByRollNoAPILink();

        rollNoField.setOnKeyPressed(this::handleRollNoFieldKeyPress);
    }

    private void handleRollNoFieldKeyPress(KeyEvent keyEvent) {
        if (keyEvent.getCode()==KeyCode.ENTER)
            searchButton.fire();
    }

    @FXML
    private void searchByRollNo(){

        String rollNo = rollNoField.getText().trim();

        if(rollNo.isEmpty())
            return;

        disableControls();

        try{
            String url = searchByRollNoAPILink
                    + "?accessToken=" + URLEncoder.encode(accessToken, "UTF-8")
                    + "&rollNo=" + URLEncoder.encode(rollNo, "UTF-8");

            HttpClient httpClient = HttpClients.createDefault();
            HttpGet httpGet = new HttpGet(url);

            Task<Void> task = new Task<Void>() {
                @Override
                protected Void call() throws Exception {
                    HttpResponse response = httpClient.execute(httpGet);

                    if(response.getStatusLine().getStatusCode()==200){
                        HttpEntity entity = response.getEntity();

                        if (entity!=null) {
                            String responseContent = EntityUtils.toString(entity);
                            JSONObject jsonResponse = new JSONObject(responseContent);
                            boolean success = jsonResponse.getBoolean("success");

                            if(success){

                                try{

                                    JSONObject personalData = jsonResponse.getJSONObject("personalData");
                                    JSONObject personalDataOrder = jsonResponse.getJSONObject("personalDataOrder");

                                    String personalDataKeysText = "";
                                    String personalDataValuesText = "";
                                    for(int i=1; ; i++){
                                        if(personalDataOrder.has(Integer.toString(i))){
                                            String key = (String) personalDataOrder.get(Integer.toString(i));
                                            if(!personalData.get(key).equals("")){
                                                personalDataKeysText += key + "\n";
                                                personalDataValuesText += personalData.get(key) + "\n";
                                            }
                                        }else
                                            break;
                                    }
                                    String personalDataKeys = personalDataKeysText;
                                    String personalDataValues = personalDataValuesText;

                                    JSONObject academicRecord = jsonResponse.getJSONObject("academicRecord");
                                    JSONObject academicRecordOrder = jsonResponse.getJSONObject("academicRecordOrder");

                                    String academicRecordKeysText = "";
                                    String academicRecordValuesText = "";
                                    for(int i=1; ; i++){
                                        if(academicRecordOrder.has(Integer.toString(i))){
                                            String key = (String) academicRecordOrder.get(Integer.toString(i));
                                            if(!academicRecord.get(key).equals("")){
                                                academicRecordKeysText += key + "\n";
                                                academicRecordValuesText += academicRecord.get(key) + "\n";
                                            }
                                        }else
                                            break;
                                    }
                                    String academicRecordKeys = academicRecordKeysText;
                                    String academicRecordValues = academicRecordValuesText;

                                    JSONObject placementStatus = jsonResponse.getJSONObject("placementStatus");

                                    String placementStatusKeysText = "";
                                    String placementStatusValuesText = "";
                                    for(String key: placementStatus.keySet()){
                                        placementStatusKeysText += key + "\n";
                                        placementStatusValuesText += placementStatus.get(key) + "\n";
                                    }
                                    String placementStatusKeys = placementStatusKeysText;
                                    String placementStatusValues = placementStatusValuesText;

                                    Platform.runLater(() -> {
                                        personalDataTitleLabel.setText("Personal Data");
                                        personalDataKeysLabel.setText(personalDataKeys);
                                        personalDataValuesLabel.setText(personalDataValues);
                                        academicRecordTitleLabel.setText("Academic Record");
                                        academicRecordKeysLabel.setText(academicRecordKeys);
                                        academicRecordValuesLabel.setText(academicRecordValues);
                                        placementStatusTitleLabel.setText("Placement Status");
                                        placementStatusKeysLabel.setText(placementStatusKeys);
                                        placementStatusValuesLabel.setText(placementStatusValues);
                                    });

                                }catch (Exception e){
                                    e.printStackTrace();
                                    Platform.runLater(() -> errorMessageLabel.setText("Data received in incorrect format"));
                                    enableControls();
                                }

                            }else{
                                String error = jsonResponse.getString("error");
                                Platform.runLater(() -> errorMessageLabel.setText(error));
                            }
                        }
                    }

                    enableControls();
                    return null;
                }
            };

            Thread thread = new Thread(task);
            thread.start();
        }catch (Exception e){
            e.printStackTrace();
            enableControls();
        }
    }

    private void disableControls(){
        searchButton.setDisable(true);
        searchButton.setFocusTraversable(false);
        rollNoField.setFocusTraversable(false);
        progressIndicator.setVisible(true);
        errorMessageLabel.setText("");
        personalDataTitleLabel.setText("");
        personalDataKeysLabel.setText("");
        personalDataValuesLabel.setText("");
        academicRecordTitleLabel.setText("");
        academicRecordKeysLabel.setText("");
        academicRecordValuesLabel.setText("");
        placementStatusTitleLabel.setText("");
        placementStatusKeysLabel.setText("");
        placementStatusValuesLabel.setText("");
    }

    private void enableControls(){
        searchButton.setDisable(false);
        progressIndicator.setVisible(false);
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

    @FXML
    private void logout() throws IOException {
        Stage stage = (Stage) logoutButton.getScene().getWindow();
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("loginScreen.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 600, 400);
        stage.setTitle("Login");
        stage.setScene(scene);
    }

}
