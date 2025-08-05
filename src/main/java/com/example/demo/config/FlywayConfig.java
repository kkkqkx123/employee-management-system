package com.example.demo.config;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.configuration.FluentConfiguration;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.flyway.FlywayConfigurationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

import javax.sql.DataSource;

/**
 * Flyway Configuration for database migrations.
 * 
 * Configures Flyway for versioned database schema management
 * with proper migration validation and rollback capabilities.
 */
@Configuration
public class FlywayConfig {

    /**
     * Flyway configuration customizer for additional settings
     */
    @Bean
    public FlywayConfigurationCustomizer flywayConfigurationCustomizer() {
        return new FlywayConfigurationCustomizer() {
            @Override
            public void customize(FluentConfiguration configuration) {
                // Enable validation of migrations
                configuration.validateOnMigrate(true);
                
                // Allow out of order migrations in development
                configuration.outOfOrder(false);
                
                // Set baseline version
                configuration.baselineVersion("0");
                configuration.baselineDescription("Initial baseline");
                
                // Configure migration locations
                configuration.locations("classpath:db/migration");
                
                // Set table name for migration history
                configuration.table("flyway_schema_history");
                
                // Configure encoding
                configuration.encoding("UTF-8");
                
                // Set placeholders for environment-specific values
                configuration.placeholderReplacement(true);
                configuration.placeholders(java.util.Map.of(
                    "database.name", "employee_management"
                ));
                
                // Configure callbacks for custom migration logic
                configuration.callbacks("com.example.demo.config.FlywayConfig$MigrationCallback");
            }
        };
    }

    /**
     * Custom Flyway instance with enhanced configuration
     */
    @Bean
    @DependsOn("dataSource")
    public Flyway flyway(@Qualifier("dataSource") DataSource dataSource) {
        return Flyway.configure()
                .dataSource(dataSource)
                .locations("classpath:db/migration")
                .baselineOnMigrate(true)
                .validateOnMigrate(true)
                .outOfOrder(false)
                .table("flyway_schema_history")
                .encoding("UTF-8")
                .placeholderReplacement(true)
                .load();
    }

    /**
     * Migration validation service
     */
    @Bean
    public MigrationValidator migrationValidator(Flyway flyway) {
        return new MigrationValidator(flyway);
    }

    /**
     * Custom migration callback for logging and validation
     */
    public static class MigrationCallback implements org.flywaydb.core.api.callback.Callback {
        
        @Override
        public boolean supports(org.flywaydb.core.api.callback.Event event, 
                              org.flywaydb.core.api.callback.Context context) {
            return event == org.flywaydb.core.api.callback.Event.BEFORE_MIGRATE ||
                   event == org.flywaydb.core.api.callback.Event.AFTER_MIGRATE ||
                   event == org.flywaydb.core.api.callback.Event.BEFORE_EACH_MIGRATE ||
                   event == org.flywaydb.core.api.callback.Event.AFTER_EACH_MIGRATE;
        }

        @Override
        public void handle(org.flywaydb.core.api.callback.Event event, 
                          org.flywaydb.core.api.callback.Context context) {
            switch (event) {
                case BEFORE_MIGRATE:
                    System.out.println("Starting database migration...");
                    break;
                case AFTER_MIGRATE:
                    System.out.println("Database migration completed successfully.");
                    break;
                case BEFORE_EACH_MIGRATE:
                    System.out.println("Executing migration: " + 
                        context.getMigrationInfo().getDescription());
                    break;
                case AFTER_EACH_MIGRATE:
                    System.out.println("Completed migration: " + 
                        context.getMigrationInfo().getDescription());
                    break;
                case AFTER_BASELINE:
                    break;
                case AFTER_BASELINE_ERROR:
                    break;
                case AFTER_BASELINE_OPERATION_FINISH:
                    break;
                case AFTER_CLEAN:
                    break;
                case AFTER_CLEAN_ERROR:
                    break;
                case AFTER_CLEAN_OPERATION_FINISH:
                    break;
                case AFTER_EACH_MIGRATE_ERROR:
                    break;
                case AFTER_EACH_MIGRATE_STATEMENT:
                    break;
                case AFTER_EACH_MIGRATE_STATEMENT_ERROR:
                    break;
                case AFTER_EACH_UNDO:
                    break;
                case AFTER_EACH_UNDO_ERROR:
                    break;
                case AFTER_EACH_UNDO_STATEMENT:
                    break;
                case AFTER_EACH_UNDO_STATEMENT_ERROR:
                    break;
                case AFTER_INFO:
                    break;
                case AFTER_INFO_ERROR:
                    break;
                case AFTER_INFO_OPERATION_FINISH:
                    break;
                case AFTER_MIGRATE_APPLIED:
                    break;
                case AFTER_MIGRATE_ERROR:
                    break;
                case AFTER_MIGRATE_OPERATION_FINISH:
                    break;
                case AFTER_REPAIR:
                    break;
                case AFTER_REPAIR_ERROR:
                    break;
                case AFTER_REPAIR_OPERATION_FINISH:
                    break;
                case AFTER_UNDO:
                    break;
                case AFTER_UNDO_ERROR:
                    break;
                case AFTER_UNDO_OPERATION_FINISH:
                    break;
                case AFTER_VALIDATE:
                    break;
                case AFTER_VALIDATE_ERROR:
                    break;
                case AFTER_VALIDATE_OPERATION_FINISH:
                    break;
                case AFTER_VERSIONED:
                    break;
                case BEFORE_BASELINE:
                    break;
                case BEFORE_CLEAN:
                    break;
                case BEFORE_CONNECT:
                    break;
                case BEFORE_CREATE_SCHEMA:
                    break;
                case BEFORE_EACH_MIGRATE_STATEMENT:
                    break;
                case BEFORE_EACH_UNDO:
                    break;
                case BEFORE_EACH_UNDO_STATEMENT:
                    break;
                case BEFORE_INFO:
                    break;
                case BEFORE_REPAIR:
                    break;
                case BEFORE_REPEATABLES:
                    break;
                case BEFORE_UNDO:
                    break;
                case BEFORE_VALIDATE:
                    break;
                case CREATE_SCHEMA:
                    break;
                default:
                    break;
            }
        }

        @Override
        public boolean canHandleInTransaction(org.flywaydb.core.api.callback.Event event, 
                                            org.flywaydb.core.api.callback.Context context) {
            return true;
        }

        @Override
        public String getCallbackName() {
            return "MigrationCallback";
        }
    }

    /**
     * Service for validating and managing migrations
     */
    public static class MigrationValidator {
        private final Flyway flyway;

        public MigrationValidator(Flyway flyway) {
            this.flyway = flyway;
        }

        /**
         * Validates all pending migrations
         */
        public boolean validateMigrations() {
            try {
                flyway.validate();
                return true;
            } catch (Exception e) {
                System.err.println("Migration validation failed: " + e.getMessage());
                return false;
            }
        }

        /**
         * Gets information about pending migrations
         */
        public org.flywaydb.core.api.MigrationInfo[] getPendingMigrations() {
            return flyway.info().pending();
        }

        /**
         * Gets information about applied migrations
         */
        public org.flywaydb.core.api.MigrationInfo[] getAppliedMigrations() {
            return flyway.info().applied();
        }

        /**
         * Repairs the migration history table
         */
        public void repairMigrations() {
            flyway.repair();
        }

        /**
         * Creates a baseline for existing databases
         */
        public void baseline() {
            flyway.baseline();
        }
    }
}