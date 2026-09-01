package com.anicictina.backend.material;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.anicictina.backend.material.dto.MaterialResponse;
import com.anicictina.backend.security.JwtService;
import jakarta.validation.ValidationException;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = MaterialController.class)
@AutoConfigureMockMvc(addFilters = false)
class MaterialControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MaterialService materialService;

    @MockitoBean
    private JwtService jwtService;

    @Test
    void uploadFileReturns201WithCreatedMaterial() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "notes.txt", "text/plain", "sadrzaj gradiva".getBytes());

        MaterialResponse response = MaterialResponse.builder()
            .id(1L)
            .subjectId(3L)
            .subjectName("Baze podataka")
            .title("notes")
            .content("sadrzaj gradiva")
            .status(MaterialStatus.NOT_STARTED)
            .createdAt(Instant.now())
            .build();

        when(materialService.uploadFile(eq(3L), any(), any())).thenReturn(response);

        mockMvc.perform(multipart("/api/materials/upload")
                .file(file)
                .param("subjectId", "3"))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.title", is("notes")));
    }

    @Test
    void uploadFileReturns400WhenFilePartIsMissing() throws Exception {
        mockMvc.perform(multipart("/api/materials/upload")
                .param("subjectId", "3"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message", is("Nedostaje obavezan deo zahteva: file")));
    }

    @Test
    void uploadFileReturns400WhenServiceRejectsUnsupportedFileType() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "notes.docx", "application/msword", "sadrzaj".getBytes());

        when(materialService.uploadFile(eq(3L), any(), any()))
            .thenThrow(new ValidationException("Podržani su samo PDF i TXT fajlovi."));

        mockMvc.perform(multipart("/api/materials/upload")
                .file(file)
                .param("subjectId", "3"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message", is("Podržani su samo PDF i TXT fajlovi.")));
    }
}
