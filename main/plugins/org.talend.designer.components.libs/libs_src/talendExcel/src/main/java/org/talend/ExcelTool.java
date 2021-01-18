package org.talend;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.ss.usermodel.FormulaEvaluator;

public class ExcelTool {

    private Workbook wb = null;

    private Workbook preWb = null;

    private String sheetName = null;

    private Sheet sheet = null;

    private Sheet preSheet = null;

    private Row curRow = null;

    private Row preRow = null;

    private Cell curCell = null;

    private Cell preCell = null;

    private boolean appendWorkbook = false;

    private boolean appendSheet = false;

    private int startX = 0;

    private int curY = 0;

    private int xOffset = 0;

    private boolean isAbsY = false;

    private int absX = 0;

    private int absY = 0;

    private boolean keepCellFormat = false;

    private Font font = null;

    private Map<String, CellStyle> cellStylesMapping;

    private boolean recalculateFormula = false;

    private int rowAccessWindowSize = SXSSFWorkbook.DEFAULT_WINDOW_SIZE;// used in auto flush

    private boolean isTrackAllColumns = false;

    private boolean isTruncateExceedingCharacters = false;

    private Map<CellStyle, CellStyle> existedOriginToClone;

    private static final int CELL_CHARACTERS_LIMIT = 32767;

    public ExcelTool() {
        cellStylesMapping = new HashMap<>();
    }

    public void setAppend(boolean appendWorkbook, boolean appendSheet) {
        this.appendWorkbook = appendWorkbook;
        this.appendSheet = appendSheet;
    }

    public void setXY(boolean isAbsY, int absX, int absY, boolean keepFormat) {
        this.isAbsY = isAbsY;
        this.absX = absX;
        this.absY = absY;
        this.keepCellFormat = keepFormat;
    }

    public void setSheet(String sheetName) {
        this.sheetName = sheetName;
    }

    public void setRecalculateFormula(boolean recalculateFormula) {
        this.recalculateFormula = recalculateFormula;
    }

    public void prepareStream() {
        wb = new SXSSFWorkbook(rowAccessWindowSize);
        sheet = wb.createSheet(sheetName);
        if (isAbsY) {
            startX = absX;
            curY = absY;
        }
    }

    public void prepareXlsxFile(String fileName) throws Exception {
        File xlsxFile = new File(fileName);
        if (xlsxFile.exists()) {
            if (isAbsY && keepCellFormat) {
                initPreXlsx(fileName);
            }
            if (appendWorkbook) {
                InputStream inp = new FileInputStream(fileName);
                wb = WorkbookFactory.create(inp);
                sheet = wb.getSheet(sheetName);
                if (sheet != null) {
                    if (appendSheet) {
                        if (sheet.getLastRowNum() != 0 || sheet.getRow(0) != null) {
                            curY = sheet.getLastRowNum() + 1;
                        }
                    } else {
                        wb.removeSheetAt(wb.getSheetIndex(sheetName));
                        sheet = wb.createSheet(sheetName);
                    }
                } else {
                    sheet = wb.createSheet(sheetName);
                }
            } else {
                xlsxFile.delete();
                wb = new SXSSFWorkbook(rowAccessWindowSize);
                sheet = wb.createSheet(sheetName);
            }
        } else {
            wb = new SXSSFWorkbook(rowAccessWindowSize);
            sheet = wb.createSheet(sheetName);
        }
        if (isAbsY) {
            startX = absX;
            curY = absY;
        }
    }

    /**
     *
     * @return start insert row index.
     */
    public int getStartRow() {
        return curY;
    }

    private void initPreXlsx(String fileName) throws Exception {
        InputStream preIns = new FileInputStream(fileName);
        preWb = WorkbookFactory.create(preIns);
        preSheet = preWb.getSheet(sheetName);
    }

    public void setFont(String fontName) {
        if (StringUtils.isNotEmpty(fontName)) {
            font = wb.createFont();
            font.setFontName(fontName);
        }
    }

    public void addRow() {
        if (isAbsY && keepCellFormat) {
            if (preSheet != null) {
                preRow = preSheet.getRow(curY);
            } else {
                preRow = null;
            }
        }
        curRow = sheet.getRow(curY);
        if (curRow == null) {
            curRow = sheet.createRow(curY);
        }
        if (keepCellFormat) {
            short rowHeight = curRow.getHeight();
            if (rowHeight != -1) {
                curRow.setHeight(rowHeight);
            }
        }
        curY = curY + 1;
        xOffset = 0;
    }

    private void addCell() {
        if (isAbsY && keepCellFormat) {
            if (preRow != null) {
                preCell = preRow.getCell(startX + xOffset);
            } else {
                preCell = null;
            }
        }
        curCell = curRow.createCell(startX + xOffset);
        xOffset++;
    }

