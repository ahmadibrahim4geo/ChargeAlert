# Generate legacy launcher PNGs and README artwork from the same vector geometry.
# Run on Windows with PowerShell. Adaptive icons use the equivalent Android vector resources.
Add-Type -AssemblyName System.Drawing

function New-RoundedPath([single]$x, [single]$y, [single]$w, [single]$h, [single]$r) {
    $p = [System.Drawing.Drawing2D.GraphicsPath]::new()
    $d = 2 * $r
    $p.AddArc($x, $y, $d, $d, 180, 90)
    $p.AddArc($x + $w - $d, $y, $d, $d, 270, 90)
    $p.AddArc($x + $w - $d, $y + $h - $d, $d, $d, 0, 90)
    $p.AddArc($x, $y + $h - $d, $d, $d, 90, 90)
    $p.CloseFigure()
    return ,$p
}

function Export-ChargeIcon([int]$size, [string]$path, [bool]$round = $false) {
    $large = $size * 4
    $bitmap = [System.Drawing.Bitmap]::new($large, $large)
    $g = [System.Drawing.Graphics]::FromImage($bitmap)
    $g.SmoothingMode = [System.Drawing.Drawing2D.SmoothingMode]::AntiAlias
    $navy = [System.Drawing.SolidBrush]::new([System.Drawing.ColorTranslator]::FromHtml('#091724'))
    $cyan = [System.Drawing.SolidBrush]::new([System.Drawing.ColorTranslator]::FromHtml('#5BE7F3'))
    $white = [System.Drawing.SolidBrush]::new([System.Drawing.Color]::White)
    if ($round) { $g.FillEllipse($navy, 0, 0, $large, $large) }
    else {
        $bg = New-RoundedPath 0 0 $large $large ($large * 16 / 72)
        $g.FillPath($navy, $bg)
        $bg.Dispose()
    }
    $s = $large / 72.0
    $matrix = [System.Drawing.Drawing2D.Matrix]::new([single]$s, 0, 0, [single]$s, [single](-18 * $s), [single](-18 * $s))
    $g.Transform = $matrix
    $outline = New-RoundedPath 34 30 40 50 8
    $inside = New-RoundedPath 38 34 32 42 4
    $outline.FillMode = [System.Drawing.Drawing2D.FillMode]::Alternate
    $outline.AddPath($inside, $false)
    $g.FillPath($cyan, $outline)
    $cap = New-RoundedPath 47 24 14 4 2
    $g.FillPath($cyan, $cap)
    $points = [System.Drawing.PointF[]]@(
        [System.Drawing.PointF]::new(57,36), [System.Drawing.PointF]::new(44,56),
        [System.Drawing.PointF]::new(53,56), [System.Drawing.PointF]::new(50,71),
        [System.Drawing.PointF]::new(65,49), [System.Drawing.PointF]::new(56,49))
    $g.FillPolygon($white, $points)
    $g.Dispose()
    $output = [System.Drawing.Bitmap]::new($size, $size)
    $og = [System.Drawing.Graphics]::FromImage($output)
    $og.InterpolationMode = [System.Drawing.Drawing2D.InterpolationMode]::HighQualityBicubic
    $og.DrawImage($bitmap, 0, 0, $size, $size)
    $output.Save($path, [System.Drawing.Imaging.ImageFormat]::Png)
    $og.Dispose(); $output.Dispose(); $bitmap.Dispose()
    $outline.Dispose(); $inside.Dispose(); $cap.Dispose(); $matrix.Dispose()
    $navy.Dispose(); $cyan.Dispose(); $white.Dispose()
}

$projectRoot = Split-Path -Parent $PSScriptRoot
Export-ChargeIcon 512 (Join-Path $projectRoot 'assets/branding/charge-alert.png')
foreach ($density in @(@('mdpi',48), @('hdpi',72), @('xhdpi',96), @('xxhdpi',144), @('xxxhdpi',192))) {
    $folder = Join-Path $projectRoot ('app/src/main/res/mipmap-' + $density[0])
    Export-ChargeIcon $density[1] (Join-Path $folder 'ic_launcher.png')
    Export-ChargeIcon $density[1] (Join-Path $folder 'ic_launcher_round.png') $true
}
