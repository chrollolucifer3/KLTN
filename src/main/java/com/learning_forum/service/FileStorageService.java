package com.learning_forum.service;

import com.learning_forum.domain.STATUS;
import com.learning_forum.dto.request.ApproveOrRejectPostRequest;
import com.learning_forum.dto.request.UploadDocumentRequest;
import com.learning_forum.dto.respone.DocumentFromCategoryResponse;
import com.learning_forum.dto.respone.DocumentResponse;
import com.learning_forum.dto.respone.ListDocumentResponse;
import com.learning_forum.dto.respone.ListDocumentResponseForAdmin;
import com.learning_forum.entity.Document;
import com.learning_forum.exception.AppException;
import com.learning_forum.exception.ErrorCode;
import com.learning_forum.mapper.DocumentMapper;
import com.learning_forum.repository.CategoryRepository;
import com.learning_forum.repository.DocumentRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.apache.commons.io.FilenameUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class FileStorageService {

    DocumentRepository documentRepository;
    DocumentMapper documentMapper;
    CategoryRepository categoryRepository;

    public String storeFile(MultipartFile file) throws IOException {
        String uploadDir = System.getProperty("user.dir") + "/uploads/Image";
        Path uploadPath = Paths.get(uploadDir);

        // Tạo thư mục nếu chưa tồn tại
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
        Path filePath = uploadPath.resolve(fileName);

        file.transferTo(filePath.toFile());

        return "/uploads/Image/" + fileName;
    }

    public String storeDocument(MultipartFile file, UploadDocumentRequest request) throws IOException {
        String uploadDir = System.getProperty("user.dir") + "/uploads/Document";
        Path uploadPath = Paths.get(uploadDir);

        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        if (request.getStatus() == null) {
            request.setStatus(STATUS.PENDING);
        }

        String fileName = request.getFileName();
        if (!fileName.contains(".")) {
            String extension = FilenameUtils.getExtension(file.getOriginalFilename());
            if (!extension.isEmpty()) {
                fileName = fileName + "." + extension;
            }
        }

        String contentType = file.getContentType();
        if (!contentType.startsWith("application/") && !contentType.startsWith("text/")) {
            throw new AppException(ErrorCode.INVALID_FILE_TYPE);
        }

        Path filePath = uploadPath.resolve(fileName);
        int count = 1;
        while (Files.exists(filePath)) {
            String name = FilenameUtils.getBaseName(fileName);
            String extension = FilenameUtils.getExtension(fileName);
            fileName = name + "_" + count + (extension.isEmpty() ? "" : "." + extension);
            filePath = uploadPath.resolve(fileName);
            count++;
        }

        // Tạo Document entity từ mapper
        Document document = documentMapper.toDocument(request, fileName);

        // Lưu entity vào DB
        documentRepository.save(document);

        // Lưu file vật lý
        file.transferTo(filePath.toFile());

        return "/uploads/Document/" + fileName;
    }

    public ListDocumentResponseForAdmin getAllDocuments(int page, int size, String sortBy, String order, String search) {
        log.info("getAllDocuments" + " with page: {}, size: {}, sortBy: {}, order: {}, search: {}", page, size, sortBy, order, search);

        int pageIndex = Math.max(page - 1, 0);
        Sort.Direction direction = Sort.Direction.fromString(order);
        Pageable pageable = PageRequest.of(pageIndex, size, Sort.by(direction, sortBy));

        Specification<Document> spec = getDocumentSpecification(search);
        Page<Document> documents = documentRepository.findAll(spec, pageable);

        List<DocumentResponse> response = documents.getContent()
                .stream()
                .map(documentMapper::toDocumentResponseForAdmin)
                .collect(Collectors.toList());

        return new ListDocumentResponseForAdmin(response, documents.getTotalElements(), documents.getTotalPages(), page, size);
    }

    private static Specification<Document> getDocumentSpecification(String search) {
        Specification<Document> spec = Specification.where(null); // không lọc mặc định gì cả

        if (search != null && !search.trim().isEmpty()) {
            Specification<Document> searchSpec = (root, query, criteriaBuilder) ->
                    criteriaBuilder.like(
                            criteriaBuilder.lower(root.get("fileName")),
                            "%" + search.toLowerCase() + "%"
                    );
            spec = spec.and(searchSpec);
        }

        return spec;
    }

    public ListDocumentResponse getDocumentsByCategory(String id, int page, int size, String sortBy, String order) {
        log.info("getDocumentsByCategory with id: {}, page: {}, size: {}, sortBy: {}, order: {}", id, page, size, sortBy, order);

        int pageIndex = Math.max(page - 1, 0);
        Sort.Direction direction = Sort.Direction.fromString(order);
        Pageable pageable = PageRequest.of(pageIndex, size, Sort.by(direction, sortBy));
        Page<Document> documentPage = documentRepository.findAllByCategoryIdAndStatus(id, STATUS.APPROVED, pageable);

        List<DocumentFromCategoryResponse> documentResponses = documentPage.getContent()
                .stream()
                .map(documentMapper::toDocumentResponse)
                .collect(Collectors.toList());

        var categoryName = categoryRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_FOUND))
                .getName();

        return new ListDocumentResponse(
                categoryName,
                documentResponses,
                documentPage.getTotalElements(),
                documentPage.getTotalPages(),
                page,
                size
        );
    }

    public void approveOrRejectDocument(ApproveOrRejectPostRequest request) {
        log.info("approveOrRejectDocument with request: {}", request);
        Document document = documentRepository.findById(request.getId())
                .orElseThrow(() -> new AppException(ErrorCode.DOCUMENT_NOT_FOUND));
        if (request.getRejectReason() == null) {
            document.setRejectReason(null);
            document.setStatus(STATUS.APPROVED);
        } else {
            document.setStatus(STATUS.REJECTED);
            document.setRejectReason(request.getRejectReason());
        }

        documentRepository.save(document);
    }

    //Lấy số lượng tài liệu được tải lên trong tháng hiện tại
    public int countDocumentsInCurrentMonth() {
        log.info("Counting documents in the current month");
        LocalDate now = LocalDate.now();
        LocalDateTime startOfMonth = now.withDayOfMonth(1).atStartOfDay();
        LocalDateTime endOfMonth = now.withDayOfMonth(now.lengthOfMonth()).atTime(23, 59, 59);

        return documentRepository.countByCreatedAtBetween(startOfMonth, endOfMonth, STATUS.APPROVED);
    }
}
