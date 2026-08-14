# Builds the DeepSeek Harness Web tool-window plugin using a JetBrains IDE's
# bundled JDK (no Gradle/Kotlin required). Produces
#   releases/dsh-web-jetbrains-toolwindow.jar  (plugin, install from disk)
#   releases/dsh-web-jetbrains-toolwindow.zip  (same bytes, for Marketplace upload)
# Install via Settings | Plugins | Install Plugin from Disk.
param(
  [string]$IdeRoot = ""
)
$ErrorActionPreference = "Stop"
$here = Split-Path -Parent $MyInvocation.MyCommand.Path

if (-not $IdeRoot) {
  $cands = @(
    "D:\Program Files\JetBrains\Rider*",
    "$env:LOCALAPPDATA\Programs\Rider*",
    "$env:LOCALAPPDATA\JetBrains\Toolbox\apps\Rider*",
    "C:\Program Files\JetBrains\Rider*"
  )
  foreach ($c in $cands) {
    $d = Get-Item $c -ErrorAction SilentlyContinue | Sort-Object LastWriteTime -Descending | Select-Object -First 1
    if ($d) { $IdeRoot = $d.FullName; break }
  }
}
if (-not $IdeRoot) { throw "Rider install not found; pass -IdeRoot" }
$javac = Join-Path $IdeRoot "jbr\bin\javac.exe"
if (-not (Test-Path $javac)) { throw "javac not found under $IdeRoot\jbr" }

$lib      = Join-Path $IdeRoot "lib"
$jcef     = Join-Path $IdeRoot "plugins\jcef-plugin\lib"
$out      = Join-Path $here "out"
$classes  = Join-Path $here "classes"
$staging  = Join-Path $here "staging"
$release  = Join-Path $here "releases"

# Classpath: every IDE platform jar + the JCEF Java classes (intellij.*.jcef jars).
$cp = @()
$cp += Get-ChildItem $lib -Filter "*.jar" | ForEach-Object { $_.FullName }
if (Test-Path $jcef) {
  $cp += Get-ChildItem $jcef -Filter "*.jar" | ForEach-Object { $_.FullName }
  if (Test-Path (Join-Path $jcef "modules")) {
    $cp += Get-ChildItem (Join-Path $jcef "modules") -Filter "*.jar" | ForEach-Object { $_.FullName }
  }
}
$classpath = ($cp | Select-Object -Unique) -join ";"

# Argument file avoids Windows command-line length limits (hundreds of jars).
# javac @file format: one argument per line; quote tokens containing spaces.
# No BOM (UTF8 without BOM) and forward slashes, or javac misparses the first
# token / backslash escapes.
$argFile = Join-Path $here "compile.args"
$classpath = (($cp | Select-Object -Unique) | ForEach-Object { $_ -replace '\\', '/' }) -join ';'
$lines = @()
$lines += "-classpath"
$lines += ('"' + $classpath + '"')
$lines += "-encoding"
$lines += "UTF-8"
$lines += "--release"
$lines += "17"
$lines += "-d"
$lines += ('"' + ($classes -replace '\\', '/') + '"')
Get-ChildItem (Join-Path $here "src") -Recurse -Filter "*.java" | ForEach-Object {
  $lines += ('"' + ($_.FullName -replace '\\', '/') + '"')
}
$utf8NoBom = New-Object System.Text.UTF8Encoding($false)
[System.IO.File]::WriteAllLines($argFile, $lines, $utf8NoBom)

if (Test-Path $classes) { Remove-Item $classes -Recurse -Force }
New-Item -ItemType Directory -Path $classes -Force | Out-Null
& $javac "@$argFile"
if ($LASTEXITCODE -ne 0) { throw "javac failed (exit $LASTEXITCODE)" }

# Stage: plugin.xml at jar root + compiled classes.
if (Test-Path $staging) { Remove-Item $staging -Recurse -Force }
New-Item -ItemType Directory -Path $staging -Force | Out-Null
Copy-Item (Join-Path $here "META-INF") $staging -Recurse -Force
if (Test-Path (Join-Path $here "icons")) { Copy-Item (Join-Path $here "icons") $staging -Recurse -Force }
Copy-Item (Join-Path $classes "*") $staging -Recurse -Force
# Package with the IDE's own JDK (java.util.zip): guarantees a byte-standard
# jar/zip that JetBrains' JarFile-based loader accepts. Produces both .jar and .zip.
$zipBuilder = Join-Path $here "ZipBuilder.java"
$java = Join-Path (Split-Path -Parent $javac) "java.exe"
& $javac -encoding UTF-8 -d $here $zipBuilder
if ($LASTEXITCODE -ne 0) { throw "ZipBuilder compile failed" }
New-Item -ItemType Directory -Path $release -Force | Out-Null
$jar = Join-Path $release "dsh-web-jetbrains-toolwindow.jar"
$zip = Join-Path $release "dsh-web-jetbrains-toolwindow.zip"
if (Test-Path $zip) { Remove-Item $zip -Force }
if (Test-Path $jar) { Remove-Item $jar -Force }
& $java -cp $here ZipBuilder $staging $jar
if ($LASTEXITCODE -ne 0) { throw "jar packaging failed" }
Copy-Item $jar $zip -Force

Write-Host ""
Write-Host "Built: $jar" -ForegroundColor Green
Write-Host "       $zip"
Write-Host "IDE  : $IdeRoot"
Write-Host "Install: Settings | Plugins | (gear) Install Plugin from Disk -> $jar"
