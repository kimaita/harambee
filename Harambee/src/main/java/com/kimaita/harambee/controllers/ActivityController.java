package com.kimaita.harambee.controllers;


import com.kimaita.harambee.dao.HarambeeDAO;
import com.kimaita.harambee.models.DonationPledge;
import com.kimaita.harambee.models.Request;
import com.kimaita.harambee.models.User;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.sql.SQLException;
import java.util.Map;

public class ActivityController {

    // --- PLEDGES TAB ---
    @FXML private TextField txtSearchPledge;
    @FXML private ComboBox<String> cbFilterPledgeStatus;
    @FXML private TableView<DonationPledge> tblPledges;
    @FXML private TableColumn<DonationPledge, Integer> colPledgeID;
    @FXML private TableColumn<DonationPledge, String> colDonor, colPledgeItems;
    @FXML private TableColumn<DonationPledge, Object> colPledgeDate;

    // --- REQUESTS TAB ---
    @FXML private TextField txtSearchRequest;
    @FXML private ComboBox<String> cbFilterRequestStatus;
    @FXML private TableView<Request> tblRequests;
    @FXML private TableColumn<Request, Integer> colRequestID;
    @FXML private TableColumn<Request, String> colRecipient, colRequestItems;
    @FXML private TableColumn<Request, Object> colDeadline;

    // --- USERS ---
    @FXML private PieChart chartUserRoles;
    @FXML private PieChart chartDonorTypes;
    @FXML private TableView<User> tblRecentUsers;
    @FXML private TableColumn<User, Integer> colUserId;
    @FXML private TableColumn<User, String> colUserName, colUserRole, colUserEmail;
    @FXML private TableColumn<User, Object> colUserDate;

    private final HarambeeDAO dao = new HarambeeDAO();

    @FXML
    public void initialize() {
        setupPledgeTable();
        setupRequestTable();
        setupUserTable();
        refreshData();
    }

    private void setupPledgeTable() {
        colPledgeID.setCellValueFactory(new PropertyValueFactory<>("id"));
        colDonor.setCellValueFactory(new PropertyValueFactory<>("donorName"));
        colPledgeDate.setCellValueFactory(new PropertyValueFactory<>("createdAt"));
        colPledgeItems.setCellValueFactory(new PropertyValueFactory<>("itemsSummary"));

        // Status filter values
        cbFilterPledgeStatus.setItems(FXCollections.observableArrayList("All", "Pending", "Fulfilled"));
        cbFilterPledgeStatus.getSelectionModel().selectFirst();
    }

    private void setupRequestTable() {
        colRequestID.setCellValueFactory(new PropertyValueFactory<>("id"));
        colRecipient.setCellValueFactory(new PropertyValueFactory<>("recipientName"));
        colDeadline.setCellValueFactory(new PropertyValueFactory<>("createdAt")); // Using created_at for now
        colRequestItems.setCellValueFactory(new PropertyValueFactory<>("itemsSummary"));

        cbFilterRequestStatus.setItems(FXCollections.observableArrayList("All", "Open", "Closed"));
        cbFilterRequestStatus.getSelectionModel().selectFirst();
    }
    private void setupUserTable() {
        colUserId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colUserName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colUserRole.setCellValueFactory(new PropertyValueFactory<>("role"));
        colUserEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colUserDate.setCellValueFactory(new PropertyValueFactory<>("createdAt"));
    }
    private void refreshData() {
        try {
            // 1. PLEDGES
            ObservableList<DonationPledge> pledgeData = FXCollections.observableArrayList(dao.getAllPledges());
            FilteredList<DonationPledge> filteredPledges = new FilteredList<>(pledgeData, p -> true);

            txtSearchPledge.textProperty().addListener((obs, oldVal, newVal) -> {
                filteredPledges.setPredicate(pledge -> {
                    if (newVal == null || newVal.isEmpty()) return true;
                    String lower = newVal.toLowerCase();
                    return pledge.getDonorName().toLowerCase().contains(lower) ||
                            String.valueOf(pledge.getId()).contains(lower) ||
                            pledge.getItemsSummary().toLowerCase().contains(lower);
                });
            });

            SortedList<DonationPledge> sortedPledges = new SortedList<>(filteredPledges);
            sortedPledges.comparatorProperty().bind(tblPledges.comparatorProperty());
            tblPledges.setItems(sortedPledges);

            // 2. REQUESTS
            ObservableList<Request> requestData = FXCollections.observableArrayList(dao.getAllRequests());
            FilteredList<Request> filteredRequests = new FilteredList<>(requestData, r -> true);

            txtSearchRequest.textProperty().addListener((obs, oldVal, newVal) -> {
                filteredRequests.setPredicate(req -> {
                    if (newVal == null || newVal.isEmpty()) return true;
                    String lower = newVal.toLowerCase();
                    return req.getRecipientName().toLowerCase().contains(lower) ||
                            String.valueOf(req.getId()).contains(lower) ||
                            req.getItemsSummary().toLowerCase().contains(lower);
                });
            });

            SortedList<Request> sortedRequests = new SortedList<>(filteredRequests);
            sortedRequests.comparatorProperty().bind(tblRequests.comparatorProperty());
            tblRequests.setItems(sortedRequests);

            // 3. LOAD USER STATISTICS

            // A. Roles Pie Chart
            Map<String, Integer> roleStats = dao.getUserRoleStats();
            ObservableList<PieChart.Data> roleData = FXCollections.observableArrayList();
            roleStats.forEach((key, value) -> roleData.add(new PieChart.Data(key + " (" + value + ")", value)));
            chartUserRoles.setData(roleData);

            // B. Donor Types Pie Chart
            Map<String, Integer> typeStats = dao.getDonorTypeStats();
            ObservableList<PieChart.Data> typeData = FXCollections.observableArrayList();
            typeStats.forEach((key, value) -> typeData.add(new PieChart.Data(key + " (" + value + ")", value)));
            chartDonorTypes.setData(typeData);

            // C. Recent Users Table
            ObservableList<User> userList = FXCollections.observableArrayList(dao.getRecentUsers());
            tblRecentUsers.setItems(userList);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
