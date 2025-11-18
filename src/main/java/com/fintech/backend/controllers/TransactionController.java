package com.fintech.backend.controllers;

import com.fintech.backend.dto.TransactionDetailsDto;
import com.fintech.backend.service.ReceiptAnalyzerService;
import com.fintech.backend.service.TransactionsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;

@Slf4j
@RestController
@RequestMapping("/api/transactions")
public class TransactionController extends FormattedResponseMapping {
    private final TransactionsService transactionsService;
    private final ReceiptAnalyzerService receiptAnalyzerService;

    public TransactionController(TransactionsService transactionsService, ReceiptAnalyzerService receiptAnalyzerService) {
        this.transactionsService = transactionsService;
        this.receiptAnalyzerService = receiptAnalyzerService;
    }

    @GetMapping("/user/{id}")
    private ResponseEntity<HashMap<String, Object>> getAllTransactions(@PathVariable Long id) {
        return getResponseFormat(HttpStatus.OK, "Transactions Found", transactionsService.getAllTransactionsByUserId(id));
    }

    @PostMapping("/{userId}")
    private ResponseEntity<HashMap<String, Object>> createNewTransaction(@PathVariable Long userId, @RequestBody TransactionDetailsDto transactionDetailsDto) {
        return getResponseFormat(HttpStatus.CREATED, "Transaction Created", transactionsService.createNewTransaction(userId, transactionDetailsDto));
    }

    @GetMapping("/income-per-month/{userId}")
    private ResponseEntity<HashMap<String, Object>> getIncomePerMonth(@PathVariable Long userId) {
        return getResponseFormat(HttpStatus.OK, "Income Found", transactionsService.getIncomePerMonth(userId));
    }

    @GetMapping("/spending-per-month/{userId}")
    private ResponseEntity<HashMap<String, Object>> getSpendingPerMonth(@PathVariable Long userId) {
        return getResponseFormat(HttpStatus.OK, "Spending Found", transactionsService.getSpendingPerMonth(userId));
    }

    @GetMapping("/balance/{userId}")
    private ResponseEntity<HashMap<String, Object>> getBalance(@PathVariable Long userId) {
        return getResponseFormat(HttpStatus.OK, "Balance Found", transactionsService.getBalance(userId));
    }

    @GetMapping("/{userId}/last-week-summary")
    public ResponseEntity<HashMap<String, Object>> getLastWeekSummary(@PathVariable Long userId) {
        return getResponseFormat(HttpStatus.OK, "Last Week Summary Found", transactionsService.lastWeekDetails(userId));
    }


    @PostMapping(value = "/{userId}/upload-analyze", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<HashMap<String,Object>> uploadAndAnalyzeReceipt(@RequestPart("file") MultipartFile file, @PathVariable Long userId) {
       return getResponseFormat(HttpStatus.OK, "Image processed successfully", receiptAnalyzerService.getResponse(file, userId));
    }

}

