param([switch]$Offline)

$ErrorActionPreference = 'Stop'
$projectRoot = Split-Path $PSScriptRoot -Parent
$signingVariables = @('KEYSTORE_PATH', 'STORE_PASSWORD', 'KEY_ALIAS', 'KEY_PASSWORD')
$previousValues = @{}
foreach ($name in $signingVariables) {
    $previousValues[$name] = [Environment]::GetEnvironmentVariable($name, 'Process')
}

Push-Location $projectRoot
try {
    $credentialsFile = Join-Path $projectRoot '.env.signing'
    if (Test-Path -LiteralPath $credentialsFile) {
        foreach ($line in [IO.File]::ReadAllLines($credentialsFile)) {
            if ($line -match '^\s*(#|$)') { continue }
            $parts = $line.Split('=', 2)
            if ($parts.Length -eq 2 -and $parts[0] -in $signingVariables) {
                [Environment]::SetEnvironmentVariable($parts[0], $parts[1], 'Process')
            }
        }
    }

    foreach ($name in @('KEYSTORE_PATH', 'STORE_PASSWORD', 'KEY_PASSWORD')) {
        if ([string]::IsNullOrWhiteSpace([Environment]::GetEnvironmentVariable($name, 'Process'))) {
            throw "Missing signing setting: $name. Configure it locally; never commit credentials."
        }
    }
    if (-not (Test-Path -LiteralPath $env:KEYSTORE_PATH -PathType Leaf)) {
        throw 'The configured release keystore does not exist.'
    }

    $gradleArguments = @('testDebugUnitTest', 'lintRelease', 'assembleRelease', '--no-configuration-cache')
    if ($Offline) { $gradleArguments += '--offline' }
    & .\gradlew.bat @gradleArguments
    if ($LASTEXITCODE -ne 0) { throw 'Release checks or build failed.' }

    $releaseApk = Join-Path $projectRoot 'app/build/outputs/apk/release/app-release.apk'
    if (-not (Test-Path -LiteralPath $releaseApk -PathType Leaf)) {
        throw 'The signed release APK was not produced.'
    }

    $sdkRoot = $env:ANDROID_HOME
    if ([string]::IsNullOrWhiteSpace($sdkRoot)) { $sdkRoot = $env:ANDROID_SDK_ROOT }
    $localProperties = Join-Path $projectRoot 'local.properties'
    if ([string]::IsNullOrWhiteSpace($sdkRoot) -and (Test-Path -LiteralPath $localProperties)) {
        $sdkLine = [IO.File]::ReadAllLines($localProperties) | Where-Object { $_ -match '^sdk\.dir=' } | Select-Object -First 1
        if ($sdkLine) { $sdkRoot = $sdkLine.Substring(8).Replace('\:', ':').Replace('\\', '\') }
    }
    if ([string]::IsNullOrWhiteSpace($sdkRoot)) { throw 'Set ANDROID_HOME to verify the APK signature.' }
    $apkSigner = Get-ChildItem -LiteralPath (Join-Path $sdkRoot 'build-tools') -Directory |
        Sort-Object Name -Descending |
        ForEach-Object { Join-Path $_.FullName 'apksigner.bat' } |
        Where-Object { Test-Path -LiteralPath $_ } |
        Select-Object -First 1
    if (-not $apkSigner) { throw 'apksigner.bat was not found in Android SDK build-tools.' }
    & $apkSigner verify --verbose --print-certs $releaseApk
    if ($LASTEXITCODE -ne 0) { throw 'Release APK signature verification failed.' }

    $uploadDirectory = Join-Path $projectRoot 'build/release-upload'
    New-Item -ItemType Directory -Path $uploadDirectory -Force | Out-Null
    Copy-Item -LiteralPath $releaseApk -Destination (Join-Path $projectRoot 'ChargeAlert.apk') -Force
    Copy-Item -LiteralPath $releaseApk -Destination (Join-Path $uploadDirectory 'ChargeAlert.apk') -Force
    $hash = (Get-FileHash -LiteralPath $releaseApk -Algorithm SHA256).Hash.ToLowerInvariant()
    $utf8 = [Text.UTF8Encoding]::new($false)
    [IO.File]::WriteAllText((Join-Path $uploadDirectory 'SHA256SUMS.txt'), "$hash  ChargeAlert.apk`n", $utf8)

    $notes = [IO.File]::ReadAllText((Join-Path $projectRoot 'docs/RELEASE_NOTES.md'))
    $body = [regex]::Match($notes, '(?s)<!-- RELEASE_BODY_START -->\s*(.*?)\s*<!-- RELEASE_BODY_END -->').Groups[1].Value
    if ([string]::IsNullOrWhiteSpace($body)) { throw 'Release description markers were not found.' }
    [IO.File]::WriteAllText((Join-Path $uploadDirectory 'RELEASE_DESCRIPTION.md'), "$body`n", $utf8)
    $title = [regex]::Match($notes, '(?m)^\| Title \| (.*?) \|\s*$').Groups[1].Value
    if ([string]::IsNullOrWhiteSpace($title)) { throw 'Release title was not found.' }
    [IO.File]::WriteAllText((Join-Path $uploadDirectory 'RELEASE_TITLE.txt'), "$title`n", $utf8)
    Write-Output "Signed release APK and upload files: $uploadDirectory"
}
finally {
    foreach ($name in $signingVariables) {
        [Environment]::SetEnvironmentVariable($name, $previousValues[$name], 'Process')
    }
    Pop-Location
}
