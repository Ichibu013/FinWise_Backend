package com.fintech.backend.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fintech.backend.dto.CategoryGoalsDto;
import com.fintech.backend.service.GoalsService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;

@RestController
@RequestMapping("/api/goals")
public class GoalsController extends FormattedResponseMapping {
    private final GoalsService goalsService;

    public GoalsController(GoalsService goalsService) {
        this.goalsService = goalsService;
    }

    @GetMapping("/{userId}")
    public ResponseEntity<HashMap<String, Object>> getAllGoals(@PathVariable Long userId) {
        return getResponseFormat(HttpStatus.OK, "All goals Details", goalsService.getListOfCurrentGoalsByCategory(userId));
    }

    @PostMapping("/{userId}")
    public ResponseEntity<HashMap<String, Object>> createAndUpdateGoalsByCategory(
            @RequestBody CategoryGoalsDto categoryGoalsDto,
            @PathVariable Long userId) {
        return getResponseFormat(HttpStatus.OK,
                "Goals Data Updated",
                goalsService.createOrUpdateCategoryGoalForUser(userId, categoryGoalsDto));
    }

    @PostMapping({"/category/{userId}"})
    public ResponseEntity<HashMap<String, Object>> getGoalDetailsByCategory(@RequestBody JsonNode body, @PathVariable Long userId) {
        return getResponseFormat(HttpStatus.OK, "Goal Details Found", goalsService.getAllCategoryGoalDetails(userId, body.get("category").asText()));
    }

    @PostMapping({"/record/{userId}"})
    public ResponseEntity<HashMap<String, Object>> getGoalRecordDetails(@RequestBody JsonNode category, @PathVariable Long userId) {
        return getResponseFormat(HttpStatus.OK, "ALl Records Found", goalsService.getAllRecordsByCategory(userId, category.get("category").asText()));
    }

    @GetMapping({"/summary/{userId}"})
    public ResponseEntity<HashMap<String, Object>> getSummary(@PathVariable Long userId) {
        return getResponseFormat(HttpStatus.OK, "Overall Summary Found", goalsService.getOverAllSavingPercentage(userId));
    }

    @PostMapping({"/category-summary/{userId}"})
    public ResponseEntity<HashMap<String, Object>> getSummaryPerCategory(@RequestBody JsonNode category, @PathVariable Long userId) {
        return getResponseFormat(HttpStatus.OK, "Category wise Summary Found", goalsService.getSavingPercentagePerCategory(userId, category.get("category").asText()));
    }

    @PatchMapping({"/{userId}"})
    public ResponseEntity<HashMap<String, Object>> updateSavingGoalForUser(@RequestBody JsonNode goalAmount, @PathVariable Long userId) {
        return getResponseFormat(HttpStatus.OK, "Goal Updated Successfully", goalsService.updateSavingGoalForUser(userId, goalAmount.get("goalAmount").asDouble()));
    }

}
