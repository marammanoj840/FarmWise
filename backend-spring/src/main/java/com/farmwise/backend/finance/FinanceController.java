package com.farmwise.backend.finance;

import com.farmwise.backend.common.MessageResponse;
import com.farmwise.backend.common.PagedResponse;
import com.farmwise.backend.finance.FinanceDtos.ExpenseRequest;
import com.farmwise.backend.finance.FinanceDtos.ExpenseResponse;
import com.farmwise.backend.finance.FinanceDtos.FinanceSummaryResponse;
import com.farmwise.backend.finance.FinanceDtos.IncomeRequest;
import com.farmwise.backend.finance.FinanceDtos.IncomeResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/finance")
@Tag(name = "Finance & Reports", description = "Expenses, income, profit & loss summaries, and CSV/PDF report exports")
public class FinanceController {

    private final FinanceService financeService;

    public FinanceController(FinanceService financeService) {
        this.financeService = financeService;
    }

    @GetMapping("/expenses")
    @Operation(summary = "List farm expenses")
    public ResponseEntity<PagedResponse<ExpenseResponse>> listExpenses(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(financeService.listExpenses(page, size));
    }

    @PostMapping("/expenses")
    @Operation(summary = "Record a farm expense")
    public ResponseEntity<ExpenseResponse> createExpense(@Valid @RequestBody ExpenseRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(financeService.createExpense(request));
    }

    @DeleteMapping("/expenses/{id}")
    @Operation(summary = "Delete an expense record")
    public ResponseEntity<MessageResponse> deleteExpense(@PathVariable UUID id) {
        return ResponseEntity.ok(financeService.deleteExpense(id));
    }

    @GetMapping("/income")
    @Operation(summary = "List farm income records")
    public ResponseEntity<PagedResponse<IncomeResponse>> listIncome(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(financeService.listIncome(page, size));
    }

    @PostMapping("/income")
    @Operation(summary = "Record farm income")
    public ResponseEntity<IncomeResponse> createIncome(@Valid @RequestBody IncomeRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(financeService.createIncome(request));
    }

    @DeleteMapping("/income/{id}")
    @Operation(summary = "Delete an income record")
    public ResponseEntity<MessageResponse> deleteIncome(@PathVariable UUID id) {
        return ResponseEntity.ok(financeService.deleteIncome(id));
    }

    @GetMapping("/summary")
    @Operation(summary = "Get farm and crop financial summary and P&L overview")
    public ResponseEntity<FinanceSummaryResponse> summary() {
        return ResponseEntity.ok(financeService.getSummary());
    }

    @GetMapping("/reports/export")
    @Operation(summary = "Download CSV or PDF reports (EXPENSES, INCOME, FINANCE_PNL, FARMS, CROPS, INVENTORY, HARVEST, ACTIVITIES)")
    public ResponseEntity<byte[]> exportReport(
            @RequestParam(defaultValue = "FINANCE_PNL") String type,
            @RequestParam(defaultValue = "CSV") String format) {
        byte[] content = financeService.exportReport(type, format);
        boolean isPdf = "PDF".equalsIgnoreCase(format);
        String ext = isPdf ? "pdf" : "csv";
        String filename = "farmwise-" + type.toLowerCase() + "-report." + ext;
        MediaType mediaType = isPdf ? MediaType.APPLICATION_PDF : MediaType.parseMediaType("text/csv");

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(mediaType)
                .body(content);
    }
}
