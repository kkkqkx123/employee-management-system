# test-external-connection.ps1 - Test external database connection

Write-Host "Testing external database connection..." -ForegroundColor Yellow

# Set environment variables
$env:DB_HOST="localhost"
$env:DB_PORT="5432"
$env:DB_NAME="employee_management"
$env:DB_USERNAME="employee_admin"
$env:DB_PASSWORD="admin123"

Write-Host "Connection parameters:" -ForegroundColor Cyan
Write-Host "  Host: $env:DB_HOST" -ForegroundColor Cyan
Write-Host "  Port: $env:DB_PORT" -ForegroundColor Cyan
Write-Host "  Database: $env:DB_NAME" -ForegroundColor Cyan
Write-Host "  Username: $env:DB_USERNAME" -ForegroundColor Cyan
Write-Host "  Password: $env:DB_PASSWORD" -ForegroundColor Cyan

# Test connection using psql from host machine (if available)
Write-Host ""
Write-Host "Testing connection from host machine..." -ForegroundColor Yellow

# Create a temporary .pgpass file for authentication
$pgpassContent = "localhost:5432:employee_management:employee_admin:admin123"
$pgpassFile = "$env:TEMP\.pgpass"
$pgpassContent | Out-File -FilePath $pgpassFile -Encoding ASCII

try {
    # Try to connect using psql (if available on host)
    $result = psql -h localhost -p 5432 -U employee_admin -d employee_management -c "SELECT 'External connection successful' as status;" 2>&1
    
    if ($LASTEXITCODE -eq 0) {
        Write-Host "External connection successful!" -ForegroundColor Green
        Write-Host $result
    } else {
        Write-Host "External connection failed (psql not available on host or connection issue)" -ForegroundColor Red
        Write-Host $result
    }
} catch {
    Write-Host "psql not available on host machine" -ForegroundColor Yellow
}

# Clean up
if (Test-Path $pgpassFile) {
    Remove-Item $pgpassFile
}

Write-Host ""
Write-Host "Testing Java JDBC connection simulation..." -ForegroundColor Yellow

# Create a simple Java test (if Java is available)
$javaTest = @"
import java.sql.*;

public class TestConnection {
    public static void main(String[] args) {
        String url = "jdbc:postgresql://localhost:5432/employee_management";
        String username = "employee_admin";
        String password = "admin123";
        
        try {
            Connection conn = DriverManager.getConnection(url, username, password);
            System.out.println("Java JDBC connection successful!");
            
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT 'Java connection test' as status");
            
            while (rs.next()) {
                System.out.println("Result: " + rs.getString("status"));
            }
            
            conn.close();
        } catch (Exception e) {
            System.out.println("Java JDBC connection failed: " + e.getMessage());
        }
    }
}
"@

$javaFile = "TestConnection.java"
$javaTest | Out-File -FilePath $javaFile -Encoding UTF8

Write-Host "Java test file created: $javaFile" -ForegroundColor Cyan
Write-Host "To test Java connection manually, run:" -ForegroundColor Cyan
Write-Host "  javac TestConnection.java" -ForegroundColor Cyan
Write-Host "  java -cp .;postgresql-driver.jar TestConnection" -ForegroundColor Cyan

Write-Host ""
Write-Host "=== Test Complete ===" -ForegroundColor Green