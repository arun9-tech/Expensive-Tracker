# Login credentials
$body = @{
    username = "admin"
    password = "Admin@123"
} | ConvertTo-Json

try {
    # Send login request
    $response = Invoke-WebRequest -Uri "http://localhost:8080/api/auth/signin" `
        -Method Post `
        -Body $body `
        -ContentType "application/json" `
        -ErrorAction Stop
    
    # Parse the response
    $token = ($response.Content | ConvertFrom-Json).accessToken
    
    # Display the token
    Write-Host "Login successful!" -ForegroundColor Green
    Write-Host "Your JWT Token:" -ForegroundColor Cyan
    Write-Host $token -ForegroundColor Yellow
    
    # Store the token in an environment variable for later use
    $env:JWT_TOKEN = $token
    
    # Show how to use the token
    Write-Host "`nUse this token in subsequent requests like this:" -ForegroundColor Cyan
    Write-Host 'Invoke-WebRequest -Uri "http://localhost:8080/api/transactions" -Headers @{ "Authorization" = "Bearer ' + $token + '" }' -ForegroundColor Yellow
    
} catch {
    Write-Host "Login failed!" -ForegroundColor Red
    Write-Host "Status Code: $($_.Exception.Response.StatusCode.value__)" -ForegroundColor Red
    Write-Host "Response: $($_.ErrorDetails.Message)" -ForegroundColor Red
}
