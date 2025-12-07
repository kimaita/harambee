package com.kimaita.harambee.dao;

import com.kimaita.harambee.models.*;
import com.kimaita.harambee.utils.AppDatabase;

import java.sql.*;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.*;

public class HarambeeDAO {

    public void registerUser(User user) throws SQLException {
        String sql = "INSERT INTO users (name, email, phone_number, address, role, entity_type, created_at) "
                + "VALUES (?, ?, ?, ?, ?::user_type, ?::entities, ?)";

        try (Connection conn = AppDatabase.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, user.getName());
            pstmt.setString(2, user.getEmail());
            pstmt.setString(3, user.getPhoneNumber());
            pstmt.setString(4, user.getAddress());
            pstmt.setString(5, user.getRole().name().toLowerCase());
            pstmt.setString(6, user.getEntityType().name().toLowerCase());
            pstmt.setObject(7, OffsetDateTime.now());

            pstmt.executeUpdate();
        }
    }

    public List<User> getUsersByRole(UserType role) throws SQLException {
        List<User> users = new ArrayList<>();
        String sql = "SELECT id, name, role FROM users WHERE role = ?::user_type";

        try (Connection conn = AppDatabase.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, role.name().toLowerCase());
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                User u = new User();
                u.setId(rs.getInt("id"));
                u.setName(rs.getString("name"));
                u.setRole(role);
                users.add(u);
            }
        }
        return users;
    }

    public List<InventoryItem> getAllInventory() throws SQLException {
        List<InventoryItem> items = new ArrayList<>();
        String sql = "SELECT i.id, i.name, i.quantity, i.units, c.name as cat_name "
                + "FROM inventory_items i LEFT JOIN item_categories c ON i.category = c.id";

        try (Connection conn = AppDatabase.getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                InventoryItem item = new InventoryItem();
                item.setId(rs.getInt("id"));
                item.setName(rs.getString("name"));
                item.setQuantity(rs.getInt("quantity"));
                item.setUnits(rs.getString("units"));
                item.setCategoryName(rs.getString("cat_name"));
                items.add(item);
            }
        }
        return items;
    }

    public int findOrCreateCategory(String categoryName) throws SQLException {
        // 1. Try to find
        String findSql = "SELECT id FROM item_categories WHERE name = ?";
        try (Connection conn = AppDatabase.getConnection(); PreparedStatement pstmt = conn.prepareStatement(findSql)) {
            pstmt.setString(1, categoryName);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("id");
            }
        }

        // 2. Create if not found
        String insertSql = "INSERT INTO item_categories (name) VALUES (?) RETURNING id";
        try (Connection conn = AppDatabase.getConnection(); PreparedStatement pstmt = conn.prepareStatement(insertSql)) {
            pstmt.setString(1, categoryName);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("id");
            }
        }
        throw new SQLException("Could not create category");
    }

    public List<InventoryItem> getInventoryList() throws SQLException {
        List<InventoryItem> items = new ArrayList<>();
        String sql = "SELECT id, name, units FROM inventory_items ORDER BY name";

        try (Connection conn = AppDatabase.getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                InventoryItem item = new InventoryItem();
                item.setId(rs.getInt("id"));
                item.setName(rs.getString("name"));
                item.setUnits(rs.getString("units"));
                items.add(item);
            }
        }
        return items;
    }

    public InventoryItem createInventoryItem(InventoryItem item) throws SQLException {
        // 1. Get/Create Category ID (Default to 'General' if null)
        int catId = findOrCreateCategory(item.getCategoryName() != null ? item.getCategoryName() : "General");

        // 2. Generate ID (Simple Max+1)
        int newId = 1;
        String idSql = "SELECT COALESCE(MAX(id), 0) + 1 FROM inventory_items";

        Connection conn = AppDatabase.getConnection();
        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(idSql)) {
            if (rs.next()) {
                newId = rs.getInt(1);
            }
        }

        String sql = "INSERT INTO inventory_items (id, name, quantity, units, category) VALUES (?, ?, 0, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, newId);
            pstmt.setString(2, item.getName());
            pstmt.setString(3, item.getUnits());
            pstmt.setInt(4, catId);
            pstmt.executeUpdate();
        }

        item.setId(newId); // Return object with ID populated
        return item;
    }


    public int findItemIdByName(String name) throws SQLException {
        String sql = "SELECT id FROM inventory_items WHERE name = ?";
        try (Connection conn = AppDatabase.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, name);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("id");
            }
        }
        // If item doesn't exist, we usually create it or throw error.
        // For simplicity, let's create it.
        InventoryItem newItem = new InventoryItem(name, "General", "units");
        createInventoryItem(newItem);
        return findItemIdByName(name);
    }

    public void createPledge(DonationPledge pledge) throws SQLException {
        Connection conn = AppDatabase.getConnection();
        // Transaction
        // 1. Insert Pledge -> Insert Pledge Items
        conn.setAutoCommit(false);

        try {
            String pledgeSql = "INSERT INTO pledges (user_id, description, created_at) VALUES (?, ?, ?) RETURNING id";
            int pledgeId;
            try (PreparedStatement pstmt = conn.prepareStatement(pledgeSql)) {
                pstmt.setInt(1, pledge.getUserId());
                pstmt.setString(2, pledge.getDescription());
                pstmt.setObject(3, OffsetDateTime.now());
                ResultSet rs = pstmt.executeQuery();
                if (!rs.next()) {
                    throw new SQLException("Failed to create pledge");
                }
                pledgeId = rs.getInt(1);
            }

            String itemSql = "INSERT INTO pledge_items (pledge_id, item_id, quantity, delivery_date) VALUES (?, ?, ?, ?)";
            try (PreparedStatement pstmt = conn.prepareStatement(itemSql)) {
                for (PledgeItem item : pledge.getItems()) {
                    int itemId = findItemIdByName(item.getItemName());
                    pstmt.setInt(1, pledgeId);
                    pstmt.setInt(2, itemId);
                    pstmt.setInt(3, item.getQuantity());
                    pstmt.setObject(4, item.getDeliveryDate());
                    pstmt.addBatch();
                }
                pstmt.executeBatch();
            }

            conn.commit();
        } catch (SQLException e) {
            conn.rollback();
            throw e;
        } finally {
            conn.setAutoCommit(true);
        }
    }

    public void createRequest(Request request) throws SQLException {
        Connection conn = AppDatabase.getConnection();
        conn.setAutoCommit(false);

        try {
            String reqSql = "INSERT INTO requests (user_id, description, created_at, updated_at, repeating) VALUES (?, ?, ?, ?, ?) RETURNING id";
            int reqId;
            try (PreparedStatement pstmt = conn.prepareStatement(reqSql)) {
                pstmt.setInt(1, request.getUserId());
                pstmt.setString(2, request.getDescription());
                pstmt.setObject(3, OffsetDateTime.now());
                pstmt.setObject(4, OffsetDateTime.now());
                pstmt.setInt(5, request.isRecurring() ? 1 : 0);
                ResultSet rs = pstmt.executeQuery();
                if (!rs.next()) {
                    throw new SQLException("Failed to create request");
                }
                reqId = rs.getInt(1);
            }

            String itemSql = "INSERT INTO request_items (request_id, item_id, quantity, date_needed) VALUES (?, ?, ?, ?)";
            try (PreparedStatement pstmt = conn.prepareStatement(itemSql)) {
                for (RequestItem item : request.getItems()) {
                    int itemId = findItemIdByName(item.getItemName());
                    pstmt.setInt(1, reqId);
                    pstmt.setInt(2, itemId);
                    pstmt.setInt(3, item.getQuantity());
                    pstmt.setObject(4, java.sql.Date.valueOf(item.getDateNeeded()));
                    pstmt.addBatch();
                }
                pstmt.executeBatch();
            }
            conn.commit();
        } catch (SQLException e) {
            conn.rollback();
            throw e;
        } finally {
            conn.setAutoCommit(true);
        }
    }

    public List<DonationPledge> getAllPledges() throws SQLException {
        List<DonationPledge> list = new ArrayList<>();
        // Using STRING_AGG to get a summary of items in one query
        String sql = """
                    SELECT p.id, p.user_id, u.name as donor_name, p.created_at,
                           STRING_AGG(i.name || ' (' || pi.quantity || ' ' || i.units || ')', ', ') as items_summary
                    FROM pledges p
                    JOIN users u ON p.user_id = u.id
                    LEFT JOIN pledge_items pi ON p.id = pi.pledge_id
                    LEFT JOIN inventory_items i ON pi.item_id = i.id
                    GROUP BY p.id, u.name, p.created_at
                    ORDER BY p.created_at DESC
                """;

        try (Connection conn = AppDatabase.getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                DonationPledge p = new DonationPledge();
                p.setId(rs.getInt("id"));
                p.setUserId(rs.getInt("user_id"));
                p.setDonorName(rs.getString("donor_name")); // Need to add this field to Pledge model
                p.setCreatedAt(rs.getObject("created_at", OffsetDateTime.class));
                p.setItemsSummary(rs.getString("items_summary")); // Need to add this field
                list.add(p);
            }
        }
        return list;
    }

    public List<Request> getAllRequests() throws SQLException {
        List<Request> list = new ArrayList<>();
        String sql = """
                    SELECT r.id, r.user_id, u.name as recipient_name, r.created_at, r.repeating,
                           STRING_AGG(i.name || ' (' || ri.quantity || ' ' || i.units || ')', ', ') as items_summary
                    FROM requests r
                    JOIN users u ON r.user_id = u.id
                    LEFT JOIN request_items ri ON r.id = ri.request_id
                    LEFT JOIN inventory_items i ON ri.item_id = i.id
                    GROUP BY r.id, u.name, r.created_at, r.repeating
                    ORDER BY r.created_at DESC
                """;

        try (Connection conn = AppDatabase.getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Request r = new Request();
                r.setId(rs.getInt("id"));
                r.setRecipientName(rs.getString("recipient_name")); // Add to Request model
                r.setCreatedAt(rs.getObject("created_at", OffsetDateTime.class));
                r.setRecurring(rs.getInt("repeating") == 1);
                r.setItemsSummary(rs.getString("items_summary")); // Add to Request model
                list.add(r);
            }
        }
        return list;
    }

    public DonationPledge getPledgeById(int id) throws SQLException {
        String sql = "SELECT p.id, p.user_id, u.name, p.created_at FROM pledges p JOIN users u ON p.user_id = u.id WHERE p.id = ?";
        try (Connection conn = AppDatabase.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                DonationPledge p = new DonationPledge();
                p.setId(rs.getInt("id"));
                p.setUserId(rs.getInt("user_id"));
                p.setDonorName(rs.getString("name"));
                p.setCreatedAt(rs.getObject("created_at", OffsetDateTime.class));
                return p;
            }
        }
        return null;
    }

    public List<PledgeItem> getPledgeItems(int pledgeId) throws SQLException {
        List<PledgeItem> items = new ArrayList<>();
        // Check if item is in 'deliveries' table to determine status
        String sql = """
                    SELECT pi.id, pi.item_id, i.name, pi.quantity, i.units,
                           CASE WHEN d.id IS NOT NULL THEN 'Delivered' ELSE 'Pending' END as status
                    FROM pledge_items pi
                    JOIN inventory_items i ON pi.item_id = i.id
                    LEFT JOIN deliveries d ON pi.id = d.pledge_item
                    WHERE pi.pledge_id = ?
                """;

        try (Connection conn = AppDatabase.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, pledgeId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                PledgeItem item = new PledgeItem(rs.getString("name"), rs.getInt("quantity"), rs.getString("units"));
                item.setId(rs.getInt("id"));
                item.setItemId(rs.getInt("item_id"));
                item.setStatus(rs.getString("status")); // Add status field to PledgeItem
                items.add(item);
            }
        }
        return items;
    }

    public void recordDelivery(int pledgeItemId, int donorId, int quantity, OffsetDateTime date) throws SQLException {
        // 1. Insert into deliveries
        String sql = "INSERT INTO deliveries (donor, pledge_item, quantity, delivery_date) VALUES (?, ?, ?, ?)";

        // 2. Update Inventory (Increase stock)
        String stockSql = "UPDATE inventory_items SET quantity = quantity + ? WHERE id = (SELECT item_id FROM pledge_items WHERE id = ?)";

        Connection conn = AppDatabase.getConnection();
        conn.setAutoCommit(false);
        try {
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, donorId);
                pstmt.setInt(2, pledgeItemId);
                pstmt.setInt(3, quantity);
                pstmt.setObject(4, date);
                pstmt.executeUpdate();
            }

            try (PreparedStatement pstmt = conn.prepareStatement(stockSql)) {
                pstmt.setInt(1, quantity);
                pstmt.setInt(2, pledgeItemId);
                pstmt.executeUpdate();
            }

            conn.commit();
        } catch (SQLException e) {
            conn.rollback();
            throw e;
        } finally {
            conn.setAutoCommit(true);
        }
    }

    public int getPendingPledgeCount() throws SQLException {
        // Count pledge items that do NOT have a corresponding delivery entry
        String sql = "SELECT COUNT(*) FROM pledge_items pi LEFT JOIN deliveries d ON pi.id = d.pledge_item WHERE d.id IS NULL";
        try (Connection conn = AppDatabase.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        }
        return 0;
    }


    public int getOpenRequestCount() throws SQLException {
        // Count request items that do NOT have a corresponding disbursement entry
        String sql = "SELECT COUNT(*) FROM request_items ri LEFT JOIN disbursements d ON ri.id = d.request_item WHERE d.id IS NULL";
        try (Connection conn = AppDatabase.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        }
        return 0;
    }


    public int getTotalStockCount() throws SQLException {
        String sql = "SELECT SUM(quantity) FROM inventory_items";
        try (Connection conn = AppDatabase.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        }
        return 0;
    }


    public List<String> getUpcomingDeadlines() throws SQLException {
        List<String> deadlines = new ArrayList<>();
        // Fetch items needed in the next 7 days
        String sql = """
                    SELECT i.name, ri.quantity, i.units, ri.date_needed 
                    FROM request_items ri 
                    JOIN inventory_items i ON ri.item_id = i.id 
                    WHERE ri.date_needed BETWEEN CURRENT_DATE AND CURRENT_DATE + INTERVAL '7 days'
                    ORDER BY ri.date_needed ASC
                """;

        try (Connection conn = AppDatabase.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                String line = String.format("%s: %d %s needed by %s",
                        rs.getString("name"),
                        rs.getInt("quantity"),
                        rs.getString("units"),
                        rs.getDate("date_needed").toString()
                );
                deadlines.add(line);
            }
        }
        return deadlines;
    }


    public Map<String, Integer> getPledgeStatusStats() throws SQLException {
        Map<String, Integer> stats = new HashMap<>();
        // Group by Delivery Status
        String sql = """
                    SELECT 
                        CASE WHEN d.id IS NOT NULL THEN 'Delivered' ELSE 'Pending' END as status, 
                        COUNT(*) as count 
                    FROM pledge_items pi 
                    LEFT JOIN deliveries d ON pi.id = d.pledge_item 
                    GROUP BY status
                """;

        try (Connection conn = AppDatabase.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                stats.put(rs.getString("status"), rs.getInt("count"));
            }
        }
        return stats;
    }


    public Map<String, Integer> getRequestTrends() throws SQLException {
        // Use LinkedHashMap to keep month order
        Map<String, Integer> trends = new LinkedHashMap<>();
        // Get request count for last 6 months
        String sql = """
                    SELECT TO_CHAR(created_at, 'Mon') as month, COUNT(*) as count, TO_CHAR(created_at, 'MM') as m_num 
                    FROM requests 
                    WHERE created_at > CURRENT_DATE - INTERVAL '6 months' 
                    GROUP BY month, m_num 
                    ORDER BY m_num
                """;

        try (Connection conn = AppDatabase.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                trends.put(rs.getString("month"), rs.getInt("count"));
            }
        }
        return trends;
    }

    public List<RequestItem> getRequestItemsWithStatus(int requestId) throws SQLException {
        List<RequestItem> items = new ArrayList<>();
        // Query to calculate Requested Qty vs Already Given Qty
        String sql = """
                    SELECT ri.id, ri.item_id, i.name, i.units, ri.quantity as requested_qty, 
                           COALESCE(SUM(d.quantity), 0) as given_qty
                    FROM request_items ri
                    JOIN inventory_items i ON ri.item_id = i.id
                    LEFT JOIN disbursements d ON ri.id = d.request_item
                    WHERE ri.request_id = ?
                    GROUP BY ri.id, ri.item_id, i.name, i.units, ri.quantity
                """;

        try (Connection conn = AppDatabase.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, requestId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                String name = rs.getString("name");
                int requested = rs.getInt("requested_qty");
                String units = rs.getString("units");

                // We create a RequestItem and populate transient fields for UI
                RequestItem item = new RequestItem(name, requested, units);
                item.setId(rs.getInt("id")); // This is the request_items.id
                item.setItemId(rs.getInt("item_id"));

                // We need a place to store 'given_qty'. 
                // Since RequestItem is a POJO, let's assume we added a transient field or we handle logic in controller.
                // For this example, I will repurpose the quantity logic in the controller, 
                // but ideally, add 'private int fulfilledQuantity' to RequestItem model.
                item.setFulfilledQuantity(rs.getInt("given_qty"));

                items.add(item);
            }
        }
        return items;
    }

    public int getInventoryStock(int itemId) throws SQLException {
        String sql = "SELECT quantity FROM inventory_items WHERE id = ?";
        try (Connection conn = AppDatabase.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, itemId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) return rs.getInt(1);
        }
        return 0;
    }

    public void recordDisbursement(int requestItemId, int recipientId, long quantity, long dateEpoch) throws SQLException {
        Connection conn = AppDatabase.getConnection();
        conn.setAutoCommit(false); // Transaction start

        try {
            // 1. Insert Disbursement Record
            String insertSql = "INSERT INTO disbursements (request_item, recipient, quantity, date_given) VALUES (?, ?, ?, ?)";
            try (PreparedStatement pstmt = conn.prepareStatement(insertSql)) {
                pstmt.setInt(1, requestItemId);
                pstmt.setInt(2, recipientId);
                pstmt.setLong(3, quantity);
                pstmt.setLong(4, dateEpoch);
                pstmt.executeUpdate();
            }

            // 2. Reduce Inventory Stock
            // We need the inventory_item_id associated with this request_item
            String updateStockSql = """
                        UPDATE inventory_items 
                        SET quantity = quantity - ? 
                        WHERE id = (SELECT item_id FROM request_items WHERE id = ?)
                    """;
            try (PreparedStatement pstmt = conn.prepareStatement(updateStockSql)) {
                pstmt.setLong(1, quantity);
                pstmt.setInt(2, requestItemId);
                pstmt.executeUpdate();
            }

            conn.commit();
        } catch (SQLException e) {
            conn.rollback();
            throw e;
        } finally {
            conn.setAutoCommit(true);
        }
    }

    public Map<String, Integer> getUserRoleStats() throws SQLException {
        Map<String, Integer> stats = new HashMap<>();
        String sql = "SELECT role, COUNT(*) as count FROM users GROUP BY role";
        try (Connection conn = AppDatabase.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                // Capitalize first letter for display (e.g., "donor" -> "Donor")
                String role = rs.getString("role");
                String label = role.substring(0, 1).toUpperCase() + role.substring(1);
                stats.put(label, rs.getInt("count"));
            }
        }
        return stats;
    }

    public Map<String, Integer> getDonorTypeStats() throws SQLException {
        Map<String, Integer> stats = new HashMap<>();
        String sql = "SELECT entity_type, COUNT(*) as count FROM users WHERE role = 'donor' GROUP BY entity_type";
        try (Connection conn = AppDatabase.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                String type = rs.getString("entity_type");
                String label = type.substring(0, 1).toUpperCase() + type.substring(1);
                stats.put(label, rs.getInt("count"));
            }
        }
        return stats;
    }

    public List<User> getRecentUsers() throws SQLException {
        List<User> users = new ArrayList<>();
        // Fetch last 10 registered users
        String sql = "SELECT id, name, role, email, created_at FROM users ORDER BY created_at DESC LIMIT 10";
        try (Connection conn = AppDatabase.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                User u = new User();
                u.setId(rs.getInt("id"));
                u.setName(rs.getString("name"));
                u.setEmail(rs.getString("email"));
                // Map Enum strings safely
                u.setRole(UserType.valueOf(rs.getString("role").toUpperCase()));
                u.setCreatedAt(rs.getObject("created_at", java.time.OffsetDateTime.class));
                users.add(u);
            }
        }
        return users;
    }


    public List<DonationPledge> getPledgesByDateRange(int userId, LocalDate from, LocalDate to) throws SQLException {
        List<DonationPledge> list = new ArrayList<>();
        // Note: We cast OffsetDateTime to Date for comparison with LocalDate
        String sql = """
                    SELECT p.id, p.user_id, p.description, p.created_at,
                           STRING_AGG(i.name || ' (' || pi.quantity || ' ' || i.units || ')', ', ') as items_summary
                    FROM pledges p
                    JOIN pledge_items pi ON p.id = pi.pledge_id
                    JOIN inventory_items i ON pi.item_id = i.id
                    WHERE p.user_id = ? 
                      AND p.created_at >= ?::timestamp 
                      AND p.created_at <= ?::timestamp
                    GROUP BY p.id, p.user_id, p.description, p.created_at
                    ORDER BY p.created_at DESC
                """;

        try (Connection conn = AppDatabase.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            // Convert LocalDate to start/end of day timestamps
            pstmt.setObject(2, from.atStartOfDay());
            pstmt.setObject(3, to.atTime(23, 59, 59));

            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                DonationPledge p = new DonationPledge();
                p.setId(rs.getInt("id"));
                p.setUserId(rs.getInt("user_id"));
                p.setDescription(rs.getString("description"));
                p.setCreatedAt(rs.getObject("created_at", java.time.OffsetDateTime.class));
                p.setItemsSummary(rs.getString("items_summary"));
                list.add(p);
            }
        }
        return list;
    }


    public List<String> getDisbursementsByDateRange(int userId, LocalDate from, LocalDate to) throws SQLException {
        List<String> lines = new ArrayList<>();
        String sql = """
                    SELECT d.date_given, i.name, d.quantity, i.units
                    FROM disbursements d
                    JOIN request_items ri ON d.request_item = ri.id
                    JOIN inventory_items i ON ri.item_id = i.id
                    WHERE d.recipient = ?
                      AND to_timestamp(d.date_given) >= ?::timestamp
                      AND to_timestamp(d.date_given) <= ?::timestamp
                    ORDER BY d.date_given DESC
                """;

        try (Connection conn = AppDatabase.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            pstmt.setObject(2, from.atStartOfDay());
            pstmt.setObject(3, to.atTime(23, 59, 59));

            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                // Convert epoch seconds back to Readable Date
                java.time.Instant instant = java.time.Instant.ofEpochSecond(rs.getLong("date_given"));
                String dateStr = instant.atZone(java.time.ZoneOffset.UTC).toLocalDate().toString();

                String line = String.format("%s | Received: %d %s of %s",
                        dateStr,
                        rs.getInt("quantity"),
                        rs.getString("units"),
                        rs.getString("name"));
                lines.add(line);
            }
        }
        return lines;
    }


    public List<String> getInventoryReportData() throws SQLException {
        List<String> lines = new ArrayList<>();
        String sql = """
                    SELECT c.name as cat, i.name, i.quantity, i.units 
                    FROM inventory_items i 
                    LEFT JOIN item_categories c ON i.category = c.id
                    ORDER BY c.name, i.name
                """;

        try (Connection conn = AppDatabase.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            String currentCat = "";
            while (rs.next()) {
                String cat = rs.getString("cat");
                if (!cat.equals(currentCat)) {
                    lines.add("\n--- " + cat.toUpperCase() + " ---");
                    currentCat = cat;
                }
                lines.add(String.format("%-20s : %d %s", rs.getString("name"), rs.getInt("quantity"), rs.getString("units")));
            }
        }
        return lines;
    }
}

