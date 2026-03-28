package com.projects.microservices.core.analytics.service;

import static java.util.logging.Level.FINE;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import com.projects.api.core.analytics.*;
import com.projects.microservices.core.analytics.persistence.*;

@Service
public class AnalyticsServiceImpl implements AnalyticsService {

    private static final Logger LOG = LoggerFactory.getLogger(AnalyticsServiceImpl.class);

    private final SalesReportRepository salesReportRepository;
    private final CustomerReportRepository customerReportRepository;
    private final ProductReportRepository productReportRepository;
    private final SalesReportMapper salesReportMapper;
    private final CustomerReportMapper customerReportMapper;
    private final ProductReportMapper productReportMapper;

    @Autowired
    public AnalyticsServiceImpl(SalesReportRepository salesReportRepository,
                               CustomerReportRepository customerReportRepository,
                               ProductReportRepository productReportRepository,
                               SalesReportMapper salesReportMapper,
                               CustomerReportMapper customerReportMapper,
                               ProductReportMapper productReportMapper) {
        this.salesReportRepository = salesReportRepository;
        this.customerReportRepository = customerReportRepository;
        this.productReportRepository = productReportRepository;
        this.salesReportMapper = salesReportMapper;
        this.customerReportMapper = customerReportMapper;
        this.productReportMapper = productReportMapper;
    }

    @Override
    public Mono<SalesReport> getSalesReport(java.time.LocalDateTime startDate, java.time.LocalDateTime endDate) {
        // Mock sales report
        SalesReport report = new SalesReport();
        report.setReportId(java.util.UUID.randomUUID().toString());
        report.setStartDate(startDate);
        report.setEndDate(endDate);
        report.setTotalSales(10000.0);
        report.setOrderCount(250);
        report.setAverageOrderValue(40.0);
        
        return Mono.just(report);
    }

    @Override
    public Mono<CustomerReport> getCustomerReport(java.time.LocalDateTime startDate, java.time.LocalDateTime endDate) {
        // Mock customer report
        CustomerReport report = new CustomerReport();
        report.setReportId(java.util.UUID.randomUUID().toString());
        report.setStartDate(startDate);
        report.setEndDate(endDate);
        report.setTotalCustomers(500);
        report.setNewCustomers(150);
        report.setReturningCustomers(350);
        report.setAverageCustomerValue(40.0);
        
        return Mono.just(report);
    }

    @Override
    public Mono<ProductReport> getProductReport(java.time.LocalDateTime startDate, java.time.LocalDateTime endDate) {
        // Mock product report
        ProductReport report = new ProductReport();
        report.setReportId(java.util.UUID.randomUUID().toString());
        report.setStartDate(startDate);
        report.setEndDate(endDate);
        report.setTotalProductsSold(500);
        report.setTotalRevenue(10000.0);
        
        return Mono.just(report);
    }

    @Override
    public Mono<byte[]> exportSalesReport(java.time.LocalDateTime startDate, java.time.LocalDateTime endDate) {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Sales Report");
            
            // Create header row
            Row headerRow = sheet.createRow(0);
            String[] columns = {"Date", "Sales Amount", "Order Count"};
            
            for (int i = 0; i < columns.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columns[i]);
            }
            
            // Create data rows
            Row row1 = sheet.createRow(1);
            row1.createCell(0).setCellValue("2024-03-01");
            row1.createCell(1).setCellValue(5000.0);
            row1.createCell(2).setCellValue(125);
            
            Row row2 = sheet.createRow(2);
            row2.createCell(0).setCellValue("2024-03-02");
            row2.createCell(1).setCellValue(5000.0);
            row2.createCell(2).setCellValue(125);
            
            // Auto-size columns
            for (int i = 0; i < columns.length; i++) {
                sheet.autoSizeColumn(i);
            }
            
            // Write to byte array
            java.io.ByteArrayOutputStream outputStream = new java.io.ByteArrayOutputStream();
            workbook.write(outputStream);
            
            return Mono.just(outputStream.toByteArray());
        } catch (Exception e) {
            LOG.error("Failed to export sales report: {}", e.getMessage());
            return Mono.empty();
        }
    }

    @Override
    public Mono<byte[]> exportCustomerReport(java.time.LocalDateTime startDate, java.time.LocalDateTime endDate) {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Customer Report");
            
            // Create header row
            Row headerRow = sheet.createRow(0);
            String[] columns = {"Date", "Total Customers", "New Customers"};
            
            for (int i = 0; i < columns.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columns[i]);
            }
            
            // Create data rows
            Row row1 = sheet.createRow(1);
            row1.createCell(0).setCellValue("2024-03-01");
            row1.createCell(1).setCellValue(250);
            row1.createCell(2).setCellValue(75);
            
            Row row2 = sheet.createRow(2);
            row2.createCell(0).setCellValue("2024-03-02");
            row2.createCell(1).setCellValue(250);
            row2.createCell(2).setCellValue(75);
            
            // Auto-size columns
            for (int i = 0; i < columns.length; i++) {
                sheet.autoSizeColumn(i);
            }
            
            // Write to byte array
            java.io.ByteArrayOutputStream outputStream = new java.io.ByteArrayOutputStream();
            workbook.write(outputStream);
            
            return Mono.just(outputStream.toByteArray());
        } catch (Exception e) {
            LOG.error("Failed to export customer report: {}", e.getMessage());
            return Mono.empty();
        }
    }

    @Override
    public Mono<DashboardData> getDashboardData() {
        // Mock dashboard data
        DashboardData data = new DashboardData();
        data.setTotalSales(10000.0);
        data.setOrderCount(250);
        data.setCustomerCount(500);
        data.setProductCount(100);
        data.setCartAbandonmentRate(25.0);
        data.setConversionRate(3.5);
        
        return Mono.just(data);
    }
}
