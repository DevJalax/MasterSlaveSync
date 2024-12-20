import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class DatabaseSyncScheduler {

    private final JdbcTemplate slaveJdbcTemplate;
    private final JdbcTemplate masterJdbcTemplate;

    public DatabaseSyncScheduler(
            @Qualifier("slaveJdbcTemplate") JdbcTemplate slaveJdbcTemplate,
            @Qualifier("masterJdbcTemplate") JdbcTemplate masterJdbcTemplate) {
        this.slaveJdbcTemplate = slaveJdbcTemplate;
        this.masterJdbcTemplate = masterJdbcTemplate;
    }

    @Scheduled(cron = "0 0 23 * * *") // Runs every day at 11:00 PM
    public void syncDatabases() {
        System.out.println("Starting database synchronization...");

        try {
            // Fetch data from the Slave DB
            String fetchQuery = "SELECT * FROM some_table"; // Replace 'some_table' with your table
            List<Map<String, Object>> data = slaveJdbcTemplate.queryForList(fetchQuery);

            // Update Master DB
            String insertQuery = "INSERT INTO some_table (id, name, value) VALUES (?, ?, ?)"
                    + "ON DUPLICATE KEY UPDATE name = VALUES(name), value = VALUES(value)";

            for (Map<String, Object> row : data) {
                masterJdbcTemplate.update(
                        insertQuery,
                        row.get("id"),
                        row.get("name"),
                        row.get("value")
                );
            }

            System.out.println("Database synchronization completed successfully.");
        } catch (Exception e) {
            System.err.println("Error occurred during database synchronization: " + e.getMessage());
        }
    }
}
