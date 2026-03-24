param(
    [string]$ProjectRoot = (Resolve-Path "$PSScriptRoot\..").Path
)

$ErrorActionPreference = "Stop"

function Get-StringMap {
    param([string]$Path)

    if (-not (Test-Path $Path)) {
        throw "Missing file: $Path"
    }

    try {
        [xml]$xml = Get-Content -Raw -Encoding UTF8 $Path
    } catch {
        throw "XML parse failed: $Path`n$($_.Exception.Message)"
    }

    $map = @{}
    foreach ($node in $xml.resources.string) {
        $map[$node.name] = [string]$node.'#text'
    }
    return $map
}

function Get-Placeholders {
    param([string]$Text)
    if ([string]::IsNullOrEmpty($Text)) { return @() }
    return [regex]::Matches($Text, "%\d+\$[sdf]") | ForEach-Object { $_.Value }
}

function Ensure-KeySetSame {
    param(
        [hashtable]$BaseMap,
        [hashtable]$CompareMap,
        [string]$CompareName
    )

    $baseKeys = $BaseMap.Keys | Sort-Object
    $compareKeys = $CompareMap.Keys | Sort-Object
    $diff = Compare-Object -ReferenceObject $baseKeys -DifferenceObject $compareKeys
    if ($diff) {
        $missing = $diff | Where-Object { $_.SideIndicator -eq "<=" } | ForEach-Object { $_.InputObject }
        $extra = $diff | Where-Object { $_.SideIndicator -eq "=>" } | ForEach-Object { $_.InputObject }
        throw "Key set mismatch: $CompareName`nMissing: $($missing -join ', ')`nExtra: $($extra -join ', ')"
    }
}

function Ensure-PlaceholderSame {
    param(
        [hashtable]$BaseMap,
        [hashtable]$CompareMap,
        [string]$CompareName
    )

    $mismatch = @()
    foreach ($key in $BaseMap.Keys) {
        $basePlaceholders = Get-Placeholders -Text $BaseMap[$key]
        $comparePlaceholders = Get-Placeholders -Text $CompareMap[$key]
        if (($basePlaceholders -join "|") -ne ($comparePlaceholders -join "|")) {
            $mismatch += $key
        }
    }

    if ($mismatch.Count -gt 0) {
        throw "Placeholder mismatch: $CompareName`n$($mismatch -join ', ')"
    }
}

function Ensure-NoBrokenRawPattern {
    param(
        [string]$Path,
        [string]$Name
    )

    $raw = Get-Content -Raw -Encoding UTF8 $Path
    $replacementChar = [string][char]0xFFFD
    $badRawPatterns = @("?/string>", $replacementChar)
    $hits = @()
    foreach ($pattern in $badRawPatterns) {
        if ($raw.Contains($pattern)) {
            $hits += $pattern
        }
    }

    if ($hits.Count -gt 0) {
        throw "Broken raw pattern detected: $Name`n$($hits -join ', ')"
    }
}

$basePath = Join-Path $ProjectRoot "app\src\main\res\values\strings.xml"
$zhPath = Join-Path $ProjectRoot "app\src\main\res\values-zh-rCN\strings.xml"
$inPath = Join-Path $ProjectRoot "app\src\main\res\values-in\strings.xml"

$baseMap = Get-StringMap -Path $basePath
$zhMap = Get-StringMap -Path $zhPath
$inMap = Get-StringMap -Path $inPath

Ensure-KeySetSame -BaseMap $baseMap -CompareMap $zhMap -CompareName "values-zh-rCN"
Ensure-KeySetSame -BaseMap $baseMap -CompareMap $inMap -CompareName "values-in"
Ensure-PlaceholderSame -BaseMap $baseMap -CompareMap $zhMap -CompareName "values-zh-rCN"
Ensure-PlaceholderSame -BaseMap $baseMap -CompareMap $inMap -CompareName "values-in"
Ensure-NoBrokenRawPattern -Path $basePath -Name "values"
Ensure-NoBrokenRawPattern -Path $zhPath -Name "values-zh-rCN"
Ensure-NoBrokenRawPattern -Path $inPath -Name "values-in"

Write-Output "strings consistency check passed."
