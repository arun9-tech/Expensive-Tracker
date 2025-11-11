$body = @{
    username = 'testuser'
    password = 'Test123!'
} | ConvertTo-Json

try {
    $response = Invoke-WebRequest -Uri "http://localhost:8080/api/auth/signin" `
        -Method Post `
        -Body $body `
        -ContentType "application/json"
    
    $token = ($response.Content | ConvertFrom-Json).accessToken
    Write-Host "Your JWT Token:"
    Write-Host $token
    
    # Store the token in an environment variable for later use
    $env:JWT_TOKEN = $token
    
    # Show how to use the token
    Write-Host "`nUse this token in subsequent requests like this:"
    Write-Host "Invoke-WebRequest -Uri 'http://localhost:8080/api/transactions' -Headers @{ 'Authorization' = 'Bearer ' + `$env:JWT_TOKEN }"
} catch {
    Write-Host "Error: $_"
    Write-Host "Response: $($_.Exception.Response.StatusCode) - $($_.Exception.Response.StatusDescription)"
    if ($_.ErrorDetails.Message) {
        Write-Host "Details: $($_.ErrorDetails.Message)"
    }
}
