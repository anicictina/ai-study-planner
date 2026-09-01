package com.anicictina.backend.material;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.anicictina.backend.ai.GeminiClient;
import com.anicictina.backend.ai.RawKeyDefinition;
import com.anicictina.backend.ai.RawMaterialSummary;
import com.anicictina.backend.ai.SummaryPromptBuilder;
import com.anicictina.backend.ai.SummaryValidator;
import com.anicictina.backend.common.exception.ResourceNotFoundException;
import com.anicictina.backend.security.CurrentUserProvider;
import com.anicictina.backend.subject.Subject;
import com.anicictina.backend.subject.SubjectRepository;
import com.anicictina.backend.user.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.ValidationException;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

@ExtendWith(MockitoExtension.class)
class MaterialServiceTest {

    @Mock
    private MaterialRepository materialRepository;

    @Mock
    private MaterialSummaryRepository materialSummaryRepository;

    @Mock
    private SubjectRepository subjectRepository;

    @Mock
    private CurrentUserProvider currentUserProvider;

    @Mock
    private SummaryPromptBuilder summaryPromptBuilder;

    @Mock
    private GeminiClient geminiClient;

    @Mock
    private SummaryValidator summaryValidator;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private MaterialService materialService;

    private User currentUser;
    private Subject ownedSubject;

    @BeforeEach
    void setUp() {
        materialService = new MaterialService(
            materialRepository, materialSummaryRepository, subjectRepository,
            currentUserProvider, summaryPromptBuilder, geminiClient, summaryValidator, objectMapper);

        currentUser = User.builder().id(1L).email("owner@example.com").build();
        ownedSubject = Subject.builder().id(3L).user(currentUser).name("Baze podataka").color("#000").build();
    }

    private StudyMaterial materialOwnedBySubject(Subject subject) {
        return StudyMaterial.builder()
            .id(8L)
            .subject(subject)
            .title("Predavanje 1")
            .content("Neki sadrzaj gradiva.")
            .status(MaterialStatus.NOT_STARTED)
            .build();
    }

    @Test
    void findOneThrowsWhenMaterialBelongsToSubjectOfAnotherUser() {
        when(currentUserProvider.getCurrentUser()).thenReturn(currentUser);
        when(materialRepository.findByIdAndSubjectUserId(8L, 1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> materialService.findOne(8L));
        verify(materialRepository).findByIdAndSubjectUserId(8L, 1L);
    }

    @Test
    void uploadFileRejectsUnsupportedExtension() {
        when(currentUserProvider.getCurrentUser()).thenReturn(currentUser);
        when(subjectRepository.findByIdAndUserId(3L, 1L)).thenReturn(Optional.of(ownedSubject));

        MockMultipartFile file = new MockMultipartFile("file", "notes.docx", "text/plain", "sadrzaj".getBytes());

        assertThrows(ValidationException.class, () -> materialService.uploadFile(3L, null, file));
    }

    @Test
    void uploadFileUsesFilenameWithoutExtensionAsDefaultTitle() {
        when(currentUserProvider.getCurrentUser()).thenReturn(currentUser);
        when(subjectRepository.findByIdAndUserId(3L, 1L)).thenReturn(Optional.of(ownedSubject));

        MockMultipartFile file = new MockMultipartFile("file", "Normalizacija.txt", "text/plain", "SQL sadrzaj".getBytes());

        var response = materialService.uploadFile(3L, null, file);

        assertEquals("Normalizacija", response.title());
    }

    @Test
    void uploadFileUsesProvidedTitleWhenGiven() {
        when(currentUserProvider.getCurrentUser()).thenReturn(currentUser);
        when(subjectRepository.findByIdAndUserId(3L, 1L)).thenReturn(Optional.of(ownedSubject));

        MockMultipartFile file = new MockMultipartFile("file", "raw.txt", "text/plain", "sadrzaj".getBytes());

        var response = materialService.uploadFile(3L, "Moj naslov", file);

        assertEquals("Moj naslov", response.title());
    }

    @Test
    void generateSummaryPersistsValidatedSummaryForFirstTime() {
        when(currentUserProvider.getCurrentUser()).thenReturn(currentUser);
        when(materialRepository.findByIdAndSubjectUserId(8L, 1L))
            .thenReturn(Optional.of(materialOwnedBySubject(ownedSubject)));
        when(summaryPromptBuilder.build(anyString(), anyString())).thenReturn("prompt");
        when(summaryPromptBuilder.buildResponseSchema()).thenReturn(java.util.Map.of());
        when(geminiClient.generateJson(anyString(), anyMap())).thenReturn(
            "{\"summaryText\":\"Dovoljno dugacak sazetak gradiva.\","
                + "\"keyTerms\":[\"3NF\"],\"keyDefinitions\":[],\"practiceQuestions\":[]}");

        RawMaterialSummary validated = new RawMaterialSummary(
            "Dovoljno dugacak sazetak gradiva.", List.of("3NF"), List.of(new RawKeyDefinition("t", "d")), List.of());
        when(summaryValidator.validate(any())).thenReturn(validated);
        when(materialSummaryRepository.findByMaterialId(8L)).thenReturn(Optional.empty());

        var response = materialService.generateSummary(8L);

        assertEquals("Dovoljno dugacak sazetak gradiva.", response.summaryText());
        verify(materialSummaryRepository).save(any(MaterialSummary.class));
    }

    @Test
    void getSummaryThrowsWhenNoSummaryGeneratedYet() {
        when(currentUserProvider.getCurrentUser()).thenReturn(currentUser);
        when(materialRepository.findByIdAndSubjectUserId(8L, 1L))
            .thenReturn(Optional.of(materialOwnedBySubject(ownedSubject)));
        when(materialSummaryRepository.findByMaterialId(8L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> materialService.getSummary(8L));
    }
}
