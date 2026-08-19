package org.example.sddinventory.integration;

import org.example.sddinventory.entity.Category;
import org.example.sddinventory.model.CreateCategoryRequestDTO;
import org.example.sddinventory.model.RenameCategoryRequestDTO;
import org.example.sddinventory.repository.CategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import com.fasterxml.jackson.databind.ObjectMapper;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:application-test.properties")
public class CategoryIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private Long userId;

    @BeforeEach
    public void setUp() {
        categoryRepository.deleteAll();
        userId = 1L;
    }

    @Test
    @WithMockUser(username = "test@example.com")
    public void testCreateCategoryHappyPath() throws Exception {
        CreateCategoryRequestDTO request = new CreateCategoryRequestDTO("Electronics");

        mockMvc.perform(post("/api/categories")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.name").value("Electronics"))
            .andExpect(jsonPath("$.itemCount").value(0));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    public void testCreateDuplicateCategoryRejected() throws Exception {
        CreateCategoryRequestDTO request = new CreateCategoryRequestDTO("Electronics");

        mockMvc.perform(post("/api/categories")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated());

        mockMvc.perform(post("/api/categories")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value("CATEGORY_NAME_NOT_UNIQUE"));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    public void testRenameCategoryHappyPath() throws Exception {
        CreateCategoryRequestDTO createRequest = new CreateCategoryRequestDTO("Electronics");
        String createResponse = mockMvc.perform(post("/api/categories")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(createRequest)))
            .andExpect(status().isCreated())
            .andReturn()
            .getResponse()
            .getContentAsString();

        String categoryId = objectMapper.readTree(createResponse).get("id").asText();

        RenameCategoryRequestDTO renameRequest = new RenameCategoryRequestDTO("Tools");
        mockMvc.perform(patch("/api/categories/" + categoryId)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(renameRequest)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("Tools"));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    public void testListCategoriesHappyPath() throws Exception {
        CreateCategoryRequestDTO request1 = new CreateCategoryRequestDTO("Electronics");
        CreateCategoryRequestDTO request2 = new CreateCategoryRequestDTO("Tools");

        mockMvc.perform(post("/api/categories")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request1)))
            .andExpect(status().isCreated());

        mockMvc.perform(post("/api/categories")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request2)))
            .andExpect(status().isCreated());

        mockMvc.perform(get("/api/categories"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(2));
    }
}
