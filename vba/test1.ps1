# 定义要搜索的字符串
$searchString = "学校"

# 定义要遍历的文件夹路径
$folderPath = "C:\Users\81804\Desktop\転職資料"

# 定义结果输出文件
$resultFile = "C:\Users\81804\Desktop\vba\result.txt"

# 清空结果文件内容
Clear-Content -Path $resultFile

# 遍历文件夹和子文件夹中的Excel文件
Get-ChildItem -LiteralPath $folderPath -Recurse -Filter *.xls | ForEach-Object {
    $excelFile = $_.FullName
    
    # 创建 Excel 应用程序对象
    $excel = New-Object -ComObject Excel.Application
    
    try {
        # 打开 Excel 文件
        $workbook = $excel.Workbooks.Open($excelFile)
        
        # 遍历每个工作表
        foreach ($sheet in $workbook.Sheets) {
            $worksheet = $workbook.Sheets.Item($sheet.Name)
            
            # 遍历每个单元格
            foreach ($row in $worksheet.UsedRange.Rows) {
                foreach ($cell in $row.Columns) {
                    if ($cell.Text -like "*$searchString*") {
                        # 输出整行内容
                        $rowValues = @()
                        foreach ($rCell in $row.Columns) {
                            $rowValues += $rCell.Text
                        }
                        Add-Content -Path $resultFile -Value ($rowValues -join "`t")
                        
                        # 输出文件名、工作表名和行号
                        $locationInfo = "$excelFile - $sheet.Name - Row $($cell.Row)"
                        Add-Content -Path $resultFile -Value $locationInfo
                    }
                }
            }
        }
        
        # 关闭工作簿
        $workbook.Close()
    } catch {
        Write-Host "Error processing"
    } finally {
        # 退出 Excel 应用程序
        $excel.Quit()
        
        # 释放 COM 对象
        [System.Runtime.InteropServices.Marshal]::ReleaseComObject($excel) | Out-Null
        [System.GC]::Collect()
        [System.GC]::WaitForPendingFinalizers()
    }
}

Write-Host "Search completed. Results are saved in $resultFile"