    private CellStyle getNormalCellStyle() {
        CellStyle preCellStyle = getPreCellStyle();
        if (preCellStyle == null) {
            if (cellStylesMapping.get("normal") != null) {
                return cellStylesMapping.get("normal");
            } else {
                CellStyle style = wb.createCellStyle();
                if (font != null) {
                    style.setFont(font);
                }
                cellStylesMapping.put("normal", style);
                return style;
            }
        } else {
            return preCellStyle;
        }

    }

    private CellStyle getDateCellStyle(String pattern) {
        CellStyle preCellStyle = getPreCellStyle();
        if (preCellStyle == null) {
            if (cellStylesMapping.get(pattern) != null) {
                return cellStylesMapping.get(pattern);
            } else {
                CellStyle style = wb.createCellStyle();
                if (font != null) {
                    style.setFont(font);
                }
                if (StringUtils.isNotEmpty(pattern)) {
                    style.setDataFormat(wb.getCreationHelper().createDataFormat().getFormat(pattern));
                }
                cellStylesMapping.put(pattern, style);
                return style;
            }
        } else {
            return preCellStyle;
        }
    }

    private CellStyle getPreCellStyle() {
        if (preSheet != null && isAbsY && keepCellFormat) {
            if(existedOriginToClone == null) {
                existedOriginToClone = new HashMap<>();
            }
            CellStyle preCellStyle;
            if (preCell == null) {
                preCellStyle = preSheet.getColumnStyle(curCell.getColumnIndex());
            } else {
                preCellStyle = preCell.getCellStyle();
            }

            CellStyle targetCellStyle = existedOriginToClone.get(preCellStyle);
            if(targetCellStyle == null) {
                targetCellStyle = wb.createCellStyle();
                targetCellStyle.cloneStyleFrom(preCellStyle);
                existedOriginToClone.put(preCellStyle, targetCellStyle);
            }
            return targetCellStyle;

        } else {
            return null;
        }
    }

    public void addCellValue(boolean booleanValue) {
        addCell();
        curCell.setCellValue(booleanValue);
        curCell.setCellStyle(getNormalCellStyle());
    }

    public void addCellValue(Date dateValue, String pattern) {
        addCell();
        curCell.setCellValue(dateValue);
        curCell.setCellStyle(getDateCellStyle(pattern));
    }

    public void addCellValue(double doubleValue) {
        addCell();
        curCell.setCellValue(doubleValue);
        curCell.setCellStyle(getNormalCellStyle());
    }

    public void addCellValue(String stringValue) {
        addCell();
        String value = isTruncateExceedingCharacters && stringValue != null && stringValue.length() > CELL_CHARACTERS_LIMIT
                ? stringValue.substring(0, CELL_CHARACTERS_LIMIT)
                        : stringValue;
        curCell.setCellValue(value);
        curCell.setCellStyle(getNormalCellStyle());
    }

    public void addCellNullValue() {
        addCell();
        curCell.setCellStyle(getNormalCellStyle());
    }

    public void setColAutoSize(int colNum) {
        if (!isTrackAllColumns) {
            trackAllColumnsForAutoSizing();
        }
        sheet.autoSizeColumn(startX + colNum, true);
    }

    public void trackAllColumnsForAutoSizing() {
        if (sheet instanceof SXSSFSheet) {
            ((SXSSFSheet) sheet).trackAllColumnsForAutoSizing();
        }
        isTrackAllColumns = true;
    }

    public void setRowAccessWindowSize(int rowAccessWindowSize) {
        this.rowAccessWindowSize = rowAccessWindowSize;
    }

    public void writeExcel(OutputStream outputStream) throws Exception {
        wb.write(outputStream);
        if(existedOriginToClone!=null) {
            existedOriginToClone = null;
        }
    }

    public void writeExcel(String fileName, boolean createDir) throws Exception {
        if (createDir) {
            File file = new File(fileName);
            File pFile = file.getParentFile();
            if (pFile != null && !pFile.exists()) {
                pFile.mkdirs();
            }
        }
        FileOutputStream fileOutput = new FileOutputStream(fileName);
        if (appendWorkbook && appendSheet && recalculateFormula) {
            evaluateFormulaCell();
        }
        wb.write(fileOutput);
        if(existedOriginToClone!=null) {
        	existedOriginToClone = null;
        }
        fileOutput.close();
    }

    private void evaluateFormulaCell() {
        FormulaEvaluator evaluator = wb.getCreationHelper().createFormulaEvaluator();
        for (int sheetNum = 0; sheetNum < wb.getNumberOfSheets(); sheetNum++) {
            sheet = wb.getSheetAt(sheetNum);
            for (Row r : sheet) {
                for (Cell c : r) {
                    if (c.getCellTypeEnum() == CellType.FORMULA) {
                        evaluator.evaluateFormulaCellEnum(c);
                    }
                }
            }
        }
    }

    public void flushRowInMemory() throws Exception {
        if (wb instanceof SXSSFWorkbook) {
            ((SXSSFSheet) sheet).flushRows();
        }
    }

    public void setTruncateExceedingCharacters(boolean isTruncateExceedingCharacters) {
        this.isTruncateExceedingCharacters = isTruncateExceedingCharacters;
    }
}
