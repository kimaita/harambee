package com.kimaita.harambee.controllers;


import com.kimaita.harambee.dao.HarambeeDAO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;

import java.sql.SQLException;
import java.util.Map;

public class HomeController {

    @FXML private Label lblPendingPledges;
    @FXML private Label lblOpenRequests;
    @FXML private Label lblTotalStock;

    @FXML private ListView<String> listDeadlines;

    @FXML private PieChart chartPledgeStatus;
    @FXML private BarChart<String, Integer> chartRequests;

    private final HarambeeDAO dao = new HarambeeDAO();

    @FXML
    public void initialize() {
        loadDashboardData();
    }

    public void loadDashboardData() {
        try {
            // 1. Load KPI Cards
            int pending = dao.getPendingPledgeCount();
            int open = dao.getOpenRequestCount();
            int stock = dao.getTotalStockCount();

            lblPendingPledges.setText(String.valueOf(pending));
            lblOpenRequests.setText(String.valueOf(open));
            lblTotalStock.setText(String.valueOf(stock));

            // 2. Load Upcoming Deadlines List
            listDeadlines.setItems(FXCollections.observableArrayList(dao.getUpcomingDeadlines()));

            // 3. Load Pledge Status Pie Chart
            Map<String, Integer> pieData = dao.getPledgeStatusStats();
            ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();
            for (Map.Entry<String, Integer> entry : pieData.entrySet()) {
                pieChartData.add(new PieChart.Data(entry.getKey(), entry.getValue()));
            }
            chartPledgeStatus.setData(pieChartData);

            // 4. Load Request Trends Bar Chart
            Map<String, Integer> barData = dao.getRequestTrends();
            XYChart.Series<String, Integer> series = new XYChart.Series<>();
            series.setName("Requests per Month");

            for (Map.Entry<String, Integer> entry : barData.entrySet()) {
                series.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
            }

            chartRequests.getData().clear();
            chartRequests.getData().add(series);

        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("Error loading dashboard data: " + e.getMessage());
        }
    }
}