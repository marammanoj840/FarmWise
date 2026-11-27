package com.farmwise.backend.publicapi;

import com.farmwise.backend.admin.ContactInquiryEntity;
import com.farmwise.backend.admin.ContactInquiryRepository;
import com.farmwise.backend.common.MessageResponse;
import com.farmwise.backend.crop.CropRepository;
import com.farmwise.backend.expert.ExpertRepository;
import com.farmwise.backend.marketplace.ProductCategoryRepository;
import com.farmwise.backend.marketplace.ProductRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/public")
@Tag(name = "Public Website", description = "Public platform statistics and contact form persistence")
public class PublicController {

    private final ContactInquiryRepository contactRepository;
    private final CropRepository cropRepository;
    private final ProductCategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final ExpertRepository expertRepository;
    private final Clock utcClock;

    public PublicController(
            ContactInquiryRepository contactRepository,
            CropRepository cropRepository,
            ProductCategoryRepository categoryRepository,
            ProductRepository productRepository,
            ExpertRepository expertRepository,
            Clock utcClock) {
        this.contactRepository = contactRepository;
        this.cropRepository = cropRepository;
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
        this.expertRepository = expertRepository;
        this.utcClock = utcClock;
    }

    public record ContactRequest(
            @NotBlank @Size(min = 2, max = 120) String name,
            @NotBlank @Email @Size(max = 255) String email,
            @Size(max = 32) String phone,
            @Size(max = 64) String roleInterest,
            @NotBlank @Size(min = 3, max = 180) String subject,
            @NotBlank @Size(min = 10, max = 3000) String message) {
    }

    @GetMapping("/overview")
    @Operation(summary = "Get public catalog overview counts")
    public ResponseEntity<Map<String, Object>> overview() {
        return ResponseEntity.ok(Map.of(
                "supportedCrops", cropRepository.count(),
                "productCategories", categoryRepository.count(),
                "approvedProducts", productRepository.countByApprovalStatus("APPROVED"),
                "registeredExperts", expertRepository.count(),
                "defaultCurrency", "INR"));
    }

    @PostMapping("/contact")
    @Operation(summary = "Submit a public contact or partnership inquiry")
    public ResponseEntity<MessageResponse> submitContact(@Valid @RequestBody ContactRequest request) {
        ContactInquiryEntity entity = new ContactInquiryEntity();
        entity.setName(request.name().trim());
        entity.setEmail(request.email().trim().toLowerCase());
        entity.setPhone(request.phone());
        entity.setRoleInterest(request.roleInterest());
        entity.setSubject(request.subject().trim());
        entity.setMessage(request.message().trim());
        entity.setStatus("OPEN");
        contactRepository.save(entity);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new MessageResponse(
                        "Thank you for contacting FarmWise. Your inquiry has been logged for review.",
                        OffsetDateTime.now(utcClock)));
    }
}
