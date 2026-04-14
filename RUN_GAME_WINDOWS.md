# Run Game on Windows (JavaFX)

## 1) Prerequisites

- JDK 21 (64-bit)
- JavaFX SDK for Windows x64 (must contain native DLLs like `prism_sw.dll`)

## 2) Verify JavaFX SDK Package

Check that at least one of these exists:

- `...\javafx-sdk-21.0.10\bin\prism_sw.dll`
- `...\javafx-sdk-21.0.10\lib\prism_sw.dll`

PowerShell check:

```powershell
Get-ChildItem "C:\path\to\javafx-sdk-21.0.10\bin","C:\path\to\javafx-sdk-21.0.10\lib" -Filter "prism_sw.dll" -Recurse
```

If no result is returned, the downloaded package is incomplete/wrong for runtime use.

## 3) Run Command

From repo root:

```powershell
.\run-game.ps1 -JavaFxLibPath "C:\path\to\javafx-sdk-21.0.10\lib" -SafeRender -PrismVerbose
```

## 4) Optional Env Var

```powershell
$env:PATH_TO_FX="C:\path\to\javafx-sdk-21.0.10\lib"
.\run-game.ps1 -SafeRender -PrismVerbose
```

## 5) JavaFX Download (Terminal)

```powershell
$ver = "21.0.10"
$zip = "openjfx-$ver`_windows-x64_bin-sdk.zip"
$url = "https://download2.gluonhq.com/openjfx/$ver/$zip"
$dest = "C:\Users\dell\Downloads"

Invoke-WebRequest -Uri $url -OutFile (Join-Path $dest $zip)
Expand-Archive -Path (Join-Path $dest $zip) -DestinationPath $dest -Force
```

## 6) Current Known Error and Meaning

Error:

`no prism_sw in java.library.path`

Meaning:

- JavaFX native renderer DLL is missing from your installed SDK path.
- Reinstall/extract correct Windows x64 JavaFX SDK and retry.
