package com.fintech.backend.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fintech.backend.dto.TransactionDetailsDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.content.Media;
import org.springframework.ai.google.genai.GoogleGenAiChatOptions;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.InvalidMimeTypeException;
import org.springframework.util.MimeType;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

// 1. Define the Java POJOs for Structured Output
// (These remain unchanged)

/**
 * Schema for TransactionItem
 */
record TransactionItem(
        String productName,
        double totalPrice,
        int quantity,
        double pricePerItem
) {
}

/**
 * Full Schema for TransactionData
 */
record TransactionData(
        String title,
        String category,
        String description,
        List<TransactionItem> transactionItems,
        String paymentMethod,
        double paymentAmount, // Use double or BigDecimal for currency
        String date,          // yyyy-MM-dd
        String time,          // HH:mm:ss
        String status,
        boolean isExpense,
        String transactionId
) {
}

@Slf4j
@Service
public class ReceiptAnalyzerService {

    private final ChatClient chatClient;
    private final ObjectMapper objectMapper;
    private final TransactionsService transactionsService;

    public ReceiptAnalyzerService(ChatClient.Builder chatClientBuilder,
                                  ObjectMapper objectMapper,
                                  TransactionsService transactionsService) {
        this.chatClient = chatClientBuilder.build();
        this.objectMapper = objectMapper;
        this.transactionsService = transactionsService;
    }

    /**
     * Generates structured JSON content from a file's byte data using Spring AI.
     *
     * @param fileData The byte array of the file.
     * @param mimeType The MIME type of the file (e.g., image/jpeg).
     * @return The response body as a JsonNode.
     */
    public JsonNode generateContent(byte[] fileData, String mimeType) {

        // 1. Define the System Prompt
        String systemPrompt = """
                Analyze the provided image, which is a receipt or invoice.
                Extract all relevant transaction details and return them as a JSON object that strictly adheres
                to the following Java class schema:
                
                TransactionData {
                    "title": "A concise title for the transaction",
                    "category": "e.g., 'Groceries', 'Restaurant', 'Electronics'",
                    "description": "A brief summary of the items/service",
                    "transactionItems": "A list of TransactionItem objects",
                    "paymentMethod": "e.g., 'Credit Card', 'Cash'",
                    "paymentAmount": "The total price of the transaction, returned strictly as a numeric value without any currency symbols (e.g., 12.34)"
                    "date": "The date of the transaction with format 'yyyy-MM-dd'",
                    "time": "The time of the transaction with format 'HH:mm:ss'"
                    "status": "e.g., 'COMPLETE', 'PENDING', 'CANCELLED'",
                    "isExpense": "true if the transaction is an expense, false if it is a purchase"
                    "transactionId": "A unique identifier for the transaction use bill no if present"
                }
                
                Be Sure that title doesn't exceed more than 2 words in length
                
                Be sure that category can only be ["FOOD","TRANSPORT","MEDICINE", "GROCERIES", "RENT", "INSURANCE", "SUBSCRIPTIONS", "ENTERTAINMENT"]
                and any transactions the do fall in above mentioned categories should be labeled as "OTHER".
                
                Be sure to accurately determine the productName, totalPrice, quantity, and pricePerItem for
                each item in the 'transactionItems' list. Infer 'isExpense' as true.
                """;
        SystemMessage systemMessage = new SystemMessage(systemPrompt);



        // 2. Create the Media and UserMessage Parts
        // Media is now created from a ByteArrayResource.
        Resource imageResource = new ByteArrayResource(fileData);
        UserMessage userMessage = UserMessage
                .builder()
                .text(systemPrompt)
                .media(new Media(MimeType.valueOf(mimeType), imageResource))
                .build();

        // 3. Execute the Structured Call using the POJO
        // The Prompt must include both SystemMessage and UserMessage.
        TransactionData transactionData = chatClient.prompt(new Prompt(List.of(systemMessage, userMessage)))
                // Use .options() to specify the output model and schema details.
                .options(GoogleGenAiChatOptions.builder()
                        .model("gemini-2.5-flash") // Using a known correct model name
                        .responseMimeType("application/json") // Ensure the model returns JSON
                        .build()) // Map the response JSON directly to the Java POJO
                .call()
                .entity(TransactionData.class); // Map the response JSON directly to the Java POJO

        // 4. Convert the POJO back to a generic JsonNode for the required return type
        return objectMapper.valueToTree(transactionData);
    }

    public JsonNode getResponse(MultipartFile file,Long userId){
        try {
            byte[] fileData = file.getBytes();
            String mineType = file.getContentType();

            if (mineType == null || !mineType.startsWith("image/")) {
                assert mineType != null;
                throw new InvalidMimeTypeException(mineType,"Invalid mime type");
            }

            JsonNode fullResponse = generateContent(fileData,mineType);
            ObjectMapper mapper = new ObjectMapper(); // Navigate to the model's text part
            TransactionDetailsDto transactionDetailsDto = mapper.readValue(fullResponse.toString(), TransactionDetailsDto.class);
            log.info("Dto: {}", transactionDetailsDto);
            Object response = transactionsService.createNewTransaction(userId, transactionDetailsDto);
            return mapper.valueToTree(response);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
