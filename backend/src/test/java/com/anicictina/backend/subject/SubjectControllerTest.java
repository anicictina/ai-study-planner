package com.anicictina.backend.subject;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.anicictina.backend.security.JwtService;
import com.anicictina.backend.subject.dto.SubjectRequest;
import com.anicictina.backend.subject.dto.SubjectResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = SubjectController.class)
@AutoConfigureMockMvc(addFilters = false)
class SubjectControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private SubjectService subjectService;

    @MockitoBean
    private JwtService jwtService;

    @Test
    void createReturns201WithNewSubjectData() throws Exception {
        SubjectRequest request = new SubjectRequest();
        request.setName("Baze podataka");
        request.setCredits(6);
        request.setDifficulty(Level.MEDIUM);
        request.setPriority(Level.HIGH);

        SubjectResponse response = SubjectResponse.builder()
            .id(1L)
            .name("Baze podataka")
            .credits(6)
            .difficulty(Level.MEDIUM)
            .priority(Level.HIGH)
            .knowledgePercent(0)
            .color("#3F51B5")
            .archived(false)
            .createdAt(Instant.now())
            .build();

        when(subjectService.create(any())).thenReturn(response);

        mockMvc.perform(post("/api/subjects")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id", is(1)))
            .andExpect(jsonPath("$.name", is("Baze podataka")));
    }

    @Test
    void createReturns400WhenNameIsMissing() throws Exception {
        SubjectRequest request = new SubjectRequest();
        request.setCredits(6);
        request.setDifficulty(Level.MEDIUM);
        request.setPriority(Level.HIGH);

        mockMvc.perform(post("/api/subjects")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());
    }

    @Test
    void createReturns400WhenCreditsBelowMinimum() throws Exception {
        SubjectRequest request = new SubjectRequest();
        request.setName("Baze podataka");
        request.setCredits(0);
        request.setDifficulty(Level.MEDIUM);
        request.setPriority(Level.HIGH);

        mockMvc.perform(post("/api/subjects")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());
    }

    @Test
    void findOneReturns400WhenIdIsNotNumeric() throws Exception {
        mockMvc.perform(get("/api/subjects/{id}", "abc"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message", is("Nevažeća vrednost parametra: id")));
    }

    @Test
    void createReturns400WhenBodyHasInvalidEnumValue() throws Exception {
        String malformedJson = "{\"name\":\"Baze podataka\",\"credits\":6,"
            + "\"difficulty\":\"NOT_A_REAL_LEVEL\",\"priority\":\"HIGH\"}";

        mockMvc.perform(post("/api/subjects")
                .contentType(MediaType.APPLICATION_JSON)
                .content(malformedJson))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message", is("Telo zahteva nije validno ili sadrži nepoznatu vrednost.")));
    }

    @Test
    void deleteReturns204WithNoBody() throws Exception {
        mockMvc.perform(delete("/api/subjects/{id}", 1L))
            .andExpect(status().isNoContent())
            .andExpect(jsonPath("$").doesNotExist());
    }
}
