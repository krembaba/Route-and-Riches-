param(
    [string]$JavaFxLibPath,
    [switch]$SafeRender,
    [switch]$PrismVerbose
)

$ErrorActionPreference = "Stop"

if ([string]::IsNullOrWhiteSpace($JavaFxLibPath)) {
    $JavaFxLibPath = $env:PATH_TO_FX
}

if ([string]::IsNullOrWhiteSpace($JavaFxLibPath)) {
    throw "JavaFX lib path is missing. Pass -JavaFxLibPath or set PATH_TO_FX."
}

if (-not (Test-Path -LiteralPath $JavaFxLibPath)) {
    throw "JavaFX lib path not found: $JavaFxLibPath"
}

$sources = Get-ChildItem -Path "routeandriches" -Recurse -Filter *.java | ForEach-Object { $_.FullName }
$fxModules = "javafx.controls,javafx.fxml"
$fxRoot = Split-Path -Parent $JavaFxLibPath
$fxBinPath = Join-Path $fxRoot "bin"
$nativePaths = @($JavaFxLibPath)
if (Test-Path -LiteralPath $fxBinPath) {
    $nativePaths += $fxBinPath
}
$nativeLibraryPath = [string]::Join([IO.Path]::PathSeparator, $nativePaths)

Write-Host "Compiling sources..."
& javac --module-path $JavaFxLibPath --add-modules $fxModules $sources

if ($LASTEXITCODE -ne 0) {
    throw "Compilation failed."
}

Write-Host "Launching game..."
$javaArgs = @()

if ($SafeRender) {
    # Safer launch path for machines where D3D init fails.
    $javaArgs += "-Dprism.order=sw"
    $javaArgs += "-Dprism.forceGPU=false"
}

if ($PrismVerbose) {
    $javaArgs += "-Dprism.verbose=true"
}

$javaArgs += "-Djava.library.path=$nativeLibraryPath"

$javaArgs += "--module-path"
$javaArgs += $JavaFxLibPath
$javaArgs += "--add-modules"
$javaArgs += $fxModules
$javaArgs += "routeandriches.Main"

& java @javaArgs
