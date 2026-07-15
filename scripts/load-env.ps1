param(
    [string]$Path = (Join-Path $PSScriptRoot "..\.env")
)

if (-not (Test-Path -LiteralPath $Path)) {
    throw "Environment file not found: $Path. Copy .env.example to .env first."
}

Get-Content -LiteralPath $Path -Encoding UTF8 | ForEach-Object {
    $line = $_.Trim()
    if (-not $line -or $line.StartsWith('#')) {
        return
    }

    $separator = $line.IndexOf('=')
    if ($separator -le 0) {
        return
    }

    $name = $line.Substring(0, $separator).Trim()
    $value = $line.Substring($separator + 1).Trim()
    Set-Item -Path "Env:$name" -Value $value
}

Write-Host "Loaded development environment from $Path"
